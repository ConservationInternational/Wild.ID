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
package org.wildid.dao;

import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.Image;
import org.wildid.entity.ImageExif;
import org.wildid.entity.ImageIndividual;
import org.wildid.entity.ImageRepository;
import org.wildid.entity.ImageSequence;
import org.wildid.entity.ImageSpecies;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectOrganization;
import org.wildid.entity.ProjectPersonRole;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectDAOImpl implements ProjectDAO {

    @Override
    public void addProject(Project project) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(project);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<Project> listProject() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Project> list = s.createQuery("FROM Project AS proj  ORDER BY proj.name").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public void removeProject(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Project project = (Project) s.load(Project.class, id);

        for (Object object : project.getProjectOrganizations()) {
            ProjectOrganization po = (ProjectOrganization) object;
            s.delete(po);
        }

        for (Object object : project.getProjectPersonRoles()) {
            ProjectPersonRole ppr = (ProjectPersonRole) object;
            s.delete(ppr);
        }

        for (Object object : project.getCameraTrapArrays()) {
            CameraTrapArray array = (CameraTrapArray) object;
            for (Object obj : array.getCameraTraps()) {
                CameraTrap trap = (CameraTrap) obj;
                for (Deployment deployment : trap.getDeployments()) {
                    ImageRepository.removeDeployment(deployment);
                    for (ImageSequence seq : deployment.getImageSequences()) {
                        for (Image image : seq.getImages()) {
                            for (ImageExif imageExif : image.getImageExifs()) {
                                s.delete(imageExif);
                            }
                            for (Object o : image.getImageSpecieses()) {
                                ImageSpecies is = (ImageSpecies) o;
                                for (ImageIndividual ind : is.getImageIndividuals()) {
                                    s.delete(ind);
                                }
                                s.delete(is);
                            }
                            s.delete(image);
                        }
                        s.delete(seq);
                    }
                    s.delete(deployment);
                }
                s.delete(trap);
            }
            s.delete(array);
        }

        for (Object object : project.getCameras()) {
            Camera camera = (Camera) object;
            s.delete(camera);
        }

        for (Event event : project.getEvents()) {
            s.delete(event);
        }

        s.delete(project);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void updateProject(Project project) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(project);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void addProjectOrganization(ProjectOrganization projectOrganization) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(projectOrganization);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public Project loadProjectWithDeployments(Integer id) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Project> projects = s.createQuery("FROM Project AS proj WHERE proj.projectId=" + id).list();
        for (Project project : projects) {
            for (CameraTrapArray array : project.getCameraTrapArrays()) {
                for (CameraTrap trap : array.getCameraTraps()) {
                    for (Deployment deployment : trap.getDeployments()) {
                        Hibernate.initialize(deployment);
                    }
                }
            }
        }
        s.getTransaction().commit();
        s.close();
        if (projects.isEmpty()) {
            return null;
        } else {
            return projects.get(0);
        }
    }

    @Override
    public List<Deployment> getDeployments(Project project) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Deployment> deployments = s.createQuery("FROM Deployment AS deployment WHERE deployment.event.project.projectId=" + project.getProjectId()).list();
        for (Deployment deployment : deployments) {
            Hibernate.initialize(deployment.getEvent());
            Hibernate.initialize(deployment.getCameraTrap());
            Hibernate.initialize(deployment.getCamera());
            Hibernate.initialize(deployment.getBaitType());
            Hibernate.initialize(deployment.getFeatureType());
            Hibernate.initialize(deployment.getFailureType());
            Hibernate.initialize(deployment.getSetupPerson());
            Hibernate.initialize(deployment.getPickupPerson());
            for (ImageSequence seq : deployment.getImageSequences()) {
                Hibernate.initialize(seq);
            }
        }
        s.getTransaction().commit();
        s.close();
        return deployments;

    }

    @Override
    public List<Deployment> getDeployments(Event event) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Deployment> deployments = s.createQuery("FROM Deployment AS deployment WHERE deployment.event.eventId=" + event.getEventId()).list();
        for (Deployment deployment : deployments) {
            Hibernate.initialize(deployment.getEvent());
            Hibernate.initialize(deployment.getCameraTrap());
            Hibernate.initialize(deployment.getCamera());
            Hibernate.initialize(deployment.getBaitType());
            Hibernate.initialize(deployment.getFeatureType());
            Hibernate.initialize(deployment.getFailureType());
            Hibernate.initialize(deployment.getSetupPerson());
            Hibernate.initialize(deployment.getPickupPerson());
            for (ImageSequence seq : deployment.getImageSequences()) {
                Hibernate.initialize(seq);
            }

        }
        s.getTransaction().commit();
        s.close();
        return deployments;

    }

    @Override
    public List<Deployment> getDeployments(Event event, CameraTrapArray array) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Deployment> deployments = s.createQuery(
                "FROM Deployment AS deployment "
                + " WHERE deployment.event.eventId=" + event.getEventId()
                + "   AND deployment.cameraTrap.cameraTrapArray.cameraTrapArrayId=" + array.getCameraTrapArrayId()).list();
        for (Deployment deployment : deployments) {
            Hibernate.initialize(deployment.getEvent());
            Hibernate.initialize(deployment.getCameraTrap());
            Hibernate.initialize(deployment.getCamera());
            Hibernate.initialize(deployment.getBaitType());
            Hibernate.initialize(deployment.getFeatureType());
            Hibernate.initialize(deployment.getFailureType());
            Hibernate.initialize(deployment.getSetupPerson());
            Hibernate.initialize(deployment.getPickupPerson());
            for (ImageSequence seq : deployment.getImageSequences()) {
                Hibernate.initialize(seq);
            }

        }
        s.getTransaction().commit();
        s.close();
        return deployments;
    }

    @Override
    public Deployment getDeployment(Event event, CameraTrap trap) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Deployment> deployments = s.createQuery(
                "FROM Deployment AS deployment "
                + " WHERE deployment.event.eventId=" + event.getEventId()
                + "   AND deployment.cameraTrap.cameraTrapId=" + trap.getCameraTrapId()).list();
        for (Deployment deployment : deployments) {
            Hibernate.initialize(deployment.getEvent());
            Hibernate.initialize(deployment.getCameraTrap());
            Hibernate.initialize(deployment.getCamera());
            Hibernate.initialize(deployment.getBaitType());
            Hibernate.initialize(deployment.getFeatureType());
            Hibernate.initialize(deployment.getFailureType());
            Hibernate.initialize(deployment.getSetupPerson());
            Hibernate.initialize(deployment.getPickupPerson());
            for (ImageSequence seq : deployment.getImageSequences()) {
                Hibernate.initialize(seq);
            }

        }
        s.getTransaction().commit();
        s.close();
        if (deployments.isEmpty()) {
            return null;
        } else {
            return deployments.get(0);
        }
    }

}
