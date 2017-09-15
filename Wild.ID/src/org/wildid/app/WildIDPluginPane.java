package org.wildid.app;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wildid.app.plugin.Plugin;
import org.wildid.app.plugin.ServerPlugin;
import static org.wildid.app.WildIDDataPane.TITLE_STYLE;

public class WildIDPluginPane extends WildIDDataPane implements LanguageChangable {

    WildIDController controller;
    protected LanguageModel language;
    private final Label titleLabel;
    private final int width = 600;
    public VBox vbox;
    static Logger log = Logger.getLogger(WildIDPluginPane.class.getName());
    private List<ServerPlugin> pluginObjects;
    private final WildIDMenuBar menuBar;

    public WildIDPluginPane(WildIDController controller, LanguageModel language, WildIDMenuBar menuBar) throws Exception {
        this.controller = controller;
        this.language = language;
        this.menuBar = menuBar;
        this.setId("WildIDPlugin");

        titleLabel = new Label(language.getString("menu_plugin_managePlugins"));
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
        //grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        List<ServerPlugin> serverPlugins = getServerPluginList();

        Set<Plugin> installedPlugins = this.menuBar.getInstalledPlugins();

        int i = 0;
        String min_wildid_version = "0.9.14";
        boolean valid_min_wildid_version = WildID.VERSION.equals(min_wildid_version) || Util.isNewerVersion(min_wildid_version, WildID.VERSION);

        for (ServerPlugin serverPlugin : serverPlugins) {

            HBox hbox_plugin = new HBox(20);
            VBox vbox_plugin = new VBox(5);

            String serverPluginName = serverPlugin.getPluginName();
            String serverPluginVersion = serverPlugin.getCurrentVersion();
            String serverPluginDescription = serverPlugin.getDescription();
            String serverPluginLogo = serverPlugin.getLogo();
            String required_wildid_version = serverPlugin.getRequiredWildIDVersion();
            boolean multiLanguages = serverPlugin.getMultiLanguages();
            boolean valid_required_wildid_version = WildID.VERSION.equals(required_wildid_version) || Util.isNewerVersion(required_wildid_version, WildID.VERSION);

            Text plugin_name_text = new Text(serverPluginName);
            plugin_name_text.setStyle("-fx-font-weight: bold; -fx-font-size:14px; -fx-font-family: Verdana;");
            Text latest_version_text = new Text(language.getString("plugin_latest_version") + ": " + serverPluginVersion);
            Text description_text = new Text(language.getString("plugin_description") + ": " + serverPluginDescription);
            description_text.wrappingWidthProperty().bind(this.widthProperty().subtract(250));

            Text multi_languages_text = new Text(language.getString("plugin_multi_languages")
                    + ": " + (language.getString(multiLanguages ? "plugin_multi_languages_true" : "plugin_multi_languages_false")));

            Text required_wildid_version_text = new Text(language.getString("plugin_required_wildid_version")
                    + ": " + required_wildid_version);

            boolean canUpdatePlugin = false;
            boolean installed = false;
            String installedPluginVersion = null;

            for (Plugin installedPlugin : installedPlugins) {
                if (installedPlugin.getPluginName() != null
                        && installedPlugin.getPluginName().equals(serverPluginName)) {
                    installed = true;

                    installedPluginVersion = installedPlugin.getPluginVersion();
                    canUpdatePlugin = Util.isNewerVersion(installedPluginVersion, serverPluginVersion);

                    break;
                }
            }

            Button btn_install = new Button(language.getString("plugin_install_button"));
            Button btn_uninstall = new Button(language.getString("plugin_remove_button"));
            Button btn_update = new Button(language.getString("plugin_update_button"));

            HBox hbox_btns = new HBox(10);
            hbox_btns.setAlignment(Pos.CENTER);
            hbox_btns.setPrefWidth(width - 250);
            hbox_btns.setMaxWidth(width - 250);

            Text local_version_text = null;

            if (installed) {
                local_version_text = new Text(language.getString("plugin_your_version") + ": " + installedPluginVersion);

                if (canUpdatePlugin && valid_required_wildid_version) {
                    hbox_btns.getChildren().add(btn_update);
                    btn_update.setId("plugin_update_" + serverPluginName);
                    btn_update.setOnAction(controller);
                }

                hbox_btns.getChildren().add(btn_uninstall);
                btn_uninstall.setId("plugin_uninstall_" + serverPluginName);
                btn_uninstall.setOnAction(controller);
            } else if(valid_required_wildid_version){
                hbox_btns.getChildren().add(btn_install);
                btn_install.setId("plugin_install_" + serverPluginName);
                btn_install.setOnAction(controller);
            }

            vbox_plugin.getChildren().addAll(
                    plugin_name_text,
                    new Text(""),
                    description_text,
                    multi_languages_text,
                    required_wildid_version_text,
                    latest_version_text);

            if (local_version_text != null) {
                vbox_plugin.getChildren().add(local_version_text);
            }

            vbox_plugin.getChildren().addAll(new Text(""), hbox_btns);

            hbox_plugin.getChildren().addAll(
                    new ImageView(new Image("resources/icons/" + serverPluginLogo)),
                    vbox_plugin);

            hbox_plugin.setStyle(GRAY_DIV);

            grid.add(hbox_plugin, 1, i++);
        }

        if (!valid_min_wildid_version) {
            Text required_version = new Text(language.getString("plugin_required_version"));
            required_version.setStyle(ALERT_WARNING_TEXT + " -fx-font-weight: bold; -fx-font-size:14px;");
            required_version.setUnderline(true);

            Text required_version_detail = new Text(
                    language.getString("plugin_required_minimum_version") + " Wild.ID " + min_wildid_version
                    + "\n" + language.getString("plugin_your_version") + ": Wild.ID " + WildID.VERSION
                    + "\n" + language.getString("plugin_required_version_msg"));

            required_version_detail.setStyle(ALERT_WARNING_TEXT);
            required_version_detail.wrappingWidthProperty().bind(this.widthProperty().subtract(120));
            required_version_detail.setTranslateX(20);

            VBox required_version_box = new VBox();

            required_version_box.setStyle(ALERT_WARNING_DIV);

            required_version_box.getChildren().addAll(required_version, new Text(""), required_version_detail);

            grid.add(required_version_box, 1, i++);
        }

        return grid;
    }

