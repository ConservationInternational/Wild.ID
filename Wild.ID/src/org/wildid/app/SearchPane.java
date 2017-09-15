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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.wildid.entity.ImageType;
import org.wildid.entity.Project;
import org.wildid.entity.TaxaCommonEnglishName;
import org.wildid.service.ImageTypeService;
import org.wildid.service.ImageTypeServiceImpl;
import org.wildid.service.ProjectService;
import org.wildid.service.ProjectServiceImpl;
import org.wildid.service.TaxonomyService;
import org.wildid.service.TaxonomyServiceImpl;
import static org.wildid.app.WildIDDataPane.BG_COLOR_STYLE;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class SearchPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    protected GridPane grid;

    protected Label titleLabel;
    protected Label searchProjectNameLabel;
    protected Label searchStartDateLabel;
    protected Label searchEndDateLabel;
    protected Label searchImageTypeLabel;
    protected Label searchGenusLabel;
    protected Label searchSpeciesLabel;
    protected Label searchCommonNameLabel;

    protected List<Project> projects = new ArrayList<>();
    protected List<CheckBox> searchProjectCheckBoxes = new ArrayList<>();
    protected DatePicker searchStartDatePicker = new DatePicker();
    protected DatePicker searchEndDatePicker = new DatePicker();
    protected ImageTypeComboBox imageTypeCombo;
    protected ComboBox<String> genusCombo = new ComboBox<>();
    protected ComboBox<String> speciesCombo = new ComboBox<>();
    protected ComboBox<String> commonNameCombo = new ComboBox<>();
    protected Button searchButton;

    protected HBox hbBtn = new HBox(10);
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");

    public SearchPane(LanguageModel language) {

        this.language = language;
        this.setStyle(BG_COLOR_STYLE);

        titleLabel = new Label(language.getString("search_pane_title"));
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

    public SearchPane(LanguageModel language,
            Project[] selectedProjects,
            Date startDate,
            Date endDate,
            ImageType imageType,
            String genus,
            String species) {

        this(language);

        for (int i = 0; i < projects.size(); i++) {
            searchProjectCheckBoxes.get(i).setSelected(false);
            for (Project proj : selectedProjects) {
                if (projects.get(i).getProjectId().intValue() == proj.getProjectId().intValue()) {
                    searchProjectCheckBoxes.get(i).setSelected(true);
                    break;
                }
            }
        }

        if (startDate != null) {
            searchStartDatePicker.setValue(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        if (endDate != null) {
            searchEndDatePicker.setValue(endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        imageTypeCombo.setValue(imageType);
        genusCombo.setValue(genus);
        speciesCombo.setValue(species);

        if (!imageType.getName().equals("Animal")) {
            hideGenusForm();
        }

    }

    private Pane createForm() {

        grid = new GridPane();
        grid.setPadding(new Insets(30, 10, 10, 30));
        grid.setHgap(20);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        // projects
        searchProjectNameLabel = new Label(language.getString("search_pane_projects_label"));
        grid.add(searchProjectNameLabel, 1, 0);

        ProjectService projectService = new ProjectServiceImpl();
        int row = 0;
        for (Project project : projectService.listProject()) {
            projects.add(project);
            CheckBox cb = new CheckBox(project.getName());
            cb.setSelected(true);
            searchProjectCheckBoxes.add(cb);
            grid.add(cb, 2, row++);

            cb.setOnAction((event) -> {
                List<Project> selectedProjects = new ArrayList<>();
                for (int i = 0; i < projects.size(); i++) {
                    if (searchProjectCheckBoxes.get(i).isSelected()) {
                        selectedProjects.add(projects.get(i));
                    }
                }

                Date startDate = null;
                LocalDate start = searchStartDatePicker.getValue();
                if (start != null) {
                    startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
                }

                Date endDate = null;
                LocalDate end = searchEndDatePicker.getValue();
                if (end != null) {
                    endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
            });
        }

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
        searchStartDatePicker.setPrefWidth(200);
        searchStartDatePicker.setEditable(false);
        searchStartDatePicker.setConverter(converter);
        searchStartDatePicker.setPromptText(pattern.toLowerCase());
        searchStartDateLabel = new Label(language.getString("search_start_date_label"));
        grid.add(searchStartDateLabel, 1, row);
        grid.add(searchStartDatePicker, 2, row++);

        // end date
        searchEndDatePicker.setPrefWidth(200);
        searchEndDatePicker.setEditable(false);
        searchEndDatePicker.setConverter(converter);
        searchEndDatePicker.setPromptText(pattern.toLowerCase());
        searchEndDateLabel = new Label(language.getString("search_end_date_label"));
        grid.add(searchEndDateLabel, 1, row);
        grid.add(searchEndDatePicker, 2, row++);

        // image type combo
        searchImageTypeLabel = new Label(language.getString("search_image_type_label"));
        ImageTypeService imageTypeService = new ImageTypeServiceImpl();
        List<ImageType> imageTypes = imageTypeService.listImageType();
        this.imageTypeCombo = new ImageTypeComboBox(language, imageTypes);
        this.imageTypeCombo.setOnAction((Event event) -> {
            ImageType imageType = (ImageType) this.imageTypeCombo.getSelectionModel().getSelectedItem();
            if (imageType.getName().equals("Animal")) {
                insertGenusForm();
            } else {
                int rowCount = getRowCount(grid);
                if (rowCount > projects.size() + 4) {
                    hideGenusForm();
                }
            }
        });

        this.imageTypeCombo.setPrefWidth(200);
        grid.add(searchImageTypeLabel, 1, row);
        grid.add(imageTypeCombo, 2, row++);

        if (WildID.preference.getSpeciesNaming().equals("Binomial Nomenclature")) {

            // genus
            searchGenusLabel = new Label(language.getString("search_genus_label"));
            TaxonomyService taxaService = new TaxonomyServiceImpl();
            TreeSet<String> genusList = new TreeSet<>();
            genusList.addAll(taxaService.loadUsedGenus());
            genusCombo.getItems().addAll(genusList);
            if (genusList.size() == 1) {
                genusCombo.setValue(genusList.iterator().next());
            }

            genusCombo.setPrefWidth(200);
            grid.add(searchGenusLabel, 1, row);
            grid.add(genusCombo, 2, row++);

            genusCombo.setOnAction((event) -> {
                String genus = this.genusCombo.getSelectionModel().getSelectedItem();
                TreeSet<String> speciesList = new TreeSet<>();
                speciesList.addAll(taxaService.loadUsedSpecies(genus));
                speciesCombo.getItems().clear();
                speciesCombo.getItems().addAll(speciesList);
                if (speciesList.size() == 1) {
                    speciesCombo.setValue(speciesList.iterator().next());
                }
            });

            // species
            searchSpeciesLabel = new Label(language.getString("search_species_label"));
            speciesCombo.setPrefWidth(200);

            String genus = this.genusCombo.getSelectionModel().getSelectedItem();
            TreeSet<String> speciesList = new TreeSet<>();
            speciesList.addAll(taxaService.loadUsedSpecies(genus));
            speciesCombo.getItems().clear();
            speciesCombo.getItems().addAll(speciesList);
            if (speciesList.size() == 1) {
                speciesCombo.setValue(speciesList.iterator().next());
            }

            grid.add(searchSpeciesLabel, 1, row);
            grid.add(speciesCombo, 2, row++);

        } else {
            // common name
            searchCommonNameLabel = new Label(language.getString("search_common_name_label"));

            TaxonomyService taxaService = new TaxonomyServiceImpl();
            TreeSet<String> usedCommonNameList = new TreeSet<>();
            usedCommonNameList.addAll(taxaService.loadUsedCommonNames());
            commonNameCombo.getItems().addAll(usedCommonNameList);
            commonNameCombo.setPrefWidth(200);
            commonNameCombo.setOnAction((event) -> {
                String cn = this.commonNameCombo.getSelectionModel().getSelectedItem();
                TaxaCommonEnglishName commonName = taxaService.getCommonEnglishNames(cn);
                genusCombo.setValue(commonName.getSpecies().getGenus());
                speciesCombo.setValue(commonName.getSpecies().getSpecies());
            });

            grid.add(searchCommonNameLabel, 1, row);
            grid.add(commonNameCombo, 2, row++);

        }

        // button
        searchButton = new Button(language.getString("search_button"));
        searchButton.setId("search_button");
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().add(searchButton);
        grid.add(hbBtn, 2, row++);

        return grid;
    }

    public Button getSaveButton() {
        return searchButton;
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        titleLabel.setText(language.getString("search_pane_title"));
        searchProjectNameLabel.setText(language.getString("search_pane_projects_label"));
        searchStartDateLabel.setText(language.getString("search_start_date_label"));
        searchEndDateLabel.setText(language.getString("search_end_date_label"));
        searchImageTypeLabel.setText(language.getString("search_image_type_label"));

        if (WildID.preference.getSpeciesNaming().equals("Binomial Nomenclature")) {
            searchGenusLabel.setText(language.getString("search_genus_label"));
            searchSpeciesLabel.setText(language.getString("search_species_label"));
        } else {
            searchCommonNameLabel.setText(language.getString("search_common_name_label"));
        }
        searchButton.setText(language.getString("search_button"));

    }

    private void insertGenusForm() {
        int rowCount = getRowCount(grid);

        if (WildID.preference.getSpeciesNaming().equals("Binomial Nomenclature")) {
            Util.addEmptyRow(grid, rowCount - 1);
            grid.add(searchSpeciesLabel, 1, rowCount - 1);
            grid.add(speciesCombo, 2, rowCount - 1);

            Util.addEmptyRow(grid, rowCount - 1);
            grid.add(searchGenusLabel, 1, rowCount - 1);
            grid.add(genusCombo, 2, rowCount - 1);
        } else {
            Util.addEmptyRow(grid, rowCount - 1);
            grid.add(searchCommonNameLabel, 1, rowCount - 1);
            grid.add(commonNameCombo, 2, rowCount - 1);
        }
    }

    private void hideGenusForm() {
        int rowCount = getRowCount(grid);
        if (WildID.preference.getSpeciesNaming().equals("Binomial Nomenclature")) {
            Util.removeRow(grid, rowCount - 2);
            Util.removeRow(grid, rowCount - 3);
        } else {
            Util.removeRow(grid, rowCount - 2);
        }
    }

    private static int getRowCount(GridPane pane) {
        int numRows = pane.getRowConstraints().size();
        for (int i = 0; i < pane.getChildren().size(); i++) {
            Node child = pane.getChildren().get(i);
            if (child.isManaged()) {
                Integer rowIndex = GridPane.getRowIndex(child);
                if (rowIndex != null) {
                    numRows = Math.max(numRows, rowIndex + 1);
                }
            }
        }
        return numRows;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.searchButton.setOnAction(controller);
    }

    public Project[] getSelectedProjects() {
        List<Project> selectedProjects = new ArrayList<>();
        for (int i = 0; i < projects.size(); i++) {
            if (searchProjectCheckBoxes.get(i).isSelected()) {
                selectedProjects.add(projects.get(i));
            }
        }

        Project[] array = new Project[selectedProjects.size()];
        return selectedProjects.toArray(array);
    }

    public Date getStartDate() {
        Date startDate = null;
        LocalDate start = searchStartDatePicker.getValue();
        if (start != null) {
            startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return startDate;
    }

    public Date getEndDate() {
        Date endDate = null;
        LocalDate end = searchEndDatePicker.getValue();
        if (end != null) {
            endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return endDate;
    }

    public ImageType getImageType() {
        return (ImageType) this.imageTypeCombo.getValue();
    }

    public String getGenus() {
        return this.genusCombo.getValue();
    }

    public String getSpecies() {
        return this.speciesCombo.getValue();
    }

    public void showSearchCount(int count) {

        String title = language.getString("search_result_count_title");
        String header = language.getString("search_result_count_header");
        String context = language.getString("search_result_count_context1") + " " + count + " " + language.getString("search_result_count_context2");

        Util.alertInformationPopup(
                title,
                header,
                context,
                language.getString("alert_ok"));
    }

}
