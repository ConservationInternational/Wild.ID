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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wildid.entity.VersionUpdate;
import static org.wildid.app.WildIDDataPane.TITLE_STYLE;

public class WildIDUpdatePane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    private final Label titleLabel;
    private final int width = 600;
    private WildIDController controller;
    private Text updateText;
    private Label currentVersionLabel;
    private Label latestVersionLabel;
    private String latestVersion;
    private String upgradableVersion;
    private boolean isNew = false;
    private boolean upgradable = false;
    private Button updateButton;
    private String uuid;
    public VBox vbox;
    static Logger log = Logger.getLogger(WildIDUpdatePane.class.getName());
    private ScrollPane whatsNewPane;
    private List<VersionUpdate> versionUpdates;

    public WildIDUpdatePane(WildIDController controller, LanguageModel language) throws Exception {
        this.language = language;
        this.controller = controller;

        titleLabel = new Label(language.getString("menu_help_checkUpdate"));
        titleLabel.setStyle(TITLE_STYLE);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 10));
        titleBox.getChildren().addAll(this.titleLabel);

        this.vbox = new VBox(0);
        this.vbox.setAlignment(Pos.TOP_CENTER);

        this.vbox.getChildren().addAll(titleBox, createForm());
        this.getChildren().add(this.vbox);

        this.vbox.setMinWidth(width);
        this.vbox.setMinHeight(width);

        this.vbox.prefWidthProperty().bind(this.widthProperty());
        this.vbox.prefHeightProperty().bind(this.heightProperty());
    }

    public final Pane createForm() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(30, 10, 10, 30));
        grid.setHgap(20);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        uuid = WildID.wildIDProperties.getUUID();

        // Safety check
        if (uuid == null || uuid.trim().equals("")) {
            uuid = Util.generateUUID();
            Util.updateWildIDProperties("uuid", uuid);
            WildID.wildIDProperties.setUUID(uuid);
        }

        boolean isError = false;
        updateText = new Text();
        currentVersionLabel = new Label(language.getString("wildID_updatePane_current_version") + ":");
        latestVersionLabel = new Label(language.getString("wildID_updatePane_latest_version") + ":");

        grid.add(currentVersionLabel, 1, 0);
        grid.add(new Label("Wild.ID " + WildID.VERSION), 2, 0);

        grid.add(latestVersionLabel, 1, 1);
        latestVersion = getLatestWildIdVersion();

        if (latestVersion == null) {
            isError = true;
            grid.add(new Label("N/A"), 2, 1);
            updateText.setText(language.getString("wildID_updatePane_latestVersion_not_available"));
        } else {
            grid.add(new Label("Wild.ID " + latestVersion), 2, 1);
        }

        isNew = Util.isNewerVersion(WildID.VERSION, latestVersion);

        if (isNew) {
            versionUpdates = getVersionUpdateList();
            if (versionUpdates != null && versionUpdates.size() > 0) {
                upgradable = true;
                upgradableVersion = versionUpdates.get(versionUpdates.size() - 1).getNewVersion();
            }
        }

        grid.add(updateText, 1, 2, 2, 1);

        if (!isError) {
            if (isNew) {
                if (upgradable) {
                    updateText.setText(language.getString("wildID_updatePane_update_available") + " Wild.ID " + upgradableVersion);
                    updateButton = new Button(language.getString("wildID_updatePane_update_button"));
                    updateButton.setId("update_wild_id");

                    HBox hbBtn = new HBox(10);
                    hbBtn.setAlignment(Pos.BOTTOM_CENTER);
                    hbBtn.getChildren().add(updateButton);
                    updateButton.setOnAction(controller);
                    grid.add(hbBtn, 1, 3);
                } else {
                    updateText.setText(language.getString("wildID_updatePane_not_upgradable"));
                }
            } else {
                updateText.setText(language.getString("wildID_updatePane_up_to_date"));
            }
        }

        Hyperlink whatsNewLink = new Hyperlink(language.getString("wildID_updatePane_whatIsNew"));
        whatsNewLink.setVisited(true);
        whatsNewLink.setOnAction((ActionEvent event) -> {
            if (whatsNewPane == null) {
                whatsNewPane = createWhatsNewForm();
                this.vbox.getChildren().add(whatsNewPane);
            }
        });

        grid.add(whatsNewLink, 1, 4);
        return grid;
    }

    public ScrollPane createWhatsNewForm() {

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(0, 10, 10, 30));
        grid.setHgap(20);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        ScrollPane scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Horizontal scroll bar
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scroll bar
        scroll.setStyle("-fx-background-color:transparent; ");
        scroll.setContent(grid);
        boolean success = true;

        try {
            String updateUrlStr = WildID.WILDID_SERVER + "service.jsp?q=allUpdates";
            URL url = new URL(updateUrlStr);

            // read from the URL
            Scanner scan = new Scanner(url.openStream());
            String jsonData = new String();
            while (scan.hasNext()) {
                jsonData += scan.nextLine();
            }
            scan.close();
            // build a JSON object
            JSONObject obj = new JSONObject(jsonData);

            JSONArray jsArray = obj.getJSONArray("updates");

            // now 10%
            ArrayList<VersionUpdate> logVersionUpdates = new ArrayList<>();

            for (int i = 0; i < jsArray.length(); i++) {
                // Just list last 10 update
                if (i >= 10) {
                    break;
                }

                JSONObject updateObj = jsArray.getJSONObject(i);
                String new_version = updateObj.getString("new_version");
                String old_version = updateObj.getString("old_version");

                VersionUpdate versionUpdate = new VersionUpdate();
                versionUpdate.setNewVersion(new_version);
                versionUpdate.setOldVersion(old_version);

                if (!updateObj.isNull("log_file")) {
                    String log_file = updateObj.getString("log_file");
                    if (log_file.endsWith(".log")) {
                        versionUpdate.setLogFile(log_file);
                    }
                }

                logVersionUpdates.add(versionUpdate);
            }

            int rowIndex = 0;
            for (VersionUpdate logVersionUpdate : logVersionUpdates) {
                // read from the URL
                String updateLog = "";

                try {
                    URL logUrl = new URL(WildID.WILDID_SERVER + "versions/" + logVersionUpdate.getNewVersion() + "/" + logVersionUpdate.getLogFile());

                    Scanner log_scan = new Scanner(logUrl.openStream());
                    while (log_scan.hasNext()) {
                        updateLog += log_scan.nextLine() + "\n";
                    }
                    scan.close();
                } catch (Exception ex) {
                    log.info("Exception: " + ex.toString());
                }

                TextArea ta = new TextArea(updateLog);
                ta.setWrapText(true);
                ta.setEditable(false);
                TitledPane tp = new TitledPane(logVersionUpdate.getNewVersion(), ta);
                tp.setExpanded(false);
                grid.add(tp, 1, rowIndex++);
            }
        } catch (MalformedURLException ex) {
            log.info("MalformedURLException: " + ex.toString());
            success = false;
        } catch (IOException ex) {
            log.info("IOException: " + ex.toString());
            success = false;
        } catch (JSONException ex) {
            log.info("JSONException: " + ex.toString());
            success = false;
        }

        if (!success) {
            Text errorText = new Text(language.getString("wildID_updatePane_unable_to_update"));
            grid.add(errorText, 1, 1);
        }

        return scroll;
    }

    public List<VersionUpdate> getVersionUpdateList() {
        if (versionUpdates != null) {
            return versionUpdates;
        }

        List<VersionUpdate> version_updates = new ArrayList<>();

        try {
            String updateUrlStr = WildID.WILDID_SERVER + "service.jsp?q=updates&local_version=" + WildID.VERSION;
            URL url = new URL(updateUrlStr);

            // read from the URL
            Scanner scan = new Scanner(url.openStream());
            String jsonData = new String();
            while (scan.hasNext()) {
                jsonData += scan.nextLine();
            }
            scan.close();
            // build a JSON object
            JSONObject obj = new JSONObject(jsonData);

            JSONArray jsArray = obj.getJSONArray("updates");

            for (int i = 0; i < jsArray.length(); i++) {
                JSONObject updateObj = jsArray.getJSONObject(i);
                String new_version = updateObj.getString("new_version");
                String old_version = updateObj.getString("old_version");

                VersionUpdate versionUpdate = new VersionUpdate();
                versionUpdate.setNewVersion(new_version);
                versionUpdate.setOldVersion(old_version);

                if (!updateObj.isNull("sql_file")) {
                    String sql_file = updateObj.getString("sql_file");
                    if (sql_file.endsWith(".sql")) {
                        versionUpdate.setSqlFile(sql_file);
                    }
                }

                if (!updateObj.isNull("jar_file")) {
                    String jar_file = updateObj.getString("jar_file");
                    if (jar_file.endsWith(".jar")) {
                        versionUpdate.setJarFile(jar_file);
                    }
                }

                if (!updateObj.isNull("compressed_lib_file")) {
                    String compressed_lib_file = updateObj.getString("compressed_lib_file");
                    if (compressed_lib_file.endsWith(".zip") || compressed_lib_file.endsWith(".jar")) {
                        versionUpdate.setLibFile(compressed_lib_file);
                    }
                }

                boolean can_upgrade = updateObj.getBoolean("can_upgrade");
                versionUpdate.setCanUpgrade(can_upgrade);

                if (can_upgrade) {
                    version_updates.add(versionUpdate);
                } else {
                    break;
                }
            }
        } catch (MalformedURLException ex) {
            log.info(ex.getMessage());
        } catch (JSONException ex) {
            log.info(ex.getMessage());
        } catch (IOException ex) {
            log.info(ex.getMessage());
        }

        return version_updates;
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        titleLabel.setText(language.getString("menu_help_checkUpdate"));

        currentVersionLabel.setText(language.getString("wildID_updatePane_current_version"));
        latestVersionLabel.setText(language.getString("wildID_updatePane_latest_version"));

        if (isNew) {
            if (upgradable) {
                updateText.setText(language.getString("wildID_updatePane_update_available") + " Wild.ID " + upgradableVersion);
            } else {
                updateText.setText(language.getString("wildID_updatePane_not_upgradable"));
            }
        } else {
            updateText.setText(language.getString("wildID_updatePane_up_to_date"));
        }

        updateButton.setText(language.getString("wildID_updatePane_update_button"));
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.controller = controller;
        this.updateButton.setOnAction(controller);
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getCurrentVersion() {
        return WildID.VERSION;
    }

    public String getLatestVersion() {
        return this.latestVersion;
    }

    public static String getLatestWildIdVersion() {
        String version = null;
        try {
            String urlStr = WildID.WILDID_SERVER + "service.jsp?q=version";
            URL url = new URL(urlStr);

            // read from the URL
            Scanner scan = new Scanner(url.openStream());
            String jsonData = new String();
            while (scan.hasNext()) {
                jsonData += scan.nextLine();
            }
            scan.close();

            // build a JSON object
            JSONObject obj = new JSONObject(jsonData);

            if (!obj.isNull("version")) {
                version = obj.getString("version");
            }

        } catch (MalformedURLException ex) {
            log.info("MalformedURLException:" + ex.getMessage());
        } catch (IOException ex) {
            log.info("IOException:" + ex.getMessage());
        } catch (JSONException ex) {
            log.info("JSONException:" + ex.getMessage());
        }

        return version;
    }

    public void viewUpdatePane() {
        this.vbox.getChildren().remove(1);
        this.vbox.getChildren().add(1, this.createForm());
    }

    public LanguageModel getLanguage() {
        return this.language;
    }
}