    public List<ServerPlugin> getServerPluginList() {
        if (pluginObjects != null) {
            return pluginObjects;
        }

        List<ServerPlugin> plugin_objects = new ArrayList<>();

        try {
            String pluginUrlStr = WildID.WILDID_SERVER + "service.jsp?q=allplugins";
            URL url = new URL(pluginUrlStr);

            // read from the URL
            Scanner scan = new Scanner(url.openStream());
            String jsonData = new String();
            while (scan.hasNext()) {
                jsonData += scan.nextLine();
            }
            scan.close();
            // build a JSON object
            JSONObject obj = new JSONObject(jsonData);

            JSONArray jsArray = obj.getJSONArray("plugins");

            for (int i = 0; i < jsArray.length(); i++) {
                JSONObject jsonObj = jsArray.getJSONObject(i);

                ServerPlugin plugin_object = new ServerPlugin();
                plugin_object.setPluginId(jsonObj.getInt("plugin_id"));
                plugin_object.setPluginName(jsonObj.getString("plugin_name"));
                plugin_object.setCurrentVersion(jsonObj.getString("current_version"));
                plugin_object.setJarFile(jsonObj.getString("jar_file"));
                plugin_object.setDescription(jsonObj.getString("description"));
                plugin_object.setLogo(jsonObj.getString("logo"));
                plugin_object.setMultiLanguages(jsonObj.getBoolean("multi_languages"));
                plugin_object.setRequiredWildIDVersion(jsonObj.getString("requires_wildid_version"));

                plugin_objects.add(plugin_object);
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }

        return plugin_objects;
    }

    /**
     *
     * @param language
     */
    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        titleLabel.setText(language.getString("menu_plugin_managePlugins"));
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.controller = controller;
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    public WildIDMenuBar getWildIDMenuBar() {
        return this.menuBar;
    }
}
