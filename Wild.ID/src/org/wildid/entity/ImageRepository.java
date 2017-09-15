/*
Copyright (c) 2007 The Regents of the University of California

Permission to use, copy, modify, and distribute this software and its documentation
for educational, research and non-profit purposes, without fee, and without a written
agreement is hereby granted, provided that the above copyright notice, this
paragraph and the following three paragraphs appear in all copies.

Permission to make commercial use of this software may be obtained
by contacting:
Technology Transfer Office
9500 Gilman Drive, Mail Code 0910
University of California
La Jolla, CA 92093-0910
(858) 534-5815
invent@ucsd.edu

THIS SOFTWARE IS PROVIDED BY THE REGENTS OF THE UNIVERSITY OF CALIFORNIA AND
CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.wildid.entity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.wildid.app.ExifTool;
import org.wildid.app.ExifTool.Feature;
import org.wildid.app.LoadDeploymentTask;
import org.wildid.app.Util;
import org.wildid.app.WildID;
import org.wildid.app.exception.ImageFromDifferentCameraException;
import org.wildid.app.exception.DuplicateImageException;
import org.wildid.app.exception.ImageCaptureDateException;
import org.wildid.service.CameraModelService;
import org.wildid.service.CameraModelServiceImpl;
import org.wildid.service.CameraService;
import org.wildid.service.CameraServiceImpl;
import org.wildid.service.DeploymentService;
import org.wildid.service.DeploymentServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ImageRepository {

    final private static File IMAGE_DIR = Paths.get("").toFile();
    final private static int SEQUENCE_TIME_INTERVAL_MS = 30000;    //30 seconds
    final private static DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private static Logger log = Logger.getLogger(ImageRepository.class.getName());

    public static List<Image> loadImages(List<ImageFeature> imageFeatures, Deployment deployment, File sourceFolder, LoadDeploymentTask task)
            throws IOException, DuplicateImageException, ImageFromDifferentCameraException, ImageCaptureDateException {

        File destFolder = getFolder(deployment);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        // load crash mapping
        Map<String, Boolean> crashMap = new HashMap<>();
        getCrashMapping(sourceFolder, crashMap);

        ExifTool tool = new ExifTool(Feature.STAY_OPEN);
        return loadImages(imageFeatures, sourceFolder, destFolder, deployment, task, tool, crashMap);
    }

    public static File getFolder(Deployment deployment) {

        Event event = deployment.getEvent();
        Project project = event.getProject();
        CameraTrap trap = deployment.getCameraTrap();
        CameraTrapArray array = trap.getCameraTrapArray();

        // create destination folder
        File destFolder = new File(
                IMAGE_DIR.getAbsolutePath() + File.separatorChar
                + "images" + File.separatorChar
                + project.getProjectId() + File.separatorChar
                + event.getEventId() + File.separatorChar
                + array.getCameraTrapArrayId() + File.separatorChar
                + trap.getCameraTrapId());

        return destFolder;
    }

    public static File getExportImageDirectory(Deployment deployment, File parentDir) {
        return new File(parentDir.getAbsolutePath() + File.separatorChar + Util.getObjectId(deployment));
    }

    public static File getThumbFolder(Deployment deployment) {

        Event event = deployment.getEvent();
        Project project = event.getProject();
        CameraTrap trap = deployment.getCameraTrap();
        CameraTrapArray array = trap.getCameraTrapArray();

        // create destination folder
        File destFolder = new File(
                IMAGE_DIR.getAbsolutePath() + File.separatorChar
                + "images" + File.separatorChar
                + "thumbnails" + File.separatorChar
                + project.getProjectId() + File.separatorChar
                + event.getEventId() + File.separatorChar
                + array.getCameraTrapArrayId() + File.separatorChar
                + trap.getCameraTrapId());

        return destFolder;
    }

    private static List<Image> loadImages(
            List<ImageFeature> imageFeatures,
            File sourceFolder,
            File destFolder,
            Deployment deployment,
            LoadDeploymentTask task,
            ExifTool tool,
            Map<String, Boolean> crashMap)
            throws IOException, DuplicateImageException, ImageFromDifferentCameraException, ImageCaptureDateException {

        List<Image> images = new ArrayList<>();

        // load exif mapping
        CameraModel cameraModel = deployment.getCamera().getCameraModel();
        List<CameraModelExifFeature> exifFeatures = getCameraModelExifFeatures(cameraModel, imageFeatures);

        String[] filenames = sourceFolder.list();
        CopyOption[] options = new CopyOption[]{
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES
        };

        boolean first = true;
        String cameraMakerName = null;
        String cameraModelName = null;
        String cameraSerialNumber = null;

        ImageSequence sequence = null;
        Date last_image_date = new Date(0);
        Date start_sequence_date = new Date(0);
        boolean start_new_sequence;
        int sequence_group_time_interval_ms = WildID.preference.getTimeGroupInterval() * 60000;

        boolean hasDuplicate = false;
        for (String filename : filenames) {
            Boolean crashed = crashMap.get(filename);
            if (crashed != null && crashed) {
                hasDuplicate = true;
                break;
            }
        }

        int total_jpgs = 0;
        for (File file : sourceFolder.listFiles()) {
            if (file.isFile() && !file.isHidden() && Util.isJpgFilename(file)) {
                total_jpgs++;
            }
        }

        int count_jpg = 0;

        for (String filename : filenames) {
            String newFilename;
            if (hasDuplicate) {
                newFilename = sourceFolder.getName() + "_" + filename;
            } else {
                newFilename = filename;
            }

            File srcFile = new File(sourceFolder, filename);
            File dstFile = new File(destFolder, newFilename);
            Path from = srcFile.toPath();
            Path to = dstFile.toPath();

            if (srcFile.isHidden()) {
                log.error("Ignore hidden file: " + dstFile.getName());
                continue;
            }

            if (Util.isJpgFilename(srcFile)) {

                if (dstFile.exists()) {
                    removeDeploymentFolder(destFolder);
                    log.error("DuplicateImageException: " + dstFile.getName());
                    throw new DuplicateImageException(dstFile.getName());
                }

                // update the progressbar
                count_jpg++;
                task.finished(sourceFolder.getName(), srcFile.getName(), count_jpg, total_jpgs);

                // copy the file
                Files.copy(from, to, options);

                // set sequence
                //ImageSequence sequence = new ImageSequence();
                //sequence.setDeployment(deployment);
                // set image
                Image image = new Image();
                image.setImageSequence(sequence);
                image.setRawName(newFilename);
                image.setSystemName(deployment.getEvent().getName() + "_" + deployment.getCameraTrap().getName() + "_" + newFilename);

                HashMap<ImageFeature, String> feature2values = getImageFeatures(exifFeatures, tool, srcFile);
                String dateStr = null;
                Set<ImageExif> exifs = new HashSet<>();

                for (ImageFeature feature : feature2values.keySet()) {
                    if (feature.getName().equals("Capture Time")) {
                        dateStr = feature2values.get(feature);
                    } else if (feature.getName().equals("Camera Maker")) {
                        if (first) {
                            cameraMakerName = feature2values.get(feature);
                        } else if (!isSame(cameraMakerName, feature2values.get(feature))) {
                            removeDeploymentFolder(destFolder);
                            throw new ImageFromDifferentCameraException(srcFile.getName());
                        }
                    } else if (feature.getName().equals("Camera Model Name")) {
                        if (first) {
                            cameraModelName = feature2values.get(feature);
                        } else if (!isSame(cameraModelName, feature2values.get(feature))) {
                            removeDeploymentFolder(destFolder);
                            throw new ImageFromDifferentCameraException(srcFile.getName());
                        }
                    } else if (feature.getName().equals("Serial Number")) {
                        if (first) {
                            cameraSerialNumber = feature2values.get(feature);
                        } else if (!isSame(cameraSerialNumber, feature2values.get(feature))) {
                            removeDeploymentFolder(destFolder);
                            throw new ImageFromDifferentCameraException(srcFile.getName());
                        }
                    }

                    String exifValue = feature2values.get(feature);
                    if (exifValue != null) {
                        ImageExif exif = new ImageExif();
                        exif.setImage(image);
                        exif.setImageFeature(feature);
                        exif.setExifTagValue(exifValue);
                        exifs.add(exif);
                    }
                }

                if (first) {
                    first = false;
                }

                Date current_image_date = null;
                try {
                    current_image_date = dateFormat.parse(dateStr);

                    long diff = current_image_date.getTime() - last_image_date.getTime();
                    long group_diff = current_image_date.getTime() - start_sequence_date.getTime();

                    diff = Math.abs(diff);
                    group_diff = Math.abs(group_diff);

                    start_new_sequence = (diff > SEQUENCE_TIME_INTERVAL_MS && group_diff > sequence_group_time_interval_ms);

                } catch (Exception ex) {
                    removeDeploymentFolder(destFolder);
                    throw new ImageCaptureDateException(srcFile.getName());
                }

                last_image_date = current_image_date;
                image.setTimeCaptured(current_image_date);
                image.setImageExifs(exifs);
                images.add(image);

                if (start_new_sequence || sequence == null) {
                    sequence = new ImageSequence();
                    sequence.setDeployment(deployment);
                    deployment.getImageSequences().add(sequence);
                    start_sequence_date = current_image_date;
                }

                sequence.getImages().add(image);
                image.setImageSequence(sequence);

            } else if (srcFile.isDirectory()) {
                images.addAll(loadImages(imageFeatures, srcFile, destFolder, deployment, task, tool, crashMap));
            }
        }

        // Optional correction, ignore if error
        try {
            if (!images.isEmpty() && cameraModelName != null && cameraMakerName != null && cameraSerialNumber != null) {

                if (!isSame(cameraMakerName, cameraModel.getMaker())
                        || !isSame(cameraModelName, cameraModel.getName())) {

                    CameraModelService cms = new CameraModelServiceImpl();

                    // Make sure new maker name has equalsIgnoreCase with current makers
                    List<String> makers = cms.getCameraMakerNames();
                    for (String maker : makers) {
                        if (maker.equalsIgnoreCase(cameraMakerName)) {
                            cameraMakerName = maker;
                            break;
                        }
                    }

                    CameraModel newCameraModel = cms.getCameraModel(cameraModelName, cameraMakerName);
                    if (newCameraModel == null) {
                        newCameraModel = new CameraModel(cameraModelName, cameraMakerName);
                        cms.addCameraModel(newCameraModel);
                    }

                    Project project = deployment.getEvent().getProject();
                    CameraService cs = new CameraServiceImpl();

                    Camera cameraFound = null;

                    Set<Camera> cams = project.getCameras();
                    if (cams != null && !cams.isEmpty()) {
                        for (Camera c : cams) {
                            if (c.getSerialNumber().equals(cameraSerialNumber)
                                    && c.getCameraModel().getCameraModelId().equals(newCameraModel.getCameraModelId())) {
                                cameraFound = c;
                                break;
                            }
                        }
                    }

                    log.info("cameraExist: " + cameraFound);

                    if (cameraFound == null) {
                        Camera selectedCamera = deployment.getCamera();
                        selectedCamera.setCameraModel(newCameraModel);
                        selectedCamera.setSerialNumber(cameraSerialNumber);
                        cs.updateCamera(selectedCamera);
                        log.info("Update Camera from " + selectedCamera.getSerialNumber() + " to " + cameraSerialNumber);
                    } else {
                        DeploymentService ds = new DeploymentServiceImpl();
                        List<Deployment> depls = ds.listDeployment();

                        boolean error = false;
                        for (Deployment depl : depls) {
                            if (depl.getEvent().getEventId().equals(deployment.getEvent().getEventId())) {
                                if (depl.getCamera().getCameraId().equals(cameraFound.getCameraId())
                                        && depl.getCameraTrap().getCameraTrapArray().getCameraTrapArrayId().equals(deployment.getCameraTrap().getCameraTrapArray().getCameraTrapArrayId())) {
                                    log.info("Should be error ");
                                    error = true;
                                }
                            }
                        }

                        if (!error) {
                            deployment.setCamera(cameraFound);
                            log.info("Use found Camera " + cameraFound.getSerialNumber());
                        } else {
                            log.info("Camera in used in same array, do nothing");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error updating Camera Information: " + ex.getLocalizedMessage());
        }

        int thumb_width = 150;
        log.info("Creating thumbnail");

        if (!images.isEmpty()) {
            int count = 0;
            for (Image image : images) {
                count++;
                task.finished_thumbnail(sourceFolder.getName(), image.getRawName(), count, total_jpgs);

                try {
                    File thumbFile = getThumbFile(image);
                    if (!thumbFile.exists()) {
                        // create a thumbnail for the first time                    
                        File thumbFolder = thumbFile.getParentFile();

                        if (!thumbFolder.exists()) {
                            thumbFolder.mkdirs();
                        }

                        BufferedImage bufferedImage = ImageIO.read(ImageRepository.getFile(image));
                        BufferedImage t_bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, thumb_width, Scalr.OP_ANTIALIAS);
                        ImageIO.write(t_bufferedImage, "jpg", thumbFile);
                        bufferedImage.flush();
                        t_bufferedImage.flush();
                    }
                } catch (Exception ex) {
                    log.error("Error generate thumbnail: " + image.getRawName() + "\n" + ex.getMessage());
                }
            }
        }

        return images;
    }

    public static String checkDuplicateFileName(File sourceFolder) {
        Set<String> fileNameSet = new HashSet<>();
        return checkDuplicateFileName(sourceFolder, fileNameSet);
    }

    public static String checkDuplicateFileName(File sourceFolder, Set<String> fileNameSet) {
        String[] filenames = sourceFolder.list();

        for (String filename : filenames) {
            File srcFile = new File(sourceFolder, filename);

            if (srcFile.isDirectory()) {
                String dupName = checkDuplicateFileName(srcFile, fileNameSet);
                if (dupName != null) {
                    return dupName;
                }
            } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                if (!fileNameSet.add(sourceFolder.getName() + "_" + filename)) {
                    log.error("Duplicate image name: " + filename);
                    return filename;
                }
            }
        }
        return null;
    }

    public static void removeDeployment(Deployment deployment) {

        File destFolder = getFolder(deployment);

        if (destFolder.exists()) {
            Path arrayPath = destFolder.toPath().getParent();
            Path eventPath = arrayPath.getParent();
            Path projectPath = eventPath.getParent();

            // delete all image files
            String[] filenames = destFolder.list();

            for (String filename : filenames) {
                File file = new File(destFolder, filename);
                file.delete();
            }

            // delete trap folder
            destFolder.delete();

            // delete array folder
            if (arrayPath.toFile().list().length == 0) {
                arrayPath.toFile().delete();
            }

            // delete event folder
            if (eventPath.toFile().list().length == 0) {
                eventPath.toFile().delete();
            }

            // delete project folder
            if (projectPath.toFile().list().length == 0) {
                projectPath.toFile().delete();
            }
        }

        // Also delete thumbnails folder, if existed
        File destThumbFolder = getThumbFolder(deployment);
        if (destThumbFolder.exists()) {
            Path arrayThumbPath = destThumbFolder.toPath().getParent();
            Path eventThumbPath = arrayThumbPath.getParent();
            Path projectThumbPath = eventThumbPath.getParent();

            // delete all image files            
            String[] filenames = destThumbFolder.list();

            for (String filename : filenames) {
                File file = new File(destThumbFolder, filename);
                file.delete();
            }

            // delete trap folder
            destThumbFolder.delete();

            // delete array folder
            if (arrayThumbPath.toFile().list().length == 0) {
                arrayThumbPath.toFile().delete();
            }

            // delete event folder
            if (eventThumbPath.toFile().list().length == 0) {
                eventThumbPath.toFile().delete();
            }

            // delete project folder            
            if (projectThumbPath.toFile().list().length == 0) {
                projectThumbPath.toFile().delete();
            }
        }
    }

    private static void removeDeploymentFolder(File destFolder) {

        Path arrayPath = destFolder.toPath().getParent();
        Path eventPath = arrayPath.getParent();
        Path projectPath = eventPath.getParent();

        // delete all image files
        String[] filenames = destFolder.list();
        for (String filename : filenames) {
            File file = new File(destFolder, filename);
            file.delete();
        }

        // delete trap folder
        destFolder.delete();

        // delete array folder
        if (arrayPath.toFile().list().length == 0) {
            arrayPath.toFile().delete();
        }

        // delete event folder
        if (eventPath.toFile().list().length == 0) {
            eventPath.toFile().delete();
        }

        // delete project folder
        if (projectPath.toFile().list().length == 0) {
            projectPath.toFile().delete();
        }
    }

    public static void updateDeployment(Deployment deployment, File oldFolder) throws IOException {

        File newFolder = getFolder(deployment);

        if (newFolder.equals(oldFolder)) {
            return;
        }

        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }

        CopyOption[] options = new CopyOption[]{
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES
        };

        for (String filename : oldFolder.list()) {
            File srcFile = new File(oldFolder, filename);
            File dstFile = new File(newFolder, filename);
            Path from = srcFile.toPath();
            Path to = dstFile.toPath();
            Files.copy(from, to, options);
            srcFile.delete();
        }

        Path arrayPath = oldFolder.toPath().getParent();
        Path eventPath = arrayPath.getParent();

        oldFolder.delete();

        if (arrayPath.toFile().list().length == 0) {
            // delete array folder
            arrayPath.toFile().delete();
        }

        if (eventPath.toFile().list().length == 0) {
            // delete event folder
            eventPath.toFile().delete();
        }
    }

    public static File getFile(Image image) {
        return new File(getFolder(image.getImageSequence().getDeployment()), image.getRawName());
    }

    public static File getThumbFile(Image image) {
        return new File(getThumbFolder(image.getImageSequence().getDeployment()), "t_" + image.getRawName());
    }

    private static List<CameraModelExifFeature> getCameraModelExifFeatures(CameraModel cameraModel, List<ImageFeature> imageFeatures) {

        List<CameraModelExifFeature> exifFeatures = new ArrayList<>();
        if (!cameraModel.getCameraModelExifFeatures().isEmpty()) {
            for (CameraModelExifFeature exifFeature : cameraModel.getCameraModelExifFeatures()) {
                if (exifFeature.getImageFeature() != null) {
                    exifFeatures.add(exifFeature);
                }
            }
        } else {
            for (ImageFeature imageFeature : imageFeatures) {
                CameraModelExifFeature exifFeature = new CameraModelExifFeature();
                exifFeature.setImageFeature(imageFeature);
                exifFeature.setExifTagName(imageFeature.getDefaultExifTagName());
                exifFeatures.add(exifFeature);
            }
        }
        return exifFeatures;
    }

    private static List<CameraModelExifFeature> loadExifFromImage(ExifTool tool, File file) throws IOException {

        List<String> tagNames = new ArrayList<>();
        Map<String, String> tagValues = new HashMap<>();
        Map<String, String> specialTagValues = new HashMap<>();
        tagValues.putAll(tool.getAllImageMeta(file, ExifTool.Format.NUMERIC, tagNames, specialTagValues, ExifTool.Tag.ALL));

        List<CameraModelExifFeature> modelFeatures = new ArrayList<>();
        for (String tag : tagNames) {
            if (tagValues.get(tag) != null) {
                CameraModelExifFeature modelFeature = new CameraModelExifFeature();
                modelFeature.setExifTagName(tag);
                modelFeature.setExifTagValue(tagValues.get(tag));
                modelFeatures.add(modelFeature);

            } else {
                String value = specialTagValues.get(tag);
                Pattern r = Pattern.compile("[a-zA-Z]\\w+:");
                Matcher m = r.matcher(value);

                List<String> tags = new ArrayList<>();
                List<Integer> locs = new ArrayList<>();
                Set<Character> delimits = new HashSet<>();
                LinkedHashMap<String, String> tag2val = new LinkedHashMap<>();
                while (m.find()) {
                    tags.add(m.group(0));
                    locs.add(m.start());
                }

                for (int i = 0; i < tags.size(); i++) {
                    String sTag = tags.get(i);
                    int sLoc = locs.get(i);
                    if (i + 1 < locs.size()) {
                        int nLoc = locs.get(i + 1);
                        String sVal = value.substring(sLoc + sTag.length(), nLoc);
                        char c = sVal.charAt(sVal.length() - 1);
                        tag2val.put(sTag, sVal.substring(0, sVal.length() - 1).trim());
                        delimits.add(c);
                    }
                }

                if (delimits.size() == 1) {
                    // parsing is okay
                    for (String sTag : tag2val.keySet()) {
                        String sVal = tag2val.get(sTag);
                        CameraModelExifFeature modelFeature = new CameraModelExifFeature();
                        modelFeature.setExifTagName(tag);
                        modelFeature.setExifTagValue(sVal);
                        modelFeature.setSecondaryTagName(sTag.substring(0, sTag.length() - 1));
                        modelFeature.setSecondaryTagDelimit(delimits.iterator().next().toString());
                        modelFeatures.add(modelFeature);
                    }
                } else {
                    // parsing failed
                    CameraModelExifFeature modelFeature = new CameraModelExifFeature();
                    modelFeature.setExifTagName(tag);
                    modelFeature.setExifTagValue(value);
                    modelFeatures.add(modelFeature);
                }
            }
        }
        return modelFeatures;
    }

    private static LinkedHashMap<ImageFeature, String> getImageFeatures(List<CameraModelExifFeature> exifFeatures, ExifTool tool, File file) throws IOException {

        List<CameraModelExifFeature> exifValues = loadExifFromImage(tool, file);

        LinkedHashMap<ImageFeature, String> result = new LinkedHashMap<>();
        for (CameraModelExifFeature feature : exifFeatures) {
            String tag = getTag(feature);

            for (CameraModelExifFeature value : exifValues) {
                String valueTag = getTag(value);
                if (tag.equals(valueTag)) {
                    result.put(feature.getImageFeature(), value.getExifTagValue());
                }
            }
        }
        return result;
    }

    private static String getTag(CameraModelExifFeature feature) {
        String tag = feature.getExifTagName();
        String stag = feature.getSecondaryTagName();
        if (stag != null) {
            tag += "." + stag;
        }
        return tag;
    }

    private static boolean isSame(String s1, String s2) {
        return s1 == null || s2 == null || s1.equalsIgnoreCase(s2);
    }

    private static void getCrashMapping(File sourceFolder, Map<String, Boolean> map) {

        if (sourceFolder.isDirectory()) {
            String[] filenames = sourceFolder.list();
            for (String filename : filenames) {
                File file = new File(sourceFolder, filename);
                if (file.isDirectory()) {
                    getCrashMapping(file, map);
                } else {
                    Boolean b = map.get(filename);
                    if (b == null) {
                        b = false;
                    } else {
                        b = true;
                    }
                    map.put(filename, b);
                }
            }
        }
    }

}
