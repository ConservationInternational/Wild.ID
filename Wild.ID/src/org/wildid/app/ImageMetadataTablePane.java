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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Event;
import org.wildid.entity.Image;
import org.wildid.entity.ImageRepository;
import org.wildid.entity.Project;
import org.wildid.service.ImageService;
import org.wildid.service.ImageServiceImpl;

/**
 *
 * @author MinhPhan
 */
public class ImageMetadataTablePane extends WildIDDataPane implements LanguageChangable {

    protected Label titleLabel;
    protected LanguageModel language;
    private final Project project;
    private final Event event;
    private final CameraTrapArray ctArray;
    private final CameraTrap cameraTrap;
    private static Stage dialog;
    private Stage undockWindow = null;
    public static final int DEFAULT_ROWS_PER_PAGE = 200;
    private Pagination pagination = null;
    private final ImageService imageService;
    private final String baseQuery;
    private int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
    private int totalPages;
    private int totalImages;
    private final Label totalLabel;
    private final Label rowsPerPageLabel;
    private final WildIDImageTree imageTree;
    private Hyperlink undockLink = null;
    private ImageMetadataTablePane newTablePane;
    private boolean newWindow = false;
    static Logger log = Logger.getLogger(ImageMetadataTablePane.class.getName());

    public ImageMetadataTablePane(
            LanguageModel language,
            Project project,
            Event event,
            CameraTrapArray ctArray,
            CameraTrap cameraTrap,
            WildIDImageTree imageTree
    ) {
        this(language, project, event, ctArray, cameraTrap, imageTree, false);
    }

    public ImageMetadataTablePane(
            LanguageModel language,
            Project project,
            Event event,
            CameraTrapArray ctArray,
            CameraTrap cameraTrap,
            WildIDImageTree imageTree,
            boolean newWindow
    ) {
        this.language = language;
        this.project = project;
        this.event = event;
        this.ctArray = ctArray;
        this.cameraTrap = cameraTrap;
        this.imageTree = imageTree;
        this.newWindow = newWindow;

        //this.setStyle(bgcolorStyle);
        String title = language.getString("image_metadata_table_pane_title") + " ";
        if (event == null) {
            title += language.getString("image_metadata_table_pane_type_project") + ": " + project.getName();
        } else if (ctArray == null) {
            title += language.getString("image_metadata_table_pane_type_event") + ": " + event.getName();
        } else if (cameraTrap == null) {
            title += language.getString("image_metadata_table_pane_type_ctArray") + ": " + ctArray.getName();
        } else {
            title += language.getString("image_metadata_table_pane_type_deployment") + ": " + cameraTrap.getName();
        }

        titleLabel = new Label(title);
        titleLabel.setStyle(TITLE_STYLE);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);

        imageService = new ImageServiceImpl();
        ExportImageMetadata exportImageMetadata = new ExportImageMetadata(null, project, event, ctArray, cameraTrap);
        totalImages = imageService.getImageMetadataExportCount(exportImageMetadata.getTotalCountQuery());
        totalPages = (totalImages / rowsPerPage) + (totalImages % rowsPerPage == 0 ? 0 : 1);

        ObservableList<Integer> options = FXCollections.observableArrayList(25, 50, 100, 200, 300, 400, 500, 1000);

        final ComboBox rowPerPageComboBox = new ComboBox(options);
        rowPerPageComboBox.setValue(DEFAULT_ROWS_PER_PAGE);

