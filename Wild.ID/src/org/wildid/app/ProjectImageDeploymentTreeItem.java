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

import java.util.TreeSet;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.wildid.entity.Deployment;
import org.wildid.entity.ImageComparatorByTimestampAndName;
import org.wildid.entity.Project;
import org.wildid.service.DeploymentService;
import org.wildid.service.DeploymentServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectImageDeploymentTreeItem extends TreeItem {

    private boolean hasLoadedChildren = false;
    private Deployment deployment;

    public ProjectImageDeploymentTreeItem(Deployment deployment) {
        super(deployment.getCameraTrap().getName(),
                deployment.getFailureType() == null
                        ? new ImageView(new Image("resources/icons/camera.png"))
                        : new ImageView(new Image("resources/icons/camera_delete.png")));
        this.deployment = deployment;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
        this.setValue(deployment.getCameraTrap().getName());
    }

    public Project getProject() {

        TreeItem arrayItem = this.getParent();
        TreeItem eventItem = arrayItem.getParent();
        TreeItem imageItem = eventItem.getParent();
        TreeItem projectItem = imageItem.getParent();

        ProjectTreeItem projectTreeItem = (ProjectTreeItem) projectItem;
        if (projectTreeItem != null) {
            return projectTreeItem.getProject();
        } else {
            return null;
        }
    }

    @Override
    public ObservableList getChildren() {
        if (hasLoadedChildren == false) {
            loadChildren();
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        if (hasLoadedChildren == false) {
            return false;
        } else {
            return super.getChildren().isEmpty();
        }
    }

    private void loadChildren() {
        hasLoadedChildren = true;

        DeploymentService deploymentService = new DeploymentServiceImpl();
        //List<org.wildid.entity.Image> images = deploymentService.getImages(this.deployment);
        TreeSet<org.wildid.entity.Image> images = new TreeSet<>(new ImageComparatorByTimestampAndName());
        images.addAll(deploymentService.getImages(this.deployment));
        for (org.wildid.entity.Image image : images) {
            ProjectImageTreeItem imageTreeItem = new ProjectImageTreeItem(image);
            this.getChildren().add(imageTreeItem);
        }
    }
}
