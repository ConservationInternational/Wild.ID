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

import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.Image;
import org.wildid.entity.ImageType;
import org.wildid.entity.Organization;
import org.wildid.entity.Person;
import org.wildid.entity.Project;

/**
 * @author Kai Lin, Minh Phan
 */
public class WildIDNavigationPane extends AnchorPane implements LanguageChangable {

    // language used in the display
    private final LanguageModel language;

    private final ScrollPane projectScrollPane;
    private final WildIDProjectTree projectTree;

    private final ScrollPane orgScrollPane;
    private final WildIDOrganizationTree orgTree;

    private final ScrollPane personScrollPane;
    private final WildIDPersonTree personTree;

    private final ScrollPane cameraModelScrollPane;
    private final WildIDCameraModelTree cameraModelTree;

    private ScrollPane imageScrollPane;
    private WildIDImageTree imageTree;

    private ScrollPane searchResultScrollPane;
    private WildIDSearchResultTree searchResultTree;

    private WildIDController controller;

    public WildIDNavigationPane(LanguageModel language,
            ObservableList<Project> projects,
            ObservableList<Organization> orgs,
            ObservableList<Person> persons,
            ObservableList<CameraModel> cameraModels) {

        this.language = language;

        // project tab
        // create project tree
        ProjectListTreeItem projectRootItem = new ProjectListTreeItem(language, projects);
        this.projectTree = new WildIDProjectTree(language, projects, projectRootItem);
        projectScrollPane = new ScrollPane();
        projectScrollPane.setFitToHeight(true);
        projectScrollPane.setFitToWidth(true);
        projectScrollPane.setContent(this.projectTree);

        // organization tab
        // create organization tree
        OrganizationListTreeItem orgRootItem = new OrganizationListTreeItem(language);
        this.orgTree = new WildIDOrganizationTree(language, orgs, orgRootItem);
        this.orgTree.getSelectionModel().select(orgRootItem);
        orgScrollPane = new ScrollPane();
        orgScrollPane.setFitToHeight(true);
        orgScrollPane.setFitToWidth(true);
        orgScrollPane.setContent(this.orgTree);

        // person tab
        // create person tree
        PersonListTreeItem personRootItem = new PersonListTreeItem(language);
        this.personTree = new WildIDPersonTree(language, persons, personRootItem);
        this.personTree.getSelectionModel().select(personRootItem);
        personScrollPane = new ScrollPane();
        personScrollPane.setFitToHeight(true);
        personScrollPane.setFitToWidth(true);
        personScrollPane.setContent(this.personTree);

        // camera model tab
        // create camera model tree
        CameraModelListTreeItem cameraModelRootItem = new CameraModelListTreeItem(language);
        this.cameraModelTree = new WildIDCameraModelTree(language, cameraModels, cameraModelRootItem);
        this.cameraModelTree.getSelectionModel().select(cameraModelRootItem);
        cameraModelScrollPane = new ScrollPane();
        cameraModelScrollPane.setFitToHeight(true);
        cameraModelScrollPane.setFitToWidth(true);
        cameraModelScrollPane.setContent(this.cameraModelTree);

        this.getChildren().add(projectScrollPane);
        AnchorPane.setTopAnchor(projectScrollPane, 0.0);
        AnchorPane.setLeftAnchor(projectScrollPane, 0.0);
        AnchorPane.setRightAnchor(projectScrollPane, 0.0);
        AnchorPane.setBottomAnchor(projectScrollPane, 0.0);
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.projectTree.setLanguage(language);
        this.orgTree.setLanguage(language);
        this.personTree.setLanguage(language);
        this.cameraModelTree.setLanguage(language);
        if (this.imageTree != null) {
            this.imageTree.setLanguage(language);
        }
        if (this.searchResultTree != null) {
            this.searchResultTree.setLanguage(language);
        }
    }

