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
package org.wildid.app;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javax.imageio.ImageIO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class Util {

    static Logger log = Logger.getLogger(Util.class.getName());

    public static boolean isImage(File file) {
        try {
            InputStream input = new FileInputStream(file);
            ImageIO.read(input).toString();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isJpgFilename(File file) {
        String lowerCaseFileName = file.getName().toLowerCase();
        return lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg");
    }

    public static int countFiles(File directory) {
        int count = 0;
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                count += countFiles(file);
            } else if (!file.isHidden()) {
                count++;
            }
        }
        return count;
    }

    public static int countJpgFiles(File directory) {
        int count = 0;
        if (directory.listFiles() != null) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    count += countJpgFiles(file);
                } else if (!file.isHidden() && isJpgFilename(file)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static boolean hasJpgFile(File directory) {
        if (directory.listFiles() != null) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    if (hasJpgFile(file)) {
                        return true;
                    }
                } else if (!file.isHidden() && isJpgFilename(file)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected static void addEmptyRow(GridPane gridPane, int row) {
        List<Node> children = new LinkedList<>(gridPane.getChildren());
        for (Node node : children) {
            int nodeRow = GridPane.getRowIndex(node);
            if (nodeRow >= row) {
                GridPane.setRowIndex(node, nodeRow + 1);
            }
        }
    }

    protected static void removeRow(GridPane gridPane, int row) {
        List<Node> children = new LinkedList<>(gridPane.getChildren());
        for (Node node : children) {
            int nodeRow = GridPane.getRowIndex(node);
            if (nodeRow == row) {
                gridPane.getChildren().remove(node);
            } else if (nodeRow > row) {
                GridPane.setRowIndex(node, nodeRow - 1);
            }
        }
    }

    public static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {

        // We allowed a failure deplyment has no image, and so no image directory created
        if (!sourceFolder.exists()) {
            return;
        }

        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then copy the file directly to new location
        if (sourceFolder.isDirectory()) {
            //Verify if destinationFolder is already present; If not then create it
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
            }

            //Get all files from source directory
            String files[] = sourceFolder.list();

            //Iterate over all files and copy them to destinationFolder one by one
            for (String file : files) {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);

                //Recursive function call
                copyFolder(srcFile, destFile);
            }
        } else {
            //Copy the file content from one place to another 
            Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static void zipDir(File srcDir, ZipOutputStream out, File destDir) throws IOException {

        File[] files = srcDir.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (File file : files) {
            if (file.isDirectory()) {
                zipDir(file, out, destDir);
                continue;
            }
            FileInputStream in = new FileInputStream(file.getAbsolutePath());

            String zipFilePath = file.getCanonicalPath().substring(destDir.getCanonicalPath().length() + 1,
                    file.getCanonicalPath().length());

            out.putNextEntry(new ZipEntry(zipFilePath));

            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }

    public static void delete(File f) {

        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        f.delete();
    }

    public static String getObjectId(Project project) {
        return spaces2UnderScore(project.getAbbrevName());
    }

    public static String getObjectId(Event event) {
        return getObjectId(event.getProject()) + "_" + spaces2UnderScore(event.getName());
    }

    public static String getObjectId(Event event, CameraTrapArray ctArray) {
        return getObjectId(event) + "_" + spaces2UnderScore(ctArray.getName());
    }

    public static String getObjectId(Event event, CameraTrap trap) {
        return getObjectId(event) + "_" + spaces2UnderScore(trap.getName());
    }

    public static String getObjectId(Deployment deployment) {
        Event event = deployment.getEvent();
        CameraTrap trap = deployment.getCameraTrap();
        return getObjectId(event, trap);
    }

    public static String getObjectId(Project project, Event event, CameraTrapArray ctArray, CameraTrap trap) {
        if (trap != null && event != null) {
            return getObjectId(event, trap);
        } else if (ctArray != null && event != null) {
            return getObjectId(event, ctArray);
        } else if (event != null) {
            return getObjectId(event);
        } else if (project != null) {
            return getObjectId(project);
        } else {
            return "error";
        }
    }

    public static String spaces2UnderScore(String str) {
        //return str.replaceAll("\\s+", "_").replaceAll("\\.+", "_");
        return str.replaceAll("\\s+", "_");
    }

    public static String truncateString(String string, int length) {
        if (string == null || string.trim().isEmpty() || string.length() <= length) {
            return string;
        }

        StringBuilder sb = new StringBuilder(string);
        int actualLength = length - 3;
        if (sb.length() > actualLength) {
            int endIndex = sb.indexOf(" ", actualLength);
            if (endIndex != -1) {
                return sb.insert(endIndex, "...").substring(0, endIndex + 3);
            } else {
                return sb.substring(0, length - 3) + "...";
            }
        }
        return string;
    }

    //generate random UUIDs    
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static boolean isNewerVersion(String oldVersion, String newVersion) {
        boolean isNewer = false;

        try {
            String[] oldTokens = oldVersion.split("\\.");
            String[] newTokens = newVersion.split("\\.");

            if (oldTokens.length == 3 && newTokens.length == 3) {
                int oldMajor = Integer.valueOf(oldTokens[0]);
                int oldMinor = Integer.valueOf(oldTokens[1]);
                int oldBuild = Integer.valueOf(oldTokens[2]);

                int newMajor = Integer.valueOf(newTokens[0]);
                int newMinor = Integer.valueOf(newTokens[1]);
                int newBuild = Integer.valueOf(newTokens[2]);

                if (newMajor > oldMajor
                        || (newMajor == oldMajor && newMinor > oldMinor)
                        || (newMajor == oldMajor && newMinor == oldMinor && newBuild > oldBuild)) {
                    isNewer = true;
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return isNewer;
    }

    public static void updateWildIDProperties(String pkey, String pval) {
        FileInputStream in = null;
        FileOutputStream out = null;

        Properties props = new Properties();
        File prop_file = Paths.get("WildID.properties").toFile();

        try {
            in = new FileInputStream(prop_file);
            props.load(in);
            in.close();

            out = new FileOutputStream(prop_file);
            props.setProperty(pkey, pval);
            props.store(out, null);
            out.close();
        } catch (IOException ex) {
            //ex.printStackTrace();
            log.error(ex.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    log.error(e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    log.error(e.getMessage());
                }
            }
        }
    }

    public static void getProperties() {
        Properties prop = Util.getWildIDProperties();

        String opened_projectStr = prop.getProperty("opened_project");
        Integer opened_project_id = -1;
        String registeredStr = prop.getProperty("registered");
        boolean registered = false;
        String enableImageIndividualStr = prop.getProperty("enable_image_individual");
        boolean enableImageIndividual = false;
        String enableSubspeciesStr = prop.getProperty("enable_subspecies");
        boolean enableSubspecies = false;

        try {
            if (opened_projectStr != null) {
                opened_project_id = Integer.valueOf(opened_projectStr);
            }
            if (registeredStr != null) {
                registered = Boolean.valueOf(registeredStr);
            }
            if (enableImageIndividualStr != null) {
                enableImageIndividual = Boolean.valueOf(enableImageIndividualStr);
            }
            if (enableSubspeciesStr != null) {
                enableSubspecies = Boolean.valueOf(enableSubspeciesStr);
            }

        } catch (Exception ex) {
        }

        WildID.wildIDProperties.setOpenedProject(opened_project_id);
        WildID.wildIDProperties.setRegistered(registered);
        WildID.wildIDProperties.setUUID(prop.getProperty("uuid"));
        WildID.wildIDProperties.setWorkingDir(prop.getProperty("working_dir"));
        WildID.wildIDProperties.setEnableImageIndividual(enableImageIndividual);
        WildID.wildIDProperties.setEnableSubspecies(enableSubspecies);
    }

    private static File generateWildIDProperties() {
        OutputStream output = null;
        Properties prop_out = new Properties();
        File prop_out_file = Paths.get("WildID.properties").toFile();

        try {
            String uuid = generateUUID();

            output = new FileOutputStream(prop_out_file);
            prop_out.setProperty("uuid", uuid);
            prop_out.setProperty("registered", "false");
            prop_out.store(output, null);
        } catch (IOException ex) {
            //ex.printStackTrace();
            log.error(ex.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    log.error(e.getMessage());
                }
            }
        }

        return prop_out_file;
    }

    public static Properties getWildIDProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        File prop_file = Paths.get("WildID.properties").toFile();

        try {
            if (!prop_file.exists()) {
                prop_file = generateWildIDProperties();
            }

            input = new FileInputStream(prop_file);
            prop.load(input);
        } catch (IOException ex) {
            //ex.printStackTrace();
            log.error(ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    log.error(e.getMessage());
                }
            }
        }

        return prop;
    }

    public static final void unzip(File zip, File extractTo) throws IOException {

        ZipFile archive = new ZipFile(zip);
        Enumeration e = archive.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            File file = new File(extractTo, entry.getName());
            if (entry.isDirectory() && !file.exists()) {
                file.mkdirs();
            } else if (entry.isDirectory() && file.exists()) {

            } else {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                InputStream in = archive.getInputStream(entry);
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

                byte[] buffer = new byte[8192];
                int read;

                while (-1 != (read = in.read(buffer))) {
                    out.write(buffer, 0, read);
                }

                in.close();
                out.close();
            }
        }

        archive.close();
    }

    public static File saveUrlToFile(String urlStr, String fileName) {
        try {
            URL jarUrl = new URL(urlStr);
            ReadableByteChannel rbc = Channels.newChannel(jarUrl.openStream());
            FileOutputStream fos = new FileOutputStream("Wild.ID.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            return new File(fileName);
        } catch (MalformedURLException ex) {
            log.error(ex.getMessage());
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }

        return null;
    }

    public static boolean MoveFile(File src, File des, boolean overwrite) {
        try {
            if (des.exists() && !des.isFile()) {
                return false;
            }
            if (des.exists() && des.isFile()) {
                if (overwrite) {
                    des.delete();
                } else {
                    return false;
                }
            }
            return src.renameTo(des);
        } catch (Exception e) {
            //e.printStackTrace();
            log.error(e.getMessage());
            return false;
        }
    }

    public static String tail(File file, int lines) {
        java.io.RandomAccessFile fileHandler = null;
        try {
            fileHandler = new java.io.RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                } else if (readByte == 0xD) {
                    if (filePointer < fileLength - 1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            //e.printStackTrace();
            log.error(e.getMessage());
            return null;
        } catch (java.io.IOException e) {
            //e.printStackTrace();
            log.error(e.getMessage());
            return null;
        } finally {
            if (fileHandler != null) {
                try {
                    fileHandler.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String getTodayLog(String todayLog) {
        String result = "";
        try {
            BufferedReader reader = new BufferedReader(new StringReader(todayLog));
            String line = reader.readLine();

            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = format.format(today);

            boolean started = false;
            while (line != null) {
                if (line.startsWith(dateStr)) {
                    started = true;
                }

                if (started) {
                    result += line + "\n";
                }

                line = reader.readLine();
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            log.error(ex.getMessage());
        }
        return result;
    }

    public static void sendMail(String from, String subject, String message) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(WildID.WILDID_SERVER + "mail/mail.jsp");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("from", from));
            nvps.add(new BasicNameValuePair("subject", subject));
            nvps.add(new BasicNameValuePair("message", URLEncoder.encode(message, "utf-8")));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    public static boolean isOnline() {
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress("www.teamnetwork.org", 8080), 2000);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static void alertInformationPopup(String title, String header, String content, String ok_btn) {
        alertInformationPopup(null, title, header, content, ok_btn);
    }

    public static void alertInformationPopup(Pane pane, String title, String header, String content, String ok_btn) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
        buttonTypes.clear();
        buttonTypes.add(new ButtonType(ok_btn, ButtonBar.ButtonData.OK_DONE));
        if (pane != null) {
            alert.initOwner(pane.getScene().getWindow());
            alert.initModality(Modality.WINDOW_MODAL);
        }
        alert.showAndWait();
    }

    public static void alertErrorPopup(String title, String header, String content, String ok_btn) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
        buttonTypes.clear();
        buttonTypes.add(new ButtonType(ok_btn, ButtonBar.ButtonData.OK_DONE));
        alert.showAndWait();
    }

    public static void alertWarningPopup(String title, String header, String content, String ok_btn) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
        buttonTypes.clear();
        buttonTypes.add(new ButtonType(ok_btn, ButtonBar.ButtonData.OK_DONE));
        alert.showAndWait();
    }

    public static boolean alertConfirmPopup(String title, String header, String content, String ok_btn, String cancel_btn) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
        buttonTypes.clear();
        buttonTypes.add(new ButtonType(ok_btn, ButtonBar.ButtonData.OK_DONE));
        buttonTypes.add(new ButtonType(cancel_btn, ButtonBar.ButtonData.CANCEL_CLOSE));
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData() == ButtonType.OK.getButtonData();
    }

    public static void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean validateEmailAddress(String email) {
        Pattern valid_email_regex = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = valid_email_regex.matcher(email);
        return matcher.find();
    }
}
