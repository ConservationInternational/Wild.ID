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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.CameraTrapArrayComparator;
import org.wildid.entity.CameraTrapComparator;
import org.wildid.entity.Event;
import org.wildid.entity.EventComparator;
import org.wildid.entity.Organization;
import org.wildid.entity.Person;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectOrganization;
import org.wildid.entity.ProjectPersonRole;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDProjectTree extends TreeView implements LanguageChangable {

    private LanguageModel language;
    private WildIDController controller;
    private ObservableList<Project> projects;
    private ProjectListTreeItem rootItem;

    public WildIDProjectTree(
            LanguageModel language,
            ObservableList<Project> projects,
            ProjectListTreeItem rootItem) {

        super(rootItem);
        this.rootItem = rootItem;
        this.rootItem.setExpanded(true);

        this.language = language;
        this.projects = projects;

        projects.stream().map((project) -> new ProjectTreeItem(project)).map((projectTreeItem) -> {
            rootItem.getChildren().add(projectTreeItem);
            if (projects.size() == 1) {
                projectTreeItem.setExpanded(true);
            }

            return projectTreeItem;
        }).forEach((ProjectTreeItem projectTreeItem) -> {

            ProjectPersonListTreeItem personListTreeItem
                    = new ProjectPersonListTreeItem(language);
            ProjectOrganizationListTreeItem orgListTreeItem
                    = new ProjectOrganizationListTreeItem(language);
            ProjectCameraTrapArrayListTreeItem arrayListTreeItem
                    = new ProjectCameraTrapArrayListTreeItem(language);
            ProjectEventListTreeItem eventListTreeItem
                    = new ProjectEventListTreeItem(language);

            projectTreeItem.getChildren().addAll(
                    orgListTreeItem,
                    personListTreeItem,
                    new ProjectCameraListTreeItem(language),
                    arrayListTreeItem,
                    eventListTreeItem
            //new ProjectImageListTreeItem(language)
            );

            // add tree items for all persons in the project
            Project project = projectTreeItem.getProject();
            Set<ProjectPersonRole> pprs = project.getProjectPersonRoles();
            List<Person> members = new ArrayList<>();
            pprs.stream().forEach((ppr) -> {
                Person person = ppr.getPerson();
                boolean found = false;
                for (Person member : members) {
                    if (member.getPersonId() == person.getPersonId().intValue()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    boolean added = false;
                    String displayName = ppr.getPerson().getLastName() + " " + ppr.getPerson().getFirstName();
                    int position = -1;
                    for (Person member : members) {
                        String memberDisplayName = member.getLastName() + " " + member.getFirstName();
                        position++;
                        if (displayName.compareTo(memberDisplayName) < 0) {
                            members.add(position, ppr.getPerson());
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        members.add(ppr.getPerson());
                    }
                }
            });

            members.stream().map((person) -> new ProjectPersonTreeItem(project, person)).forEach((personTreeItem) -> {
                personListTreeItem.getChildren().add(personTreeItem);
            });

            // add tree items for all organizations in the project
            Set<ProjectOrganization> pos = project.getProjectOrganizations();
            pos.stream().forEach((po) -> {
                orgListTreeItem.getChildren().add(new ProjectOrganizationTreeItem(project, po.getOrganization()));
            });

            // add tree items for all camera trap arrays in the project
            Set<CameraTrapArray> arraySet = project.getCameraTrapArrays();
            TreeSet<CameraTrapArray> arrays = new TreeSet<>(new CameraTrapArrayComparator());
            arrays.addAll(arraySet);
            for (CameraTrapArray array : arrays) {
                ProjectCameraTrapArrayTreeItem arrayTreeItem = new ProjectCameraTrapArrayTreeItem(array);
                arrayListTreeItem.getChildren().add(arrayTreeItem);

                TreeSet<CameraTrap> traps = new TreeSet<>(new CameraTrapComparator());
                traps.addAll(array.getCameraTraps());
                for (CameraTrap trap : traps) {
                    ProjectCameraTrapTreeItem trapTreeItem = new ProjectCameraTrapTreeItem(trap);
                    arrayTreeItem.getChildren().add(trapTreeItem);
                }
            }

            // add tree items for all events in the project
            Set<Event> events = project.getEvents();
            for (Event event : events) {
                ProjectEventTreeItem eventTreeItem = new ProjectEventTreeItem(event);
                eventListTreeItem.getChildren().add(eventTreeItem);
            }

        });

        this.setStyle("-fx-background-color:gray;");

        this.requestFocus();
        this.getSelectionModel().select(rootItem);

    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        setItemLanguage(this.rootItem, language);
    }

    private void setItemLanguage(TreeItem treeItem, LanguageModel language) {

        if (treeItem instanceof LanguageChangable) {
            ((LanguageChangable) treeItem).setLanguage(language);
        }

        treeItem.getChildren().stream().forEach((object) -> {
            setItemLanguage((TreeItem) object, language);
        });

    }

    public void setWildIDController(WildIDController controller) {
        this.getSelectionModel().selectedItemProperty().addListener(controller);
        this.controller = controller;
    }

    public void addNewProject(Project project) {

        int index = -1;
        for (int i = 0; i < projects.size(); i++) {
            Project proj = projects.get(i);
            if (project.getName().compareTo(proj.getName()) < 0) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            this.projects.add(project);
        } else {
            this.projects.add(index, project);
        }

        TreeItem<String> rootItem = this.getRoot();
        ProjectTreeItem projectTreeItem = new ProjectTreeItem(project);

        if (index < 0) {
            rootItem.getChildren().add(projectTreeItem);
        } else {
            rootItem.getChildren().add(index, projectTreeItem);
        }
        projectTreeItem.getChildren().addAll(
                new ProjectOrganizationListTreeItem(language),
                new ProjectPersonListTreeItem(language),
                new ProjectCameraListTreeItem(language),
                new ProjectCameraTrapArrayListTreeItem(language),
                new ProjectEventListTreeItem(language)
        //new ProjectImageListTreeItem(language)
        );

        for (TreeItem item : rootItem.getChildren()) {
            item.setExpanded(false);
        }
        projectTreeItem.setExpanded(true);

        this.requestFocus();
        this.getSelectionModel().select(projectTreeItem);
        this.getFocusModel().focus(index + 1);
    }

    public void updateProject(Project project) {

        TreeItem<String> rootItem = this.getRoot();
        for (Object object : rootItem.getChildren()) {
            ProjectTreeItem item = (ProjectTreeItem) object;
            if (item.getProject().getProjectId().longValue() == project.getProjectId().longValue()) {
                item.setProject(project);
                break;
            }
        }

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("project_update_confirm_header"),
                language.getString("project_update_confirm_context"),
                language.getString("alert_ok"));
    }

    public void removeProject(Project project) {

        TreeItem<String> rootItem = this.getRoot();
        for (Object object : rootItem.getChildren()) {
            ProjectTreeItem item = (ProjectTreeItem) object;
            if (item.getProject().getProjectId().longValue() == project.getProjectId().longValue()) {
                rootItem.getChildren().remove(item);
                break;
            }
        }

        this.requestFocus();
        this.getSelectionModel().select(0);
        this.getFocusModel().focus(0);
    }

    public void showStandardView(Project project) {

        TreeItem<String> rootItem = this.getRoot();
        ProjectTreeItem projectTreeItem = null;
        int index = 0;
        for (Object object : rootItem.getChildren()) {
            ProjectTreeItem item = (ProjectTreeItem) object;
            if (item.getProject().getProjectId().longValue() == project.getProjectId().longValue()) {
                projectTreeItem = item;
                collapesAll(projectTreeItem);
                item.setExpanded(true);
            } else {
                item.setExpanded(false);
            }
            index++;
        }

        for (Object object : projectTreeItem.getChildren()) {
            TreeItem item = (TreeItem) object;
            item.setExpanded(false);
        }

        this.requestFocus();
        this.getSelectionModel().select(projectTreeItem);
        this.getFocusModel().focus(index);

    }

    public void collapesAll(TreeItem treeItem) {
        treeItem.setExpanded(false);
        for (Object object : treeItem.getChildren()) {
            TreeItem tmpItem = (TreeItem) object;
            collapesAll(tmpItem);
        }
    }

    public void addNewPerson(Project project, Person person) {

        ProjectPersonListTreeItem personListTreeItem = getPersonListTreeItem(project);
        ProjectPersonTreeItem newPersonTreeItem = new ProjectPersonTreeItem(project, person);
        ObservableList items = personListTreeItem.getChildren();
        boolean added = false;
        for (int i = 0; i < items.size(); i++) {
            ProjectPersonTreeItem personTreeItem = (ProjectPersonTreeItem) items.get(i);
            Person itemPerson = personTreeItem.getPerson();
            String displayName = person.getLastName() + " " + person.getFirstName();
            String itemName = itemPerson.getLastName() + " " + itemPerson.getFirstName();
            if (displayName.compareTo(itemName) < 0) {
                items.add(i, newPersonTreeItem);
                added = true;
                break;
            }
        }

        if (!added) {
            items.add(newPersonTreeItem);
        }

        this.requestFocus();
        this.getSelectionModel().select(newPersonTreeItem);

    }

    public void updatePerson(Project project, Person person) {

        for (Project proj : projects) {
            Set<ProjectPersonRole> pprs = proj.getProjectPersonRoles();
            for (ProjectPersonRole ppr : pprs) {
                if (ppr.getPerson().getPersonId() == person.getPersonId().intValue()) {
                    ProjectPersonTreeItem personTreeItem = getPersonTreeItem(ppr.getProject(), person);
                    personTreeItem.setPerson(person);
                }
            }
        }

        ProjectPersonTreeItem personTreeItem = getPersonTreeItem(project, person);
        this.requestFocus();
        this.getSelectionModel().select(personTreeItem);

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("person_update_confirm_header"),
                language.getString("person_update_confirm_context"),
                language.getString("alert_ok"));
    }

    public void removePerson(Project project, Person person) {

        ProjectPersonListTreeItem personListTreeItem = getPersonListTreeItem(project);
        for (Object object : personListTreeItem.getChildren()) {
            ProjectPersonTreeItem item = (ProjectPersonTreeItem) object;
            if (item.getPerson().getPersonId().longValue() == person.getPersonId().longValue()) {
                personListTreeItem.getChildren().remove(item);
                break;
            }
        }

        this.requestFocus();
        this.getSelectionModel().select(personListTreeItem);
    }

    public ProjectTreeItem getProjectTreeItem(int projectId) {

        TreeItem<String> rootItem = this.getRoot();
        for (Object object : rootItem.getChildren()) {
            ProjectTreeItem item = (ProjectTreeItem) object;
            if (item.getProject().getProjectId().intValue() == projectId) {
                return item;
            }
        }
        return null;
    }

    public ProjectTreeItem getProjectTreeItem(Project project) {
        int projectId = project.getProjectId().intValue();
        return getProjectTreeItem(projectId);
    }

    public ProjectPersonListTreeItem getPersonListTreeItem(Project project) {

        ProjectTreeItem projectTreeItem = getProjectTreeItem(project);
        if (projectTreeItem != null) {
            for (Object object : projectTreeItem.getChildren()) {
                if (object instanceof ProjectPersonListTreeItem) {
                    return (ProjectPersonListTreeItem) object;
                }
            }
        }
        return null;
    }

    public ProjectPersonTreeItem getPersonTreeItem(Project project, Person person) {

        ProjectPersonListTreeItem personListTreeItem = getPersonListTreeItem(project);
        if (personListTreeItem != null) {
            for (Object object : personListTreeItem.getChildren()) {
                if (object instanceof ProjectPersonTreeItem) {
                    ProjectPersonTreeItem personTreeItem = (ProjectPersonTreeItem) object;
                    if (personTreeItem.getPerson().getPersonId() == person.getPersonId().intValue()) {
                        return personTreeItem;
                    }
                }
            }
        }
        return null;
    }

    public List<ProjectPersonTreeItem> getProjectPersonTreeItems(Person person) {

        List<ProjectPersonTreeItem> personTreeItemList = new ArrayList<>();
        for (ProjectPersonListTreeItem personsTreeItem : getProjectPersonListTreeItems()) {
            for (Object object : personsTreeItem.getChildren()) {
                ProjectPersonTreeItem personTreeItem = (ProjectPersonTreeItem) object;
                Person p = personTreeItem.getPerson();
                if (p.getPersonId().intValue() == person.getPersonId().intValue()) {
                    personTreeItemList.add(personTreeItem);
                }
            }
        }
        return personTreeItemList;
    }

    public List<ProjectPersonListTreeItem> getProjectPersonListTreeItems() {

        List<ProjectPersonListTreeItem> personsTreeItemList = new ArrayList<>();
        for (Object object : rootItem.getChildren()) {
            ProjectTreeItem projectTreeItem = (ProjectTreeItem) object;
            for (Object tmp : projectTreeItem.getChildren()) {
                if (tmp instanceof ProjectPersonListTreeItem) {
                    personsTreeItemList.add((ProjectPersonListTreeItem) tmp);
                }
            }
        }
        return personsTreeItemList;
    }

    public List<ProjectOrganizationTreeItem> getProjectOrganizationTreeItems(Organization org) {

        List<ProjectOrganizationTreeItem> orgTreeItemList = new ArrayList<>();
        for (ProjectOrganizationListTreeItem orgsTreeItem : getProjectOrganizationListTreeItems()) {
            for (Object object : orgsTreeItem.getChildren()) {
                ProjectOrganizationTreeItem orgTreeItem = (ProjectOrganizationTreeItem) object;
                Organization p = orgTreeItem.getOrganization();
                if (p.getOrganizationId() == org.getOrganizationId().intValue()) {
                    orgTreeItemList.add(orgTreeItem);
                }
            }
        }
        return orgTreeItemList;
    }

    public List<ProjectOrganizationListTreeItem> getProjectOrganizationListTreeItems() {

        List<ProjectOrganizationListTreeItem> orgsTreeItemList = new ArrayList<>();
        for (Object object : rootItem.getChildren()) {
            ProjectTreeItem projectTreeItem = (ProjectTreeItem) object;
            for (Object tmp : projectTreeItem.getChildren()) {
                if (tmp instanceof ProjectOrganizationListTreeItem) {
                    orgsTreeItemList.add((ProjectOrganizationListTreeItem) tmp);
                }
            }
        }
        return orgsTreeItemList;
    }

    public ProjectOrganizationListTreeItem getOrganizationListTreeItem(Project project) {

        ProjectTreeItem projectTreeItem = getProjectTreeItem(project);
        if (projectTreeItem != null) {
            for (Object object : projectTreeItem.getChildren()) {
                if (object instanceof ProjectOrganizationListTreeItem) {
                    return (ProjectOrganizationListTreeItem) object;
                }
            }
        }
        return null;
    }

    public ProjectCameraTrapArrayListTreeItem getCameraTrapArrayListTreeItem(Project project) {

        ProjectTreeItem projectTreeItem = getProjectTreeItem(project);
        if (projectTreeItem != null) {
            for (Object object : projectTreeItem.getChildren()) {
                if (object instanceof ProjectCameraTrapArrayListTreeItem) {
                    return (ProjectCameraTrapArrayListTreeItem) object;
                }
            }
        }
        return null;
    }

    public ProjectEventListTreeItem getEventListTreeItem(Project project) {

        ProjectTreeItem projectTreeItem = getProjectTreeItem(project);
        if (projectTreeItem != null) {
            for (Object object : projectTreeItem.getChildren()) {
                if (object instanceof ProjectEventListTreeItem) {
                    return (ProjectEventListTreeItem) object;
                }
            }
        }
        return null;
    }

    public ProjectImageListTreeItem getImageListTreeItem(Project project) {

        ProjectTreeItem projectTreeItem = getProjectTreeItem(project);
        if (projectTreeItem != null) {
            for (Object object : projectTreeItem.getChildren()) {
                if (object instanceof ProjectImageListTreeItem) {
                    return (ProjectImageListTreeItem) object;
                }
            }
        }
        return null;
    }

    public ProjectCameraTrapArrayTreeItem getCameraTrapArrayTreeItem(CameraTrapArray array) {

        ProjectCameraTrapArrayListTreeItem arrayListTreeItem = getCameraTrapArrayListTreeItem(array.getProject());
        if (arrayListTreeItem != null) {
            for (Object object : arrayListTreeItem.getChildren()) {
                ProjectCameraTrapArrayTreeItem arrayTreeItem = (ProjectCameraTrapArrayTreeItem) object;
                CameraTrapArray arrayItem = arrayTreeItem.getCameraTrapArray();
                if (arrayItem.getCameraTrapArrayId().intValue() == array.getCameraTrapArrayId().intValue()) {
                    return arrayTreeItem;
                }
            }
        }
        return null;
    }

    public ProjectCameraTrapTreeItem getCameraTrapTreeItem(CameraTrap trap) {
        ProjectCameraTrapArrayTreeItem arrayTreeItem = getCameraTrapArrayTreeItem(trap.getCameraTrapArray());
        if (arrayTreeItem != null) {
            for (Object object : arrayTreeItem.getChildren()) {
                ProjectCameraTrapTreeItem trapTreeItem = (ProjectCameraTrapTreeItem) object;
                CameraTrap trapItem = trapTreeItem.getCameraTrap();
                if (trapItem.getCameraTrapId().intValue() == trap.getCameraTrapId().intValue()) {
                    return trapTreeItem;
                }
            }
        }
        return null;
    }

    public ProjectEventTreeItem getEventTreeItem(Event event) {

        ProjectEventListTreeItem eventListTreeItem = getEventListTreeItem(event.getProject());
        if (eventListTreeItem != null) {
            for (Object object : eventListTreeItem.getChildren()) {
                ProjectEventTreeItem eventTreeItem = (ProjectEventTreeItem) object;
                Event eventItem = eventTreeItem.getEvent();
                if (eventItem.getEventId().intValue() == event.getEventId().intValue()) {
                    return eventTreeItem;
                }
            }
        }
        return null;
    }

    public ProjectOrganizationTreeItem getOrganizationTreeItem(Project project, Organization org) {

        ProjectOrganizationListTreeItem orgListTreeItem = getOrganizationListTreeItem(project);
        if (orgListTreeItem != null) {
            for (Object object : orgListTreeItem.getChildren()) {
                if (object instanceof ProjectOrganizationTreeItem) {
                    ProjectOrganizationTreeItem orgTreeItem = (ProjectOrganizationTreeItem) object;
                    if (orgTreeItem.getOrganization().getOrganizationId()
                            == org.getOrganizationId().intValue()) {
                        return orgTreeItem;
                    }
                }
            }
        }
        return null;
    }

    public void addNewOrganization(Project project, Organization org) {

        ProjectOrganizationListTreeItem institutionListTreeItem = getOrganizationListTreeItem(project);
        ProjectOrganizationTreeItem newInstitutionTreeItem = new ProjectOrganizationTreeItem(project, org);
        ObservableList items = institutionListTreeItem.getChildren();
        boolean added = false;
        for (int i = 0; i < items.size(); i++) {
            ProjectOrganizationTreeItem institutionTreeItem = (ProjectOrganizationTreeItem) items.get(i);
            Organization itemInstitution = institutionTreeItem.getOrganization();
            String displayName = org.getName();
            String itemName = itemInstitution.getName();
            if (displayName.compareTo(itemName) < 0) {
                items.add(i, newInstitutionTreeItem);
                added = true;
                break;
            }
        }

        if (!added) {
            items.add(newInstitutionTreeItem);
        }

        //this.requestFocus();
        this.getSelectionModel().select(newInstitutionTreeItem);
    }

    public void updateOrganization(Project project, Organization org) {

        for (Project proj : projects) {
            Set<ProjectOrganization> pos = proj.getProjectOrganizations();
            for (ProjectOrganization po : pos) {
                if (po.getOrganization().getOrganizationId().intValue() == org.getOrganizationId().intValue()) {
                    ProjectOrganizationTreeItem orgTreeItem = getOrganizationTreeItem(po.getProject(), org);
                    orgTreeItem.setOrganization(org);
                }
            }
        }

        ProjectOrganizationTreeItem orgTreeItem = getOrganizationTreeItem(project, org);
        //this.requestFocus();
        this.getSelectionModel().select(orgTreeItem);

    }

    public void removeOrganization(Project project, Organization org) {

        ProjectOrganizationListTreeItem orgListTreeItem = getOrganizationListTreeItem(project);
        for (Object object : orgListTreeItem.getChildren()) {
            ProjectOrganizationTreeItem item = (ProjectOrganizationTreeItem) object;
            if (item.getOrganization().getOrganizationId().longValue() == org.getOrganizationId().longValue()) {
                orgListTreeItem.getChildren().remove(item);
                break;
            }
        }

        //this.requestFocus();
        this.getSelectionModel().select(orgListTreeItem);
    }

    public void updatePerson(Person person) {

        List<ProjectPersonTreeItem> personTreeItemList = getProjectPersonTreeItems(person);
        ProjectPersonTreeItem personTreeItem = null;
        for (ProjectPersonTreeItem item : personTreeItemList) {
            personTreeItem = (ProjectPersonTreeItem) item;
            Person p = personTreeItem.getPerson();
            if (p.getPersonId() == person.getPersonId().intValue()) {
                personTreeItem.setPerson(person);
            }
        }

        this.requestFocus();
        this.getSelectionModel().select(personTreeItem);

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("person_update_confirm_header"),
                language.getString("person_update_confirm_context"),
                language.getString("alert_ok"));
    }

    public void removePerson(Person person) {

        List<ProjectPersonTreeItem> personTreeItemList = getProjectPersonTreeItems(person);
        personTreeItemList.stream().forEach((item) -> {
            item.getParent().getChildren().remove(item);
        });

        //this.requestFocus();
        this.getSelectionModel().select(rootItem);
    }

    public void updateOrganization(Organization org) {

        List<ProjectOrganizationTreeItem> orgTreeItemList = getProjectOrganizationTreeItems(org);
        ProjectOrganizationTreeItem orgTreeItem = null;
        for (ProjectOrganizationTreeItem item : orgTreeItemList) {
            orgTreeItem = (ProjectOrganizationTreeItem) item;
            Organization p = orgTreeItem.getOrganization();
            if (p.getOrganizationId() == org.getOrganizationId().intValue()) {
                orgTreeItem.setOrganization(org);
            }
        }

        //this.requestFocus();
        this.getSelectionModel().select(orgTreeItem);

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("org_update_confirm_header"),
                language.getString("org_update_confirm_context"),
                language.getString("alert_ok"));
    }

    public void removeOrganization(Organization org) {

        List<ProjectOrganizationTreeItem> orgTreeItemList = getProjectOrganizationTreeItems(org);
        orgTreeItemList.stream().forEach((item) -> {
            item.getParent().getChildren().remove(item);
        });

        //this.requestFocus();
        this.getSelectionModel().select(rootItem);
    }

    public void createNewCameraTrapArray(CameraTrapArray array) {

        ProjectCameraTrapArrayListTreeItem arrayListTreeItem = getCameraTrapArrayListTreeItem(array.getProject());
        ProjectCameraTrapArrayTreeItem arrayTreeItem = new ProjectCameraTrapArrayTreeItem(array);
        ObservableList items = arrayListTreeItem.getChildren();
        boolean added = false;
        for (int i = 0; i < items.size(); i++) {
            ProjectCameraTrapArrayTreeItem itemTreeItem = (ProjectCameraTrapArrayTreeItem) items.get(i);
            CameraTrapArray arrayItem = itemTreeItem.getCameraTrapArray();
            if (new CameraTrapArrayComparator().compare(array, arrayItem) < 0) {
                items.add(i, arrayTreeItem);
                added = true;
                break;
            }
        }

        if (!added) {
            items.add(arrayTreeItem);
        }

        //this.requestFocus();
        this.getSelectionModel().select(arrayTreeItem);
        this.scrollTo(this.getSelectionModel().getSelectedIndex());

    }

    public void removeCameraTrapArray(CameraTrapArray array) {

        ProjectCameraTrapArrayListTreeItem arrayListTreeItem = getCameraTrapArrayListTreeItem(array.getProject());
        for (Object object : arrayListTreeItem.getChildren()) {
            ProjectCameraTrapArrayTreeItem item = (ProjectCameraTrapArrayTreeItem) object;
            if (item.getCameraTrapArray().getCameraTrapArrayId().longValue() == array.getCameraTrapArrayId().longValue()) {
                arrayListTreeItem.getChildren().remove(item);
                break;
            }
        }

        //this.requestFocus();
        //this.getSelectionModel().select(arrayListTreeItem);
    }

    public void updateCameraTrapArray(CameraTrapArray array) {
        ProjectCameraTrapArrayTreeItem arrayTreeItem = getCameraTrapArrayTreeItem(array);
        arrayTreeItem.setCameraTrapArray(array);

        // sort the camera trap arrays
        ProjectCameraTrapArrayListTreeItem arrayListTreeItem = (ProjectCameraTrapArrayListTreeItem) arrayTreeItem.getParent();
        arrayListTreeItem.getChildren().remove(arrayTreeItem);

        ObservableList items = arrayListTreeItem.getChildren();
        boolean added = false;
        for (int i = 0; i < items.size(); i++) {
            ProjectCameraTrapArrayTreeItem itemTreeItem = (ProjectCameraTrapArrayTreeItem) items.get(i);
            CameraTrapArray arrayItem = itemTreeItem.getCameraTrapArray();
            if (new CameraTrapArrayComparator().compare(array, arrayItem) < 0) {
                items.add(i, arrayTreeItem);
                added = true;
                break;
            }
        }

        if (!added) {
            items.add(arrayTreeItem);
        }

        //this.requestFocus();
        this.getSelectionModel().select(arrayTreeItem);

    }

    public void createNewCameraTrap(CameraTrap trap) {
        ProjectCameraTrapArrayTreeItem arrayTreeItem = getCameraTrapArrayTreeItem(trap.getCameraTrapArray());
        ProjectCameraTrapTreeItem trapTreeItem = new ProjectCameraTrapTreeItem(trap);

        ObservableList items = arrayTreeItem.getChildren();
        boolean added = false;
        for (int i = 0; i < items.size(); i++) {
            ProjectCameraTrapTreeItem itemTreeItem = (ProjectCameraTrapTreeItem) items.get(i);
            CameraTrap trapItem = itemTreeItem.getCameraTrap();
            if (new CameraTrapComparator().compare(trap, trapItem) < 0) {
                items.add(i, trapTreeItem);
                added = true;
                break;
            }
        }

        if (!added) {
            items.add(trapTreeItem);
        }

        //this.requestFocus();
        this.getSelectionModel().select(trapTreeItem);

    }

    public void cancelCameraTrapEditing(CameraTrap trap) {

        //ProjectCameraTrapTreeItem trapTreeItem = getCameraTrapTreeItem(trap);
        this.getSelectionModel().select(null);

    }

    public void updateCameraTrap(CameraTrap trap) {

        //System.out.println("----> update trap: " + trap.getName());
        ProjectCameraTrapTreeItem trapTreeItem = getCameraTrapTreeItem(trap);

        //System.out.println("      find node: " + trapTreeItem);
        // sort the camera traps
        ProjectCameraTrapArrayTreeItem arrayTreeItem = (ProjectCameraTrapArrayTreeItem) trapTreeItem.getParent();
        CameraTrapArray array = arrayTreeItem.getCameraTrapArray();

        //System.out.println("      array: " + array.getName() + " has traps " + array.getCameraTraps().size());
        arrayTreeItem.getChildren().clear();

        //System.out.println("      clear all children of the array: " + array.getName());
        TreeSet<CameraTrap> traps = new TreeSet<>(new CameraTrapComparator());
        traps.addAll(array.getCameraTraps());
        for (CameraTrap aTrap : traps) {
            //System.out.println("    add tree item for " + aTrap.getName());
            ProjectCameraTrapTreeItem aTrapTreeItem = new ProjectCameraTrapTreeItem(aTrap);
            if (aTrap.getCameraTrapId() == trap.getCameraTrapId().intValue()) {
                trapTreeItem = aTrapTreeItem;
            }
            arrayTreeItem.getChildren().add(aTrapTreeItem);
        }

        //this.requestFocus();
        this.getSelectionModel().select(trapTreeItem);

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("trap_update_confirm_header"),
                language.getString("trap_update_confirm_context"),
                language.getString("alert_ok"));
    }

    public void removeCameraTrap(CameraTrap trap) {

        ProjectCameraTrapTreeItem trapTreeItem = getCameraTrapTreeItem(trap);
        trapTreeItem.getParent().getChildren().remove(trapTreeItem);
        this.getSelectionModel().clearSelection();

    }

    public void selectCameraTrap(CameraTrap trap) {
        ProjectCameraTrapTreeItem trapTreeItem = getCameraTrapTreeItem(trap);
        this.getSelectionModel().select(trapTreeItem);
    }

    public void createEvent(Event event) {

        ProjectEventListTreeItem eventListTreeItem = getEventListTreeItem(event.getProject());
        ProjectEventTreeItem eventTreeItem = new ProjectEventTreeItem(event);

        ObservableList items = eventListTreeItem.getChildren();
        boolean added = false;
        for (int i = 0; i < items.size(); i++) {
            ProjectEventTreeItem itemTreeItem = (ProjectEventTreeItem) items.get(i);
            Event eventItem = itemTreeItem.getEvent();
            if (new EventComparator().compare(event, eventItem) < 0) {
                items.add(i, eventTreeItem);
                added = true;
                break;
            }
        }

        if (!added) {
            items.add(eventTreeItem);
        }

        this.getSelectionModel().select(eventTreeItem);
    }

    public void removeEvent(Event event) {

        ProjectEventTreeItem eventTreeItem = getEventTreeItem(event);
        eventTreeItem.getParent().getChildren().remove(eventTreeItem);
    }

    public void updateEvent(Event event) {

        ProjectEventTreeItem eventTreeItem = null;

        // sort the events
        ProjectEventListTreeItem eventListTreeItem = getEventListTreeItem(event.getProject());
        eventListTreeItem.getChildren().clear();

        TreeSet<Event> events = new TreeSet<>(new EventComparator());
        events.addAll(event.getProject().getEvents());
        for (Event evt : events) {
            ProjectEventTreeItem evtTreeItem = new ProjectEventTreeItem(evt);
            if (evt.getEventId() == event.getEventId().intValue()) {
                eventTreeItem = evtTreeItem;
            }
            eventListTreeItem.getChildren().add(evtTreeItem);
        }

        this.getSelectionModel().select(eventTreeItem);

        Util.alertInformationPopup(
                language.getString("title_success"),
                language.getString("event_update_confirm_header"),
                language.getString("event_update_confirm_context"),
                language.getString("alert_ok"));
    }

    public void saveExifCameraModelFeatures(CameraModel cameraModel) {
        this.getSelectionModel().select(rootItem);
    }

    public void updatePreference() {
        this.getSelectionModel().select(rootItem);
    }

}
