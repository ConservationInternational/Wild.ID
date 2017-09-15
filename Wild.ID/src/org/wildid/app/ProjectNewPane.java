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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.wildid.entity.Country;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectStatus;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectNewPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;

    protected Label titleLabel;
    protected Label projectNameLabel;
    protected Label projectAbbrevLabel;
    protected Label projectShortNameLabel;
    protected Label projectLatitudeLabel;
    protected Label projectLlongitudeLabel;
    protected Label projectStartDateLabel;
    protected Label projectEndDateLabel;
    protected Label projectCountryLabel;
    protected Label projectTimezoneLabel;
    protected Label projectStatusLabel;
    protected Label projectObjectiveLabel;
    protected Label projectUseLabel;

    protected TextField projectNameTextField = new TextField();
    protected TextField projectAbbrevTextField = new TextField();
    protected TextField projectShortNameTextField = new TextField();
    protected TextField latitudeTextField = new NumberField(90, -90);
    protected TextField longitudeTextField = new NumberField(180, -180);
    protected DatePicker startPicker = new DatePicker();
    protected DatePicker endPicker = new DatePicker();
    protected CountryComboBox countryCombo;
    protected ComboBox<String> timezoneCombo = new ComboBox<>();
    protected ProjectStatusComboBox statusCombo;
    protected TextArea objectiveTextArea = new TextArea();
    protected TextArea useTextArea = new TextArea();
    protected Button saveButton;

    protected HBox hbBtn = new HBox(10);
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");

    public ProjectNewPane(LanguageModel language) {
        this.language = language;
        this.setStyle(BG_COLOR_STYLE);

        titleLabel = new Label(language.getString("project_new_pane_title"));
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
        vbox.getChildren().addAll(titleBox, createForm());
        vbox.prefWidthProperty().bind(this.widthProperty());

        this.getChildren().add(vbox);
    }

    private Pane createForm() {

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(30, 10, 10, 30));
        grid.setHgap(20);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        // project name
        projectNameTextField.setMaxWidth(450);
        //projectNameTextField.setStyle(requiredStyle);
        projectNameTextField.setId("project_name_textfield");
        projectNameLabel = new Label(language.getString("project_name"));
        grid.add(projectNameLabel, 1, 0);
        grid.add(projectNameTextField, 2, 0);

        // project abbreviation
        projectAbbrevTextField.setMaxWidth(200);
        projectAbbrevTextField.setStyle(REQUIRED_STYLE);
        projectAbbrevTextField.setId("project_abbrev_textfield");

        projectAbbrevTextField.lengthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int limitAbbrev = 8;
                if (newValue.intValue() > oldValue.intValue()) {
                    if (projectAbbrevTextField.getText().length() >= limitAbbrev) {
                        projectAbbrevTextField.setText(projectAbbrevTextField.getText().substring(0, limitAbbrev));
                    }
                }
            }
        });

        projectAbbrevLabel = new Label(language.getString("project_abbrev"));
        grid.add(projectAbbrevLabel, 1, 1);
        grid.add(projectAbbrevTextField, 2, 1);

        // project short name
        projectShortNameTextField.setMaxWidth(200);

        projectShortNameTextField.lengthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int limitShortName = 16;
                if (newValue.intValue() > oldValue.intValue()) {
                    if (projectShortNameTextField.getText().length() >= limitShortName) {
                        projectShortNameTextField.setText(projectShortNameTextField.getText().substring(0, limitShortName));
                    }
                }
            }
        });

        projectShortNameLabel = new Label(language.getString("project_short_name"));
        grid.add(projectShortNameLabel, 1, 2);
        grid.add(projectShortNameTextField, 2, 2);

        latitudeTextField.setMaxWidth(200);
        projectLatitudeLabel = new Label(language.getString("project_latitude"));
        grid.add(projectLatitudeLabel, 1, 3);
        grid.add(latitudeTextField, 2, 3);

        longitudeTextField.setMaxWidth(200);
        projectLlongitudeLabel = new Label(language.getString("project_longitude"));
        grid.add(projectLlongitudeLabel, 1, 4);
        grid.add(longitudeTextField, 2, 4);

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
        startPicker.setPrefWidth(200);
        startPicker.setEditable(false);
        startPicker.setConverter(converter);
        startPicker.setPromptText(pattern.toLowerCase());
        projectStartDateLabel = new Label(language.getString("project_start_date"));
        grid.add(projectStartDateLabel, 1, 5);
        grid.add(startPicker, 2, 5);

        // end date
        endPicker.setPrefWidth(200);
        endPicker.setEditable(false);
        endPicker.setConverter(converter);
        endPicker.setPromptText(pattern.toLowerCase());
        projectEndDateLabel = new Label(language.getString("project_end_date"));
        grid.add(projectEndDateLabel, 1, 6);
        grid.add(endPicker, 2, 6);

        // country combo
        countryCombo = new CountryComboBox(this.language);
        countryCombo.setPrefWidth(200);
        projectCountryLabel = new Label(language.getString("project_country"));
        grid.add(projectCountryLabel, 1, 7);
        grid.add(countryCombo, 2, 7);

        // time zone
        String[] ids = TimeZone.getAvailableIDs();
        timezoneCombo.getItems().addAll(ids);
        timezoneCombo.setPrefWidth(200);
        timezoneCombo.setValue(TimeZone.getDefault().getID());
        projectTimezoneLabel = new Label(language.getString("project_timezone"));
        grid.add(projectTimezoneLabel, 1, 8);
        grid.add(timezoneCombo, 2, 8);

        // project status
        statusCombo = new ProjectStatusComboBox(this.language);
        statusCombo.setPrefWidth(200);
        projectStatusLabel = new Label(language.getString("project_status"));
        grid.add(projectStatusLabel, 1, 9);
        grid.add(statusCombo, 2, 9);

        // objective
        objectiveTextArea.setPrefRowCount(3);
        objectiveTextArea.setMaxWidth(450);
        projectObjectiveLabel = new Label(language.getString("project_objective"));
        GridPane.setValignment(projectObjectiveLabel, VPos.TOP);
        grid.add(projectObjectiveLabel, 1, 10);
        grid.add(objectiveTextArea, 2, 10);

        // use and constraints
        useTextArea.setPrefRowCount(3);
        useTextArea.setMaxWidth(450);
        projectUseLabel = new Label(language.getString("project_use"));
        GridPane.setValignment(projectUseLabel, VPos.TOP);
        grid.add(projectUseLabel, 1, 11);
        grid.add(useTextArea, 2, 11);

        // button
        saveButton = new Button(language.getString("project_save_button"));
        saveButton.setId("new_project_save");
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().add(saveButton);
        grid.add(hbBtn, 2, 13);

        return grid;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        titleLabel.setText(language.getString("project_new_pane_title"));
        projectNameLabel.setText(language.getString("project_name"));
        projectAbbrevLabel.setText(language.getString("project_abbrev"));
        projectShortNameLabel.setText(language.getString("project_short_name"));
        projectLatitudeLabel.setText(language.getString("project_latitude"));
        projectLlongitudeLabel.setText(language.getString("project_longitude"));
        projectStartDateLabel.setText(language.getString("project_start_date"));
        projectEndDateLabel.setText(language.getString("project_end_date"));
        projectCountryLabel.setText(language.getString("project_country"));
        projectTimezoneLabel.setText(language.getString("project_timezone"));
        projectStatusLabel.setText(language.getString("project_status"));
        projectObjectiveLabel.setText(language.getString("project_objective"));
        projectUseLabel.setText(language.getString("project_use"));
        saveButton.setText(language.getString("project_save_button"));

    }

    public boolean validate(ObservableList<Project> projects) {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        String projectName = projectNameTextField.getText();
        if (projectName == null || projectName.trim().equals("")) {
            title = language.getString("title_error");
            header = language.getString("empty_project_name_error_header");
            context = language.getString("empty_project_name_error_context");
            ok = false;
        } else {
            for (Project project : projects) {
                if (project.getName().equals(projectName)) {
                    title = language.getString("title_error");
                    header = language.getString("duplicate_project_name_error_header");
                    context = language.getString("duplicate_project_name_error_context");
                    ok = false;
                    break;
                }
            }
        }

        if (ok) {
            String projectAbbrev = projectAbbrevTextField.getText();
            if (projectAbbrev == null || projectAbbrev.trim().equals("")) {
                title = language.getString("title_error");
                header = language.getString("empty_project_abbrev_error_header");
                context = language.getString("empty_project_abbrev_error_context");
                ok = false;
            } else {
                for (Project project : projects) {
                    if (project.getAbbrevName().equals(projectAbbrev)) {
                        title = language.getString("title_error");
                        header = language.getString("duplicate_project_abbrev_error_header");
                        context = language.getString("duplicate_project_abbrev_error_context");
                        ok = false;
                        break;
                    }
                }
            }
        }

        if (ok) {
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();
            if (start != null && end != null && end.isBefore(start)) {
                title = language.getString("title_error");
                header = language.getString("project_end_before_start_error_header");
                context = language.getString("project_end_before_start_error_context");
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

    public Project getProject() {
        Project project = new Project();
        project.setName(projectNameTextField.getText());
        project.setAbbrevName(projectAbbrevTextField.getText());
        project.setShortName(projectShortNameTextField.getText());

        String lat = latitudeTextField.getText();
        if (lat != null && !lat.trim().equals("")) {
            project.setLatitude(Double.parseDouble(lat));
        }

        String lng = longitudeTextField.getText();
        if (lng != null && !lng.trim().equals("")) {
            project.setLongitude(Double.parseDouble(lng));
        }

        LocalDate start = startPicker.getValue();
        if (start != null) {
            project.setStartTime(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        LocalDate end = endPicker.getValue();
        if (end != null) {
            project.setEndTime(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        project.setCountry((Country) countryCombo.getValue());
        project.setTimeZone(timezoneCombo.getValue());
        project.setProjectStatus((ProjectStatus) statusCombo.getValue());
        project.setObjective(objectiveTextArea.getText());
        project.setUseAndConstraints(useTextArea.getText());
        project.setLastUpdateTime(new Date());
        return project;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
    }

}
