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
import java.util.Date;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import org.wildid.entity.Country;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectStatus;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectEditPane extends ProjectNewPane implements LanguageChangable {

    private final Project project;
    private final Button deleteButton = new Button(language.getString("project_delete_button"));

    public ProjectEditPane(LanguageModel language, Project project) {
        super(language);
        this.project = project;

        this.titleLabel.setText(language.getString("project_edit_pane_title") + " : " + project.getName());
        this.imgView.setImage(new Image("resources/icons/page_edit.png"));

        this.projectNameTextField.setText(project.getName());
        this.projectAbbrevTextField.setText(project.getAbbrevName());
        this.projectShortNameTextField.setText(project.getShortName());

        Double lat = project.getLatitude();
        if (lat != null) {
            this.latitudeTextField.setText(lat.toString());
        }

        Double lng = project.getLongitude();
        if (lng != null) {
            this.longitudeTextField.setText(lng.toString());
        }

        Date start = project.getStartTime();
        if (start != null) {
            this.startPicker.setValue(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        Date end = project.getEndTime();
        if (end != null) {
            this.endPicker.setValue(end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        Country country = project.getCountry();
        if (country != null) {
            this.countryCombo.setValue(country);
        }

        this.timezoneCombo.setValue(project.getTimeZone());
        this.statusCombo.setValue(project.getProjectStatus());
        this.objectiveTextArea.setText(project.getObjective());
        this.useTextArea.setText(project.getUseAndConstraints());
        this.saveButton.setText(language.getString("project_update_button"));
        this.saveButton.setId("edit_project_save");

        this.hbBtn.getChildren().add(deleteButton);
        this.deleteButton.setId("edit_project_delete");

    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        this.titleLabel.setText(language.getString("project_edit_pane_title") + " : " + project.getName());
        this.projectNameLabel.setText(language.getString("project_name"));
        this.projectAbbrevLabel.setText(language.getString("project_abbrev"));
        this.projectShortNameLabel.setText(language.getString("project_short_name"));
        this.projectLatitudeLabel.setText(language.getString("project_latitude"));
        this.projectLlongitudeLabel.setText(language.getString("project_longitude"));
        this.projectStartDateLabel.setText(language.getString("project_start_date"));
        this.projectEndDateLabel.setText(language.getString("project_end_date"));
        this.projectCountryLabel.setText(language.getString("project_country"));
        this.projectTimezoneLabel.setText(language.getString("project_timezone"));
        this.projectStatusLabel.setText(language.getString("project_status"));
        this.projectObjectiveLabel.setText(language.getString("project_objective"));
        this.projectUseLabel.setText(language.getString("project_use"));
        this.saveButton.setText(language.getString("project_update_button"));
        this.deleteButton.setText(language.getString("project_delete_button"));
    }

    @Override
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
            for (Project proj : projects) {
                if (proj.getProjectId().longValue() == project.getProjectId().longValue()) {
                    continue;
                }

                if (proj.getName().equals(projectName)) {
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
                for (Project proj : projects) {
                    if (proj.getProjectId().longValue() == project.getProjectId().longValue()) {
                        continue;
                    }

                    if (proj.getAbbrevName().equals(projectAbbrev)) {
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

    public Project getOriginalProject() {
        return project;
    }

    @Override
    public Project getProject() {

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

    public Button getDeleteButton() {
        return deleteButton;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        this.deleteButton.setOnAction(controller);
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

}
