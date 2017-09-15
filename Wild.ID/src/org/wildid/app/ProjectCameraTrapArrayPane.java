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

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.wildid.entity.CameraTrapArrayComparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import static org.wildid.app.WildIDController.log;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Project;
import org.wildid.service.CameraTrapArrayService;
import org.wildid.service.CameraTrapArrayServiceImpl;
import org.wildid.service.CameraTrapService;
import org.wildid.service.CameraTrapServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectCameraTrapArrayPane extends WildIDDataPane {

    private LanguageModel language;
    private Project project;
    protected List<CameraTrapArray> arrays;

    protected Label titleLabel;
    protected Label arrayNameLabel;
    protected TextField arrayNameTextField = new TextField();

    protected TableView<CameraTrapArray> arrayTable = new TableView<>();
    protected TableColumn<CameraTrapArray, String> nameColumn;

    protected Button saveButton;
    protected Button deleteButton;
    protected Button cancelButton;
    protected Button bulkUploadArray;

    protected HBox hbBtn = new HBox(10);
    protected GridPane grid;
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");
    protected Image editImg = new Image("resources/icons/page_edit.png");
    protected List<CameraTrapArray> newCtArrays;

    public ProjectCameraTrapArrayPane(LanguageModel language, Project project) {

        this.language = language;
        this.project = project;

        this.arrays = new ArrayList<>();
        TreeSet<CameraTrapArray> arraySet = new TreeSet<>(new CameraTrapArrayComparator());
        arraySet.addAll(project.getCameraTrapArrays());
        this.arrays.addAll(arraySet);

        this.titleLabel = new Label(language.getString("project_array_pane_title") + " : " + project.getName());
        this.titleLabel.setStyle(TITLE_STYLE);

        this.imgView = new ImageView(pageImg);
        this.imgView.setVisible(true);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel, this.imgView);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.prefWidthProperty().bind(this.widthProperty());

        arrayTable.prefHeightProperty().bind(this.heightProperty().subtract(180));
        arrayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        arrayTable.setPlaceholder(new Label(language.getString("project_array_pane_empty_table_message")));
        arrayTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                CameraTrapArray array = (CameraTrapArray) newSelection;
                startEditing(array);
            }
        });

        vbox.getChildren().addAll(titleBox, createForm(), arrayTable);
        this.getChildren().add(vbox);

        populateArrayTableView();
    }

    public ProjectCameraTrapArrayPane(LanguageModel language, CameraTrapArray array, List<CameraTrapArray> arrays) {

        this.language = language;
        this.project = array.getProject();
        this.arrays = arrays;

        this.titleLabel = new Label(language.getString("project_array_pane_edit_title") + " : " + project.getName());
        this.titleLabel.setStyle(TITLE_STYLE);

        this.imgView = new ImageView(editImg);
        this.imgView.setVisible(true);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel, this.imgView);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.prefWidthProperty().bind(this.widthProperty());

        arrayTable.prefHeightProperty().bind(this.heightProperty().subtract(180));
        arrayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        arrayTable.setPlaceholder(new Label(language.getString("project_array_pane_empty_table_message")));
        arrayTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                CameraTrapArray cameraTrapArray = (CameraTrapArray) newSelection;
                startEditing(cameraTrapArray);
            }
        });

        vbox.getChildren().addAll(titleBox, createForm(), arrayTable);
        this.getChildren().add(vbox);

        populateArrayTableView();

        this.arrayNameTextField.setText(array.getName());
        this.arrayTable.getSelectionModel().select(array);
    }

    private Pane createForm() {

        this.grid = new GridPane();
        this.grid.setAlignment(Pos.TOP_LEFT);
        this.grid.setPadding(new Insets(30, 10, 50, 50));
        this.grid.setHgap(20);
        this.grid.setStyle(TEXT_STYLE);
        this.grid.setVgap(10);

        this.arrayNameTextField.setMinWidth(350);
        this.arrayNameTextField.setStyle(REQUIRED_STYLE);
        this.arrayNameTextField.setText(getDefaultArrayName(project, arrays));
        this.arrayNameLabel = new Label(language.getString("project_array_pane_name_label"));
        this.grid.add(this.arrayNameLabel, 0, 0);
        this.grid.add(this.arrayNameTextField, 1, 0);

        this.saveButton = new Button(language.getString("project_array_pane_save_button"));
        this.saveButton.setId("project_array_pane_save_button");

        this.deleteButton = new Button(language.getString("project_array_pane_delete_button"));
        this.deleteButton.setId("project_array_pane_delete_button");

        this.cancelButton = new Button(language.getString("project_array_pane_cancel_button"));
        this.cancelButton.setId("project_array_pane_cancel_button");
        this.cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                stopEditing();
            }
        });

        this.bulkUploadArray = new Button(language.getString("project_array_pane_bulk_upload_button"));
        this.bulkUploadArray.setId("project_array_pane_bulk_upload_button");

        this.hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        this.hbBtn.getChildren().addAll(saveButton, bulkUploadArray);
        this.grid.add(hbBtn, 1, 2);

        return grid;
    }

    // populate the tableview
    private void populateArrayTableView() {

        arrays = new ArrayList<>();
        TreeSet<CameraTrapArray> arraySet = new TreeSet<>(new CameraTrapArrayComparator());
        arraySet.addAll(project.getCameraTrapArrays());
        arrays.addAll(arraySet);
        arrayTable.setItems(FXCollections.observableList(arrays));

        TableColumn<CameraTrapArray, Number> indexColumn = new TableColumn<>("");
        indexColumn.setSortable(false);
        indexColumn.setMinWidth(40);
        indexColumn.setMaxWidth(40);
        indexColumn.setPrefWidth(40);
        indexColumn.setStyle(INDEX_COLUMN_STYLE);
        indexColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(arrayTable.getItems().indexOf(column.getValue()) + 1));

        nameColumn = new TableColumn<>(language.getString("project_array_pane_name_label"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setMinWidth(300);
        nameColumn.setMaxWidth(300);
        nameColumn.setPrefWidth(300);
        //nameColumn.prefWidthProperty().bind(arrayTable.widthProperty().subtract(550));

        arrayTable.getColumns().setAll(indexColumn, nameColumn);
    }

    @Override
    public void setLanguage(LanguageModel language) {

        this.language = language;
        if (this.saveButton.getId().equals("project_array_pane_save_button")) {
            this.titleLabel.setText(language.getString("project_array_pane_title") + " : " + project.getName());
            this.saveButton.setText(language.getString("project_array_pane_save_button"));
            this.bulkUploadArray.setText(language.getString("project_array_pane_bulk_upload_button"));
        } else if (this.saveButton.getId().equals("project_array_pane_update_button")) {
            this.titleLabel.setText(language.getString("project_array_pane_edit_title") + " : " + project.getName());
            this.saveButton.setText(language.getString("project_array_pane_update_button"));
        }

        this.arrayNameLabel.setText(language.getString("project_array_pane_name_label"));
        this.nameColumn.setText(language.getString("project_array_pane_name_label"));

        this.deleteButton.setText(language.getString("project_array_pane_delete_button"));
        this.cancelButton.setText(language.getString("project_array_pane_cancel_button"));
        this.arrayTable.setPlaceholder(new Label(language.getString("project_array_pane_empty_table_message")));

    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        this.deleteButton.setOnAction(controller);
        this.bulkUploadArray.setOnAction(controller);
        //this.cancelButton.setOnAction(controller);
    }

    private String getDefaultArrayName(Project project, List<CameraTrapArray> arrays) {

        int i = 1;
        boolean ok = false;
        String name = null;
        while (!ok) {
            name = language.getString("project_array_default_prefix") + "-" + project.getAbbrevName() + "-" + i;
            CameraTrapArray tmp = new CameraTrapArray();
            tmp.setName(name);

            boolean found = false;
            for (CameraTrapArray array : arrays) {

                if (new CameraTrapArrayComparator().compare(tmp, array) == 0) {
                    //if (array.getName().equals(name)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                i++;
            } else {
                break;
            }

        }
        return name;
    }

    public Project getProject() {
        return this.project;
    }

    public boolean validate() {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        String name = arrayNameTextField.getText();
        if (name == null || name.trim().equals("")) {
            title = language.getString("title_error");
            header = language.getString("empty_array_name_error_header");
            context = language.getString("empty_array_name_error_context");
            ok = false;
        } else {
            for (CameraTrapArray array : project.getCameraTrapArrays()) {

                if (this.saveButton.getId().equals("project_array_pane_update_button")) {
                    CameraTrapArray selectedArray = (CameraTrapArray) (arrayTable.getSelectionModel().getSelectedItem());
                    if (Objects.equals(array.getCameraTrapArrayId(), selectedArray.getCameraTrapArrayId())) {
                        continue;
                    }
                }

                if (array.getName().equals(name)) {
                    title = language.getString("title_error");
                    header = language.getString("duplicate_array_name_error_header");
                    context = language.getString("duplicate_array_name_error_context");
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

    public CameraTrapArray getCameraTrapArray() {

        if (this.saveButton.getId().equals("project_array_pane_save_button")) {
            CameraTrapArray array = new CameraTrapArray();
            array.setName(arrayNameTextField.getText());
            array.setProject(this.project);
            return array;
        } else if (this.saveButton.getId().equals("project_array_pane_update_button")) {
            CameraTrapArray array = (CameraTrapArray) (arrayTable.getSelectionModel().getSelectedItem());
            array.setName(arrayNameTextField.getText());
            return array;
        } else {
            return null;
        }
    }

    public void createNewCameraTrapArray(CameraTrapArray array) {

        populateArrayTableView();
        this.arrayNameTextField.setText(getDefaultArrayName(project, arrays));

        this.arrayTable.getSelectionModel().select(array);
        this.arrayTable.scrollTo(array);
    }

    public void removeCameraTrapArray(CameraTrapArray array) {

        populateArrayTableView();
        stopEditing();
    }

    public void startEditing(CameraTrapArray array) {

        // change the title
        this.titleLabel.setText(language.getString("project_array_pane_edit_title") + " : " + project.getName());

        // setup fields
        this.arrayNameTextField.setText(array.getName());

        // setup buttons
        if (this.saveButton.getId().equals("project_array_pane_save_button")) {
            this.hbBtn.getChildren().add(deleteButton);
            this.hbBtn.getChildren().add(cancelButton);
        }

        // setup buttons
        this.saveButton.setText(language.getString("project_array_pane_update_button"));
        this.saveButton.setId("project_array_pane_update_button");

        // show the image to remind the user the current status
        this.imgView.setImage(editImg);

    }

    public void stopEditing() {

        // change the title
        this.titleLabel.setText(language.getString("project_array_pane_title") + " : " + project.getName());

        // setup fields
        this.arrayNameTextField.setText(getDefaultArrayName(project, arrays));

        // setup buttons
        this.saveButton.setText(language.getString("project_array_pane_save_button"));
        this.saveButton.setId("project_array_pane_save_button");

        this.hbBtn.getChildren().remove(deleteButton);
        this.hbBtn.getChildren().remove(cancelButton);

        this.imgView.setImage(pageImg);

    }

    public void updateCameraTrapArray(CameraTrapArray array) {

        populateArrayTableView();
        arrayTable.getSelectionModel().select(array);
        arrayTable.scrollTo(array);
    }

    public List<CameraTrap> bulkUploadCTTrapsInMultipleArrays() {

        List<CameraTrap> newCameraTraps = new ArrayList<>();
        newCtArrays = new ArrayList<>();

        String title = language.getString("title_information");
        String header = language.getString("bulk_upload_traps_confirm_header");
        String context = language.getString("bulk_upload_traps_confirm_multiple_context");

        boolean confirmed = Util.alertConfirmPopup(
                title,
                header,
                context,
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            try {
                FileChooser fileChooser = new FileChooser();

                if (WildID.wildIDProperties.getWorkingDirObj() != null) {
                    fileChooser.setInitialDirectory(WildID.wildIDProperties.getWorkingDirObj());
                }

                fileChooser.setTitle("Select CSV File");
                ExtensionFilter extFilter_csv = new ExtensionFilter(language.getString("csv_extension_filter"), "*.csv");
                fileChooser.getExtensionFilters().addAll(extFilter_csv);

                File csvFile = fileChooser.showOpenDialog(null);

                if (csvFile != null) {
                    //Project project = arrayPane.getProject();

                    Set<CameraTrapArray> currentArraysSet = project.getCameraTrapArrays();
                    List<CameraTrap> currentTraps = new ArrayList<>();

                    currentArraysSet.stream().forEach((anArray) -> {
                        currentTraps.addAll(anArray.getCameraTraps());
                    });

                    //List<CameraTrap> traps = arrayPane.getCameraTrapList();
                    CSVReader reader = new CSVReader(new FileReader(csvFile), ',');

                    String[] tokens;
                    int row = 0;
                    boolean ok = true;

                    while ((tokens = reader.readNext()) != null) {
                        row++;

                        if (tokens != null) {
                            // Skip empty line
                            if (tokens.length == 1 && tokens[0].trim().equals("")) {
                                continue;
                            }

                            if (tokens.length != 4) {
                                title = language.getString("title_error");
                                header = language.getString("bulk_upload_traps_not_4_columns_error_header");
                                context = language.getString("bulk_upload_traps_not_4_columns_error_context");
                                ok = false;
                                break;
                            }

                            String token_array_name = tokens[0].trim();
                            String token_ct_name = tokens[1].trim();
                            String token_lat = tokens[2].trim();
                            String token_lng = tokens[3].trim();

                            if (token_array_name.trim().isEmpty()) {
                                title = language.getString("title_error");
                                header = language.getString("empty_array_name_error_header");
                                context = language.getString("empty_array_name_error_context");
                                ok = false;
                                break;
                            } else if (token_ct_name.trim().isEmpty()) {
                                title = language.getString("title_error");
                                header = language.getString("empty_trap_name_error_header");
                                context = language.getString("empty_trap_name_error_context");
                                ok = false;
                                break;
                            } else {
                                for (CameraTrap aTrap : currentTraps) {
                                    if (aTrap.getName().equals(token_ct_name)) {
                                        title = language.getString("title_error");
                                        header = language.getString("duplicate_trap_name_error_header");
                                        context = language.getString("duplicate_trap_name_error_context") + ": " + token_ct_name;
                                        ok = false;
                                        break;
                                    }
                                }

                                if (!ok) {
                                    break;
                                }
                            }

                            Double lat;
                            Double lng;

                            try {
                                lat = Double.valueOf(token_lat);

                                if (lat < -90 || lat > 90) {
                                    title = language.getString("title_error");
                                    header = language.getString("bulk_upload_traps_lat_lon_error_header");
                                    context = language.getString("bulk_upload_traps_lat_error_context");

                                    ok = false;
                                    break;
                                }
                            } catch (Exception ex) {
                                lat = null;
                            }

                            try {
                                lng = Double.valueOf(token_lng);

                                if (lng < -180 || lng > 180) {
                                    title = language.getString("title_error");
                                    header = language.getString("bulk_upload_traps_lat_lon_error_header");
                                    context = language.getString("bulk_upload_traps_lon_error_context");

                                    ok = false;
                                    break;
                                }
                            } catch (Exception ex) {
                                lng = null;
                            }

                            if (lat == null && lng != null) {
                                title = language.getString("title_error");
                                header = language.getString("bulk_upload_traps_lat_lon_error_header");
                                context = language.getString("bulk_upload_traps_lat_error_context");

                                ok = false;
                                break;
                            }

                            if (lat != null && lng == null) {
                                title = language.getString("title_error");
                                header = language.getString("bulk_upload_traps_lat_lon_error_header");
                                context = language.getString("bulk_upload_traps_lon_error_context");

                                ok = false;
                                break;
                            }

                            boolean isNewArray = true;

                            for (CameraTrapArray array : currentArraysSet) {
                                if (array.getName().equals(token_array_name)) {
                                    newCameraTraps.add(new CameraTrap(array, token_ct_name, lat, lng, null));
                                    isNewArray = false;
                                    break;
                                }
                            }

                            if (isNewArray) {
                                CameraTrapArray newArray = new CameraTrapArray(project, token_array_name);
                                newCameraTraps.add(new CameraTrap(newArray, token_ct_name, lat, lng, null));
                                currentArraysSet.add(newArray);
                                newCtArrays.add(newArray);
                            }
                        }
                        // End 1 row in CSV
                    }
                    // End while loop
                    reader.close();

                    if (!ok) {
                        String errorLine = language.getString("bulk_upload_traps_error_line") + " " + row;
                        Util.alertErrorPopup(title, header, errorLine + "\n" + context, language.getString("alert_ok"));

                    } else {
                        CameraTrapService trapService = new CameraTrapServiceImpl();
                        CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
                        // First, do insert new CTArrays 
                        for (CameraTrapArray ctArray : newCtArrays) {
                            log.info("Insert new CTArray: " + ctArray.getName());
                            arrayService.addCameraTrapArray(ctArray);
                            createNewCameraTrapArray(ctArray);
                            project.getCameraTrapArrays().add(ctArray);
                        }

                        // Then, do insert new CTs
                        for (CameraTrap trap : newCameraTraps) {
                            log.info("New CT: " + trap.getCameraTrapArray().getName() + ", CT Name: " + trap.getName());
                            trapService.addCameraTrap(trap);
                            trap.getCameraTrapArray().getCameraTraps().add(trap);
                        }

                        Util.alertInformationPopup(
                                language.getString("title_success"),
                                language.getString("bulk_upload_traps_success_header"),
                                language.getString("bulk_upload_traps_success_multiple_context"),
                                language.getString("alert_ok"));

                    }
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ProjectCameraTrapArrayPane.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        return newCameraTraps;
    }

    public List<CameraTrapArray> getNewCameraTrapArraysBulkUpload() {
        return this.newCtArrays;
    }
}
