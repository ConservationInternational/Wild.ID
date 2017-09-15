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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import org.wildid.entity.BaitType;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraComparator;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.CameraTrapArrayComparator;
import org.wildid.entity.CameraTrapComparator;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.FailureType;
import org.wildid.entity.FeatureType;
import org.wildid.entity.ImageComparatorByTimestampAndName;
import org.wildid.entity.ImageSequence;
import org.wildid.entity.Person;
import org.wildid.entity.Project;
import org.wildid.service.DeploymentService;
import org.wildid.service.DeploymentServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectEditDeploymentPane extends ProjectNewDeploymentPane {

    private Deployment deployment;

    protected Button deleteButton;

    public ProjectEditDeploymentPane(LanguageModel language,
            Deployment deployment,
            Project project,
            Map<Event, List<CameraTrap>> event2trap,
            Map<Event, Map<CameraTrap, List<Camera>>> event2trap2camera,
            List<Person> persons,
            List<BaitType> baitTypes,
            List<FeatureType> featureTypes,
            List<FailureType> failureTypes) {

        super(language,
                project,
                event2trap,
                event2trap2camera,
                persons,
                baitTypes,
                featureTypes,
                failureTypes);

        this.deployment = deployment;
        this.imgView.setImage(new Image("resources/icons/page_edit.png"));

        // set event
        this.eventCombo.setValue(deployment.getEvent());

        // reset arrayCombo
        List<CameraTrapArray> arrays = new ArrayList<>();
        for (Event event : event2trap.keySet()) {
            if (event.getEventId().intValue() == deployment.getEvent().getEventId()) {
                for (CameraTrap trap : event2trap.get(event)) {
                    CameraTrapArray array = trap.getCameraTrapArray();
                    boolean found = false;
                    for (CameraTrapArray ary : arrays) {
                        if (ary.getCameraTrapArrayId().intValue() == array.getCameraTrapArrayId().intValue()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        arrays.add(array);
                    }
                }
                break;
            }
        }
        SortedList<CameraTrapArray> sortedArrayList = new SortedList<>(FXCollections.observableList(arrays), new CameraTrapArrayComparator());
        this.arrayCombo.setItems(sortedArrayList);

        // set array
        this.arrayCombo.setValue(deployment.getCameraTrap().getCameraTrapArray());

        // set camera trap and camera trap list
        this.cameraTrapCombo.setValue(deployment.getCameraTrap());

        // reset cameraTrapCombo
        List<CameraTrap> traps = new ArrayList<>();
        for (Event event : event2trap.keySet()) {
            if (event.getEventId().intValue() == deployment.getEvent().getEventId()) {
                for (CameraTrap trap : event2trap.get(event)) {
                    if (trap.getCameraTrapArray().getCameraTrapArrayId().intValue()
                            == deployment.getCameraTrap().getCameraTrapArray().getCameraTrapArrayId().intValue()) {
                        traps.add(trap);
                    }
                }
                break;
            }
        }
        SortedList<CameraTrap> sortedTrapList = new SortedList<>(FXCollections.observableList(traps), new CameraTrapComparator());
        this.cameraTrapCombo.setItems(sortedTrapList);

        // set name
        this.nameField.setText(deployment.getName());

        // set camera and camera list
        this.cameraCombo.setValue(deployment.getCamera());

        // reset cameraCombo
        List<Camera> cameras = getCameras(deployment.getEvent(), deployment.getCameraTrap(), event2trap2camera);
        SortedList<Camera> sortedCameraList = new SortedList<>(FXCollections.observableList(cameras), new CameraComparator());
        this.cameraCombo.setItems(sortedCameraList);

        Date start = deployment.getStartTime();
        if (start != null) {
            this.startPicker.setValue(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        Date end = deployment.getEndTime();
        if (end != null) {
            this.endPicker.setValue(end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        this.setupPersonCombo.setValue(deployment.getSetupPerson());
        this.pickupPersonCombo.setValue(deployment.getPickupPerson());

        this.baitTypeCombo.setValue(deployment.getBaitType());
        this.baitDetailTextField.setText(deployment.getBaitDetail());

        this.featureTypeCombo.setValue(deployment.getFeatureType());
        this.featureDetailTextField.setText(deployment.getFeatureTypeDetail());

        this.failureTypeCombo.setValue(deployment.getFailureType());
        DeploymentService deploymentService = new DeploymentServiceImpl();
        List<org.wildid.entity.Image> images = deploymentService.getImages(this.deployment);
        if (deployment.getFailureType() != null && images.isEmpty()) {
            //this.failureTypeCombo.getItems().remove(null);
        }
        this.failureDetailTextField.setText(deployment.getFailureDetail());

        this.quietSettingTextField.setText(deployment.getQuietPeriodSetting());
        this.restrictionTextField.setText(deployment.getRestrctionOnAccess());

        this.titleLabel.setText(language.getString("project_deployment_pane_edit_title") + " : " + project.getName());
        this.saveButton.setText(language.getString("project_deployment_pane_update_button"));
        this.saveButton.setId("project_deployment_pane_update_button");

        this.deleteButton = new Button(language.getString("project_deployment_pane_delete_button"));
        this.hbBtn.getChildren().add(this.deleteButton);
        this.deleteButton.setId("project_deployment_pane_delete_button");

        this.baitTypeCombo.setOnAction(null);
        this.featureTypeCombo.setOnAction(null);
        this.failureTypeCombo.setOnAction(null);

        super.removeRow(this.grid, 5);

        this.baitTypeCombo.setOnAction((evt) -> {
            BaitType baitType = (BaitType) baitTypeCombo.getSelectionModel().getSelectedItem();
            if (baitType.getName().equals("No Bait")) {
                // remove baitDetail
                removeRow(this.grid, 10);
                this.baitDetail = false;
            } else if (!baitDetail) {
                // add baitDetail
                addEmptyRow(this.grid, 10);
                this.grid.add(this.baitDetailLabel, 0, 10);
                this.grid.add(this.baitDetailTextField, 1, 10);
                this.baitDetail = true;
            }
        });

        this.featureTypeCombo.setOnAction((evt) -> {
            FeatureType featureType = (FeatureType) featureTypeCombo.getSelectionModel().getSelectedItem();
            BaitType baitType = (BaitType) baitTypeCombo.getValue();
            if (featureType == null) {
                // remove featureDetail
                if (baitType.getName().equals("No Bait")) {
                    removeRow(this.grid, 11);
                } else {
                    removeRow(this.grid, 12);
                }
                this.featureDetail = false;
            } else if (!this.featureDetail) {
                // add featureDetail
                if (baitType.getName().equals("No Bait")) {
                    addEmptyRow(this.grid, 11);
                    this.grid.add(this.featureDetailLabel, 0, 11);
                    this.grid.add(this.featureDetailTextField, 1, 11);
                } else {
                    addEmptyRow(this.grid, 12);
                    this.grid.add(this.featureDetailLabel, 0, 12);
                    this.grid.add(this.featureDetailTextField, 1, 12);
                }
                this.featureDetail = true;
            }
        });

        this.failureTypeCombo.setOnAction((evt) -> {
            FailureType failureType = (FailureType) failureTypeCombo.getSelectionModel().getSelectedItem();
            BaitType baitType = (BaitType) baitTypeCombo.getValue();
            FeatureType featureType = (FeatureType) featureTypeCombo.getValue();

            //if (failureType.getName().equals("Functioning")) {
            if (failureType == null) {
                // remove failureDetail
                if (baitType.getName().equals("No Bait")) {
                    if (featureType == null) {
                        removeRow(this.grid, 12);
                    } else {
                        removeRow(this.grid, 13);
                    }
                } else if (featureType == null) {
                    removeRow(this.grid, 13);
                } else {
                    removeRow(this.grid, 14);
                }
                this.failureDetail = false;
            } else if (!this.failureDetail) {
                if (baitType.getName().equals("No Bait")) {
                    if (featureType == null) {
                        // row 11
                        addEmptyRow(this.grid, 12);
                        this.grid.add(this.failureDetailLabel, 0, 12);
                        this.grid.add(this.failureDetailTextField, 1, 12);
                    } else {
                        // row 12
                        addEmptyRow(this.grid, 13);
                        this.grid.add(this.failureDetailLabel, 0, 13);
                        this.grid.add(this.failureDetailTextField, 1, 13);
                    }
                } else if (featureType == null) {
                    // row 12
                    addEmptyRow(this.grid, 13);
                    this.grid.add(this.failureDetailLabel, 0, 13);
                    this.grid.add(this.failureDetailTextField, 1, 13);
                } else {
                    // row 13
                    addEmptyRow(this.grid, 14);
                    this.grid.add(this.failureDetailLabel, 0, 14);
                    this.grid.add(this.failureDetailTextField, 1, 14);
                }
                this.failureDetail = true;
            }
        });

        if (!deployment.getBaitType().getName().equals("No Bait")) {
            addEmptyRow(this.grid, 10);
            this.grid.add(this.baitDetailLabel, 0, 10);
            this.grid.add(this.baitDetailTextField, 1, 10);
            this.baitDetail = true;
        } else {
            this.baitDetail = false;
        }

        if (deployment.getFeatureType() != null) {
            if (deployment.getBaitType().getName().equals("No Bait")) {
                addEmptyRow(this.grid, 12);
                this.grid.add(this.featureDetailLabel, 0, 12);
                this.grid.add(this.featureDetailTextField, 1, 12);
            } else {
                addEmptyRow(this.grid, 13);
                this.grid.add(this.featureDetailLabel, 0, 13);
                this.grid.add(this.featureDetailTextField, 1, 13);
            }
            this.featureDetail = true;
        } else {
            this.featureDetail = false;
        }

        if (deployment.getFailureType() != null) {
            if (deployment.getBaitType().getName().equals("No Bait")) {
                if (deployment.getFeatureType() == null) {
                    // row 11
                    addEmptyRow(this.grid, 12);
                    this.grid.add(this.failureDetailLabel, 0, 12);
                    this.grid.add(this.failureDetailTextField, 1, 12);
                } else {
                    // row 12
                    addEmptyRow(this.grid, 13);
                    this.grid.add(this.failureDetailLabel, 0, 13);
                    this.grid.add(this.failureDetailTextField, 1, 13);
                }
            } else if (deployment.getFeatureType() == null) {
                // row 12
                addEmptyRow(this.grid, 13);
                this.grid.add(this.failureDetailLabel, 0, 13);
                this.grid.add(this.failureDetailTextField, 1, 13);
            } else {
                // row 13
                addEmptyRow(this.grid, 14);
                this.grid.add(this.failureDetailLabel, 0, 14);
                this.grid.add(this.failureDetailTextField, 1, 14);
            }
            this.failureDetail = true;
        } else {
            this.failureDetail = false;
        }

    }

    @Override
    public void setLanguage(LanguageModel language) {
        super.setLanguage(language);
        this.titleLabel.setText(language.getString("project_deployment_pane_edit_title") + " : " + this.project.getName());
        this.saveButton.setText(language.getString("project_deployment_pane_update_button"));
        this.deleteButton.setText(language.getString("project_deployment_pane_delete_button"));
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        this.deleteButton.setOnAction(controller);
    }

    @Override
    public Deployment getDeployment() {

        this.deployment.setEvent((Event) this.eventCombo.getValue());
        this.deployment.setCameraTrap((CameraTrap) this.cameraTrapCombo.getValue());
        this.deployment.setCamera((Camera) this.cameraCombo.getValue());
        this.deployment.setName(this.nameField.getText());

        LocalDate start = startPicker.getValue();
        if (start != null) {
            this.deployment.setStartTime(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        LocalDate end = endPicker.getValue();
        if (end != null) {
            this.deployment.setEndTime(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        this.deployment.setSetupPerson((Person) setupPersonCombo.getValue());
        this.deployment.setPickupPerson((Person) pickupPersonCombo.getValue());
        this.deployment.setBaitType((BaitType) baitTypeCombo.getValue());
        this.deployment.setBaitDetail(baitDetailTextField.getText());
        this.deployment.setFeatureType((FeatureType) featureTypeCombo.getValue());
        this.deployment.setFeatureTypeDetail(featureDetailTextField.getText());
        this.deployment.setFailureType((FailureType) failureTypeCombo.getValue());
        this.deployment.setFailureDetail(failureDetailTextField.getText());
        this.deployment.setQuietPeriodSetting(quietSettingTextField.getText());
        this.deployment.setRestrctionOnAccess(restrictionTextField.getText());

        return this.deployment;
    }

    @Override
    public boolean validate() {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        Event event = (Event) eventCombo.getValue();
        if (event == null) {
            title = language.getString("title_error");
            header = language.getString("deployment_empty_event_error_header");
            context = language.getString("deployment_empty_event_error_context");
            ok = false;
        }

        if (ok) {
            CameraTrap trap = (CameraTrap) cameraTrapCombo.getValue();
            if (trap == null) {
                title = language.getString("title_error");
                header = language.getString("deployment_empty_trap_error_header");
                context = language.getString("deployment_empty_trap_error_context");
                ok = false;
            }
        }

        if (ok) {
            String deploymentName = nameField.getText();
            if (deploymentName == null || deploymentName.trim().equals("")) {
                title = language.getString("title_error");
                header = language.getString("deployment_empty_name_error_header");
                context = language.getString("deployment_empty_name_error_context");
                ok = false;
            } else {
                DeploymentService deployService = new DeploymentServiceImpl();
                boolean used = deployService.hasAnotherDeloymentWithName(deploymentName, deployment);
                if (used) {
                    title = language.getString("title_error");
                    header = language.getString("deployment_duplicate_name_error_header");
                    context = language.getString("deployment_duplicate_name_error_context");
                    ok = false;
                }
            }
        }

        if (ok) {
            Camera camera = (Camera) cameraCombo.getValue();
            if (camera == null) {
                title = language.getString("title_error");
                header = language.getString("deployment_empty_camera_error_header");
                context = language.getString("deployment_empty_camera_error_context");
                ok = false;
            }
        }

        if (ok) {
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();
            if (start == null) {
                title = language.getString("deployment_empty_start_error_title");
                header = language.getString("deployment_empty_start_error_header");
                context = language.getString("deployment_empty_start_error_context");
                ok = false;
            } else if (end == null) {
                title = language.getString("deployment_empty_end_error_title");
                header = language.getString("deployment_empty_end_error_header");
                context = language.getString("deployment_empty_end_error_context");
                ok = false;
            } else if (end.isBefore(start)) {
                title = language.getString("title_error");
                header = language.getString("deployment_end_before_start_error_header");
                context = language.getString("deployment_end_before_start_error_context");
                ok = false;
            } else {
                //List<org.wildid.entity.Image> images = getAllImagesInDeployment();

                DeploymentService deployService = new DeploymentServiceImpl();
                List<org.wildid.entity.Image> images = deployService.getImages(deployment);

                Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());

                LocalDateTime endDateMidnight = LocalDateTime.of(end, LocalTime.MAX);
                Date endDate = Date.from(endDateMidnight.atZone(ZoneId.systemDefault()).toInstant());
                //Date endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());

                Date startImageTime = images.get(0).getTimeCaptured();
                Date endImageTime = images.get(images.size() - 1).getTimeCaptured();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                if (startImageTime.before(startDate)) {
                    title = language.getString("title_error");
                    header = language.getString("deployment_start_after_image_start_error_header");
                    context = language.getString("deployment_start_after_image_start_error_context") + " (" + dateFormat.format(startImageTime) + ")";
                    ok = false;
                } else if (endImageTime.after(endDate)) {
                    title = language.getString("title_error");
                    header = language.getString("deployment_end_before_image_end_error_header");
                    context = language.getString("deployment_end_before_image_end_error_context") + " (" + dateFormat.format(endImageTime) + ")";
                    ok = false;
                }
            }

        }

        if (!ok) {
            Util.alertErrorPopup(
                    title,
                    header,
                    context,
                    language.getString("alert_ok"));
        }

        return ok;
    }

    public Deployment getOldDeployment() {
        return this.deployment;
    }

    public List<org.wildid.entity.Image> getAllImagesInDeployment() {
        List<org.wildid.entity.Image> images = new ArrayList<>();

        for (ImageSequence sequence : deployment.getImageSequences()) {
            images.addAll((Collection) sequence.getImages());
        }

        Collections.sort(images, new ImageComparatorByTimestampAndName());
        return images;
    }
}