    public void setWildIDController(WildIDController controller) {

        this.controller = controller;

        this.projectTree.setWildIDController(controller);

        this.orgTree.getSelectionModel().selectedItemProperty().addListener(controller);
        this.personTree.getSelectionModel().selectedItemProperty().addListener(controller);
        this.cameraModelTree.getSelectionModel().selectedItemProperty().addListener(controller);

        if (this.imageTree != null) {
            this.imageTree.getSelectionModel().selectedItemProperty().addListener(controller);
        }
    }

    public void setProjectPane() {
        this.getChildren().clear();
        this.getChildren().addAll(projectScrollPane);
        AnchorPane.setTopAnchor(projectScrollPane, 0.0);
        AnchorPane.setLeftAnchor(projectScrollPane, 0.0);
        AnchorPane.setRightAnchor(projectScrollPane, 0.0);
        AnchorPane.setBottomAnchor(projectScrollPane, 0.0);
    }

    public void setOrganizationPane() {
        this.getChildren().clear();
        this.getChildren().addAll(orgScrollPane);
        AnchorPane.setTopAnchor(orgScrollPane, 0.0);
        AnchorPane.setLeftAnchor(orgScrollPane, 0.0);
        AnchorPane.setRightAnchor(orgScrollPane, 0.0);
        AnchorPane.setBottomAnchor(orgScrollPane, 0.0);
    }

    public void setPersonPane() {
        this.getChildren().clear();
        this.getChildren().addAll(personScrollPane);
        AnchorPane.setTopAnchor(personScrollPane, 0.0);
        AnchorPane.setLeftAnchor(personScrollPane, 0.0);
        AnchorPane.setRightAnchor(personScrollPane, 0.0);
        AnchorPane.setBottomAnchor(personScrollPane, 0.0);
    }

    public void setCameraModelPane() {
        this.getChildren().clear();
        this.getChildren().addAll(cameraModelScrollPane);
        AnchorPane.setTopAnchor(cameraModelScrollPane, 0.0);
        AnchorPane.setLeftAnchor(cameraModelScrollPane, 0.0);
        AnchorPane.setRightAnchor(cameraModelScrollPane, 0.0);
        AnchorPane.setBottomAnchor(cameraModelScrollPane, 0.0);
    }

    public final void setImagePane(int projectId, WildIDController controller) {
        Project project = null;
        for (Object object : projectTree.getRoot().getChildren()) {
            ProjectTreeItem tmpItem = (ProjectTreeItem) object;
            if (tmpItem.getProject().getProjectId() == projectId) {
                project = tmpItem.getProject();
                break;
            }
        }

        setImagePane(project, controller);
    }

    public void setImagePane(Project project, WildIDController controller) {
        if (project != null) {
            ProjectTreeItem projectRootItem = new ProjectTreeItem(project);
            this.imageTree = new WildIDImageTree(language, projectRootItem);
            this.imageTree.setWildIDController(controller);
            this.imageTree.getSelectionModel().select(projectRootItem);
            this.imageScrollPane = new ScrollPane();
            this.imageScrollPane.setFitToHeight(true);
            this.imageScrollPane.setFitToWidth(true);
            this.imageScrollPane.setContent(this.imageTree);

            projectRootItem.getChildren().addAll(
                    new ProjectImageListTreeItem(language)
            );

            if (controller != null) {
                this.imageTree.getSelectionModel().selectedItemProperty().addListener(controller);
            }
        }

        this.getChildren().clear();
        this.getChildren().addAll(imageScrollPane);
        AnchorPane.setTopAnchor(imageScrollPane, 0.0);
        AnchorPane.setLeftAnchor(imageScrollPane, 0.0);
        AnchorPane.setRightAnchor(imageScrollPane, 0.0);
        AnchorPane.setBottomAnchor(imageScrollPane, 0.0);

    }

