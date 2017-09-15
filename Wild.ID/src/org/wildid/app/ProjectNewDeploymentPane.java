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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
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
import org.wildid.entity.ImageRepository;
import org.wildid.entity.Person;
import org.wildid.entity.Project;
import org.wildid.service.DeploymentService;
import org.wildid.service.DeploymentServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectNewDeploymentPane extends WildIDDataPane {

    protected LanguageModel language;
    protected Project project;
    private final Map<Event, List<CameraTrap>> event2trap;
    private final Map<Event, Map<CameraTrap, List<Camera>>> event2trap2camera;
    private final List<Person> persons;
    private final List<BaitType> baitTypes;
    private final List<FeatureType> featureTypes;
    private final List<FailureType> failureTypes;

    protected Label titleLabel;
    protected Label eventLabel;
    protected Label arrayLabel;
    protected Label cameraTrapLabel;
    protected Label nameLabel;
    protected Label folderLabel;
    protected Label cameraLabel;
    protected Label startDateLabel;
    protected Label endDateLabel;
    protected Label setupPersonLabel;
    protected Label pickupPersonLabel;
    protected Label baitTypeLabel;
    protected Label baitDetailLabel;
    protected Label featureTypeLabel;
    protected Label featureDetailLabel;
    protected Label failureTypeLabel;
    protected Label failureDetailLabel;
    protected Label quietPeriodSettingLabel;
    protected Label restrctionOnAccessLabel;

    protected EventComboBox eventCombo;
    protected CameraTrapArrayComboBox arrayCombo;
    protected CameraTrapComboBox cameraTrapCombo;
    protected TextField nameField;
    protected TextField folderField;
    protected Button folderButton;
    protected CameraComboBox cameraCombo;
    protected DatePicker startPicker = new DatePicker();
    protected DatePicker endPicker = new DatePicker();
    protected PersonComboBox setupPersonCombo;
    protected PersonComboBox pickupPersonCombo;
    protected BaitTypeComboBox baitTypeCombo;
    protected TextField baitDetailTextField;
    protected FeatureTypeComboBox featureTypeCombo;
    protected TextField featureDetailTextField;
    protected FailureTypeComboBox failureTypeCombo;
    protected TextField failureDetailTextField;
    protected TextField quietSettingTextField;
    protected TextField restrictionTextField;

    protected Button saveButton;

    protected HBox hbBtn = new HBox(10);
    protected GridPane grid;
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");
    protected Image editImg = new Image("resources/icons/page_edit.png");
    protected Image folderImg = new Image("resources/icons/folder.png");

    protected boolean baitDetail = false;
    protected boolean featureDetail = false;
    protected boolean failureDetail = false;

    public ProjectNewDeploymentPane(LanguageModel language,
            Project project,
            Map<Event, List<CameraTrap>> event2trap,
            Map<Event, Map<CameraTrap, List<Camera>>> event2trap2camera,
            List<Person> persons,
            List<BaitType> baitTypes,
            List<FeatureType> featureTypes,
            List<FailureType> failureTypes) {

        this.language = language;
        this.project = project;
        this.event2trap = event2trap;
        this.event2trap2camera = event2trap2camera;
        this.persons = persons;
        this.baitTypes = baitTypes;
        this.featureTypes = featureTypes;
        this.failureTypes = failureTypes;

        this.titleLabel = new Label(language.getString("project_deployment_pane_title") + " : " + project.getName());
        this.titleLabel.setStyle(WildIDDataPane.TITLE_STYLE);

        this.imgView = new ImageView(pageImg);
        this.imgView.setVisible(true);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel, this.imgView);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.prefWidthProperty().bind(this.widthProperty());

        Pane gridPane = createForm();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.prefHeightProperty().bind(this.heightProperty());
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(gridPane);

        vbox.getChildren().addAll(titleBox, scrollPane);
        this.getChildren().add(vbox);

    }

    private Pane createForm() {

        this.grid = new GridPane();
        this.grid.setAlignment(Pos.TOP_LEFT);
        this.grid.setPadding(new Insets(30, 10, 50, 50));
        this.grid.setHgap(20);
        this.grid.setStyle(TEXT_STYLE);
        this.grid.setVgap(8);

        // events
        this.eventLabel = new Label(language.getString("project_deployment_pane_event_label"));
        this.grid.add(this.eventLabel, 0, 0);

        // list all events that are not fully loaded
        this.eventCombo = new EventComboBox(new ArrayList<>(event2trap.keySet()));
        this.eventCombo.setMinWidth(350);
        this.eventCombo.setOnAction((event) -> {
            Event evt = (Event) eventCombo.getSelectionModel().getSelectedItem();

            // reset arrayCombo
            List<CameraTrapArray> arrays = new ArrayList<>();
            if (evt != null) {
                for (CameraTrap trap : event2trap.get(evt)) {
                    boolean found = false;
                    for (CameraTrapArray tmpArray : arrays) {
                        if (tmpArray.getCameraTrapArrayId().intValue() == trap.getCameraTrapArray().getCameraTrapArrayId().intValue()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        arrays.add(trap.getCameraTrapArray());
                    }
                }
            }

            CameraTrapArray oldArray = (CameraTrapArray) this.arrayCombo.getValue();
            CameraTrapArray ary = null;
            SortedList<CameraTrapArray> sortedArrayList = new SortedList<>(FXCollections.observableList(arrays), new CameraTrapArrayComparator());
            this.arrayCombo.setItems(sortedArrayList);

            if (!sortedArrayList.isEmpty()) {
                boolean found = false;
                if (oldArray != null) {
                    for (CameraTrapArray array : arrays) {
                        if (array.getCameraTrapArrayId().intValue() == oldArray.getCameraTrapArrayId()) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    if (sortedArrayList.size() == 1) {
                        this.arrayCombo.setValue(sortedArrayList.get(0));
                        ary = sortedArrayList.get(0);
                    } else {
                        this.arrayCombo.setValue(null);
                    }
                } else {
                    this.arrayCombo.setValue(oldArray);
                    ary = oldArray;
                }
            }
        });

        this.grid.add(this.eventCombo, 1, 0);

        // camera trap arrays
        this.arrayLabel = new Label(language.getString("image_metadata_table_pane_type_ctArray"));
        this.grid.add(this.arrayLabel, 0, 1);

        List<CameraTrapArray> arrays = new ArrayList<>();
        Event event = (Event) this.eventCombo.getValue();
        if (event != null) {
            for (CameraTrap trap : event2trap.get(event)) {
                boolean found = false;
                for (CameraTrapArray tmpArray : arrays) {
                    if (tmpArray.getCameraTrapArrayId().intValue() == trap.getCameraTrapArray().getCameraTrapArrayId().intValue()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    arrays.add(trap.getCameraTrapArray());
                }
            }
        }
        this.arrayCombo = new CameraTrapArrayComboBox(arrays);
        this.arrayCombo.setMinWidth(350);
        this.arrayCombo.setOnAction((event1) -> {
            Event evt = (Event) eventCombo.getSelectionModel().getSelectedItem();
            CameraTrapArray ary = (CameraTrapArray) arrayCombo.getSelectionModel().getSelectedItem();

            // reset cameraTrapCombo
            List<CameraTrap> traps = new ArrayList<>();
            if (evt != null) {
                for (CameraTrap trp : event2trap.get(evt)) {
                    if (trp.getCameraTrapArray().getCameraTrapArrayId().intValue()
                            == ary.getCameraTrapArrayId().intValue()) {
                        traps.add(trp);
                    }
                }
            }

            CameraTrap oldTrap = (CameraTrap) this.cameraTrapCombo.getValue();
            CameraTrap trp = null;
            SortedList<CameraTrap> sortedTrapList = new SortedList<>(FXCollections.observableList(traps), new CameraTrapComparator());
            this.cameraTrapCombo.setItems(sortedTrapList);
            if (!sortedTrapList.isEmpty()) {

                boolean found = false;
                if (oldTrap != null) {
                    for (CameraTrap trap : traps) {
                        if (trap.getCameraTrapId().intValue() == oldTrap.getCameraTrapId()) {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    if (sortedTrapList.size() == 1) {
                        this.cameraTrapCombo.setValue(sortedTrapList.get(0));
                        trp = sortedTrapList.get(0);
                    } else {
                        this.cameraTrapCombo.setValue(null);
                    }
                } else {
                    this.cameraTrapCombo.setValue(oldTrap);
                    trp = oldTrap;
                }
            }

        });
        this.grid.add(this.arrayCombo, 1, 1);

        // camera traps
        this.cameraTrapLabel = new Label(language.getString("project_deployment_pane_camera_trap_label"));
        this.grid.add(this.cameraTrapLabel, 0, 2);

        // list all traps that are not loaded for the selected events
        CameraTrapArray array = (CameraTrapArray) this.arrayCombo.getValue();
        List<CameraTrap> traps = new ArrayList<>();
        if (event != null && array != null) {
            for (CameraTrap trp : event2trap.get(event)) {
                if (trp.getCameraTrapArray().getCameraTrapArrayId().intValue()
                        == array.getCameraTrapArrayId().intValue()) {
                    traps.add(trp);
                }
            }
        }

        this.cameraTrapCombo = new CameraTrapComboBox(traps);
        this.cameraTrapCombo.setMinWidth(350);
        this.cameraTrapCombo.setOnAction((evt) -> {
            CameraTrap trap = (CameraTrap) cameraTrapCombo.getSelectionModel().getSelectedItem();
            Event event1 = (Event) this.eventCombo.getValue();
            if (trap != null && event1 != null) {
                List<Camera> cameras = getCameras(event1, trap, event2trap2camera);
                Camera oldCamera = (Camera) this.cameraCombo.getValue();
                SortedList<Camera> sortedCameraList = new SortedList<>(FXCollections.observableList(cameras), new CameraComparator());
                this.cameraCombo.setItems(sortedCameraList);

                if (!sortedCameraList.isEmpty()) {
                    boolean found = false;
                    if (oldCamera != null) {
                        for (Camera camera : cameras) {
                            if (camera.getCameraId().intValue() == oldCamera.getCameraId()) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        this.cameraCombo.setValue(null);
                    } else {
                        this.cameraCombo.setValue(oldCamera);
                    }
                } else {
                    this.cameraCombo.setValue(null);
                }

                String defaultDeploymentName = event1.getProject().getAbbrevName()
                        + "_" + event1.getName().replaceAll(" ", "_")
                        + "_" + trap.getName().replaceAll(" ", "_");
                this.nameField.setText(defaultDeploymentName);
            }
        });

        this.grid.add(this.cameraTrapCombo, 1, 2);

        // name
        this.nameLabel = new Label(language.getString("project_deployment_pane_name_label"));
        this.grid.add(this.nameLabel, 0, 3);

        this.nameField = new TextField();
        this.grid.add(this.nameField, 1, 3);

        // cameras
        this.cameraLabel = new Label(language.getString("project_deployment_pane_camera_label"));
        this.grid.add(this.cameraLabel, 0, 4);

        List<Camera> cameras = new ArrayList<>();
        CameraTrap trap = (CameraTrap) this.cameraTrapCombo.getValue();
        if (event != null && trap != null) {
            cameras.addAll(getCameras(event, trap, event2trap2camera));
        }

        //cameras.add(null);
        this.cameraCombo = new CameraComboBox(cameras);
        this.cameraCombo.setMinWidth(350);
        if (cameras.size() == 1) {
            this.cameraCombo.setValue(cameras.get(0));
        } else {
            this.cameraCombo.setValue(null);
        }
        this.grid.add(this.cameraCombo, 1, 4);

        // folder
        this.folderLabel = new Label(language.getString("project_deployment_pane_folder_label"));
        this.grid.add(this.folderLabel, 0, 5);

        HBox folderHBox = new HBox(0);
        this.folderField = new TextField();
        this.folderField.setEditable(true);
        this.folderButton = new Button();
        this.folderButton.setGraphic(new ImageView(folderImg));
        this.folderButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Directory Chooser");

                if (WildID.wildIDProperties.getWorkingDirObj() != null) {
                    directoryChooser.setInitialDirectory(WildID.wildIDProperties.getWorkingDirObj());
                }

                //Show open file dialog
                File file = directoryChooser.showDialog(null);
                if (file != null) {
                    folderField.setText(file.getPath());
                }
            }

        });

        folderHBox.getChildren().addAll(folderField, folderButton);
        folderField.setMinWidth(320);
        this.grid.add(folderHBox, 1, 5);

        String pattern = "yyyy-MM-dd";
        StringConverter converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter
                    = DateTimeFormatter.ofPattern(pattern);

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        // start date
        this.startDateLabel = new Label(language.getString("project_deployment_pane_stare_date_label"));
        Tooltip tooltip_zoomin = new Tooltip(this.language.getString("project_imageview_pane_tooltip_zoomin"));
        this.startDateLabel.setTooltip(tooltip_zoomin);

        this.grid.add(this.startDateLabel, 0, 6);

        this.startPicker.setPrefWidth(200);
        this.startPicker.setEditable(false);
        this.startPicker.setConverter(converter);
        this.startPicker.setPromptText(pattern.toLowerCase());
        this.grid.add(startPicker, 1, 6);

        // end date
        this.endDateLabel = new Label(language.getString("project_deployment_pane_end_date_label"));
        this.grid.add(this.endDateLabel, 0, 7);

        this.endPicker.setPrefWidth(200);
        this.endPicker.setEditable(false);
        this.endPicker.setConverter(converter);
        this.endPicker.setPromptText(pattern.toLowerCase());
        this.grid.add(endPicker, 1, 7);

        // setup person
        this.setupPersonLabel = new Label(language.getString("project_deployment_pane_setup_person_label"));
        this.grid.add(this.setupPersonLabel, 0, 8);

        List<Person> setupPersonList = new ArrayList<>(persons);
        this.setupPersonCombo = new PersonComboBox(setupPersonList);
        this.setupPersonCombo.getItems().add(null);
        this.setupPersonCombo.setValue(null);
        this.setupPersonCombo.setPrefWidth(350);
        this.grid.add(this.setupPersonCombo, 1, 8);

        // pickup person
        this.pickupPersonLabel = new Label(language.getString("project_deployment_pane_pickup_person_label"));
        this.grid.add(this.pickupPersonLabel, 0, 9);

        List<Person> pickupPersonList = new ArrayList<>(persons);
        this.pickupPersonCombo = new PersonComboBox(pickupPersonList);
        this.pickupPersonCombo.getItems().add(null);
        this.pickupPersonCombo.setValue(null);
        this.pickupPersonCombo.setPrefWidth(350);
        this.grid.add(this.pickupPersonCombo, 1, 9);

        // bait
        this.baitTypeLabel = new Label(language.getString("project_deployment_pane_bait_type_label"));
        this.grid.add(this.baitTypeLabel, 0, 10);

        this.baitTypeCombo = new BaitTypeComboBox(language, baitTypes);
        this.baitTypeCombo.setPrefWidth(350);

        this.baitDetail = false;
        this.baitTypeCombo.setOnAction((evt) -> {
            BaitType baitType = (BaitType) baitTypeCombo.getSelectionModel().getSelectedItem();
            if (baitType.getName().equals("No Bait")) {
                // remove baitDetail
                removeRow(this.grid, 11);
                this.baitDetail = false;
            } else if (!baitDetail) {
                // add baitDetail
                addEmptyRow(this.grid, 11);
                this.grid.add(this.baitDetailLabel, 0, 11);
                this.grid.add(this.baitDetailTextField, 1, 11);
                this.baitDetail = true;
            }
        });
        this.grid.add(this.baitTypeCombo, 1, 10);

        this.baitDetailLabel = new Label(language.getString("project_deployment_pane_bait_detail_label"));
        this.baitDetailTextField = new TextField();

        // feature 
        this.featureTypeLabel = new Label(language.getString("project_deployment_pane_feature_type_label"));
        this.grid.add(this.featureTypeLabel, 0, 11);

        this.featureTypeCombo = new FeatureTypeComboBox(language, featureTypes);
        this.featureTypeCombo.setPrefWidth(350);
        this.featureTypeCombo.getItems().add(null);
        this.featureTypeCombo.setValue(null);

        this.featureDetail = false;
        this.featureTypeCombo.setOnAction((evt) -> {
            FeatureType featureType = (FeatureType) featureTypeCombo.getSelectionModel().getSelectedItem();
            BaitType baitType = (BaitType) baitTypeCombo.getValue();
            if (featureType == null) {
                // remove featureDetail
                if (baitType.getName().equals("No Bait")) {
                    removeRow(this.grid, 12);
                    this.featureDetail = false;
                } else {
                    removeRow(this.grid, 13);
                    this.featureDetail = false;
                }
            } else if (!this.featureDetail) {
                // add featureDetail
                if (baitType.getName().equals("No Bait")) {
                    addEmptyRow(this.grid, 12);
                    this.grid.add(this.featureDetailLabel, 0, 12);
                    this.grid.add(this.featureDetailTextField, 1, 12);
                    this.featureDetail = true;
                } else {
                    addEmptyRow(this.grid, 13);
                    this.grid.add(this.featureDetailLabel, 0, 13);
                    this.grid.add(this.featureDetailTextField, 1, 13);
                    this.featureDetail = true;
                }
            }
        });

        this.grid.add(this.featureTypeCombo, 1, 11);

        this.featureDetailLabel = new Label(language.getString("project_deployment_pane_feature_detail_label"));
        this.featureDetailTextField = new TextField();

        // failure
        this.failureTypeLabel = new Label(language.getString("project_deployment_pane_failure_type_label"));
        this.grid.add(this.failureTypeLabel, 0, 12);

        this.failureTypeCombo = new FailureTypeComboBox(this.language, this.failureTypes);
        this.failureTypeCombo.getItems().add(null);
        this.failureTypeCombo.setValue(null);
        this.failureTypeCombo.setPrefWidth(350);

        this.failureDetail = false;
        this.failureTypeCombo.setOnAction((evt) -> {
            FailureType failureType = (FailureType) failureTypeCombo.getSelectionModel().getSelectedItem();
            BaitType baitType = (BaitType) baitTypeCombo.getValue();
            FeatureType featureType = (FeatureType) featureTypeCombo.getValue();

            //if (failureType.getName().equals("Functioning")) {
            if (failureType == null) {
                // remove failureDetail
                if (baitType.getName().equals("No Bait")) {
                    if (featureType == null) {
                        removeRow(this.grid, 13);
                        this.failureDetail = false;
                    } else {
                        removeRow(this.grid, 14);
                        this.failureDetail = false;
                    }
                } else if (featureType == null) {
                    removeRow(this.grid, 14);
                    this.failureDetail = false;
                } else {
                    removeRow(this.grid, 15);
                    this.failureDetail = false;
                }
            } else if (!this.failureDetail) {
                if (baitType.getName().equals("No Bait")) {
                    if (featureType == null) {
                        // row 11
                        addEmptyRow(this.grid, 13);
                        this.grid.add(this.failureDetailLabel, 0, 13);
                        this.grid.add(this.failureDetailTextField, 1, 13);
                    } else {
                        // row 12
                        addEmptyRow(this.grid, 14);
                        this.grid.add(this.failureDetailLabel, 0, 14);
                        this.grid.add(this.failureDetailTextField, 1, 14);
                    }
                } else if (featureType == null) {
                    // row 12
                    addEmptyRow(this.grid, 14);
                    this.grid.add(this.failureDetailLabel, 0, 14);
                    this.grid.add(this.failureDetailTextField, 1, 14);
                } else {
                    // row 13
                    addEmptyRow(this.grid, 15);
                    this.grid.add(this.failureDetailLabel, 0, 15);
                    this.grid.add(this.failureDetailTextField, 1, 15);
                }
                this.failureDetail = true;
            }
        });

        this.grid.add(this.failureTypeCombo, 1, 12);
        this.failureDetailLabel = new Label(language.getString("project_deployment_pane_failure_detail_label"));
        this.failureDetailTextField = new TextField();

        // quiet setting
        this.quietPeriodSettingLabel = new Label(language.getString("project_deployment_pane_quiet_period_setting_label"));
        this.grid.add(this.quietPeriodSettingLabel, 0, 13);

        this.quietSettingTextField = new TextField();
        this.grid.add(this.quietSettingTextField, 1, 13);

        this.restrctionOnAccessLabel = new Label(language.getString("project_deployment_pane_restriction_label"));
        this.grid.add(this.restrctionOnAccessLabel, 0, 14);

        this.restrictionTextField = new TextField();
        this.grid.add(this.restrictionTextField, 1, 14);

        this.saveButton = new Button(language.getString("project_deployment_pane_create_button"));
        this.saveButton.setId("project_deployment_pane_create_button");

        this.hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        this.hbBtn.getChildren().add(saveButton);
        this.grid.add(hbBtn, 1, 15);

        return grid;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
    }

    @Override
    public void setLanguage(LanguageModel language) {

        this.language = language;
        this.titleLabel.setText(language.getString("project_deployment_pane_title") + " : " + project.getName());
        this.eventLabel.setText(language.getString("project_deployment_pane_event_label"));
        this.arrayLabel.setText(language.getString("image_metadata_table_pane_type_ctArray"));
        this.cameraTrapLabel.setText(language.getString("project_deployment_pane_camera_trap_label"));
        this.nameLabel.setText(language.getString("project_deployment_pane_name_label"));
        this.cameraLabel.setText(language.getString("project_deployment_pane_camera_label"));
        this.folderLabel.setText(language.getString("project_deployment_pane_folder_label"));
        this.startDateLabel.setText(language.getString("project_deployment_pane_stare_date_label"));
        this.endDateLabel.setText(language.getString("project_deployment_pane_end_date_label"));
        this.setupPersonLabel.setText(language.getString("project_deployment_pane_setup_person_label"));
        this.pickupPersonLabel.setText(language.getString("project_deployment_pane_pickup_person_label"));
        this.baitTypeLabel.setText(language.getString("project_deployment_pane_bait_type_label"));
        this.baitDetailLabel.setText(language.getString("project_deployment_pane_bait_detail_label"));
        this.featureTypeLabel.setText(language.getString("project_deployment_pane_feature_type_label"));
        this.featureDetailLabel.setText(language.getString("project_deployment_pane_feature_detail_label"));
        this.failureTypeLabel.setText(language.getString("project_deployment_pane_failure_type_label"));
        this.quietPeriodSettingLabel.setText(language.getString("project_deployment_pane_quiet_period_setting_label"));
        this.restrctionOnAccessLabel.setText(language.getString("project_deployment_pane_restriction_label"));
        this.saveButton.setText(language.getString("project_deployment_pane_create_button"));
    }

    protected void addEmptyRow(GridPane gridPane, int row) {
        List<Node> children = new LinkedList<>(gridPane.getChildren());
        for (Node node : children) {
            int nodeRow = GridPane.getRowIndex(node);
            if (nodeRow >= row) {
                GridPane.setRowIndex(node, nodeRow + 1);
            }
        }
    }

    protected void removeRow(GridPane gridPane, int row) {
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
                boolean used = deployService.hasDeloymentWithName(deploymentName);
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

            FailureType failureType = (FailureType) failureTypeCombo.getValue();
            if (failureType == null) {
                String folderName = (String) folderField.getText();
                if (folderName == null || folderName.trim().equals("")) {
                    title = language.getString("title_error");
                    header = language.getString("deployment_empty_folder_error_header");
                    context = language.getString("deployment_empty_folder_error_context");
                    ok = false;
                } else {
                    File folder = new File(folderName.trim());
                    if (!folder.exists()) {
                        title = language.getString("title_error");
                        header = language.getString("deployment_folder_not_found_error_header");
                        context = language.getString("deployment_folder_not_found_error_context");
                        ok = false;
                    } else if (!Util.hasJpgFile(folder)) {
                        title = language.getString("title_error");
                        header = language.getString("deployment_folder_has_no_jpg_error_header");
                        context = language.getString("deployment_folder_has_no_jpg_error_context");
                        ok = false;
                    } else {
                        String dupName = ImageRepository.checkDuplicateFileName(folder);
                        if (dupName != null) {
                            title = language.getString("title_error");
                            header = language.getString("deployment_folder_image_name_duplicate_error_header");
                            context = language.getString("deployment_folder_image_name_duplicate_error_context") + ": " + dupName;
                            ok = false;
                        }
                    }
                }
            }
        }

        if (ok) {
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();
            if (start != null && end != null && end.isBefore(start)) {
                title = language.getString("title_error");
                header = language.getString("deployment_end_before_start_error_header");
                context = language.getString("deployment_end_before_start_error_context");
                ok = false;
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

    public Deployment getDeployment() {

        Deployment deployment = new Deployment();
        deployment.setEvent((Event) this.eventCombo.getValue());
        deployment.setCameraTrap((CameraTrap) this.cameraTrapCombo.getValue());
        deployment.setName(this.nameField.getText());
        deployment.setCamera((Camera) this.cameraCombo.getValue());

        LocalDate start = startPicker.getValue();
        if (start != null) {
            deployment.setStartTime(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        LocalDate end = endPicker.getValue();
        if (end != null) {
            deployment.setEndTime(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        deployment.setSetupPerson((Person) setupPersonCombo.getValue());
        deployment.setPickupPerson((Person) pickupPersonCombo.getValue());
        deployment.setBaitType((BaitType) baitTypeCombo.getValue());
        deployment.setBaitDetail(baitDetailTextField.getText());
        deployment.setFeatureType((FeatureType) featureTypeCombo.getValue());
        deployment.setFeatureTypeDetail(featureDetailTextField.getText());
        deployment.setFailureType((FailureType) failureTypeCombo.getValue());
        deployment.setFailureDetail(failureDetailTextField.getText());
        deployment.setQuietPeriodSetting(quietSettingTextField.getText());
        deployment.setRestrctionOnAccess(restrictionTextField.getText());

        return deployment;
    }

    public String getImageFolderPath() {
        return this.folderField.getText();
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    public void confirmLoadingSuccess(Pane pane) {
        Util.alertInformationPopup(
                pane,
                language.getString("title_success"),
                language.getString("deployment_loading_complete_header"),
                language.getString("deployment_loading_complete_context"),
                language.getString("alert_ok"));
    }

    public void confirmLoadingSuccessWithDateChanged(Pane pane) {
        Util.alertInformationPopup(
                pane,
                language.getString("title_success"),
                language.getString("deployment_loading_complete_header"),
                language.getString("deployment_loading_complete_context") + "\n\n"
                + language.getString("deployment_loading_complete_context_notice"),
                language.getString("alert_ok"));
    }

    public void confirmDamageReportSuccess() {
        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("deployment_damage_report_complete_header"),
                language.getString("deployment_damage_report_complete_context"),
                language.getString("alert_ok"));
    }

    public static List<Camera> getCameras(Event event, CameraTrap trap, Map<Event, Map<CameraTrap, List<Camera>>> event2trap2camera) {
        for (Event evt : event2trap2camera.keySet()) {
            if (evt.getEventId().intValue() == event.getEventId()) {
                for (CameraTrap trp : event2trap2camera.get(evt).keySet()) {
                    if (trp.getCameraTrapId().intValue() == trap.getCameraTrapId()) {
                        return event2trap2camera.get(evt).get(trp);
                    }
                }
            }
        }
        return new ArrayList<>();
    }
}
