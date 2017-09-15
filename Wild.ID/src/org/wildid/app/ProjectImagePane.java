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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.HomoSapiensType;
import org.wildid.entity.Image;
import org.wildid.entity.ImageExif;
import org.wildid.entity.ImageExifComparator;
import org.wildid.entity.ImageFeature;
import org.wildid.entity.ImageType;
import org.wildid.entity.ImageUncertaintyType;
import org.wildid.entity.Person;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectImagePane extends WildIDDataPane {

    private LanguageModel language;
    private WildIDController controller;
    private final Image image;
    private final List<Person> persons;
    private final List<ImageType> imageTypes;
    private final List<ImageUncertaintyType> imageUncertaintyTypes;
    private final List<HomoSapiensType> sapiensTypes;
    private final TabPane tabPane = new TabPane();
    private final Tab imageTab;
    private final Tab exifTab;
    private final Tab groupTab;
    public static Pane rightPane;
    private final Label titleLabel;
    private final double anno_pane_width = 350;
    private GridPane gridForm;
    private TitledPane tp;
    private ProjectImageAnnotationPane annoPane;
    private ProjectImageViewPane imageViewPane;
    private ProjectImageGroupEditorPane groupEditorPane;
    private ProjectImageExifPane exifPane;
    static Logger log = Logger.getLogger(ProjectImagePane.class.getName());
    private String selectedTab = "imageTab";
    public static boolean expanded_status = true;

    public ProjectImagePane(LanguageModel language,
            Image image,
            List<Person> persons,
            List<ImageType> imageTypes,
            List<ImageUncertaintyType> imageUncertaintyTypes,
            List<HomoSapiensType> sapiensTypes) throws IOException, InterruptedException {
        this(language, image, persons, imageTypes, imageUncertaintyTypes, sapiensTypes, "imageTab");
    }

    public ProjectImagePane(LanguageModel language,
            Image image,
            List<Person> persons,
            List<ImageType> imageTypes,
            List<ImageUncertaintyType> imageUncertaintyTypes,
            List<HomoSapiensType> sapiensTypes,
            String tab) throws IOException, InterruptedException {

        this.setId("ProjectImagePane");

        this.language = language;
        this.image = image;
        this.persons = persons;
        this.imageTypes = imageTypes;
        this.imageUncertaintyTypes = imageUncertaintyTypes;
        this.sapiensTypes = sapiensTypes;

        Deployment deployment = image.getImageSequence().getDeployment();
        Event event = deployment.getEvent();
        CameraTrap trap = deployment.getCameraTrap();

        this.imageTab = new Tab(language.getString("project_image_pane_image_tab_title"));
        this.exifTab = new Tab(language.getString("project_image_pane_exif_tab_title"));
        this.groupTab = new Tab(language.getString("project_image_pane_group_tab_title"));

        this.imageTab.setId("imageTab");
        this.exifTab.setId("exifTab");
        this.groupTab.setId("groupTab");

        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

            @Override
            public void changed(ObservableValue<? extends Tab> arg0, Tab oldTab, Tab newTab) {

                try {
                    selectedTab = newTab.getId();

                    if (newTab.getId().equals("imageTab")) {
                        if (!gridForm.getChildren().contains(rightPane)) {
                            gridForm.add(rightPane, 1, 0);
                        }
                    } else if (newTab.getId().equals("exifTab")) {
                        if (exifPane == null) {
                            exifPane = new ProjectImageExifPane(language, image);
                            exifTab.setContent(exifPane);
                            exifPane.prefWidthProperty().bind(widthProperty());
                            exifPane.prefHeightProperty().bind(heightProperty());
                        }

                        if (!ProjectImageExifPane.gridForm.getChildren().contains(rightPane)) {
                            ProjectImageExifPane.gridForm.add(rightPane, 1, 0);
                        }
                    } else if (newTab.getId().equals("groupTab")) {
                        if (groupEditorPane == null) {
                            groupEditorPane = new ProjectImageGroupEditorPane(language, image);
                            groupTab.setContent(groupEditorPane);
                            groupEditorPane.prefWidthProperty().bind(widthProperty());
                            groupEditorPane.prefHeightProperty().bind(heightProperty());
                            groupEditorPane.setWildIDController(controller);
                        }

                        if (!ProjectImageGroupEditorPane.gridForm.getChildren().contains(rightPane)) {
                            ProjectImageGroupEditorPane.gridForm.add(rightPane, 1, 0);
                        }
                    }
                } catch (IOException ex) {
                    log.info(ex.getMessage());
                }
            }
        });

        // setup image tab        
        this.titleLabel = new Label(language.getString("project_image_pane_title") + " : " + event.getName() + " : " + trap.getName() + " : " + image.getRawName());
        this.titleLabel.setStyle(TITLE_STYLE);

        HBox titleBox = new HBox();
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel);

        VBox vbox = new VBox(0);
        vbox.getChildren().addAll(titleBox, createForm());

        this.imageTab.setContent(vbox);

        if (tab.equals("exifTab")) {
            tabPane.getSelectionModel().select(exifTab);
        } else if (tab.equals("groupTab")) {
            tabPane.getSelectionModel().select(groupTab);
        }

        // setup tab pane
        this.tabPane.getTabs().addAll(imageTab, exifTab, groupTab);
        this.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        this.tabPane.prefWidthProperty().bind(this.widthProperty());
        this.tabPane.prefHeightProperty().bind(this.heightProperty());

        // add the tab pane into the current pane
        this.getChildren().add(tabPane);
    }

    private Pane createForm() throws IOException {
        gridForm = new GridPane();
        GridPane.setHgrow(gridForm, Priority.ALWAYS);
        GridPane.setVgrow(gridForm, Priority.ALWAYS);
        gridForm.setAlignment(Pos.TOP_LEFT);
        gridForm.setPadding(new Insets(30, 10, 10, 30));
        gridForm.setHgap(10);
        gridForm.setVgap(10);
        gridForm.setStyle(TEXT_STYLE);

        imageViewPane = new ProjectImageViewPane(language, image, null, true);
        imageViewPane.setMinWidth(200);

        gridForm.add(imageViewPane, 0, 0);
        GridPane.setValignment(imageViewPane, VPos.TOP);

        imageViewPane.prefWidthProperty().bind(this.widthProperty().subtract(anno_pane_width));
        imageViewPane.prefHeightProperty().bind(this.heightProperty());

        rightPane = createAnnotationRightPane();

        gridForm.add(rightPane, 1, 0);
        GridPane.setHalignment(rightPane, HPos.RIGHT);

        return gridForm;
    }

    private TitledPane createBasicImageExifForm() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setPadding(new Insets(20));
        grid.setHgap(20);
        grid.setVgap(0);
        grid.setStyle(TEXT_STYLE);
        grid.setStyle("-fx-background-color:transparent;");

        int countItem = 0;

        Set<ImageExif> imageExifs = image.getImageExifs();

        List<ImageExif> imgExifList = new ArrayList<>(imageExifs);
        Collections.sort(imgExifList, new ImageExifComparator());

        for (ImageExif imageExif : imgExifList) {
            ImageFeature feature = imageExif.getImageFeature();
            String tagName = feature.getName();
            String tagValue = imageExif.getExifTagValue();

            Text tagNameText = new Text(tagName);
            tagNameText.setStyle("-fx-font-weight: bold;");
            grid.add(tagNameText, 0, ++countItem);
            grid.add(new Text(tagValue), 1, countItem);
        }

        tp = new TitledPane(language.getString("project_image_pane_basic_exif_tags"), grid);
        tp.setExpanded(expanded_status);

        tp.expandedProperty().addListener((obs, status_was, status_now) -> {
            expanded_status = status_now;
        });

        return tp;
    }

    private Pane createAnnotationRightPane() {

        annoPane = new ProjectImageAnnotationPane(language, image, persons, imageTypes, imageUncertaintyTypes, sapiensTypes);
        annoPane.setPadding(new Insets(20));

        ScrollPane annoScrollPane = new ScrollPane();
        annoScrollPane.setContent(annoPane);
        annoScrollPane.setFitToHeight(true);
        annoScrollPane.setFitToWidth(true);
        annoScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        annoScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        annoScrollPane.prefHeightProperty().bind(this.heightProperty());

        VBox rightVbox = new VBox(10);
        rightVbox.getChildren().addAll(createBasicImageExifForm(), annoScrollPane);

        rightVbox.prefHeightProperty().bind(this.heightProperty());

        rightVbox.setMinWidth(anno_pane_width);
        rightVbox.setMaxWidth(anno_pane_width);
        rightVbox.setPrefWidth(anno_pane_width);

        return rightVbox;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.controller = controller;

        if (annoPane != null) {
            this.annoPane.setWildIDController(controller);
        }
        if (groupEditorPane != null) {
            this.groupEditorPane.setWildIDController(controller);
        }
        if (imageViewPane != null) {
            this.imageViewPane.setWildIDController(controller);
        }
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;

        this.imageTab.setText(language.getString("project_image_pane_image_tab_title"));
        this.exifTab.setText(language.getString("project_image_pane_exif_tab_title"));
        this.groupTab.setText(language.getString("project_image_pane_group_tab_title"));

        Deployment deployment = image.getImageSequence().getDeployment();
        Event event = deployment.getEvent();
        CameraTrap trap = deployment.getCameraTrap();
        this.titleLabel.setText(language.getString("project_image_pane_title") + " : " + event.getName() + " : " + trap.getName() + " : " + image.getRawName());

        if (annoPane != null) {
            annoPane.setLanguage(language);
        }
        if (groupEditorPane != null) {
            groupEditorPane.setLanguage(language);
        }
        if (exifPane != null) {
            exifPane.setLanguage(language);
        }
        if (imageViewPane != null) {
            imageViewPane.setLanguage(language);
        }

        tp.setText(language.getString("project_image_pane_basic_exif_tags"));
    }

    public ProjectImageAnnotationPane getAnnotationPane() {
        return annoPane;
    }

    public ProjectImageGroupEditorPane getGroupEditorPane() {
        return groupEditorPane;
    }

    public String getSelectedTab() {
        return this.selectedTab;
    }
}
