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
package org.wildid.service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.wildid.app.LoadDeploymentTask;
import org.wildid.app.exception.ImageFromDifferentCameraException;
import org.wildid.app.exception.DuplicateImageException;
import org.wildid.app.exception.ImageCaptureDateException;
import org.wildid.dao.DeploymentDAO;
import org.wildid.dao.DeploymentDAOImpl;
import org.wildid.entity.Deployment;
import org.wildid.entity.Image;
import org.wildid.entity.ImageComparatorByTimestampAndName;
import org.wildid.entity.ImageFeature;
import org.wildid.entity.ImageRepository;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class DeploymentServiceImpl implements DeploymentService {

    private final DeploymentDAO deploymentDAO = new DeploymentDAOImpl();

    @Override
    public void addDeployment(List<ImageFeature> imageFeatures, Deployment deployment, File sourceFolder, LoadDeploymentTask task)
            throws IOException, DuplicateImageException, ImageFromDifferentCameraException, ImageCaptureDateException {
        List<Image> images = ImageRepository.loadImages(imageFeatures, deployment, sourceFolder, task);

        if (!images.isEmpty()) {
            Collections.sort(images, new ImageComparatorByTimestampAndName());

            Date deployment_startTime = deployment.getStartTime();
            Date deployment_endTime = deployment.getEndTime();
            Date img_capture_startTime = images.get(0).getTimeCaptured();
            Date img_capture_endTime = images.get(images.size() - 1).getTimeCaptured();

            if (deployment_startTime == null || deployment_startTime.after(img_capture_startTime)) {
                deployment.setStartTime(img_capture_startTime);
            }

            if (deployment_endTime == null || deployment_endTime.before(img_capture_endTime)) {
                deployment.setEndTime(img_capture_endTime);
            }

            deploymentDAO.addDeployment(deployment, images);
        }
    }

    @Override
    public void addDeployment(Deployment deployment, List<Image> images) {
        deploymentDAO.addDeployment(deployment, images);
    }

    @Override
    public List<Deployment> listDeployment() {
        return deploymentDAO.listDeployment();
    }

    @Override
    public void removeDeployment(Deployment deployment) {
        ImageRepository.removeDeployment(deployment);
        deploymentDAO.removeDeployment(deployment.getDeploymentId());
    }

    @Override
    public void updateDeployment(Deployment deployment, File oldFolder) throws IOException {
        ImageRepository.updateDeployment(deployment, oldFolder);
        deploymentDAO.updateDeployment(deployment);
    }

    @Override
    public List<Image> getImages(Deployment deployment) {
        return deploymentDAO.getImages(deployment);
    }

    @Override
    public boolean hasDeloymentWithName(String deploymentName) {
        return deploymentDAO.hasDeloymentWithName(deploymentName);
    }
    
    @Override
    public boolean hasAnotherDeloymentWithName(String deploymentName, Deployment deployment) {
        return deploymentDAO.hasAnotherDeloymentWithName(deploymentName, deployment);
    }
}
