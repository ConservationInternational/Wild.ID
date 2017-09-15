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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import org.wildid.entity.BaitType;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Country;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.FailureType;
import org.wildid.entity.FeatureType;
import org.wildid.entity.Image;
import org.wildid.entity.ImageExif;
import org.wildid.entity.ImageIndividual;
import org.wildid.entity.ImageRepository;
import org.wildid.entity.ImageSequence;
import org.wildid.entity.ImageSpecies;
import org.wildid.entity.Organization;
import org.wildid.entity.Person;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectOrganization;
import org.wildid.entity.ProjectPersonRole;
import org.wildid.entity.Role;
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
import org.wildid.service.CountryService;
import org.wildid.service.CountryServiceImpl;
import org.wildid.service.DeploymentService;
import org.wildid.service.DeploymentServiceImpl;
import org.wildid.service.EventService;
import org.wildid.service.EventServiceImpl;
import org.wildid.service.FailureTypeService;
import org.wildid.service.FailureTypeServiceImpl;
import org.wildid.service.FeatureTypeService;
import org.wildid.service.FeatureTypeServiceImpl;
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
public class LoadZipInNewProjectTask extends Task<Void> {

    static Logger log = Logger.getLogger(LoadZipInNewProjectTask.class.getName());
    private int val;
    private int max;
    private final LanguageModel language;
    private final File zipFile;
    private final File tmpDir;
    private final ObservableList<Person> persons;
    private Project newProject;
    private final List<Organization> newOrganizations = new ArrayList<>();
    private final List<Person> newPersons = new ArrayList<>();
    private final List<Event> newEvents = new ArrayList<>();
    private final List<CameraTrapArray> newArrays = new ArrayList<>();
    private final List<CameraTrap> newTraps = new ArrayList<>();
    private final List<Country> countries;
    private final List<BaitType> baitTypes;
    private final List<FeatureType> featureTypes;
    private final List<FailureType> failureTypes;
    private final List<Role> roles;
    private final WildIDPane pane;
    private String errorMessage;

    public LoadZipInNewProjectTask(LanguageModel language, File zipFile, File tmpDir, WildIDPane pane, ObservableList<Person> persons) {
        this.language = language;
        this.zipFile = zipFile;
        this.tmpDir = tmpDir;
        this.pane = pane;
        this.persons = persons;
        this.max = 100;
        this.val = 0;

        CountryService countryService = new CountryServiceImpl();
        this.countries = countryService.listCountry();

        BaitTypeService baitTypeService = new BaitTypeServiceImpl();
        this.baitTypes = baitTypeService.listBaitType();

        FeatureTypeService featureTypeService = new FeatureTypeServiceImpl();
        this.featureTypes = featureTypeService.listFeatureType();

        FailureTypeService failureTypeService = new FailureTypeServiceImpl();
        this.failureTypes = failureTypeService.listFailureType();

        PersonService personService = new PersonServiceImpl();
        persons = FXCollections.observableList(personService.listPerson());

        RoleService roleService = new RoleServiceImpl();
        this.roles = roleService.listRole();
    }

