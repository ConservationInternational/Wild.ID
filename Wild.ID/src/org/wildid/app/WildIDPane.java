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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.Image;
import org.wildid.entity.Organization;
import org.wildid.entity.Person;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDPane extends BorderPane implements LanguageChangable {

    // the language used 
    private LanguageModel language;

    // the current used controller 
    private WildIDController controller;

    // the menubar 
    private final WildIDMenuBar menuBar;

    // the navigation tree 
    private final WildIDNavigationPane navPane;

    // the current data pane 
    private WildIDDataPane projectDataPane;
    private WildIDDataPane orgDataPane;
    private WildIDDataPane personDataPane;
    private WildIDDataPane cameraModelDataPane;
    private WildIDDataPane searchPane;
    private final Menu version_upgradable;
    private final String version_upgradable_language_text;

    // the pane for status
    private final WildIDStatusPane statusPane;

    // the container for data pane
    private final ScrollPane rightScrollPane;
    private Project lastOpenedProject;

    public WildIDPane(LanguageModel language,
            ObservableList<Project> projects,
            ObservableList<Organization> orgs,
            ObservableList<Person> persons,
            ObservableList<CameraModel> cameraModels) {

        this.language = language;

        int opened_project_id = WildID.wildIDProperties.getOpenedProject();

        try {
            if (opened_project_id > 0) {
                for (Project p : projects) {
                    if (p.getProjectId().equals(opened_project_id)) {
                        lastOpenedProject = p;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
        }

        this.menuBar = new WildIDMenuBar(language, projects, lastOpenedProject != null);

        String latestVersion = WildIDUpdatePane.getLatestWildIdVersion();
        if (latestVersion == null) {
            version_upgradable_language_text = "menu_unknown_version";
        } else if (Util.isNewerVersion(WildID.VERSION, WildIDUpdatePane.getLatestWildIdVersion())) {
            version_upgradable_language_text = "menu_upgradable";
        } else {
            version_upgradable_language_text = "menu_latest_version";
        }

        version_upgradable = new Menu(language.getString(version_upgradable_language_text));

        MenuBar rightBar = new MenuBar();
        rightBar.getMenus().addAll(version_upgradable);
        HBox menuBars = new HBox(this.menuBar, rightBar);
        HBox.setHgrow(this.menuBar, Priority.SOMETIMES);

        this.setTop(menuBars);

        this.statusPane = new WildIDStatusPane(language);

        // setup initial data pane
        SplitPane sp = new SplitPane();

        navPane = new WildIDNavigationPane(language, projects, orgs, persons, cameraModels);
        navPane.setMinWidth(150);
        navPane.setMaxWidth(380);
        navPane.setPrefWidth(200);
        navPane.setStyle("-fx-background-color: #00dd00");

        this.projectDataPane = new ProjectNewPane(language);
        rightScrollPane = new ScrollPane();
        rightScrollPane.setFitToHeight(true);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        
        this.setProjectDataPane(this.projectDataPane);

        sp.getItems().addAll(navPane, rightScrollPane);
        this.setCenter(sp);
        sp.setDividerPositions(0.25f, 0.75f);

        this.setBottom(statusPane);

        this.setPadding(new Insets(0, 0, 0, 0));
    }

    // setup language
    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        this.menuBar.setLanguage(language);
        this.navPane.setLanguage(language);

        if (this.projectDataPane != null) {
            this.projectDataPane.setLanguage(language);
        }

        if (this.orgDataPane != null) {
            this.orgDataPane.setLanguage(language);
        }

        if (this.personDataPane != null) {
            this.personDataPane.setLanguage(language);
        }

        if (this.cameraModelDataPane != null) {
            this.cameraModelDataPane.setLanguage(language);
        }

        version_upgradable.setText(language.getString(version_upgradable_language_text));

        this.statusPane.setLanguage(language);
    }

    public Project getLastOpenedProject() {
        return this.lastOpenedProject;
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    public WildIDDataPane getProjectDataPane() {
        return this.projectDataPane;
    }

    public WildIDDataPane getOrganizationDataPane() {
        return this.orgDataPane;
    }

    public WildIDDataPane getPersonDataPane() {
        return this.personDataPane;
    }

    public WildIDDataPane getCameraModelDataPane() {
        return this.cameraModelDataPane;
    }

    public WildIDNavigationPane getNavigationPane() {
        return this.navPane;
    }

    public WildIDStatusPane getStatusPane() {
        return this.statusPane;
    }

    public void setProjectDataPane(WildIDDataPane projectDataPane) {
        this.projectDataPane = projectDataPane;
        this.rightScrollPane.setContent(projectDataPane);
        this.projectDataPane.setWildIDController(this.controller);
        this.projectDataPane.setLanguage(language);
    }

    public void setOrganizationDataPane(WildIDDataPane orgDataPane) {
        this.orgDataPane = orgDataPane;
        this.rightScrollPane.setContent(orgDataPane);
        this.orgDataPane.setWildIDController(this.controller);
        this.orgDataPane.setLanguage(language);
    }

    public void setPersonDataPane(WildIDDataPane personDataPane) {
        this.personDataPane = personDataPane;
        this.rightScrollPane.setContent(personDataPane);
        this.personDataPane.setWildIDController(this.controller);
        this.personDataPane.setLanguage(language);
    }

    public void setCameraModelDataPane(WildIDDataPane cameraModelDataPane) {
        this.cameraModelDataPane = cameraModelDataPane;
        this.rightScrollPane.setContent(cameraModelDataPane);
        this.cameraModelDataPane.setWildIDController(this.controller);
        this.cameraModelDataPane.setLanguage(language);
    }

    public void setSearchPane(WildIDDataPane searchPane) {
        this.searchPane = searchPane;
        this.rightScrollPane.setContent(searchPane);
        this.searchPane.setWildIDController(this.controller);
        this.searchPane.setLanguage(language);
    }

    // setup controller
    public void setWildIDController(WildIDController controller) {
        this.controller = controller;
        this.menuBar.setController(controller);
        this.navPane.setWildIDController(controller);
        this.projectDataPane.setWildIDController(controller);
    }

    public void addNewProject(Project project) {
        this.navPane.addNewProject(project);
        this.menuBar.addNewProject(project);
    }

    public void updateProject(Project project) {
        this.navPane.updateProject(project);
        this.menuBar.updateProject(project);
    }

    public void removeProject(Project project) {
        this.navPane.removeProject(project);
        this.menuBar.removeProject(project);
    }

    public void showStandardView(Project project) {
        this.navPane.showStandardView(project);
    }

    public void showStandardImageView(Project project) {
        this.navPane.showStandardImageView(project);
    }

    public void addNewPerson(Project project, Person person) {
        this.navPane.addNewPerson(project, person);
    }

    public void addSelectedPerson(Project project, Person person) {
        this.navPane.addSelectedPerson(project, person);
    }

    public void updatePerson(Project project, Person person) {
        this.navPane.updatePerson(project, person);
        if (personDataPane instanceof PersonEditPane) {
            PersonEditPane personEditPane = (PersonEditPane) personDataPane;
            personEditPane.setPerson(person);
        }
    }

    public void removePerson(Project project, Person person) {
        this.navPane.removePerson(project, person);
    }

    public void addNewOrganization(Project project, Organization org) {
        this.navPane.addNewOrganization(project, org);
    }

    public void updateOrganization(Project project, Organization org) {
        this.navPane.updateOrganization(project, org);
        if (orgDataPane instanceof OrganizationEditPane) {
            OrganizationEditPane orgEditPane = (OrganizationEditPane) orgDataPane;
            orgEditPane.setOrganization(org);
        }
    }

    public void removeOrganization(Project project, Organization org) {
        this.navPane.removeOrganization(project, org);
    }

    public void addNewPerson(Person person) {
        this.controller.addNewPerson(person);
        this.navPane.addNewPerson(person);

        if (projectDataPane instanceof ProjectNewPersonPane) {
            // add this person into the select list
            ProjectNewPersonPane personPane = (ProjectNewPersonPane) projectDataPane;
            personPane.addPersonForSelection(person);
        }
    }

    public void updatePerson(Person person) {
        this.navPane.updatePerson(person);
        if (projectDataPane instanceof ProjectEditPersonPane) {
            ProjectEditPersonPane projectEditPersonPane = (ProjectEditPersonPane) projectDataPane;
            Person personInEdit = projectEditPersonPane.getOriginalPerson();
            if (personInEdit.getPersonId().intValue() == person.getPersonId()) {
                projectEditPersonPane.setPerson(person);
            }
        }

        if (projectDataPane instanceof ProjectNewPersonPane) {
            // add this person into the select list
            ProjectNewPersonPane personPane = (ProjectNewPersonPane) projectDataPane;
            personPane.updatePersonForSelection(person);
        }

    }

    public void removePerson(Person person) {
        this.navPane.removePerson(person);
    }

    public void addNewOrganization(Organization org) {
        this.controller.addNewOrganization(org);
        this.navPane.addNewOrganization(org);
    }

    public void updateOrganization(Organization org) {
        this.navPane.updateOrganization(org);
        if (projectDataPane instanceof ProjectEditOrganizationPane) {
            ProjectEditOrganizationPane projectEditOrganizationPane = (ProjectEditOrganizationPane) projectDataPane;
            projectEditOrganizationPane.setOrganization(org);
        }
    }

    public void removeOrganization(Organization org) {
        this.navPane.removeOrganization(org);
    }

    public void createNewCameraTrapArray(CameraTrapArray array) {
        this.navPane.createNewCameraTrapArray(array);
        if (projectDataPane instanceof ProjectCameraTrapArrayPane) {
            ProjectCameraTrapArrayPane arrayPane = (ProjectCameraTrapArrayPane) projectDataPane;
            arrayPane.createNewCameraTrapArray(array);
        }
    }

    public void removeCameraTrapArray(CameraTrapArray array) {
        this.navPane.removeCameraTrapArray(array);
        if (projectDataPane instanceof ProjectCameraTrapArrayPane) {
            ProjectCameraTrapArrayPane arrayPane = (ProjectCameraTrapArrayPane) projectDataPane;
            arrayPane.removeCameraTrapArray(array);
        }
    }

    public void updateCameraTrapArray(CameraTrapArray array) {
        this.navPane.updateCameraTrapArray(array);
        if (projectDataPane instanceof ProjectCameraTrapArrayPane) {
            ProjectCameraTrapArrayPane arrayPane = (ProjectCameraTrapArrayPane) projectDataPane;
            arrayPane.updateCameraTrapArray(array);
        } else if (projectDataPane instanceof ProjectCameraTrapPane) {
            ProjectCameraTrapPane trapPane = (ProjectCameraTrapPane) projectDataPane;
            trapPane.updateCameraTrapArray(array);
        }
    }

    public void createNewCameraTrap(CameraTrap trap) {
        this.navPane.createNewCameraTrap(trap);
        if (projectDataPane instanceof ProjectCameraTrapPane) {
            ProjectCameraTrapPane trapPane = (ProjectCameraTrapPane) projectDataPane;
            trapPane.createNewCameraTrap(trap);
        }
    }

    public void updateCameraTrap(CameraTrap trap) {
        this.navPane.updateCameraTrap(trap);
        if (projectDataPane instanceof ProjectCameraTrapPane) {
            ProjectCameraTrapPane trapPane = (ProjectCameraTrapPane) projectDataPane;
            trapPane.updateCameraTrap(trap);
        }
    }

    public void cancelCameraTrapEditing(CameraTrap trap) {
        this.navPane.cancelCameraTrapEditing(trap);
    }

    public void removeCameraTrap(CameraTrap trap) {
        this.navPane.removeCameraTrap(trap);
        if (projectDataPane instanceof ProjectCameraTrapPane) {
            ProjectCameraTrapPane trapPane = (ProjectCameraTrapPane) projectDataPane;
            trapPane.removeCameraTrap(trap);
        }
    }

    public void selectCameraTrap(CameraTrap trap) {
        this.navPane.selectCameraTrap(trap);
    }

    public void createEvent(Event event) {
        this.navPane.createEvent(event);
        if (projectDataPane instanceof ProjectEventPane) {
            ProjectEventPane eventPane = (ProjectEventPane) projectDataPane;
            eventPane.createEvent(event);
        }
    }

    public void removeEvent(Event event) {
        this.navPane.removeEvent(event);
        if (projectDataPane instanceof ProjectEventPane) {
            ProjectEventPane eventPane = (ProjectEventPane) projectDataPane;
            eventPane.removeEvent(event);
        }
    }

    public void createDeployment(Deployment deployment) {
        this.navPane.createDeployment(deployment);
    }

    public void updateDeployment(Deployment deployment) {
        this.navPane.updateDeployment(deployment);
    }

    public void removeDeployment(Deployment deployment) {
        this.navPane.removeDeployment(deployment);
    }

    public void createCameraModel(CameraModel cameraModel) {
        this.navPane.createCameraModel(cameraModel);
        if (cameraModelDataPane instanceof CameraModelNewPane) {
            CameraModelNewPane cameraModelPane = (CameraModelNewPane) cameraModelDataPane;
            cameraModelPane.createCameraModel(cameraModel);
        }
        if (projectDataPane instanceof ProjectCameraPane) {
            ProjectCameraPane cameraPane = (ProjectCameraPane) projectDataPane;
            cameraPane.addCameraModel(cameraModel);
        }
    }

    public void updateCameraModel(String oldMaker, String oldModel, CameraModel cameraModel) {
        this.navPane.updateCameraModel(oldMaker, oldModel, cameraModel);
        if (cameraModelDataPane instanceof CameraModelNewPane) {
            CameraModelNewPane cameraModelPane = (CameraModelNewPane) cameraModelDataPane;
            cameraModelPane.updateCameraModel(oldMaker, oldModel, cameraModel);
        }
        if (projectDataPane instanceof ProjectCameraPane) {
            ProjectCameraPane cameraPane = (ProjectCameraPane) projectDataPane;
            cameraPane.updateCameraModel(oldMaker, oldModel, cameraModel);
        }
    }

    public void deleteCameraModel(CameraModel cameraModel) {
        this.navPane.deleteCameraModel(cameraModel);
        if (cameraModelDataPane instanceof CameraModelNewPane) {
            CameraModelNewPane cameraModelPane = (CameraModelNewPane) cameraModelDataPane;
            cameraModelPane.deleteCameraModel(cameraModel);
        }
        if (projectDataPane instanceof ProjectCameraPane) {
            ProjectCameraPane cameraPane = (ProjectCameraPane) projectDataPane;
            cameraPane.deleteCameraModel(cameraModel);
        }
    }

    public void saveExifCameraModelFeatures(CameraModel cameraModel) {
        this.navPane.saveExifCameraModelFeatures(cameraModel);
        if (cameraModelDataPane instanceof CameraModelExifPane) {
            CameraModelExifPane exifPane = (CameraModelExifPane) cameraModelDataPane;
            exifPane.exifCameraModelFeaturesSaved();
        }
    }

    public void saveImageAnnotation(List<Image> images) {
        this.navPane.saveImageAnnotation(images);
    }

    public void updateEvent(Event event) {
        this.navPane.updateEvent(event);
        if (projectDataPane instanceof ProjectEventPane) {
            ProjectEventPane eventPane = (ProjectEventPane) projectDataPane;
            eventPane.updateEvent(event);
        }
    }

    public void showErrorMessage(Exception ex) {

        Dialog dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(language.getString("title_error"));
        if (ex instanceof org.hibernate.exception.DataException) {
            ex = ((org.hibernate.exception.DataException) ex).getSQLException();
        }
        dialog.setHeaderText(
                language.getString("internal_error_header")
                //+ ": " + ex.getMessage()
                + "\n" + language.getString("internal_error_context"));

        dialog.setResizable(true);

        TextArea textarea = new TextArea();
        textarea.setPrefRowCount(20);
        textarea.setMinWidth(800);
        textarea.setEditable(false);
        String log = Util.tail(new File("WildID.log"), 2000);
        log = getTodayLog(log);
        textarea.setText(log);
        textarea.positionCaret(log.lastIndexOf(" ERROR ") + 1000);
        dialog.getDialogPane().setContent(textarea);

        ObservableList<ButtonType> buttonTypes = ((Alert) dialog).getButtonTypes();
        buttonTypes.clear();
        //buttonTypes.add(new ButtonType(language.getString("alert_ok"), ButtonBar.ButtonData.OK_DONE));
        buttonTypes.add(new ButtonType(language.getString("alert_cancel"), ButtonBar.ButtonData.CANCEL_CLOSE));

        if (Util.isOnline()) {
            buttonTypes.add(new ButtonType(language.getString("submit_log_button"), ButtonBar.ButtonData.YES));
        } else {
            buttonTypes.add(new ButtonType(language.getString("save_log_button"), ButtonBar.ButtonData.YES));
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if ((result.isPresent()) && (result.get().getText().equals(language.getString("save_log_button")))) {
            FileChooser fileChooser = new FileChooser();

            if (WildID.wildIDProperties.getWorkingDirObj() != null) {
                fileChooser.setInitialDirectory(WildID.wildIDProperties.getWorkingDirObj());
            }

            fileChooser.setTitle(language.getString("save_log_button"));
            String filename = "Wild_ID_log_" + new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + ".txt";
            fileChooser.setInitialFileName(filename);
            File saveFile = fileChooser.showSaveDialog(null);
            try {
                PrintWriter pw = new PrintWriter(new FileWriter(saveFile));
                pw.println(log);
                pw.close();
            } catch (Exception err) {
            }
        } else if ((result.isPresent()) && (result.get().getText().equals(language.getString("submit_log_button")))) {
            controller.openWindowModalPopup(new ReportErrorPane(controller, this.getLanguage()));
        }

    }

    public WildIDMenuBar getMenuBar() {
        return this.menuBar;
    }

    public String getTodayLog(String log) {
        String result = "";
        try {
            BufferedReader reader = new BufferedReader(new StringReader(log));
            String line = reader.readLine();

            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = format.format(today);

            boolean started = false;
            while (line != null) {
                if (line.startsWith(dateStr)) {
                    started = true;
                }

                if (started) {
                    result += line + "\n";
                }

                line = reader.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public void viewLog() {
        Dialog dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(language.getString("menu_help_viewLog"));
        dialog.setHeaderText(language.getString("error_report_offline_info"));
        dialog.setResizable(true);

        if (Util.isOnline()) {
            controller.openWindowModalPopup(new ReportErrorPane(controller, this.getLanguage()));
        } else {
            TextArea textarea = new TextArea();
            textarea.setPrefRowCount(20);
            textarea.setMinWidth(800);
            textarea.setEditable(false);
            String log = Util.tail(new File("WildID.log"), 2000);
            log = "Java Version: " + System.getProperty("java.version") + "\n"
                    + "OS Name: " + System.getProperty("os.name") + "\n"
                    + "OS Version: " + System.getProperty("os.version") + "\n"
                    + "OS Arch: " + System.getProperty("os.arch") + "\n"
                    + "Home Directory: " + System.getProperty("user.dir") + "\n\n"
                    + getTodayLog(log);
            textarea.setText(log);
            textarea.positionCaret(log.lastIndexOf(" ERROR ") + 1000);
            dialog.getDialogPane().setContent(textarea);

            ObservableList<ButtonType> buttonTypes = ((Alert) dialog).getButtonTypes();
            buttonTypes.clear();
            ButtonType buttonTypeTwo = new ButtonType(language.getString("save_log_button"), ButtonBar.ButtonData.NO);
            ButtonType buttonTypeThree = new ButtonType(language.getString("alert_cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            buttonTypes.setAll(buttonTypeTwo, buttonTypeThree);

            Optional<ButtonType> result = dialog.showAndWait();

            if ((result.isPresent()) && (result.get().getText().equals(language.getString("save_log_button")))) {
                FileChooser fileChooser = new FileChooser();

                if (WildID.wildIDProperties.getWorkingDirObj() != null) {
                    fileChooser.setInitialDirectory(WildID.wildIDProperties.getWorkingDirObj());
                }

                fileChooser.setTitle(language.getString("save_log_button"));
                String filename = "Wild_ID_log_" + new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + ".txt";
                fileChooser.setInitialFileName(filename);
                File saveFile = fileChooser.showSaveDialog(null);
                try {
                    PrintWriter pw = new PrintWriter(new FileWriter(saveFile));
                    pw.println(log);
                    pw.close();
                } catch (Exception err) {
                }
            }
        }
    }

    public void contactUs() {
        if (Util.isOnline()) {
            controller.openWindowModalPopup(new ContactUsPane(controller, this.getLanguage()));
        } else {
            Util.alertErrorPopup(
                    language.getString("title_error"),
                    language.getString("registration_register_failed_no_network"),
                    language.getString("contact_us_offline_info"),
                    language.getString("alert_ok"));
        }
    }
}
