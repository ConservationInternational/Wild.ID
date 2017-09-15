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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.wildid.entity.Event;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectEventPane extends WildIDDataPane {

    private LanguageModel language;
    private Project project;

    protected Label titleLabel;
    protected Label eventNameLabel;
    protected Label eventDescLabel;
    protected TextField eventNameTextField = new TextField();
    protected TextArea eventDescTextArea = new TextArea();

    protected TableView<Event> eventTable = new TableView<>();
    protected TableColumn<Event, String> nameColumn;
    protected TableColumn<Event, String> descColumn;

    protected Button saveButton;
    protected Button deleteButton;
    protected Button cancelButton;

    protected HBox hbBtn = new HBox(10);
    protected GridPane grid;
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");
    protected Image editImg = new Image("resources/icons/page_edit.png");

    public ProjectEventPane(LanguageModel language, Project project) {

        this.language = language;
        this.project = project;
        this.setStyle(BG_COLOR_STYLE);

        titleLabel = new Label(language.getString("project_event_pane_create_event_title") + " : " + project.getName());
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

        eventTable.prefHeightProperty().bind(this.heightProperty().subtract(250));
        eventTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        eventTable.setPlaceholder(new Label(language.getString("project_event_pane_empty_table_message")));
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Event event = (Event) newSelection;
                startEditing(event);
            }
        });

        vbox.getChildren().addAll(titleBox, createForm(), eventTable);
        this.getChildren().add(vbox);

        populateEventTableView();
    }

    public ProjectEventPane(LanguageModel language, Event event) {

        this(language, event.getProject());
        startEditing(event);
    }

    private Pane createForm() {

        grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setPadding(new Insets(30, 10, 30, 50));
        grid.setHgap(20);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        // name
        eventNameTextField.setMaxWidth(450);
        eventNameTextField.setStyle(REQUIRED_STYLE);
        eventNameLabel = new Label(language.getString("project_event_pane_name_label"));
        grid.add(eventNameLabel, 0, 0);
        grid.add(eventNameTextField, 1, 0);

        // description
        eventDescTextArea.setPrefRowCount(3);
        eventDescTextArea.setMaxWidth(450);
        eventDescLabel = new Label(language.getString("project_event_pane_desc_label"));
        GridPane.setValignment(eventDescLabel, VPos.TOP);
        grid.add(eventDescLabel, 0, 1);
        grid.add(eventDescTextArea, 1, 1);

        // button
        saveButton = new Button(language.getString("project_event_pane_create_event_button"));
        saveButton.setId("project_event_pane_create_event_button");

        deleteButton = new Button(language.getString("project_event_pane_delete_button"));
        deleteButton.setId("project_event_pane_delete_button");

        cancelButton = new Button(language.getString("project_cancel_pane_cancel_button"));
        cancelButton.setId("project_cancel_pane_cancel_button");
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

    private void populateEventTableView() {

        List<Event> events = new ArrayList<>();
        events.addAll(project.getEvents());
        eventTable.setItems(FXCollections.observableList(events));

        TableColumn<Event, Number> indexColumn = new TableColumn<>("");
        indexColumn.setSortable(false);
        indexColumn.setMinWidth(40);
        indexColumn.setMaxWidth(40);
        indexColumn.setPrefWidth(40);
        indexColumn.setStyle(INDEX_COLUMN_STYLE);
        indexColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(eventTable.getItems().indexOf(column.getValue()) + 1));

        nameColumn = new TableColumn<>(language.getString("project_event_pane_name_label"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setMinWidth(250);
        nameColumn.setMaxWidth(250);
        nameColumn.setPrefWidth(250);

        descColumn = new TableColumn<>(language.getString("project_event_pane_desc_label"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descColumn.setMinWidth(450);
        descColumn.setMaxWidth(450);
        descColumn.setPrefWidth(450);

        eventTable.getColumns().setAll(indexColumn, nameColumn, descColumn);

    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        this.deleteButton.setOnAction(controller);
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;

        if (this.saveButton.getId().equals("project_event_pane_create_event_button")) {
            this.titleLabel.setText(language.getString("project_event_pane_create_event_title") + " : " + project.getName());
            this.saveButton.setText(language.getString("project_event_pane_create_event_button"));
        } else if (this.saveButton.getId().equals("project_event_pane_update_event_button")) {
            this.titleLabel.setText(language.getString("project_event_pane_edit_event_title") + " : " + project.getName());
            this.saveButton.setText(language.getString("project_event_pane_update_event_button"));
        }

        this.eventTable.setPlaceholder(new Label(language.getString("project_event_pane_empty_table_message")));
        this.eventNameLabel.setText(language.getString("project_event_pane_name_label"));
        this.eventDescLabel.setText(language.getString("project_event_pane_desc_label"));
        this.deleteButton.setText(language.getString("project_event_pane_delete_button"));
        this.cancelButton.setText(language.getString("project_cancel_pane_cancel_button"));
        this.nameColumn.setText(language.getString("project_event_pane_name_label"));
        this.descColumn.setText(language.getString("project_event_pane_desc_label"));
    }

    public boolean validate() {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        String name = eventNameTextField.getText();
        if (name == null || name.trim().equals("")) {
            title = language.getString("title_error");
            header = language.getString("empty_event_name_error_header");
            context = language.getString("empty_event_name_error_context");
            ok = false;
        } else {
            for (Object object : project.getEvents()) {
                Event event = (Event) object;

                if (this.saveButton.getId().equals("project_event_pane_update_event_button")) {
                    Event evt = (Event) (eventTable.getSelectionModel().getSelectedItem());
                    if (event.getEventId().intValue() == evt.getEventId().intValue()) {
                        continue;
                    }
                }

                if (event.getName().equals(name)) {
                    title = language.getString("title_error");
                    header = language.getString("duplicate_event_name_error_header");
                    context = language.getString("duplicate_event_name_error_context");
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

    public void showEventInUseError() {

        String title = language.getString("title_error");
        String header = language.getString("project_event_in_use_error_header");
        String context = language.getString("project_event_in_use_error_context");

        Util.alertErrorPopup(
                title,
                header,
                context,
                language.getString("alert_ok"));
    }

    public Project getProject() {
        return this.project;
    }

    public Event getEvent() {

        if (this.saveButton.getId().equals("project_event_pane_create_event_button")) {
            Event event = new Event();
            event.setName(eventNameTextField.getText());
            event.setDescription(eventDescTextArea.getText());
            event.setProject(project);
            return event;
        } else if (this.saveButton.getId().equals("project_event_pane_update_event_button")) {
            Event event = (Event) (eventTable.getSelectionModel().getSelectedItem());
            event.setName(eventNameTextField.getText());
            event.setDescription(eventDescTextArea.getText());
            return event;
        } else {
            return null;
        }
    }

    public void createEvent(Event event) {

        this.eventNameTextField.clear();
        this.eventDescTextArea.clear();

        populateEventTableView();

        eventTable.getSelectionModel().select(event);
        eventTable.scrollTo(event);
    }

    public void removeEvent(Event event) {

        populateEventTableView();
        stopEditing();
    }

    public void updateEvent(Event event) {
        populateEventTableView();
        eventTable.getSelectionModel().select(event);
        eventTable.scrollTo(event);
    }

    public void startEditing(Event event) {

        // change the title
        this.titleLabel.setText(language.getString("project_event_pane_edit_event_title") + " : " + project.getName());

        // setup fields
        this.eventNameTextField.setText(event.getName());
        this.eventDescTextArea.setText(event.getDescription());

        if (this.saveButton.getId().equals("project_event_pane_create_event_button")) {
            this.hbBtn.getChildren().add(deleteButton);
            this.hbBtn.getChildren().add(cancelButton);
        }

        // setup buttons
        this.saveButton.setText(language.getString("project_event_pane_update_event_button"));
        this.saveButton.setId("project_event_pane_update_event_button");

        // show the image to remind the user the current status
        this.imgView.setImage(editImg);

        this.eventTable.getSelectionModel().select(event);

    }

    public void stopEditing() {

        // change the title
        this.titleLabel.setText(language.getString("project_event_pane_create_event_title") + " : " + project.getName());

        // setup fields
        this.eventNameTextField.clear();
        this.eventDescTextArea.clear();

        // setup buttons
        this.saveButton.setText(language.getString("project_event_pane_create_event_button"));
        this.saveButton.setId("project_event_pane_create_event_button");

        this.hbBtn.getChildren().remove(deleteButton);
        this.hbBtn.getChildren().remove(cancelButton);

        this.imgView.setImage(pageImg);

        this.eventTable.getSelectionModel().clearSelection();
    }

    public LanguageModel getLanguage() {
        return this.language;
    }
}
