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
package org.wildid.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.wildid.dao.ProjectDAO;
import org.wildid.dao.ProjectDAOImpl;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.EventComparator;
import org.wildid.entity.Person;
import org.wildid.entity.PersonComparator;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectOrganization;
import org.wildid.entity.ProjectPersonRole;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectServiceImpl implements ProjectService {

    private ProjectDAO projectDAO = new ProjectDAOImpl();

    @Override
    public void addProject(Project project) {
        projectDAO.addProject(project);
    }

    @Override
    public List<Project> listProject() {
        return projectDAO.listProject();
    }

    @Override
    public void removeProject(Integer id) {
        projectDAO.removeProject(id);
    }

    @Override
    public void updateProject(Project project) {
        projectDAO.updateProject(project);
    }

    @Override
    public void addProjectOrganization(ProjectOrganization projectOrganization) {
        projectDAO.addProjectOrganization(projectOrganization);
    }

    @Override
    public Project loadProjectWithDeployments(Integer id) {
        return projectDAO.loadProjectWithDeployments(id);
    }

    @Override
    public List<Deployment> getDeployments(Project project) {
        return projectDAO.getDeployments(project);
    }

    @Override
    public List<Deployment> getDeployments(Event event) {
        return projectDAO.getDeployments(event);
    }

    @Override
    public List<Deployment> getDeployments(Event event, CameraTrapArray array) {
        return projectDAO.getDeployments(event, array);
    }

    @Override
    public Deployment getDeployment(Event event, CameraTrap trap) {
        return projectDAO.getDeployment(event, trap);
    }

    @Override
    public List<Event> getUnfinishedEvents(Project project) {
        List<Event> events = new ArrayList<>();
        for (Event event : project.getEvents()) {
            boolean loaded = true;
            for (CameraTrapArray array : project.getCameraTrapArrays()) {
                for (CameraTrap trap : array.getCameraTraps()) {
                    boolean found = false;
                    for (Deployment deployment : trap.getDeployments()) {
                        if (deployment.getEvent().getEventId() == event.getEventId().intValue()
                                && deployment.getCameraTrap().getCameraTrapId() == trap.getCameraTrapId().intValue()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        loaded = false;
                        break;
                    }
                }
                if (!loaded) {
                    break;
                }
            }
            if (!loaded) {
                events.add(event);
            }
        }
        return events;
    }

    public TreeMap<Event, List<CameraTrap>> getUnfinishedEventToTrapMap(Project project) {

        TreeMap<Event, List<CameraTrap>> event2trap = new TreeMap<>(new EventComparator());
        for (Event event : project.getEvents()) {
            List<CameraTrap> traps = new ArrayList<>();
            for (CameraTrapArray array : project.getCameraTrapArrays()) {
                for (CameraTrap trap : array.getCameraTraps()) {
                    boolean found = false;
                    for (Deployment deployment : trap.getDeployments()) {
                        if (deployment.getEvent().getEventId() == event.getEventId().intValue()
                                && deployment.getCameraTrap().getCameraTrapId() == trap.getCameraTrapId().intValue()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        traps.add(trap);
                    }
                }
            }
            if (!traps.isEmpty()) {
                event2trap.put(event, traps);
            }
        }
        return event2trap;
    }

    @Override
    public Map<Event, List<Camera>> getEventToUnusedCameraMap(Project project) {

        TreeMap<Event, List<Camera>> event2camera = new TreeMap<>(new EventComparator());
        for (Event event : project.getEvents()) {
            List<Camera> cameras = new ArrayList<>(project.getCameras());
            for (CameraTrapArray array : project.getCameraTrapArrays()) {
                for (CameraTrap trap : array.getCameraTraps()) {
                    for (Deployment deployment : trap.getDeployments()) {
                        if (deployment.getEvent().getEventId() == event.getEventId().intValue()
                                && deployment.getCameraTrap().getCameraTrapId() == trap.getCameraTrapId().intValue()) {
                            cameras.remove(deployment.getCamera());
                        }
                    }
                }

            }
            event2camera.put(event, cameras);
        }
        return event2camera;
    }

    @Override
    public List<Person> getPersons(Project project) {
        TreeSet<Person> persons = new TreeSet<>(new PersonComparator());
        for (Object object : project.getProjectPersonRoles()) {
            ProjectPersonRole ppo = (ProjectPersonRole) object;
            persons.add(ppo.getPerson());
        }
        return new ArrayList<>(persons);
    }

    @Override
    public Map<Event, Map<CameraTrap, List<Camera>>> getEventToTrapToCameraMap(Project project) {

        Map<Event, Map<CameraTrap, List<Camera>>> event2trap2camera = new HashMap<>();
        for (Event event : project.getEvents()) {

            Map<CameraTrap, List<Camera>> trap2camera = new HashMap<>();
            for (CameraTrapArray array : project.getCameraTrapArrays()) {

                // all cameras can be used for this array
                List<Camera> cameras = new ArrayList<>(project.getCameras());
                List<CameraTrap> deployedTraps = new ArrayList<>();
                for (CameraTrap trap : array.getCameraTraps()) {
                    for (Deployment deployment : trap.getDeployments()) {
                        if (deployment.getEvent().getEventId() == event.getEventId().intValue()
                                && deployment.getCameraTrap().getCameraTrapId() == trap.getCameraTrapId().intValue()) {
                            cameras.remove(deployment.getCamera());
                            deployedTraps.add(trap);
                        }
                    }
                }

                for (CameraTrap trap : array.getCameraTraps()) {
                    if (!deployedTraps.contains(trap)) {
                        trap2camera.put(trap, cameras);
                    }
                }
            }
            event2trap2camera.put(event, trap2camera);
        }

        return event2trap2camera;
    }
}
