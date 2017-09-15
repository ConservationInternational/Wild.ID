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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.application.Application;
import static javafx.application.Application.STYLESHEET_CASPIAN;
import static javafx.application.Application.STYLESHEET_MODENA;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.xml.bind.JAXBException;
import org.apache.log4j.Logger;
import org.wildid.app.plugin.PluginException;
import org.wildid.entity.Age;
import org.wildid.entity.BaitType;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraModelExifFeature;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.FailureType;
import org.wildid.entity.FeatureType;
import org.wildid.entity.HomoSapiensType;
import org.wildid.entity.Image;
import org.wildid.entity.ImageFeature;
import org.wildid.entity.ImageIndividual;
import org.wildid.entity.ImageRepository;
import org.wildid.entity.ImageSequence;
import org.wildid.entity.ImageSpecies;
import org.wildid.entity.ImageType;
import org.wildid.entity.ImageUncertaintyType;
import org.wildid.entity.Organization;
import org.wildid.entity.Person;
import org.wildid.entity.Preference;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectOrganization;
import org.wildid.entity.ProjectOrganizationId;
import org.wildid.entity.ProjectPersonRole;
import org.wildid.entity.Role;
import org.wildid.service.AgeService;
import org.wildid.service.AgeServiceImpl;
import org.wildid.service.BaitTypeService;
import org.wildid.service.BaitTypeServiceImpl;
import org.wildid.service.CameraModelService;
import org.wildid.service.CameraModelServiceImpl;
import org.wildid.service.CameraService;
import org.wildid.service.CameraServiceImpl;
import org.wildid.service.CameraTrapArrayService;
import org.wildid.service.CameraTrapArrayServiceImpl;
import org.wildid.service.CameraTrapService;
import org.wildid.service.CameraTrapServiceImpl;
import org.wildid.service.DeploymentService;
import org.wildid.service.DeploymentServiceImpl;
import org.wildid.service.EventService;
import org.wildid.service.EventServiceImpl;
import org.wildid.service.FailureTypeService;
import org.wildid.service.FailureTypeServiceImpl;
import org.wildid.service.FeatureTypeService;
import org.wildid.service.FeatureTypeServiceImpl;
import org.wildid.service.ImageFeatureService;
import org.wildid.service.ImageFeatureServiceImpl;
import org.wildid.service.ImageService;
import org.wildid.service.ImageServiceImpl;
import org.wildid.service.ImageTypeService;
import org.wildid.service.ImageTypeServiceImpl;
import org.wildid.service.ImageUncertaintyTypeService;
import org.wildid.service.ImageUncertaintyTypeServiceImpl;
import org.wildid.service.OrganizationService;
import org.wildid.service.OrganizationServiceImpl;
import org.wildid.service.PersonService;
import org.wildid.service.PersonServiceImpl;
import org.wildid.service.PreferenceService;
import org.wildid.service.PreferenceServiceImpl;
import org.wildid.service.ProjectService;
import org.wildid.service.ProjectServiceImpl;
import org.wildid.service.RoleService;
import org.wildid.service.RoleServiceImpl;
import org.wildid.service.TaxonomyService;
import org.wildid.service.TaxonomyServiceImpl;

public class WildIDController implements EventHandler<ActionEvent>, ChangeListener<TreeItem<String>> {

    private final WildIDPane pane;
    private final ObservableList<Project> projects;
    private final ObservableList<Organization> orgs;
    private final ObservableList<Person> persons;
    private Stage dialog;
    private Stage undockWindow = null;
    static Logger log = Logger.getLogger(WildIDController.class.getName());

    public WildIDController(WildIDPane pane, ObservableList<Project> projects,
            ObservableList<Organization> orgs, ObservableList<Person> persons) {
        this.pane = pane;
        this.projects = projects;
        this.orgs = orgs;
        this.persons = persons;
        this.pane.setWildIDController(this);

        Project openedProject = this.projects != null && this.projects.size() == 1
                ? this.projects.get(0) : this.pane.getLastOpenedProject();

        if (openedProject != null) {
            loadLastOpenedProject(openedProject);
        }
    }

