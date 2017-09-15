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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipOutputStream;
import javafx.concurrent.Task;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.apache.log4j.Logger;
import static org.wildid.app.SaveTransferFileTask.log;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.EventComparator;
import org.wildid.entity.ImageRepository;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class SaveTransferFileTask extends Task<Void> {

    private double workDone;
    private final double max;
    private final LanguageModel language;
    private final List<Deployment> deployments;
    private final File zipTransferFile;
    //initializing the logger
    static Logger log = Logger.getLogger(SaveTransferFileTask.class.getName());

    public SaveTransferFileTask(LanguageModel language, List<Deployment> deployments, File savedDir) {
        this.language = language;
        this.deployments = deployments;
        this.zipTransferFile = savedDir;
        this.max = 100;
        this.workDone = 0;
    }

    @Override
    protected Void call() throws Exception {

        try {
            updatePercentDone();

            log.info("save Transfer File: " + zipTransferFile.getAbsolutePath());

            finished(language.getString("save_transfer_file_progress_preparing"));

            // get the temp directory 
            File ourAppDir = Paths.get("").toFile();
            File tmpRootDir = new File(ourAppDir.getAbsolutePath() + File.separatorChar + "tmp");
            File tmpDir = new File(tmpRootDir, String.valueOf(new Date().getTime()));
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }

            // make a folder for data
            File dataDir = new File(tmpDir, "data");
            dataDir.mkdir();

            // make a folder for images
            File imageDir = new File(tmpDir, "images");
            imageDir.mkdir();

            workDone = 10.0;
            updatePercentDone();

            //try {
            JAXBContext jc = JAXBContext.newInstance(Deployment.class);
            Marshaller m = jc.createMarshaller();

            int numDeployments = deployments.size();

            for (Deployment deployment : deployments) {
                finished(language.getString("save_transfer_file_progress_copying_deployment_images"));

                // get the image folder of the deployment
                File imageFolder = ImageRepository.getFolder(deployment);

                // create destination folder
                Event evt = deployment.getEvent();
                Project project = evt.getProject();
                CameraTrap trap = deployment.getCameraTrap();
                CameraTrapArray array = trap.getCameraTrapArray();
                File destFolder = new File(
                        imageDir.getAbsolutePath() + File.separatorChar
                        + project.getProjectId() + File.separatorChar
                        + evt.getEventId() + File.separatorChar
                        + array.getCameraTrapArrayId() + File.separatorChar
                        + trap.getCameraTrapId());
                destFolder.mkdirs();

                // copy images
                Util.copyFolder(imageFolder, destFolder);
                workDone += 40.0 / numDeployments;
                updatePercentDone();

                finished(language.getString("save_transfer_file_progress_copying_deployment_metadata"));

                // export the xml file of the deployment
                File xmlFile = new File(dataDir.getAbsolutePath() + File.separatorChar
                        + "Wild_ID_" + Util.getObjectId(evt, trap) + ".xml");
                FileOutputStream fos = new FileOutputStream(xmlFile);

                SortedSet<Event> events = new TreeSet<>(new EventComparator());
                events.addAll(deployment.getEvent().getProject().getEvents());
                deployment.getEvent().getProject().setEvents(new TreeSet<>(new EventComparator()));

                m.marshal(deployment, new OutputStreamWriter(fos, "UTF-8"));
                fos.close();

                deployment.getEvent().getProject().setEvents(events);

                workDone += 10.0 / numDeployments;
                updatePercentDone();
            }

            finished(language.getString("save_transfer_file_progress_compressing_to_zip"));

            // zip the directory to a zip file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipTransferFile));
            Util.zipDir(tmpDir, out, tmpRootDir);
            out.close();

            workDone = 90.0;
            updatePercentDone();

            // delete the tmp directory
            Util.delete(tmpDir);

            workDone = 100.0;
            updatePercentDone();

        } catch (Exception ex) {
            cancel();
            ex.printStackTrace();
            log.error(ex.getMessage());
            throw ex;
        }

        return null;
    }

    public void updatePercentDone() {
        updateProgress(this.workDone, this.max);
    }

    public void increaseDone() {
        this.workDone++;
        updateProgress(this.workDone, max);
    }

    public void finished(String text) {
        updateMessage(text);
    }

}
