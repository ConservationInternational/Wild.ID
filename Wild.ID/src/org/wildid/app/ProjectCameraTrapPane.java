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
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.CameraTrapComparator;
import org.wildid.service.CameraTrapService;
import org.wildid.service.CameraTrapServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectCameraTrapPane extends WildIDDataPane {

    private LanguageModel language;
    private CameraTrapArray array;
    private Set<CameraTrapArray> arrays;
    private List<CameraTrap> traps;

    // for array 
    protected Label arrayTitleLabel;
    protected Label arrayNameLabel;
    protected TextField arrayNameTextField = new TextField();

    protected HBox arrayButtonBox = new HBox(10);
    protected GridPane arrayGrid;

    protected Button arraySaveButton;
    protected Button arrayDeleteButton;
    protected Button arrayUploadButton;

    protected ImageView arrayImgView;

    // for trap
    protected Label trapTitleLabel;
    protected Label trapNameLabel;
    protected Label trapLatitudeLabel;
    protected Label trapLongitudeLabel;

    protected TextField trapNameTextField = new TextField();
    protected TextField latitudeTextField = new NumberField(90, -90);
    protected TextField longitudeTextField = new NumberField(180, -180);

    protected TableView<CameraTrap> trapTable = new TableView<>();
    protected TableColumn<CameraTrap, String> trapNameColumn;
    protected TableColumn<CameraTrap, String> trapLatitudeColumn;
    protected TableColumn<CameraTrap, String> trapLongitudeColumn;

    protected HBox trapButtonBox = new HBox(10);
    protected GridPane trapGrid;

    protected Button trapSaveButton;
    protected Button trapDeleteButton;
    protected Button trapCancelButton;

    protected ImageView trapImgView;

    // images
    protected Image pageImg = new Image("resources/icons/page.png");
    protected Image editImg = new Image("resources/icons/page_edit.png");

    public ProjectCameraTrapPane(LanguageModel language, CameraTrapArray array) {

        this.language = language;
        this.array = array;
        this.arrays = array.getProject().getCameraTrapArrays();

        this.traps = new ArrayList<>();
        arrays.stream().forEach((anArray) -> {
            traps.addAll(anArray.getCameraTraps());
        });

        //this.arrayTab = new Tab(language.getString("project_trap_pane_array_tab_title"));
        //this.trapTab = new Tab(language.getString("project_trap_pane_trap_tab_title"));
        // setup array tab
        this.arrayTitleLabel = new Label(language.getString("project_trap_pane_edit_array_title") + " : " + array.getName());
        this.arrayTitleLabel.setStyle(TITLE_STYLE);

        this.arrayImgView = new ImageView(editImg);
        this.arrayImgView.setVisible(true);

        HBox arrayTitleBox = new HBox(15);
        arrayTitleBox.getChildren().addAll(this.arrayTitleLabel, this.arrayImgView);
        arrayTitleBox.setPadding(new Insets(10, 0, 10, 30));
        arrayTitleBox.setStyle(BG_TITLE_STYLE);

        this.arrayGrid = new GridPane();
        this.arrayGrid.setAlignment(Pos.TOP_LEFT);
        this.arrayGrid.setPadding(new Insets(30, 10, 30, 50));
        this.arrayGrid.setHgap(20);
        this.arrayGrid.setStyle(TEXT_STYLE);
        this.arrayGrid.setVgap(10);

        this.arrayNameTextField.setMinWidth(350);
        this.arrayNameTextField.setStyle(REQUIRED_STYLE);
        this.arrayNameTextField.setText(array.getName());
        this.arrayNameLabel = new Label(language.getString("project_trap_pane_array_name_label"));
        this.arrayGrid.add(this.arrayNameLabel, 0, 0);
        this.arrayGrid.add(this.arrayNameTextField, 1, 0);

        this.arraySaveButton = new Button(language.getString("project_trap_pane_array_update_button"));
        this.arraySaveButton.setId("project_trap_pane_array_update_button");

        this.arrayDeleteButton = new Button(language.getString("project_trap_pane_array_delete_button"));
        this.arrayDeleteButton.setId("project_trap_pane_array_delete_button");

        this.arrayUploadButton = new Button(language.getString("project_trap_pane_array_upload_button"));
        this.arrayUploadButton.setId("project_trap_pane_array_upload_button");

        this.arrayButtonBox.setAlignment(Pos.BOTTOM_CENTER);
        this.arrayButtonBox.getChildren().addAll(arraySaveButton, arrayDeleteButton, arrayUploadButton);
        this.arrayGrid.add(arrayButtonBox, 1, 1);

        //Horizontal separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        this.arrayGrid.add(separator, 0, 2, 3, 1);

        // setup GUI for editing camera trap 
        this.trapNameTextField.setMinWidth(350);
        this.trapNameTextField.setStyle(REQUIRED_STYLE);

        this.trapNameTextField.setText(getDefaultTrapName(traps));
        this.trapNameLabel = new Label(language.getString("project_trap_pane_trap_name_label"));
        this.arrayGrid.add(this.trapNameLabel, 0, 3);
        this.arrayGrid.add(this.trapNameTextField, 1, 3);

        this.latitudeTextField.setMaxWidth(200);
        this.trapLatitudeLabel = new Label(language.getString("trap_latitude"));
        this.arrayGrid.add(trapLatitudeLabel, 0, 4);
        this.arrayGrid.add(latitudeTextField, 1, 4);

        this.longitudeTextField.setMaxWidth(200);
        this.trapLongitudeLabel = new Label(language.getString("trap_longitude"));
        this.arrayGrid.add(trapLongitudeLabel, 0, 5);
        this.arrayGrid.add(longitudeTextField, 1, 5);

        this.trapSaveButton = new Button(language.getString("project_trap_pane_trap_save_button"));
        this.trapSaveButton.setId("project_trap_pane_trap_save_button");

        this.trapDeleteButton = new Button(language.getString("project_trap_pane_trap_delete_button"));
        this.trapDeleteButton.setId("project_trap_pane_trap_delete_button");

        this.trapCancelButton = new Button(language.getString("project_trap_pane_cancel_trap_edit_button"));
        this.trapCancelButton.setId("project_trap_pane_cancel_trap_edit_button");
        this.trapCancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                stopEditing();
            }
        });

        this.trapButtonBox.setAlignment(Pos.BOTTOM_CENTER);
        this.trapButtonBox.getChildren().addAll(trapSaveButton);
        this.arrayGrid.add(trapButtonBox, 1, 6);

        TableColumn<CameraTrap, Number> indexColumn = new TableColumn<>("");
        indexColumn.setSortable(false);
        indexColumn.setMinWidth(40);
        indexColumn.setMaxWidth(40);
        indexColumn.setPrefWidth(40);
        indexColumn.setStyle(INDEX_COLUMN_STYLE);
        indexColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(trapTable.getItems().indexOf(column.getValue()) + 1));

        this.trapNameColumn = new TableColumn<>(language.getString("project_trap_pane_trap_name_label"));
        this.trapNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.trapNameColumn.setMinWidth(300);
        this.trapNameColumn.setMaxWidth(300);
        this.trapNameColumn.setPrefWidth(300);

        this.trapLatitudeColumn = new TableColumn<>(language.getString("trap_latitude"));
        this.trapLatitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        this.trapLatitudeColumn.setMinWidth(300);
        this.trapLatitudeColumn.setMaxWidth(300);
        this.trapLatitudeColumn.setPrefWidth(300);

        this.trapLongitudeColumn = new TableColumn<>(language.getString("trap_longitude"));
        this.trapLongitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        this.trapLongitudeColumn.setMinWidth(300);
        this.trapLongitudeColumn.setMaxWidth(300);
        this.trapLongitudeColumn.setPrefWidth(300);

        this.trapTable.getColumns().setAll(indexColumn, trapNameColumn, trapLatitudeColumn, trapLongitudeColumn);

        this.trapTable.prefHeightProperty().bind(this.heightProperty().subtract(335));
        this.trapTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.trapTable.setPlaceholder(new Label(language.getString("project_trap_pane_empty_table_message")));
        this.trapTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                CameraTrap trap = (CameraTrap) newSelection;
                startEditing(trap);
            }
        });

        List<CameraTrap> arrayTraps = new ArrayList<>();
        TreeSet<CameraTrap> trapSet = new TreeSet<>(new CameraTrapComparator());
        trapSet.addAll(array.getCameraTraps());
        arrayTraps.addAll(trapSet);
        this.trapTable.setItems(FXCollections.observableList(arrayTraps));

        VBox arrayVbox = new VBox(0);
        arrayVbox.setAlignment(Pos.TOP_LEFT);
        arrayVbox.prefWidthProperty().bind(this.widthProperty());
        arrayVbox.getChildren().addAll(arrayTitleBox, arrayGrid, trapTable);

        // add the tab pane into the current pane
        this.getChildren().add(arrayVbox);

    }

    public ProjectCameraTrapPane(LanguageModel language, CameraTrap trap) {
        this(language, trap.getCameraTrapArray());

        //this.tabPane.getSelectionModel().select(trapTab);
        startEditing(trap);
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.arraySaveButton.setOnAction(controller);
        this.arrayDeleteButton.setOnAction(controller);
        this.arrayUploadButton.setOnAction(controller);
        this.trapSaveButton.setOnAction(controller);
        this.trapDeleteButton.setOnAction(controller);
        this.trapCancelButton.setOnAction(controller);
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;

        //this.arrayTab.setText(language.getString("project_trap_pane_array_tab_title"));
        //this.trapTab.setText(language.getString("project_trap_pane_trap_tab_title"));
        this.arrayTitleLabel.setText(language.getString("project_trap_pane_edit_array_title") + " : " + array.getName());
        this.arrayNameLabel.setText(language.getString("project_trap_pane_array_name_label"));
        this.arraySaveButton.setText(language.getString("project_trap_pane_array_update_button"));
        this.arrayDeleteButton.setText(language.getString("project_trap_pane_array_delete_button"));
        this.arrayUploadButton.setText(language.getString("project_trap_pane_array_upload_button"));

        if (this.trapSaveButton.getId().equals("project_trap_pane_trap_save_button")) {
            //this.trapTitleLabel.setText(language.getString("project_trap_pane_create_trap_title") + " : " + array.getProject().getName());
        } else if (this.trapSaveButton.getId().equals("project_trap_pane_trap_update_button")) {
            //this.trapTitleLabel.setText(language.getString("project_trap_pane_trap_edit_title") + " : " + array.getName());
        }

        this.trapNameLabel.setText(language.getString("project_trap_pane_trap_name_label"));
        if (this.trapSaveButton.getId().equals("project_trap_pane_trap_save_button")) {
            this.trapSaveButton.setText(language.getString("project_trap_pane_trap_save_button"));
        } else {
            this.trapSaveButton.setText(language.getString("project_trap_pane_trap_update_button"));
        }

        this.trapDeleteButton.setText(language.getString("project_trap_pane_trap_delete_button"));
        this.trapCancelButton.setText(language.getString("project_trap_pane_cancel_trap_edit_button"));

        this.trapLatitudeLabel.setText(language.getString("trap_latitude"));
        this.trapLongitudeLabel.setText(language.getString("trap_longitude"));
        this.trapTable.setPlaceholder(new Label(language.getString("project_trap_pane_empty_table_message")));

        this.trapNameColumn.setText(language.getString("project_trap_pane_trap_name_label"));
        this.trapLatitudeColumn.setText(language.getString("trap_latitude"));
        this.trapLongitudeColumn.setText(language.getString("trap_longitude"));
    }

    public CameraTrapArray getCameraTrapArray() {
        this.array.setName(arrayNameTextField.getText());
        return this.array;
    }

    public boolean validateArray() {

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
            for (CameraTrapArray anArray : array.getProject().getCameraTrapArrays()) {
                if (this.arraySaveButton.getId().equals("project_trap_pane_array_update_button")) {
                    if (Objects.equals(anArray.getCameraTrapArrayId(), array.getCameraTrapArrayId())) {
                        continue;
                    }
                }
                if (anArray.getName().equals(name)) {
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

    public boolean validateTrap() {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        String name = trapNameTextField.getText();
        if (name == null || name.trim().equals("")) {
            title = language.getString("title_error");
            header = language.getString("empty_trap_name_error_header");
            context = language.getString("empty_trap_name_error_context");
            ok = false;
        } else {
            for (CameraTrap aTrap : traps) {

                if (this.trapSaveButton.getId().equals("project_trap_pane_trap_update_button")) {
                    CameraTrap trap = (CameraTrap) (trapTable.getSelectionModel().getSelectedItem());
                    if (trap.getCameraTrapId().intValue() == aTrap.getCameraTrapId().intValue()) {
                        continue;
                    }
                }

                if (aTrap.getName().equals(name)) {
                    if (aTrap.getCameraTrapArray().getCameraTrapArrayId().intValue() == this.array.getCameraTrapArrayId().intValue()) {
                        title = language.getString("title_error");
                        header = language.getString("duplicate_trap_name_error_header");
                        context = language.getString("duplicate_trap_name_error_context");
                        ok = false;
                        break;
                    }
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

    public LanguageModel getLanguage() {
        return this.language;
    }

    public CameraTrap getCameraTrap() {

        if (this.trapSaveButton.getId().equals("project_trap_pane_trap_save_button")) {

            CameraTrap trap = new CameraTrap();
            trap.setName(trapNameTextField.getText());

            String lat = latitudeTextField.getText();
            if (lat != null && !lat.trim().equals("")) {
                trap.setLatitude(Double.parseDouble(lat));
            }

            String lng = longitudeTextField.getText();
            if (lng != null && !lng.trim().equals("")) {
                trap.setLongitude(Double.parseDouble(lng));
            }

            trap.setCameraTrapArray(this.array);
            return trap;

        } else if (this.trapSaveButton.getId().equals("project_trap_pane_trap_update_button")) {

            CameraTrap trap = (CameraTrap) (trapTable.getSelectionModel().getSelectedItem());
            trap.setName(trapNameTextField.getText());

            String lat = latitudeTextField.getText();
            if (lat != null && !lat.trim().equals("")) {
                trap.setLatitude(Double.parseDouble(lat));
            }

            String lng = longitudeTextField.getText();
            if (lng != null && !lng.trim().equals("")) {
                trap.setLongitude(Double.parseDouble(lng));
            }

            return trap;
        } else {
            return null;
        }

    }

    public void updateCameraTrapArray(CameraTrapArray array) {

        //this.arrayTitleLabel.setText(language.getString("project_trap_pane_edit_array_title") + " : " + array.getProject().getName());
        //this.trapTitleLabel.setText(language.getString("project_trap_pane_create_trap_title") + " : " + array.getName());
    }

    private String getDefaultTrapName(List<CameraTrap> traps) {

        int i = 1;
        boolean ok = false;
        String name = null;
        while (!ok) {
            name = this.array.getName() + "-" + i;
            CameraTrap tmp = new CameraTrap();
            tmp.setName(name);

            boolean found = false;
            for (CameraTrap trap : traps) {

                if (new CameraTrapComparator().compare(tmp, trap) == 0) {
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

    public void createNewCameraTrap(CameraTrap trap) {

        List<CameraTrap> trapList = new ArrayList<>();
        TreeSet<CameraTrap> trapSet = new TreeSet<>(new CameraTrapComparator());
        trapSet.addAll(array.getCameraTraps());
        trapList.addAll(trapSet);
        this.trapTable.setItems(FXCollections.observableList(trapList));
        this.traps.add(trap);

        this.trapNameTextField.setText(trap.getName());
        this.trapTable.getSelectionModel().select(trap);
        this.trapTable.scrollTo(trap);
    }

    private void startEditing(CameraTrap trap) {

        // setup fields
        this.trapNameTextField.setText(trap.getName());

        Double lat = trap.getLatitude();
        if (lat != null) {
            this.latitudeTextField.setText(lat.toString());
        } else {
            this.latitudeTextField.clear();
        }

        Double lng = trap.getLongitude();
        if (lng != null) {
            this.longitudeTextField.setText(lng.toString());
        } else {
            this.longitudeTextField.clear();
        }

        // setup buttons
        if (this.trapSaveButton.getId().equals("project_trap_pane_trap_save_button")) {
            this.trapButtonBox.getChildren().add(trapDeleteButton);
            this.trapButtonBox.getChildren().add(trapCancelButton);
        }

        // setup buttons
        this.trapSaveButton.setText(language.getString("project_trap_pane_trap_update_button"));
        this.trapSaveButton.setId("project_trap_pane_trap_update_button");

        // show the image to remind the user the current status
        //this.trapImgView.setImage(editImg);
        // change the title
        //this.trapTitleLabel.setText(language.getString("project_trap_pane_trap_edit_title") + " : " + array.getName());
        this.trapTable.getSelectionModel().select(trap);

    }

    public void stopEditing() {

        // change the title
        //this.trapTitleLabel.setText(language.getString("project_trap_pane_create_trap_title") + " : " + array.getName());
        // setup fields
        this.trapNameTextField.setText(getDefaultTrapName(traps));
        this.latitudeTextField.clear();
        this.longitudeTextField.clear();

        // setup buttons
        this.trapSaveButton.setText(language.getString("project_trap_pane_trap_save_button"));
        this.trapSaveButton.setId("project_trap_pane_trap_save_button");

        this.trapButtonBox.getChildren().remove(trapDeleteButton);
        this.trapButtonBox.getChildren().remove(trapCancelButton);

        //this.trapImgView.setImage(pageImg);
        this.trapTable.getSelectionModel().clearSelection();
    }

    public void updateCameraTrap(CameraTrap trap) {

        for (CameraTrap aTrap : traps) {
            if (aTrap.getCameraTrapId().intValue() == trap.getCameraTrapId().intValue()) {
                aTrap.setName(trap.getName());
                aTrap.setLatitude(trap.getLatitude());
                aTrap.setLongitude(trap.getLongitude());
                break;
            }
        }

        populateTrapTableView();
        this.trapTable.getSelectionModel().select(trap);
        this.trapTable.scrollTo(trap);
    }

    private void populateTrapTableView() {

        List<CameraTrap> trapList = new ArrayList<>();
        TreeSet<CameraTrap> trapSet = new TreeSet<>(new CameraTrapComparator());
        trapSet.addAll(array.getCameraTraps());
        trapList.addAll(trapSet);

        this.trapTable.setItems(FXCollections.observableList(trapList));

        TableColumn<CameraTrap, Number> indexColumn = new TableColumn<>("");
        indexColumn.setSortable(false);
        indexColumn.setMinWidth(40);
        indexColumn.setMaxWidth(40);
        indexColumn.setPrefWidth(40);
        indexColumn.setStyle(INDEX_COLUMN_STYLE);
        indexColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(trapTable.getItems().indexOf(column.getValue()) + 1));

        this.trapNameColumn = new TableColumn<>(language.getString("project_trap_pane_trap_name_label"));
        this.trapNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.trapNameColumn.setMinWidth(300);
        this.trapNameColumn.setMaxWidth(300);
        this.trapNameColumn.setPrefWidth(300);

        this.trapLatitudeColumn = new TableColumn<>(language.getString("trap_latitude"));
        this.trapLatitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        this.trapLatitudeColumn.setMinWidth(200);
        this.trapLatitudeColumn.setMaxWidth(200);
        this.trapLatitudeColumn.setPrefWidth(200);

        this.trapLongitudeColumn = new TableColumn<>(language.getString("trap_longitude"));
        this.trapLongitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        this.trapLongitudeColumn.setMinWidth(200);
        this.trapLongitudeColumn.setMaxWidth(200);
        this.trapLongitudeColumn.setPrefWidth(200);

        this.trapTable.getColumns().setAll(indexColumn, trapNameColumn, trapLatitudeColumn, trapLongitudeColumn);

    }

    public void removeCameraTrap(CameraTrap trap) {

        populateTrapTableView();
        stopEditing();
        this.traps.remove(trap);
        this.trapNameTextField.setText(getDefaultTrapName(traps));

    }

    public void showTrapInUseError() {

        String title = language.getString("title_error");
        String header = language.getString("project_trap_in_use_error_header");
        String context = language.getString("project_trap_in_use_error_context");

        Util.alertErrorPopup(
                title,
                header,
                context,
                language.getString("alert_ok"));
    }

    public List<CameraTrap> getCameraTrapList() {
        return this.traps;
    }

    public List<CameraTrap> bulkUploadCameraTrapsInSingleArray() {

        List<CameraTrap> newTraps = new ArrayList<>();

        CameraTrapArray array = getCameraTrapArray();
        CameraTrapService trapService = new CameraTrapServiceImpl();

        String title = language.getString("title_confirmation");
        String header = language.getString("bulk_upload_traps_confirm_header");
        String context = language.getString("bulk_upload_traps_confirm_context");

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

                            if (tokens.length != 3) {
                                title = language.getString("title_error");
                                header = language.getString("bulk_upload_traps_not_3_columns_error_header");
                                context = language.getString("bulk_upload_traps_not_3_columns_error_context");
                                context += ". " + language.getString("bulk_upload_traps_this_row_has")
                                        + " " + tokens.length + " " + language.getString("bulk_upload_traps_num_columns") + ": ";

                                boolean token0 = true;
                                for (String token : tokens) {
                                    if (!token0) {
                                        context += ", ";
                                    } else {
                                        token0 = false;
                                    }

                                    context += token;
                                }

                                ok = false;
                                break;
                            }

                            String token_name = tokens[0].trim();
                            String token_lat = tokens[1].trim();
                            String token_lng = tokens[2].trim();

                            if (token_name.trim().equals("")) {
                                title = language.getString("title_error");
                                header = language.getString("empty_trap_name_error_header");
                                context = language.getString("empty_trap_name_error_context");
                                ok = false;
                                break;
                            } else {
                                for (CameraTrap aTrap : traps) {
                                    if (aTrap.getName().equals(token_name)) {
                                        title = language.getString("title_error");
                                        header = language.getString("duplicate_trap_name_error_header");
                                        context = language.getString("duplicate_trap_name_error_context") + ": " + token_name;
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

                            CameraTrap trap = new CameraTrap(array, token_name, lat, lng, null);
                            newTraps.add(trap);
                        }
                    }
                    reader.close();

                    if (!ok) {
                        String errorLine = language.getString("bulk_upload_traps_error_line") + " " + row;
                        Util.alertErrorPopup(title, header, errorLine + "\n" + context, language.getString("alert_ok"));
                    } else {
                        for (CameraTrap trap : newTraps) {
                            trapService.addCameraTrap(trap);
                            array.getCameraTraps().add(trap);
                            //this.pane.createNewCameraTrap(trap);
                        }

                        Util.alertInformationPopup(
                                language.getString("title_success"),
                                language.getString("bulk_upload_traps_success_header"),
                                language.getString("bulk_upload_traps_success_context"),
                                language.getString("alert_ok"));
                    }
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ProjectCameraTrapPane.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        return newTraps;
    }

}