        rowPerPageComboBox.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue o, Number oldVal, Number newVal) {
                rowsPerPage = newVal.intValue();
                totalPages = (totalImages / rowsPerPage) + (totalImages % rowsPerPage == 0 ? 0 : 1);
                pagination.setPageCount(totalPages);
            }
        });

        HBox rowPerPageHBox = new HBox(10);
        rowsPerPageLabel = new Label(language.getString("image_metadata_table_pane_rows_per_page"));
        rowPerPageHBox.getChildren().addAll(rowsPerPageLabel, rowPerPageComboBox);

        totalLabel = new Label(language.getString("image_metadata_table_pane_total_images") + totalImages);
        baseQuery = exportImageMetadata.getExportImageMetadataQuery();

        pagination = new Pagination(totalPages, 0);
        pagination.setPageFactory((Integer pageIndex) -> createForm(pageIndex));

        AnchorPane anchor = new AnchorPane(pagination, rowPerPageHBox, totalLabel);
        anchor.setMinWidth(500);

        AnchorPane.setTopAnchor(pagination, 0.0);
        AnchorPane.setRightAnchor(pagination, 0.0);
        AnchorPane.setBottomAnchor(pagination, 0.0);
        AnchorPane.setLeftAnchor(pagination, 0.0);

        AnchorPane.setBottomAnchor(rowPerPageHBox, 10.0);
        AnchorPane.setLeftAnchor(rowPerPageHBox, 30.0);

        AnchorPane.setBottomAnchor(totalLabel, 10.0);
        AnchorPane.setRightAnchor(totalLabel, 30.0);

        if (!newWindow) {
            javafx.scene.image.Image undockImg = new javafx.scene.image.Image("resources/icons/application_double.png");
            undockLink = new Hyperlink(this.language.getString("image_metadata_table_pane_open_new_window"));
            undockLink.setGraphic(new ImageView(undockImg));
            undockLink.setCursor(Cursor.HAND);
            undockLink.setPadding(new Insets(10, 20, 10, 20));

            undockLink.setOnAction(actionEvent -> {
                if (undockWindow == null || !undockWindow.isShowing()) {
                    undockWindow = new Stage();
                    undockWindow.getIcons().add(new javafx.scene.image.Image("resources/icons/wildId32.png"));
                    undockWindow.setTitle("Wild.ID");

                    newTablePane = new ImageMetadataTablePane(
                            this.language,
                            project,
                            event,
                            ctArray,
                            cameraTrap,
                            imageTree,
                            true
                    );

                    Scene scene = new Scene(newTablePane);

                    undockWindow.setScene(scene);
                    undockWindow.setWidth(600);
                    undockWindow.setHeight(600);
                } else {
                    if (!undockWindow.isFocused()) {
                        undockWindow.requestFocus();
                    }

                    if (undockWindow.isIconified()) {
                        undockWindow.setIconified(false);
                    }
                    undockWindow.toFront();
                }

                undockWindow.show();
            });

            HBox undockHBox = new HBox(undockLink);
            undockHBox.setAlignment(Pos.CENTER_RIGHT);

            vbox.getChildren().addAll(titleBox, undockHBox, anchor);
        } else {
            vbox.getChildren().addAll(titleBox, anchor);
        }

        vbox.prefWidthProperty().bind(this.widthProperty());
        vbox.prefHeightProperty().bind(this.heightProperty());

        this.getChildren().add(vbox);
    }

    private Pane createForm(int pageIndex) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setHgap(20);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        String[] columnNames = ExportImageMetadata.columnNames;

        int columnSize = columnNames.length;
        TableColumn<String[], String>[] columnDatas = new TableColumn[columnSize];

        String sql = baseQuery;
        if (pageIndex >= 0) {
            int startRowIndex = pageIndex * rowsPerPage;
            sql += (" LIMIT " + startRowIndex + ", " + rowsPerPage);
        }

        List<Object> resultList = imageService.getImageMetadataExport(sql);

        List<String[]> models = new ArrayList<>();

        for (Object rowObj : resultList) {
            Object[] objArr = (Object[]) rowObj;

            String[] objStrArr = new String[objArr.length];
            int i = 0;
            for (Object colObj : objArr) {
                if (colObj == null) {
                    objStrArr[i++] = "";
                } else if (colObj instanceof String) {
                    objStrArr[i++] = (String) colObj;
                } else {
                    objStrArr[i++] = String.valueOf(colObj);
                }
            }
            models.add(objStrArr);
        }

        TableView<String[]> table = new TableView<>();
        table.setEditable(false);
        table.setItems(FXCollections.observableArrayList(models));

        for (int i = 0; i < columnSize; i++) {
            int index = i;
            columnDatas[i] = new TableColumn<>(columnNames[i]);
            columnDatas[i].setCellValueFactory(column -> new SimpleStringProperty((column.getValue()[index])));

            table.getColumns().add(columnDatas[i]);
        }

        columnDatas[0].setMinWidth(50);
        columnDatas[0].setMaxWidth(50);
        columnDatas[0].setPrefWidth(50);
        columnDatas[0].setStyle(INDEX_COLUMN_STYLE);
        columnDatas[0].setComparator((String o1, String o2) -> {
            return Integer.compare(new Integer(o1), new Integer(o2));
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String[] rowData = (String[]) newSelection;
                int imageId = Integer.valueOf(rowData[0]);
                Image img = imageService.getImage(imageId);

                ProjectImageTreeItem imageTreeItem = imageTree.getProjectImageTreeItem(img);

                if (newWindow) {
                    imageTree.getSelectionModel().select(imageTreeItem);
                    imageTree.scrollTo(imageTree.getRow(imageTreeItem) - 11);

                } else {
                    for (Object item : imageTreeItem.getParent().getChildren()) {
                        ((TreeItem) item).setExpanded(false);
                    }
                    for (Object item : imageTreeItem.getParent().getParent().getChildren()) {
                        ((TreeItem) item).setExpanded(false);
                    }
                    for (Object item : imageTreeItem.getParent().getParent().getParent().getChildren()) {
                        ((TreeItem) item).setExpanded(false);
                    }
                    for (Object item : imageTreeItem.getParent().getParent().getParent().getParent().getChildren()) {
                        ((TreeItem) item).setExpanded(false);
                    }

                    imageTreeItem.getParent().getParent().getParent().getParent().setExpanded(true);
                    imageTreeItem.getParent().getParent().getParent().setExpanded(true);
                    imageTreeItem.getParent().getParent().setExpanded(true);
                    imageTreeItem.getParent().setExpanded(true);
                }
            }
        });

        table.setRowFactory(tv -> {
            TableRow<String[]> row = new TableRow<>();
            row.setOnMouseClicked(clickEvent -> {
                if (clickEvent.getClickCount() == 2 && (!row.isEmpty())) {
                    String[] rowData = row.getItem();
                    int imageId = Integer.valueOf(rowData[0]);
                    Image img = imageService.getImage(imageId);
                    try {
                        double default_width = 600;
                        File imageFile = ImageRepository.getFile(img);
                        InputStream is = new FileInputStream(imageFile);
                        Pane gpane = new ProjectImageViewPane(this.language, img, null);
                        javafx.scene.image.Image popImg = new javafx.scene.image.Image(is);

                        if (dialog == null || !dialog.isShowing()) {
                            dialog = new Stage();
                            dialog.setResizable(true);
                            dialog.setMinWidth(200);
                            dialog.setScene(new Scene(gpane, default_width, default_width * popImg.getHeight() / popImg.getWidth()));
                            dialog.show();
                        } else {
                            dialog.getScene().setRoot(gpane);
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
            return row;
        });

        table.prefWidthProperty().bind(this.widthProperty());
        table.prefHeightProperty().bind(this.heightProperty());

        HBox pageBox = new HBox();
        pageBox.setPrefHeight(20);
        pageBox.setMaxHeight(20);
        pageBox.setMinHeight(20);
        pageBox.setPadding(new Insets(0, 20, 0, 0));

        grid.add(table, 0, 0);
        return grid;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;

        String title = language.getString("image_metadata_table_pane_title") + " ";
        if (event == null) {
            title += language.getString("image_metadata_table_pane_type_project") + ": " + project.getName();
        } else if (ctArray == null) {
            title += language.getString("image_metadata_table_pane_type_event") + ": " + event.getName();
        } else if (cameraTrap == null) {
            title += language.getString("image_metadata_table_pane_type_ctArray") + ": " + ctArray.getName();
        } else {
            title += language.getString("image_metadata_table_pane_type_deployment") + ": " + cameraTrap.getName();
        }

        titleLabel.setText(title);

        rowsPerPageLabel.setText(language.getString("image_metadata_table_pane_rows_per_page"));
        totalLabel.setText(language.getString("image_metadata_table_pane_total_images") + totalImages);

        if (undockLink != null) {
            undockLink.setText(language.getString("image_metadata_table_pane_open_new_window"));
        }

        if (newTablePane != null) {
            newTablePane.setLanguage(language);
        }
    }
}
