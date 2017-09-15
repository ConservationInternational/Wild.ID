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
package org.team.wildid.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.wildid.app.LanguageModel;
import org.wildid.app.ProgressForm;
import org.wildid.app.WildID;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Country;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.Image;
import org.wildid.entity.ImageRepository;
import org.wildid.entity.ImageSpecies;
import org.wildid.entity.Organization;
import org.wildid.entity.Person;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectOrganization;
import org.wildid.entity.ProjectOrganizationId;
import org.wildid.entity.ProjectPersonRole;
import org.wildid.entity.Role;
import org.wildid.service.CameraModelService;
import org.wildid.service.CameraModelServiceImpl;
import org.wildid.service.CameraService;
import org.wildid.service.CameraServiceImpl;
import org.wildid.service.CameraTrapArrayService;
import org.wildid.service.CameraTrapArrayServiceImpl;
import org.wildid.service.CameraTrapService;
import org.wildid.service.CameraTrapServiceImpl;
import org.wildid.service.CountryService;
import org.wildid.service.CountryServiceImpl;
import org.wildid.service.DeploymentService;
import org.wildid.service.DeploymentServiceImpl;
import org.wildid.service.EventService;
import org.wildid.service.EventServiceImpl;
import org.wildid.service.ImageService;
import org.wildid.service.ImageServiceImpl;
import org.wildid.service.OrganizationService;
import org.wildid.service.OrganizationServiceImpl;
import org.wildid.service.PersonService;
import org.wildid.service.PersonServiceImpl;
import org.wildid.service.ProjectService;
import org.wildid.service.ProjectServiceImpl;
import org.wildid.service.RoleService;
import org.wildid.service.RoleServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class TEAMPluginController implements javafx.event.EventHandler<ActionEvent> {

    private DefaultHttpClient validHttpclient;
    private TreeMap<String, Integer> sites;
    private TEAMPlugin plugin;
    private LanguageModel language;

    @Override
    public void handle(ActionEvent event) {

        language = new LanguageModel(WildID.preference.getLanguage());
        language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));

        Object object = event.getSource();
        if (object instanceof MenuItem) {
            MenuItem item = (MenuItem) object;
            plugin = (TEAMPlugin) item.getParentMenu();
            String id = item.getId();
            if (id != null) {
                if (id.equals("sync")) {

                    if (validHttpclient == null) {
                        // login to the TEAM
                        Dialog<Pair<String, String>> dialog = getLoginDialog(language);
                        Optional<Pair<String, String>> result = dialog.showAndWait();
                        result.ifPresent(usernamePassword -> {
                            try {
                                DefaultHttpClient httpclient = new DefaultHttpClient();
                                if (loginToTEAMNetwork(httpclient, usernamePassword.getKey(), usernamePassword.getValue())) {
                                    validHttpclient = httpclient;
                                } else {
                                    alertErrorPopup(
                                            language.getString("TEAM-title_error"),
                                            language.getString("TEAM-login-error-header"),
                                            language.getString("TEAM-login-error-content"),
                                            language.getString("TEAM-alert_ok")
                                    );
                                }
                            } catch (Exception ex) {
                                alertErrorPopup(
                                        language.getString("TEAM-title_error"),
                                        language.getString("TEAM-login-error-header"),
                                        language.getString("TEAM-login-error-content\n" + ex.getMessage()),
                                        language.getString("TEAM-alert_ok")
                                );
                            }
                        });
                    }

                    if (validHttpclient != null) {
                        try {
                            sites = getAssociatedTEAMSite(validHttpclient);
                            if (sites != null && sites.size() > 0) {
                                List<String> choices = new ArrayList<>();
                                choices.addAll(sites.keySet());
                                ChoiceDialog<String> siteDialog = getSiteDialog(language, choices);
                                Optional<String> siteResult = siteDialog.showAndWait();

                                String site = null;
                                if (siteResult.isPresent()) {
                                    site = siteResult.get();
                                }

                                if (site != null) {
                                    ProgressForm pForm = new ProgressForm(
                                            language.getString("TEAM-site-button"),
                                            language.getString("TEAM-sync-up-running-content"));
                                    //pForm.removeProgressBar();
                                    pForm.getDialogStage().show();

                                    SyncUpTask task = new SyncUpTask(language, this, site);
                                    //Faking update                                    
                                    pForm.activateProgressBar(task);

                                    task.updatePercent(30);

                                    task.setOnSucceeded(myevent -> {
                                        pForm.getDialogStage().close();
                                    });

                                    task.setOnCancelled(myevent -> {
                                        pForm.getDialogStage().close();

                                        alertErrorPopup(
                                                language.getString("TEAM-title_error"),
                                                language.getString("TEAM-sync-up-error-header"),
                                                language.getString("TEAM-sync-up-error-content") + ": \n" + task.getErrorMessage(),
                                                language.getString("TEAM-alert_ok"));
                                    });

                                    Thread thread = new Thread(task);
                                    thread.setDaemon(true);
                                    thread.start();
                                }
                            } else {
                                validHttpclient = null;
                                alertErrorPopup(
                                        language.getString("TEAM-title_error"),
                                        language.getString("TEAM-insufficient-error-header"),
                                        language.getString("TEAM-insufficient-error-content"),
                                        language.getString("TEAM-alert_ok")
                                );
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            alertErrorPopup(
                                    language.getString("TEAM-title_error"),
                                    language.getString("TEAM-sync-up-error-header"),
                                    language.getString("TEAM-sync-up-error-content") + "\n" + ex.getMessage(),
                                    language.getString("TEAM-alert_ok")
                            );
                        }
                    }
                }
            }
        }
    }

    public void syncUp(String site) {
        try {
            Project project = getProject(validHttpclient, sites.get(site));
            project.setLastUpdateTime(new Date());

            boolean foundProject = false;
            ProjectService projectService = new ProjectServiceImpl();
            Project existingProject = null;
            for (Project proj : projectService.listProject()) {
                if (proj.getProjectId().intValue() == project.getProjectId().intValue()) {
                    existingProject = proj;
                    foundProject = true;
                    break;
                }
            }

            // setup country for the project
            CountryService countryService = new CountryServiceImpl();
            List<Country> allCountries = countryService.listCountry();
            if (project.getCountry() != null) {
                for (Country country : allCountries) {
                    if (country.getName().equals(project.getCountry().getName())) {
                        project.setCountry(country);
                        break;
                    }
                }
            }

            OrganizationService orgService = new OrganizationServiceImpl();
            List<Organization> allOrgs = orgService.listOrganization();

            EventService eventService = new EventServiceImpl();
            CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();

            PersonService personService = new PersonServiceImpl();
            List<Person> allPersons = personService.listPerson();

            CameraModelService modelService = new CameraModelServiceImpl();
            List<CameraModel> allModels = modelService.listCameraModel();

            CameraService cameraService = new CameraServiceImpl();
            CameraTrapService trapService = new CameraTrapServiceImpl();

            RoleService roleService = new RoleServiceImpl();
            List<Role> roles = roleService.listRole();

            ImageService imageService = new ImageServiceImpl();

            if (!foundProject) {
                projectService.addProject(project);
                plugin.getWildIDPane().addNewProject(project);

                // save or update organizations
                SortedSet<ProjectOrganization> pos = project.getProjectOrganizations();

                for (ProjectOrganization po : pos) {
                    Organization org = po.getOrganization();
                    Organization orgAlt = findOrganization(allOrgs, org);
                    if (orgAlt == null) {

                        // although no organization with the same id exists
                        // but if there might be an organization with the same name
                        orgAlt = findOrganizationWithName(allOrgs, org);

                        if (org.getCountry() != null) {
                            for (Country country : allCountries) {
                                if (country.getName().equals(org.getCountry().getName())) {
                                    org.setCountry(country);
                                    break;
                                }
                            }
                        }
                        orgService.addOrganization(org);
                        allOrgs.add(org);
                        plugin.getWildIDPane().addNewOrganization(org);

                        if (orgAlt != null) {
                            replaceOrganization(orgAlt, org, projectService, orgService, personService, allPersons, plugin);
                        }

                    } else {
                        org = orgAlt;
                        orgService.updateOrganization(org);  // save the possible update 
                    }
                    po.setOrganization(org);
                    po.setProject(project);
                    po.setId(new ProjectOrganizationId(project.getProjectId(), org.getOrganizationId()));
                    projectService.addProjectOrganization(po);
                    plugin.getWildIDPane().addNewOrganization(project, org);
                }

                // save persons associated with project
                // and setup project person role relations
                Map<Integer, Set<ProjectPersonRole>> id2roles = new HashMap<>();
                List<Person> persons = new ArrayList<>();
                for (ProjectPersonRole ppr : project.getProjectPersonRoles()) {

                    // find role
                    for (Role role : roles) {
                        if (role.getName().equals(ppr.getRole().getName())) {
                            ppr.setRole(role);
                            break;
                        }
                    }
                    ppr.setProject(project);

                    Set<ProjectPersonRole> roleSet = id2roles.get(ppr.getPerson().getPersonId());
                    if (roleSet == null) {
                        roleSet = new HashSet<>();
                    }
                    roleSet.add(ppr);
                    id2roles.put(ppr.getPerson().getPersonId(), roleSet);

                    boolean found = false;
                    for (Person person : persons) {
                        if (ppr.getPerson().getPersonId().intValue() == person.getPersonId().intValue()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        persons.add(ppr.getPerson());
                    }
                }

                //List<Person> persons = projectService.getPersons(project);
                for (Person person : persons) {
                    person.setProjectPersonRoles(id2roles.get(person.getPersonId()));

                    if (contains(allPersons, person)) {
                        // this person exists (maybe in other TEAM projects)
                        personService.updatePerson(person);
                    } else {

                        // it might be a person with the same email
                        Person personAlt = findPersonWithEmail(allPersons, person);

                        // this person is a new person 
                        personService.addPerson(person);
                        plugin.getWildIDPane().addNewPerson(person);

                        if (personAlt != null) {
                            replacePerson(personAlt, person, projectService, imageService, personService, allPersons, plugin);
                        }

                    }
                    plugin.getWildIDPane().addSelectedPerson(project, person);
                }

                List<ProjectPersonRole> pprs = new ArrayList<>();
                pprs.addAll(project.getProjectPersonRoles());
                personService.addProjectPersonRoles(pprs);

                // save events 
                Set<Event> events = project.getEvents();
                for (Event evt : events) {
                    evt.setProject(project);
                    eventService.addEvent(evt);
                    project.getEvents().add(evt);
                    plugin.getWildIDPane().createEvent(evt);
                }

                // save camera trap arrays
                Set<CameraTrapArray> arrays = project.getCameraTrapArrays();
                for (CameraTrapArray array : arrays) {
                    array.setProject(project);
                    array.setCameraTraps(new HashSet<>());
                    arrayService.addCameraTrapArray(array);
                    project.getCameraTrapArrays().add(array);
                    plugin.getWildIDPane().createNewCameraTrapArray(array);
                }

                // save cameras
                Set<Camera> cameras = project.getCameras();
                for (Camera camera : cameras) {
                    CameraModel model = camera.getCameraModel();
                    CameraModel modelAlt = findModel(allModels, model);
                    if (modelAlt == null) {
                        modelService.addCameraModel(model);
                        allModels.add(model);
                        plugin.getWildIDPane().createCameraModel(model);
                    } else {
                        model = modelAlt;
                    }
                    camera.setCameraModel(model);
                    camera.setProject(project);
                    cameraService.addCamera(camera);
                    project.getCameras().add(camera);
                }

                // save camera traps
                List<CameraTrap> traps = getCameraTraps(validHttpclient, sites.get(site));
                for (CameraTrap trap : traps) {
                    CameraTrapArray array = findCameraTrapArray(arrays, trap.getCameraTrapArray());
                    trap.setCameraTrapArray(array);
                    array.getCameraTraps().add(trap);
                    trapService.addCameraTrap(trap);
                    plugin.getWildIDPane().createNewCameraTrap(trap);
                }

            } else {
                projectService.updateProject(project);
                //plugin.getWildIDPane().updateProject(project);

                // process organizations
                // remove all organizations from the project
                Set<ProjectOrganization> epos = existingProject.getProjectOrganizations();
                for (ProjectOrganization po : epos) {

                    // remove po from organization association
                    for (ProjectOrganization apo : po.getOrganization().getProjectOrganizations()) {
                        if (apo.getProject().getProjectId().intValue() == existingProject.getProjectId().intValue()
                                && apo.getOrganization().getOrganizationId().intValue() == po.getOrganization().getOrganizationId().intValue()) {
                            po.getOrganization().getProjectOrganizations().remove(apo);
                            break;
                        }
                    }
                    orgService.removeProjectOrganization(po);
                    plugin.getWildIDPane().removeOrganization(po.getProject(), po.getOrganization());
                }
                existingProject.getProjectOrganizations().clear();

                // setup the relation of project and organizations
                SortedSet<ProjectOrganization> pos = project.getProjectOrganizations();
                for (ProjectOrganization po : pos) {
                    Organization org = po.getOrganization();
                    if (org.getCountry() != null) {
                        for (Country country : allCountries) {
                            if (country.getName().equals(org.getCountry().getName())) {
                                org.setCountry(country);
                                break;
                            }
                        }
                    }

                    Organization orgAlt = findOrganization(allOrgs, org);
                    if (orgAlt == null) {

                        orgAlt = findOrganizationWithName(allOrgs, org);

                        orgService.addOrganization(org);
                        allOrgs.add(org);
                        plugin.getWildIDPane().addNewOrganization(org);

                        if (orgAlt != null) {
                            replaceOrganization(orgAlt, org, projectService, orgService, personService, allPersons, plugin);
                        }

                    } else {
                        orgService.updateOrganization(org);  // save the possible update 
                    }

                    po.setOrganization(org);
                    po.setProject(project);
                    po.setId(new ProjectOrganizationId(project.getProjectId(), org.getOrganizationId()));
                    projectService.addProjectOrganization(po);
                    plugin.getWildIDPane().addNewOrganization(project, org);
                }

                existingProject.setProjectOrganizations(pos);

                // process persons
                List<Person> existingPersons = projectService.getPersons(existingProject);
                for (Person person : existingPersons) {
                    List<ProjectPersonRole> pprs = personService.getProjectPersonRoles(person, existingProject);
                    personService.removeProjectPersonRoles(pprs);
                    existingProject.getProjectPersonRoles().removeAll(pprs);
                    person.getProjectPersonRoles().removeAll(pprs);
                    plugin.getWildIDPane().removePerson(existingProject, person);
                }

                Map<Integer, Set<ProjectPersonRole>> id2roles = new HashMap<>();
                for (ProjectPersonRole ppr : project.getProjectPersonRoles()) {
                    // find role
                    for (Role role : roles) {
                        if (role.getName().equals(ppr.getRole().getName())) {
                            ppr.setRole(role);
                            break;
                        }
                    }
                    ppr.setProject(project);

                    Set<ProjectPersonRole> roleSet = id2roles.get(ppr.getPerson().getPersonId());
                    if (roleSet == null) {
                        roleSet = new HashSet<>();
                    }
                    roleSet.add(ppr);
                    id2roles.put(ppr.getPerson().getPersonId(), roleSet);
                }

                List<Person> persons = projectService.getPersons(project);
                for (Person person : persons) {
                    person.setProjectPersonRoles(id2roles.get(person.getPersonId()));

                    if (contains(allPersons, person)) {
                        // this person exists (maybe in other TEAM projects)
                        personService.updatePerson(person);
                    } else {

                        Person personAlt = findPersonWithEmail(allPersons, person);

                        // this person is a new person 
                        personService.addPerson(person);
                        plugin.getWildIDPane().addNewPerson(person);

                        if (personAlt != null) {
                            replacePerson(personAlt, person, projectService, imageService, personService, allPersons, plugin);
                        }
                    }
                    plugin.getWildIDPane().addSelectedPerson(project, person);
                }

                List<ProjectPersonRole> pprs = new ArrayList<>();
                pprs.addAll(project.getProjectPersonRoles());
                personService.addProjectPersonRoles(pprs);
                existingProject.getProjectPersonRoles().addAll(pprs);

                // process events
                Set<Event> existingEvents = existingProject.getEvents();
                Set<Event> events = project.getEvents();

                for (Event evt : events) {
                    if (!contains(existingEvents, evt)) {
                        evt.setProject(project);
                        eventService.addEvent(evt);
                        existingProject.getEvents().add(evt);
                        plugin.getWildIDPane().createEvent(evt);
                    }
                }

                List<Event> deletedEvents = new ArrayList<>();
                for (Event evt : existingEvents) {
                    if (!contains(events, evt)) {
                        if (projectService.getDeployments(existingProject).isEmpty()) {
                            // remove this evt
                            eventService.removeEvent(evt.getEventId());
                            deletedEvents.add(evt);
                            plugin.getWildIDPane().removeEvent(evt);
                        } else {
                            // show error message. evt is in used but not in TEAM now
                        }
                    }
                }
                existingProject.getCameraTrapArrays().removeAll(deletedEvents);

                // process camera trap arrays
                Set<CameraTrapArray> existingArrays = existingProject.getCameraTrapArrays();
                Set<CameraTrapArray> arrays = project.getCameraTrapArrays();

                for (CameraTrapArray array : arrays) {
                    if (!contains(existingArrays, array)) {
                        array.setProject(project);
                        array.setCameraTraps(new HashSet<>());
                        arrayService.addCameraTrapArray(array);
                        existingProject.getCameraTrapArrays().add(array);
                        plugin.getWildIDPane().createNewCameraTrapArray(array);
                    } else {
                        array.setProject(project);
                        arrayService.updateCameraTrapArray(array);
                        plugin.getWildIDPane().updateCameraTrapArray(array);
                    }
                }

                List<Deployment> deployments = projectService.getDeployments(existingProject);
                List<CameraTrapArray> deletedArrays = new ArrayList<>();
                for (CameraTrapArray array : existingArrays) {
                    if (!contains(arrays, array)) {
                        boolean arrayInUse = false;
                        for (CameraTrap trap : array.getCameraTraps()) {
                            boolean trapInUse = false;
                            for (Deployment deployment : deployments) {
                                if (deployment.getCameraTrap().getCameraTrapId().intValue() == trap.getCameraTrapId()) {
                                    trapInUse = true;
                                    arrayInUse = true;
                                    break;
                                }
                            }
                            if (!trapInUse) {
                                trapService.removeCameraTrap(trap.getCameraTrapId());
                                array.getCameraTraps().remove(trap);
                                plugin.getWildIDPane().removeCameraTrap(trap);
                            }
                        }

                        if (!arrayInUse) {
                            arrayService.removeCameraTrapArray(array.getCameraTrapArrayId());
                            deletedArrays.add(array);
                            plugin.getWildIDPane().removeCameraTrapArray(array);
                        } else {
                            // show error message                                                      
                        }
                    }
                }
                existingProject.getCameraTrapArrays().removeAll(deletedArrays);

                // process cameras
                Set<Camera> existingCameras = existingProject.getCameras();
                Set<Camera> cameras = project.getCameras();

                for (Camera camera : cameras) {
                    CameraModel model = camera.getCameraModel();
                    CameraModel modelAlt = findModel(allModels, model);
                    if (modelAlt == null) {
                        modelService.addCameraModel(model);
                        allModels.add(model);
                        plugin.getWildIDPane().createCameraModel(model);
                    } else {
                        model = modelAlt;
                    }
                    camera.setCameraModel(model);
                    camera.setProject(project);
                    if (!contains(existingCameras, camera)) {
                        cameraService.addCamera(camera);
                        existingProject.getCameras().add(camera);
                    }
                }

                List<Camera> deletedCameras = new ArrayList<>();
                for (Camera camera : existingCameras) {
                    if (!contains(cameras, camera)) {
                        boolean cameraInUse = false;
                        for (Deployment deployment : deployments) {
                            Camera cam = deployment.getCamera();
                            if (cam.getCameraModel().getName().equals(camera.getCameraModel().getName())
                                    && cam.getCameraModel().getMaker().equals(camera.getCameraModel().getMaker())
                                    && cam.getSerialNumber().equals(camera.getSerialNumber())) {
                                cameraInUse = true;
                                break;
                            }
                        }

                        if (cameraInUse) {
                            // show error message
                        } else {
                            cameraService.removeCamera(camera.getCameraId());
                            deletedCameras.add(camera);
                        }
                    }
                }
                existingProject.getCameras().removeAll(deletedCameras);

                // process camera traps
                List<CameraTrap> existingTraps = new ArrayList<>();
                for (CameraTrapArray array : existingProject.getCameraTrapArrays()) {
                    existingTraps.addAll(array.getCameraTraps());
                }

                List<CameraTrap> traps = getCameraTraps(validHttpclient, sites.get(site));
                for (CameraTrap trap : traps) {
                    CameraTrapArray array = findCameraTrapArray(arrays, trap.getCameraTrapArray());
                    trap.setCameraTrapArray(array);

                    if (!contains(existingTraps, trap)) {
                        array.getCameraTraps().add(trap);
                        trapService.addCameraTrap(trap);
                        plugin.getWildIDPane().createNewCameraTrap(trap);
                    } else {
                        CameraTrap eTrap = find(existingTraps, trap);
                        trapService.updateCameraTrap(trap);
                        plugin.getWildIDPane().removeCameraTrap(eTrap);
                        plugin.getWildIDPane().createNewCameraTrap(trap);
                    }
                }

                for (CameraTrap trap : existingTraps) {
                    if (!contains(traps, trap)) {

                        boolean trapInUse = false;
                        for (Deployment deployment : deployments) {
                            if (deployment.getCameraTrap().getCameraTrapId().intValue() == trap.getCameraTrapId()) {
                                trapInUse = true;
                                break;
                            }
                        }

                        if (!trapInUse) {
                            trap.getCameraTrapArray().getCameraTraps().remove(trap);
                            trapService.removeCameraTrap(trap.getCameraTrapId());
                            plugin.getWildIDPane().removeCameraTrap(trap);
                        }
                    }
                }

                plugin.getWildIDPane().updateProject(project);

            }
            plugin.getWildIDPane().showStandardView(project);

        } catch (java.lang.IllegalStateException ex) {
            //ex.printStackTrace();
            System.out.println("Ignore this IllegalStateException");
        } catch (Exception ex) {
            ex.printStackTrace();

            alertErrorPopup(
                    language.getString("TEAM-title_error"),
                    language.getString("TEAM-sync-up-error-header"),
                    language.getString("TEAM-sync-up-error-content") + "\n" + ex.getMessage(),
                    language.getString("TEAM-alert_ok")
            );
        }
    }

    static void alertErrorPopup(String title, String header, String content, String ok_btn) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image("resources/icons/teamnetwork.png"));
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
        buttonTypes.clear();
        buttonTypes.add(new ButtonType(ok_btn, ButtonBar.ButtonData.OK_DONE));
        alert.showAndWait();
    }

    public static void alertInformationPopup(Pane pane, String title, String header, String content, String ok_btn) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
        buttonTypes.clear();
        buttonTypes.add(new ButtonType(ok_btn, ButtonBar.ButtonData.OK_DONE));
        if (pane != null) {
            alert.initOwner(pane.getScene().getWindow());
            alert.initModality(Modality.WINDOW_MODAL);
        }
        alert.showAndWait();
    }

    static private boolean loginToTEAMNetwork(DefaultHttpClient httpclient, String username, String password) throws Exception {

        boolean success = false;

        HttpPost httpost = new HttpPost("http://www.teamnetwork.org/user/login");
        HttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
        String line = reader.readLine();
        String formId = null;
        while (line != null) {
            int pos = line.indexOf("name=\"form_build_id\" value=\"");
            if (pos != -1) {
                line = line.substring(pos + 28);
                pos = line.indexOf("\" />");
                formId = line.substring(0, pos);
            }
            line = reader.readLine();
        }
        entity.consumeContent();

        // submit username and password
        httpost = new HttpPost("http://www.teamnetwork.org/user/login");
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("name", username));
        nvps.add(new BasicNameValuePair("pass", password));
        nvps.add(new BasicNameValuePair("form_id", "user_login"));
        nvps.add(new BasicNameValuePair("op", "Log in"));
        nvps.add(new BasicNameValuePair("form_build_id", formId));
        httpost.setEntity(new UrlEncodedFormEntity(nvps));

        response = httpclient.execute(httpost);
        entity = response.getEntity();
        reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
        line = reader.readLine();
        if (line == null) {
            success = true;
        }
        entity.consumeContent();

        return success;
    }

    static private TreeMap<String, Integer> getAssociatedTEAMSite(DefaultHttpClient httpclient) throws Exception {

        TreeMap<String, Integer> sites = new TreeMap<>();

        HttpPost httpost = new HttpPost("http://www.teamnetwork.org:8080/CorePortlet/jsp/management/deskTEAM/get-sites.jsp");
        HttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
        String line = reader.readLine();
        while (line != null) {
            if (line.trim().length() > 0) {
                String[] strings = line.split(",");
                sites.put(strings[1], new Integer(strings[0]));
            }
            line = reader.readLine();
        }
        entity.consumeContent();

        return sites;
    }

    private Project getProject(DefaultHttpClient httpclient, int siteId) throws Exception {

        HttpPost httpost = new HttpPost("http://www.teamnetwork.org:8080/CorePortlet/jsp/management/deskTEAM/get-site.jsp?siteId=" + siteId);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("siteId", siteId + ""));
        nvps.add(new BasicNameValuePair("version", WildID.VERSION));
        httpost.setEntity(new UrlEncodedFormEntity(nvps));

        HttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));

        // unmarshal xml file
        JAXBElement<Project> df;
        JAXBContext context = JAXBContext.newInstance(Project.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        df = unmarshaller.unmarshal(new StreamSource(reader), Project.class);
        Project project = df.getValue();
        entity.consumeContent();
        reader.close();

        return project;
    }

    private List<CameraTrap> getCameraTraps(DefaultHttpClient httpclient, int siteId) throws Exception {

        HttpPost httpost = new HttpPost("http://www.teamnetwork.org:8080/CorePortlet/jsp/management/deskTEAM/get-traps.jsp?siteId=" + siteId);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("siteId", siteId + ""));
        nvps.add(new BasicNameValuePair("version", WildID.VERSION));
        httpost.setEntity(new UrlEncodedFormEntity(nvps));

        HttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));

        // unmarshal xml file
        JAXBElement<CameraTrapList> df;
        JAXBContext context = JAXBContext.newInstance(CameraTrapList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        df = unmarshaller.unmarshal(new StreamSource(reader), CameraTrapList.class);
        CameraTrapList trapList = df.getValue();
        entity.consumeContent();
        reader.close();

        return trapList.getCameraTraps();
    }

    private static boolean contains(List<Person> persons, Person person) {
        for (Person p : persons) {
            if (p.getPersonId().intValue() == person.getPersonId().intValue()) {
                return true;
            }
        }
        return false;
    }

    private static CameraModel findModel(List<CameraModel> models, CameraModel model) {
        for (CameraModel cm : models) {
            if (cm.getName().equals(model.getName()) && cm.getMaker().equals(model.getMaker())) {
                return cm;
            }
        }
        return null;
    }

    private static Organization findOrganization(List<Organization> orgs, Organization org) {
        for (Organization organ : orgs) {
            if (organ.getOrganizationId().intValue() == org.getOrganizationId().intValue()) {
                return organ;
            }
        }
        return null;
    }

    private static Organization findOrganizationWithName(List<Organization> orgs, Organization org) {
        for (Organization organ : orgs) {
            if (organ.getName().trim().toLowerCase().equals(org.getName().trim().toLowerCase())) {
                return organ;
            }
        }
        return null;
    }

    private static Person findPersonWithEmail(List<Person> allPersons, Person person) {
        for (Person p : allPersons) {
            if (p.getEmail().equals(person.getEmail())) {
                return p;
            }
        }
        return null;
    }

    private static CameraTrapArray findCameraTrapArray(Set<CameraTrapArray> arrays, CameraTrapArray array) {
        for (CameraTrapArray ary : arrays) {
            if (ary.getCameraTrapArrayId().intValue() == array.getCameraTrapArrayId().intValue()) {
                return ary;
            }
        }
        return null;
    }

    private static boolean contains(Set<Event> events, Event event) {
        for (Event evt : events) {
            if (evt.getName().equals(event.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(Set<CameraTrapArray> arrays, CameraTrapArray array) {
        for (CameraTrapArray arr : arrays) {
            if (arr.getCameraTrapArrayId().intValue() == array.getCameraTrapArrayId().intValue()) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(Set<Camera> cameras, Camera camera) {
        for (Camera cam : cameras) {
            if (cam.getCameraModel().getName().equals(camera.getCameraModel().getName())
                    && cam.getCameraModel().getMaker().equals(camera.getCameraModel().getMaker())
                    && cam.getSerialNumber().equals(camera.getSerialNumber())) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(List<CameraTrap> traps, CameraTrap trap) {
        for (CameraTrap ct : traps) {
            if (ct.getCameraTrapId().intValue() == trap.getCameraTrapId().intValue()) {
                return true;
            }
        }
        return false;
    }

    private static CameraTrap find(List<CameraTrap> traps, CameraTrap trap) {
        for (CameraTrap ct : traps) {
            if (ct.getCameraTrapId().intValue() == trap.getCameraTrapId().intValue()) {
                return ct;
            }
        }
        return null;
    }

    private static void replaceOrganization(Organization orgAlt, Organization org,
            ProjectService projectService, OrganizationService orgService,
            PersonService personService, List<Person> allPersons, TEAMPlugin plugin) {

        // replace orgAlt by org
        for (Project proj : projectService.listProject()) {
            for (ProjectOrganization po1 : proj.getProjectOrganizations()) {
                if (po1.getOrganization().getOrganizationId().intValue() == orgAlt.getOrganizationId().intValue()) {

                    ProjectOrganization po2 = new ProjectOrganization();
                    po2.setProject(proj);
                    po2.setOrganization(org);
                    po2.setId(new ProjectOrganizationId(proj.getProjectId(), org.getOrganizationId()));

                    orgService.removeProjectOrganization(po1);
                    projectService.addProjectOrganization(po2);

                    plugin.getWildIDPane().removeOrganization(proj, orgAlt);
                    plugin.getWildIDPane().addNewOrganization(proj, org);
                }
            }
        }

        for (Person person : allPersons) {
            Organization pOrg = person.getOrganization();
            if (pOrg != null && pOrg.getOrganizationId().intValue() == orgAlt.getOrganizationId().intValue()) {
                person.setOrganization(org);
                personService.updatePerson(person);
            }
        }

        orgService.removeOrganization(orgAlt.getOrganizationId());
        plugin.getWildIDPane().removeOrganization(orgAlt);

    }

    private static void replacePerson(Person personAlt, Person person,
            ProjectService projectService, ImageService imageService,
            PersonService personService, List<Person> allPersons, TEAMPlugin plugin) {

        for (Project proj : projectService.listProject()) {

            List<ProjectPersonRole> npprs = new ArrayList<>();
            List<ProjectPersonRole> pprs = personService.getProjectPersonRoles(personAlt, proj);

            if (pprs != null && !pprs.isEmpty()) {
                for (ProjectPersonRole ppr : pprs) {
                    if (ppr.getPerson().getPersonId().intValue() == personAlt.getPersonId().intValue()) {
                        ppr.setPerson(person);
                        npprs.add(ppr);
                    }
                }

                personService.removeProjectPersonRoles(pprs);
                proj.getProjectPersonRoles().removeAll(pprs);
                person.getProjectPersonRoles().removeAll(pprs);
                plugin.getWildIDPane().removePerson(proj, personAlt);

                personService.addProjectPersonRoles(npprs);
                proj.getProjectPersonRoles().addAll(npprs);
                plugin.getWildIDPane().addSelectedPerson(proj, person);

            }

            for (Deployment deployment : projectService.getDeployments(proj)) {
                Person setup = deployment.getSetupPerson();
                if (setup != null && setup.getPersonId().intValue() == personAlt.getPersonId().intValue()) {
                    deployment.setSetupPerson(person);
                }

                Person pickup = deployment.getPickupPerson();
                if (pickup != null && pickup.getPersonId().intValue() == personAlt.getPersonId().intValue()) {
                    deployment.setPickupPerson(person);
                }

                File oldFolder = ImageRepository.getFolder(deployment);
                DeploymentService deployService = new DeploymentServiceImpl();
                try {
                    deployService.updateDeployment(deployment, oldFolder);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //plugin.getWildIDPane().updateDeployment(deployment);
            }

        }

        // get all images using personAlt
        List<Image> images = imageService.getImagesIdentifiedBy(personAlt);

        for (Image image : images) {
            if (image.getPerson().getPersonId().intValue() == personAlt.getPersonId().intValue()) {
                image.setPerson(person);
            }

            Set<ImageSpecies> nis = new HashSet<>();
            for (Object object : image.getImageSpecieses()) {
                ImageSpecies is = (ImageSpecies) object;
                if (is.getPerson().getPersonId().intValue() == personAlt.getPersonId().intValue()) {
                    is.setPerson(person);
                }
                nis.add(is);
            }
            imageService.removeImageSpecies(image);
            image.setImageSpecieses(nis);
            imageService.saveAnnotation(image);
        }

        personService.removePerson(personAlt.getPersonId());
        plugin.getWildIDPane().removePerson(personAlt);

    }

    private Dialog<Pair<String, String>> getLoginDialog(LanguageModel language) {

        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image("resources/icons/teamnetwork.png"));

        dialog.setTitle("TEAM Network");
        dialog.setHeaderText(language.getString("TEAM-login-header"));

        // Set the icon (must be included in the project).
        // dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));
        // Set the button types.
        ButtonType loginButtonType = new ButtonType(language.getString("TEAM-login-button"), ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(language.getString("TEAM-login-cancel"), ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, cancelButtonType);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(30, 10, 10, 10));

        TextField username = new TextField();
        username.setPrefWidth(250);
        username.setPromptText(language.getString("TEAM-username-label"));
        PasswordField password = new PasswordField();
        password.setPromptText(language.getString("TEAM-password-label"));

        grid.add(new Label(language.getString("TEAM-username-label")), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label(language.getString("TEAM-password-label")), 0, 1);
        grid.add(password, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        return dialog;
    }

    private ChoiceDialog<String> getSiteDialog(LanguageModel language, List<String> choices) {

        ChoiceDialog<String> siteDialog = new ChoiceDialog<>(null, choices);
        Stage stage2 = (Stage) siteDialog.getDialogPane().getScene().getWindow();
        stage2.getIcons().add(new javafx.scene.image.Image("resources/icons/teamnetwork.png"));

        siteDialog.setTitle(language.getString("TEAM-choose-site-title"));
        siteDialog.setHeaderText(language.getString("TEAM-choose-site-header"));
        siteDialog.setContentText(language.getString("TEAM-choose-site-field-label"));
        siteDialog.getDialogPane().getButtonTypes().clear();
        siteDialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType(language.getString("TEAM-site-button"), ButtonData.OK_DONE),
                new ButtonType(language.getString("TEAM-login-cancel"), ButtonData.CANCEL_CLOSE));
        return siteDialog;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    static class CameraTrapList {

        @XmlElement(name = "trap")
        private final List<CameraTrap> cameraTraps = new ArrayList<>();

        public void add(CameraTrap trap) {
            this.cameraTraps.add(trap);
        }

        public List<CameraTrap> getCameraTraps() {
            return cameraTraps;
        }

    }

}
