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
import org.wildid.app.exception.ImageFromDifferentCameraException;
import org.wildid.app.exception.DuplicateImageException;
import org.wildid.app.exception.ImageCaptureDateException;
import org.wildid.entity.Deployment;
import org.wildid.entity.ImageFeature;
import org.wildid.service.DeploymentService;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class LoadDeploymentTask extends Task<Void> {

    private final Deployment deployment;
    private final File sourceFolder;
    private final DeploymentService deployService;
    private final List<ImageFeature> imageFeatures;
    private final LanguageModel language;
    private String errorHeader;

    public LoadDeploymentTask(
            LanguageModel language,
            List<ImageFeature> imageFeatures,
            Deployment deployment,
            File sourceFolder,
            DeploymentService deployService) {

        this.language = language;
        this.deployment = deployment;
        this.sourceFolder = sourceFolder;
        this.deployService = deployService;
        this.imageFeatures = imageFeatures;

    }

    @Override
    public Void call() throws InterruptedException, IOException {
        updateProgress(0, 100);
        try {
            deployService.addDeployment(imageFeatures, deployment, sourceFolder, this);
        } catch (DuplicateImageException ex) {
            setErrorHeader(language.getString("deployment_load_image_duplicate_image_error"));
            updateMessage(language.getString("deployment_load_image_duplicate_image_error") + " " + ex.getImageName());
            cancel();
        } catch (ImageFromDifferentCameraException ex) {
            setErrorHeader(language.getString("deployment_load_image_from_different_camera_error"));
            updateMessage(language.getString("deployment_load_image_from_different_camera_error") + " " + ex.getImageName());
            cancel();
        } catch (ImageCaptureDateException ex) {
            setErrorHeader(language.getString("deployment_load_image_capture_time_error"));
            updateMessage(language.getString("deployment_load_image_capture_time_error") + " " + ex.getImageName());
            cancel();
        } catch (Exception ex) {
            setErrorHeader(language.getString("deployment_load_image_exception_error"));
            updateMessage(ex.getMessage());
            cancel();
        }

        return null;
    }

    public void setErrorHeader(String errorHeader) {
        this.errorHeader = errorHeader;
    }

    public String getErrorHeader() {
        return this.errorHeader;
    }

    public void status_update(String folder, String msg, int count, int total) {
        updateMessage(language.getString("deployment_load_image_directory") + ": " + folder + "\n" + msg);
        updateProgress(count, total);
    }

    public void finished(String folder, String img_name, int count, int total) {
        status_update(folder, language.getString("deployment_load_image_creating_thumbnail")
                + " (" + count + "/" + total + "): " + img_name, count, total);
    }

    public void finished_thumbnail(String folder, String img_name, int count, int total) {
        status_update(folder, language.getString("deployment_load_image_creating_thumbnail")
                + " (" + count + "/" + total + "): " + img_name, count, total);
    }
}
