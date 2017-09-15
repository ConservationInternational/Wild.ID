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

import java.util.ArrayList;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraModelComparator;
import org.wildid.service.CameraModelService;
import org.wildid.service.CameraModelServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class CameraModelNewPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;

    protected List<CameraModel> cameraModels;
    protected ObservableList<String> makers;

    protected Label titleLabel;
    protected Label cameraModelLabel;
    protected Label cameraMakerLabel;
    protected ComboBox cameraMakerCombo = new ComboBox();
    protected TextField cameraModelField = new TextField();

    protected TableView<CameraModel> cameraModelTable = new TableView<>();
    protected TableColumn<CameraModel, String> makerColumn;
    protected TableColumn<CameraModel, String> modelColumn;

    protected Button saveButton;
    protected Button deleteButton;
    protected Button cancelButton;

    protected HBox hbBtn = new HBox(10);
    protected GridPane grid;
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");
    protected Image editImg = new Image("resources/icons/page_edit.png");

    public CameraModelNewPane(LanguageModel language, List<CameraModel> cameraModels) {

        this.language = language;
        this.cameraModels = cameraModels;
        this.setStyle(BG_COLOR_STYLE);

        titleLabel = new Label(language.getString("camera_model_new_pane_title"));
        titleLabel.setStyle(TITLE_STYLE);

        this.imgView = new ImageView(pageImg);
        this.imgView.setVisible(true);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel, this.imgView);

        cameraModelTable.prefHeightProperty().bind(this.heightProperty().subtract(205));
        cameraModelTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cameraModelTable.setPlaceholder(new Label(language.getString("camera_model_new_pane_empty_table_message")));
        cameraModelTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                CameraModel cameraModel = (CameraModel) newSelection;
                startEditing(cameraModel);
            }
        });

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.prefWidthProperty().bind(this.widthProperty());

        vbox.getChildren().addAll(titleBox, createForm(), cameraModelTable);
        this.getChildren().add(vbox);

        populateCameraModelTableView();

    }

    public CameraModelNewPane(LanguageModel language, List<CameraModel> cameraModels, String maker) {
        this(language, cameraModels);
        cameraMakerCombo.setValue(maker);
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
        cameraMakerLabel = new Label(language.getString("camera_model_new_pane_maker_label"));
        grid.add(cameraMakerLabel, 0, 0);
        grid.add(cameraMakerCombo, 1, 0);

        // model name
        ObservableList<String> models = FXCollections.observableList(getModels(maker));
        cameraModelField.setMinWidth(350);
        cameraModelField.setMaxWidth(350);
        cameraModelField.setStyle(REQUIRED_STYLE);
        cameraModelLabel = new Label(language.getString("camera_model_new_pane_model_label"));
        grid.add(cameraModelLabel, 0, 1);
        grid.add(cameraModelField, 1, 1);

        // button
        saveButton = new Button(language.getString("camera_model_new_pane_save_button"));
        saveButton.setId("camera_model_new_pane_save_button");

        deleteButton = new Button(language.getString("camera_model_new_pane_delete_button"));
        deleteButton.setId("camera_model_new_pane_delete_button");

        cancelButton = new Button(language.getString("camera_model_new_pane_cancel_button"));
        cancelButton.setId("camera_model_new_pane_cancel_button");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                stopEditing();
            }
        });

        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().add(saveButton);
        grid.add(hbBtn, 1, 3);

        return grid;
    }

    // populate the tableview
    private void populateCameraModelTableView() {

        List<CameraModel> models = new ArrayList<>();
        TreeSet<CameraModel> modelSet = new TreeSet<>(new CameraModelComparator());
        modelSet.addAll(cameraModels);
        models.addAll(modelSet);

        cameraModelTable.setItems(FXCollections.observableList(models));

        TableColumn<CameraModel, Number> indexColumn = new TableColumn<>("");
        indexColumn.setSortable(false);
        indexColumn.setMinWidth(40);
        indexColumn.setMaxWidth(40);
        indexColumn.setPrefWidth(40);
        indexColumn.setStyle(INDEX_COLUMN_STYLE);
        indexColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(cameraModelTable.getItems().indexOf(column.getValue()) + 1));

        makerColumn = new TableColumn<>(language.getString("camera_model_new_pane_maker_label"));
        makerColumn.setCellValueFactory((TableColumn.CellDataFeatures<CameraModel, String> cellData) -> new SimpleStringProperty((cellData.getValue().getMaker())));
        makerColumn.setMinWidth(200);
        makerColumn.setMaxWidth(200);
        makerColumn.setPrefWidth(200);

        modelColumn = new TableColumn<>(language.getString("camera_model_new_pane_model_label"));
        modelColumn.setCellValueFactory((TableColumn.CellDataFeatures<CameraModel, String> cellData) -> new SimpleStringProperty((cellData.getValue().getName())));
        modelColumn.setMinWidth(200);
        modelColumn.setMaxWidth(200);
        modelColumn.setPrefWidth(200);

        cameraModelTable.getColumns().setAll(indexColumn, makerColumn, modelColumn);
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        this.deleteButton.setOnAction(controller);
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;

        if (this.saveButton.getId().equals("camera_model_new_pane_save_button")) {
            this.titleLabel.setText(language.getString("camera_model_new_pane_title"));
            this.saveButton.setText(language.getString("camera_model_new_pane_save_button"));
        } else if (this.saveButton.getId().equals("camera_model_new_pane_update_button")) {
            this.titleLabel.setText(language.getString("camera_model_new_pane_edit_title"));
            this.saveButton.setText(language.getString("camera_model_new_pane_update_button"));
        }

        this.cameraMakerLabel.setText(language.getString("camera_model_new_pane_maker_label"));
        this.cameraModelLabel.setText(language.getString("camera_model_new_pane_model_label"));
        this.deleteButton.setText(language.getString("camera_model_new_pane_delete_button"));
        this.cancelButton.setText(language.getString("camera_model_new_pane_cancel_button"));
        this.cameraModelTable.setPlaceholder(new Label(language.getString("camera_model_new_pane_empty_table_message")));

        this.makerColumn.setText(language.getString("camera_model_new_pane_maker_label"));
        this.modelColumn.setText(language.getString("camera_model_new_pane_model_label"));

    }

    private List<String> getMakers() {
        List<String> cmakers = new ArrayList<>();
        TreeSet<String> set = new TreeSet<>();
        this.cameraModels.stream().forEach((cm) -> {
            set.add(cm.getMaker());
        });
        cmakers.addAll(set);
        return cmakers;
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

    public boolean validate() {

        String modelName = this.cameraModelField.getText();
        List<String> models = getModels((String) this.cameraMakerCombo.getValue());

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        if (modelName == null || modelName.trim().equals("")) {
            title = language.getString("title_error");
            header = language.getString("empty_camera_model_name_error_header");
            context = language.getString("empty_camera_model_name_error_context");
            ok = false;
        } else {
            for (String model : models) {
                if (model.equals(modelName)) {
                    title = language.getString("title_error");
                    header = language.getString("duplicate_camera_model_name_error_header");
                    context = language.getString("duplicate_camera_model_name_error_context");
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

    public CameraModel getCameraModel() {

        CameraModel model = new CameraModel();
        model.setName(this.cameraModelField.getText());
        model.setMaker((String) this.cameraMakerCombo.getValue());
        return model;
    }

    public CameraModel getEditedCameraModel() {

        CameraModel model = this.cameraModelTable.getSelectionModel().getSelectedItem();
        model.setName(this.cameraModelField.getText());
        model.setMaker((String) this.cameraMakerCombo.getValue());
        return model;
    }

    public CameraModel getSelectedCameraModel() {

        CameraModel model = this.cameraModelTable.getSelectionModel().getSelectedItem();
        return model;
    }

    public void createCameraModel(CameraModel cameraModel) {

        this.cameraModels.add(cameraModel);
        makers = FXCollections.observableList(getMakers());
        cameraMakerCombo.setItems(makers);
        String maker = makers.iterator().next();
        if (!makers.isEmpty()) {
            cameraMakerCombo.setValue(maker);
        }

        populateCameraModelTableView();

    }

    public void startEditing(CameraModel cameraModel) {

        // change the title
        this.titleLabel.setText(language.getString("camera_model_new_pane_edit_title"));

        // setup fields
        this.cameraMakerCombo.setValue(cameraModel.getMaker());
        this.cameraModelField.setText(cameraModel.getName());

        // setup buttons
        if (this.saveButton.getId().equals("camera_model_new_pane_save_button")) {
            this.hbBtn.getChildren().add(deleteButton);
            this.hbBtn.getChildren().add(cancelButton);
        }

        // setup buttons
        this.saveButton.setText(language.getString("camera_model_new_pane_update_button"));
        this.saveButton.setId("camera_model_new_pane_update_button");

        // show the image to remind the user the current status
        this.imgView.setImage(editImg);

    }

    public void stopEditing() {

        // change the title
        this.titleLabel.setText(language.getString("camera_model_new_pane_title"));

        // setup fields
        this.cameraModelField.setText(null);

        // setup buttons
        this.saveButton.setText(language.getString("camera_model_new_pane_save_button"));
        this.saveButton.setId("camera_model_new_pane_save_button");

        this.hbBtn.getChildren().remove(deleteButton);
        this.hbBtn.getChildren().remove(cancelButton);

        this.imgView.setImage(pageImg);

        this.cameraModelTable.getSelectionModel().clearSelection();

    }

    public void updateCameraModel(String oldMaker, String oldModel, CameraModel cameraModel) {

        populateCameraModelTableView();
        cameraModelTable.getSelectionModel().select(cameraModel);
        //cameraModelTable.scrollTo(cameraModel);
    }

    public boolean confirmDelete() {
        return Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("camera_model_new_pane_delete_confirm_header"),
                language.getString("camera_model_new_pane_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));
    }

    public void deleteCameraModel(CameraModel cameraModel) {

        cameraModels.remove(cameraModel);
        populateCameraModelTableView();
        cameraModelField.clear();
        cameraModelTable.getSelectionModel().clearSelection();
    }

    public boolean deleteValidate(List<Camera> cameras) {

        if (!cameras.isEmpty()) {
            String title = language.getString("title_error");
            String header = language.getString("delete_camera_model_in_use_error_header");
            String context = language.getString("delete_camera_model_in_use_error_context");

            for (int i = 0; i < Math.min(cameras.size(), 3); i++) {
                context += "\n\n\t" + cameras.get(i).getSerialNumber();
            }

            Util.alertErrorPopup(
                    title,
                    header,
                    context,
                    language.getString("alert_ok"));

            return false;
        }
        return true;
    }

    public boolean deleteLastModelValidate(String maker) {
        CameraModelService service = new CameraModelServiceImpl();
        List<CameraModel> list = service.listCameraModelByMaker(maker);

        if (list == null || list.size() == 1) {
            String title = language.getString("title_error");
            String header = language.getString("delete_last_camera_model_in_maker_header");
            String context = language.getString("delete_last_camera_model_in_maker_context") + maker;

            Util.alertErrorPopup(
                    title,
                    header,
                    context,
                    language.getString("alert_ok"));

            return false;
        }

        return true;
    }

}
