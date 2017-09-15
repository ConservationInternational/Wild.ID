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
import java.util.List;
import org.wildid.app.LoadDeploymentTask;
import org.wildid.app.exception.ImageFromDifferentCameraException;
import org.wildid.app.exception.DuplicateImageException;
import org.wildid.app.exception.ImageCaptureDateException;
import org.wildid.entity.Deployment;
import org.wildid.entity.Image;
import org.wildid.entity.ImageFeature;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public interface DeploymentService {

    public void addDeployment(List<ImageFeature> imageFeatures, Deployment deployment, File sourceFolder, LoadDeploymentTask task)
            throws IOException, DuplicateImageException, ImageFromDifferentCameraException, ImageCaptureDateException;

    public void addDeployment(Deployment deployment, List<Image> images);

    public List<Deployment> listDeployment();

    public void removeDeployment(Deployment deployment);

    public void updateDeployment(Deployment deployment, File oldFolder) throws IOException;

    public List<Image> getImages(Deployment deployment);

    public boolean hasDeloymentWithName(String deploymentName);

    public boolean hasAnotherDeloymentWithName(String deploymentName, Deployment deployment);
}
