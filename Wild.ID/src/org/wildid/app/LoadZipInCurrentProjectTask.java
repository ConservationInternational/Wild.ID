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
import java.util.List;
import java.util.Set;
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
public class LoadZipInCurrentProjectTask extends Task<Void> {

    private int val;
    private int max;
    private final LanguageModel language;
    private final File zipFile;
    private final File tmpDir;
    private final ObservableList<Person> persons;
    private final ObservableList<Organization> organizations;
    private final Project project;
    private final List<Organization> newOrganizations = new ArrayList<>();
    private final List<Person> newPersons = new ArrayList<>();
    private final List<Event> newEvents = new ArrayList<>();
    private final List<CameraTrapArray> newArrays = new ArrayList<>();
    private final List<CameraTrap> newTraps = new ArrayList<>();
    private final List<Deployment> newDeployments = new ArrayList<>();
    private final List<Country> countries;
    private final List<BaitType> baitTypes;
    private final List<FeatureType> featureTypes;
    private final List<FailureType> failureTypes;
    private final List<Role> roles;
    private final WildIDPane pane;
    private String errorMessage;

    //initializing the logger
    static Logger log = Logger.getLogger(LoadZipInCurrentProjectTask.class.getName());

    public LoadZipInCurrentProjectTask(LanguageModel language,
            File zipFile,
            File tmpDir,
            Project project,
            WildIDPane pane,
            ObservableList<Person> persons,
            ObservableList<Organization> organizations) {
        this.language = language;
        this.zipFile = zipFile;
        this.tmpDir = tmpDir;
        this.project = project;
        this.pane = pane;
        this.persons = persons;
        this.organizations = organizations;
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

            log.info("total image number: " + max);

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

                HashMap<Integer, Event> eventMap = new HashMap<>();
                HashMap<Integer, CameraTrapArray> arrayMap = new HashMap<>();
                HashMap<Integer, Camera> cameraMap = new HashMap<>();
                HashMap<Integer, CameraTrap> trapMap = new HashMap<>();

                Set<Camera> cameras = project.getCameras();
                Set<Event> events = project.getEvents();
                Set<CameraTrapArray> arrays = project.getCameraTrapArrays();

                String[] xmlFileNames = dataDir.list();
                max += xmlFileNames.length + 6;

                for (String xmlFileName : xmlFileNames) {
                    File xmlFile = new File(dataDir, xmlFileName);

                    // unmarshal xml file
                    JAXBElement<Deployment> df;
                    JAXBContext context = JAXBContext.newInstance(Deployment.class);
                    Unmarshaller unmarshaller = context.createUnmarshaller();
                    df = unmarshaller.unmarshal(new StreamSource(new InputStreamReader(new FileInputStream(xmlFile), "UTF8")),
                            Deployment.class);
                    Deployment deployment = df.getValue();

                    log.info("processing the data for the camera trap: " + deployment.getCameraTrap().getName());

                    // images dir
                    File oldFolder = new File(
                            imageDir.getAbsolutePath() + File.separatorChar
                            + deployment.getEvent().getProject().getProjectId() + File.separatorChar
                            + deployment.getEvent().getEventId() + File.separatorChar
                            + deployment.getCameraTrap().getCameraTrapArray().getCameraTrapArrayId() + File.separatorChar
                            + deployment.getCameraTrap().getCameraTrapId());

                    log.info("image folder: " + oldFolder.getAbsolutePath());

                    // check if the deployment is in the project
                    boolean foundDeployment = false;
                    ProjectService projectService = new ProjectServiceImpl();
                    List<Deployment> deployments = projectService.getDeployments(project);
                    for (Deployment tmpDeployment : deployments) {
                        if (tmpDeployment.getEvent().getName().equals(deployment.getEvent().getName())
                                && tmpDeployment.getCameraTrap().getName().equals(deployment.getCameraTrap().getName())
                                && tmpDeployment.getCamera().getSerialNumber().equals(deployment.getCamera().getSerialNumber())) {

                            log.info("deployment exists: "
                                    + "event: " + tmpDeployment.getEvent().getName()
                                    + "camera trap: " + tmpDeployment.getCameraTrap().getName()
                                    + "camera: " + tmpDeployment.getCamera().getSerialNumber());

                            foundDeployment = true;
                            break;
                        }
                    }

                    if (foundDeployment) {
                        this.val += Util.countJpgFiles(oldFolder);
                        updateProgress(this.val, max);
                        continue;
                    }

                    Set<ProjectPersonRole> dpprs = deployment.getEvent().getProject().getProjectPersonRoles();

                    // process persons
                    for (ProjectPersonRole ppr : dpprs) {
                        Role role = ppr.getRole();
                        ppr.setRole(processRole(role));
                        ppr.setProject(this.project);
                    }

                    for (ProjectPersonRole ppr : dpprs) {
                        Person person = ppr.getPerson();
                        ppr.setPerson(processPerson(person, dpprs, "set project role"));
                    }

                    // save camera
                    Camera usedCamera = null;
                    for (Camera camera : cameras) {
                        if (camera.getSerialNumber().equals(deployment.getCamera().getSerialNumber())) {
                            deployment.setCamera(camera);
                            usedCamera = camera;
                            break;
                        }
                    }

                    if (usedCamera == null) {
                        Camera camera = deployment.getCamera();
                        Integer oldId = camera.getCameraId();
                        camera.setCameraId(null);
                        camera.setProject(project);

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

                        usedCamera = camera;
                        cameras.add(usedCamera);
                    }

                    // save event
                    Event usedEvent = null;
                    EventService eventService = new EventServiceImpl();
                    for (Event event : eventService.listEvent()) {
                        log.info("check event: " + event.getName());
                        if (event.getName().equals(deployment.getEvent().getName())
                                && event.getProject().getProjectId().intValue() == project.getProjectId().intValue()) {
                            deployment.setEvent(event);
                            usedEvent = event;
                            break;
                        }
                    }

                    if (usedEvent == null) {
                        Event event = deployment.getEvent();
                        Integer oldEventId = event.getEventId();
                        event.setEventId(null);
                        event.setProject(project);

                        increaseDone();
                        finished(language.getString("open_transfer_file_progress_save_event_info") + event.getName());

                        eventService.addEvent(event);
                        eventMap.put(oldEventId, event);
                        newEvents.add(event);

                        usedEvent = event;
                        events.add(usedEvent);
                    }

                    // save array
                    CameraTrap trap = deployment.getCameraTrap();
                    CameraTrapArray usedArray = null;
                    CameraTrap usedTrap = null;
                    for (CameraTrapArray array : arrays) {
                        if (array.getName().equals(trap.getCameraTrapArray().getName())) {
                            trap.setCameraTrapArray(array);
                            usedArray = array;

                            for (CameraTrap tmpTrap : array.getCameraTraps()) {
                                if (tmpTrap.getName().equals(trap.getName())) {
                                    usedTrap = tmpTrap;
                                    deployment.setCameraTrap(tmpTrap);
                                    break;
                                }
                            }

                            break;
                        }
                    }

                    if (usedArray == null) {
                        CameraTrapArray array = trap.getCameraTrapArray();
                        Integer oldArrayId = array.getCameraTrapArrayId();
                        array.setCameraTrapArrayId(null);
                        array.setProject(project);

                        increaseDone();
                        finished(language.getString("open_transfer_file_progress_save_array_info") + array.getName());

                        CameraTrapArrayService arrayService = new CameraTrapArrayServiceImpl();
                        arrayService.addCameraTrapArray(array);
                        arrayMap.put(oldArrayId, array);
                        newArrays.add(array);

                        usedArray = array;
                        arrays.add(usedArray);
                    }

                    if (usedTrap == null) {
                        Integer oldTrapId = trap.getCameraTrapId();
                        trap.setCameraTrapId(null);
                        usedArray.getCameraTraps().add(trap);
                        trap.setCameraTrapArray(usedArray);

                        increaseDone();
                        finished(language.getString("open_transfer_file_progress_save_trap_info") + trap.getName());

                        CameraTrapService trapService = new CameraTrapServiceImpl();
                        trapService.addCameraTrap(trap);
                        trapMap.put(oldTrapId, trap);
                        newTraps.add(trap);
                        usedTrap = trap;
                    }

                    // check setup person
                    Person setupPerson = deployment.getSetupPerson();
                    deployment.setSetupPerson(processPerson(setupPerson, dpprs, "setup"));

                    // check pickup person
                    Person pickupPerson = deployment.getPickupPerson();
                    deployment.setPickupPerson(processPerson(pickupPerson, dpprs, "pickup"));

                    // save deployment
                    deployment.setDeploymentId(null);
                    DeploymentService deployService = new DeploymentServiceImpl();

                    // set bait/feature/failure type
                    deployment.setBaitType(processBaitType(deployment.getBaitType()));
                    deployment.setFeatureType(processFeatureType(deployment.getFeatureType()));
                    deployment.setFailureType(processFailureType(deployment.getFailureType()));

                    // set name
                    if (deployment.getName() == null) {
                        String defaultDeploymentName = project.getAbbrevName()
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

                            // check the identifying person
                            Person idPerson = img.getPerson();
                            img.setPerson(processPerson(idPerson, dpprs, "identifying"));

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
                                is.setImageSpeciesId(null);
                                is.setImage(img);

                                is.setPerson(processPerson(is.getPerson(), dpprs, "annotating"));

                                for (ImageIndividual ind : is.getImageIndividuals()) {
                                    ind.setImageIndividualId(null);
                                    ind.setImageSpecies(is);
                                }

                            }
                        }
                        images.addAll(seq.getImages());
                    }

