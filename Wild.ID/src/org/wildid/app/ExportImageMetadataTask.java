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
import java.io.IOException;
import java.util.List;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.ImageRepository;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ExportImageMetadataTask extends Task<Void> {

    static Logger log = Logger.getLogger(ExportImageMetadataTask.class.getName());
    private double val = 0;
    private final double max = 100;
    private final LanguageModel language;
    private final boolean withImages;
    private final String exportType;

    private final Project project;
    private final Event event;
    private final CameraTrapArray ctArray;
    private final CameraTrap cameraTrap;
    private final List<Deployment> deployments;
    private final File exportDir;
    private final File exportFile;

    public ExportImageMetadataTask(
            LanguageModel language,
            Project project,
            Event event,
            CameraTrapArray ctArray,
            CameraTrap cameraTrap,
            List<Deployment> deployments,
            String exportType,
            File exportDir,
            File exportFile,
            boolean withImages
    ) {

        this.language = language;
        this.project = project;
        this.event = event;
        this.ctArray = ctArray;
        this.cameraTrap = cameraTrap;
        this.deployments = deployments;
        this.exportType = exportType;
        this.exportDir = exportDir;
        this.exportFile = exportFile;
        this.withImages = withImages;
    }

    @Override
    public Void call() {
        updateProgress(10, max);
        finished(language.getString("export_image_task_exporting_metadata"));
        ExportImageMetadata exportMetadata = new ExportImageMetadata(exportFile, project, event, ctArray, cameraTrap);

        if (exportType.equals("csv")) {
            exportMetadata.exportCSV();
        } else if (exportType.equals("excel")) {
            exportMetadata.exportExcel();
        } else {
            exportMetadata.exportWCS(withImages);
        }

        increaseDone(40);

        // Copy deployment images to new export directories 
        try {
            for (Deployment deployment : deployments) {
                finished(language.getString("export_image_task_copying_images") + deployment.getCameraTrap().getName());

                File sourceFolder = ImageRepository.getFolder(deployment);
                File destinationFolder = ImageRepository.getExportImageDirectory(deployment, exportDir);

                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs();
                }

                Util.copyFolder(sourceFolder, destinationFolder);
                increaseDone(50 / (double) deployments.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void increaseDone(double percent) {
        this.val += percent;
        if (this.val > this.max) {
            this.val = this.max;
        }

        updateProgress(this.val, this.max);
    }

    public void finished(String text) {
        updateMessage(text);

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
    }

}
