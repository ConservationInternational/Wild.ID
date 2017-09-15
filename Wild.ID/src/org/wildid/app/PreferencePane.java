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
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.wildid.entity.Person;
import org.wildid.entity.Preference;
import org.wildid.service.PersonService;
import org.wildid.service.PersonServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class PreferencePane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;

    protected Label titleLabel;
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");
    protected Image folderImg = new Image("resources/icons/folder.png");

    protected Label languageLabel;
    protected ComboBox languageCombo;

    protected Label generalLabel;
    protected Label annoLabel;

    protected Label styleLabel;
    protected ComboBox styleCombo;

    protected Label workingDirLabel;
    protected TextField workingDirField;
    protected Button workingDirButton;

    protected Label taxaMethodLabel;
    protected ComboBox taxaMethodCombo;

    protected Label timeGroupIntervalLabel;
    protected TextField timeGroupIntervalTextField;

    protected Label defaultPersonLabel;
    protected PersonComboBox defaultPersonCombo;

    protected Label enableImageIndividualLabel;
    protected CheckBox enableImageIndividualCheckbox;

    protected Label enableSubspeciesLabel;
    protected CheckBox enableSubspeciesCheckbox;

    protected GridPane formGrid;
    protected GridPane generalGrid;
    protected GridPane annoGrid;

    protected Button saveButton;
    protected HBox hbBtn = new HBox(10);

    public PreferencePane(LanguageModel language) {
        this.language = language;
        this.setStyle(BG_COLOR_STYLE);

        titleLabel = new Label(language.getString("preference_pane_title"));
        titleLabel.setStyle(TITLE_STYLE);

        this.imgView = new ImageView(new Image("resources/icons/page.png"));
        this.imgView.setVisible(true);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel, this.imgView);

        VBox vbox = new VBox(0);
        vbox.prefWidthProperty().bind(this.widthProperty());
        vbox.getChildren().addAll(titleBox, createForm());
        this.getChildren().add(vbox);
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
    }

    @Override
    public void setLanguage(LanguageModel language) {

        titleLabel.setText(language.getString("preference_pane_title"));
        saveButton.setText(language.getString("preference_save_button"));
        languageLabel.setText(language.getString("preference_language_label"));
        taxaMethodLabel.setText(language.getString("preference_image_annotation_taxa_method_label"));
        defaultPersonLabel.setText(language.getString("preference_image_annotation_default_person_label"));
        timeGroupIntervalLabel.setText(language.getString("preference_annotation_default_group_time_label"));
        styleLabel.setText(language.getString("preference_style_label"));
        workingDirLabel.setText(language.getString("preference_working_dir_label"));
        generalLabel.setText(language.getString("preference_general_legend"));
        annoLabel.setText(language.getString("preference_image_annotation_legend"));
        enableImageIndividualLabel.setText(language.getString("preference_enable_image_individual"));
        enableSubspeciesLabel.setText(language.getString("preference_enable_subspecies"));

        String prefTaxaMethod = (String) taxaMethodCombo.getValue();
        if (prefTaxaMethod.equals(this.language.getString("preference_taxa_method_Binomial_Nomenclature"))) {
            prefTaxaMethod = language.getString("preference_taxa_method_Binomial_Nomenclature");
        } else if (prefTaxaMethod.equals(this.language.getString("preference_taxa_method_Common_Name"))) {
            prefTaxaMethod = language.getString("preference_taxa_method_Common_Name");
        }
        taxaMethodCombo.setValue(prefTaxaMethod);

        List<String> options = new ArrayList<>();
        options.add(language.getString("preference_taxa_method_Binomial_Nomenclature"));
        options.add(language.getString("preference_taxa_method_Common_Name"));
        taxaMethodCombo.setItems(FXCollections.observableList(options));

        this.language = language;
    }

    private Pane createForm() {

        formGrid = new GridPane();
        formGrid.setPadding(new Insets(30, 10, 10, 30));
        formGrid.setHgap(20);
        formGrid.setStyle(TEXT_STYLE);
        formGrid.setVgap(10);

        // add general part
        generalGrid = new GridPane();
        generalGrid.setPadding(new Insets(0, 10, 10, 10));
        generalGrid.setHgap(20);
        generalGrid.setStyle(TEXT_STYLE);
        generalGrid.setVgap(15);

        languageLabel = new Label(language.getString("preference_language_label"));
        styleLabel = new Label(language.getString("preference_style_label"));
        workingDirLabel = new Label(language.getString("preference_working_dir_label"));

        generalLabel = new Label(language.getString("preference_general_legend"));
        generalLabel.setStyle("-fx-font-weight: bold; -fx-font-size:14px; -fx-font-family: Verdana;");

        int general_count = 0;

        generalGrid.add(generalLabel, 0, general_count++);
        general_count++;

        generalGrid.add(languageLabel, 0, general_count);

        List<String> languages = new ArrayList<>();
        languages.add("English");
        languages.add("中文简体");
        languages.add("Español");
        languages.add("Português");
        languages.add("Français");
        languageCombo = new ComboBox();
        languageCombo.getItems().addAll(languages);
        languageCombo.setPrefWidth(250);

        String prefLanguage = WildID.preference.getLanguage();
        if (prefLanguage.equals("en")) {
            languageCombo.setValue("English");
        } else if (prefLanguage.equals("cn")) {
            languageCombo.setValue("中文简体");
        } else if (prefLanguage.equals("es")) {
            languageCombo.setValue("Español");
        } else if (prefLanguage.equals("pt")) {
            languageCombo.setValue("Português");
        } else if (prefLanguage.equals("fr")) {
            languageCombo.setValue("Français");
        } else {
            languageCombo.setValue("English");
        }
        generalGrid.add(languageCombo, 1, general_count);

        general_count++;
        generalGrid.add(styleLabel, 0, general_count);

        List<String> styles = new ArrayList<>();
        styles.add("Modena");
        styles.add("Caspian");

        styleCombo = new ComboBox();
        styleCombo.getItems().addAll(styles);
        styleCombo.setPrefWidth(250);

        styleCombo.setValue(WildID.preference.getStyle());
        generalGrid.add(styleCombo, 1, general_count);

        general_count++;
        generalGrid.add(workingDirLabel, 0, general_count);
        HBox workingDirHBox = new HBox(0);
        this.workingDirField = new TextField();
        this.workingDirField.setEditable(true);

        workingDirField.setText(WildID.wildIDProperties.getWorkingDir());

        this.workingDirButton = new Button();
        this.workingDirButton.setGraphic(new ImageView(folderImg));
        this.workingDirButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Working Directory Chooser");

                //Show open file dialog
                File file = directoryChooser.showDialog(null);
                if (file != null) {
                    workingDirField.setText(file.getPath());
                }
            }

        });

        workingDirHBox.getChildren().addAll(workingDirField, workingDirButton);

        workingDirField.setMinWidth(250);
        generalGrid.add(workingDirHBox, 1, general_count);
        generalGrid.setStyle(GRAY_DIV);

        // add annotation part
        annoGrid = new GridPane();
        annoGrid.setAlignment(Pos.CENTER);
        annoGrid.setPadding(new Insets(0, 10, 10, 10));
        annoGrid.setHgap(20);
        annoGrid.setStyle(TEXT_STYLE);
        annoGrid.setVgap(15);

        annoLabel = new Label(language.getString("preference_image_annotation_legend"));
        annoLabel.setStyle("-fx-font-weight: bold; -fx-font-size:14px; -fx-font-family: Verdana;");

        int anno_count = 0;
        annoGrid.add(annoLabel, 0, anno_count);
        anno_count++;

        taxaMethodLabel = new Label(language.getString("preference_image_annotation_taxa_method_label"));
        annoGrid.add(taxaMethodLabel, 0, anno_count);

        taxaMethodCombo = new ComboBox();
        List<String> options = new ArrayList<>();

        options.add(language.getString("preference_taxa_method_Binomial_Nomenclature"));
        options.add(language.getString("preference_taxa_method_Common_Name"));

        taxaMethodCombo.setItems(FXCollections.observableList(options));
        String prefTaxaMethod = WildID.preference.getSpeciesNaming();
        if (prefTaxaMethod.equals("Binomial Nomenclature")) {
            prefTaxaMethod = language.getString("preference_taxa_method_Binomial_Nomenclature");
        } else if (prefTaxaMethod.equals("Common Name")) {
            prefTaxaMethod = language.getString("preference_taxa_method_Common_Name");
        }

        taxaMethodCombo.setValue(prefTaxaMethod);
        taxaMethodCombo.setPrefWidth(250);
        annoGrid.add(taxaMethodCombo, 1, anno_count);

        anno_count++;
        defaultPersonLabel = new Label(language.getString("preference_image_annotation_default_person_label"));
        annoGrid.add(defaultPersonLabel, 0, anno_count);

        PersonService personService = new PersonServiceImpl();
        List<Person> persons = personService.listPerson();
        defaultPersonCombo = new PersonComboBox(persons);
        defaultPersonCombo.getItems().add(null);
        Person person = WildID.preference.getDefaultAnnotationPerson();
        defaultPersonCombo.setValue(person);
        defaultPersonCombo.setPrefWidth(250);
        annoGrid.add(defaultPersonCombo, 1, anno_count);

        timeGroupIntervalLabel = new Label(language.getString("preference_annotation_default_group_time_label"));
        timeGroupIntervalTextField = new NaturalNumberField(1440, 0);
        timeGroupIntervalTextField.setText(String.valueOf(WildID.preference.getTimeGroupInterval()));
        timeGroupIntervalTextField.setMaxWidth(60);

        // this tooltip doesn't support multiple languages 
        timeGroupIntervalTextField.setTooltip(new Tooltip("A number of minutes in range [1-1440]"));
        timeGroupIntervalTextField.setText(String.valueOf(WildID.preference.getTimeGroupInterval()));

        HBox hb = new HBox(10);
        hb.getChildren().addAll(timeGroupIntervalTextField, new Label("[1-1440]"));

        anno_count++;
        annoGrid.add(timeGroupIntervalLabel, 0, anno_count);
        annoGrid.add(hb, 1, anno_count);

        enableImageIndividualLabel = new Label(language.getString("preference_enable_image_individual"));
        enableImageIndividualCheckbox = new CheckBox();
        enableImageIndividualCheckbox.setSelected(WildID.wildIDProperties.getEnableImageIndividual());

        anno_count++;
        annoGrid.add(enableImageIndividualLabel, 0, anno_count);
        annoGrid.add(enableImageIndividualCheckbox, 1, anno_count);

        enableSubspeciesLabel = new Label(language.getString("preference_enable_subspecies"));
        enableSubspeciesCheckbox = new CheckBox();
        enableSubspeciesCheckbox.setSelected(WildID.wildIDProperties.getEnableSubspecies());

        anno_count++;
        annoGrid.add(enableSubspeciesLabel, 0, anno_count);
        annoGrid.add(enableSubspeciesCheckbox, 1, anno_count);

        formGrid.add(generalGrid, 1, 0);
        formGrid.add(annoGrid, 1, 1);
        annoGrid.setStyle(GRAY_DIV);

        saveButton = new Button(language.getString("preference_save_button"));
        saveButton.setId("preference_save_button");
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().add(saveButton);
        hbBtn.setPadding(new Insets(0, 10, 10, 60));
        formGrid.add(hbBtn, 1, 2, 3, 1);

        return formGrid;

    }

    public String getWorkingDirectory() {
        return workingDirField.getText();
    }

    public boolean getEnableImageIndividual() {
        return enableImageIndividualCheckbox.isSelected();
    }

    public boolean getEnableSubspecies() {
        return enableSubspeciesCheckbox.isSelected();
    }

    public Preference getPreference() {

        // set language
        String prefLanguage = (String) languageCombo.getValue();
        if (prefLanguage.equals("English")) {
            WildID.preference.setLanguage("en");
        } else if (prefLanguage.equals("中文简体")) {
            WildID.preference.setLanguage("cn");
        } else if (prefLanguage.equals("Español")) {
            WildID.preference.setLanguage("es");
        } else if (prefLanguage.equals("Português")) {
            WildID.preference.setLanguage("pt");
        } else if (prefLanguage.equals("Français")) {
            WildID.preference.setLanguage("fr");
        }

        WildID.preference.setStyle((String) styleCombo.getValue());

        // set annotation method
        String taxaMethod = (String) taxaMethodCombo.getValue();
        if (taxaMethod.equals(this.language.getString("preference_taxa_method_Binomial_Nomenclature"))) {
            taxaMethod = "Binomial Nomenclature";
        } else if (taxaMethod.equals(this.language.getString("preference_taxa_method_Common_Name"))) {
            taxaMethod = "Common Name";
        }
        WildID.preference.setSpeciesNaming(taxaMethod);

        String minuteInterval = timeGroupIntervalTextField.getText();
        if (minuteInterval.trim().equals("")) {
            minuteInterval = "5";
        }

        WildID.preference.setTimeGroupInterval(Integer.valueOf(minuteInterval));

        // set default annotation person
        Person person = (Person) defaultPersonCombo.getValue();
        WildID.preference.setDefaultAnnotationPerson(person);

        return WildID.preference;
    }

    public void confirmPreferenceUpdated() {
        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("preference_updated_header"),
                language.getString("preference_updated_context"),
                language.getString("alert_ok"));
    }

}