                    deployService.addDeployment(deployment, images);
                    newDeployments.add(deployment);

                    File newFolder = ImageRepository.getFolder(deployment);
                    if (!newFolder.exists()) {
                        newFolder.mkdirs();
                    }
                    log.info("copy image files to " + newFolder.getAbsolutePath());

                    // copy images
                    Util.copyFolder(oldFolder, newFolder);

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

        for (Organization org : newOrganizations) {
            this.pane.addNewOrganization(project, org);
        }

        for (Person person : newPersons) {
            this.pane.addNewPerson(project, person);
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

        for (Deployment deployment : newDeployments) {
            this.pane.createDeployment(deployment);
        }

        this.pane.showStandardImageView(project);
    }

    private List<Role> getRoles(Set<ProjectPersonRole> pprs, Person person) {

        List<Role> roles = new ArrayList<>();
        for (ProjectPersonRole ppr : pprs) {
            if (ppr.getPerson().getPersonId().intValue() == person.getPersonId().intValue()) {
                roles.add(ppr.getRole());
            }
        }
        return roles;
    }

    private Person processPerson(Person aPerson, Set<ProjectPersonRole> dpprs, String note) {

        if (aPerson == null) {
            return null;
        }

        List<Role> roles = getRoles(dpprs, aPerson);
        Person usedPerson = null;
        for (Person tmpPerson : persons) {
            if (tmpPerson.getEmail().equals(aPerson.getEmail())) {
                usedPerson = tmpPerson;
                break;
            }
        }

        if (usedPerson == null) {
            aPerson.setPersonId(null);

            // check the organization
            Organization org = aPerson.getOrganization();
            aPerson.setOrganization(processOrganization(org));

            // check the country
            Country country = aPerson.getCountry();
            aPerson.setCountry(processCountry(country));

            // save this person
            increaseDone();
            finished(language.getString("open_transfer_file_progress_save_person_info") + aPerson.getEmail());

            PersonService personService = new PersonServiceImpl();
            personService.addPerson(aPerson);
            persons.add(aPerson);

            usedPerson = aPerson;
        }

        // check if usedPerson is in the project
        if (getRoles(project.getProjectPersonRoles(), usedPerson).isEmpty()) {
            log.info("The " + note + " person " + aPerson.getEmail() + " is not in the project.");

            List<ProjectPersonRole> npprs = new ArrayList<>();
            for (Role role : roles) {
                log.info("role: " + role.getName());
                ProjectPersonRole ppr = new ProjectPersonRole(usedPerson, project, processRole(role), null, null);
                usedPerson.getProjectPersonRoles().add(ppr);
                npprs.add(ppr);
            }

            if (!npprs.isEmpty()) {
                PersonService personService = new PersonServiceImpl();
                personService.addProjectPersonRoles(npprs);
                project.getProjectPersonRoles().addAll(npprs);
            }

            boolean contained = false;
            for (Person tmpPerson : newPersons) {
                if (tmpPerson.getPersonId().intValue() == usedPerson.getPersonId()) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                newPersons.add(usedPerson);
            }

        } else {
            log.info("The " + note + " person " + aPerson.getEmail() + " is already in the project.");
        }

        return usedPerson;
    }

    private Organization processOrganization(Organization aOrg) {

        if (aOrg == null) {
            return null;
        }

        Organization usedOrg = null;
        for (Organization tmpOrg : organizations) {
            if (tmpOrg.getName().equals(aOrg.getName())) {
                // org is already in the project
                usedOrg = tmpOrg;
                break;
            }
        }

        if (usedOrg == null) {
            aOrg.setOrganizationId(null);
            aOrg.setCountry(processCountry(aOrg.getCountry()));
            OrganizationService orgService = new OrganizationServiceImpl();
            orgService.addOrganization(aOrg);
            usedOrg = aOrg;
        }

        return usedOrg;

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
