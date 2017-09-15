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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.reflections.Reflections;
import org.wildid.app.plugin.Plugin;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectComparator;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDMenuBar extends MenuBar implements LanguageChangable {

    private final Menu menuWildID;
    private final MenuItem menuWildID_preferences;
    private final MenuItem menuWildID_about;
    private final MenuItem menuWildID_license;
    private final MenuItem menuWildID_registration;
    private final MenuItem menuWildID_quit;

    private final Menu menuFile;
    private final MenuItem menuFile_newProject;
    private final Menu menuFile_annotateImages;
    private final List<MenuItem> generated_menu_items = new ArrayList<>();
    private final MenuItem menuFile_saveTransferFile;
    private final Menu menuFile_openTransferFile;
    private final MenuItem menuFile_openTransferFile_InNewProject;
    private final MenuItem menuFile_openTransferFile_InProject;

    private final Menu menuEdit;
    private final Menu menuEdit_editProject;
    private final Menu menuEdit_editMasterList;
    private final MenuItem menuEdit_organizations;
    private final MenuItem menuEdit_persons;
    private final MenuItem menuEdit_cameraModels;
    private final MenuItem manuEdit_find;

    private final Menu menuView;
    private final Menu menuView_table;
    private final MenuItem menuView_tableSameWindow;
    private final MenuItem menuView_tableNewWindow;

    private final Menu menuExport;
    private final Menu menuExport_dataImages;
    private final Menu menuExport_toCSV;
    private final MenuItem menuExport_toCSV_withImages;
    private final MenuItem menuExport_toCSV_withoutImages;
    private final Menu menuExport_toExcel;
    private final MenuItem menuExport_toExcel_withImages;
    private final MenuItem menuExport_toExcel_without_images;
    private final Menu menuExport_toWCS;
    private final MenuItem menuExport_toWCS_withImages;
    private final MenuItem menuExport_toWCS_without_images;
    private final List<Project> projectOrderList;

    private final Menu menuPlugin;
    private final MenuItem menuPlugin_managePlugins;

    private final Menu menuHelp;
    private final MenuItem menuHelp_checkUpdate;
    private final MenuItem menuHelp_contactUs;
    private final MenuItem menuHelp_viewLog;
    private final MenuItem menuHelp_onlineHelp;
    private Set<Plugin> installedPlugins;

    private WildIDController controller;

    public WildIDMenuBar(LanguageModel language, ObservableList<Project> projects, boolean hasOpenedProject) {

        this.projectOrderList = new ArrayList<>(projects);
        Collections.sort(this.projectOrderList, new ProjectComparator());

        // --- Menu Wild.ID
        this.menuWildID = new Menu("Wild.ID");
        this.menuWildID.setId("Wild.ID");

        this.menuWildID_preferences = new MenuItem(language.getString("menu_preferences"),
                new ImageView(new Image("resources/icons/style.png")));
        this.menuWildID_preferences.setId("menu_preferences");

        this.menuWildID_about = new MenuItem(language.getString("menu_Wild.ID_about"),
                new ImageView(new Image("resources/icons/wildId32.png", 16, 16, false, false)));
        this.menuWildID_about.setId("menu_about");

        this.menuWildID_license = new MenuItem(language.getString("menu_Wild.ID_license"),
                new ImageView(new Image("resources/icons/report_edit.png")));
        this.menuWildID_license.setId("menu_license");

        this.menuWildID_registration = new MenuItem(language.getString("menu_Wild.ID_registration"), new ImageView(new Image("resources/icons/tag_blue.png")));

        if (WildID.wildIDProperties.getRegistered()) {
            this.menuWildID_registration.setDisable(true);
        } else {
            this.menuWildID_registration.setId("menuWildID_registration");
        }

        this.menuWildID_quit = new MenuItem(language.getString("menu_quit"),
                new ImageView(new Image("resources/icons/stop.png")));
        this.menuWildID_quit.setId("menu_WildID_quit");

        this.menuWildID.getItems().addAll(
                this.menuWildID_preferences,
                new SeparatorMenuItem(),
                this.menuWildID_about,
                this.menuWildID_license,
                this.menuWildID_registration,
                new SeparatorMenuItem(),
                this.menuWildID_quit);

        // --- Menu File
        this.menuFile = new Menu(language.getString("menu_file"));
        this.menuFile_newProject = new MenuItem(language.getString("menu_new_project"),
                new ImageView(new Image("resources/icons/application_add.png")));

        this.menuFile_newProject.setId("menu_new_project");

        this.menuFile_annotateImages = new Menu(language.getString("menu_annotateImages"),
                new ImageView(new Image("resources/icons/image.png")));
        this.menuFile_annotateImages.setId("annotate_images");

        for (Project project : this.projectOrderList) {
            String projName = project.getName();
            String truncatedName = Util.truncateString(projName, 30);
            int projId = project.getProjectId();

            MenuItem menu_project = new MenuItem(truncatedName);
            menu_project.setId("annotate_images_" + projId);
            generated_menu_items.add(menu_project);
            this.menuFile_annotateImages.getItems().add(menu_project);
        }

        this.menuFile_saveTransferFile = new MenuItem(language.getString("menu_save_transfer_file"), new ImageView(new Image("resources/icons/file_zip.png")));
        this.menuFile_saveTransferFile.setId("save_transfer_file");

        this.menuFile_openTransferFile = new Menu(language.getString("menu_open_transfer_file"), new ImageView(new Image("resources/icons/file_zip.png")));
        this.menuFile_openTransferFile.setId("open_transfer_file");

        this.menuFile_openTransferFile_InNewProject = new MenuItem(language.getString("menu_open_transfer_file_as_new_project"));
        this.menuFile_openTransferFile_InNewProject.setId("open_transfer_file_in_new_project");
        this.menuFile_openTransferFile.getItems().add(this.menuFile_openTransferFile_InNewProject);

        this.menuFile_openTransferFile_InProject = new MenuItem(language.getString("menu_open_transfer_file_in_project"));
        this.menuFile_openTransferFile_InProject.setId("open_transfer_file_in_current_project");
        this.menuFile_openTransferFile.getItems().add(this.menuFile_openTransferFile_InProject);

        this.menuFile.getItems().addAll(
                this.menuFile_newProject,
                new SeparatorMenuItem(),
                this.menuFile_annotateImages,
                new SeparatorMenuItem(),
                this.menuFile_saveTransferFile,
                this.menuFile_openTransferFile);

        // --- Menu Edit
        this.menuEdit = new Menu(language.getString("menu_edit"));
        this.menuEdit_editProject = new Menu(language.getString("menu_edit_project"));

        for (Project project : this.projectOrderList) {
            String projName = project.getName();
            String truncatedName = Util.truncateString(projName, 30);
            int projId = project.getProjectId();

            MenuItem menu_project = new MenuItem(truncatedName);
            menu_project.setId("edit_project_" + projId);
            generated_menu_items.add(menu_project);
            this.menuEdit_editProject.getItems().add(menu_project);
        }

        this.menuEdit_editMasterList = new Menu(language.getString("menu_edit_master_list"));

        this.menuEdit_organizations = new MenuItem(language.getString("menu_edit_organization"),
                new ImageView(new Image("resources/icons/building_add.png")));

        this.menuEdit_organizations.setId("menu_edit_organization");

        this.menuEdit_persons = new MenuItem(language.getString("menu_edit_person"),
                new ImageView(new Image("resources/icons/user_add.png")));

        this.menuEdit_persons.setId("menu_edit_person");

        this.menuEdit_cameraModels = new MenuItem(language.getString("menu_edit_camera_model"),
                new ImageView(new Image("resources/icons/camera_add.png")));

        this.menuEdit_cameraModels.setId("menu_edit_camera_model");

        this.menuEdit_editMasterList.getItems().addAll(
                this.menuEdit_organizations,
                this.menuEdit_persons,
                this.menuEdit_cameraModels);

        this.manuEdit_find = new MenuItem(language.getString("menu_edit_find"),
                new ImageView(new Image("resources/icons/find.png")));
        this.manuEdit_find.setId("menu_edit_find");

        this.menuEdit.getItems().addAll(
                menuEdit_editProject,
                menuEdit_editMasterList,
                new SeparatorMenuItem(),
                manuEdit_find);

        // --- Menu View 
        this.menuView = new Menu(language.getString("menu_view"));
        this.menuView_table = new Menu(language.getString("menu_viewTable"), new ImageView(new Image("resources/icons/table.png")));
        this.menuView_tableSameWindow = new MenuItem(language.getString("menu_viewTable_same_window"), new ImageView(new Image("resources/icons/application.png")));
        this.menuView_tableSameWindow.setId("menuViewTable_same_window");
        this.menuView_tableNewWindow = new MenuItem(language.getString("menu_viewTable_new_window"), new ImageView(new Image("resources/icons/application_double.png")));
        this.menuView_tableNewWindow.setId("menuViewTable_new_window");
        this.menuView_table.getItems().addAll(this.menuView_tableSameWindow, this.menuView_tableNewWindow);
        this.menuView.getItems().addAll(this.menuView_table);

        // --- Menu Exports
        this.menuExport = new Menu(language.getString("menu_export"));
        this.menuExport_dataImages = new Menu(language.getString("menu_export_metadata"), new ImageView(new Image("resources/icons/table_save.png")));
        this.menuExport_toCSV = new Menu(language.getString("menu_export_csv"), new ImageView(new Image("resources/icons/file_csv.png")));
        this.menuExport_toCSV_withImages = new MenuItem(language.getString("menu_export_with_images"));
        this.menuExport_toCSV_withoutImages = new MenuItem(language.getString("menu_export_without_images"));
        this.menuExport_toExcel = new Menu(language.getString("menu_export_excel"), new ImageView(new Image("resources/icons/file_excel.png")));
        this.menuExport_toExcel_withImages = new MenuItem(language.getString("menu_export_with_images"));
        this.menuExport_toExcel_without_images = new MenuItem(language.getString("menu_export_without_images"));
        this.menuExport_toWCS = new Menu(language.getString("menu_export_wcs"), new ImageView(new Image("resources/icons/file_excel.png")));
        this.menuExport_toWCS_withImages = new MenuItem(language.getString("menu_export_with_images"));
        this.menuExport_toWCS_without_images = new MenuItem(language.getString("menu_export_without_images"));

        this.menuExport_toCSV_withImages.setId("menuExport_toCSV_with_images");
        this.menuExport_toCSV_withoutImages.setId("menuExport_toCSV_without_images");
        this.menuExport_toExcel_withImages.setId("menuExport_toExcel_with_images");
        this.menuExport_toExcel_without_images.setId("menuExport_toExcel_without_images");
        this.menuExport_toWCS_withImages.setId("menuExport_toWCS_with_images");
        this.menuExport_toWCS_without_images.setId("menuExport_toWCS_without_images");
        this.menuExport_dataImages.getItems().addAll(this.menuExport_toCSV, this.menuExport_toExcel, this.menuExport_toWCS);
        this.menuExport_toCSV.getItems().addAll(this.menuExport_toCSV_withImages, this.menuExport_toCSV_withoutImages);
        this.menuExport_toExcel.getItems().addAll(this.menuExport_toExcel_withImages, this.menuExport_toExcel_without_images);
        this.menuExport_toWCS.getItems().addAll(this.menuExport_toWCS_withImages, this.menuExport_toWCS_without_images);

        this.menuExport.getItems().addAll(this.menuExport_dataImages);

        // --- Menu Plugin
        this.menuPlugin = new Menu(language.getString("menu_plugin"));
        this.menuPlugin_managePlugins = new MenuItem(language.getString("menu_plugin_managePlugins"), new ImageView(new Image("resources/icons/plugin.png")));
        this.menuPlugin_managePlugins.setId("menuPlugin_managePlugins");
        this.menuPlugin.getItems().add(this.menuPlugin_managePlugins);

        // search for plugins
        try {
            Reflections reflections = new Reflections();
            Set<Class<? extends Plugin>> subTypes = reflections.getSubTypesOf(Plugin.class);

            if (!subTypes.isEmpty()) {
                this.menuPlugin.getItems().add(new SeparatorMenuItem());
            }

            this.installedPlugins = new HashSet<>();

            for (Class pluginClass : subTypes) {
                Constructor[] ctors = pluginClass.getDeclaredConstructors();
                for (Constructor ctor : ctors) {
                    if (ctor.getGenericParameterTypes().length == 0) {
                        Plugin plugin = (Plugin) ctor.newInstance();
                        plugin.setLanguageCode(language.getLanguageCode());
                        plugin.getProperties().put("WildIDMenubar", this);
                        this.menuPlugin.getItems().add(plugin);
                        this.installedPlugins.add(plugin);
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        // --- Menu Help
        this.menuHelp = new Menu(language.getString("menu_help"));
        this.menuHelp_checkUpdate = new MenuItem(language.getString("menu_help_checkUpdate"), new ImageView(new Image("resources/icons/table_refresh.png")));
        this.menuHelp_checkUpdate.setId("menuHelp_checkUpdate");

        this.menuHelp_contactUs = new MenuItem(language.getString("menu_help_contactUs"), new ImageView(new Image("resources/icons/contact_us.png")));
        this.menuHelp_contactUs.setId("menuHelp_contactUs");

        this.menuHelp_viewLog = new MenuItem(language.getString("menu_help_viewLog"), new ImageView(new Image("resources/icons/calendar.png")));
        this.menuHelp_viewLog.setId("menuHelp_viewLog");

        this.menuHelp_onlineHelp = new MenuItem(language.getString("menu_help_onlineHelp"), new ImageView(new Image("resources/icons/help.png")));
        this.menuHelp_onlineHelp.setId("menuHelp_onlineHelp");

        this.menuHelp.getItems().addAll(this.menuHelp_checkUpdate, this.menuHelp_contactUs, this.menuHelp_viewLog, this.menuHelp_onlineHelp);

        this.getMenus().addAll(this.menuWildID, this.menuFile, this.menuEdit, this.menuView, this.menuExport, this.menuPlugin, this.menuHelp);

        //boolean noOpenedProject = openedProject == null;
        setEmptyProjects(this.projectOrderList.isEmpty());
        setImageMenuDisable(!hasOpenedProject);
        setSaveTransferFileDisable(!hasOpenedProject);
        setLoadTransferFileDisable(!hasOpenedProject);
    }

    public void setEmptyProjects(boolean isEmpty) {
        this.menuFile_annotateImages.setDisable(isEmpty);
        this.menuEdit_editProject.setDisable(isEmpty);
        this.menuExport_dataImages.setDisable(isEmpty);
        this.menuView_table.setDisable(isEmpty);
    }

    @Override
    public void setLanguage(LanguageModel language) {
        //this.language = language;
        this.menuWildID_about.setText(language.getString("menu_Wild.ID_about"));
        this.menuWildID_license.setText(language.getString("menu_Wild.ID_license"));
        this.menuWildID_preferences.setText(language.getString("menu_preferences"));
        this.menuWildID_registration.setText(language.getString("menu_Wild.ID_registration"));
        this.menuWildID_quit.setText(language.getString("menu_quit"));

        this.menuFile.setText(language.getString("menu_file"));
        this.menuFile_saveTransferFile.setText(language.getString("menu_save_transfer_file"));
        this.menuFile_openTransferFile.setText(language.getString("menu_open_transfer_file"));
        this.menuFile_openTransferFile_InNewProject.setText(language.getString("menu_open_transfer_file_as_new_project"));
        this.menuFile_openTransferFile_InProject.setText(language.getString("menu_open_transfer_file_in_project"));
        this.menuFile_annotateImages.setText(language.getString("menu_annotateImages"));
        this.menuFile_newProject.setText(language.getString("menu_new_project"));

        this.menuEdit_organizations.setText(language.getString("menu_edit_organization"));
        this.menuEdit_persons.setText(language.getString("menu_edit_person"));
        this.menuEdit_cameraModels.setText(language.getString("menu_edit_camera_model"));
        this.menuEdit.setText(language.getString("menu_edit"));
        this.menuEdit_editProject.setText(language.getString("menu_edit_project"));
        this.menuEdit_editMasterList.setText(language.getString("menu_edit_master_list"));
        this.manuEdit_find.setText(language.getString("menu_edit_find"));

        this.menuHelp.setText(language.getString("menu_help"));
        this.menuHelp_checkUpdate.setText(language.getString("menu_help_checkUpdate"));
        this.menuHelp_contactUs.setText(language.getString("menu_help_contactUs"));
        this.menuHelp_viewLog.setText(language.getString("menu_help_viewLog"));
        this.menuHelp_onlineHelp.setText(language.getString("menu_help_onlineHelp"));

        this.menuExport.setText(language.getString("menu_export"));
        this.menuExport_dataImages.setText(language.getString("menu_export_metadata"));
        this.menuExport_toCSV.setText(language.getString("menu_export_csv"));
        this.menuExport_toExcel.setText(language.getString("menu_export_excel"));
        this.menuExport_toWCS.setText(language.getString("menu_export_wcs"));
        this.menuExport_toCSV_withImages.setText(language.getString("menu_export_with_images"));
        this.menuExport_toCSV_withoutImages.setText(language.getString("menu_export_without_images"));
        this.menuExport_toExcel_withImages.setText(language.getString("menu_export_with_images"));
        this.menuExport_toExcel_without_images.setText(language.getString("menu_export_without_images"));
        this.menuExport_toWCS_withImages.setText(language.getString("menu_export_with_images"));
        this.menuExport_toWCS_without_images.setText(language.getString("menu_export_without_images"));

        this.menuView.setText(language.getString("menu_view"));
        this.menuView_table.setText(language.getString("menu_viewTable"));
        this.menuView_tableSameWindow.setText(language.getString("menu_viewTable_same_window"));
        this.menuView_tableNewWindow.setText(language.getString("menu_viewTable_new_window"));

        this.menuPlugin_managePlugins.setText(language.getString("menu_plugin_managePlugins"));
        this.menuPlugin.setText(language.getString("menu_plugin"));
        for (MenuItem menuItem : this.menuPlugin.getItems()) {
            if (menuItem instanceof Plugin) {
                Plugin plugin = (Plugin) menuItem;
                plugin.setLanguageCode(language.getLanguageCode());
            }
        }
    }

    public void setController(WildIDController controller) {

        this.controller = controller;

        this.menuWildID_about.setOnAction(controller);
        this.menuWildID_preferences.setOnAction(controller);
        this.menuWildID_license.setOnAction(controller);
        this.menuWildID_quit.setOnAction(controller);

        this.menuFile_saveTransferFile.setOnAction(controller);
        this.menuFile_openTransferFile_InNewProject.setOnAction(controller);
        this.menuFile_openTransferFile_InProject.setOnAction(controller);
        this.menuFile_newProject.setOnAction(controller);

        this.menuEdit_organizations.setOnAction(controller);
        this.menuEdit_persons.setOnAction(controller);
        this.menuEdit_cameraModels.setOnAction(controller);
        this.manuEdit_find.setOnAction(controller);

        this.menuView_tableSameWindow.setOnAction(controller);
        this.menuView_tableNewWindow.setOnAction(controller);

        this.menuExport_toCSV_withImages.setOnAction(controller);
        this.menuExport_toCSV_withoutImages.setOnAction(controller);
        this.menuExport_toExcel_withImages.setOnAction(controller);
        this.menuExport_toExcel_without_images.setOnAction(controller);
        this.menuExport_toWCS_withImages.setOnAction(controller);
        this.menuExport_toWCS_without_images.setOnAction(controller);

        for (MenuItem item : this.generated_menu_items) {
            item.setOnAction(controller);
        }

        this.menuPlugin_managePlugins.setOnAction(controller);

        this.menuHelp_checkUpdate.setOnAction(controller);
        this.menuHelp_contactUs.setOnAction(controller);
        this.menuHelp_viewLog.setOnAction(controller);
        this.menuHelp_onlineHelp.setOnAction(controller);
        this.menuWildID_registration.setOnAction(controller);
    }

    public void addNewProject(Project project) {
        int index = -1;
        for (int i = 0; i < this.projectOrderList.size(); i++) {
            Project proj = this.projectOrderList.get(i);
            if (project.getName().compareTo(proj.getName()) < 0) {
                index = i;
                break;
            }
        }

        String projName = project.getName();
        String truncatedName = Util.truncateString(projName, 30);
        int projId = project.getProjectId();

        MenuItem menu_annotate_images = new MenuItem(truncatedName);
        menu_annotate_images.setId("annotate_images_" + projId);
        menu_annotate_images.setOnAction(controller);

        MenuItem menu_edit_project = new MenuItem(truncatedName);
        menu_edit_project.setId("edit_project_" + projId);
        menu_edit_project.setOnAction(controller);

        setEmptyProjects(false);

        if (index < 0) {
            this.projectOrderList.add(project);
            this.menuFile_annotateImages.getItems().add(menu_annotate_images);
            this.menuEdit_editProject.getItems().add(menu_edit_project);
        } else {
            this.projectOrderList.add(index, project);
            this.menuFile_annotateImages.getItems().add(index, menu_annotate_images);
            this.menuEdit_editProject.getItems().add(index, menu_edit_project);
        }

        generated_menu_items.add(menu_annotate_images);
        generated_menu_items.add(menu_edit_project);

    }

    public void removeProject(Project project) {
        for (int i = 0; i < this.projectOrderList.size(); i++) {
            Project proj = this.projectOrderList.get(i);
            if (project.getProjectId().equals(proj.getProjectId())) {
                this.menuFile_annotateImages.getItems().remove(i);
                this.menuEdit_editProject.getItems().remove(i);
                this.projectOrderList.remove(project);
                break;
            }
        }

        setEmptyProjects(this.projectOrderList.isEmpty());
    }

    public void updateProject(Project project) {
        for (int i = 0; i < this.projectOrderList.size(); i++) {
            Project proj = this.projectOrderList.get(i);

            if (project.getProjectId().equals(proj.getProjectId())) {
                String truncatedName = Util.truncateString(project.getName(), 30);

                MenuItem menu_annotate_images = this.menuFile_annotateImages.getItems().get(i);
                menu_annotate_images.setText(truncatedName);

                MenuItem menu_editProject = this.menuEdit_editProject.getItems().get(i);
                menu_editProject.setText(truncatedName);
                break;
            }
        }
    }

    public void setSaveTransferFileDisable(boolean disable) {
        this.menuFile_saveTransferFile.setDisable(disable);
    }

    public void setLoadTransferFileDisable(boolean disable) {
        this.menuFile_openTransferFile_InProject.setDisable(disable);
    }

    public final void setImageMenuDisable(boolean disable) {
        this.menuView_table.setDisable(disable);
        this.menuExport_dataImages.setDisable(disable);
    }

    public Set<Plugin> getInstalledPlugins() {
        return this.installedPlugins;
    }

    public Menu getMenuPlugin() {
        return this.menuPlugin;
    }

    public void disableRegistration() {
        this.menuWildID_registration.setDisable(true);
    }
}
