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

import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.wildid.entity.FamilyGenusSpecies;
import org.wildid.entity.HomoSapiensType;
import org.wildid.entity.Image;
import org.wildid.entity.ImageSpecies;
import org.wildid.entity.ImageType;
import org.wildid.entity.ImageUncertaintyType;
import org.wildid.entity.Person;
import org.wildid.entity.TaxaCommonEnglishName;
import org.wildid.service.TaxonomyService;
import org.wildid.service.TaxonomyServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectImageAnnotationPane extends GridPane {

    private static final int LABEL_WIDTH = 120;
    private static final int INPUT_WIDTH = 160;
    private static final int NOTE_HEIGHT = 80;
    private static final int LINE_WIDTH = 300;

    protected LanguageModel language;
    protected Image image;
    protected List<Person> persons;
    protected List<ImageType> imageTypes;
    protected List<ImageUncertaintyType> imageUncertaintyTypes;
    protected List<HomoSapiensType> sapiensTypes;

    protected Label typeLabel;
    protected ImageTypeComboBox typeCombo;

    protected Label typeIdentifyPersonLabel;
    protected ComboBox typeIdentifyPersonCombo = new ComboBox();

    protected CheckBox groupCheckBox;

    protected Label noteLabel;
    protected TextArea noteTextArea = new TextArea();

    protected int numberOfSpecies = -1;

    protected Label speciesNumberLabel;
    protected ComboBox speciesNumberCombo;

    protected List<Label> commonNameLabels = new ArrayList<>();
    protected List<TextField> commonNameFields = new ArrayList<>();

    protected List<Label> genusLabels = new ArrayList<>();
    protected List<TextField> genusFields = new ArrayList<>();

    protected List<Label> speciesLabels = new ArrayList<>();
    protected List<TextField> speciesFields = new ArrayList<>();

    protected List<Label> subspeciesLabels = new ArrayList<>();
    protected List<TextField> subspeciesFields = new ArrayList<>();

    protected List<Label> sapiensLabels = new ArrayList<>();
    protected List<SapiensTypeComboBox> sapiensCombos = new ArrayList<>();

    protected List<Label> numberLabels = new ArrayList<>();
    protected List<ComboBox> numberCombos = new ArrayList<>();

    protected List<Label> uncertaintyLabels = new ArrayList<>();
    protected List<ImageUncertaintyComboBox> uncertaintyCombos = new ArrayList<>();

    protected List<Label> speciesIdentifiedPersonLabels = new ArrayList<>();
    protected List<PersonComboBox> speciesIdentifiedPersonCombos = new ArrayList<>();

    protected Button saveButton;
    protected boolean isBinomialNaming = WildID.preference.getSpeciesNaming().equals("Binomial Nomenclature");

    public ProjectImageAnnotationPane(
            LanguageModel language,
            Image image,
            List<Person> persons,
            List<ImageType> imageTypes,
            List<ImageUncertaintyType> imageUncertaintyTypes,
            List<HomoSapiensType> sapiensTypes) {

        this.language = language;
        this.image = image;
        this.persons = persons;
        this.imageTypes = imageTypes;
        this.imageUncertaintyTypes = imageUncertaintyTypes;
        this.sapiensTypes = sapiensTypes;

        this.setAlignment(Pos.TOP_LEFT);
        this.setHgap(10);
        this.setVgap(10);
        this.setStyle(WildIDDataPane.TEXT_STYLE);

        typeLabel = new Label(language.getString("project_anno_pane_type_label"));
        typeLabel.setMinWidth(LABEL_WIDTH);
        this.add(typeLabel, 0, 0);

        typeCombo = new ImageTypeComboBox(language, imageTypes);
        typeCombo.setValue(image.getImageType());
        typeCombo.setMinWidth(INPUT_WIDTH);
        typeCombo.setOnAction((Event event) -> {

            Person typeIdentifyPerson = (Person) typeIdentifyPersonCombo.getValue();
            if (typeIdentifyPerson == null) {
                typeIdentifyPersonCombo.setValue(WildID.preference.getDefaultAnnotationPerson());
            }

            ImageType imageType = (ImageType) typeCombo.getSelectionModel().getSelectedItem();
            if (imageType.getName().equals("Animal")) {
                insertGenusForm();
            } else {
                hideGenusForm();
            }

            for (PersonComboBox personBox : speciesIdentifiedPersonCombos) {
                if (personBox.getValue() == null) {
                    personBox.setValue(WildID.preference.getDefaultAnnotationPerson());
                }
            }

        });
        this.add(typeCombo, 1, 0);

        typeIdentifyPersonLabel = new Label(language.getString("project_anno_pane_type_identify_person"));
        this.add(typeIdentifyPersonLabel, 0, 1);

        List<Person> personList = new ArrayList<>(persons);
        typeIdentifyPersonCombo = new PersonComboBox(personList);
        typeIdentifyPersonCombo.setValue(image.getPerson());
        typeIdentifyPersonCombo.setPrefWidth(INPUT_WIDTH);
        typeIdentifyPersonCombo.setOnAction((Event event) -> {
            Person person = (Person) typeIdentifyPersonCombo.getSelectionModel().getSelectedItem();
            for (PersonComboBox personBox : speciesIdentifiedPersonCombos) {
                if (personBox.getValue() == null) {
                    personBox.setValue(person);
                }
            }
        });

        this.add(typeIdentifyPersonCombo, 1, 1);

        noteLabel = new Label(language.getString("project_anno_pane_notes"));
        this.add(noteLabel, 0, 2);
        setValignment(noteLabel, VPos.TOP);

        noteTextArea.setMaxWidth(INPUT_WIDTH);
        noteTextArea.setMinHeight(NOTE_HEIGHT);
        noteTextArea.setMaxHeight(NOTE_HEIGHT);
        noteTextArea.setText(image.getNote());
        this.add(noteTextArea, 1, 2);

        groupCheckBox = new CheckBox("project_anno_pane_group_checkbox");
        setHalignment(groupCheckBox, HPos.CENTER);
        this.add(groupCheckBox, 0, 3, 2, 1);

        if (image.getImageType() != null) {
            saveButton = new Button(language.getString("project_anno_pane_update_button"));
            saveButton.setId("project_anno_pane_update_button");
        } else {
            saveButton = new Button(language.getString("project_anno_pane_save_button"));
            saveButton.setId("project_anno_pane_save_button");
        }
        setHalignment(saveButton, HPos.CENTER);
        this.add(saveButton, 0, 4, 2, 1);

        if (image.getImageType() != null && image.getImageType().getName().equals("Animal")) {
            initializeSpeciesForm(image.getImageSpecieses());
        }

        // change traversal engine
        Algorithm algo = new Algorithm() {

            @Override
            public Node selectFirst(TraversalContext context) {
                return null;
            }

            @Override
            public Node selectLast(TraversalContext context) {
                return null;
            }

            @Override
            public Node select(Node owner, com.sun.javafx.scene.traversal.Direction dir, TraversalContext context) {

                if (owner == typeCombo) {
                    return typeIdentifyPersonCombo;
                } else if (owner == typeIdentifyPersonCombo) {
                    ImageType type = (ImageType) typeCombo.getValue();
                    if (type.getName().equals("Animal")) {
                        return speciesNumberCombo;
                    } else {
                        return noteTextArea;
                    }
                } else if (owner == speciesNumberCombo) {
                    if (isBinomialNaming) {
                        return genusFields.get(0);
                    } else {
                        return commonNameFields.get(0);
                    }
                }

                if (isBinomialNaming) {

                    for (TextField tf : genusFields) {
                        if (owner == tf) {
                            int pos = genusFields.indexOf(tf);
                            return speciesFields.get(pos);
                        }
                    }

                    for (TextField tf : speciesFields) {
                        if (owner == tf) {
                            int pos = speciesFields.indexOf(tf);
                            return numberCombos.get(pos);
                        }
                    }

                    for (ComboBox cb : numberCombos) {
                        if (owner == cb) {
                            int pos = numberCombos.indexOf(cb);
                            SapiensTypeComboBox stcb = sapiensCombos.get(pos);
                            if (stcb != null) {
                                return stcb;
                            } else {
                                return uncertaintyCombos.get(pos);
                            }
                        }
                    }

                    for (SapiensTypeComboBox stcb : sapiensCombos) {
                        if (owner == stcb) {
                            int pos = sapiensCombos.indexOf(stcb);
                            return uncertaintyCombos.get(pos);
                        }
                    }

                    for (ImageUncertaintyComboBox iucb : uncertaintyCombos) {
                        if (owner == iucb) {
                            int pos = uncertaintyCombos.indexOf(iucb);
                            return speciesIdentifiedPersonCombos.get(pos);
                        }
                    }

                    for (PersonComboBox pcb : speciesIdentifiedPersonCombos) {
                        if (owner == pcb) {
                            int pos = speciesIdentifiedPersonCombos.indexOf(pcb);
                            int kind = (Integer) speciesNumberCombo.getSelectionModel().getSelectedItem();
                            if (pos < kind - 1) {
                                return genusFields.get(pos + 1);
                            } else {
                                return noteTextArea;
                            }
                        }
                    }

                } else {

                    // using common name
                    for (TextField tf : commonNameFields) {
                        if (owner == tf) {
                            int pos = commonNameFields.indexOf(tf);
                            return numberCombos.get(pos);
                        }
                    }

                    for (ComboBox cb : numberCombos) {
                        if (owner == cb) {
                            int pos = numberCombos.indexOf(cb);
                            SapiensTypeComboBox stcb = sapiensCombos.get(pos);
                            if (stcb != null) {
                                return stcb;
                            } else {
                                return uncertaintyCombos.get(pos);
                            }
                        }
                    }

                    for (SapiensTypeComboBox stcb : sapiensCombos) {
                        if (owner == stcb) {
                            int pos = sapiensCombos.indexOf(stcb);
                            return uncertaintyCombos.get(pos);
                        }
                    }

                    for (ImageUncertaintyComboBox iucb : uncertaintyCombos) {
                        if (owner == iucb) {
                            int pos = uncertaintyCombos.indexOf(iucb);
                            return speciesIdentifiedPersonCombos.get(pos);
                        }
                    }

                    for (PersonComboBox pcb : speciesIdentifiedPersonCombos) {
                        if (owner == pcb) {
                            int pos = speciesIdentifiedPersonCombos.indexOf(pcb);
                            int kind = (Integer) speciesNumberCombo.getSelectionModel().getSelectedItem();
                            if (pos < kind - 1) {
                                return commonNameFields.get(pos + 1);
                            } else {
                                return noteTextArea;
                            }
                        }
                    }

                }
                return null;
            }

        };

        ParentTraversalEngine engine = new ParentTraversalEngine(this, algo);
        this.setImpl_traversalEngine(engine);

    }

    private void initializeSpeciesForm() {

        for (int i = 1; i < 11; i++) {
            String index = (i == 1 ? "" : " (" + i + ")");
            final int pos = i - 1;

            // setup the form for genus
            Label commonNameLabel = new Label(language.getString("project_anno_pane_common_name") + " " + index);     //////
            commonNameLabels.add(commonNameLabel);                       //////

            TextField commonNameField = new TextField();                 //////
            commonNameField.setMaxWidth(INPUT_WIDTH);                    //////
            AutoCompletionBinding<String> bind = TextFields.bindAutoCompletion(commonNameField, sr -> {
                TaxonomyService taxaService = new TaxonomyServiceImpl();
                return taxaService.getCommonNameSuggestion(sr.getUserText());
            });
            commonNameFields.add(commonNameField);                       //////

            Label genusLabel = new Label(language.getString("project_anno_pane_genus") + index);
            genusLabels.add(genusLabel);

            TextField genusField = new TextField();
            genusField.setMaxWidth(INPUT_WIDTH);
            bind = TextFields.bindAutoCompletion(genusField, sr -> {
                TaxonomyService taxaService = new TaxonomyServiceImpl();
                return taxaService.getGenusSuggestion(sr.getUserText());
            });

            genusFields.add(genusField);

            // setup the form for species
            Label speciesLabel = new Label(language.getString("project_anno_pane_species") + index);
            speciesLabels.add(speciesLabel);

            TextField speciesField = new TextField();
            speciesField.setMaxWidth(INPUT_WIDTH);
            bind = TextFields.bindAutoCompletion(speciesField, sr -> {
                TaxonomyService taxaService = new TaxonomyServiceImpl();
                return taxaService.getSpeciesSuggestion(genusField.getText(), sr.getUserText());
            });

            speciesFields.add(speciesField);

            Label subspeciesLabel = new Label(language.getString("project_anno_pane_subspecies") + index);
            subspeciesLabels.add(subspeciesLabel);

            TextField subspeciesField = new TextField();
            subspeciesField.setMaxWidth(INPUT_WIDTH);
            subspeciesFields.add(subspeciesField);

            // setup the form for animal number
            Label numberLabel = new Label(language.getString("project_anno_pane_animal_number") + index);
            numberLabels.add(numberLabel);

            ComboBox numberCombo = new ComboBox();
            numberCombo.setMaxWidth(INPUT_WIDTH);
            List<Integer> intList = new ArrayList<>();
            for (int j = 1; j < 41; j++) {
                intList.add(j);
            }
            numberCombo.setItems(FXCollections.observableList(intList));
            numberCombo.setValue(1);
            numberCombos.add(numberCombo);

            // setup sapiens type
            /*
             Label sapiensLabel = new Label("Sapiens Type");
             sapiensLabels.add(sapiensLabel);

             SapiensTypeComboBox sapiensCombo = new SapiensTypeComboBox(sapiensTypes);
             sapiensCombo.setMaxWidth(INPUT_WIDTH);
             sapiensCombo.setDisable(true);
             sapiensCombos.add(sapiensCombo);
             */
            sapiensLabels.add(null);
            sapiensCombos.add(null);

            // setup the form for uncertainty
            Label uncertaintyLabel = new Label(language.getString("project_anno_pane_uncertainty") + index);
            uncertaintyLabels.add(uncertaintyLabel);

            ImageUncertaintyComboBox uncertaintyCombox = new ImageUncertaintyComboBox(language, this.imageUncertaintyTypes);
            uncertaintyCombox.setValue(this.imageUncertaintyTypes.get(0));
            uncertaintyCombox.setMaxWidth(INPUT_WIDTH);
            uncertaintyCombos.add(uncertaintyCombox);

            // set the form for species identifying person
            Label speciesIdentifiedPersonLabel = new Label(language.getString("project_anno_pane_species_identify_person") + index);
            speciesIdentifiedPersonLabels.add(speciesIdentifiedPersonLabel);

            PersonComboBox speciesIdentifiedPersonCombo = new PersonComboBox(persons);
            //speciesIdentifiedPersonCombo.getItems().add(null);
            speciesIdentifiedPersonCombo.setValue(null);
            speciesIdentifiedPersonCombo.setPrefWidth(INPUT_WIDTH);
            speciesIdentifiedPersonCombos.add(speciesIdentifiedPersonCombo);

            genusField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                    if (isBinomialNaming) {
                        System.out.println("Call from genusField");
                        checkHomoSapiensType(pos);
                        speciesFields.get(pos).setText(null);
                    }
                }
            });

            speciesField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                    if (isBinomialNaming) {
                        System.out.println("Call from speciesField");
                        checkHomoSapiensType(pos);
                    }
                }
            });

            commonNameField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                    if (!isBinomialNaming) {
                        System.out.println("Call from commonNameField");
                        checkHomoSapiensType(pos);
                    }
                }
            });
        }
    }

    private void initializeSpeciesForm(Set<ImageSpecies> imageSpecies) {

        initializeSpeciesForm();

        if (speciesNumberLabel == null) {
            speciesNumberLabel = new Label(language.getString("project_anno_pane_species_number"));
            List<Integer> intList = new ArrayList<>();
            for (int i = 1; i < 11; i++) {
                intList.add(i);
            }
            speciesNumberCombo = new ComboBox();
            speciesNumberCombo.setOnAction((evt) -> {
                int kind = (Integer) speciesNumberCombo.getSelectionModel().getSelectedItem();
                numberOfSpecies = kind;
                hideGenusForm();
                insertGenusForm();
                typeCombo.requestFocus();
            });
            speciesNumberCombo.setMinWidth(INPUT_WIDTH);
            speciesNumberCombo.setItems(FXCollections.observableList(intList));
        }

        speciesNumberCombo.setValue(imageSpecies.size());
        numberOfSpecies = imageSpecies.size();

        int index = 0;
        for (ImageSpecies imgSpecies : imageSpecies) {

            TaxaCommonEnglishName ecn = imgSpecies.getEngishCommonName();
            FamilyGenusSpecies fgs = imgSpecies.getFamilyGenusSpecies();
            if (ecn != null) {
                commonNameFields.get(index).setText(ecn.getName());
            } else if (fgs != null) {
                commonNameFields.get(index).setText(fgs.getGenus() + " " + fgs.getSpecies());
            }

            if (fgs != null) {
                genusFields.get(index).setText(fgs.getGenus());
                speciesFields.get(index).setText(fgs.getSpecies());
            }

            subspeciesFields.get(index).setText(imgSpecies.getSubspecies());
            numberCombos.get(index).setValue(imgSpecies.getIndividualCount());
            uncertaintyCombos.get(index).setValue(imgSpecies.getUncertainty());
            speciesIdentifiedPersonCombos.get(index).setValue(imgSpecies.getPerson());

            HomoSapiensType sapiensType = imgSpecies.getHomoSapiensType();
            if (sapiensType != null && sapiensCombos.get(index) != null) {
                sapiensCombos.get(index).setValue(sapiensType);
            }

            index++;
        }

        insertGenusForm();

    }

    private void insertGenusForm() {

        // insert the number of species
        if (speciesNumberLabel == null) {
            speciesNumberLabel = new Label(language.getString("project_anno_pane_species_number"));
            List<Integer> intList = new ArrayList<>();
            for (int i = 1; i < 11; i++) {
                intList.add(i);
            }
            speciesNumberCombo = new ComboBox();
            speciesNumberCombo.setOnAction((evt) -> {
                int kind = (Integer) speciesNumberCombo.getSelectionModel().getSelectedItem();
                numberOfSpecies = kind;
                hideGenusForm();
                insertGenusForm();
                typeCombo.requestFocus();
            });
            speciesNumberCombo.setMinWidth(INPUT_WIDTH);
            speciesNumberCombo.setItems(FXCollections.observableList(intList));
            speciesNumberCombo.setValue(1);

            numberOfSpecies = 1;
        }

        if (genusLabels.isEmpty()) {
            initializeSpeciesForm();
        }

        // add lines
        Util.addEmptyRow(this, 2);
        Line line = new Line(0, 0, LINE_WIDTH, 0);
        line.setStroke(Color.LIGHTGREY);
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(5, 0, 5, 0));
        vbox.getChildren().add(line);
        this.add(vbox, 0, 2, 2, 1);

        // restore all genus/species fields
        for (int i = Math.min(numberOfSpecies, genusLabels.size()) - 1; i > -1; i--) {

            // add animal indentify person combo
            Util.addEmptyRow(this, 2);
            this.add(speciesIdentifiedPersonLabels.get(i), 0, 2);
            this.add(speciesIdentifiedPersonCombos.get(i), 1, 2);

            // add uncertainty
            Util.addEmptyRow(this, 2);
            this.add(uncertaintyLabels.get(i), 0, 2);
            this.add(uncertaintyCombos.get(i), 1, 2);

            // add number of animal
            Util.addEmptyRow(this, 2);
            this.add(numberLabels.get(i), 0, 2);
            this.add(numberCombos.get(i), 1, 2);

            // add sapiens type
            if (sapiensLabels.get(i) != null) {
                Util.addEmptyRow(this, 2);
                this.add(sapiensLabels.get(i), 0, 2);
                this.add(sapiensCombos.get(i), 1, 2);
            }
            
            if (isBinomialNaming) {
                // add subspecies if enabled and Binomial naming
                if (WildID.wildIDProperties.getEnableSubspecies()) {
                    Util.addEmptyRow(this, 2);
                    this.add(subspeciesLabels.get(i), 0, 2);
                    this.add(subspeciesFields.get(i), 1, 2);
                }

                // add species
                Util.addEmptyRow(this, 2);
                this.add(speciesLabels.get(i), 0, 2);
                this.add(speciesFields.get(i), 1, 2);

                // add genus
                Util.addEmptyRow(this, 2);
                this.add(genusLabels.get(i), 0, 2);
                this.add(genusFields.get(i), 1, 2);

            } else {

                // add common name
                Util.addEmptyRow(this, 2);
                this.add(commonNameLabels.get(i), 0, 2);
                this.add(commonNameFields.get(i), 1, 2);
            }

            // add lines
            Util.addEmptyRow(this, 2);
            line = new Line(0, 0, LINE_WIDTH, 0);
            line.setStroke(Color.LIGHTGRAY);
            vbox = new VBox();
            vbox.setPadding(new Insets(5, 0, 5, 0));
            vbox.getChildren().add(line);
            this.add(vbox, 0, 2, 2, 1);
        }

        // add an empty row
        Util.addEmptyRow(this, 2);
        this.add(speciesNumberLabel, 0, 2);
        this.add(speciesNumberCombo, 1, 2);
    }

    private void hideGenusForm() {

        int rowCount = getRowCount(this);
        System.out.println("rowCount: " + rowCount);

        for (int i = 0; i < rowCount - 5; i++) {

            Util.removeRow(this, 2);
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

    public void setLanguage(LanguageModel language) {
        this.language = language;

        typeLabel.setText(language.getString("project_anno_pane_type_label"));
        typeIdentifyPersonLabel.setText(language.getString("project_anno_pane_type_identify_person"));
        noteLabel.setText(language.getString("project_anno_pane_notes"));
        groupCheckBox.setText(language.getString("project_anno_pane_group_checkbox"));

        if (speciesNumberLabel != null) {
            speciesNumberLabel.setText(language.getString("project_anno_pane_species_number"));
        }

        for (int i = 1; i < Math.min(11, genusLabels.size()); i++) {
            String index = (i == 1 ? "" : " (" + i + ")");
            commonNameLabels.get(i - 1).setText(language.getString("project_anno_pane_common_name") + " " + index);
            genusLabels.get(i - 1).setText(language.getString("project_anno_pane_genus") + index);
            speciesLabels.get(i - 1).setText(language.getString("project_anno_pane_species") + index);
            subspeciesLabels.get(i - 1).setText(language.getString("project_anno_pane_subspecies") + index);

            Label sapiensLabel = sapiensLabels.get(i - 1);
            if (sapiensLabel != null) {
                sapiensLabel.setText(language.getString("project_anno_pane_sapeins"));
            }
            numberLabels.get(i - 1).setText(language.getString("project_anno_pane_animal_number") + index);
            uncertaintyLabels.get(i - 1).setText(language.getString("project_anno_pane_uncertainty") + index);
            speciesIdentifiedPersonLabels.get(i - 1).setText(language.getString("project_anno_pane_species_identify_person") + index);
        }

        if (image.getImageType() != null) {
            saveButton.setText(language.getString("project_anno_pane_update_button"));
        } else {
            saveButton.setText(language.getString("project_anno_pane_save_button"));
        }

    }

    public void setWildIDController(WildIDController controller) {

        this.saveButton.setOnAction(controller);
    }

    public boolean validate() {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        ImageType type = (ImageType) typeCombo.getValue();
        if (type == null) {
            title = language.getString("title_error");
            header = language.getString("project_anno_pane_empty_type_error_header");
            context = language.getString("project_anno_pane_empty_type_error_context");
            ok = false;
        }

        if (ok) {
            Person typeIdentified = (Person) typeIdentifyPersonCombo.getValue();
            if (typeIdentified == null) {
                title = language.getString("title_error");
                header = language.getString("project_anno_pane_empty_type_identifier_error_header");
                context = language.getString("project_anno_pane_empty_type_identifier_error_context");
                ok = false;
            }
        }

        if (ok && type.getName().equals("Animal")) {
            for (int i = 0; i < Math.min(numberOfSpecies, genusLabels.size()); i++) {
                String index = (i == 0 ? "" : " (" + (i + 1) + ")");

                if (isBinomialNaming) {    //////

                    String genus = genusFields.get(i).getText();
                    String species = speciesFields.get(i).getText();

                    if (genus == null || genus.trim().equals("")) {
                        title = language.getString("title_error");
                        header = language.getString("project_anno_pane_empty_genus_error_header");
                        context = language.getString("project_anno_pane_empty_genus_error_context") + index;
                        ok = false;
                        break;
                    }

                    if (species == null || species.trim().equals("")) {
                        title = language.getString("title_error");
                        header = language.getString("project_anno_pane_empty_species_error_header");
                        context = language.getString("project_anno_pane_empty_species_error_context") + index;
                        ok = false;
                        break;
                    }

                    TaxonomyService taxaService = new TaxonomyServiceImpl();
                    FamilyGenusSpecies fgs = taxaService.getFamilyGenusSpecies(genus, species);
                    if (fgs == null) {
                        title = language.getString("title_error");
                        header = language.getString("project_anno_pane_invalid_species_error_header");
                        context = language.getString("project_anno_pane_invalid_species_error_context") + index;
                        ok = false;
                        break;
                    }

                } else {

                    if (commonNameFields.get(i).getText() == null || commonNameFields.get(i).getText().trim().equals("")) {
                        title = language.getString("title_error");
                        header = language.getString("project_anno_pane_empty_common_name_error_header");
                        context = language.getString("project_anno_pane_empty_common_name_error_context") + index;
                        ok = false;
                        break;
                    }

                    String commonName = commonNameFields.get(i).getText();
                    TaxonomyService taxaService = new TaxonomyServiceImpl();
                    TaxaCommonEnglishName commonEnglishName = taxaService.getCommonEnglishNames(commonName);
                    if (commonEnglishName == null) {
                        title = language.getString("title_error");
                        header = language.getString("project_anno_pane_invalid_common_name_error_header");
                        context = language.getString("project_anno_pane_invalid_common_name_error_context") + index;
                        ok = false;
                        break;
                    }

                }

                Person speciesIdentifier = (Person) speciesIdentifiedPersonCombos.get(i).getValue();

                if (speciesIdentifier == null) {
                    title = language.getString("title_error");
                    header = language.getString("project_anno_pane_empty_species_identifier_error_header");
                    context = language.getString("project_anno_pane_empty_species_identifier_error_context") + index;
                    ok = false;
                    break;
                }

                // check no duplicate annotation
                for (int j = 0; j < i; j++) {
                    if (genusFields.get(i).getText() != null
                            && genusFields.get(j).getText() != null
                            && speciesFields.get(i).getText() != null
                            && speciesFields.get(j).getText() != null
                            && genusFields.get(i).getText().equals(genusFields.get(j).getText())
                            && speciesFields.get(i).getText().equals(speciesFields.get(j).getText())) {

                        title = language.getString("title_error");
                        header = language.getString("project_anno_pane_duplicate_species_error_header");
                        context = language.getString("project_anno_pane_duplicate_species_error_context") + index;
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

    public Image getAnnotatedImage() {

        image.setImageType((ImageType) typeCombo.getValue());
        image.setPerson((Person) typeIdentifyPersonCombo.getValue());

        if (image.getImageType().getName().equals("Animal")) {

            TaxonomyService taxaService = new TaxonomyServiceImpl();
            for (int i = 0; i < Math.min(numberOfSpecies, genusLabels.size()); i++) {
                ImageSpecies imageSpecies = new ImageSpecies();
                imageSpecies.setImage(image);

                if (isBinomialNaming) {    //////

                    String genus = genusFields.get(i).getText();
                    String species = speciesFields.get(i).getText();
                    FamilyGenusSpecies fgs = taxaService.getFamilyGenusSpecies(genus, species);
                    imageSpecies.setFamilyGenusSpecies(fgs);

                    List<TaxaCommonEnglishName> commonEnglishNames = taxaService.getCommonEnglishNames(fgs);
                    if (imageSpecies.getEngishCommonName() == null && commonEnglishNames != null && commonEnglishNames.size() > 0) {
                        imageSpecies.setEngishCommonName(commonEnglishNames.get(0));
                    }

                    if (WildID.wildIDProperties.getEnableSubspecies()) {
                        imageSpecies.setSubspecies(subspeciesFields.get(i).getText());
                    }

                } else {

                    String commonName = commonNameFields.get(i).getText();
                    TaxaCommonEnglishName commonEnglishName = taxaService.getCommonEnglishNames(commonName);

                    imageSpecies.setFamilyGenusSpecies(commonEnglishName.getSpecies());
                    imageSpecies.setEngishCommonName(commonEnglishName);
                }

                imageSpecies.setPerson((Person) speciesIdentifiedPersonCombos.get(i).getValue());
                imageSpecies.setIndividualCount((int) numberCombos.get(i).getValue());
                imageSpecies.setUncertainty((ImageUncertaintyType) uncertaintyCombos.get(i).getValue());

                if (sapiensCombos.get(i) != null) {
                    Object object = sapiensCombos.get(i).getValue();
                    if (object != null) {
                        HomoSapiensType sapiensType = (HomoSapiensType) object;
                        imageSpecies.setHomoSapiensType(sapiensType);
                    }
                }

                image.getImageSpecieses().add(imageSpecies);
            }

        } else {
            image.getImageSpecieses().clear();
        }

        image.setNote(noteTextArea.getText());

        return image;
    }

    public Image getImage() {
        return this.image;
    }

    public void annotationSaved() {

        typeLabel.requestFocus();

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("project_anno_pane_annotation_saved_header"),
                language.getString("project_anno_pane_annotation_saved_context"),
                language.getString("alert_ok"));
    }

    public void annotationUpdated() {

        typeLabel.requestFocus();

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("project_anno_pane_annotation_updated_header"),
                language.getString("project_anno_pane_annotation_updated_context"),
                language.getString("alert_ok"));
    }

    private void checkHomoSapiensType(int index) {

        int rowIndex = getSpeciesRowIndex(index);
        System.out.println("rowIndex: " + rowIndex);
        
        String indexStr = (index == 0 ? "" : " (" + (index + 1) + ")");

        if (isBinomialNaming
                && genusFields.get(index).getText().equals("Homo")
                && speciesFields.get(index).getText() != null
                && speciesFields.get(index).getText().equals("sapiens")) {

            Label sapiensLabel = new Label(language.getString("project_anno_pane_sapeins") + indexStr);
            sapiensLabels.add(index, sapiensLabel);

            SapiensTypeComboBox sapiensCombo = new SapiensTypeComboBox(language, sapiensTypes);
            sapiensCombo.setMaxWidth(INPUT_WIDTH);
            sapiensCombos.add(index, sapiensCombo);

            int rowCount = getRowCount(this);
            if (rowIndex + 1 < rowCount) {
                Util.addEmptyRow(this, rowIndex + 1);
                this.add(sapiensLabel, 0, rowIndex + 1);
                this.add(sapiensCombo, 1, rowIndex + 1);
            }

            //subspeciesFields.get(index).setDisable(true);
            //sapiensCombos.get(index).setDisable(false);
        } else if (WildID.preference.getSpeciesNaming().equals("Common Name")
                && commonNameFields.get(index).getText() != null
                && commonNameFields.get(index).getText().equals("Human")) {

            Label sapiensLabel = new Label(language.getString("project_anno_pane_sapeins") + indexStr);
            sapiensLabels.add(index, sapiensLabel);

            SapiensTypeComboBox sapiensCombo = new SapiensTypeComboBox(language, sapiensTypes);
            sapiensCombo.setMaxWidth(INPUT_WIDTH);
            sapiensCombos.add(index, sapiensCombo);

            int rowCount = getRowCount(this);
            if (rowIndex + 1 < rowCount) {
                Util.addEmptyRow(this, rowIndex + 1);
                this.add(sapiensLabel, 0, rowIndex + 1);
                this.add(sapiensCombo, 1, rowIndex + 1);
            }

            //subspeciesFields.get(index).setDisable(true);
        } else if (sapiensLabels.get(index) != null) {

            Util.removeRow(this, rowIndex + 1);
            sapiensLabels.remove(sapiensLabels.get(index));
            sapiensLabels.add(index, null);

            sapiensCombos.remove(sapiensCombos.get(index));
            sapiensCombos.add(index, null);

            //subspeciesFields.get(index).setDisable(false);
            // sapiensCombos.get(index).setDisable(true);
        }
    }

    private int getSpeciesRowIndex_(int index) {

        int rowIndex = 3;
        for (int i = 0; i < index; i++) {
            if (genusFields.get(i).getText().equals("Homo") && speciesFields.get(i).getText().equals("sapiens")) {
                rowIndex += 7;
            } else {
                rowIndex += 6;
            }

            if (WildID.wildIDProperties.getEnableSubspecies() && isBinomialNaming) {
                rowIndex += 1;
            }
        }
        rowIndex += 2;

        System.out.println("rowIndex = " + rowIndex);

        return rowIndex;
    }

    private int getSpeciesRowIndex(int index) {
        Integer intObj;

        if (isBinomialNaming) {
            intObj = GridPane.getRowIndex(speciesFields.get(index));
        } else {
            intObj = GridPane.getRowIndex(commonNameFields.get(index));
        }

        if (intObj != null) {
            return intObj;
        } else {
            return 5;
        }
    }

    public boolean applyToGroup() {

        return groupCheckBox.selectedProperty().getValue();

    }

}
