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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.log4j.Logger;
import static org.wildid.app.WildIDDataPane.TEXT_STYLE;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraModelExifFeature;
import org.wildid.entity.CameraModelExifFeatureComparator;
import org.wildid.entity.ImageFeature;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class CameraModelExifPane extends WildIDDataPane implements LanguageChangable {

    static Logger log = Logger.getLogger(CameraModelExifPane.class.getName());
    protected LanguageModel language;
    protected CameraModel cameraModel;
    protected List<ImageFeature> defaultImageFeatures;
    protected List<CameraModelExifFeature> modelFeatures;
    protected List<CameraModelExifFeatureComboBox> modelExifBoxes = new ArrayList<>();
    protected Map<String, String> oldAttrToExif = new HashMap<>();

    protected Label titleLabel;
    protected Label mappingLabel;

    protected Text openText;
    protected Text endText;

    protected Label attrHeader;
    protected Label exifHeader;
    protected Label statusHeader;
    protected Label valueHeader;

    protected GridPane grid;
    protected List<Label> statusLabels = new ArrayList<>();

    protected HBox buttonBox;
    protected Button openButton;
    protected Button saveButton;
    protected Button deleteButton;

    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");

    private final String headerStyle = "-fx-font-weight:bold; -fx-color: #f0f0f0; -fx-font-size:13; -fx-background-color: #cdcdcd;";
    private final String attrStyle = "-fx-font-weight:bold; -fx-color: #f0f0f0;";
    private final String normalStyle = "-fx-font-weight:normal; -fx-color: #f0f0f0;";
    private final String color_red = "#cd5c5c";
    private final String color_green = "#006400";

    public CameraModelExifPane(LanguageModel language,
            CameraModel cameraModel,
            List<ImageFeature> imageFeatures) {

        this.language = language;
        this.cameraModel = cameraModel;
        this.defaultImageFeatures = imageFeatures;

        this.setStyle(BG_COLOR_STYLE);
        this.setId("ExifPane");

        titleLabel = new Label(language.getString("camera_model_exif_pane_title") + " : " + cameraModel.getMaker() + " " + cameraModel.getName());
        titleLabel.setStyle(TITLE_STYLE);

        this.imgView = new ImageView(pageImg);
        this.imgView.setVisible(true);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel, this.imgView);

        VBox vbox = new VBox(0);
        grid = createForm(cameraModel.getCameraModelExifFeatures());
        vbox.getChildren().addAll(titleBox, grid);
        this.getChildren().add(vbox);
        grid.prefWidthProperty().bind(this.widthProperty());

        this.prefHeightProperty().bind(grid.prefHeightProperty().add(titleBox.prefHeightProperty()));

    }

    private GridPane createForm(Set<CameraModelExifFeature> modelExifFeatures) {

        GridPane gridpane = new GridPane();
        gridpane.setAlignment(Pos.TOP_LEFT);
        gridpane.setPadding(new Insets(30, 10, 30, 50));
        gridpane.setStyle(TEXT_STYLE);
        gridpane.setVgap(30);

        openText = new Text(language.getString("camera_model_exif_pane_open_text"));
        gridpane.add(openText, 1, 0);

        if (modelExifFeatures.isEmpty()) {
            //oldAttrToExif.putAll(getAttrToExifMapping(defaultImageFeatures));
            gridpane.add(createDefaultExifFeatureGrid(), 1, 1);
        } else {
            modelFeatures = new ArrayList<>();
            modelFeatures.addAll(cameraModel.getCameraModelExifFeatures());
            oldAttrToExif.putAll(getModelAttrToExifMapping(modelFeatures));
            gridpane.add(createModelExifFeatureGrid(modelFeatures), 1, 1);
        }

        endText = new Text(language.getString("camera_model_exif_pane_end_text"));
        endText.setWrappingWidth(600);
        gridpane.add(endText, 1, 2);

        // add a button for choosing an image file
        openButton = new Button(language.getString("camera_model_exif_pane_open_button"));
        final FileChooser fileChooser = new FileChooser();

        if (WildID.wildIDProperties.getWorkingDirObj() != null) {
            fileChooser.setInitialDirectory(WildID.wildIDProperties.getWorkingDirObj());
        }

        ExtensionFilter extFilter_jpg = new ExtensionFilter(language.getString("jpeg_extension_filter"), "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().addAll(extFilter_jpg);

        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                File file = fileChooser.showOpenDialog(titleLabel.getScene().getWindow());
                if (file != null) {
                    if (Util.isImage(file) && Util.isJpgFilename(file)) {
                        try {
                            modelExifBoxes.clear();
                            modelFeatures = loadExifFromImage(file);

                            GridPane modelExifFeatureGrid = createModelExifFeatureGrid(modelFeatures);
                            removeRow(gridpane, 1);
                            addEmptyRow(gridpane, 1);
                            gridpane.add(modelExifFeatureGrid, 1, 1);
                            if (isMappingChanged()) {
                                saveButton.setDisable(false);
                            } else {
                                saveButton.setDisable(true);
                            }
                        } catch (Exception ex) {
                            log.error(ex.getMessage());
                            ex.printStackTrace();
                        }
                    } else {
                        // popup error message
                    }
                }
            }
        });

        saveButton = new Button(language.getString("camera_model_exif_pane_save_button"));
        saveButton.setId("camera_model_exif_pane_save_button");
        saveButton.setDisable(true);

        buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(openButton, saveButton);

        deleteButton = new Button(language.getString("camera_model_exif_pane_delete_button"));
        deleteButton.setId("camera_model_exif_pane_delete_button");
        if (!modelExifFeatures.isEmpty()) {
            buttonBox.getChildren().add(deleteButton);
        }

        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        gridpane.add(buttonBox, 1, 3);

        return gridpane;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        if (this.deleteButton != null) {
            this.deleteButton.setOnAction(controller);
        }
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        this.titleLabel.setText(language.getString("camera_model_exif_pane_title") + " : " + cameraModel.getMaker() + " " + cameraModel.getName());
        this.openText.setText(language.getString("camera_model_exif_pane_open_text"));
        this.endText.setText(language.getString("camera_model_exif_pane_end_text"));
        this.openButton.setText(language.getString("camera_model_exif_pane_open_button"));
        this.saveButton.setText(language.getString("camera_model_exif_pane_save_button"));

        if (this.deleteButton != null) {
            this.deleteButton.setText(language.getString("camera_model_exif_pane_delete_button"));
        }

        this.attrHeader.setText(language.getString("camera_model_exif_pane_attr_header"));
        this.exifHeader.setText(language.getString("camera_model_exif_pane_exif_header"));
        this.statusHeader.setText(language.getString("camera_model_exif_pane_status_header"));
        if (this.valueHeader != null) {
            this.valueHeader.setText(language.getString("camera_model_exif_pane_value_header"));
        }
    }

    private List<CameraModelExifFeature> loadExifFromImage(File file) throws IOException {

        ExifTool tool = new ExifTool();
        List<String> tagNames = new ArrayList<>();
        Map<String, String> tagValues = new HashMap<>();
        Map<String, String> specialTagValues = new HashMap<>();
        tagValues.putAll(tool.getAllImageMeta(file, ExifTool.Format.NUMERIC, tagNames, specialTagValues, ExifTool.Tag.ALL));

        List<CameraModelExifFeature> model_features = new ArrayList<>();
        for (String tag : tagNames) {
            if (tagValues.get(tag) != null) {
                CameraModelExifFeature modelFeature = new CameraModelExifFeature();
                modelFeature.setCameraModel(cameraModel);
                modelFeature.setExifTagName(tag);
                ImageFeature imageFeature = getImageFeature(tag);
                if (imageFeature != null) {
                    modelFeature.setImageFeature(imageFeature);
                }
                modelFeature.setExifTagValue(tagValues.get(tag));
                model_features.add(modelFeature);

            } else {
                String value = specialTagValues.get(tag);
                Pattern r = Pattern.compile("[a-zA-Z]\\w+:");
                Matcher m = r.matcher(value);

                List<String> tags = new ArrayList<>();
                List<Integer> locs = new ArrayList<>();
                Set<Character> delimits = new HashSet<>();
                LinkedHashMap<String, String> tag2val = new LinkedHashMap<>();
                while (m.find()) {
                    tags.add(m.group(0));
                    locs.add(m.start());
                }

                for (int i = 0; i < tags.size(); i++) {
                    String sTag = tags.get(i);
                    int sLoc = locs.get(i);
                    if (i + 1 < locs.size()) {
                        int nLoc = locs.get(i + 1);
                        String sVal = value.substring(sLoc + sTag.length(), nLoc);
                        char c = sVal.charAt(sVal.length() - 1);
                        tag2val.put(sTag, sVal.substring(0, sVal.length() - 1).trim());
                        delimits.add(c);
                    }
                }

                if (delimits.size() == 1) {
                    // parsing is okay
                    for (String sTag : tag2val.keySet()) {
                        String sVal = tag2val.get(sTag);
                        CameraModelExifFeature modelFeature = new CameraModelExifFeature();
                        modelFeature.setCameraModel(cameraModel);
                        modelFeature.setExifTagName(tag);
                        modelFeature.setExifTagValue(sVal);
                        modelFeature.setSecondaryTagName(sTag.substring(0, sTag.length() - 1));
                        modelFeature.setSecondaryTagDelimit(delimits.iterator().next().toString());
                        model_features.add(modelFeature);
                    }
                } else {
                    // parsing failed
                    CameraModelExifFeature modelFeature = new CameraModelExifFeature();
                    modelFeature.setCameraModel(cameraModel);
                    modelFeature.setExifTagName(tag);
                    modelFeature.setExifTagValue(value);
                    ImageFeature imageFeature = getImageFeature(tag);
                    if (imageFeature != null) {
                        modelFeature.setImageFeature(imageFeature);
                    }
                    model_features.add(modelFeature);
                }

            }

        }

        return model_features;
    }

    private ImageFeature getImageFeature(String tag) {
        for (ImageFeature imageFeature : defaultImageFeatures) {
            if (imageFeature.getDefaultExifTagName().equals(tag)) {
                return imageFeature;
            }
        }
        return null;
    }

    private GridPane createModelExifFeatureGrid(List<CameraModelExifFeature> modelFeatures) {

        GridPane modelGrid = new GridPane();
        modelGrid.setHgap(10);
        modelGrid.setVgap(3);
        modelGrid.setPadding(new Insets(10));
        modelGrid.setSnapToPixel(false);
        modelGrid.setStyle(GRAY_DIV);

        statusLabels.clear();

        attrHeader = new Label(language.getString("camera_model_exif_pane_attr_header"));
        attrHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        attrHeader.setPadding(new Insets(10));
        attrHeader.setStyle(headerStyle);
        modelGrid.add(attrHeader, 1, 0);

        exifHeader = new Label(language.getString("camera_model_exif_pane_exif_header"));
        exifHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        exifHeader.setPadding(new Insets(10));
        exifHeader.setStyle(headerStyle);
        modelGrid.add(exifHeader, 2, 0);

        statusHeader = new Label(language.getString("camera_model_exif_pane_status_header"));
        statusHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        statusHeader.setPadding(new Insets(10));
        statusHeader.setStyle(headerStyle);
        modelGrid.add(statusHeader, 3, 0);

        valueHeader = new Label(language.getString("camera_model_exif_pane_value_header"));
        valueHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        valueHeader.setPadding(new Insets(10));
        valueHeader.setStyle(headerStyle);
        modelGrid.add(valueHeader, 4, 0);

        int count = 1;
        for (ImageFeature feature : defaultImageFeatures) {
            boolean found = false;
            for (CameraModelExifFeature modelFeature : modelFeatures) {
                if (modelFeature.getImageFeature() != null
                        && modelFeature.getImageFeature().getImageFeatureId().intValue() == feature.getImageFeatureId()) {
                    found = true;

                    if (feature.getDefaultExifTagName().equals(modelFeature.getExifTagName())) {

                        Label attrLabel = new Label(feature.getName());
                        attrLabel.setPadding(new Insets(2));
                        attrLabel.setStyle(attrStyle);
                        modelGrid.add(attrLabel, 1, count);

                        Label tagLabel = new Label(feature.getDefaultExifTagName());
                        tagLabel.setPadding(new Insets(2));
                        tagLabel.setStyle(normalStyle);
                        modelGrid.add(tagLabel, 2, count);

                        Label statusLabel = new Label(language.getString("camera_model_exif_pane_status_matched"));
                        statusLabel.setPadding(new Insets(2));
                        statusLabels.add(statusLabel);

                        String oldTag = oldAttrToExif.get(feature.getName());
                        String newTag = modelFeature.getExifTagName();
                        if (modelFeature.getSecondaryTagName() != null) {
                            newTag += "." + modelFeature.getSecondaryTagName();
                        }
                        if (oldTag == null && newTag != null
                                || oldTag != null && newTag == null
                                || oldTag != null && newTag != null && !oldTag.equals(newTag)) {
                            statusLabel.setTextFill(Color.web(color_red));
                        } else {
                            statusLabel.setTextFill(Color.web(color_green));
                        }

                        statusLabel.setMinWidth(120);
                        statusLabel.setStyle(normalStyle);
                        modelGrid.add(statusLabel, 3, count);

                        Label valueLabel = new Label(modelFeature.getExifTagValue());
                        valueLabel.setPadding(new Insets(2));
                        valueLabel.setStyle(normalStyle);
                        modelGrid.add(valueLabel, 4, count);

                        count++;

                    } else {

                        // already mapped
                        Label attrLabel = new Label(feature.getName());
                        attrLabel.setPadding(new Insets(2));
                        attrLabel.setStyle(attrStyle);
                        modelGrid.add(attrLabel, 1, count);

                        Label statusLabel = new Label(language.getString("camera_model_exif_pane_status_mapped"));
                        statusLabel.setPadding(new Insets(2));
                        statusLabels.add(statusLabel);

                        String oldTag = oldAttrToExif.get(feature.getName());
                        String newTag = modelFeature.getExifTagName();
                        if (modelFeature.getSecondaryTagName() != null) {
                            newTag += "." + modelFeature.getSecondaryTagName();
                        }
                        if (oldTag == null && newTag != null
                                || oldTag != null && newTag == null
                                || oldTag != null && newTag != null && !oldTag.equals(newTag)) {
                            statusLabel.setTextFill(Color.web(color_red));
                        } else {
                            statusLabel.setTextFill(Color.web(color_green));
                        }

                        //statusLabel.setTextFill(Color.web("#191970"));
                        statusLabel.setMinWidth(120);
                        statusLabel.setStyle(normalStyle);
                        modelGrid.add(statusLabel, 3, count);

                        Label valueLabel = new Label(modelFeature.getExifTagValue());
                        valueLabel.setPadding(new Insets(2));
                        valueLabel.setStyle(normalStyle);
                        modelGrid.add(valueLabel, 4, count);

                        List<CameraModelExifFeature> tmpModelFeatures = new ArrayList<>();
                        tmpModelFeatures.addAll(modelFeatures);
                        CameraModelExifFeatureComboBox box = new CameraModelExifFeatureComboBox(feature, tmpModelFeatures);
                        box.getItems().add(null);   // could be null
                        box.setValue(modelFeature);
                        box.setOnAction((evt) -> {
                            CameraModelExifFeature selectedExifFeature = (CameraModelExifFeature) box.getSelectionModel().getSelectedItem();
                            if (selectedExifFeature != null) {
                                valueLabel.setText(selectedExifFeature.getExifTagValue());
                                statusLabel.setText(language.getString("camera_model_exif_pane_status_mapped"));
                            } else {
                                valueLabel.setText("");
                                statusLabel.setText(language.getString("camera_model_exif_pane_status_ignored"));
                            }

                            if (isMappingChanged(box)) {
                                statusLabel.setTextFill(Color.web(color_red));
                            } else {
                                statusLabel.setTextFill(Color.web(color_green));
                            }

                            if (isMappingChanged()) {
                                saveButton.setDisable(false);
                            } else {
                                saveButton.setDisable(true);
                            }

                        });
                        modelExifBoxes.add(box);
                        modelGrid.add(box, 2, count);

                        count++;

                    }

                    break;
                }
            }
            if (!found) {

                Label attrLabel = new Label(feature.getName());
                attrLabel.setPadding(new Insets(2));
                attrLabel.setStyle(attrStyle);
                modelGrid.add(attrLabel, 1, count);

                Label statusLabel = new Label(language.getString("camera_model_exif_pane_status_ignored"));
                statusLabels.add(statusLabel);
                statusLabel.setPadding(new Insets(2));
                String oldTag = oldAttrToExif.get(feature.getName());
                if (oldTag != null) {
                    statusLabel.setTextFill(Color.web(color_red));
                } else {
                    statusLabel.setTextFill(Color.web(color_green));
                }
                statusLabel.setPrefWidth(80);
                statusLabel.setStyle(normalStyle);
                modelGrid.add(statusLabel, 3, count);

                Label valueLabel = new Label("");
                valueLabel.setPadding(new Insets(2));
                valueLabel.setStyle(normalStyle);
                modelGrid.add(valueLabel, 4, count);

                List<CameraModelExifFeature> tmpModelFeatures = new ArrayList<>();
                TreeSet<CameraModelExifFeature> sortedFeatures = new TreeSet<>(new CameraModelExifFeatureComparator());
                sortedFeatures.addAll(modelFeatures);
                tmpModelFeatures.addAll(sortedFeatures);
                CameraModelExifFeatureComboBox box = new CameraModelExifFeatureComboBox(feature, tmpModelFeatures);
                box.getItems().add(null);   // could be null
                box.setValue(null);
                box.setOnAction((evt) -> {
                    CameraModelExifFeature selectedExifFeature = (CameraModelExifFeature) box.getSelectionModel().getSelectedItem();
                    if (selectedExifFeature != null) {
                        valueLabel.setText(selectedExifFeature.getExifTagValue());
                        statusLabel.setText(language.getString("camera_model_exif_pane_status_mapped"));
                    } else {
                        valueLabel.setText("");
                        statusLabel.setText(language.getString("camera_model_exif_pane_status_ignored"));
                    }

                    if (isMappingChanged(box)) {
                        statusLabel.setTextFill(Color.web(color_red));
                    } else {
                        statusLabel.setTextFill(Color.web(color_green));
                    }

                    if (isMappingChanged()) {
                        saveButton.setDisable(false);
                    } else {
                        saveButton.setDisable(true);
                    }

                });
                modelExifBoxes.add(box);
                modelGrid.add(box, 2, count);

                count++;

            }
        }

        return modelGrid;
    }

    private GridPane createDefaultExifFeatureGrid() {

        GridPane defaultGrid = new GridPane();
        defaultGrid.setHgap(10);
        defaultGrid.setVgap(3);
        defaultGrid.setPadding(new Insets(10));
        defaultGrid.setSnapToPixel(false);
        defaultGrid.setStyle(GRAY_DIV);

        statusLabels.clear();

        attrHeader = new Label(language.getString("camera_model_exif_pane_attr_header"));
        attrHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        attrHeader.setPadding(new Insets(10));
        attrHeader.setStyle(headerStyle);
        defaultGrid.add(attrHeader, 2, 0);

        exifHeader = new Label(language.getString("camera_model_exif_pane_exif_header"));
        exifHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        exifHeader.setPadding(new Insets(10));
        exifHeader.setStyle(headerStyle);
        defaultGrid.add(exifHeader, 3, 0);

        statusHeader = new Label(language.getString("camera_model_exif_pane_status_header"));
        statusHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        statusHeader.setPadding(new Insets(10));
        statusHeader.setMinWidth(120);
        statusHeader.setStyle(headerStyle);

        defaultGrid.add(statusHeader, 4, 0);

        int count = 1;
        for (ImageFeature feature : defaultImageFeatures) {

            Label attrLabel = new Label(feature.getName());
            attrLabel.setPadding(new Insets(2));
            attrLabel.setStyle(attrStyle);
            defaultGrid.add(attrLabel, 2, count);

            Label exifTagLabel = new Label(feature.getDefaultExifTagName());
            exifTagLabel.setPadding(new Insets(2));
            exifTagLabel.setStyle(normalStyle);
            defaultGrid.add(exifTagLabel, 3, count);

            Label statusLabel = new Label(language.getString("camera_model_exif_pane_status_not_verified"));
            statusLabels.add(statusLabel);
            statusLabel.setPadding(new Insets(2));
            statusLabel.setTextFill(Color.web(color_red));
            statusLabel.setMinWidth(120);
            statusLabel.setStyle(normalStyle);
            defaultGrid.add(statusLabel, 4, count);

            count++;
        }
        return defaultGrid;
    }

    protected void addEmptyRow(GridPane gridPane, int row) {
        List<Node> children = new LinkedList<>(gridPane.getChildren());
        for (Node node : children) {
            int nodeRow = GridPane.getRowIndex(node);
            if (nodeRow >= row) {
                GridPane.setRowIndex(node, nodeRow + 1);
            }
        }
    }

    protected void removeRow(GridPane gridPane, int row) {
        List<Node> children = new LinkedList<>(gridPane.getChildren());
        for (Node node : children) {
            int nodeRow = GridPane.getRowIndex(node);
            if (nodeRow == row) {
                gridPane.getChildren().remove(node);
            } else if (nodeRow > row) {
                GridPane.setRowIndex(node, nodeRow - 1);
            }
        }
    }

    public List<CameraModelExifFeature> getModelExifFeatures() {

        // merge all comboxboxes with model exif features
        for (CameraModelExifFeatureComboBox box : modelExifBoxes) {
            ImageFeature imageFeature = box.getFeature();
            CameraModelExifFeature selectedExifFeature = (CameraModelExifFeature) box.getSelectionModel().getSelectedItem();

            for (CameraModelExifFeature modelFeature : modelFeatures) {
                if (modelFeature.getImageFeature() != null && modelFeature.getImageFeature().getImageFeatureId().intValue() == imageFeature.getImageFeatureId()) {
                    modelFeature.setImageFeature(null);
                }
            }

            if (selectedExifFeature != null) {
                selectedExifFeature.setImageFeature(imageFeature);
            }
        }

        return modelFeatures;

    }

    public CameraModel getCameraModel() {
        return this.cameraModel;
    }

    private Map<String, String> getModelAttrToExifMapping(List<CameraModelExifFeature> modelFeatures) {

        Map<String, String> result = new HashMap<>();
        for (CameraModelExifFeature feature : modelFeatures) {
            if (feature.getImageFeature() != null) {
                String tag = feature.getExifTagName();
                String stag = feature.getSecondaryTagName();
                if (stag != null) {
                    tag += "." + stag;
                }
                result.put(feature.getImageFeature().getName(), tag);
            }
        }
        return result;
    }

    private boolean isMappingChanged() {

        for (CameraModelExifFeatureComboBox box : modelExifBoxes) {
            if (isMappingChanged(box)) {
                return true;
            }
        }

        for (CameraModelExifFeature modelFeature : modelFeatures) {
            if (modelFeature.getCameraModelExifFeatureId() == null) {
                return true;
            }
        }

        return false;
    }

    private boolean isMappingChanged(CameraModelExifFeatureComboBox box) {

        ImageFeature imageFeature = box.getFeature();    // attribute name
        CameraModelExifFeature selectedExifFeature = (CameraModelExifFeature) box.getSelectionModel().getSelectedItem();  // new exif tag name
        String oldTag = oldAttrToExif.get(imageFeature.getName());    // old exif tag name

        if (selectedExifFeature == null) {
            if (oldTag != null) {
                return true;
            }
        } else if (oldTag == null) {
            return true;
        } else {
            String newTag = selectedExifFeature.getExifTagName();
            if (selectedExifFeature.getSecondaryTagName() != null) {
                newTag += "." + selectedExifFeature.getSecondaryTagName();
            }
            if (!newTag.equals(oldTag)) {
                return true;
            }
        }

        return false;
    }

    public void exifCameraModelFeaturesSaved() {

        for (Label statusLabel : statusLabels) {
            statusLabel.setTextFill(Color.web(color_green));
        }

        this.saveButton.setDisable(true);

        if (!modelFeatures.isEmpty() && !buttonBox.getChildren().contains(deleteButton)) {
            buttonBox.getChildren().add(deleteButton);
        }

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("camera_model_exif_pane_confirm_header"),
                language.getString("camera_model_exif_pane_confirm_context"),
                language.getString("alert_ok"));
    }

    public void reset() {

        modelExifBoxes.clear();
        modelFeatures.clear();
        oldAttrToExif.clear();
        statusLabels.clear();

        removeRow(grid, 1);
        addEmptyRow(grid, 1);
        grid.add(createDefaultExifFeatureGrid(), 1, 1);

        saveButton.setDisable(true);
        buttonBox.getChildren().remove(deleteButton);
    }

    public boolean confirmDelete() {
        return Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("camera_model_exif_pane_delete_confirm_header"),
                language.getString("camera_model_exif_pane_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));
    }

}
