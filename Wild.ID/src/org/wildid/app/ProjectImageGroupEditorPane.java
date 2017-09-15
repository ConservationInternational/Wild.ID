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

import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.Image;
import org.wildid.entity.ImageComparator;
import org.wildid.entity.ImageRepository;
import org.wildid.entity.ImageSequence;
import org.wildid.service.ImageService;
import org.wildid.service.ImageServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectImageGroupEditorPane extends WildIDDataPane implements LanguageChangable {

    private LanguageModel language;
    private final Image image;
    private final int thumb_width = 150;
    private final Label titleLabel;
    private Button saveButton;
    private Button preSequenceBtn;
    private Button nextSequenceBtn;
    private CheckBox[] thumbCheckBoxes;
    private TilePane tile;
    private static Stage dialog;
    private List<Image> imgList;
    private final Deployment deployment;
    private final Event event;
    private final CameraTrap trap;
    private final ImageSequence sequence;
    private final List<Tooltip> tooltips = new ArrayList<>();
    private final double anno_pane_width = 350;
    public static GridPane gridForm;
    private Hyperlink checkAllLink;
    private Hyperlink unCheckAllLink;
    static Logger log = Logger.getLogger(ProjectImageGroupEditorPane.class.getName());

    public ProjectImageGroupEditorPane(
            LanguageModel language,
            Image image) throws IOException {

        this.language = language;
        this.image = image;

        deployment = image.getImageSequence().getDeployment();
        event = deployment.getEvent();
        trap = deployment.getCameraTrap();
        sequence = image.getImageSequence();

        this.titleLabel = new Label(language.getString("project_image_pane_title") + " : " + event.getName() + " : " + trap.getName() + " : " + image.getRawName());
        this.titleLabel.setStyle(WildIDDataPane.TITLE_STYLE);

        this.setStyle(WildIDDataPane.TEXT_STYLE);

        HBox titleBox = new HBox();
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);

        vbox.getChildren().addAll(titleBox, createForm());
        this.getChildren().add(vbox);

        vbox.prefWidthProperty().bind(this.widthProperty());
        vbox.prefHeightProperty().bind(this.heightProperty());
    }

    private Pane createForm() {
        gridForm = new GridPane();
        GridPane.setHgrow(gridForm, Priority.ALWAYS);
        GridPane.setVgrow(gridForm, Priority.ALWAYS);
        gridForm.setAlignment(Pos.TOP_LEFT);
        gridForm.setPadding(new Insets(30, 10, 10, 30));
        gridForm.setHgap(10);
        gridForm.setVgap(10);
        gridForm.setStyle(TEXT_STYLE);

        tile = new TilePane();
        tile.setHgap(10);
        tile.setVgap(10);
        tile.setPadding(new Insets(0));

        Set<Image> images = sequence.getImages();

        imgList = new ArrayList<>(images);
        Collections.sort(imgList, new ImageComparator());

        InputStream is = null;
        int selectedIndex = 0;

        try {
            thumbCheckBoxes = new CheckBox[imgList.size()];
            String selected_thumb_style = "-fx-border-color: #ffff66; -fx-border-width: 3;";
            String thumb_style = "-fx-border-color: #f0f0f0; -fx-border-width: 3;";

            for (int i = 0; i < imgList.size(); i++) {
                Image img = imgList.get(i);

                File thumbFile = ImageRepository.getThumbFile(img);

                if (!thumbFile.exists()) {
                    // create a thumbnail for the first time                    
                    File thumbFolder = ImageRepository.getThumbFolder(sequence.getDeployment());

                    if (!thumbFolder.exists()) {
                        thumbFolder.mkdirs();
                    }

                    BufferedImage bufferedImage = ImageIO.read(ImageRepository.getFile(img));
                    BufferedImage t_bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, thumb_width, Scalr.OP_ANTIALIAS);
                    ImageIO.write(t_bufferedImage, "jpg", thumbFile);
                    bufferedImage.flush();
                    t_bufferedImage.flush();
                }

                is = new FileInputStream(thumbFile);
                javafx.scene.image.Image t_img = new javafx.scene.image.Image(is, thumb_width, -1, true, false);
                is.close();

                ImageView thumbView = new ImageView(t_img);

                HBox hBox_thumb = new HBox();
                hBox_thumb.getChildren().add(thumbView);

                if (this.image.getImageId().intValue() == img.getImageId()) {
                    hBox_thumb.setStyle(selected_thumb_style);
                    selectedIndex = i;
                } else {
                    hBox_thumb.setStyle(thumb_style);
                }

                Tooltip tooltip = new Tooltip(img.getRawName() + "\n" + language.getString("project_group_pane_tooltip_large_image"));
                tooltip.setId(img.getRawName());

                Tooltip.install(thumbView, tooltip);
                tooltips.add(tooltip);

                thumbView.setOnMouseClicked(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        if (dialog == null || !dialog.isShowing()) {
                            dialog = new Stage();
                            dialog.setResizable(true);
                            dialog.setMinWidth(200);
                        }

                        try {
                            File imageFile = ImageRepository.getFile(img);
                            double default_width = 800;
                            Pane gpane = new ProjectImageViewPane(language, img, dialog);
                            javafx.scene.image.Image popImg = new javafx.scene.image.Image(new FileInputStream(imageFile));

                            Scene scene;
                            double ratio = popImg.getWidth() / popImg.getHeight();

                            if (dialog.getScene() == null) {
                                scene = new Scene(gpane, default_width, default_width / ratio);
                                dialog.setScene(scene);
                                dialog.show();
                            } else {
                                scene = dialog.getScene();
                                scene.setRoot(gpane);
                            }

                            if (!dialog.isFocused()) {
                                dialog.requestFocus();
                            }

                            if (dialog.isIconified()) {
                                dialog.setIconified(false);
                            }

                            dialog.toFront();

                            dialog.setTitle(img.getRawName());

                        } catch (IOException ex) {
                            log.info(ex.getMessage());
                        }
                    }
                });

                Label thumbLabel = new Label(img.getRawName());
                thumbCheckBoxes[i] = new CheckBox("");
                thumbCheckBoxes[i].setId("cb_" + img.getImageId());
                HBox thumbHbox = new HBox(0);

                if (imgList.size() == 1) {
                    thumbCheckBoxes[i].setDisable(true);
                    thumbHbox.getChildren().addAll(thumbLabel);
                } else {
                    thumbCheckBoxes[i].selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            if (newValue) {
                                thumbView.setOpacity(0.5);
                            } else {
                                thumbView.setOpacity(1);
                            }
                        }
                    });

                    thumbHbox.getChildren().addAll(thumbCheckBoxes[i], thumbLabel);
                }
                thumbHbox.setAlignment(Pos.CENTER);

                VBox thumbVbox = new VBox(10);
                thumbVbox.getChildren().addAll(hBox_thumb, thumbHbox);
                thumbVbox.setPadding(new Insets(0, 0, 20, 0));
                thumbVbox.setAlignment(Pos.TOP_CENTER);
                thumbVbox.setId("vbox_" + img.getImageId());

                tile.getChildren().addAll(thumbVbox);
            }
        } catch (IOException | IllegalArgumentException | ImagingOpException ex) {
            //ex.printStackTrace();
            log.error(ex.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    log.info(ex.getMessage());
                }
            }
        }

        ImageService imageService = new ImageServiceImpl();
        List<Integer> sequences = imageService.getOrderedSequences(deployment);

        int sid = 0;
        int preSequence = -1;
        int nextSequence = -1;
        preSequenceBtn = new Button("<< " + language.getString("project_group_pane_previous_group"));
        nextSequenceBtn = new Button(language.getString("project_group_pane_next_group") + " >>");

        for (Integer intObj : sequences) {
            if (intObj.intValue() == sequence.getImageSequenceId()) {
                sid = sequences.indexOf(intObj);
            }
        }

        if (sid > 0) {
            preSequence = sequences.get(sid - 1);
        } else {
            preSequenceBtn.setDisable(true);
        }

        if (sid < sequences.size() - 1) {
            nextSequence = sequences.get(sid + 1);
        } else {
            nextSequenceBtn.setDisable(true);
        }

        preSequenceBtn.setId("jump_to_group_" + preSequence);
        nextSequenceBtn.setId("jump_to_group_" + nextSequence);

        ScrollPane scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVvalue((double) selectedIndex / (double) imgList.size());
        scroll.setStyle("-fx-background-color:transparent;");

        saveButton = new Button(language.getString("project_group_pane_remove_button"));
        saveButton.setId("project_group_pane_remove_button");

        checkAllLink = new Hyperlink(language.getString("project_group_pane_check_all"));
        checkAllLink.setVisited(true);
        checkAllLink.setOnAction((ActionEvent aevent) -> {
            for (CheckBox thumbCheckBox : thumbCheckBoxes) {
                thumbCheckBox.setSelected(true);
            }
        });

        unCheckAllLink = new Hyperlink(language.getString("project_group_pane_uncheck_all"));
        unCheckAllLink.setVisited(true);
        unCheckAllLink.setOnAction((ActionEvent aevent) -> {
            for (CheckBox thumbCheckBox : thumbCheckBoxes) {
                thumbCheckBox.setSelected(false);
            }
        });

        VBox leftVbox = new VBox(20);
        leftVbox.setAlignment(Pos.TOP_LEFT);

        leftVbox.getChildren().add(tile);
        leftVbox.prefWidthProperty().bind(this.widthProperty().subtract(anno_pane_width + 52));
        scroll.prefHeightProperty().bind(this.heightProperty());

        if (imgList.size() <= 1) {
            saveButton.setDisable(true);
        }

        HBox hb = new HBox();
        hb.getChildren().addAll(saveButton);
        hb.setAlignment(Pos.CENTER);
        leftVbox.getChildren().add(hb);

        scroll.setContent(leftVbox);
        scroll.setMinWidth(200);

        HBox sequence_hb = new HBox(20);
        sequence_hb.getChildren().addAll(preSequenceBtn, checkAllLink, unCheckAllLink, nextSequenceBtn);
        sequence_hb.setAlignment(Pos.CENTER);

        VBox leftPane = new VBox(20);
        leftPane.getChildren().addAll(sequence_hb, scroll);

        gridForm.add(leftPane, 0, 0);
        GridPane.setValignment(scroll, VPos.TOP);

        gridForm.add(ProjectImagePane.rightPane, 1, 0);
        return gridForm;
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        titleLabel.setText(language.getString("project_image_pane_title") + " : " + event.getName() + " : " + trap.getName() + " : " + image.getRawName());
        saveButton.setText(language.getString("project_group_pane_remove_button"));
        checkAllLink.setText(language.getString("project_group_pane_check_all"));
        unCheckAllLink.setText(language.getString("project_group_pane_uncheck_all"));
        preSequenceBtn.setText("<< " + language.getString("project_group_pane_previous_group"));
        nextSequenceBtn.setText(language.getString("project_group_pane_next_group") + " >>");

        for (Tooltip tooltip : tooltips) {
            tooltip.setText(tooltip.getId() + "\n" + language.getString("project_group_pane_tooltip_large_image"));
        }

        if (dialog != null && dialog.getScene() != null) {
            ProjectImageViewPane gpane = (ProjectImageViewPane) dialog.getScene().getRoot();
            gpane.setLanguage(language);
        }
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        this.preSequenceBtn.setOnAction(controller);
        this.nextSequenceBtn.setOnAction(controller);
    }

    public boolean validate() {

        boolean ok = true;

        String title = null;
        String header = null;
        String context = null;

        boolean checked = false;

        for (CheckBox thumbCheckBox : thumbCheckBoxes) {
            if (thumbCheckBox.selectedProperty().getValue()) {
                checked = true;
                break;
            }
        }

        if (!checked) {
            title = language.getString("title_error");
            header = language.getString("project_group_pane_not_check_error_header");
            context = language.getString("project_group_pane_not_check_error_context");
            ok = false;
        }

        boolean allChecked = true;

        for (CheckBox thumbCheckBox : thumbCheckBoxes) {
            if (!thumbCheckBox.selectedProperty().getValue()) {
                allChecked = false;
                break;
            }
        }

        if (allChecked) {
            title = language.getString("title_error");
            header = language.getString("project_group_pane_all_check_error_header");
            context = language.getString("project_group_pane_all_check_error_context");
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

    public Image getImage() {
        return this.image;
    }

    public CheckBox[] getCheckBoxes() {
        return this.thumbCheckBoxes;
    }

    public TilePane getTile() {
        return this.tile;
    }

    public Stage getDialog() {
        return ProjectImageGroupEditorPane.dialog;
    }
}
