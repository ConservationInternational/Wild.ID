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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.wildid.entity.Image;
import org.wildid.entity.ImageComparator;
import org.wildid.entity.ImageIndividual;
import org.wildid.entity.ImageIndividualComparator;
import org.wildid.entity.ImageRepository;
import org.wildid.entity.ImageSequence;
import org.wildid.entity.ImageSpecies;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectImageViewPane extends WildIDDataPane implements LanguageChangable {

    private LanguageModel language;
    private Image image;
    private Button button_plus;
    private Button button_minus;
    private Button button_next;
    private Button button_prev;
    private Button button_actual_size;
    private boolean isFitWidth = true;
    private List<Image> imgList;
    private ImageSequence sequence;
    private int current_index;
    private ScrollPane scroll;
    private ImageView imgView;
    private Stage popup_window;
    private Tooltip tooltip_zoomin;
    private Tooltip tooltip_zoomout;
    private Tooltip tooltip_actual_size;
    private Tooltip tooltip_fit_width;
    private Tooltip tooltip_next;
    private Tooltip tooltip_prev;
    private Tooltip tooltip_button_img_individual;
    private CheckBox checkbox_toggle_img_individual;
    private static boolean display_img_individual_checked = true;
    private double zoomNumber = 1.05;
    private StackPane stackPane = new StackPane();
    private StackPane imgStackPane = new StackPane();
    private double ori_width = 0;
    private DoubleBinding ratio_binding;
    private ImageCursor imgCursor;
    private Button button_img_individual;
    private boolean cursorOn = false;
    private boolean visible_individual = true;
    private int count_ii = 0;

    private WildIDController controller;
    private List<ImageIndividual> imageIndividualSet = new ArrayList<>();
    private Map<ImageIndividual, ImageView> imgIndividual2ImgView = new HashMap<>();
    static Logger log = Logger.getLogger(ProjectImageViewPane.class.getName());

    public ProjectImageViewPane(
            LanguageModel language,
            Image image,
            Stage popup_window) throws IOException {

        this(language, image, popup_window, false);
    }

    public ProjectImageViewPane(
            LanguageModel language,
            Image image,
            Stage popup_window,
            boolean isMain) throws IOException {

        this.language = language;
        this.image = image;
        this.popup_window = popup_window;

        this.setStyle(WildIDDataPane.TEXT_STYLE);
        this.prefWidthProperty().bind(this.widthProperty());

        visible_individual = WildID.wildIDProperties.getEnableImageIndividual() && isMain;

        if (visible_individual) {
            if (image.getImageType() == null || !image.getImageType().getName().equals("Animal")) {
                visible_individual = false;
            }
        }

        javafx.scene.image.Image imagefx;
        InputStream is = null;

        try {
            File imgFile = ImageRepository.getFile(this.image);
            is = new FileInputStream(imgFile);
            imagefx = new javafx.scene.image.Image(is);
            ori_width = imagefx.getWidth();

            imgView = new ImageView(imagefx);
            imgView.setPreserveRatio(true);

            ratio_binding = new DoubleBinding() {
                {
                    super.bind(imgView.fitWidthProperty());
                }

                @Override
                protected double computeValue() {
                    return (imgView.fitWidthProperty().get() / ori_width);
                }
            };

            imgView.setOnMouseExited((MouseEvent e) -> {
                if (visible_individual && cursorOn) {
                    setImageIndividualStatus(false);
                }
            });

            imgView.setOnMouseClicked((MouseEvent e) -> {
                if (visible_individual && cursorOn) {
                    int img_x = (int) (e.getX() / ratio_binding.get());
                    int img_y = (int) (e.getY() / ratio_binding.get());

                    this.controller.addingImageIndividual(this, image, img_x, img_y, e.getScreenX(), e.getScreenY());
                    setImageIndividualStatus(false);
                }
            });

            imgStackPane.getChildren().add(imgView);

            scroll = new ScrollPane();
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Horizontal scroll bar
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Vertical scroll bar
            scroll.prefWidthProperty().bind(this.widthProperty());
            scroll.prefHeightProperty().bind(this.heightProperty());
            scroll.setStyle("-fx-background-color:transparent; ");
            scroll.setPannable(true);
            scroll.setContent(imgStackPane);

            setImageFitWidthWindow();

            ImageView iv_plus = new ImageView();
            iv_plus.setImage(new javafx.scene.image.Image("resources/icons/img_plus.png"));
            button_plus = new Button();
            button_plus.setStyle(BTN_TRANSPARENT_STYLE);
            button_plus.setCursor(Cursor.HAND);
            button_plus.setGraphic(iv_plus);
            button_plus.setOpacity(0.5);

            tooltip_zoomin = new Tooltip(this.language.getString("project_imageview_pane_tooltip_zoomin"));
            button_plus.setTooltip(tooltip_zoomin);
            button_plus.setOnMouseEntered((MouseEvent t) -> {
                button_plus.setOpacity(1);
            });
            button_plus.setOnMouseExited((MouseEvent t) -> {
                button_plus.setOpacity(0.5);
            });

            ImageView iv_minus = new ImageView();
            iv_minus.setImage(new javafx.scene.image.Image("resources/icons/img_minus.png"));
            button_minus = new Button();
            button_minus.setStyle(BTN_TRANSPARENT_STYLE);
            button_minus.setCursor(Cursor.HAND);
            button_minus.setGraphic(iv_minus);
            button_minus.setOpacity(0.5);
            tooltip_zoomout = new Tooltip(this.language.getString("project_imageview_pane_tooltip_zoomout"));
            button_minus.setTooltip(tooltip_zoomout);
            button_minus.setOnMouseEntered((MouseEvent t) -> {
                button_minus.setOpacity(1);
            });
            button_minus.setOnMouseExited((MouseEvent t) -> {
                button_minus.setOpacity(0.5);
            });

            ImageView iv_actual_size = new ImageView();
            iv_actual_size.setImage(new javafx.scene.image.Image("resources/icons/img_actual_size.png"));
            button_actual_size = new Button();
            button_actual_size.setStyle(BTN_TRANSPARENT_STYLE);
            button_actual_size.setCursor(Cursor.HAND);
            button_actual_size.setGraphic(iv_actual_size);
            button_actual_size.setOpacity(0.5);
            tooltip_actual_size = new Tooltip(this.language.getString("project_imageview_pane_tooltip_actual_size"));
            button_actual_size.setTooltip(tooltip_actual_size);
            button_actual_size.setOnMouseEntered((MouseEvent t) -> {
                button_actual_size.setOpacity(1);
            });
            button_actual_size.setOnMouseExited((MouseEvent t) -> {
                button_actual_size.setOpacity(0.5);
            });

            ImageView iv_button_img_individual = new ImageView();
            iv_button_img_individual.setImage(new javafx.scene.image.Image("resources/icons/locator.png"));
            button_img_individual = new Button();
            button_img_individual.setStyle(BTN_TRANSPARENT_STYLE);
            button_img_individual.setCursor(Cursor.HAND);
            button_img_individual.setGraphic(iv_button_img_individual);
            button_img_individual.setOpacity(0.5);
            tooltip_button_img_individual = new Tooltip(this.language.getString("image_individual_activate_btn_tooltip"));
            button_img_individual.setTooltip(tooltip_button_img_individual);

            javafx.scene.image.Image cursor_image = new javafx.scene.image.Image("resources/icons/locator2.png");
            imgCursor = new ImageCursor(cursor_image, 16, 16);

            if (visible_individual) {
                button_img_individual.setOnMouseClicked((MouseEvent t) -> {
                    setImageIndividualStatus(true);
                });

                button_img_individual.setOnMouseEntered((MouseEvent t) -> {
                    button_img_individual.setOpacity(1.0);
                });

                button_img_individual.setOnMouseExited((MouseEvent t) -> {
                    if (!cursorOn) {
                        button_img_individual.setOpacity(0.5);
                    }
                });
            }

            button_img_individual.setVisible(visible_individual);

            checkbox_toggle_img_individual = new CheckBox(language.getString("image_individual_toggle_btn_label"));
            checkbox_toggle_img_individual.setSelected(display_img_individual_checked);
            checkbox_toggle_img_individual.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (visible_individual && imageIndividualSet.size() > 0) {

                        display_img_individual_checked = newValue;

                        for (ImageIndividual imageIndividual : imageIndividualSet) {
                            ImageView iv = imgIndividual2ImgView.get(imageIndividual);
                            if (iv != null) {
                                iv.setVisible(display_img_individual_checked);
                            }

                        }
                    }
                }
            });
            checkbox_toggle_img_individual.setVisible(false);

            stackPane.getChildren().addAll(scroll, button_plus, button_minus, button_actual_size, button_img_individual, checkbox_toggle_img_individual);

            StackPane.setAlignment(checkbox_toggle_img_individual, Pos.TOP_LEFT);

            StackPane.setAlignment(button_plus, Pos.TOP_LEFT);
            button_plus.setTranslateX(10);
            button_plus.setTranslateY(10);

            StackPane.setAlignment(button_minus, Pos.TOP_LEFT);
            button_minus.setTranslateX(45);
            button_minus.setTranslateY(10);

            StackPane.setAlignment(button_actual_size, Pos.TOP_LEFT);
            button_actual_size.setTranslateX(80);
            button_actual_size.setTranslateY(10);

            StackPane.setAlignment(button_img_individual, Pos.TOP_LEFT);
            button_img_individual.setTranslateX(115);
            button_img_individual.setTranslateY(10);

            checkbox_toggle_img_individual.setTranslateY(-23);

            //Event for zoom in button
            button_plus.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    imgView.fitWidthProperty().unbind();
                    if (imgView.getFitWidth() <= 10000) {
                        imgView.setFitWidth(imgView.getFitWidth() * zoomNumber);
                    }
                    event.consume();
                }
            });

            //Event for zoom out button
            button_minus.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    imgView.fitWidthProperty().unbind();
                    if (imgView.getFitWidth() >= scroll.getWidth()) {
                        imgView.setFitWidth(imgView.getFitWidth() / zoomNumber);
                    }
                    event.consume();
                }
            });

            //Event for zoom to actual size or fit width screen
            button_actual_size.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    isFitWidth = !isFitWidth;

                    if (isFitWidth) {
                        setImageFitWidthWindow();
                        iv_actual_size.setImage(new javafx.scene.image.Image("resources/icons/img_actual_size.png"));
                        tooltip_actual_size = new Tooltip(language.getString("project_imageview_pane_tooltip_actual_size"));
                        button_actual_size.setTooltip(tooltip_actual_size);
                    } else {
                        imgView.fitWidthProperty().unbind();
                        imgView.setFitWidth(ori_width);
                        iv_actual_size.setImage(new javafx.scene.image.Image("resources/icons/img_fit_width.png"));
                        tooltip_fit_width = new Tooltip(language.getString("project_imageview_pane_tooltip_fit_width_size"));
                        button_actual_size.setTooltip(tooltip_fit_width);
                    }

                    event.consume();
                }
            });

            if (visible_individual) {
                imgView.fitWidthProperty().addListener(observable -> updateOverlayImageIndividuals());
            }

            DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

            //Listener for zoom to full screen or fit width screen            
            zoomProperty.addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable arg0) {
                    imgView.fitWidthProperty().unbind();
                    imgView.setFitWidth(zoomProperty.get() * 4);
                }
            });

            scroll.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {

                    if (imgView.getFitWidth() > 10000) {
                        imgView.setFitWidth(10000);
                    } else if (event.getDeltaY() > 0) {
                        zoomProperty.set(zoomProperty.get() * zoomNumber);
                    } else if (imgView.getFitWidth() > scroll.getWidth()) {
                        zoomProperty.set(zoomProperty.get() / zoomNumber);
                    } else {
                        setImageFitWidthWindow();
                    }

                    event.consume();
                }
            });

            Set<ImageSpecies> imageSpecieses = this.image.getImageSpecieses();

            for (ImageSpecies imageSpecies : imageSpecieses) {
                TreeSet<ImageIndividual> imageIndividuals = new TreeSet<>(new ImageIndividualComparator());
                imageIndividuals.addAll(imageSpecies.getImageIndividuals());

                for (ImageIndividual imageIndividual : imageIndividuals) {
                    overlayImageIndividual(imageIndividual);
                    imageIndividualSet.add(imageIndividual);
                }
            }

            if (popup_window != null) {
                sequence = image.getImageSequence();
                Set<Image> images = sequence.getImages();
                imgList = new ArrayList<>(images);
                Collections.sort(imgList, new ImageComparator());

                ImageView iv_next = new ImageView();
                iv_next.setImage(new javafx.scene.image.Image("resources/icons/img_next.png"));
                button_next = new Button();
                button_next.setStyle(BTN_TRANSPARENT_STYLE);
                button_next.setCursor(Cursor.HAND);
                button_next.setGraphic(iv_next);
                button_next.setOpacity(0.5);
                tooltip_next = new Tooltip(language.getString("project_imageview_pane_tooltip_next"));
                button_next.setTooltip(tooltip_next);
                button_next.setOnMouseEntered((MouseEvent t) -> {
                    button_next.setOpacity(1);
                });
                button_next.setOnMouseExited((MouseEvent t) -> {
                    button_next.setOpacity(0.5);
                });

                ImageView iv_prev = new ImageView();
                iv_prev.setImage(new javafx.scene.image.Image("resources/icons/img_prev.png"));
                button_prev = new Button();
                button_prev.setStyle(BTN_TRANSPARENT_STYLE);
                button_prev.setCursor(Cursor.HAND);
                button_prev.setGraphic(iv_prev);
                button_prev.setOpacity(0.5);
                tooltip_prev = new Tooltip(language.getString("project_imageview_pane_tooltip_prev"));
                button_prev.setTooltip(tooltip_prev);
                button_prev.setOnMouseEntered((MouseEvent t) -> {
                    button_prev.setOpacity(1);
                });
                button_prev.setOnMouseExited((MouseEvent t) -> {
                    button_prev.setOpacity(0.5);
                });

                stackPane.getChildren().add(button_next);
                StackPane.setAlignment(button_next, Pos.CENTER_RIGHT);

                stackPane.getChildren().add(button_prev);
                StackPane.setAlignment(button_prev, Pos.CENTER_LEFT);

                current_index = imgList.indexOf(image);

                if (current_index == 0) {
                    button_prev.setOpacity(0.3);
                    button_prev.setDisable(true);
                }

                if (current_index == imgList.size() - 1) {
                    button_next.setOpacity(0.3);
                    button_next.setDisable(true);
                }

                button_next.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        imgView.setImage(getNextImage(current_index));
                        setImageFitWidthWindow();
                    }
                });

                button_prev.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        imgView.setImage(getPrevImage(current_index));
                        setImageFitWidthWindow();
                    }
                });
            }

            this.getChildren().add(stackPane);
        } catch (FileNotFoundException ex) {
            log.info(ex.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                log.info(ex.getMessage());
            }
        }

    }

    public final void overlayImageIndividual(ImageIndividual imageIndividual) {
        if (visible_individual) {
            count_ii++;
            String img_name = (count_ii < 10) ? "locator_" + count_ii + ".png" : "locator_0.png";
            ImageView iv = new ImageView(new javafx.scene.image.Image("resources/icons/" + img_name));
            imgIndividual2ImgView.put(imageIndividual, iv);

            imgStackPane.getChildren().add(iv);
            imgStackPane.setAlignment(Pos.TOP_LEFT);

            iv.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    viewUpdateImageIndividual(imageIndividual, event.getScreenX(), event.getScreenY());
                }
            });

            iv.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    iv.setCursor(Cursor.HAND);
                }
            });

        }
    }

    public void updateOverlayImageIndividuals() {
        if (visible_individual && imageIndividualSet.size() > 0) {
            checkbox_toggle_img_individual.setVisible(true);

            for (ImageIndividual imageIndividual : imageIndividualSet) {
                ImageView iv = imgIndividual2ImgView.get(imageIndividual);
                if (iv != null) {
                    DoubleProperty translateXProperty = iv.translateXProperty();
                    translateXProperty.set(((double) imageIndividual.getX()) * ratio_binding.get() - 12);

                    DoubleProperty translateYProperty = iv.translateYProperty();
                    translateYProperty.set(((double) imageIndividual.getY()) * ratio_binding.get() - 12);

                    iv.setVisible(display_img_individual_checked);
                }
            }
        }
    }

    public void removeOverlayImageIndividual(ImageIndividual imageIndividual) {
        if (visible_individual) {
            imageIndividualSet.remove(imageIndividual);
            imgStackPane.getChildren().remove(imgIndividual2ImgView.get(imageIndividual));

            if (imageIndividualSet.isEmpty()) {
                checkbox_toggle_img_individual.setVisible(false);
            }
        }
    }

    public void viewUpdateImageIndividual(ImageIndividual imageIndividual, double screenX, double screenY) {
        if (visible_individual) {
            controller.viewUpdateImageIndividual(this, image, imageIndividual, screenX, screenY);
        }
    }

    public void addOverlayImageIndividual(ImageIndividual imageIndividual) {
        if (visible_individual) {
            imageIndividualSet.add(imageIndividual);
            overlayImageIndividual(imageIndividual);
            updateOverlayImageIndividuals();
        }
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;

        if (tooltip_zoomin != null) {
            tooltip_zoomin.setText(this.language.getString("project_imageview_pane_tooltip_zoomin"));
        }
        if (tooltip_zoomout != null) {
            tooltip_zoomout.setText(this.language.getString("project_imageview_pane_tooltip_zoomout"));
        }
        if (tooltip_actual_size != null) {
            tooltip_actual_size.setText(this.language.getString("project_imageview_pane_tooltip_actual_size"));
        }
        if (tooltip_fit_width != null) {
            tooltip_actual_size.setText(this.language.getString("project_imageview_pane_tooltip_fit_width_size"));
        }
        if (tooltip_next != null) {
            tooltip_next.setText(this.language.getString("project_imageview_pane_tooltip_next"));
        }
        if (tooltip_prev != null) {
            tooltip_prev.setText(this.language.getString("project_imageview_pane_tooltip_prev"));
        }
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    public javafx.scene.image.Image getNextImage(int index) {
        javafx.scene.image.Image imagefx = null;
        button_next.setDisable(false);
        button_next.setOpacity(1);

        button_prev.setDisable(false);
        button_prev.setOpacity(0.5);

        if (index < imgList.size() - 1) {
            try {
                Image img = imgList.get(index + 1);
                InputStream is = new FileInputStream(ImageRepository.getFile(img));
                imagefx = new javafx.scene.image.Image(is, -1, -1, true, true);
                current_index = index + 1;
                popup_window.setTitle(img.getRawName());
            } catch (FileNotFoundException ex) {
                log.info(ex.getMessage());
            }
        } else {
            current_index = imgList.size() - 1;
        }

        if (index == imgList.size() - 2) {
            button_next.setDisable(true);
            button_next.setOpacity(0.3);
        }

        return imagefx;
    }

    public javafx.scene.image.Image getPrevImage(int index) {
        javafx.scene.image.Image imagefx = null;
        button_next.setDisable(false);
        button_next.setOpacity(0.5);

        button_prev.setDisable(false);
        button_prev.setOpacity(1);

        if (index > 0) {
            try {
                Image img = imgList.get(index - 1);
                InputStream is = new FileInputStream(ImageRepository.getFile(img));
                imagefx = new javafx.scene.image.Image(is, -1, -1, true, true);
                current_index = index - 1;
                popup_window.setTitle(img.getRawName());
            } catch (FileNotFoundException ex) {
                log.info(ex.getMessage());
            }
        } else {
            current_index = 0;
        }

        if (index == 1) {
            button_prev.setDisable(true);
            button_prev.setOpacity(0.3);
        }
        return imagefx;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.controller = controller;
    }

    public WildIDController getWildIDController() {
        return this.controller;
    }

    public Image getImage() {
        return this.image;
    }

    private void setImageFitWidthWindow() {
        imgView.fitWidthProperty().bind(scroll.widthProperty().subtract(2));
    }

    private void setImageIndividualStatus(boolean isOn) {
        cursorOn = isOn;
        if (isOn) {
            imgView.setCursor(imgCursor);
            button_img_individual.setCursor(imgCursor);
            button_img_individual.setOpacity(1.0);
        } else {
            imgView.setCursor(Cursor.DEFAULT);
            button_img_individual.setCursor(Cursor.HAND);
            button_img_individual.setOpacity(0.5);
        }
    }
}