    public void setSearchResultPane(
            Project[] selectedProjects,
            Date startDate,
            Date endDate,
            ImageType imageType,
            String genus,
            String species,
            TreeMap<Project, TreeMap<Event, TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>>>> projectMap) {

        SearchResultTreeItem searchResultRootItem = new SearchResultTreeItem(this.language, selectedProjects, startDate, endDate, imageType, genus, species);
        this.searchResultTree = new WildIDSearchResultTree(language, searchResultRootItem);

        for (Project project : projectMap.keySet()) {

            SearchResultProjectTreeItem projectItem = new SearchResultProjectTreeItem(project);
            searchResultRootItem.getChildren().add(projectItem);

            TreeMap<Event, TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>>> eventMap = projectMap.get(project);
            for (Event event : eventMap.keySet()) {

                SearchResultEventTreeItem eventItem = new SearchResultEventTreeItem(event);
                projectItem.getChildren().add(eventItem);

                TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>> arrayMap = eventMap.get(event);
                for (CameraTrapArray array : arrayMap.keySet()) {

                    SearchResultCameraTrapArrayTreeItem arrayItem = new SearchResultCameraTrapArrayTreeItem(array);
                    eventItem.getChildren().add(arrayItem);

                    TreeMap<CameraTrap, TreeSet<Image>> trapMap = arrayMap.get(array);
                    for (CameraTrap trap : trapMap.keySet()) {

                        SearchResultCameraTrapTreeItem trapItem = new SearchResultCameraTrapTreeItem(trap);
                        arrayItem.getChildren().add(trapItem);

                        for (Image img : trapMap.get(trap)) {
                            SearchResultImageTreeItem imageItem = new SearchResultImageTreeItem(img);
                            trapItem.getChildren().add(imageItem);
                        }
                    }
                }
            }
        }

        this.searchResultTree.getSelectionModel().select(searchResultRootItem);
        this.searchResultScrollPane = new ScrollPane();
        this.searchResultScrollPane.setFitToHeight(true);
        this.searchResultScrollPane.setFitToWidth(true);
        this.searchResultScrollPane.setContent(this.searchResultTree);
        this.searchResultTree.getSelectionModel().selectedItemProperty().addListener(controller);

        this.getChildren().clear();
        this.getChildren().addAll(searchResultScrollPane);
        AnchorPane.setTopAnchor(searchResultScrollPane, 0.0);
        AnchorPane.setLeftAnchor(searchResultScrollPane, 0.0);
        AnchorPane.setRightAnchor(searchResultScrollPane, 0.0);
        AnchorPane.setBottomAnchor(searchResultScrollPane, 0.0);
    }

    public void addNewProject(Project project) {
        this.projectTree.addNewProject(project);
    }

    public void updateProject(Project project) {
        this.projectTree.updateProject(project);
    }

    public void removeProject(Project project) {
        this.projectTree.removeProject(project);
    }

    public void showStandardView(Project project) {
        this.setProjectPane();
        this.projectTree.showStandardView(project);
    }

    public void showStandardImageView(Project project) {
        this.setImagePane(project.getProjectId(), this.controller);
        this.projectTree.showStandardView(project);
        this.imageTree.showStandardView();
    }

    public void addNewPerson(Project project, Person person) {
        this.projectTree.addNewPerson(project, person);
        this.personTree.addNewPerson(person);
    }

    public void addSelectedPerson(Project project, Person person) {
        this.projectTree.addNewPerson(project, person);
    }

    public void updatePerson(Project project, Person person) {
        this.projectTree.updatePerson(project, person);
        this.personTree.updatePerson(person);
    }

    public void removePerson(Project project, Person person) {
        this.projectTree.removePerson(project, person);
    }

    public void addNewOrganization(Project project, Organization org) {
        this.projectTree.addNewOrganization(project, org);
        this.orgTree.addNewOrganization(org);
    }

    public void updateOrganization(Project project, Organization org) {
        this.projectTree.updateOrganization(project, org);
    }

    public void removeOrganization(Project project, Organization org) {
        this.projectTree.removeOrganization(project, org);
    }