    @Override
    protected Void call() throws Exception {

        try {
            updateProgress(0, max);

            log.info("zip file: " + zipFile.getAbsolutePath());
            log.info("uncompressing to: " + tmpDir.getAbsolutePath());

            finished(language.getString("open_transfer_file_progress_uncompress_info"));
            Util.unzip(zipFile, tmpDir);

            max = Util.countJpgFiles(tmpDir);

            if (tmpDir.list().length == 1) {
                File zipRootDir = new File(tmpDir, tmpDir.list()[0]);
                File dataDir = new File(zipRootDir, "data");
                File imageDir = new File(zipRootDir, "images");

                if (!dataDir.exists()) {
                    log.error("the data folder not found.");
                }

                if (!imageDir.exists()) {
                    log.error("the image folder not found.");
                }

                //Project newProject = null;
                HashMap<Integer, Event> eventMap = new HashMap<>();
                HashMap<Integer, CameraTrapArray> arrayMap = new HashMap<>();
                HashMap<Integer, Camera> cameraMap = new HashMap<>();
                HashMap<Integer, CameraTrap> trapMap = new HashMap<>();
                HashMap<Integer, Organization> orgMap = new HashMap<>();

                String[] xmlFileNames = dataDir.list();
                max += xmlFileNames.length + 6;
                String projectAbbrev;
                for (String xmlFileName : xmlFileNames) {
                    File xmlFile = new File(dataDir, xmlFileName);

                    // unmarshal xml file
                    JAXBElement<Deployment> df;
                    JAXBContext context = JAXBContext.newInstance(Deployment.class);
                    Unmarshaller unmarshaller = context.createUnmarshaller();
                    df = unmarshaller.unmarshal(new StreamSource(new InputStreamReader(new FileInputStream(xmlFile), "UTF8")),
                            Deployment.class);
                    Deployment deployment = df.getValue();

                    log.info("Saving the data for the camera trap: " + deployment.getCameraTrap().getName());

                    if (newProject == null) {
                        // images dir
                        File oldFolder = new File(
                                imageDir.getAbsolutePath() + File.separatorChar
                                + deployment.getEvent().getProject().getProjectId() + File.separatorChar
                                + deployment.getEvent().getEventId() + File.separatorChar
                                + deployment.getCameraTrap().getCameraTrapArray().getCameraTrapArrayId() + File.separatorChar
                                + deployment.getCameraTrap().getCameraTrapId());

                        log.info("image folder: " + oldFolder.getAbsolutePath());

                        // save newProject
                        newProject = deployment.getEvent().getProject();
                        log.info("original project id: " + newProject.getProjectId());
                        newProject.setProjectId(null);

                        // check the newProject name
                        ProjectService projectService = new ProjectServiceImpl();
                        List<Project> projects = projectService.listProject();
                        String projectName = newProject.getName();
                        projectAbbrev = newProject.getAbbrevName();
                        log.info("original project Name: " + projectName);

                        int index = 0;
                        boolean foundProjectName = false;
                        while (!foundProjectName) {
                            for (Project tmpProject : projects) {
                                if (projectName.equals(tmpProject.getName())) {
                                    foundProjectName = true;
                                    break;
                                }
                            }

                            if (foundProjectName) {
                                index++;
                                projectName = newProject.getName() + " " + index;
                                //projectAbbrev = newProject.getAbbrevName() + index;
                                foundProjectName = false;
                            } else {
                                break;
                            }
                        }

                        log.info("final project Name = " + projectName);
                        newProject.setName(projectName);

                        // check the abbrevation
                        index = 0;
                        boolean foundProjectAbbrev = false;
                        while (!foundProjectAbbrev) {
                            for (Project tmpProject : projects) {
                                if (projectAbbrev.equals(tmpProject.getAbbrevName())) {
                                    foundProjectAbbrev = true;
                                    break;
                                }
                            }

                            if (foundProjectAbbrev) {
                                index++;
                                if (index < 10) {
                                    if (newProject.getAbbrevName().length() < 8) {
                                        projectAbbrev = newProject.getAbbrevName() + " " + index;
                                    } else {
                                        projectAbbrev = newProject.getAbbrevName().substring(0, 7) + " " + index;
                                    }
                                } else if (newProject.getAbbrevName().length() < 7) {
                                    projectAbbrev = newProject.getAbbrevName() + " " + index;
                                } else {
                                    projectAbbrev = newProject.getAbbrevName().substring(0, 6) + " " + index;
                                }
                                foundProjectAbbrev = false;
                            } else {
                                break;
                            }
                        }

                        log.info("final project abbrev = " + projectAbbrev);
                        newProject.setAbbrevName(projectAbbrev);

                        newProject.setCountry(processCountry(newProject.getCountry()));

                        increaseDone();
                        finished(language.getString("open_transfer_file_progress_save_project_info") + projectName);

                        projectService.addProject(newProject);
                        log.info("project id = " + newProject.getProjectId());

                        // save cameras
                        Set<Camera> cameras = newProject.getCameras();
                        for (Camera camera : cameras) {

                            Integer oldId = camera.getCameraId();

                            if (camera.getCameraId().intValue() == deployment.getCamera().getCameraId().intValue()) {
                                deployment.setCamera(camera);
                            }
                            camera.setCameraId(null);
                            camera.setProject(newProject);

                            increaseDone();
                            finished(language.getString("open_transfer_file_progress_save_camera_info") + camera.getSerialNumber());

                            // check if the camera model is in the db 
                            CameraModel cameraModel = camera.getCameraModel();
                            CameraModel usedCameraModel = null;
                            CameraModelService cameraModelService = new CameraModelServiceImpl();
                            for (CameraModel cm : cameraModelService.listCameraModel()) {
                                if (cm.getName().equals(cameraModel.getName())
                                        && cm.getMaker().equals(cameraModel.getMaker())) {
                                    usedCameraModel = cm;
                                    break;
                                }
                            }

                            if (usedCameraModel == null) {
                                usedCameraModel = new CameraModel();
                                usedCameraModel.setName(cameraModel.getName());
                                usedCameraModel.setMaker(cameraModel.getMaker());
                                cameraModelService.addCameraModel(usedCameraModel);
                            }
                            camera.setCameraModel(usedCameraModel);
                            // end of processing the camera model

                            CameraService cameraService = new CameraServiceImpl();
                            cameraService.addCamera(camera);
                            cameraMap.put(oldId, camera);
                        }

                        // save event
                        Event event = deployment.getEvent();
                        Integer oldEventId = event.getEventId();
                        event.setEventId(null);
                        event.setProject(newProject);
                        newProject.getEvents().add(event);

                        increaseDone();
                        finished(language.getString("open_transfer_file_progress_save_event_info") + event.getName());

                        EventService eventService = new EventServiceImpl();
                        eventService.addEvent(event);
                        eventMap.put(oldEventId, event);
                        newEvents.add(event);

                        // save arrays and trap
                        CameraTrap trap = deployment.getCameraTrap();

                        /*
                         String[] trapNameElements = trap.getName().split("-");
                         if (trapNameElements.length == 4 && trapNameElements[0].equals(language.getString("project_array_default_prefix"))) {
                         trap.setName(trapNameElements[0] + "-" + projectAbbrev + "-" + trapNameElements[2] + "-" + trapNameElements[3]);
                         }
                         */
                        Integer oldTrapId = trap.getCameraTrapId();
                        trap.setCameraTrapId(null);

                        Set<CameraTrapArray> arrays = newProject.getCameraTrapArrays();
                        for (CameraTrapArray array : arrays) {
                            Integer oldArrayId = array.getCameraTrapArrayId();
                            array.setCameraTrapArrayId(null);
                            array.setProject(newProject);

                            increaseDone();
                            finished(language.getString("open_transfer_file_progress_save_array_info") + array.getName());

                            CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
                            arrayService.addCameraTrapArray(array);
                            arrayMap.put(oldArrayId, array);
                            newArrays.add(array);
                            newProject.getCameraTrapArrays().add(array);
                        }

                        boolean foundArray = false;
                        /*
                        for (CameraTrapArray array : arrays) {
                            System.out.println("    check: "+array.getName()+"  "+array.getCameraTrapArrayId());
                            if (array.getCameraTrapArrayId().intValue() == trap.getCameraTrapArray().getCameraTrapArrayId()) {
                                trap.setCameraTrapArray(array);
                                array.getCameraTraps().add(trap);
                                foundArray = true;
                                break;
                            }
                        }
                         */

                        CameraTrapArray usedArray = arrayMap.get(trap.getCameraTrapArray().getCameraTrapArrayId());
                        if (usedArray != null) {
                            foundArray = true;
                            trap.setCameraTrapArray(usedArray);
                            usedArray.getCameraTraps().add(trap);
                        }

                        /*
                             String[] arrayNameElements = array.getName().split("-");
                             if (arrayNameElements.length == 3 && arrayNameElements[0].equals(language.getString("project_array_default_prefix"))) {
                             array.setName(arrayNameElements[0] + "-" + projectAbbrev + "-" + arrayNameElements[2]);
                             }
                         */
                        if (!foundArray) {
                            CameraTrapArray array = trap.getCameraTrapArray();
                            Integer oldArrayId = array.getCameraTrapArrayId();
                            array.setCameraTrapArrayId(null);
                            array.setProject(newProject);

                            increaseDone();
                            finished(language.getString("open_transfer_file_progress_save_array_info") + array.getName());

                            CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
                            arrayService.addCameraTrapArray(array);
                            arrayMap.put(oldArrayId, array);
                            newArrays.add(array);
                            newProject.getCameraTrapArrays().add(array);
                        }

                        increaseDone();
                        finished(language.getString("open_transfer_file_progress_save_trap_info") + trap.getName());
                        CameraTrapService trapService = new CameraTrapServiceImpl();
                        trapService.addCameraTrap(trap);
                        trapMap.put(oldTrapId, trap);
                        newTraps.add(trap);

                        // save organization
                        SortedSet<ProjectOrganization> pos = newProject.getProjectOrganizations();

                        OrganizationService orgService = new OrganizationServiceImpl();
                        List<Organization> orgs = orgService.listOrganization();
                        for (ProjectOrganization po : pos) {
                            po.setProject(newProject);
                            Organization org = po.getOrganization();
                            Integer oldOrgId = org.getOrganizationId();
                            org.setOrganizationId(null);

                            boolean found = false;
                            for (Organization tmpOrg : orgs) {
                                if (org.getName().toLowerCase().replaceAll(" ", "").equals(tmpOrg.getName().toLowerCase().replaceAll(" ", ""))) {
                                    org = tmpOrg;
                                    po.setOrganization(org);
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                increaseDone();
                                finished(language.getString("open_transfer_file_progress_save_org_info") + org.getName());

                                orgService.addOrganization(org);
                            }

                            newProject.setProjectOrganizations(pos);
                            ProjectOrganization tmpPO = new ProjectOrganization(org, newProject);
                            projectService.addProjectOrganization(tmpPO);
                            newOrganizations.add(org);
                            orgMap.put(oldOrgId, org);
                        }

                        // save persons
                        Set<ProjectPersonRole> projectPersonRoles = new HashSet<>();
                        projectPersonRoles.addAll(newProject.getProjectPersonRoles());
                        PersonService personService = new PersonServiceImpl();
                        //List<Person> persons = personService.listPerson();
                        List<ProjectPersonRole> pprs = new ArrayList<>();
                        for (ProjectPersonRole ppr : projectPersonRoles) {
                            ppr.setProjectPersonRoleId(null);
                            ppr.setProject(newProject);
                            ppr.setRole(processRole(ppr.getRole()));

                            Person person = ppr.getPerson();
                            //System.out.println("--------------------");
                            //System.out.println("person = " + person.getEmail() + "   " + person.getPersonId());

                            boolean found = false;
                            for (Person tmpPerson : persons) {
                                //System.out.println("     check: " + tmpPerson.getEmail());
                                if (tmpPerson.getEmail().equals(person.getEmail())) {
                                    ppr.setPerson(tmpPerson);
                                    person = tmpPerson;
                                    found = true;
                                    break;
                                }
                            }
                            //System.out.println("     found: " + found);

                            person.getProjectPersonRoles().add(ppr);

                            if (!found) {
                                Organization pOrg = person.getOrganization();
                                if (pOrg != null) {

                                    Organization usedOrg = null;
                                    for (Organization tmpOrg : orgService.listOrganization()) {
                                        if (tmpOrg.getName().equals(pOrg.getName())) {
                                            usedOrg = tmpOrg;
                                            break;
                                        }
                                    }

                                    if (usedOrg == null) {
                                        Integer oldOrgId = pOrg.getOrganizationId();
                                        pOrg.setOrganizationId(null);

                                        increaseDone();
                                        finished(language.getString("open_transfer_file_progress_save_org_info") + pOrg.getName());

                                        orgService.addOrganization(pOrg);
                                        usedOrg = pOrg;
                                        newOrganizations.add(pOrg);
                                    }
                                    person.setOrganization(usedOrg);
                                }

                                // check the country
                                Country country = person.getCountry();
                                person.setCountry(processCountry(country));

                                increaseDone();
                                finished(language.getString("open_transfer_file_progress_save_person_info") + person.getEmail());

                                personService.addPerson(person);
                                persons.add(person);
                            }

                            // notify the main pane to change
                            boolean contained = false;
                            for (Person tmpPerson : newPersons) {
                                if (tmpPerson.getPersonId().intValue() == person.getPersonId()) {
                                    contained = true;
                                    break;
                                }
                            }
                            if (!contained) {
                                newPersons.add(person);
                            }

                            //this.pane.addNewPerson(newProject, person);
                            pprs.add(ppr);
                            newProject.getProjectPersonRoles().addAll(person.getProjectPersonRoles());

                        }
                        personService.addProjectPersonRoles(pprs);

                        // save deployment
                        deployment.setDeploymentId(null);

                        if (deployment.getSetupPerson() != null) {
                            log.info("setup person: " + deployment.getSetupPerson().getEmail());
                            for (Person tmpPerson : persons) {
                                if (tmpPerson.getEmail().equals(deployment.getSetupPerson().getEmail())) {
                                    deployment.setSetupPerson(tmpPerson);
                                    log.info("found: " + tmpPerson.getPersonId());
                                    break;
                                }
                            }
                        }

                        if (deployment.getPickupPerson() != null) {
                            log.info("pickup person: " + deployment.getPickupPerson().getEmail());
                            for (Person tmpPerson : persons) {
                                if (tmpPerson.getEmail().equals(deployment.getPickupPerson().getEmail())) {
                                    deployment.setPickupPerson(tmpPerson);
                                    log.info("found: " + tmpPerson.getPersonId());
                                    break;
                                }
                            }
                        }

                        DeploymentService deployService = new DeploymentServiceImpl();

                        // set bait/feature/failure type
                        deployment.setBaitType(processBaitType(deployment.getBaitType()));
                        deployment.setFeatureType(processFeatureType(deployment.getFeatureType()));
                        deployment.setFailureType(processFailureType(deployment.getFailureType()));

                        // set name
                        if (deployment.getName() == null) {
                            String defaultDeploymentName = newProject.getAbbrevName()
                                    + "_" + deployment.getEvent().getName().replaceAll(" ", "_")
                                    + "_" + trap.getName().replaceAll(" ", "_");
                            deployment.setName(defaultDeploymentName);
                        }

                        // save images
                        Set<ImageSequence> sequences = deployment.getImageSequences();
                        List<Image> images = new ArrayList<>();
                        for (ImageSequence seq : sequences) {
                            seq.setImageSequenceId(null);
                            seq.setDeployment(deployment);
                            for (Image img : seq.getImages()) {

                                if (img.getPerson() != null) {
                                    boolean found = false;
                                    for (Person tmpPerson : persons) {
                                        if (tmpPerson.getEmail().equals(img.getPerson().getEmail())) {
                                            img.setPerson(tmpPerson);
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        Person tmpPerson = img.getPerson();
                                        tmpPerson.setPersonId(null);
                                        personService.addPerson(tmpPerson);
                                        persons.add(tmpPerson);
                                        boolean contained = false;
                                        for (Person aPerson : newPersons) {
                                            if (aPerson.getPersonId().intValue() == tmpPerson.getPersonId()) {
                                                contained = true;
                                                break;
                                            }
                                        }
                                        if (!contained) {
                                            newPersons.add(tmpPerson);
                                        }
                                    }
                                }

                                increaseDone();
                                finished(language.getString("open_transfer_file_progress_save_image_info") + img.getRawName());

                                img.setImageId(null);
                                img.setImageSequence(seq);
                                for (ImageExif exif : img.getImageExifs()) {
                                    exif.setImageExifId(null);
                                    exif.setImage(img);
                                }
                                for (Object object : img.getImageSpecieses()) {
                                    ImageSpecies is = (ImageSpecies) object;

                                    if (is.getPerson() != null) {
                                        for (Person tmpPerson : persons) {
                                            if (tmpPerson.getEmail().equals(is.getPerson().getEmail())) {
                                                is.setPerson(tmpPerson);
                                                break;
                                            }
                                        }
                                    }

                                    is.setImageSpeciesId(null);
                                    is.setImage(img);

                                    for (ImageIndividual ind : is.getImageIndividuals()) {
                                        ind.setImageIndividualId(null);
                                        ind.setImageSpecies(is);
                                    }

                                }
                            }
                            images.addAll(seq.getImages());
                        }

                        deployService.addDeployment(deployment, images);
                        //this.pane.createDeployment(deployment);

                        File newFolder = ImageRepository.getFolder(deployment);
                        if (!newFolder.exists()) {
                            newFolder.mkdirs();
                        }

                        // copy images
                        Util.copyFolder(oldFolder, newFolder);

                    } else {

                        // images dir
                        File oldFolder = new File(
                                imageDir.getAbsolutePath() + File.separatorChar
                                + deployment.getEvent().getProject().getProjectId() + File.separatorChar
                                + deployment.getEvent().getEventId() + File.separatorChar
                                + deployment.getCameraTrap().getCameraTrapArray().getCameraTrapArrayId() + File.separatorChar
                                + deployment.getCameraTrap().getCameraTrapId());

                        ProjectService projectService = new ProjectServiceImpl();

                        // set camera 
                        Camera camera = deployment.getCamera();
                        deployment.setCamera(cameraMap.get(camera.getCameraId()));

                        // set event
                        Event event = deployment.getEvent();
                        Integer oldEventId = event.getEventId();
                        Event tmpEvent = eventMap.get(oldEventId);
                        if (tmpEvent != null) {
                            deployment.setEvent(tmpEvent);
                        } else {
                            event.setEventId(null);
                            event.setProject(newProject);
                            newProject.getEvents().add(event);
                            EventService eventService = new EventServiceImpl();
                            eventService.addEvent(event);
                            eventMap.put(oldEventId, event);
                            newEvents.add(event);
                        }

                        // save arrays and trap
                        CameraTrap trap = deployment.getCameraTrap();

                        /*
                         String[] trapNameElements = trap.getName().split("-");
                         if (trapNameElements.length == 4 && trapNameElements[0].equals(language.getString("project_array_default_prefix"))) {
                         trap.setName(trapNameElements[0] + "-" + projectAbbrev + "-" + trapNameElements[2] + "-" + trapNameElements[3]);
                         }
                         */
                        trap.setCameraTrapId(null);

                        CameraTrapArray array = arrayMap.get(trap.getCameraTrapArray().getCameraTrapArrayId());
                        if (array == null) {
                            array = trap.getCameraTrapArray();
                            array.setCameraTrapArrayId(null);
                            array.setProject(newProject);
                            CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
                            arrayService.addCameraTrapArray(array);
                            arrayMap.put(array.getCameraTrapArrayId(), array);
                            newArrays.add(array);
                        }

                        trap.setCameraTrapArray(array);
                        array.getCameraTraps().add(trap);

                        CameraTrapService trapService = new CameraTrapServiceImpl();
                        trapService.addCameraTrap(trap);
                        newTraps.add(trap);

                        //this.pane.createNewCameraTrap(trap);
                        // save deployment
                        deployment.setDeploymentId(null);

                        if (deployment.getSetupPerson() != null) {
                            log.info("setup person: " + deployment.getSetupPerson().getEmail());
                            for (Person tmpPerson : persons) {
                                log.info("check: " + tmpPerson.getEmail());
                                if (tmpPerson.getEmail().equals(deployment.getSetupPerson().getEmail())) {
                                    deployment.setSetupPerson(tmpPerson);
                                    log.info("found: " + tmpPerson.getPersonId());
                                    break;
                                }
                            }
                        }

                        if (deployment.getPickupPerson() != null) {
                            log.info("pickup person: " + deployment.getPickupPerson().getEmail());
                            for (Person tmpPerson : persons) {
                                if (tmpPerson.getEmail().equals(deployment.getPickupPerson().getEmail())) {
                                    deployment.setPickupPerson(tmpPerson);
                                    log.info("found: " + tmpPerson.getPersonId());
                                    break;
                                }
                            }
                        }

                        deployment.setBaitType(processBaitType(deployment.getBaitType()));
                        deployment.setFeatureType(processFeatureType(deployment.getFeatureType()));
                        deployment.setFailureType(processFailureType(deployment.getFailureType()));

                        // set name
                        if (deployment.getName() == null) {
                            String defaultDeploymentName = newProject.getAbbrevName()
                                    + "_" + deployment.getEvent().getName().replaceAll(" ", "_")
                                    + "_" + trap.getName().replaceAll(" ", "_");
                            deployment.setName(defaultDeploymentName);
                        }

                        DeploymentService deployService = new DeploymentServiceImpl();

                        // save images
                        Set<ImageSequence> sequences = deployment.getImageSequences();
                        List<Image> images = new ArrayList<>();
                        for (ImageSequence seq : sequences) {
                            seq.setImageSequenceId(null);
                            seq.setDeployment(deployment);
                            for (Image img : seq.getImages()) {

                                if (img.getPerson() != null) {
                                    for (Person tmpPerson : persons) {
                                        if (tmpPerson.getEmail().equals(img.getPerson().getEmail())) {
                                            img.setPerson(tmpPerson);
                                            break;
                                        }
                                    }
                                }

                                increaseDone();
                                finished(language.getString("open_transfer_file_progress_save_image_info") + img.getRawName());

                                img.setImageId(null);
                                img.setImageSequence(seq);
                                for (ImageExif exif : img.getImageExifs()) {
                                    exif.setImageExifId(null);
                                    exif.setImage(img);
                                }
                                for (Object object : img.getImageSpecieses()) {
                                    ImageSpecies is = (ImageSpecies) object;

                                    if (is.getPerson() != null) {
                                        for (Person tmpPerson : persons) {
                                            if (tmpPerson.getEmail().equals(is.getPerson().getEmail())) {
                                                is.setPerson(tmpPerson);
                                                break;
                                            }
                                        }
                                    }

                                    is.setImageSpeciesId(null);
                                    is.setImage(img);
                                }
                            }
                            images.addAll(seq.getImages());
                        }

                        deployService.addDeployment(deployment, images);
                        //this.pane.createDeployment(deployment);

                        File newFolder = ImageRepository.getFolder(deployment);
                        if (!newFolder.exists()) {
                            newFolder.mkdirs();
                        }

                        // copy images
                        Util.copyFolder(oldFolder, newFolder);

                    }

                    //this.pane.showStandardView(newProject);
                }

            }
        } catch (Exception ex) {
            this.errorMessage = ex.getMessage();
            cancel();
            ex.printStackTrace();
        } finally {
            Util.delete(tmpDir);
        }

        return null;
    }

    public String getErrorMessage() {
        if (this.errorMessage == null) {
            return "Unknown error";
        } else {
            return this.errorMessage;
        }
    }

    public void increaseDone() {
        this.val++;
        updateProgress(this.val, max);
    }

    public void finished(String text) {
        updateMessage(text);
    }

    public void updatePane() {
        if (newProject == null) {
            return;
        }
        this.pane.addNewProject(newProject);

        for (Organization org : newOrganizations) {
            this.pane.addNewOrganization(newProject, org);
        }

        for (Person person : newPersons) {
            this.pane.addNewPerson(newProject, person);
        }

        for (Event event : newEvents) {
            this.pane.createEvent(event);
        }

        for (CameraTrapArray array : newArrays) {
            this.pane.createNewCameraTrapArray(array);
        }

        for (CameraTrap trap : newTraps) {
            this.pane.createNewCameraTrap(trap);
        }

        this.pane.showStandardView(newProject);

    }

    private Country processCountry(Country country) {
        if (country == null) {
            return null;
        }

        Country usedCountry = null;
        for (Country tmpCountry : countries) {
            if (tmpCountry.getName().equals(country.getName())) {
                usedCountry = tmpCountry;
                break;
            }
        }

        return usedCountry;
    }

    private BaitType processBaitType(BaitType baitType) {
        if (baitType == null) {
            return null;
        }

        BaitType usedBaitType = null;
        for (BaitType tmpBaitType : baitTypes) {
            if (tmpBaitType.getName().equals(baitType.getName())) {
                usedBaitType = tmpBaitType;
                break;
            }
        }

        return usedBaitType;
    }

    private FeatureType processFeatureType(FeatureType featureType) {
        if (featureType == null) {
            return null;
        }

        FeatureType usedFeatureType = null;
        for (FeatureType tmpFeatureType : featureTypes) {
            if (tmpFeatureType.getName().equals(featureType.getName())) {
                usedFeatureType = tmpFeatureType;
                break;
            }
        }

        return usedFeatureType;
    }

    private FailureType processFailureType(FailureType failureType) {
        if (failureType == null) {
            return null;
        }

        FailureType usedFailureType = null;
        for (FailureType tmpFailureType : failureTypes) {
            if (tmpFailureType.getName().equals(failureType.getName())) {
                usedFailureType = tmpFailureType;
                break;
            }
        }

        return usedFailureType;
    }

    private Role processRole(Role role) {
        if (role == null) {
            return null;
        }

        Role usedRole = null;
        for (Role tmpRole : roles) {
            if (tmpRole.getName().equals(role.getName())) {
                usedRole = tmpRole;
                break;
            }
        }

        return usedRole;
    }

}
