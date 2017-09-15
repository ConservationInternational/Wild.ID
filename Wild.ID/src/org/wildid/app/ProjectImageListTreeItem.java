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

import java.util.List;
import java.util.TreeMap;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.CameraTrapArrayComparator;
import org.wildid.entity.CameraTrapComparator;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.EventComparator;
import org.wildid.entity.Project;
import org.wildid.service.ProjectService;
import org.wildid.service.ProjectServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectImageListTreeItem extends TreeItem implements LanguageChangable {

    private boolean hasLoadedChildren = false;

    public ProjectImageListTreeItem(LanguageModel language) {
        super(language.getString("tree_images"), new ImageView(new Image("resources/icons/image.png")));
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.setValue(language.getString("tree_images"));
    }

    public Project getProject() {
        ProjectTreeItem projectTreeItem = (ProjectTreeItem) this.getParent();
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

        ProjectService projectService = new ProjectServiceImpl();
        List<Deployment> deployments = projectService.getDeployments(getProject());

        TreeMap<Event, TreeMap<CameraTrapArray, TreeMap<CameraTrap, Deployment>>> event2trap
                = new TreeMap<>(new EventComparator());

        for (Deployment deployment : deployments) {

            Event event = deployment.getEvent();
            CameraTrap trap = deployment.getCameraTrap();
            CameraTrapArray array = trap.getCameraTrapArray();

            TreeMap<CameraTrapArray, TreeMap<CameraTrap, Deployment>> array2trap = event2trap.get(event);
            if (array2trap == null) {
                array2trap = new TreeMap<>(new CameraTrapArrayComparator());
            }

            TreeMap<CameraTrap, Deployment> trap2deployment = array2trap.get(array);
            if (trap2deployment == null) {
                trap2deployment = new TreeMap<>(new CameraTrapComparator());
            }

            trap2deployment.put(trap, deployment);
            array2trap.put(array, trap2deployment);
            event2trap.put(event, array2trap);

        }

        for (Event event : event2trap.keySet()) {
            ProjectImageEventTreeItem eventTreeItem = new ProjectImageEventTreeItem(event);
            super.getChildren().add(eventTreeItem);

            TreeMap<CameraTrapArray, TreeMap<CameraTrap, Deployment>> array2trap = event2trap.get(event);
            for (CameraTrapArray array : array2trap.keySet()) {
                ProjectImageEventArrayTreeItem arrayTreeItem = new ProjectImageEventArrayTreeItem(event, array);
                eventTreeItem.getChildren().add(arrayTreeItem);

                TreeMap<CameraTrap, Deployment> trap2deployment = array2trap.get(array);
                for (CameraTrap trap : trap2deployment.keySet()) {
                    ProjectImageDeploymentTreeItem trapTreeItem
                            = new ProjectImageDeploymentTreeItem(trap2deployment.get(trap));
                    arrayTreeItem.getChildren().add(trapTreeItem);
                }
            }
        }
    }

}