    public void addNewPerson(Person person) {
        this.personTree.addNewPerson(person);
    }

    public void updatePerson(Person person) {
        this.projectTree.updatePerson(person);
        this.personTree.updatePerson(person);
    }

    public void removePerson(Person person) {
        this.projectTree.removePerson(person);
        this.personTree.removePerson(person);
    }

    public void addNewOrganization(Organization org) {
        this.orgTree.addNewOrganization(org);
    }

    public void updateOrganization(Organization org) {
        this.projectTree.updateOrganization(org);
        this.orgTree.updateOrganization(org);
    }

    public void removeOrganization(Organization org) {
        this.projectTree.removeOrganization(org);
        this.orgTree.removeOrganization(org);
    }

    public void createNewCameraTrapArray(CameraTrapArray array) {
        this.projectTree.createNewCameraTrapArray(array);
    }

    public void removeCameraTrapArray(CameraTrapArray array) {
        this.projectTree.removeCameraTrapArray(array);
    }

    public void updateCameraTrapArray(CameraTrapArray array) {
        this.projectTree.updateCameraTrapArray(array);
    }

    public void createNewCameraTrap(CameraTrap trap) {
        this.projectTree.createNewCameraTrap(trap);
    }

    public void updateCameraTrap(CameraTrap trap) {
        this.projectTree.updateCameraTrap(trap);
    }

    public void cancelCameraTrapEditing(CameraTrap trap) {
        this.projectTree.cancelCameraTrapEditing(trap);
    }

    public void removeCameraTrap(CameraTrap trap) {
        this.projectTree.removeCameraTrap(trap);
    }

    public void selectCameraTrap(CameraTrap trap) {
        this.projectTree.selectCameraTrap(trap);
    }

    public void createEvent(Event event) {
        this.projectTree.createEvent(event);
    }

    public void removeEvent(Event event) {
        this.projectTree.removeEvent(event);
    }

    public void updateEvent(Event event) {
        this.projectTree.updateEvent(event);
    }

    public void createDeployment(Deployment deployment) {
        //this.projectTree.createDeployment(deployment);
        this.imageTree.createDeployment(deployment);
    }

    public void updateDeployment(Deployment deployment) {
        //this.projectTree.updateDeployment(deployment);
        this.imageTree.updateDeployment(deployment);
    }

    public void removeDeployment(Deployment deployment) {
        //this.projectTree.removeDeployment(deployment);

        if (this.imageTree != null) {
            this.imageTree.removeDeployment(deployment);
        }
    }

    public void createCameraModel(CameraModel cameraModel) {
        this.cameraModelTree.createCameraModel(cameraModel);
    }

    public void updateCameraModel(String oldMaker, String oldModel, CameraModel cameraModel) {
        this.cameraModelTree.updateCameraModel(oldMaker, oldModel, cameraModel);
    }

    public void deleteCameraModel(CameraModel cameraModel) {
        this.cameraModelTree.deleteCameraModel(cameraModel);
    }

    public void saveExifCameraModelFeatures(CameraModel cameraModel) {
        this.projectTree.saveExifCameraModelFeatures(cameraModel);
    }

    public void saveImageAnnotation(List<Image> images) {
        //this.projectTree.saveImageAnnotation(images);

        if (this.imageTree != null) {
            this.imageTree.saveImageAnnotation(images);
        }
    }

    public void updatePreference() {
        this.projectTree.updatePreference();
    }

    public WildIDProjectTree getProjectTree() {
        return this.projectTree;
    }

    public WildIDOrganizationTree getOrganizationTree() {
        return this.orgTree;
    }

    public WildIDPersonTree getPersonTree() {
        return this.personTree;
    }

    public WildIDCameraModelTree getCameraModelTree() {
        return this.cameraModelTree;
    }

    public WildIDImageTree getImageTree() {
        return this.imageTree;
    }

    public boolean isImageTreeActive() {
        return this.getChildren().contains(imageScrollPane);
    }
}
