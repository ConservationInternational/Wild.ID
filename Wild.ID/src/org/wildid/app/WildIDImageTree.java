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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.Image;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDImageTree extends TreeView implements LanguageChangable {

    private LanguageModel language;
    private WildIDController controller;
    private ProjectTreeItem rootItem;

    public WildIDImageTree(
            LanguageModel language,
            ProjectTreeItem rootItem) {

        super(rootItem);
        this.rootItem = rootItem;
        this.rootItem.setExpanded(true);
        this.language = language;

        this.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> p) {
                ExportableTreeCell treecell = new ExportableTreeCell();
                treecell.setWildIDController(controller);
                return treecell;
            }
        });

        this.setStyle("-fx-background-color:gray;");
        this.requestFocus();
        this.getSelectionModel().select(rootItem);
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        setItemLanguage(this.rootItem, language);
    }

    public void setWildIDController(WildIDController controller) {
        if (controller != null) {
            //this.getSelectionModel().selectedItemProperty().addListener(controller);
            this.controller = controller;
        }
    }

    private void setItemLanguage(TreeItem treeItem, LanguageModel language) {

        if (treeItem instanceof LanguageChangable) {
            ((LanguageChangable) treeItem).setLanguage(language);
        }

        treeItem.getChildren().stream().forEach((object) -> {
            setItemLanguage((TreeItem) object, language);
        });

    }

    public void saveImageAnnotation(List<Image> images) {
        for (Image image : images) {
            Deployment deployment = image.getImageSequence().getDeployment();
            ProjectImageDeploymentTreeItem deploymentTreeItem = getProjectImageDeploymentTreeItem(deployment);
            for (Object object : deploymentTreeItem.getChildren()) {
                ProjectImageTreeItem imageTreeItem = (ProjectImageTreeItem) object;
                if (imageTreeItem.getImage().getImageId().intValue() == image.getImageId()) {
                    imageTreeItem.setGraphic(new ImageView(new javafx.scene.image.Image("resources/icons/bullet_green.png")));
                }
            }
        }
    }

    public ProjectImageListTreeItem getImageListTreeItem() {
        return (ProjectImageListTreeItem) rootItem.getChildren().get(0);
    }

    public ProjectImageDeploymentTreeItem getProjectImageDeploymentTreeItem(Deployment deployment) {
        ProjectImageListTreeItem imageTreeItem = getImageListTreeItem();
        return getProjectImageDeploymentTreeItem(imageTreeItem, deployment);
    }

    private ProjectImageDeploymentTreeItem getProjectImageDeploymentTreeItem(ProjectImageListTreeItem imageTreeItem, Deployment deployment) {

        for (Object object : imageTreeItem.getChildren()) {
            ProjectImageEventTreeItem eventTreeItem = (ProjectImageEventTreeItem) object;
            for (Object item : eventTreeItem.getChildren()) {
                ProjectImageEventArrayTreeItem arrayTreeItem = (ProjectImageEventArrayTreeItem) item;
                for (Object obj : arrayTreeItem.getChildren()) {
                    ProjectImageDeploymentTreeItem deploymentTreeItem = (ProjectImageDeploymentTreeItem) obj;
                    Deployment deployItem = deploymentTreeItem.getDeployment();
                    if (deployItem.getDeploymentId().intValue() == deployment.getDeploymentId()) {
                        return deploymentTreeItem;
                    }
                }
            }
        }

        return null;
    }

    public void removeDeployment(Deployment deployment) {

        ProjectImageDeploymentTreeItem deploymentTreeItem = getProjectImageDeploymentTreeItem(deployment);
        ProjectImageEventArrayTreeItem arrayTreeItem = (ProjectImageEventArrayTreeItem) deploymentTreeItem.getParent();
        ProjectImageEventTreeItem eventTreeItem = (ProjectImageEventTreeItem) arrayTreeItem.getParent();
        ProjectImageListTreeItem imageTreeItem = (ProjectImageListTreeItem) eventTreeItem.getParent();

        arrayTreeItem.getChildren().remove(deploymentTreeItem);

        if (arrayTreeItem.getChildren().isEmpty()) {
            eventTreeItem.getChildren().remove(arrayTreeItem);
        }

        if (eventTreeItem.getChildren().isEmpty()) {
            imageTreeItem.getChildren().remove(eventTreeItem);
        }

        this.getSelectionModel().select(imageTreeItem);
    }

    public void createDeployment(Deployment deployment) {

        ProjectImageDeploymentTreeItem deploymentTreeItem = new ProjectImageDeploymentTreeItem(deployment);
        ProjectImageEventArrayTreeItem arrayTreeItem = getOrCreateProjectImageEventArrayTreeItem(deployment.getEvent(), deployment.getCameraTrap().getCameraTrapArray());

        boolean found = false;
        for (Object object : arrayTreeItem.getChildren()) {
            ProjectImageDeploymentTreeItem item = (ProjectImageDeploymentTreeItem) object;
            Deployment itemDeployment = item.getDeployment();
            if (itemDeployment.getDeploymentId().intValue() == deployment.getDeploymentId()) {
                found = true;
                break;
            }
        }

        if (!found) {
            arrayTreeItem.getChildren().add(deploymentTreeItem);
        }

        this.getSelectionModel().select(deploymentTreeItem);
        deploymentTreeItem.setExpanded(true);
    }

    private ProjectImageEventArrayTreeItem getOrCreateProjectImageEventArrayTreeItem(Event event, CameraTrapArray array) {

        ProjectImageListTreeItem imageTreeItem = getImageListTreeItem(event.getProject());
        for (Object object : imageTreeItem.getChildren()) {
            ProjectImageEventTreeItem eventTreeItem = (ProjectImageEventTreeItem) object;
            Event evt = eventTreeItem.getEvent();
            if (evt.getEventId().intValue() == event.getEventId()) {
                // found the event

                // search for the array
                boolean arrayFound = false;
                for (Object item : eventTreeItem.getChildren()) {
                    ProjectImageEventArrayTreeItem arrayTreeItem = (ProjectImageEventArrayTreeItem) item;
                    CameraTrapArray ary = arrayTreeItem.getCameraTrapArray();
                    if (ary.getCameraTrapArrayId().intValue() == array.getCameraTrapArrayId()) {
                        return arrayTreeItem;
                    }
                }

                // make array tree item
                ProjectImageEventArrayTreeItem arrayTreeItem = new ProjectImageEventArrayTreeItem(event, array);
                eventTreeItem.getChildren().add(arrayTreeItem);
                return arrayTreeItem;
            }
        }

        // make event tree item
        ProjectImageEventTreeItem eventTreeItem = new ProjectImageEventTreeItem(event);
        imageTreeItem.getChildren().add(eventTreeItem);

        // make array tree item
        ProjectImageEventArrayTreeItem arrayTreeItem = new ProjectImageEventArrayTreeItem(event, array);
        eventTreeItem.getChildren().add(arrayTreeItem);

        return arrayTreeItem;

    }

    public ProjectImageListTreeItem getImageListTreeItem(Project project) {

        ProjectTreeItem projectTreeItem = rootItem;
        if (projectTreeItem != null) {
            for (Object object : projectTreeItem.getChildren()) {
                if (object instanceof ProjectImageListTreeItem) {
                    return (ProjectImageListTreeItem) object;
                }
            }
        }
        return null;
    }

    public void expandProjectTreeUpToLevel(int level) {
        if (level > 0) {
            ProjectImageListTreeItem projectImageListTreeItem = getImageListTreeItem();
            projectImageListTreeItem.setExpanded(true);

            if (level > 1) {
                for (Object obj1 : projectImageListTreeItem.getChildren()) {
                    ProjectImageEventTreeItem eventTreeItem = (ProjectImageEventTreeItem) obj1;
                    eventTreeItem.setExpanded(true);

                    if (level > 2) {
                        for (Object obj2 : eventTreeItem.getChildren()) {
                            ProjectImageEventArrayTreeItem arrayTreeItem = (ProjectImageEventArrayTreeItem) obj2;
                            arrayTreeItem.setExpanded(true);

                            if (level > 3) {
                                for (Object obj3 : arrayTreeItem.getChildren()) {
                                    ProjectImageDeploymentTreeItem deploymentTreeItem = (ProjectImageDeploymentTreeItem) obj3;
                                    deploymentTreeItem.setExpanded(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateDeployment(Deployment deployment) {

        ProjectImageDeploymentTreeItem deploymentTreeItem = getProjectImageDeploymentTreeItem(deployment);
        deploymentTreeItem.setDeployment(deployment);
        if (deployment.getFailureType() == null) {
            deploymentTreeItem.setGraphic(new ImageView(new javafx.scene.image.Image("resources/icons/camera.png")));
        } else {
            deploymentTreeItem.setGraphic(new ImageView(new javafx.scene.image.Image("resources/icons/camera_delete.png")));
        }

        // check camera trap array
        ProjectImageEventArrayTreeItem arrayTreeItem = (ProjectImageEventArrayTreeItem) deploymentTreeItem.getParent();
        CameraTrapArray array = arrayTreeItem.getCameraTrapArray();

        ProjectImageEventTreeItem eventTreeItem = (ProjectImageEventTreeItem) arrayTreeItem.getParent();
        Event event = eventTreeItem.getEvent();

        if (event.getEventId() != deployment.getEvent().getEventId().intValue()
                || array.getCameraTrapArrayId().intValue() != deployment.getCameraTrap().getCameraTrapArray().getCameraTrapArrayId()) {
            arrayTreeItem.getChildren().remove(deploymentTreeItem);

            if (arrayTreeItem.getChildren().isEmpty()) {
                eventTreeItem.getChildren().remove(arrayTreeItem);
            }

            if (eventTreeItem.getChildren().isEmpty()) {
                eventTreeItem.getParent().getChildren().remove(eventTreeItem);
            }

            arrayTreeItem = getOrCreateProjectImageEventArrayTreeItem(deployment.getEvent(), deployment.getCameraTrap().getCameraTrapArray());
            arrayTreeItem.getChildren().add(deploymentTreeItem);
        }

        this.getSelectionModel().select(deploymentTreeItem);

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("deployment_update_confirm_header"),
                language.getString("deployment_update_confirm_context"),
                language.getString("alert_ok"));
    }

    public void showStandardView() {

        ProjectTreeItem projectTreeItem = rootItem;
        ProjectImageListTreeItem imageTreeItem = (ProjectImageListTreeItem) projectTreeItem.getChildren().get(0);
        for (Object object : imageTreeItem.getChildren()) {
            TreeItem item = (TreeItem) object;
            item.setExpanded(false);
        }

        projectTreeItem.setExpanded(true);
        imageTreeItem.setExpanded(true);

        this.requestFocus();
        this.getSelectionModel().select(projectTreeItem);
        this.getFocusModel().focus(1);

    }

    public ProjectImageTreeItem getProjectImageTreeItem(Image image) {
        Deployment deployment = image.getImageSequence().getDeployment();
        ProjectImageDeploymentTreeItem deploymentTreeItem = getProjectImageDeploymentTreeItem(deployment);

        ProjectImageTreeItem imageTreeItem;

        for (Object object : deploymentTreeItem.getChildren()) {
            imageTreeItem = (ProjectImageTreeItem) object;
            if (imageTreeItem.getImage().getImageId().intValue() == image.getImageId()) {
                return imageTreeItem;
            }
        }

        return null;
    }

    class ExportableTreeCell extends TextFieldTreeCell<String> {

        private final ContextMenu contextMenu;

        private final Menu menuExportMetadata;
        private final Menu menuExport_toCSV;
        private final MenuItem menuExport_toCSV_with_images;
        private final MenuItem menuExport_toCSV_without_images;
        private final Menu menuExport_toExcel;
        private final MenuItem menuExport_toExcel_with_images;
        private final MenuItem menuExport_toExcel_without_images;
        private final Menu menuExport_toWCS;
        private final MenuItem menuExport_toWCS_with_images;
        private final MenuItem menuExport_toWCS_without_images;
        private final Menu menuViewTable;
        private final MenuItem menuViewTable_same_window;
        private final MenuItem menuViewTable_new_window;
        private final MenuItem menuExport_toZip;

        public ExportableTreeCell() {
            this.contextMenu = new ContextMenu();

            this.menuExportMetadata = new Menu(language.getString("menu_export_metadata"), new ImageView(new javafx.scene.image.Image("resources/icons/table_save.png")));
            this.menuExport_toCSV = new Menu(language.getString("menu_export_csv"), new ImageView(new javafx.scene.image.Image("resources/icons/file_csv.png")));

            this.menuExport_toCSV_with_images = new MenuItem(language.getString("menu_export_with_images"));
            this.menuExport_toCSV_without_images = new MenuItem(language.getString("menu_export_without_images"));

            this.menuExport_toExcel = new Menu(language.getString("menu_export_excel"), new ImageView(new javafx.scene.image.Image("resources/icons/file_excel.png")));

            this.menuExport_toExcel_with_images = new MenuItem(language.getString("menu_export_with_images"));
            this.menuExport_toExcel_without_images = new MenuItem(language.getString("menu_export_without_images"));

            this.menuExport_toWCS = new Menu(language.getString("menu_export_wcs"), new ImageView(new javafx.scene.image.Image("resources/icons/file_excel.png")));

            this.menuExport_toWCS_with_images = new MenuItem(language.getString("menu_export_with_images"));
            this.menuExport_toWCS_without_images = new MenuItem(language.getString("menu_export_without_images"));

            this.menuExport_toCSV_with_images.setId("menuExport_toCSV_with_images");
            this.menuExport_toCSV_without_images.setId("menuExport_toCSV_without_images");
            this.menuExport_toExcel_with_images.setId("menuExport_toExcel_with_images");
            this.menuExport_toExcel_without_images.setId("menuExport_toExcel_without_images");
            this.menuExport_toWCS_with_images.setId("menuExport_toWCS_with_images");
            this.menuExport_toWCS_without_images.setId("menuExport_toWCS_without_images");

            this.menuExportMetadata.getItems().addAll(menuExport_toCSV, menuExport_toExcel, menuExport_toWCS);
            this.menuExport_toCSV.getItems().addAll(menuExport_toCSV_with_images, menuExport_toCSV_without_images);
            this.menuExport_toExcel.getItems().addAll(menuExport_toExcel_with_images, menuExport_toExcel_without_images);
            this.menuExport_toWCS.getItems().addAll(menuExport_toWCS_with_images, menuExport_toWCS_without_images);

            this.menuViewTable = new Menu(language.getString("menu_viewTable"), new ImageView(new javafx.scene.image.Image("resources/icons/table.png")));
            this.menuViewTable_same_window = new MenuItem(language.getString("menu_viewTable_same_window"), new ImageView(new javafx.scene.image.Image("resources/icons/application.png")));
            this.menuViewTable_same_window.setId("menuViewTable_same_window");
            this.menuViewTable_new_window = new MenuItem(language.getString("menu_viewTable_new_window"), new ImageView(new javafx.scene.image.Image("resources/icons/application_double.png")));
            this.menuViewTable_new_window.setId("menuViewTable_new_window");
            this.menuViewTable.getItems().addAll(menuViewTable_same_window, menuViewTable_new_window);

            this.menuExport_toZip = new MenuItem(language.getString("menu_save_transfer_file"), new ImageView(new javafx.scene.image.Image("resources/icons/file_zip.png")));
            this.menuExport_toZip.setId("save_transfer_file");

            this.contextMenu.getItems().addAll(menuExportMetadata, menuViewTable, menuExport_toZip);
        }

        public void setWildIDController(WildIDController controller) {
            if (this.menuExport_toCSV_with_images != null) {
                this.menuExport_toCSV_with_images.setOnAction(controller);
            }
            if (this.menuExport_toCSV_without_images != null) {
                this.menuExport_toCSV_without_images.setOnAction(controller);
            }
            if (this.menuExport_toExcel_with_images != null) {
                this.menuExport_toExcel_with_images.setOnAction(controller);
            }
            if (this.menuExport_toExcel_without_images != null) {
                this.menuExport_toExcel_without_images.setOnAction(controller);
            }
            if (this.menuExport_toWCS_with_images != null) {
                this.menuExport_toWCS_with_images.setOnAction(controller);
            }
            if (this.menuExport_toWCS_without_images != null) {
                this.menuExport_toWCS_without_images.setOnAction(controller);
            }
            if (this.menuViewTable_same_window != null) {
                this.menuViewTable_same_window.setOnAction(controller);
            }
            if (this.menuViewTable_new_window != null) {
                this.menuViewTable_new_window.setOnAction(controller);
            }
            if (this.menuExport_toZip != null) {
                this.menuExport_toZip.setOnAction(controller);
            }
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                return;
            }

            if (controller == null) {
                return;
            }

            TreeItem treeItem = getTreeItem();

            if (treeItem instanceof ProjectTreeItem
                    || treeItem instanceof ProjectImageListTreeItem
                    || treeItem instanceof ProjectImageEventTreeItem
                    || treeItem instanceof ProjectImageEventArrayTreeItem
                    || treeItem instanceof ProjectImageDeploymentTreeItem) {
                this.menuExportMetadata.setText(language.getString("menu_export_metadata"));
                this.menuExport_toCSV.setText(language.getString("menu_export_csv"));
                this.menuExport_toExcel.setText(language.getString("menu_export_excel"));
                this.menuExport_toWCS.setText(language.getString("menu_export_wcs"));
                this.menuExport_toCSV_with_images.setText(language.getString("menu_export_with_images"));
                this.menuExport_toCSV_without_images.setText(language.getString("menu_export_without_images"));
                this.menuExport_toExcel_with_images.setText(language.getString("menu_export_with_images"));
                this.menuExport_toExcel_without_images.setText(language.getString("menu_export_without_images"));
                this.menuExport_toWCS_with_images.setText(language.getString("menu_export_with_images"));
                this.menuExport_toWCS_without_images.setText(language.getString("menu_export_without_images"));
                this.menuViewTable.setText(language.getString("menu_viewTable"));
                this.menuViewTable_same_window.setText(language.getString("menu_viewTable_same_window"));
                this.menuViewTable_new_window.setText(language.getString("menu_viewTable_new_window"));
                this.menuExport_toZip.setText(language.getString("menu_save_transfer_file"));

                if (treeItem instanceof ProjectImageDeploymentTreeItem) {
                    if (((ProjectImageDeploymentTreeItem) treeItem).getDeployment().getFailureType() != null) {
                        Tooltip tooltip = new Tooltip();
                        tooltip.setText("Camera failure");
                        this.setTooltip(tooltip);
                    }
                }

                setContextMenu(contextMenu);
            } else {
                setContextMenu(null);
            }
        }
    }
}
