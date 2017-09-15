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

import java.io.IOException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.Image;
import org.wildid.entity.ImageRepository;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectImageExifPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    protected Image image;

    protected Label titleLabel;
    public static GridPane gridForm;

    protected VBox groupVbox;
    protected TableColumn<String[], String> tagNameCol;
    protected TableColumn<String[], String> tagValueCol;
    protected TableColumn<String[], String> selected_tagNameCol;
    protected TableColumn<String[], String> selected_tagValueCol;
    protected CheckBox checkbox_numeric_format;
    protected static boolean numeric_format_checked = false;
    static Logger log = Logger.getLogger(ProjectImageExifPane.class.getName());

    private TableView<String[]> selected_table;

    public ProjectImageExifPane(
            LanguageModel language,
            Image image) throws IOException {

        this.language = language;
        this.image = image;

        this.setStyle(WildIDDataPane.TEXT_STYLE);

        Deployment deployment = image.getImageSequence().getDeployment();
        org.wildid.entity.Event event = deployment.getEvent();
        CameraTrap trap = deployment.getCameraTrap();

        this.titleLabel = new Label(language.getString("project_image_pane_title") + " : " + event.getName() + " : " + trap.getName() + " : " + image.getRawName());
        this.titleLabel.setStyle(WildIDDataPane.TITLE_STYLE);

        HBox titleBox = new HBox();
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.getChildren().addAll(titleBox, createForm());
        vbox.prefWidthProperty().bind(this.widthProperty());
        vbox.prefHeightProperty().bind(this.heightProperty());

        this.getChildren().add(vbox);
    }

    private Pane createForm() throws IOException {
        gridForm = new GridPane();
        GridPane.setHgrow(gridForm, Priority.ALWAYS);
        GridPane.setVgrow(gridForm, Priority.ALWAYS);
        gridForm.setAlignment(Pos.TOP_LEFT);
        gridForm.setPadding(new Insets(30, 10, 10, 0));
        gridForm.setHgap(10);
        gridForm.setVgap(10);
        gridForm.setStyle(TEXT_STYLE);

        ExifTool exifTool = new ExifTool();
        List<String[]> exifTags = exifTool.readFullExifTags(ImageRepository.getFile(image), numeric_format_checked);

        TableView<String[]> default_table = new TableView<>();
        default_table.setEditable(false);
        default_table.setItems(FXCollections.observableArrayList(exifTags));

        tagNameCol = new TableColumn<>(language.getString("project_image_exif_pane_tag_name"));
        tagNameCol.setCellValueFactory(column -> new SimpleStringProperty((column.getValue()[0])));
        tagNameCol.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 0 30px;");
        tagNameCol.setPrefWidth(250);
        tagNameCol.setMinWidth(200);
        tagNameCol.setMaxWidth(300);

        tagValueCol = new TableColumn<>(language.getString("project_image_exif_pane_tag_value"));
        tagValueCol.setSortable(false);
        tagValueCol.setCellValueFactory(column -> new SimpleStringProperty((column.getValue()[1])));

        default_table.getColumns().setAll(tagNameCol, tagValueCol);

        default_table.prefWidthProperty().bind(this.widthProperty());
        default_table.prefHeightProperty().bind(this.heightProperty());
        default_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        StackPane stackPane = new StackPane();

        checkbox_numeric_format = new CheckBox(language.getString("project_image_exif_pane_umeric_format"));
        checkbox_numeric_format.setSelected(numeric_format_checked);
        checkbox_numeric_format.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                numeric_format_checked = newValue;

                if (selected_table == null) {
                    try {
                        selected_table = new TableView<>();
                        selected_table.setEditable(false);

                        List<String[]> exifTags = exifTool.readFullExifTags(ImageRepository.getFile(image), numeric_format_checked);
                        selected_table.setItems(FXCollections.observableArrayList(exifTags));

                        selected_tagNameCol = new TableColumn<>(language.getString("project_image_exif_pane_tag_name"));
                        selected_tagNameCol.setCellValueFactory(column -> new SimpleStringProperty((column.getValue()[0])));
                        selected_tagNameCol.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 0 30px;");
                        selected_tagNameCol.setPrefWidth(250);
                        selected_tagNameCol.setMinWidth(200);
                        selected_tagNameCol.setMaxWidth(300);

                        selected_tagValueCol = new TableColumn<>(language.getString("project_image_exif_pane_tag_value"));
                        selected_tagValueCol.setSortable(false);
                        selected_tagValueCol.setCellValueFactory(column -> new SimpleStringProperty((column.getValue()[1])));

                        selected_table.getColumns().setAll(selected_tagNameCol, selected_tagValueCol);

                        selected_table.prefWidthProperty().bind(widthProperty());
                        selected_table.prefHeightProperty().bind(heightProperty());
                        selected_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                    } catch (IOException ex) {
                        log.info(ex.getMessage());
                    }
                }

                if (stackPane.getChildren().contains(default_table)) {
                    stackPane.getChildren().remove(default_table);
                    stackPane.getChildren().add(0, selected_table);
                } else {
                    stackPane.getChildren().remove(selected_table);
                    stackPane.getChildren().add(0, default_table);
                }
            }
        });

        stackPane.getChildren().addAll(default_table, checkbox_numeric_format);

        StackPane.setAlignment(default_table, Pos.TOP_LEFT);
        StackPane.setAlignment(checkbox_numeric_format, Pos.TOP_LEFT);
        StackPane.setMargin(checkbox_numeric_format, new Insets(-23, 0, 0, 30));

        gridForm.add(stackPane, 0, 0);
        gridForm.add(ProjectImagePane.rightPane, 1, 0);
        return gridForm;
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        Deployment deployment = image.getImageSequence().getDeployment();
        Event event = deployment.getEvent();
        CameraTrap trap = deployment.getCameraTrap();
        this.titleLabel.setText(language.getString("project_image_pane_title") + " : " + event.getName() + " : " + trap.getName() + " : " + image.getRawName());
        tagNameCol.setText(language.getString("project_image_exif_pane_tag_name"));
        tagValueCol.setText(language.getString("project_image_exif_pane_tag_value"));

        if (selected_tagNameCol != null) {
            selected_tagNameCol.setText(language.getString("project_image_exif_pane_tag_name"));
        }

        if (selected_tagValueCol != null) {
            selected_tagValueCol.setText(language.getString("project_image_exif_pane_tag_value"));
        }

        checkbox_numeric_format.setText(language.getString("project_image_exif_pane_umeric_format"));
    }

    @Override
    public void setWildIDController(WildIDController controller) {
    }
}
