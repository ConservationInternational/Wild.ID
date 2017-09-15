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
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.wildid.entity.Age;
import org.wildid.entity.Image;
import org.wildid.entity.ImageIndividual;
import org.wildid.entity.ImageSpecies;

public class ImageIndividualPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    private final Label titleLabel;
    private WildIDController controller;
    private final Image image;
    private final List<Age> ages;

    public VBox vbox;
    private Label speciesLabel;
    private Label ageLabel;
    private Label genderLabel;
    private Label nameLabel;
    private Label noteLabel;
    private Button submitButton;
    private Button updateButton;
    private Button deleteButton;
    private TextField nameTextField;
    private Button cancelButton;
    private ComboBox speciesCombo;
    private AgeComboBox ageCombo;
    private GenderComboBox genderComboBox;
    protected TextArea noteTextArea;
    static Logger log = Logger.getLogger(ImageIndividualPane.class.getName());
    private final ProjectImageViewPane projectImageViewPane;
    private final ImageIndividual imageIndividual;
    boolean isNew = true;

    public ImageIndividualPane(ProjectImageViewPane projectImageViewPane, List<Age> ages, ImageIndividual imageIndividual) throws Exception {
        this.language = projectImageViewPane.getLanguage();
        this.controller = projectImageViewPane.getWildIDController();
        this.image = projectImageViewPane.getImage();
        this.projectImageViewPane = projectImageViewPane;
        this.ages = ages;
        this.imageIndividual = imageIndividual;

        if (imageIndividual.getImageIndividualId() == null) {
            titleLabel = new Label(language.getString("image_individual_new_pane_title"));
        } else {
            titleLabel = new Label(language.getString("image_individual_update_pane_title"));
            isNew = false;
        }

        titleLabel.setStyle(SUBTITLE_STYLE);
        titleLabel.setPadding(new Insets(0, 0, 0, 10));

        HBox titleBox = new HBox(15);
        titleBox.setPadding(new Insets(5));
        titleBox.getChildren().addAll(this.titleLabel);

        this.vbox = new VBox(0);
        this.vbox.setAlignment(Pos.TOP_CENTER);

        this.vbox.getChildren().addAll(titleBox, createForm());
        this.getChildren().add(this.vbox);

        this.vbox.prefWidthProperty().bind(this.widthProperty());
        this.vbox.prefHeightProperty().bind(this.heightProperty());
    }

    public final Pane createForm() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5));
        grid.setHgap(10);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);
        int row_count = 0;
        int input_width = 200;

        speciesLabel = new Label(language.getString("image_individual_species_label"));
        ageLabel = new Label(language.getString("image_individual_age_label"));
        genderLabel = new Label(language.getString("image_individual_gender_label"));
        nameLabel = new Label(language.getString("image_individual_name_label"));
        noteLabel = new Label(language.getString("image_individual_note_label"));

        Age unknownAge = new Age("Unknown");
        ages.add(unknownAge);
        ageCombo = new AgeComboBox(language, ages);

        genderComboBox = new GenderComboBox(language);

        List<ImageSpecies> imageSpeciesesList = new ArrayList<>(image.getImageSpecieses());
        speciesCombo = new ImageSpeciesComboBox(imageSpeciesesList);

        if (isNew) {
            submitButton = new Button(language.getString("image_individual_save_btn"));
            submitButton.setId("image_individual_save_btn");
            submitButton.setOnAction(controller);

            ageCombo.setValue(unknownAge);
            genderComboBox.setValue(language.getString("image_individual_gender_Unknown"));
            nameTextField = new TextField();
            noteTextArea = new TextArea();
        } else {
            updateButton = new Button(language.getString("image_individual_update_btn"));
            updateButton.setId("image_individual_update_btn");
            updateButton.setOnAction(controller);

            deleteButton = new Button(language.getString("image_individual_delete_btn"));
            deleteButton.setId("image_individual_delete_btn");
            deleteButton.setOnAction(controller);

            ageCombo.setValue(imageIndividual.getAge() == null ? unknownAge : imageIndividual.getAge());
            genderComboBox.setValue(
                    imageIndividual.getSex() == null || imageIndividual.getSex().equals("Unknown") || imageIndividual.getSex().isEmpty()
                    ? language.getString("image_individual_gender_Unknown") : imageIndividual.getSex());
            speciesCombo.setValue(imageIndividual.getImageSpecies());
            nameTextField = new TextField(imageIndividual.getName());
            noteTextArea = new TextArea(imageIndividual.getNote());
        }

        cancelButton = new Button(language.getString("alert_cancel"));
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Scene scene = cancelButton.getScene();
                scene.getWindow().hide();
            }
        });

        noteTextArea.setPrefRowCount(2);
        noteTextArea.setMaxWidth(input_width);

        grid.add(speciesLabel, 1, ++row_count);
        grid.add(speciesCombo, 2, row_count);
        speciesCombo.setPrefWidth(input_width);

        grid.add(ageLabel, 1, ++row_count);
        grid.add(ageCombo, 2, row_count);
        ageCombo.setPrefWidth(input_width);

        grid.add(genderLabel, 1, ++row_count);
        grid.add(genderComboBox, 2, row_count);
        genderComboBox.setPrefWidth(input_width);

        grid.add(nameLabel, 1, ++row_count);
        grid.add(nameTextField, 2, row_count);
        nameTextField.setPrefWidth(input_width);

        grid.add(noteLabel, 1, ++row_count);
        grid.add(noteTextArea, 2, row_count);
        noteTextArea.setPrefWidth(input_width);

        row_count++;
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);

        if (isNew) {
            hbBtn.getChildren().addAll(submitButton, cancelButton);
        } else {
            hbBtn.getChildren().addAll(updateButton, deleteButton, cancelButton);
        }
        grid.add(hbBtn, 1, ++row_count, 2, 1);
        return grid;
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.controller = controller;
    }

    public ProjectImageViewPane getProjectImageViewPane() {
        return projectImageViewPane;
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    public boolean validate() {
        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        try {
            ImageSpecies imageSpecies = this.imageIndividual.getImageSpecies();
            Set<ImageIndividual> imageIndividuals = imageSpecies.getImageIndividuals();

            if (!imageIndividuals.isEmpty() && imageIndividuals.size() >= imageIndividual.getImageSpecies().getIndividualCount()) {
                title = language.getString("title_error");
                header = language.getString("image_individual_add_new_error_header");
                context = language.getString("image_individual_add_new_max_count_error_msg");
                ok = false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ok = false;
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

    public ImageIndividual getImageIndividual() {
        try {
            Age age = (Age) ageCombo.getValue();
            if (age.getName() != null && age.getName().equals("Unknown")) {
                age = null;
            }
            String gender = (String) genderComboBox.getValue();
            if (gender != null && gender.equals("Unknown")) {
                gender = null;
            }

            this.imageIndividual.setImageSpecies((ImageSpecies) speciesCombo.getValue());
            this.imageIndividual.setName(nameTextField.getText());
            this.imageIndividual.setSex(gender);
            this.imageIndividual.setAge(age);
            this.imageIndividual.setNote(noteTextArea.getText());

            return this.imageIndividual;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
