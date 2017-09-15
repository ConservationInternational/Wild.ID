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

import org.wildid.entity.CameraComparator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraModel;
import org.wildid.entity.Project;
import org.wildid.service.ProjectService;
import org.wildid.service.ProjectServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectCameraPane extends WildIDDataPane {

    private LanguageModel language;
    private final Project project;
    private final List<CameraModel> cameraModels;
    protected ObservableList<Integer> yearList;
    protected ObservableList<String> makers;

    protected Label titleLabel;
    protected Label cameraModelLabel;
    protected Label cameraMakerLabel;
    protected Label cameraSerialNumberLabel;
    protected Label cameraPurchaseYearLabel;

    protected ComboBox cameraMakerCombo = new ComboBox();
    protected ComboBox cameraModelCombo = new ComboBox();
    protected TextField cameraSerialNumberTextField = new TextField();
    protected ComboBox cameraPurchaseYearCombo = new ComboBox();

    protected TableView<Camera> cameraTable = new TableView<>();
    protected TableColumn<Camera, String> makerColumn;
    protected TableColumn<Camera, String> modelColumn;
    protected TableColumn<Camera, String> purchaseYearColumn;
    protected TableColumn<Camera, String> serialNumberColumn;

    protected Button saveButton;
    protected Button deleteButton;
    protected Button cancelButton;

    protected HBox hbBtn = new HBox(10);
    protected GridPane grid;
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");
    protected Image editImg = new Image("resources/icons/page_edit.png");

    public ProjectCameraPane(LanguageModel language, List<CameraModel> cameraModels, Project project) {

        this.language = language;
        this.cameraModels = cameraModels;
        this.project = project;
        this.setStyle(BG_COLOR_STYLE);

        titleLabel = new Label(language.getString("project_camera_pane_title") + " : " + project.getName());
        //titleLabel.setPadding(new Insets(20, 0, 20, 50));
        titleLabel.setStyle(TITLE_STYLE);

        this.imgView = new ImageView(pageImg);
        this.imgView.setVisible(true);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel, this.imgView);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.prefWidthProperty().bind(this.widthProperty());

        cameraTable.prefHeightProperty().bind(this.heightProperty().subtract(275));
        cameraTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cameraTable.setPlaceholder(new Label(language.getString("project_camera_pane_empty_table_message")));
        cameraTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Camera camera = (Camera) newSelection;
                startEditing(camera);
            }
        });

        vbox.getChildren().addAll(titleBox, createForm(), cameraTable);
        this.getChildren().add(vbox);

        populateCameraTableView();
    }

    // populate the tableview
    private void populateCameraTableView() {

        List<Camera> cameras = new ArrayList<>();
        TreeSet<Camera> cameraSet = new TreeSet<>(new CameraComparator());
        cameraSet.addAll(project.getCameras());
        cameras.addAll(cameraSet);
        cameraTable.setItems(FXCollections.observableList(cameras));

        TableColumn<Camera, Number> indexColumn = new TableColumn<>("");
        indexColumn.setSortable(false);
        indexColumn.setMinWidth(40);
        indexColumn.setMaxWidth(40);
        indexColumn.setPrefWidth(40);
        indexColumn.setStyle(INDEX_COLUMN_STYLE);
        indexColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(cameraTable.getItems().indexOf(column.getValue()) + 1));

        makerColumn = new TableColumn<>(language.getString("project_camera_pane_maker_label"));
        makerColumn.setCellValueFactory((TableColumn.CellDataFeatures<Camera, String> cellData) -> new SimpleStringProperty((cellData.getValue().getCameraModel().getMaker())));
        makerColumn.setMinWidth(150);
        makerColumn.setMaxWidth(150);
        makerColumn.setPrefWidth(150);

        modelColumn = new TableColumn<>(language.getString("project_camera_pane_model_label"));
        modelColumn.setCellValueFactory((TableColumn.CellDataFeatures<Camera, String> cellData) -> new SimpleStringProperty((cellData.getValue().getCameraModel().getName())));
        modelColumn.setMinWidth(200);
        modelColumn.setMaxWidth(200);
        modelColumn.setPrefWidth(200);

        serialNumberColumn = new TableColumn<>(language.getString("project_camera_pane_serial_number_label"));
        serialNumberColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));

        purchaseYearColumn = new TableColumn<>(language.getString("project_camera_pane_purchase_year_label"));
        purchaseYearColumn.setCellValueFactory(new PropertyValueFactory<>("yearPurchased"));
        purchaseYearColumn.setMinWidth(130);
        purchaseYearColumn.setMaxWidth(130);
        purchaseYearColumn.setPrefWidth(130);
        //purchaseYearNumberCol.setStyle( "-fx-alignment: CENTER;");

        cameraTable.getColumns().setAll(indexColumn, makerColumn, modelColumn, purchaseYearColumn, serialNumberColumn);
    }

    private Pane createForm() {

        grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setPadding(new Insets(30, 10, 30, 50));
        grid.setHgap(20);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        // maker
        makers = FXCollections.observableList(getMakers());
        cameraMakerCombo.setItems(makers);
        String maker = makers.iterator().next();
        if (!makers.isEmpty()) {
            cameraMakerCombo.setValue(maker);
        }

        cameraMakerCombo.setMinWidth(350);
        cameraMakerCombo.setStyle(REQUIRED_STYLE);
        cameraMakerLabel = new Label(language.getString("project_camera_pane_maker_label"));
        grid.add(cameraMakerLabel, 0, 0);
        grid.add(cameraMakerCombo, 1, 0);

        // model name
        ObservableList<String> models = FXCollections.observableList(getModels(maker));
        cameraModelCombo.setItems(models);
        String model = models.iterator().next();
        if (!models.isEmpty()) {
            cameraModelCombo.setValue(model);
        }

        cameraModelCombo.setMinWidth(350);
        cameraModelCombo.setStyle(REQUIRED_STYLE);
        cameraModelLabel = new Label(language.getString("project_camera_pane_model_label"));
        grid.add(cameraModelLabel, 0, 1);
        grid.add(cameraModelCombo, 1, 1);

        // serial number
        cameraSerialNumberTextField.setMaxWidth(350);
        cameraSerialNumberTextField.setStyle(REQUIRED_STYLE);
        cameraSerialNumberLabel = new Label(language.getString("project_camera_pane_serial_number_label"));
        grid.add(cameraSerialNumberLabel, 0, 2);
        grid.add(cameraSerialNumberTextField, 1, 2);

        // purchase year
        List<Integer> years = new ArrayList<>();
        for (int i = new Date().getYear() + 1900; i >= 2000; i--) {
            years.add(i);
        }
        yearList = FXCollections.observableList(years);
        cameraPurchaseYearCombo.setItems(yearList);
        cameraPurchaseYearCombo.setValue(yearList.get(0));
        cameraPurchaseYearCombo.setMaxWidth(350);
        cameraPurchaseYearCombo.setStyle(REQUIRED_STYLE);
        cameraPurchaseYearLabel = new Label(language.getString("project_camera_pane_purchase_year_label"));
        grid.add(cameraPurchaseYearLabel, 0, 3);
        grid.add(cameraPurchaseYearCombo, 1, 3);

        // button
        saveButton = new Button(language.getString("project_camera_pane_save_button"));
        saveButton.setId("project_camera_pane_save_button");

        deleteButton = new Button(language.getString("project_camera_pane_delete_button"));
        deleteButton.setId("project_camera_pane_delete_button");

        cancelButton = new Button(language.getString("project_camera_pane_cancel_button"));
        cancelButton.setId("project_camera_pane_cancel_button");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                stopEditing();
            }
        });

        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().add(saveButton);
        grid.add(hbBtn, 1, 5);

        // set action handler
        cameraMakerCombo.setOnAction((event) -> {
            String makerName = (String) cameraMakerCombo.getSelectionModel().getSelectedItem();
            ObservableList<String> modelList = FXCollections.observableList(getModels(makerName));
            cameraModelCombo.setItems(modelList);
            String modelName = modelList.iterator().next();
            if (!models.isEmpty()) {
                cameraModelCombo.setValue(modelName);
            }
        });

        return grid;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        this.deleteButton.setOnAction(controller);
        //this.cancelButton.setOnAction(controller);
    }

    @Override
    public void setLanguage(LanguageModel language) {

        this.language = language;

        if (this.saveButton.getId().equals("project_camera_pane_save_button")) {
            this.titleLabel.setText(language.getString("project_camera_pane_title") + " : " + project.getName());
            this.saveButton.setText(language.getString("project_camera_pane_save_button"));
        } else if (this.saveButton.getId().equals("project_camera_pane_update_button")) {
            this.titleLabel.setText(language.getString("project_camera_pane_edit_title") + " : " + project.getName());
            this.saveButton.setText(language.getString("project_camera_pane_update_button"));
        }

        this.cameraMakerLabel.setText(language.getString("project_camera_pane_maker_label"));
        this.cameraModelLabel.setText(language.getString("project_camera_pane_model_label"));
        this.cameraSerialNumberLabel.setText(language.getString("project_camera_pane_serial_number_label"));
        this.cameraPurchaseYearLabel.setText(language.getString("project_camera_pane_purchase_year_label"));
        this.deleteButton.setText(language.getString("project_camera_pane_delete_button"));
        this.cancelButton.setText(language.getString("project_camera_pane_cancel_button"));
        this.cameraTable.setPlaceholder(new Label(language.getString("project_camera_pane_empty_table_message")));

        this.makerColumn.setText(language.getString("project_camera_pane_maker_label"));
        this.modelColumn.setText(language.getString("project_camera_pane_model_label"));
        this.purchaseYearColumn.setText(language.getString("project_camera_pane_purchase_year_label"));
        this.serialNumberColumn.setText(language.getString("project_camera_pane_serial_number_label"));
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    private List<String> getMakers() {
        List<String> makers = new ArrayList<>();
        TreeSet<String> set = new TreeSet<>();
        this.cameraModels.stream().forEach((cm) -> {
            set.add(cm.getMaker());
        });
        makers.addAll(set);
        return makers;
    }

    private List<String> getModels(String maker) {
        List<String> models = new ArrayList<>();
        TreeSet<String> set = new TreeSet<>();
        cameraModels.stream().filter((cm) -> (cm.getMaker().equals(maker))).forEach((cm) -> {
            set.add(cm.getName());
        });
        models.addAll(set);
        return models;
    }

    public Project getProject() {
        return this.project;
    }

    public boolean canDeleteCamera() {
        boolean ok = true;

        ProjectService projectService = new ProjectServiceImpl();
        Camera camera = this.getCamera();

        // check if the event is used in a deployment
        if (!projectService.getDeployments(camera.getProject()).isEmpty()) {

            String title = language.getString("title_error");
            String header = language.getString("project_camera_in_use_error_header");
            String context = language.getString("project_camera_in_use_error_context");

            Util.alertErrorPopup(
                    title,
                    header,
                    context,
                    language.getString("alert_ok"));

            ok = false;
        }

        return ok;
    }

    public boolean validate() {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        String serialNumber = cameraSerialNumberTextField.getText();
        if (serialNumber == null || serialNumber.trim().equals("")) {
            title = language.getString("title_error");
            header = language.getString("empty_camera_serial_number_error_header");
            context = language.getString("empty_camera_serial_number_error_context");
            ok = false;
        } else {
            for (Camera camera : project.getCameras()) {

                if (this.saveButton.getId().equals("project_camera_pane_update_button")) {
                    Camera cam = (Camera) (cameraTable.getSelectionModel().getSelectedItem());
                    if (camera.getCameraId().intValue() == cam.getCameraId().intValue()) {
                        continue;
                    }
                }

                if (camera.getSerialNumber().equals(serialNumber)) {
                    title = language.getString("title_error");
                    header = language.getString("duplicate_camera_serial_number_error_header");
                    context = language.getString("duplicate_camera_serial_number_error_context");
                    ok = false;
                    break;
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

    public Camera getCamera() {

        if (this.saveButton.getId().equals("project_camera_pane_save_button")) {
            Camera camera = new Camera();
            camera.setSerialNumber(cameraSerialNumberTextField.getText());
            camera.setProject(this.project);
            camera.setYearPurchased((Integer) this.cameraPurchaseYearCombo.getValue());

            String maker = (String) this.cameraMakerCombo.getValue();
            String model = (String) this.cameraModelCombo.getValue();
            camera.setCameraModel(getCameraModel(maker, model));
            return camera;
        } else if (this.saveButton.getId().equals("project_camera_pane_update_button")) {
            Camera camera = (Camera) (cameraTable.getSelectionModel().getSelectedItem());
            camera.setSerialNumber(cameraSerialNumberTextField.getText());
            camera.setYearPurchased((Integer) this.cameraPurchaseYearCombo.getValue());

            String maker = (String) this.cameraMakerCombo.getValue();
            String model = (String) this.cameraModelCombo.getValue();
            camera.setCameraModel(getCameraModel(maker, model));
            return camera;
        } else {
            return null;
        }
    }

    private CameraModel getCameraModel(String maker, String model) {

        for (CameraModel cm : cameraModels) {
            if (cm.getMaker().equals(maker) && cm.getName().equals(model)) {
                return cm;
            }
        }
        return null;
    }

    public void createNewCamera(Camera camera) {

        String maker = makers.iterator().next();
        if (!makers.isEmpty()) {
            this.cameraMakerCombo.setValue(maker);
        }

        ObservableList<String> models = FXCollections.observableList(getModels(maker));
        this.cameraModelCombo.setItems(models);
        String model = models.iterator().next();
        if (!models.isEmpty()) {
            this.cameraModelCombo.setValue(model);
        }

        this.cameraSerialNumberTextField.clear();
        this.cameraPurchaseYearCombo.setValue(yearList.get(0));

        populateCameraTableView();

        cameraTable.getSelectionModel().select(camera);
        cameraTable.scrollTo(camera);
    }

    public void updateCamera(Camera camera) {

        populateCameraTableView();
        cameraTable.getSelectionModel().select(camera);
        cameraTable.scrollTo(camera);
    }

    public void removeCamera(Camera camera) {

        populateCameraTableView();
        stopEditing();
    }

    public void startEditing(Camera camera) {

        // change the title
        this.titleLabel.setText(language.getString("project_camera_pane_edit_title") + " : " + project.getName());

        // setup fields
        this.cameraSerialNumberTextField.setText(camera.getSerialNumber());
        this.cameraPurchaseYearCombo.setValue(camera.getYearPurchased());

        String maker = camera.getCameraModel().getMaker();
        this.cameraMakerCombo.setValue(maker);

        ObservableList<String> modelList = FXCollections.observableList(getModels(maker));
        this.cameraModelCombo.setItems(modelList);
        this.cameraModelCombo.setValue(camera.getCameraModel().getName());

        if (this.saveButton.getId().equals("project_camera_pane_save_button")) {
            this.hbBtn.getChildren().add(deleteButton);
            this.hbBtn.getChildren().add(cancelButton);
        }

        // setup buttons
        this.saveButton.setText(language.getString("project_camera_pane_update_button"));
        this.saveButton.setId("project_camera_pane_update_button");

        // show the image to remind the user the current status
        this.imgView.setImage(editImg);

    }

    public void stopEditing() {

        // change the title
        this.titleLabel.setText(language.getString("project_camera_pane_title") + " : " + project.getName());

        // setup fields
        this.cameraSerialNumberTextField.setText(null);
        this.cameraPurchaseYearCombo.setValue(yearList.get(0));

        String maker = makers.get(0);
        this.cameraMakerCombo.setValue(maker);

        ObservableList<String> modelList = FXCollections.observableList(getModels(maker));
        this.cameraModelCombo.setItems(modelList);
        this.cameraModelCombo.setValue(modelList.get(0));

        // setup buttons
        this.saveButton.setText(language.getString("project_camera_pane_save_button"));
        this.saveButton.setId("project_camera_pane_save_button");

        this.hbBtn.getChildren().remove(deleteButton);
        this.hbBtn.getChildren().remove(cancelButton);

        this.imgView.setImage(pageImg);

        this.cameraTable.getSelectionModel().clearSelection();

    }

    public void addCameraModel(CameraModel cameraModel) {

        this.cameraModels.add(cameraModel);
        String maker = (String) this.cameraMakerCombo.getValue();
        this.cameraModelCombo.setItems(FXCollections.observableList(getModels(maker)));
    }

    public void updateCameraModel(String oldMaker, String oldModel, CameraModel cameraModel) {

        CameraModel oldCameraModel = null;
        for (CameraModel cm : this.cameraModels) {
            if (cm.getMaker().equals(oldMaker) && cm.getName().equals(oldModel)) {
                oldCameraModel = cm;
                break;
            }
        }
        if (oldCameraModel != null) {
            this.cameraModels.remove(oldCameraModel);
        }

        this.cameraModels.add(cameraModel);
        String maker = (String) this.cameraMakerCombo.getValue();
        this.cameraModelCombo.setItems(FXCollections.observableList(getModels(maker)));

    }

    public void deleteCameraModel(CameraModel cameraModel) {

        CameraModel oldCameraModel = null;
        for (CameraModel cm : this.cameraModels) {
            if (cm.getMaker().equals(cameraModel.getMaker()) && cm.getName().equals(cameraModel.getName())) {
                oldCameraModel = cm;
                break;
            }
        }
        if (oldCameraModel != null) {
            this.cameraModels.remove(oldCameraModel);
        }

        this.cameraModels.remove(oldCameraModel);
        String maker = (String) this.cameraMakerCombo.getValue();
        this.cameraModelCombo.setItems(FXCollections.observableList(getModels(maker)));
    }

}