    // this method controls the behavior of the menubar and the buttons on the data pane
    @Override
    public void handle(ActionEvent event) {
        try {
            Object object = event.getSource();
            if (object instanceof MenuItem) {
                MenuItem item = (MenuItem) object;
                String id = item.getId();
                log.info("Menu " + id);

                if (id != null) {
                    if (id.equals("menu_WildID_quit")) {
                        WildID.shutdown();
                    } else if (id.equals("menu_preferences")) {
                        PreferencePane prefPane = new PreferencePane(pane.getLanguage());
                        pane.setProjectDataPane(prefPane);
                        pane.getStatusPane().write(pane.getLanguage().getString("preference_pane_title"));
                    } else if (id.equals("menu_new_project")) {
                        setNewProjectPane();
                    } else if (id.equals("menu_edit_organization")) {
                        setNewOrganizationPane();
                    } else if (id.equals("menu_edit_person")) {
                        setNewPersonPane();
                    } else if (id.equals("menu_edit_camera_model")) {
                        setNewCameraModelPane();
                    } else if (id.equals("menu_edit_find")) {
                        setFindPane();
                    } else if (id.equals("save_transfer_file")) {
                        TreeItem selectedTreeItem = (TreeItem) this.pane.getNavigationPane().getImageTree().getSelectionModel().getSelectedItem();
                        List<Deployment> deployments = new ArrayList<>();
                        String fileName = "Wild_ID_";

                        if (selectedTreeItem == null) {

                            ProjectTreeItem projectTreeItem = (ProjectTreeItem) this.pane.getNavigationPane().getImageTree().getRoot();
                            ProjectService projectService = new ProjectServiceImpl();
                            deployments.addAll(projectService.getDeployments(projectTreeItem.getProject()));

                            fileName += Util.getObjectId(projectTreeItem.getProject());

                        } else if (selectedTreeItem instanceof ProjectTreeItem) {

                            ProjectTreeItem projectTreeItem = (ProjectTreeItem) selectedTreeItem;
                            ProjectService projectService = new ProjectServiceImpl();
                            deployments.addAll(projectService.getDeployments(projectTreeItem.getProject()));

                            fileName += Util.getObjectId(projectTreeItem.getProject());

                        } else if (selectedTreeItem instanceof ProjectImageListTreeItem) {

                            ProjectImageListTreeItem projectImageTreeItem = (ProjectImageListTreeItem) selectedTreeItem;
                            ProjectService projectService = new ProjectServiceImpl();
                            deployments.addAll(projectService.getDeployments(projectImageTreeItem.getProject()));

                            fileName += Util.getObjectId(projectImageTreeItem.getProject());

                        } else if (selectedTreeItem instanceof ProjectImageEventTreeItem) {

                            ProjectImageEventTreeItem projectImageEventTreeItem = (ProjectImageEventTreeItem) selectedTreeItem;
                            ProjectService projectService = new ProjectServiceImpl();
                            deployments.addAll(projectService.getDeployments(projectImageEventTreeItem.getEvent()));

                            fileName += Util.getObjectId(projectImageEventTreeItem.getEvent());

                        } else if (selectedTreeItem instanceof ProjectImageEventArrayTreeItem) {

                            ProjectImageEventArrayTreeItem projectImageEventArrayTreeItem = (ProjectImageEventArrayTreeItem) selectedTreeItem;
                            ProjectService projectService = new ProjectServiceImpl();
                            deployments.addAll(projectService.getDeployments(projectImageEventArrayTreeItem.getEvent(), projectImageEventArrayTreeItem.getCameraTrapArray()));
                            Event evt = projectImageEventArrayTreeItem.getEvent();
                            CameraTrapArray ctArray = projectImageEventArrayTreeItem.getCameraTrapArray();

                            fileName += Util.getObjectId(evt, ctArray);

                        } else if (selectedTreeItem instanceof ProjectImageDeploymentTreeItem) {

                            ProjectImageDeploymentTreeItem projectImageDeploymentTreeItem = (ProjectImageDeploymentTreeItem) selectedTreeItem;
                            ProjectService projectService = new ProjectServiceImpl();
                            Deployment deployment = projectService.getDeployment(projectImageDeploymentTreeItem.getDeployment().getEvent(), projectImageDeploymentTreeItem.getDeployment().getCameraTrap());
                            Event evt = deployment.getEvent();
                            CameraTrap trap = deployment.getCameraTrap();
                            deployments.add(deployment);
                            fileName += Util.getObjectId(evt, trap);

                        } else {
                            Util.alertErrorPopup(
                                    pane.getLanguage().getString("title_error"),
                                    pane.getLanguage().getString("wild_id_error_header"),
                                    pane.getLanguage().getString("wild_id_error_context") + ": " + pane.getLanguage().getString("error_zipped_select_wrong_node"),
                                    pane.getLanguage().getString("alert_ok"));

                            return;
                        }

                        //exportDeploymentsAsZip(deployments, fileName);
                        saveTransferFile(deployments, fileName);

                    } else if (id.equals("open_transfer_file_in_new_project")) {
                        openTransferFileInNewProject();
                    } else if (id.equals("open_transfer_file_in_current_project")) {

                        TreeItem selectedTreeItem = (TreeItem) this.pane.getNavigationPane().getImageTree().getRoot();
                        if (selectedTreeItem instanceof ProjectListTreeItem) {
                            Util.alertErrorPopup(
                                    pane.getLanguage().getString("title_error"),
                                    pane.getLanguage().getString("wild_id_error_header"),
                                    pane.getLanguage().getString("wild_id_error_context") + ": " + pane.getLanguage().getString("open_transfer_file_in_current_project_no_project_error"),
                                    pane.getLanguage().getString("alert_ok"));
                        } else {

                            while (!(selectedTreeItem instanceof ProjectTreeItem)) {
                                selectedTreeItem = selectedTreeItem.getParent();
                            }

                            openTransferFileInCurrentProject(((ProjectTreeItem) selectedTreeItem).getProject());
                        }
                    } else if (id.equals("menu_language_english")) {
                        this.pane.setLanguage(new LanguageModel("en"));
                    } else if (id.equals("menu_language_spanish")) {
                        this.pane.setLanguage(new LanguageModel("es"));
                    } else if (id.equals("menu_language_chinese")) {
                        this.pane.setLanguage(new LanguageModel("cn"));
                    } else if (id.equals("menu_language_portuguese")) {
                        this.pane.setLanguage(new LanguageModel("pt"));
                    } else if (id.equals("menuExport_toCSV_with_images")) {
                        exportMetadata("csv", true);
                    } else if (id.equals("menuExport_toCSV_without_images")) {
                        exportMetadata("csv", false);
                    } else if (id.equals("menuExport_toExcel_with_images")) {
                        exportMetadata("excel", true);
                    } else if (id.equals("menuExport_toExcel_without_images")) {
                        exportMetadata("excel", false);
                    } else if (id.equals("menuExport_toWCS_with_images")) {
                        exportMetadata("wcs", true);
                    } else if (id.equals("menuExport_toWCS_without_images")) {
                        exportMetadata("wcs", false);
                    } else if (id.equals("menuViewTable_same_window")) {
                        exportMetadata("table", false);
                    } else if (id.equals("menuViewTable_new_window")) {
                        exportMetadata("table", true);
                    } else if (id.equals("menu_about")) {
                        aboutWildID();
                    } else if (id.equals("menu_license")) {
                        licenseWildID();
                    } else if (id.startsWith("annotate_images_")) {
                        int projectId = Integer.valueOf(id.substring(16));
                        setProjectAnnotationPane(projectId);
                    } else if (id.startsWith("edit_project_")) {
                        int projectId = Integer.valueOf(id.substring(13));
                        setEditProjectPane(projectId);
                    } else if (id.equals("menuHelp_checkUpdate")) {
                        checkUpdate();
                    } else if (id.equals("menuHelp_contactUs")) {
                        contactUs();
                    } else if (id.equals("menuHelp_viewLog")) {
                        reportError();
                    } else if (id.equals("menuWildID_registration")) {
                        register();
                    } else if (id.equals("menuHelp_onlineHelp")) {
                        Util.openBrowser(WildID.WILDID_SERVER + "help.jsp");
                    } else if (id.equals("menuPlugin_managePlugins")) {
                        managePlugin();
                    }
                }
            } else if (object instanceof Node) {
                Node node = (Node) object;
                String id = node.getId();

                if (id != null) {
                    log.info("Action " + id);

                    if (id.equals("new_project_save")) {
                        createNewProject((ProjectNewPane) (pane.getProjectDataPane()));
                    } else if (id.equals("edit_project_save")) {
                        updateProject((ProjectEditPane) (pane.getProjectDataPane()));
                    } else if (id.equals("edit_project_delete")) {
                        deleteProject((ProjectEditPane) (pane.getProjectDataPane()));
                    } else if (id.equals("new_person_save")) {
                        createNewPersonInProject((ProjectNewPersonPane) (pane.getProjectDataPane()));
                    } else if (id.equals("new_selected_person_save")) {
                        addPersonToProject((ProjectNewPersonPane) (pane.getProjectDataPane()));
                    } else if (id.equals("edit_user_save")) {
                        updatePersonInProject((ProjectEditPersonPane) (pane.getProjectDataPane()));
                    } else if (id.equals("edit_user_delete")) {
                        deletePersonFromProject((ProjectEditPersonPane) (pane.getProjectDataPane()));
                    } else if (id.equals("new_org_save")) {
                        createNewOrganizationInProject((ProjectNewOrganizationPane) (pane.getProjectDataPane()));
                    } else if (id.equals("edit_org_save")) {
                        updateOrganizationInProject((ProjectEditOrganizationPane) (pane.getProjectDataPane()));
                    } else if (id.equals("edit_org_delete")) {
                        deleteOrganizationFromProject((ProjectEditOrganizationPane) (pane.getProjectDataPane()));
                    } else if (id.equals("new_selected_org_save")) {
                        addOrganizationToProject((ProjectNewOrganizationPane) (pane.getProjectDataPane()));
                    } else if (id.equals("person_new_pane_save_button")) {
                        createNewPerson((PersonNewPane) (pane.getPersonDataPane()));
                    } else if (id.equals("person_edit_pane_save_button")) {
                        updatePerson((PersonEditPane) (pane.getPersonDataPane()));
                    } else if (id.equals("person_edit_pane_delete_button")) {
                        deletePerson((PersonEditPane) (pane.getPersonDataPane()));
                    } else if (id.equals("org_new_pane_save_button")) {
                        createNewOrganization((OrganizationNewPane) (pane.getOrganizationDataPane()));
                    } else if (id.equals("org_edit_pane_save_button")) {
                        updateOrganization((OrganizationEditPane) (pane.getOrganizationDataPane()));
                    } else if (id.equals("org_edit_pane_delete_button")) {
                        deleteOrganization((OrganizationEditPane) (pane.getOrganizationDataPane()));
                    } else if (id.equals("project_camera_pane_save_button")) {
                        createNewCamera((ProjectCameraPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_camera_pane_update_button")) {
                        updateCamera((ProjectCameraPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_camera_pane_delete_button")) {
                        deleteCamera((ProjectCameraPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_array_pane_save_button")) {
                        createNewCameraTrapArray((ProjectCameraTrapArrayPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_array_pane_bulk_upload_button")) {
                        bulkUploadCTTrapsInMultipleArrays((ProjectCameraTrapArrayPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_array_pane_delete_button")) {
                        deleteCameraTrapArray((ProjectCameraTrapArrayPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_array_pane_update_button")) {
                        updateCameraTrapArray((ProjectCameraTrapArrayPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_trap_pane_array_update_button")) {
                        updateCameraTrapArray((ProjectCameraTrapPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_trap_pane_array_upload_button")) {
                        bulkUploadCameraTrapsInSingleArray((ProjectCameraTrapPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_trap_pane_array_delete_button")) {
                        deleteCameraTrapArray((ProjectCameraTrapPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_trap_pane_trap_save_button")) {
                        createNewCameraTrap((ProjectCameraTrapPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_trap_pane_trap_update_button")) {
                        updateCameraTrap((ProjectCameraTrapPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_trap_pane_trap_delete_button")) {
                        deleteCameraTrap((ProjectCameraTrapPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_trap_pane_cancel_trap_edit_button")) {
                        cancelCameraTrapEditing((ProjectCameraTrapPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_event_pane_create_event_button")) {
                        createEvent((ProjectEventPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_event_pane_delete_button")) {
                        deleteEvent((ProjectEventPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_event_pane_update_event_button")) {
                        updateEvent((ProjectEventPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_deployment_pane_create_button")) {
                        createDeployment((ProjectNewDeploymentPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_deployment_pane_update_button")) {
                        updateDeployment((ProjectEditDeploymentPane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_deployment_pane_delete_button")) {
                        deleteDeployment((ProjectEditDeploymentPane) (pane.getProjectDataPane()));
                    } else if (id.equals("camera_model_exif_pane_save_button")) {
                        saveExifFeature((CameraModelExifPane) (pane.getCameraModelDataPane()));
                    } else if (id.equals("camera_model_exif_pane_delete_button")) {
                        deleteExifFeature((CameraModelExifPane) (pane.getCameraModelDataPane()));
                    } else if (id.equals("camera_model_new_pane_save_button")) {
                        createCameraModel((CameraModelNewPane) (pane.getCameraModelDataPane()));
                    } else if (id.equals("camera_model_new_pane_update_button")) {
                        updateCameraModel((CameraModelNewPane) (pane.getCameraModelDataPane()));
                    } else if (id.equals("camera_model_new_pane_delete_button")) {
                        deleteCameraModel((CameraModelNewPane) (pane.getCameraModelDataPane()));
                    } else if (id.equals("project_anno_pane_save_button")) {
                        saveImageAnnotation((ProjectImagePane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_anno_pane_update_button")) {
                        updateImageAnnotation((ProjectImagePane) (pane.getProjectDataPane()));
                    } else if (id.equals("project_group_pane_remove_button")) {
                        updateImageGroupEditor((ProjectImagePane) (pane.getProjectDataPane()));
                    } else if (id.equals("preference_save_button")) {
                        updatePreference((PreferencePane) pane.getProjectDataPane());
                    } else if (id.equals("hyperlink_license")) {
                        licenseWildID();
                    } else if (id.equals("update_wild_id")) {
                        updateWildID();
                    } else if (id.equals("search_button")) {
                        search((SearchPane) pane.getProjectDataPane());
                    } else if (id.equals("submit_log_button")) {
                        ReportErrorPane reportPane = (ReportErrorPane) dialog.getScene().lookup("#ReportError");
                        reportError(reportPane);
                    } else if (id.equals("submit_send_mail_button")) {
                        ContactUsPane contactUsPane = (ContactUsPane) dialog.getScene().lookup("#ContactUs");
                        contactUs(contactUsPane);
                    } else if (id.startsWith("jump_to_group_")) {
                        int image_sequence_id = Integer.valueOf(id.substring(14));
                        ImageService imageService = new ImageServiceImpl();
                        Image img = imageService.getFirstImageInSequence(image_sequence_id);
                        WildIDImageTree imageTree = this.pane.getNavigationPane().getImageTree();
                        ProjectImageTreeItem imgTreeItem = imageTree.getProjectImageTreeItem(img);

                        imageTree.getSelectionModel().select(imgTreeItem);
                        imageTree.scrollTo(imageTree.getSelectionModel().getSelectedIndex());
                    } else if (id.equals("registration_submit_button")) {
                        submitRegistration();
                    } else if (id.startsWith("plugin_update_")) {
                        plugin_update(id.substring(14));
                    } else if (id.startsWith("plugin_install_")) {
                        plugin_install(id.substring(15));
                    } else if (id.startsWith("plugin_uninstall_")) {
                        plugin_uninstall(id.substring(17));
                    } else if (id.equals("image_individual_save_btn")) {
                        saveImageIndividual();
                    } else if (id.equals("image_individual_update_btn")) {
                        updateImageIndividual();
                    } else if (id.equals("image_individual_delete_btn")) {
                        deleteImageIndividual();
                    }
                }
            }

        } catch (Exception ex) {
            if (ex instanceof PluginException) {
                PluginException pe = (PluginException) ex;
                String msg = pe.getMessage(pane.getLanguage().getLanguageCode());
                log.error("ERROR: " + msg);

                Util.alertErrorPopup(
                        pane.getLanguage().getString("plugin_error_title"),
                        pane.getLanguage().getString("plugin_error_header"),
                        msg,
                        pane.getLanguage().getString("alert_ok"));
            } else {
                //ex.printStackTrace();
                log.error("System Internal Error", ex);
                this.pane.showErrorMessage(ex);
            }
        }

    }

// this method controls the behavior of the trees
    @Override
    public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> old_val, TreeItem<String> new_val) {

        try {
            TreeItem<String> selectedItem = new_val;
            LanguageModel language = pane.getLanguage();

            if (selectedItem != null) {
                log.info("Select TreeItem: " + selectedItem.toString());
            }

            if (selectedItem instanceof ProjectListTreeItem) {

                ProjectNewPane newProjectPane = new ProjectNewPane(language);
                pane.setProjectDataPane(newProjectPane);
                pane.getStatusPane().write(pane.getLanguage().getString("project_new_pane_title"));

            } else if (selectedItem instanceof ProjectTreeItem) {

                ProjectTreeItem projectTreeItem = (ProjectTreeItem) selectedItem;
                if (projectTreeItem.getParent() == null) {
                    return;
                }
                Project project = projectTreeItem.getProject();
                ProjectEditPane editProjectPane = new ProjectEditPane(language, project);
                pane.setProjectDataPane(editProjectPane);
                pane.getStatusPane().write(pane.getLanguage().getString("project_edit_pane_title"));

            } else if (selectedItem instanceof ProjectPersonListTreeItem) {

                ProjectPersonListTreeItem projectPersonTreeItem = (ProjectPersonListTreeItem) selectedItem;
                Project project = projectPersonTreeItem.getProject();

                // get all non-member persons
                PersonService personService = new PersonServiceImpl();
                List<Person> nonMembers = personService.getNonMembers(project);

                // get all roles
                RoleService roleService = new RoleServiceImpl();
                List<Role> roles = roleService.listRole();

                ProjectNewPersonPane newPersonPane = new ProjectNewPersonPane(language, project, nonMembers, roles);
                pane.setProjectDataPane(newPersonPane);
                pane.getStatusPane().write(pane.getLanguage().getString("person_new_pane_title"));

            } else if (selectedItem instanceof ProjectPersonTreeItem) {

                ProjectPersonTreeItem projectPersonTreeItem = (ProjectPersonTreeItem) selectedItem;
                Person person = projectPersonTreeItem.getPerson();
                Project project = projectPersonTreeItem.getProject();

                RoleService roleService = new RoleServiceImpl();
                List<Role> roles = roleService.listRole();

                ProjectEditPersonPane editPersonPane = new ProjectEditPersonPane(language, person, project, roles);
                pane.setProjectDataPane(editPersonPane);
                pane.getStatusPane().write(pane.getLanguage().getString("person_edit_pane_title"));

            } else if (selectedItem instanceof ProjectOrganizationListTreeItem) {

                ProjectOrganizationListTreeItem orgTreeItem = (ProjectOrganizationListTreeItem) selectedItem;
                Project project = orgTreeItem.getProject();

                OrganizationService orgService = new OrganizationServiceImpl();
                List<Organization> nonMembers = orgService.getNonMemberOrganizations(project);

                ProjectNewOrganizationPane newInstitutionPane = new ProjectNewOrganizationPane(language, project, nonMembers);
                pane.setProjectDataPane(newInstitutionPane);
                pane.getStatusPane().write(pane.getLanguage().getString("org_new_pane_title"));

            } else if (selectedItem instanceof ProjectOrganizationTreeItem) {

                ProjectOrganizationTreeItem orgTreeItem = (ProjectOrganizationTreeItem) selectedItem;
                Organization org = orgTreeItem.getOrganization();
                Project project = orgTreeItem.getProject();

                ProjectEditOrganizationPane editOrganizationPane = new ProjectEditOrganizationPane(language, org, project);
                pane.setProjectDataPane(editOrganizationPane);
                pane.getStatusPane().write(pane.getLanguage().getString("org_edit_pane_title"));

            } else if (selectedItem instanceof PersonListTreeItem) {

                //PersonListTreeItem personListTreeItem = (PersonListTreeItem) selectedItem;
                PersonNewPane personNewPane = new PersonNewPane(language);
                pane.setPersonDataPane(personNewPane);
                pane.getStatusPane().write(pane.getLanguage().getString("person_new_pane_title"));

            } else if (selectedItem instanceof PersonTreeItem) {

                PersonTreeItem personTreeItem = (PersonTreeItem) selectedItem;
                Person person = personTreeItem.getPerson();

                PersonEditPane personEditPane = new PersonEditPane(language, person);
                pane.setPersonDataPane(personEditPane);
                pane.getStatusPane().write(pane.getLanguage().getString("person_edit_pane_title"));

            } else if (selectedItem instanceof OrganizationListTreeItem) {

                //OrganizationListTreeItem orgListTreeItem = (OrganizationListTreeItem) selectedItem;
                OrganizationNewPane orgNewPane = new OrganizationNewPane(language);
                pane.setOrganizationDataPane(orgNewPane);
                pane.getStatusPane().write(pane.getLanguage().getString("org_new_pane_title"));

            } else if (selectedItem instanceof OrganizationTreeItem) {

                OrganizationTreeItem orgTreeItem = (OrganizationTreeItem) selectedItem;
                Organization org = orgTreeItem.getOrganization();
                OrganizationEditPane orgEditPane = new OrganizationEditPane(language, org);
                pane.setOrganizationDataPane(orgEditPane);
                pane.getStatusPane().write(pane.getLanguage().getString("org_edit_pane_title"));

            } else if (selectedItem instanceof ProjectCameraListTreeItem) {

                ProjectCameraListTreeItem camerasTreeList = (ProjectCameraListTreeItem) selectedItem;
                Project project = camerasTreeList.getProject();
                CameraModelService cameraModelService = new CameraModelServiceImpl();
                List<CameraModel> cameraModels = cameraModelService.listCameraModel();
                ProjectCameraPane cameraPane = new ProjectCameraPane(language, cameraModels, project);
                pane.setProjectDataPane(cameraPane);
                pane.getStatusPane().write(pane.getLanguage().getString("project_camera_pane_title") + ": " + project.getName());

            } else if (selectedItem instanceof ProjectCameraTrapArrayListTreeItem) {

                ProjectCameraTrapArrayListTreeItem arraysTreeItem = (ProjectCameraTrapArrayListTreeItem) selectedItem;
                Project project = arraysTreeItem.getProject();
                ProjectCameraTrapArrayPane arrayPane = new ProjectCameraTrapArrayPane(language, project);
                pane.setProjectDataPane(arrayPane);
                pane.getStatusPane().write(pane.getLanguage().getString("project_array_pane_title"));

            } else if (selectedItem instanceof ProjectCameraTrapArrayTreeItem) {

                ProjectCameraTrapArrayTreeItem arrayTreeItem = (ProjectCameraTrapArrayTreeItem) selectedItem;
                CameraTrapArray array = arrayTreeItem.getCameraTrapArray();

                ProjectCameraTrapPane trapPane = new ProjectCameraTrapPane(language, array);
                pane.setProjectDataPane(trapPane);
                pane.getStatusPane().write(
                        pane.getLanguage().getString("project_trap_pane_edit_array_title") + ": " + array.getName() + " | "
                        + pane.getLanguage().getString("project_trap_pane_create_trap_title")
                );

            } else if (selectedItem instanceof ProjectCameraTrapTreeItem) {

                ProjectCameraTrapTreeItem trapTreeItem = (ProjectCameraTrapTreeItem) selectedItem;
                CameraTrap trap = trapTreeItem.getCameraTrap();
                CameraTrapArray array = trap.getCameraTrapArray();

                ProjectCameraTrapPane trapPane = new ProjectCameraTrapPane(language, trap);
                pane.setProjectDataPane(trapPane);
                pane.getStatusPane().write(
                        pane.getLanguage().getString("project_trap_pane_edit_array_title") + ": " + array.getName() + " | "
                        + pane.getLanguage().getString("project_trap_pane_trap_tab_title") + ": " + trap.getName()
                );

            } else if (selectedItem instanceof ProjectEventListTreeItem) {

                ProjectEventListTreeItem eventListTreeItem = (ProjectEventListTreeItem) selectedItem;
                Project project = eventListTreeItem.getProject();
                ProjectEventPane eventPane = new ProjectEventPane(language, project);
                pane.setProjectDataPane(eventPane);
                pane.getStatusPane().write(pane.getLanguage().getString("project_event_pane_create_event_title") + ": " + project.getName());

            } else if (selectedItem instanceof ProjectEventTreeItem) {

                ProjectEventTreeItem eventTreeItem = (ProjectEventTreeItem) selectedItem;
                Event event = eventTreeItem.getEvent();
                ProjectEventPane eventPane = new ProjectEventPane(language, event);
                pane.setProjectDataPane(eventPane);
                pane.getStatusPane().write(pane.getLanguage().getString("project_event_pane_edit_event_title") + ": " + event.getProject().getName());

            } else if (selectedItem instanceof ProjectImageListTreeItem) {

                ProjectImageListTreeItem imageListTreeItem = (ProjectImageListTreeItem) selectedItem;
                Project project = imageListTreeItem.getProject();

                ProjectService projectService = new ProjectServiceImpl();
                project = projectService.loadProjectWithDeployments(project.getProjectId());
                Map<Event, List<CameraTrap>> event2trap = projectService.getUnfinishedEventToTrapMap(project);
                Map<Event, Map<CameraTrap, List<Camera>>> event2trap2camera = projectService.getEventToTrapToCameraMap(project);

                List<Person> people = projectService.getPersons(project);

                BaitTypeService baitService = new BaitTypeServiceImpl();
                List<BaitType> baitTypes = baitService.listBaitType();

                FeatureTypeService featureService = new FeatureTypeServiceImpl();
                List<FeatureType> featureTypes = featureService.listFeatureType();

                FailureTypeService failureService = new FailureTypeServiceImpl();
                List<FailureType> failureTypes = failureService.listFailureType();

                ProjectNewDeploymentPane deploymentPane
                        = new ProjectNewDeploymentPane(language, project, event2trap, event2trap2camera, people, baitTypes, featureTypes, failureTypes);
                pane.setProjectDataPane(deploymentPane);
                pane.getStatusPane().write(pane.getLanguage().getString("project_deployment_pane_title") + ": " + project.getName());

            } else if (selectedItem instanceof ProjectImageDeploymentTreeItem) {

                ProjectImageDeploymentTreeItem deploymentTreeItem = (ProjectImageDeploymentTreeItem) selectedItem;
                Deployment deployment = deploymentTreeItem.getDeployment();
                //Project project = deploymentTreeItem.getProject();
                Project project = deployment.getCameraTrap().getCameraTrapArray().getProject();

                ProjectService projectService = new ProjectServiceImpl();
                project = projectService.loadProjectWithDeployments(project.getProjectId());
                Map<Event, List<CameraTrap>> event2trap = projectService.getUnfinishedEventToTrapMap(project);
                Map<Event, Map<CameraTrap, List<Camera>>> event2trap2camera = projectService.getEventToTrapToCameraMap(project);
                List<Person> people = projectService.getPersons(project);

                BaitTypeService baitService = new BaitTypeServiceImpl();
                List<BaitType> baitTypes = baitService.listBaitType();

                FeatureTypeService featureService = new FeatureTypeServiceImpl();
                List<FeatureType> featureTypes = featureService.listFeatureType();

                FailureTypeService failureService = new FailureTypeServiceImpl();
                List<FailureType> failureTypes = failureService.listFailureType();

                // add the camera trap of this deployment
                List<CameraTrap> traps = new ArrayList<>();
                Event evt = deployment.getEvent();
                for (Event event : event2trap.keySet()) {
                    if (event.getEventId().intValue() == deployment.getEvent().getEventId()) {
                        evt = event;
                        traps = event2trap.get(event);
                        break;
                    }
                }
                traps.add(deployment.getCameraTrap());
                event2trap.put(evt, traps);

                // add the camera of this deployment for all traps in the same array
                List<Camera> cameras = new ArrayList<>();
                Map<Event, List<Camera>> event2camera = projectService.getEventToUnusedCameraMap(project);
                for (Event event : event2camera.keySet()) {
                    if (event.getEventId().intValue() == deployment.getEvent().getEventId()) {
                        cameras = event2camera.get(event);
                    }
                }
                cameras.add(deployment.getCamera());   // includes all cameras can be used for this array

                Map<CameraTrap, List<Camera>> trap2camera = new HashMap<>();
                for (Event event : event2trap2camera.keySet()) {
                    if (event.getEventId().intValue() == deployment.getEvent().getEventId()) {
                        trap2camera = event2trap2camera.get(event);
                        break;
                    }
                }
                for (CameraTrap trap : trap2camera.keySet()) {
                    trap2camera.put(trap, cameras);
                }
                trap2camera.put(deployment.getCameraTrap(), cameras);
                event2trap2camera.put(evt, trap2camera);
                ProjectEditDeploymentPane deploymentPane
                        = new ProjectEditDeploymentPane(language, deployment, project, event2trap, event2trap2camera, people, baitTypes, featureTypes, failureTypes);
                pane.setProjectDataPane(deploymentPane);
                pane.getStatusPane().write(pane.getLanguage().getString("project_deployment_pane_edit_title") + ": " + project.getName());

            } else if (selectedItem instanceof ProjectImageTreeItem) {

                ProjectImageTreeItem imageTreeItem = (ProjectImageTreeItem) selectedItem;
                Project project = imageTreeItem.getProject();
                Image image = imageTreeItem.getImage();

                ProjectService projectService = new ProjectServiceImpl();
                List<Person> people = projectService.getPersons(project);

                // load the image again for the lazy parts
                ImageService imageService = new ImageServiceImpl();
                image = imageService.getImage(image.getImageId());

                ImageTypeService imageTypeService = new ImageTypeServiceImpl();
                List<ImageType> imageTypes = imageTypeService.listImageType();

                ImageUncertaintyTypeService imageUncertaintyTypeService = new ImageUncertaintyTypeServiceImpl();
                List<ImageUncertaintyType> imageUncertaintyTypes = imageUncertaintyTypeService.listImageUncertaintyType();

                TaxonomyService taxaService = new TaxonomyServiceImpl();
                List<HomoSapiensType> sapiensTypes = taxaService.listHomoSapiensType();

                if (pane.getProjectDataPane().getId() != null && pane.getProjectDataPane().getId().equals("ProjectImagePane")) {
                    ProjectImagePane previousImagePane = (ProjectImagePane) pane.getProjectDataPane();
                    ProjectImagePane imagePane = new ProjectImagePane(language, image, people, imageTypes, imageUncertaintyTypes, sapiensTypes, previousImagePane.getSelectedTab());
                    pane.setProjectDataPane(imagePane);
                } else {
                    ProjectImagePane imagePane = new ProjectImagePane(language, image, people, imageTypes, imageUncertaintyTypes, sapiensTypes);
                    pane.setProjectDataPane(imagePane);
                }
            } else if (selectedItem instanceof SearchResultImageTreeItem) {

                SearchResultImageTreeItem imageTreeItem = (SearchResultImageTreeItem) selectedItem;
                Project project = imageTreeItem.getProject();
                Image image = imageTreeItem.getImage();

                ProjectService projectService = new ProjectServiceImpl();
                List<Person> people = projectService.getPersons(project);

                // load the image again for the lazy parts
                ImageService imageService = new ImageServiceImpl();
                image = imageService.getImage(image.getImageId());

                ImageTypeService imageTypeService = new ImageTypeServiceImpl();
                List<ImageType> imageTypes = imageTypeService.listImageType();

                ImageUncertaintyTypeService imageUncertaintyTypeService = new ImageUncertaintyTypeServiceImpl();
                List<ImageUncertaintyType> imageUncertaintyTypes = imageUncertaintyTypeService.listImageUncertaintyType();

                TaxonomyService taxaService = new TaxonomyServiceImpl();
                List<HomoSapiensType> sapiensTypes = taxaService.listHomoSapiensType();

                if (pane.getProjectDataPane().getId() != null && pane.getProjectDataPane().getId().equals("ProjectImagePane")) {
                    ProjectImagePane previousImagePane = (ProjectImagePane) pane.getProjectDataPane();
                    ProjectImagePane imagePane = new ProjectImagePane(language, image, people, imageTypes, imageUncertaintyTypes, sapiensTypes, previousImagePane.getSelectedTab());
                    pane.setProjectDataPane(imagePane);
                } else {
                    ProjectImagePane imagePane = new ProjectImagePane(language, image, people, imageTypes, imageUncertaintyTypes, sapiensTypes);
                    pane.setProjectDataPane(imagePane);
                }

            } else if (selectedItem instanceof SearchResultTreeItem) {

                SearchResultTreeItem searchResultItem = (SearchResultTreeItem) selectedItem;
                SearchPane searchPane = new SearchPane(this.pane.getLanguage(),
                        searchResultItem.getProjects(),
                        searchResultItem.getStartDate(),
                        searchResultItem.getEndDate(),
                        searchResultItem.getImageType(),
                        searchResultItem.getGenus(),
                        searchResultItem.getSpecies());
                this.pane.setProjectDataPane(searchPane);

            } else if (selectedItem instanceof CameraModelMakerModelTreeItem) {

                CameraModelMakerModelTreeItem modelTreeItem = (CameraModelMakerModelTreeItem) selectedItem;
                CameraModel cameraModel = modelTreeItem.getCameraModel();

                // load the image features
                ImageFeatureService imageFeatureService = new ImageFeatureServiceImpl();
                List<ImageFeature> imageFeatures = imageFeatureService.listImageFeature();

                CameraModelExifPane modelExifPane = new CameraModelExifPane(language, cameraModel, imageFeatures);
                pane.setCameraModelDataPane(modelExifPane);

            } else if (selectedItem instanceof CameraModelListTreeItem) {

                CameraModelService cameraModelService = new CameraModelServiceImpl();
                List<CameraModel> cameraModels = cameraModelService.listCameraModel();
                CameraModelNewPane cameraModelDataPane = new CameraModelNewPane(language, cameraModels);
                pane.setCameraModelDataPane(cameraModelDataPane);

            } else if (selectedItem instanceof CameraModelMakerTreeItem) {

                CameraModelMakerTreeItem makerTreeItem = (CameraModelMakerTreeItem) selectedItem;
                String maker = makerTreeItem.getMaker();

                CameraModelService cameraModelService = new CameraModelServiceImpl();
                List<CameraModel> cameraModels = cameraModelService.listCameraModel();
                CameraModelNewPane cameraModelDataPane = new CameraModelNewPane(language, cameraModels, maker);
                pane.setCameraModelDataPane(cameraModelDataPane);

            }

        } catch (Exception ex) {
            //ex.printStackTrace();
            log.error("System Internal Error", ex);
            this.pane.showErrorMessage(ex);
        }

    }

    private void loadLastOpenedProject(Project proj) {
        this.pane.getNavigationPane().setImagePane(proj.getProjectId(), this);
        this.pane.getNavigationPane().getImageTree().getSelectionModel().select(0);
        this.pane.getNavigationPane().getImageTree().expandProjectTreeUpToLevel(3);

        System.out.println("Load last opened proj = " + proj.getName());

        ProjectService projectService = new ProjectServiceImpl();
        BaitTypeService baitService = new BaitTypeServiceImpl();
        FeatureTypeService featureService = new FeatureTypeServiceImpl();
        FailureTypeService failureService = new FailureTypeServiceImpl();

        Project project = projectService.loadProjectWithDeployments(proj.getProjectId());
        Map<Event, List<CameraTrap>> event2trap = projectService.getUnfinishedEventToTrapMap(project);
        Map<Event, Map<CameraTrap, List<Camera>>> event2trap2camera = projectService.getEventToTrapToCameraMap(project);
        List<Person> people = projectService.getPersons(project);
        List<BaitType> baitTypes = baitService.listBaitType();
        List<FeatureType> featureTypes = featureService.listFeatureType();
        List<FailureType> failureTypes = failureService.listFailureType();

        ProjectNewDeploymentPane deploymentPane
                = new ProjectNewDeploymentPane(pane.getLanguage(), project, event2trap, event2trap2camera, people, baitTypes, featureTypes, failureTypes);
        this.pane.setProjectDataPane(deploymentPane);
    }

    private void createNewProject(ProjectNewPane newProjectPane) {

        if (newProjectPane.validate(projects)) {
            // create a project in the database
            Project project = newProjectPane.getProject();
            ProjectService projectService = new ProjectServiceImpl();
            projectService.addProject(project);
            this.pane.addNewProject(project);

            // create a default event
            Event event = new Event();
            event.setName(this.pane.getLanguage().getString("project_event_default_name"));
            event.setProject(project);
            EventService eventService = new EventServiceImpl();
            eventService.addEvent(event);
            project.getEvents().add(event);
            this.pane.createEvent(event);
            WildIDProjectTree projectTree = this.pane.getNavigationPane().getProjectTree();
            projectTree.getSelectionModel().select(null);

            // create a default camera trap array
            CameraTrapArray array = new CameraTrapArray();
            array.setName(this.pane.getLanguage().getString("project_array_default_prefix") + "-" + project.getAbbrevName() + "-1");
            array.setProject(project);
            CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
            arrayService.addCameraTrapArray(array);
            project.getCameraTrapArrays().add(array);
            this.pane.createNewCameraTrapArray(array);
            projectTree.getSelectionModel().select(null);

            // make sure the project is selected on the navigation tree         
            projectTree.getSelectionModel().select(projectTree.getProjectTreeItem(project));

        }
    }

    private void updateProject(ProjectEditPane editProjectPane) {

        if (editProjectPane.validate(projects)) {

            //update the project in the database
            Project project = editProjectPane.getProject();
            ProjectService projectService = new ProjectServiceImpl();
            projectService.updateProject(project);

            this.pane.updateProject(project);
        }
    }

    private void deleteProject(ProjectEditPane editProjectPane) {

        Project project = editProjectPane.getProject();
        LanguageModel language = editProjectPane.getLanguage();

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("project_delete_confirm_header"),
                language.getString("project_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            ProjectService projectService = new ProjectServiceImpl();
            projectService.removeProject(project.getProjectId());
            projects.remove(project);

            // notify the main pane to change
            this.pane.removeProject(project);
        }
    }

    private void createNewPersonInProject(ProjectNewPersonPane newPersonPane) {

        if (newPersonPane.validate(persons)) {
            Person person = newPersonPane.getPerson();
            PersonService personService = new PersonServiceImpl();
            personService.addPerson(person);

            this.addNewPerson(person);

            // add all project_person_roles into the project
            Project project = newPersonPane.getProject();
            project.getProjectPersonRoles().addAll(person.getProjectPersonRoles());

            // notify the main pane to change
            this.pane.addNewPerson(project, person);
        }

    }

    private void addPersonToProject(ProjectNewPersonPane newPersonPane) {

        if (newPersonPane.validateSelectedPerson()) {
            List<ProjectPersonRole> pprs = newPersonPane.createNewProjectPersonRolesForSelectedPerson();
            PersonService personService = new PersonServiceImpl();
            personService.addProjectPersonRoles(pprs);

            // add all project_person_roles into the project
            Project project = newPersonPane.getProject();
            project.getProjectPersonRoles().addAll(pprs);

            Person person = newPersonPane.getSelectedPerson();
            person.getProjectPersonRoles().addAll(pprs);

            // notify the main pane to change
            this.pane.addSelectedPerson(project, person);
        }

    }

    private void updatePersonInProject(ProjectEditPersonPane editPersonPane) {

        if (editPersonPane.validate(persons)) {

            //update the person in the database
            Person person = editPersonPane.getOriginalPerson();
            Project project = editPersonPane.getProject();

            PersonService personService = new PersonServiceImpl();
            List<ProjectPersonRole> oldRoles = personService.getProjectPersonRoles(person, project);
            project.getProjectPersonRoles().removeAll(oldRoles);

            person = editPersonPane.getPerson();
            List<ProjectPersonRole> newRoles = personService.getProjectPersonRoles(person, project);
            project.getProjectPersonRoles().addAll(newRoles);

            personService.updatePerson(person);
            personService.removeProjectPersonRoles(oldRoles);
            personService.addProjectPersonRoles(newRoles);

            // notify the main pane to change
            this.pane.updatePerson(project, person);
        }
    }

    private void deletePersonFromProject(ProjectEditPersonPane editPersonPane) {

        Project project = editPersonPane.getProject();
        Person person = editPersonPane.getOriginalPerson();
        LanguageModel language = editPersonPane.getLanguage();

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("person_delete_confirm_header"),
                language.getString("person_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            PersonService personService = new PersonServiceImpl();
            List<ProjectPersonRole> pprs = personService.getProjectPersonRoles(person, project);
            personService.removeProjectPersonRoles(pprs);

            project.getProjectPersonRoles().removeAll(pprs);
            person.getProjectPersonRoles().removeAll(pprs);

            // notify the main pane to change
            this.pane.removePerson(project, person);
        }
    }

    private void createNewOrganizationInProject(ProjectNewOrganizationPane newOrganizationPane) {

        if (newOrganizationPane.validate(orgs)) {

            Organization org = newOrganizationPane.getOrganization();
            OrganizationService orgService = new OrganizationServiceImpl();
            orgService.addOrganization(org);

            Project project = newOrganizationPane.getProject();
            ProjectOrganizationId poid = new ProjectOrganizationId(project.getProjectId(), org.getOrganizationId());
            ProjectOrganization po = new ProjectOrganization(poid, org, project);

            ProjectService projectService = new ProjectServiceImpl();
            projectService.addProjectOrganization(po);

            // add po into the project and org
            Set pos = project.getProjectOrganizations();
            if (pos != null) {
                pos.add(po);
            }
            org.getProjectOrganizations().add(po);

            // notify the main pane to change
            this.pane.addNewOrganization(project, org);
        }
    }

    private void updateOrganizationInProject(ProjectEditOrganizationPane editOrganizationPane) {

        if (editOrganizationPane.validate(orgs)) {

            //update the org in the database
            Organization org = editOrganizationPane.getOrganization();
            Project project = editOrganizationPane.getProject();

            OrganizationService orgService = new OrganizationServiceImpl();
            orgService.updateOrganization(org);

            // notify the main pane to change
            this.pane.updateOrganization(project, org);
        }
    }

    private void deleteOrganizationFromProject(ProjectEditOrganizationPane editOrganizationPane) {

        Project project = editOrganizationPane.getProject();
        Organization org = editOrganizationPane.getOriginalOrganization();
        LanguageModel language = editOrganizationPane.getLanguage();

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("org_delete_confirm_header"),
                language.getString("org_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            OrganizationService orgService = new OrganizationServiceImpl();
            ProjectOrganization po = orgService.getProjectOrganization(org, project);
            orgService.removeProjectOrganization(po);

            // remove po from project association
            for (ProjectOrganization apo : project.getProjectOrganizations()) {
                if (apo.getProject().getProjectId().intValue() == project.getProjectId().intValue()
                        && apo.getOrganization().getOrganizationId().intValue() == org.getOrganizationId().intValue()) {
                    project.getProjectOrganizations().remove(apo);
                    break;
                }
            }

            // remove po from organization association
            for (ProjectOrganization apo : org.getProjectOrganizations()) {
                if (apo.getProject().getProjectId().intValue() == project.getProjectId().intValue()
                        && apo.getOrganization().getOrganizationId().intValue() == org.getOrganizationId().intValue()) {
                    org.getProjectOrganizations().remove(apo);
                    break;
                }
            }

            // notify the main pane to change
            this.pane.removeOrganization(project, org);
        }
    }

    private void addOrganizationToProject(ProjectNewOrganizationPane newOrganizationPane) {

        if (newOrganizationPane.validateSelectedOrganization()) {

            Project project = newOrganizationPane.getProject();
            Organization org = newOrganizationPane.getSelectedOrganization();

            ProjectOrganizationId poid = new ProjectOrganizationId(project.getProjectId(), org.getOrganizationId());
            ProjectOrganization po = new ProjectOrganization(poid, org, project);
            ProjectService projectService = new ProjectServiceImpl();
            projectService.addProjectOrganization(po);

            // add po to the project and org
            project.getProjectOrganizations().add(po);
            org.getProjectOrganizations().add(po);

            // notify the main pane to change
            this.pane.addNewOrganization(project, org);
        }

    }

    private void createNewPerson(PersonNewPane personNewPane) {

        if (personNewPane.validate(persons)) {
            Person person = personNewPane.getPerson();
            PersonService personService = new PersonServiceImpl();
            personService.addPerson(person);

            int position = -1;
            boolean added = false;
            for (Person p : persons) {
                position++;
                if (person.compareTo(p) < 0) {
                    persons.add(position, person);
                    added = true;
                    break;
                } else if (person.compareTo(p) == 0) {
                    added = true;
                    break;
                }
            }
            if (!added) {
                persons.add(person);
            }

            // notify the main pane to change
            this.pane.addNewPerson(person);
        }

    }

    private void updatePerson(PersonEditPane personEditPane) {

        if (personEditPane.validate(persons)) {

            //update the person in the database
            Person person = personEditPane.getPerson();
            PersonService personService = new PersonServiceImpl();
            personService.updatePerson(person);

            // notify the main pane to change
            this.pane.updatePerson(person);
        }
    }

    private void deletePerson(PersonEditPane personEditPane) {

        Person person = personEditPane.getPerson();
        LanguageModel language = personEditPane.getLanguage();

        // check if this person is in a project, deployment, image type identifying and annotation
        PersonService personService = new PersonServiceImpl();
        if (personService.inProjects(person)
                || personService.inDeployments(person)
                || personService.inAnnotations(person)) {

            String title = language.getString("title_error");
            String header = language.getString("user_in_use_error_header");
            String context = language.getString("user_in_use_error_context");

            Util.alertErrorPopup(title, header, context, language.getString("alert_ok"));

            return;
        }

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("person_edit_pane_delete_confirm_header"),
                language.getString("person_edit_pane_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {

            personService.removePerson(person.getPersonId());
            persons.remove(person);

            // notify the main pane to change
            this.pane.removePerson(person);
        }
    }

    public void createNewOrganization(OrganizationNewPane orgNewPane) {

        if (orgNewPane.validate(orgs)) {
            Organization org = orgNewPane.getOrganization();
            OrganizationService orgService = new OrganizationServiceImpl();
            orgService.addOrganization(org);

            this.addNewOrganization(org);

            // notify the main pane to change
            this.pane.addNewOrganization(org);
        }
    }

    public void updateOrganization(OrganizationEditPane orgEditPane) {

        if (orgEditPane.validate(orgs)) {

            //update the org in the database
            Organization org = orgEditPane.getOrganization();
            OrganizationService orgService = new OrganizationServiceImpl();
            orgService.updateOrganization(org);

            // notify the main pane to change
            this.pane.updateOrganization(org);
        }

    }

    private void deleteOrganization(OrganizationEditPane orgEditPane) {

        Organization org = orgEditPane.getOrganization();
        LanguageModel language = orgEditPane.getLanguage();

        OrganizationService orgService = new OrganizationServiceImpl();
        if (orgService.inProjects(org) || orgService.inPersons(org)) {

            String title = language.getString("title_error");
            String header = language.getString("org_in_use_error_header");
            String context = language.getString("org_in_use_error_context");

            Util.alertErrorPopup(title, header, context, language.getString("alert_ok"));

            return;
        }

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("org_edit_pane_delete_confirm_header"),
                language.getString("org_edit_pane_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {

            orgService.removeOrganization(org.getOrganizationId());
            orgs.remove(org);

            // notify the main pane to change
            this.pane.removeOrganization(org);
        }
    }

    private void createNewCamera(ProjectCameraPane projectCameraPane) {

        Project project = projectCameraPane.getProject();
        if (projectCameraPane.validate()) {
            Camera camera = projectCameraPane.getCamera();
            camera.setProject(project);

            CameraService cameraService = new CameraServiceImpl();
            cameraService.addCamera(camera);
            project.getCameras().add(camera);

            projectCameraPane.createNewCamera(camera);
        }
    }

    private void updateCamera(ProjectCameraPane projectCameraPane) {

        Project project = projectCameraPane.getProject();
        if (projectCameraPane.validate()) {
            Camera camera = projectCameraPane.getCamera();
            CameraService cameraService = new CameraServiceImpl();
            cameraService.updateCamera(camera);

            projectCameraPane.updateCamera(camera);
        }

    }

    private void deleteCamera(ProjectCameraPane projectCameraPane) {

        if (projectCameraPane.canDeleteCamera()) {

            Project project = projectCameraPane.getProject();
            LanguageModel language = projectCameraPane.getLanguage();

            boolean confirmed = Util.alertConfirmPopup(
                    language.getString("title_confirmation"),
                    language.getString("project_camera_pane_delete_confirm_header"),
                    language.getString("project_camera_pane_delete_confirm_context"),
                    language.getString("alert_ok"),
                    language.getString("alert_cancel"));

            if (confirmed) {
                Camera camera = projectCameraPane.getCamera();
                CameraService cameraService = new CameraServiceImpl();
                cameraService.removeCamera(camera.getCameraId());
                project.getCameras().remove(camera);

                projectCameraPane.removeCamera(camera);
            }

        }
    }

    private void createNewCameraTrapArray(ProjectCameraTrapArrayPane arrayPane) {

        Project project = arrayPane.getProject();
        if (arrayPane.validate()) {
            CameraTrapArray array = arrayPane.getCameraTrapArray();
            CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
            arrayService.addCameraTrapArray(array);
            project.getCameraTrapArrays().add(array);

            //arrayPane.createNewCameraTrapArray(array);
            this.pane.createNewCameraTrapArray(array);
        }
    }

    private void bulkUploadCameraTrapsInSingleArray(ProjectCameraTrapPane trapPane) {
        List<CameraTrap> newCameraTraps = trapPane.bulkUploadCameraTrapsInSingleArray();

        if (newCameraTraps != null) {
            for (CameraTrap newCameraTrap : newCameraTraps) {
                this.pane.createNewCameraTrap(newCameraTrap);
            }
        }
    }

    private void bulkUploadCTTrapsInMultipleArrays(ProjectCameraTrapArrayPane arrayPane) {
        List<CameraTrap> newCameraTraps = arrayPane.bulkUploadCTTrapsInMultipleArrays();

        if (newCameraTraps != null) {
            List<CameraTrapArray> newCtArrays = arrayPane.getNewCameraTrapArraysBulkUpload();
            for (CameraTrapArray ctArray : newCtArrays) {
                this.pane.createNewCameraTrapArray(ctArray);
            }

            for (CameraTrap newCameraTrap : newCameraTraps) {
                this.pane.createNewCameraTrap(newCameraTrap);
            }
        }
    }

    private void deleteCameraTrapArray(ProjectCameraTrapArrayPane arrayPane) {

        Project project = arrayPane.getProject();
        LanguageModel language = arrayPane.getLanguage();

        String title = language.getString("title_confirmation");
        String header = language.getString("project_array_pane_delete_confirm_header");
        String context = language.getString("project_array_pane_delete_confirm_context");

        boolean confirmed = Util.alertConfirmPopup(
                title,
                header,
                context,
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            CameraTrapArray array = arrayPane.getCameraTrapArray();

            if (array.getCameraTraps().isEmpty()) {
                CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
                arrayService.removeCameraTrapArray(array.getCameraTrapArrayId());
                project.getCameraTrapArrays().remove(array);

                this.pane.removeCameraTrapArray(array);
            } else {
                title = language.getString("title_error");
                header = language.getString("project_array_pane_not_empty_error_header");
                context = language.getString("project_array_pane_not_empty_error_context");

                Util.alertErrorPopup(title, header, context, language.getString("alert_ok"));
            }
        }
    }

    private void updateCameraTrapArray(ProjectCameraTrapArrayPane arrayPane) {

        if (arrayPane.validate()) {
            CameraTrapArray array = arrayPane.getCameraTrapArray();
            CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
            arrayService.updateCameraTrapArray(array);

            this.pane.updateCameraTrapArray(array);
        }
    }

    private void updateCameraTrapArray(ProjectCameraTrapPane trapPane) {

        if (trapPane.validateArray()) {
            CameraTrapArray array = trapPane.getCameraTrapArray();
            CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
            arrayService.updateCameraTrapArray(array);

            this.pane.updateCameraTrapArray(array);
        }
    }

    private void deleteCameraTrapArray(ProjectCameraTrapPane trapPane) {

        Project project = trapPane.getCameraTrapArray().getProject();
        LanguageModel language = trapPane.getLanguage();

        CameraTrapArray array = trapPane.getCameraTrapArray();

        if (!array.getCameraTraps().isEmpty()) {
            Util.alertErrorPopup(
                    language.getString("title_error"),
                    language.getString("project_array_pane_not_empty_error_header"),
                    language.getString("project_array_pane_not_empty_error_context"),
                    language.getString("alert_ok"));
        } else {
            boolean confirmed = Util.alertConfirmPopup(
                    language.getString("title_confirmation"),
                    language.getString("project_array_pane_delete_confirm_header"),
                    language.getString("project_array_pane_delete_confirm_context"),
                    language.getString("alert_ok"),
                    language.getString("alert_cancel"));

            if (confirmed) {

                CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
                arrayService.removeCameraTrapArray(array.getCameraTrapArrayId());
                project.getCameraTrapArrays().remove(array);

                this.pane.removeCameraTrapArray(array);
            }
        }
    }

    private void createNewCameraTrap(ProjectCameraTrapPane trapPane) {

        if (trapPane.validateTrap()) {
            CameraTrap trap = trapPane.getCameraTrap();
            CameraTrapService trapService = new CameraTrapServiceImpl();
            trapService.addCameraTrap(trap);

            CameraTrapArray array = trap.getCameraTrapArray();
            array.getCameraTraps().add(trap);

            this.pane.createNewCameraTrap(trap);
        }

    }

    private void updateCameraTrap(ProjectCameraTrapPane trapPane) {

        if (trapPane.validateTrap()) {
            CameraTrap trap = trapPane.getCameraTrap();
            CameraTrapService trapService = new CameraTrapServiceImpl();
            trapService.updateCameraTrap(trap);
            this.pane.updateCameraTrap(trap);
        }
    }

    private void cancelCameraTrapEditing(ProjectCameraTrapPane trapPane) {

        trapPane.stopEditing();
        CameraTrap trap = trapPane.getCameraTrap();
        this.pane.cancelCameraTrapEditing(trap);
    }

    private void deleteCameraTrap(ProjectCameraTrapPane trapPane) {

        CameraTrap trap = trapPane.getCameraTrap();
        LanguageModel language = trapPane.getLanguage();

        // check if the trap is used in a deployment
        ProjectService projectService = new ProjectServiceImpl();
        List<Deployment> deployments = projectService.getDeployments(trap.getCameraTrapArray().getProject());
        for (Deployment deployment : deployments) {
            if (deployment.getCameraTrap().getCameraTrapId().intValue() == trap.getCameraTrapId()) {
                trapPane.showTrapInUseError();
                return;
            }
        }

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("project_trap_pane_trap_delete_confirm_header"),
                language.getString("project_trap_pane_trap_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            CameraTrapService trapService = new CameraTrapServiceImpl();
            trapService.removeCameraTrap(trap.getCameraTrapId());
            trap.getCameraTrapArray().getCameraTraps().remove(trap);

            this.pane.removeCameraTrap(trap);
        }
    }

    public void createEvent(ProjectEventPane eventPane) {

        if (eventPane.validate()) {
            Event event = eventPane.getEvent();
            EventService eventService = new EventServiceImpl();
            eventService.addEvent(event);

            Project project = eventPane.getProject();
            project.getEvents().add(event);

            this.pane.createEvent(event);
        }
    }

    public void deleteEvent(ProjectEventPane eventPane) {

        Event event = eventPane.getEvent();
        LanguageModel language = eventPane.getLanguage();

        // check if the event is used in a deployment
        ProjectService projectService = new ProjectServiceImpl();
        if (!projectService.getDeployments(event.getProject()).isEmpty()) {
            eventPane.showEventInUseError();
            return;
        }

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("project_event_pane_event_delete_confirm_header"),
                language.getString("project_event_pane_event_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            EventService eventService = new EventServiceImpl();
            eventService.removeEvent(event.getEventId());
            event.getProject().getEvents().remove(event);

            this.pane.removeEvent(event);
        }
    }

    public void updateEvent(ProjectEventPane eventPane) {

        if (eventPane.validate()) {
            Event event = eventPane.getEvent();
            EventService eventService = new EventServiceImpl();
            eventService.updateEvent(event);
            this.pane.updateEvent(event);
        }
    }

    public void createDeployment(ProjectNewDeploymentPane deploymentPane) throws IOException {
        if (deploymentPane.validate()) {
            Deployment deployment = deploymentPane.getDeployment();
            String imageFolderPath = deploymentPane.getImageFolderPath();
            LanguageModel language = deploymentPane.getLanguage();

            Date deployment_startTime = deployment.getStartTime();
            Date deployment_endTime = deployment.getEndTime();

            ImageFeatureService imageFeatureService = new ImageFeatureServiceImpl();
            List<ImageFeature> imageFeatures = imageFeatureService.listImageFeature();

            if (!imageFolderPath.equals("")) {

                File sourceFolder = new File(imageFolderPath);
                DeploymentService deployService = new DeploymentServiceImpl();

                ProgressForm pForm = new ProgressForm(
                        pane,
                        language.getString("deployment_load_image_progress_form_title"),
                        language.getString("deployment_load_image_task_wait_info"));
                LoadDeploymentTask task = new LoadDeploymentTask(language, imageFeatures, deployment, sourceFolder, deployService);

                // binds progress of progress bars to progress of task:
                pForm.activateProgressBar(task);

                // in real life this method would get the result of the task
                // and update the UI based on its value:
                task.setOnSucceeded(event -> {
                    pane.createDeployment(deployment);
                    pForm.getDialogStage().close();
                    if ((deployment_startTime != null && !deployment_startTime.equals(deployment.getStartTime()))
                            || (deployment_endTime != null && !deployment_endTime.equals(deployment.getEndTime()))) {
                        deploymentPane.confirmLoadingSuccessWithDateChanged(pane);
                    } else {
                        deploymentPane.confirmLoadingSuccess(pane);
                    }
                });

                task.setOnCancelled(event -> {
                    pForm.getDialogStage().close();

                    Util.alertErrorPopup(
                            language.getString("title_error"),
                            task.getErrorHeader(),
                            task.getMessage(),
                            language.getString("alert_ok"));
                });

                pForm.getDialogStage().show();

                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();

            } else {

                // save damaged camera trap
                DeploymentService deployService = new DeploymentServiceImpl();
                deployService.addDeployment(deployment, new ArrayList<>());
                pane.createDeployment(deployment);
                deploymentPane.confirmDamageReportSuccess();

            }
        }

    }

    public void updateDeployment(ProjectEditDeploymentPane deploymentPane) throws IOException {
        if (deploymentPane.validate()) {
            Deployment oldDeployment = deploymentPane.getOldDeployment();
            File oldFolder = ImageRepository.getFolder(oldDeployment);

            Deployment deployment = deploymentPane.getDeployment();
            DeploymentService deployService = new DeploymentServiceImpl();
            deployService.updateDeployment(deployment, oldFolder);

            this.pane.updateDeployment(deployment);
        }
    }

    public void deleteDeployment(ProjectEditDeploymentPane deploymentPane) {

        LanguageModel language = deploymentPane.getLanguage();

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("project_deployment_pane_deployment_delete_confirm_header"),
                language.getString("project_deployment_pane_deployment_delete_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            Deployment deployment = deploymentPane.getDeployment();
            DeploymentService deployService = new DeploymentServiceImpl();
            deployService.removeDeployment(deployment);

            this.pane.removeDeployment(deployment);
        }
    }

    private void saveExifFeature(CameraModelExifPane modelExifPane) {

        CameraModel cameraModel = modelExifPane.getCameraModel();
        CameraModelService modelService = new CameraModelServiceImpl();

        List<CameraModelExifFeature> modelFeatures = modelExifPane.getModelExifFeatures();
        boolean isNew = true;
        for (CameraModelExifFeature modelExifFeature : modelFeatures) {
            if (modelExifFeature.getCameraModelExifFeatureId() != null) {
                isNew = false;
            }
        }

        if (isNew) {
            modelService.addExifFeatures(cameraModel, modelFeatures);
        } else {
            modelService.updateExifFeatures(modelFeatures);
        }

        Set<CameraModelExifFeature> set = new HashSet<>();
        set.addAll(modelFeatures);
        cameraModel.setCameraModelExifFeatures(set);

        this.pane.saveExifCameraModelFeatures(cameraModel);
        //modelExifPane.exifCameraModelFeaturesSaved();
    }

    private void deleteExifFeature(CameraModelExifPane modelExifPane) {

        if (modelExifPane.confirmDelete()) {
            CameraModel cameraModel = modelExifPane.getCameraModel();
            CameraModelService modelService = new CameraModelServiceImpl();
            modelService.deleteExifFeatures(cameraModel);
            cameraModel.setCameraModelExifFeatures(new HashSet<>());
            modelExifPane.reset();
        }
    }

    private void createCameraModel(CameraModelNewPane cameraModelPane) {

        if (cameraModelPane.validate()) {
            CameraModel cameraModel = cameraModelPane.getCameraModel();

            CameraModelService modelService = new CameraModelServiceImpl();
            modelService.addCameraModel(cameraModel);

            this.pane.createCameraModel(cameraModel);
        }
    }

    private void updateCameraModel(CameraModelNewPane cameraModelPane) {

        if (cameraModelPane.validate()) {

            // get the selected camera model
            CameraModel selectedCameraModel = cameraModelPane.getSelectedCameraModel();
            String oldMaker = selectedCameraModel.getMaker();
            String oldModel = selectedCameraModel.getName();

            // get the edited camera model
            CameraModel newCameraModel = cameraModelPane.getEditedCameraModel();

            CameraModelService modelService = new CameraModelServiceImpl();
            modelService.updateCameraModel(newCameraModel);

            this.pane.updateCameraModel(oldMaker, oldModel, newCameraModel);
        }
    }

    private void deleteCameraModel(CameraModelNewPane cameraModelPane) {

        CameraModel selectedCameraModel = cameraModelPane.getSelectedCameraModel();

        if (selectedCameraModel == null) {
            return;
        }

        CameraModelService modelService = new CameraModelServiceImpl();
        List<Camera> cameras = modelService.getCameras(selectedCameraModel);
        if (cameraModelPane.deleteValidate(cameras) && cameraModelPane.deleteLastModelValidate(selectedCameraModel.getMaker())) {
            if (cameras.isEmpty()) {
                if (cameraModelPane.confirmDelete()) {
                    modelService.removeCameraModel(selectedCameraModel.getCameraModelId());

                    this.pane.deleteCameraModel(selectedCameraModel);
                }
            }
        }
    }

    private void saveImageAnnotation(ProjectImagePane imagePane) {

        ProjectImageAnnotationPane annoPane = imagePane.getAnnotationPane();
        if (annoPane.validate()) {

            Image image = annoPane.getImage();

            List<Image> images = new ArrayList<>();
            if (annoPane.applyToGroup()) {
                images.addAll(image.getImageSequence().getImages());
            } else {
                images.add(image);
            }

            ImageService imageService = new ImageServiceImpl();
            for (Image img : images) {
                imageService.removeImageSpecies(img);
            }

            image.getImageSpecieses().clear();
            image = annoPane.getAnnotatedImage();
            for (Image img : images) {

                if (img.getImageId().intValue() != image.getImageId()) {

                    img.setImageType(image.getImageType());
                    img.setPerson(image.getPerson());
                    img.setNote(image.getNote());

                    img.getImageSpecieses().clear();

                    for (Object object : image.getImageSpecieses()) {
                        ImageSpecies is = (ImageSpecies) object;
                        ImageSpecies newIs = new ImageSpecies();
                        newIs.setImage(img);
                        newIs.setFamilyGenusSpecies(is.getFamilyGenusSpecies());
                        newIs.setIndividualCount(is.getIndividualCount());
                        newIs.setHomoSapiensType(is.getHomoSapiensType());
                        newIs.setUncertainty(is.getUncertainty());
                        newIs.setPerson(is.getPerson());
                        newIs.setEngishCommonName(is.getEngishCommonName());
                        newIs.setSubspecies(is.getSubspecies());
                        img.getImageSpecieses().add(newIs);
                    }
                }

                imageService.saveAnnotation(img);
            }

            annoPane.annotationSaved();
            this.pane.saveImageAnnotation(images);
        }
    }

    private void updateImageAnnotation(ProjectImagePane imagePane) {

        ProjectImageAnnotationPane annoPane = imagePane.getAnnotationPane();
        if (annoPane.validate()) {

            Image image = annoPane.getImage();
            List<Image> images = new ArrayList<>();
            if (annoPane.applyToGroup()) {
                images.addAll(image.getImageSequence().getImages());
            } else {
                images.add(image);
            }

            ImageService imageService = new ImageServiceImpl();

            List<ImageSpecies> oiss = new ArrayList<>();
            oiss.addAll(imageService.getImage(image.getImageId()).getImageSpecieses());
            Map<String, List<ImageSpecies>> img2species = new HashMap<>();

            images.forEach((img) -> {
                img = imageService.getImage(img.getImageId());
                List<ImageSpecies> isList = new ArrayList<>();
                isList.addAll(img.getImageSpecieses());
                img2species.put(img.getImageId().toString(), isList);
                imageService.removeImageSpecies(img);
            });

            image.getImageSpecieses().clear();
            image = annoPane.getAnnotatedImage();
            for (Image img : images) {

                if (img.getImageId().intValue() != image.getImageId()) {

                    img.setImageType(image.getImageType());
                    img.setPerson(image.getPerson());
                    img.setNote(image.getNote());
                    img.getImageSpecieses().clear();

                    for (Object object : image.getImageSpecieses()) {
                        ImageSpecies is = (ImageSpecies) object;
                        ImageSpecies newIs = new ImageSpecies();
                        newIs.setImage(img);
                        newIs.setFamilyGenusSpecies(is.getFamilyGenusSpecies());
                        newIs.setIndividualCount(is.getIndividualCount());
                        newIs.setHomoSapiensType(is.getHomoSapiensType());
                        newIs.setUncertainty(is.getUncertainty());
                        newIs.setPerson(is.getPerson());
                        newIs.setEngishCommonName(is.getEngishCommonName());
                        newIs.setSubspecies(is.getSubspecies());
                        img.getImageSpecieses().add(newIs);
                    }

                }

                // try to keep individuals 
                oiss = img2species.get(img.getImageId().toString());

                List<ImageSpecies> niss = new ArrayList<>();
                niss.addAll(img.getImageSpecieses());

                if (niss.size() == 1 && oiss.size() == 1) {
                    ImageSpecies nis = niss.get(0);
                    ImageSpecies ois = oiss.get(0);

                    Set<ImageIndividual> indSet = new HashSet<>();
                    int count = 0;
                    for (Object obj : ois.getImageIndividuals()) {
                        ImageIndividual ind = (ImageIndividual) obj;
                        count++;
                        if (count <= nis.getIndividualCount()) {
                            ind.setImageIndividualId(null);
                            ind.setImageSpecies(nis);
                            indSet.add(ind);
                        }
                    }
                    nis.setImageIndividuals(indSet);
                }

                imageService.saveAnnotation(img);
            }

            annoPane.annotationUpdated();
            this.pane.saveImageAnnotation(images);

            try {
                ProjectService projectService = new ProjectServiceImpl();
                List<Person> people = projectService.getPersons(image.getImageSequence().getDeployment().getEvent().getProject());

                ImageTypeService imageTypeService = new ImageTypeServiceImpl();
                List<ImageType> imageTypes = imageTypeService.listImageType();

                ImageUncertaintyTypeService imageUncertaintyTypeService = new ImageUncertaintyTypeServiceImpl();
                List<ImageUncertaintyType> imageUncertaintyTypes = imageUncertaintyTypeService.listImageUncertaintyType();

                TaxonomyService taxaService = new TaxonomyServiceImpl();
                List<HomoSapiensType> sapiensTypes = taxaService.listHomoSapiensType();

                if (pane.getProjectDataPane().getId() != null && pane.getProjectDataPane().getId().equals("ProjectImagePane")) {
                    ProjectImagePane previousImagePane = (ProjectImagePane) pane.getProjectDataPane();
                    imagePane = new ProjectImagePane(pane.getLanguage(), image, people, imageTypes, imageUncertaintyTypes, sapiensTypes, previousImagePane.getSelectedTab());
                    pane.setProjectDataPane(imagePane);
                } else {
                    imagePane = new ProjectImagePane(pane.getLanguage(), image, people, imageTypes, imageUncertaintyTypes, sapiensTypes);
                    pane.setProjectDataPane(imagePane);
                }
            } catch (Exception ex) {

            }

        }
    }

    private void updateImageGroupEditor(ProjectImagePane imagePane) {

        ProjectImageGroupEditorPane groupEditorPane = imagePane.getGroupEditorPane();
        if (groupEditorPane.validate()) {

            CheckBox[] thumbCheckBoxes = groupEditorPane.getCheckBoxes();

            Image image = groupEditorPane.getImage();
            ImageSequence sequence = image.getImageSequence();

            ImageService imageService = new ImageServiceImpl();
            List<Image> checkedImages = new ArrayList<>();

            Scene scene = imagePane.getScene();
            TilePane tile = groupEditorPane.getTile();

            for (CheckBox thumbCheckBox : thumbCheckBoxes) {
                if (thumbCheckBox.selectedProperty().getValue()) {
                    String thumbCheckBoxId = thumbCheckBox.getId();
                    String vboxId = thumbCheckBoxId.replaceAll("cb_", "#vbox_");

                    VBox vb = (VBox) scene.lookup(vboxId);
                    if (vb != null) {
                        tile.getChildren().remove(vb);
                    }

                    Image img = imageService.getImage(new Integer(thumbCheckBoxId.substring(3)));
                    checkedImages.add(img);

                    Image found_img = null;
                    for (Image ori_img : sequence.getImages()) {
                        if (ori_img.getImageId().intValue() == Integer.valueOf(thumbCheckBoxId.substring(3))) {
                            found_img = ori_img;
                            break;
                        }
                    }

                    if (found_img != null) {
                        sequence.getImages().remove(found_img);
                    }
                }
            }

            if (checkedImages.size() > 0) {
                imageService.removeImagesFromSequence(checkedImages, sequence);
            }
        }
    }

    private void updatePreference(PreferencePane pane) {

        Preference pref = pane.getPreference();
        PreferenceService prefService = new PreferenceServiceImpl();
        prefService.updatePreference(pref);

        String workingDir = pane.getWorkingDirectory();
        File workingDirObj = new File(workingDir);

        if (workingDirObj.exists() && workingDirObj.isDirectory() && workingDirObj.canRead() && workingDirObj.canWrite()) {
            WildID.wildIDProperties.setWorkingDir(workingDir);
        } else {
            WildID.wildIDProperties.setWorkingDir(null);
        }

        Util.updateWildIDProperties("working_dir", workingDir);
        WildID.wildIDProperties.setWorkingDir(workingDir);

        boolean enableImageIndividual = pane.getEnableImageIndividual();
        WildID.wildIDProperties.setEnableImageIndividual(enableImageIndividual);
        Util.updateWildIDProperties("enable_image_individual", String.valueOf(enableImageIndividual));

        boolean enableSubspecies = pane.getEnableSubspecies();
        WildID.wildIDProperties.setEnableSubspecies(enableSubspecies);
        Util.updateWildIDProperties("enable_subspecies", String.valueOf(enableSubspecies));

        pane.confirmPreferenceUpdated();
        this.pane.setLanguage(new LanguageModel(WildID.preference.getLanguage()));

        String style = WildID.preference.getStyle();
        if (style.equals("Caspian")) {
            Application.setUserAgentStylesheet(STYLESHEET_CASPIAN);
        } else {
            Application.setUserAgentStylesheet(STYLESHEET_MODENA);
        }

        WildIDNavigationPane navPane = this.pane.getNavigationPane();
        navPane.updatePreference();

    }

    private void saveTransferFile(List<Deployment> deployments, String fileName) throws JAXBException, IOException {
        LanguageModel language = this.pane.getLanguage();

        ProgressForm pForm = new ProgressForm(
                language.getString("save_transfer_file_progress_form_title"),
                language.getString("save_transfer_file_task_wait_info"));

        FileChooser fileChooser = new FileChooser();
        if (WildID.wildIDProperties.getWorkingDirObj() != null) {
            fileChooser.setInitialDirectory(WildID.wildIDProperties.getWorkingDirObj());
        }

        fileChooser.setTitle(language.getString("save_transfer_file_file_chooser_title"));
        fileChooser.setInitialFileName(fileName + ".zip");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter(language.getString("zip_extension_filter"), "*.zip")
        );

        File zipTransferFile = fileChooser.showSaveDialog(null);

        if (zipTransferFile != null) {
            SaveTransferFileTask task = new SaveTransferFileTask(language, deployments, zipTransferFile);

            // binds progress of progress bars to progress of task:
            pForm.activateProgressBar(task);
            task.setOnSucceeded(event -> {
                pForm.getDialogStage().close();

                Util.alertInformationPopup(
                        language.getString("title_success"),
                        language.getString("zipped_data_exported_confirm_header"),
                        language.getString("zipped_data_exported_confirm_context") + zipTransferFile.getAbsolutePath(),
                        language.getString("alert_ok"));
            });

            task.setOnCancelled(event -> {
                pForm.getDialogStage().close();

                Util.alertErrorPopup(
                        language.getString("title_error"),
                        language.getString("zipped_data_exported_error_header"),
                        language.getString("zipped_data_exported_error_msg"),
                        language.getString("alert_ok"));
            });

            pForm.getDialogStage().show();

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void openTransferFileInNewProject() throws IOException, JAXBException {

        LanguageModel language = this.pane.getLanguage();

        FileChooser fileChooser = new FileChooser();
        if (WildID.wildIDProperties.getWorkingDirObj() != null) {
            fileChooser.setInitialDirectory(WildID.wildIDProperties.getWorkingDirObj());
        }

        fileChooser.setTitle(language.getString("open_transfer_file_file_chooser_title"));
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter(language.getString("zip_extension_filter"), "*.zip")
        );
        File zipFile = fileChooser.showOpenDialog(null);

        if (zipFile != null) {

            // get the temp directory 
            File ourAppDir = Paths.get("").toFile();
            File tmpRootDir = new File(ourAppDir.getAbsolutePath() + File.separatorChar + "tmp");
            File tmpDir = new File(tmpRootDir, String.valueOf(new Date().getTime()));
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }

            ProgressForm pForm = new ProgressForm(
                    language.getString("open_transfer_file_progress_form_title"),
                    language.getString("open_transfer_file_task_wait_info"));

            LoadZipInNewProjectTask task = new LoadZipInNewProjectTask(language, zipFile, tmpDir, this.pane, this.persons);

            // binds progress of progress bars to progress of task:
            pForm.activateProgressBar(task);

            // in real life this method would get the result of the task
            // and update the UI based on its value:
            task.setOnSucceeded(event -> {
                pForm.getDialogStage().close();
                task.updatePane();
            });

            task.setOnCancelled(event -> {
                pForm.getDialogStage().close();

                Util.alertErrorPopup(
                        language.getString("title_error"),
                        language.getString("open_transfer_file_progress_form_error_title"),
                        language.getString("open_transfer_file_progress_form_error_msg") + ": " + task.getErrorMessage(),
                        language.getString("alert_ok"));
            });

            pForm.getDialogStage().show();

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        }

    }

    private void openTransferFileInCurrentProject(Project project) throws IOException, JAXBException {

        log.info("open a transfer file in the project: " + project.getName());

        LanguageModel language = this.pane.getLanguage();

        FileChooser fileChooser = new FileChooser();
        if (WildID.wildIDProperties.getWorkingDirObj() != null) {
            fileChooser.setInitialDirectory(WildID.wildIDProperties.getWorkingDirObj());
        }

        fileChooser.setTitle(language.getString("open_transfer_file_file_chooser_title"));
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter(language.getString("zip_extension_filter"), "*.zip")
        );
        File zipFile = fileChooser.showOpenDialog(null);

        if (zipFile != null) {

            // get the temp directory 
            File ourAppDir = Paths.get("").toFile();
            File tmpRootDir = new File(ourAppDir.getAbsolutePath() + File.separatorChar + "tmp");
            File tmpDir = new File(tmpRootDir, String.valueOf(new Date().getTime()));
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }

            ProgressForm pForm = new ProgressForm(
                    language.getString("open_transfer_file_progress_form_title"),
                    language.getString("open_transfer_file_task_wait_info"));

            LoadZipInCurrentProjectTask task = new LoadZipInCurrentProjectTask(language, zipFile, tmpDir, project, this.pane, this.persons, this.orgs);

            // binds progress of progress bars to progress of task:
            pForm.activateProgressBar(task);

            // in real life this method would get the result of the task
            // and update the UI based on its value:
            task.setOnSucceeded(event -> {
                pForm.getDialogStage().close();
                task.updatePane();
                setProjectAnnotationPane(project.getProjectId());
            });

            task.setOnCancelled(event -> {
                pForm.getDialogStage().close();

                Util.alertErrorPopup(
                        language.getString("title_error"),
                        language.getString("open_transfer_file_progress_form_error_title"),
                        language.getString("open_transfer_file_progress_form_error_msg") + ": " + task.getErrorMessage(),
                        language.getString("alert_ok"));
            });

            pForm.getDialogStage().show();

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        }

    }

    private void exportMetadata(String exportType, boolean withImages) throws MalformedURLException {
        boolean isValid = false;
        Project project = null;
        Event event = null;
        CameraTrapArray ctArray = null;
        CameraTrap cameraTrap = null;
        LanguageModel language = this.pane.getLanguage();

        TreeItem selectedTreeItem;
        List<Deployment> deployments = new ArrayList<>();

        if (this.pane.getNavigationPane().isImageTreeActive()) {
            WildIDImageTree imageTree = this.pane.getNavigationPane().getImageTree();

            selectedTreeItem = (TreeItem) imageTree.getSelectionModel().getSelectedItem();

            //Tree: Image node, upgrade its level to parent node: Deployment
            // so we don't have to get annoying message with wrong tree node
            if (selectedTreeItem instanceof ProjectImageTreeItem) {
                ProjectImageTreeItem projectImageTreeItem = (ProjectImageTreeItem) selectedTreeItem;
                selectedTreeItem = projectImageTreeItem.getParent();
            }

            //Tree: Project
            if (selectedTreeItem instanceof ProjectTreeItem) {
                ProjectTreeItem projectTreeItem = (ProjectTreeItem) selectedTreeItem;
                project = projectTreeItem.getProject();
                isValid = true;

                if (withImages) {
                    ProjectService projectService = new ProjectServiceImpl();
                    deployments.addAll(projectService.getDeployments(project));
                }
            } //Tree: Images from Project
            else if (selectedTreeItem instanceof ProjectImageListTreeItem) {
                ProjectImageListTreeItem projectImageTreeItem = (ProjectImageListTreeItem) selectedTreeItem;
                project = projectImageTreeItem.getProject();
                isValid = true;

                if (withImages) {
                    ProjectService projectService = new ProjectServiceImpl();
                    deployments.addAll(projectService.getDeployments(project));
                }
            } //Tree: Images from Event
            else if (selectedTreeItem instanceof ProjectImageEventTreeItem) {
                ProjectImageEventTreeItem projectImageEventTreeItem = (ProjectImageEventTreeItem) selectedTreeItem;
                event = projectImageEventTreeItem.getEvent();

                ProjectImageListTreeItem projectImageListTreeItem = (ProjectImageListTreeItem) projectImageEventTreeItem.getParent();
                project = projectImageListTreeItem.getProject();
                isValid = true;

                if (withImages) {
                    ProjectService projectService = new ProjectServiceImpl();
                    deployments.addAll(projectService.getDeployments(projectImageEventTreeItem.getEvent()));
                }
            } //Tree: Images from Camera Trap Array
            else if (selectedTreeItem instanceof ProjectImageEventArrayTreeItem) {
                ProjectImageEventArrayTreeItem projectImageEventArrayTreeItem = (ProjectImageEventArrayTreeItem) selectedTreeItem;
                ctArray = projectImageEventArrayTreeItem.getCameraTrapArray();

                ProjectImageEventTreeItem imageEventTreeItem = (ProjectImageEventTreeItem) projectImageEventArrayTreeItem.getParent();
                ProjectImageListTreeItem imageListTreeItem = (ProjectImageListTreeItem) imageEventTreeItem.getParent();

                project = imageListTreeItem.getProject();
                event = imageEventTreeItem.getEvent();
                isValid = true;

                if (withImages) {
                    ProjectService projectService = new ProjectServiceImpl();
                    deployments.addAll(projectService.getDeployments(projectImageEventArrayTreeItem.getEvent(), projectImageEventArrayTreeItem.getCameraTrapArray()));
                }
            } //Tree: Images from Camera Trap | Deployment
            else if (selectedTreeItem instanceof ProjectImageDeploymentTreeItem) {
                ProjectImageDeploymentTreeItem projectImageDeploymentTreeItem = (ProjectImageDeploymentTreeItem) selectedTreeItem;
                cameraTrap = projectImageDeploymentTreeItem.getDeployment().getCameraTrap();

                ProjectImageEventArrayTreeItem imageEventArrayTreeItem = (ProjectImageEventArrayTreeItem) projectImageDeploymentTreeItem.getParent();
                ProjectImageEventTreeItem imageEventTreeItem = (ProjectImageEventTreeItem) imageEventArrayTreeItem.getParent();
                ProjectImageListTreeItem imageListTreeItem = (ProjectImageListTreeItem) imageEventTreeItem.getParent();

                project = imageListTreeItem.getProject();
                event = imageEventTreeItem.getEvent();
                ctArray = imageEventArrayTreeItem.getCameraTrapArray();
                isValid = true;

                if (withImages) {
                    deployments.add(projectImageDeploymentTreeItem.getDeployment());
                }
            }
        }

        if (isValid) {
            if (exportType.equals("table")) {
                viewImageMetadataTable(project, event, ctArray, cameraTrap, withImages);
                return;
            }

            File exportFile;
            File exportDir;

            if (withImages) {
                DirectoryChooser dirChooser = new DirectoryChooser();
                //dirChooser.setTitle("Select Export Directory");
                dirChooser.setTitle(language.getString("export_image_pane_select_dir"));

                if (WildID.wildIDProperties.getWorkingDirObj() != null) {
                    dirChooser.setInitialDirectory(WildID.wildIDProperties.getWorkingDirObj());
                }

                File exportBaseDir = dirChooser.showDialog(null);

                if (exportBaseDir == null) {
                    return;
                } else {
                    String defaultName = "Wild_ID_" + Util.getObjectId(project, event, ctArray, cameraTrap);

                    exportDir = new File(exportBaseDir.getAbsolutePath() + File.separatorChar + defaultName);
                    int g = 0;

                    while (exportDir.exists()) {
                        String newName = defaultName + "_" + (++g);

                        exportDir = new File(exportBaseDir.getAbsolutePath() + File.separatorChar + newName);
                    }

                    boolean created = exportDir.mkdirs();

                    if (!created) {
                        String title = language.getString("title_error");
                        String header = language.getString("export_image_pane_write_failed_header");
                        String context = language.getString("export_image_pane_write_failed_context");

                        Util.alertErrorPopup(title, header, context, language.getString("alert_ok"));

                        return;
                    }

                    String exportFileName = defaultName;
                    if (exportType.equals("csv")) {
                        exportFileName += ".csv";
                    } else {
                        exportFileName += ".xlsx";
                    }

                    exportFile = new File(exportDir.getAbsolutePath() + File.separatorChar + exportFileName);
                }
            } else {
                FileChooser fileChooser = new FileChooser();

                fileChooser.setTitle("Select Export File Name");
                fileChooser.setInitialFileName("Wild_ID_" + Util.getObjectId(project, event, ctArray, cameraTrap));

                exportDir = WildID.wildIDProperties.getWorkingDirObj();
                if (exportDir == null) {
                    exportDir = Paths.get("export").toFile();
                    if (!exportDir.exists()) {
                        exportDir.mkdirs();
                    }
                }

                fileChooser.setInitialDirectory(exportDir);

                ExtensionFilter extFilter_csv = new ExtensionFilter(language.getString("csv_extension_filter"), "*.csv");
                ExtensionFilter extFilter_txt = new ExtensionFilter(language.getString("txt_extension_filter"), "*.txt");
                ExtensionFilter extFilter_xlsx = new ExtensionFilter(language.getString("xlsx_extension_filter"), "*.xlsx");
                ExtensionFilter extFilter_xls = new ExtensionFilter(language.getString("xls_extension_filter"), "*.xls");

                if (exportType.equals("csv")) {
                    fileChooser.getExtensionFilters().addAll(extFilter_csv, extFilter_txt);
                } else {
                    fileChooser.getExtensionFilters().addAll(extFilter_xlsx, extFilter_xls);
                }

                exportFile = fileChooser.showSaveDialog(null);
            }

            if (exportFile != null) {

                ProgressForm pForm = new ProgressForm(
                        language.getString("export_image_pane_info"),
                        language.getString("export_image_pane_status"));

                ExportImageMetadataTask task = new ExportImageMetadataTask(
                        language,
                        project,
                        event,
                        ctArray,
                        cameraTrap,
                        deployments,
                        exportType,
                        exportDir,
                        exportFile,
                        withImages
                );

                // binds progress of progress bars to progress of task:
                pForm.activateProgressBar(task);

                // in real life this method would get the result of the task
                // and update the UI based on its value:
                task.setOnSucceeded(thisEvent -> {
                    pForm.getDialogStage().close();

                    Util.alertInformationPopup(
                            language.getString("title_success"),
                            language.getString("export_image_pane_success_header"),
                            language.getString("export_image_pane_success_context")
                            + " " + exportFile.getAbsolutePath(),
                            language.getString("alert_ok"));
                });

                task.setOnCancelled(thisEvent -> {
                    pForm.getDialogStage().close();

                    Util.alertErrorPopup(
                            language.getString("title_error"),
                            language.getString("export_image_pane_error_header"),
                            language.getString("export_image_pane_error_context"),
                            language.getString("alert_ok"));
                });

                pForm.getDialogStage().show();

                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } else if (exportType.equals("table")) {
            Util.alertWarningPopup(
                    language.getString("title_error"),
                    language.getString("image_metadata_table_pane_wrong_tree_item_header"),
                    language.getString("image_metadata_table_pane_wrong_tree_item_context"),
                    language.getString("alert_ok"));
        } else {
            Util.alertWarningPopup(
                    language.getString("title_error"),
                    language.getString("export_image_pane_wrong_tree_item_header"),
                    language.getString("image_metadata_table_pane_wrong_tree_item_context"),
                    language.getString("alert_ok"));
        }
    }

    public void viewImageMetadataTable(Project project, Event event, CameraTrapArray ctArray, CameraTrap cameraTrap, boolean newWindow) {
        WildIDImageTree imageTree = this.pane.getNavigationPane().getImageTree();
        ImageMetadataTablePane imageMetadataTablePane = new ImageMetadataTablePane(
                this.pane.getLanguage(),
                project,
                event,
                ctArray,
                cameraTrap,
                imageTree,
                newWindow
        );

        if (newWindow) {
            if (undockWindow != null) {
                undockWindow.close();
            }

            undockWindow = new Stage();
            undockWindow.getIcons().add(new javafx.scene.image.Image("resources/icons/wildId32.png"));
            undockWindow.setTitle("Wild.ID");

            Scene scene = new Scene(imageMetadataTablePane);

            undockWindow.setScene(scene);
            undockWindow.setWidth(600);
            undockWindow.setHeight(600);

            if (!undockWindow.isFocused()) {
                undockWindow.requestFocus();
            }

            if (undockWindow.isIconified()) {
                undockWindow.setIconified(false);
            }
            undockWindow.toFront();
            undockWindow.show();
        } else {
            pane.setProjectDataPane(imageMetadataTablePane);
        }
    }

    public void setNewProjectPane() {
        this.pane.getNavigationPane().setProjectPane();
        this.pane.getNavigationPane().getProjectTree().getSelectionModel().select(0);

        ProjectNewPane newProjectPane = new ProjectNewPane(pane.getLanguage());
        this.pane.setProjectDataPane(newProjectPane);

        this.pane.getMenuBar().setSaveTransferFileDisable(true);
        this.pane.getMenuBar().setLoadTransferFileDisable(true);
        this.pane.getMenuBar().setImageMenuDisable(true);
    }

    public void setNewOrganizationPane() {
        this.pane.getNavigationPane().setOrganizationPane();
        if (this.pane.getOrganizationDataPane() == null) {
            this.pane.setOrganizationDataPane(new OrganizationNewPane(pane.getLanguage()));
            this.pane.getNavigationPane().getOrganizationTree().getSelectionModel().select(0);
        }
        this.pane.setOrganizationDataPane(this.pane.getOrganizationDataPane());

        this.pane.getMenuBar().setSaveTransferFileDisable(true);
        this.pane.getMenuBar().setLoadTransferFileDisable(true);
        this.pane.getMenuBar().setImageMenuDisable(true);
    }

    public void setNewPersonPane() {
        this.pane.getNavigationPane().setPersonPane();
        if (this.pane.getPersonDataPane() == null) {
            this.pane.setPersonDataPane(new PersonNewPane(pane.getLanguage()));
            this.pane.getNavigationPane().getPersonTree().getSelectionModel().select(0);
        }
        this.pane.setPersonDataPane(this.pane.getPersonDataPane());

        this.pane.getMenuBar().setSaveTransferFileDisable(true);
        this.pane.getMenuBar().setLoadTransferFileDisable(true);
        this.pane.getMenuBar().setImageMenuDisable(true);
    }

    public void setNewCameraModelPane() {
        this.pane.getNavigationPane().setCameraModelPane();

        if (this.pane.getCameraModelDataPane() == null) {
            String maker = "Browning";
            CameraModelService cameraModelService = new CameraModelServiceImpl();
            List<CameraModel> cameraModels = cameraModelService.listCameraModel();
            CameraModelNewPane cameraModelDataPane = new CameraModelNewPane(pane.getLanguage(), cameraModels, maker);
            pane.setCameraModelDataPane(cameraModelDataPane);
            this.pane.getNavigationPane().getCameraModelTree().getSelectionModel().select(1);
        }
        this.pane.setCameraModelDataPane(this.pane.getCameraModelDataPane());

        this.pane.getMenuBar().setSaveTransferFileDisable(true);
        this.pane.getMenuBar().setLoadTransferFileDisable(true);
        this.pane.getMenuBar().setImageMenuDisable(true);
    }

    public void aboutWildID() {
        openWindowModalPopup(new WildIDAboutPane(this, pane.getLanguage()));
    }

    public void licenseWildID() {
        openWindowModalPopup(new WildIDLicensePane(pane.getLanguage()));
    }

    public void openWindowModalPopup(Pane pane) {
        openWindowModalPopup(pane, "Wild.ID");
    }

    public void openWindowModalPopup(Pane pane, String title) {
        if (dialog != null) {
            dialog.close();
        }

        dialog = new Stage();
        dialog.setTitle(title);
        dialog.getIcons().add(new javafx.scene.image.Image("resources/icons/wildId32.png"));
        dialog.setResizable(false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.sizeToScene();

        Scene scene = new Scene(pane);
        dialog.setScene(scene);
        dialog.show();
    }

    public void setProjectAnnotationPane(int projectId) {
        this.pane.getNavigationPane().setImagePane(projectId, this);

        WildIDImageTree imageTree = this.pane.getNavigationPane().getImageTree();
        imageTree.getSelectionModel().select(1);
        imageTree.expandProjectTreeUpToLevel(3);

        this.pane.getMenuBar().setSaveTransferFileDisable(false);
        this.pane.getMenuBar().setLoadTransferFileDisable(false);
        this.pane.getMenuBar().setImageMenuDisable(false);
        Util.updateWildIDProperties("opened_project", String.valueOf(projectId));
    }

    public void setEditProjectPane(int projectId) {
        this.pane.getNavigationPane().setProjectPane();
        WildIDProjectTree projectTree = this.pane.getNavigationPane().getProjectTree();

        ProjectTreeItem projectTreeItem = projectTree.getProjectTreeItem(projectId);
        projectTree.getSelectionModel().select(projectTreeItem);
        projectTreeItem.setExpanded(true);

        this.pane.setProjectDataPane(new ProjectEditPane(this.pane.getLanguage(), projectTreeItem.getProject()));

        this.pane.getMenuBar().setSaveTransferFileDisable(true);
        this.pane.getMenuBar().setLoadTransferFileDisable(true);
        this.pane.getMenuBar().setImageMenuDisable(true);
    }

    public void checkUpdate() {
        WildIDDataPane updatePane;
        try {
            updatePane = new WildIDUpdatePane(this, pane.getLanguage());
            updatePane.setId("updatePane");
            openWindowModalPopup(updatePane);
            dialog.setResizable(true);
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }
    }

    public void managePlugin() {
        WildIDDataPane pluginPane;
        try {
            pluginPane = new WildIDPluginPane(this, pane.getLanguage(), pane.getMenuBar());
            openWindowModalPopup(pluginPane);
            dialog.setResizable(true);
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }
    }

    public void contactUs() {
        this.pane.contactUs();
    }

    public void reportError() {
        this.pane.viewLog();
    }

    public void register() {
        if (!WildID.wildIDProperties.getRegistered()) {
            openWindowModalPopup(new RegistrationPane(this, pane.getLanguage()));
        } else {
            this.pane.getMenuBar().disableRegistration();
        }
    }

    public void updateWildID() {
        WildIDUpdatePane updatePane = (WildIDUpdatePane) dialog.getScene().lookup("#updatePane");
        LanguageModel language = updatePane.getLanguage();

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("wildID_updatePane_restart_confirm_header"),
                language.getString("wildID_updatePane_restart_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            ProgressForm pForm = new ProgressForm(
                    language.getString("wildID_updatePane_update"),
                    language.getString("wildID_updatePane_update_msg"));

            WildIDUpdateTask task = new WildIDUpdateTask(pane.getLanguage(), updatePane);

            // binds progress of progress bars to progress of task:
            pForm.activateProgressBar(task);

            task.setOnSucceeded(event -> {
                pForm.getDialogStage().close();
                WildID.restart();
            });

            task.setOnCancelled(event -> {
                pForm.getDialogStage().close();

                Util.alertErrorPopup(
                        language.getString("title_error"),
                        language.getString("wildID_updatePane_update_error_header"),
                        language.getString("wildID_updatePane_update_error_msg") + ": \n" + task.getErrorMessage(),
                        language.getString("alert_ok"));
            });

            pForm.getDialogStage().show();

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void setFindPane() {
        SearchPane searchPane;
        if (!(this.pane.getProjectDataPane() instanceof SearchPane)) {
            searchPane = new SearchPane(this.pane.getLanguage());
        } else {
            searchPane = (SearchPane) this.pane.getProjectDataPane();
        }
        this.pane.setProjectDataPane(searchPane);
    }

    public void search(SearchPane searchPane) {

        ImageService imageService = new ImageServiceImpl();
        int count = imageService.getImageCount(searchPane.getSelectedProjects(),
                searchPane.getStartDate(),
                searchPane.getEndDate(),
                searchPane.getImageType(),
                searchPane.getGenus(),
                searchPane.getSpecies());
        searchPane.showSearchCount(count);

        TreeMap<Project, TreeMap<Event, TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>>>> projectMap
                = imageService.searchImages(searchPane.getSelectedProjects(),
                        searchPane.getStartDate(),
                        searchPane.getEndDate(),
                        searchPane.getImageType(),
                        searchPane.getGenus(),
                        searchPane.getSpecies());

        this.pane.getNavigationPane().setSearchResultPane(searchPane.getSelectedProjects(),
                searchPane.getStartDate(),
                searchPane.getEndDate(),
                searchPane.getImageType(),
                searchPane.getGenus(),
                searchPane.getSpecies(),
                projectMap);

    }

    private void reportError(ReportErrorPane reportPane) {
        LanguageModel language = reportPane.getLanguage();

        if (reportPane.validate()) {

            String emailAddr = reportPane.getEmailAddress();
            if (emailAddr == null || emailAddr.trim().equals("")) {
                emailAddr = "anonymous@unknown.com";
            }
            String desc = reportPane.getDescription();

            String todayLog = Util.tail(new File("WildID.log"), 2000);
            todayLog = "Product Version: Wild.ID " + WildID.VERSION + "\n"
                    + "Java Version: " + System.getProperty("java.version") + "\n"
                    + "OS Name: " + System.getProperty("os.name") + "\n"
                    + "OS Version: " + System.getProperty("os.version") + "\n"
                    + "OS Arch: " + System.getProperty("os.arch") + "\n"
                    + "Home Directory: " + System.getProperty("user.dir") + "\nn"
                    + Util.getTodayLog(todayLog);

            try {
                Util.sendMail(emailAddr, "Wild.ID Error Report", desc + "\n\n" + todayLog);
                dialog.hide();

                Util.alertInformationPopup(
                        language.getString("title_success"),
                        language.getString("report_error_success_header"),
                        language.getString("report_error_success_text"),
                        language.getString("alert_ok"));
            } catch (Exception ex) {
                //ex.printStackTrace();
                log.error(ex.getMessage());
            }

        }
    }

    private void contactUs(ContactUsPane contactUsPane) {
        LanguageModel language = contactUsPane.getLanguage();

        if (contactUsPane.validate()) {

            String emailAddr = contactUsPane.getEmailAddress();
            if (emailAddr == null || emailAddr.trim().equals("")) {
                emailAddr = "anonymous@unknown.com";
            }
            String desc = contactUsPane.getDescription();

            try {
                Util.sendMail(emailAddr, "Contact Us", desc);
                dialog.hide();

                Util.alertInformationPopup(
                        language.getString("title_success"),
                        language.getString("sent_message_success_header"),
                        language.getString("sent_message_success_text"),
                        language.getString("alert_ok"));
            } catch (Exception ex) {
                //ex.printStackTrace();
                log.error(ex.getMessage());
            }

        }
    }

    private void submitRegistration() throws UnsupportedEncodingException {
        RegistrationPane registrationPane = (RegistrationPane) dialog.getScene().lookup("#Registration");

        if (registrationPane.validate()) {
            if (registrationPane.submitRegister()) {
                this.pane.getMenuBar().disableRegistration();
            }
        }
    }

    private void plugin_install(String pluginName) {
        WildIDPluginPane pluginPane = (WildIDPluginPane) dialog.getScene().lookup("#WildIDPlugin");
        LanguageModel language = pluginPane.getLanguage();

        ProgressForm pForm = new ProgressForm(
                language.getString("plugin_progressbar_install_title"),
                language.getString("plugin_progressbar_install_msg"));

        WildIDPluginTask task = new WildIDPluginTask(pane.getLanguage(), pluginPane, pluginName, "install");

        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();

            boolean confirmed = Util.alertConfirmPopup(
                    language.getString("title_confirmation"),
                    language.getString("wildID_updatePane_restart_confirm_header"),
                    language.getString("wildID_updatePane_restart_confirm_context"),
                    language.getString("alert_ok"),
                    language.getString("alert_cancel"));

            if (confirmed) {
                WildID.restart();
            }
        });

        task.setOnCancelled(event -> {
            pForm.getDialogStage().close();

            Util.alertErrorPopup(
                    language.getString("title_error"),
                    language.getString("plugin_progressbar_install_error_header"),
                    language.getString("plugin_progressbar_install_error_msg") + ": \n" + task.getErrorMessage(),
                    language.getString("alert_ok"));
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void plugin_uninstall(String pluginName) {
        WildIDPluginPane pluginPane = (WildIDPluginPane) dialog.getScene().lookup("#WildIDPlugin");
        LanguageModel language = pluginPane.getLanguage();

        boolean confirmed = Util.alertConfirmPopup(
                language.getString("title_confirmation"),
                language.getString("plugin_unintall_confirm_header"),
                language.getString("plugin_unintall_confirm_context"),
                language.getString("alert_ok"),
                language.getString("alert_cancel"));

        if (confirmed) {
            ProgressForm pForm = new ProgressForm(
                    language.getString("plugin_progressbar_uninstall_title"),
                    language.getString("plugin_progressbar_uninstall_msg"));

            WildIDPluginTask task = new WildIDPluginTask(pane.getLanguage(), pluginPane, pluginName, "uninstall");

            // binds progress of progress bars to progress of task:
            pForm.activateProgressBar(task);

            task.setOnSucceeded(event -> {
                pForm.getDialogStage().close();
                dialog.hide();

                Util.alertInformationPopup(
                        language.getString("title_success"),
                        language.getString("plugin_progressbar_uninstall_success_header"),
                        language.getString("plugin_progressbar_uninstall_success_msg"),
                        language.getString("alert_ok"));
            });

            task.setOnCancelled(event -> {
                pForm.getDialogStage().close();
                dialog.hide();

                Util.alertErrorPopup(
                        language.getString("title_error"),
                        language.getString("plugin_progressbar_uninstall_error_header"),
                        language.getString("plugin_progressbar_uninstall_error_msg") + ": \n" + task.getErrorMessage(),
                        language.getString("alert_ok"));
            });

            pForm.getDialogStage().show();

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        }
    }

    private void plugin_update(String pluginName) {
        WildIDPluginPane pluginPane = (WildIDPluginPane) dialog.getScene().lookup("#WildIDPlugin");
        LanguageModel language = pluginPane.getLanguage();

        ProgressForm pForm = new ProgressForm(
                language.getString("plugin_progressbar_update_title"),
                language.getString("plugin_progressbar_update_msg"));

        WildIDPluginTask task = new WildIDPluginTask(pane.getLanguage(), pluginPane, pluginName, "update");

        // binds progress of progress bars to progress of task:
        pForm.activateProgressBar(task);

        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();

            boolean confirmed = Util.alertConfirmPopup(
                    language.getString("title_confirmation"),
                    language.getString("plugin_progressbar_update_success_header"),
                    language.getString("plugin_progressbar_update_success_msg"),
                    language.getString("alert_ok"),
                    language.getString("alert_cancel"));

            if (confirmed) {
                WildID.restart();
            }
        });

        task.setOnCancelled(event -> {
            pForm.getDialogStage().close();

            Util.alertErrorPopup(
                    language.getString("title_error"),
                    language.getString("plugin_progressbar_update_error_header"),
                    language.getString("plugin_progressbar_update_error_msg") + ": \n" + task.getErrorMessage(),
                    language.getString("alert_ok"));
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void addNewPerson(Person person) {
        int position = -1;
        boolean added = false;

        for (Person p : persons) {
            position++;
            if (person.compareTo(p) < 0) {
                persons.add(position, person);
                added = true;
                break;
            } else if (person.compareTo(p) == 0) {
                added = true;
                break;
            }
        }
        if (!added) {
            persons.add(person);
        }
    }

    public void addNewOrganization(Organization org) {
        int position = -1;
        boolean added = false;

        for (Organization o : orgs) {
            position++;
            if (org.compareTo(o) < 0) {
                orgs.add(position, org);
                added = true;
                break;
            } else if (org.compareTo(o) == 0) {
                added = true;
                break;
            }
        }

        if (!added) {
            orgs.add(org);
        }
    }

    public void addingImageIndividual(
            ProjectImageViewPane projectImageViewPane,
            Image image,
            int img_x,
            int img_y,
            double screenX,
            double screenY) {
        WildIDDataPane imageIndividualPane;
        try {
            AgeService ageService = new AgeServiceImpl();
            List<Age> ages = ageService.listAge();

            ImageIndividual imageIndividual = new ImageIndividual();
            imageIndividual.setX(img_x);
            imageIndividual.setY(img_y);

            imageIndividualPane = new ImageIndividualPane(projectImageViewPane, ages, imageIndividual);
            imageIndividualPane.setId("imageIndividualPane");
            openWindowModalPopup(imageIndividualPane);
            dialog.setResizable(true);
            dialog.setX(screenX);
            dialog.setY(screenY - 175);

            dialog.setWidth(300);
            dialog.setHeight(350);

        } catch (Exception ex) {
            log.info(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void viewUpdateImageIndividual(
            ProjectImageViewPane projectImageViewPane,
            Image image,
            ImageIndividual imageIndividual,
            double screenX,
            double screenY) {
        WildIDDataPane imageIndividualPane;
        try {
            AgeService ageService = new AgeServiceImpl();
            List<Age> ages = ageService.listAge();

            imageIndividualPane = new ImageIndividualPane(projectImageViewPane, ages, imageIndividual);
            imageIndividualPane.setId("imageIndividualPane");
            openWindowModalPopup(imageIndividualPane);
            dialog.setResizable(true);
            dialog.setX(screenX);
            dialog.setY(screenY - 175);
            dialog.setWidth(300);
            dialog.setHeight(350);

        } catch (Exception ex) {
            log.info(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void saveImageIndividual() {
        ImageIndividualPane imageIndividualPane = (ImageIndividualPane) dialog.getScene().lookup("#imageIndividualPane");
        LanguageModel language = this.pane.getLanguage();

        ImageIndividual imageIndividual = imageIndividualPane.getImageIndividual();

        if (imageIndividual != null) {
            ImageService imageService = new ImageServiceImpl();

            if (imageIndividualPane.validate()) {
                imageService.addImageIndividual(imageIndividual);

                //Update the model
                imageIndividual.getImageSpecies().getImageIndividuals().add(imageIndividual);

                imageIndividualPane.getProjectImageViewPane().addOverlayImageIndividual(imageIndividual);
                dialog.hide();

                Util.alertInformationPopup(
                        language.getString("title_success"),
                        language.getString("image_individual_add_new_success_header"),
                        language.getString("image_individual_add_new_success_msg"),
                        language.getString("alert_ok"));
            }
        } else {
            Util.alertErrorPopup(
                    language.getString("title_error"),
                    language.getString("image_individual_add_new_error_header"),
                    language.getString("image_individual_add_new_error_msg"),
                    language.getString("alert_ok"));
        }
    }

    public void updateImageIndividual() {
        ImageIndividualPane imageIndividualPane = (ImageIndividualPane) dialog.getScene().lookup("#imageIndividualPane");
        LanguageModel language = this.pane.getLanguage();

        ImageIndividual imageIndividual = imageIndividualPane.getImageIndividual();

        if (imageIndividual != null) {
            ImageService imageService = new ImageServiceImpl();
            imageService.updateImageIndividual(imageIndividual);

            dialog.hide();
            Util.alertInformationPopup(
                    language.getString("title_success"),
                    language.getString("image_individual_update_success_header"),
                    language.getString("image_individual_update_success_msg"),
                    language.getString("alert_ok"));
        } else {
            Util.alertErrorPopup(
                    language.getString("title_error"),
                    language.getString("image_individual_update_error_header"),
                    language.getString("image_individual_update_error_msg"),
                    language.getString("alert_ok"));
        }
    }

    public void deleteImageIndividual() {
        ImageIndividualPane imageIndividualPane = (ImageIndividualPane) dialog.getScene().lookup("#imageIndividualPane");
        LanguageModel language = this.pane.getLanguage();

        ImageIndividual imageIndividual = imageIndividualPane.getImageIndividual();

        if (imageIndividual != null) {
            ImageService imageService = new ImageServiceImpl();
            imageService.removeImageIndividual(imageIndividual);
            imageIndividualPane.getProjectImageViewPane().removeOverlayImageIndividual(imageIndividual);

            //Update the model
            imageIndividual.getImageSpecies().getImageIndividuals().remove(imageIndividual);

            dialog.hide();
            Util.alertInformationPopup(
                    language.getString("title_success"),
                    language.getString("image_individual_delete_success_header"),
                    language.getString("image_individual_delete_success_msg"),
                    language.getString("alert_ok"));
        } else {
            Util.alertErrorPopup(
                    language.getString("title_error"),
                    language.getString("image_individual_delete_error_header"),
                    language.getString("image_individual_delete_error_msg"),
                    language.getString("alert_ok"));
        }
    }
}
