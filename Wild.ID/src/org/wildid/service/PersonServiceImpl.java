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
import java.util.List;
import java.util.Set;
import org.wildid.dao.PersonDAO;
import org.wildid.dao.PersonDAOImpl;
import org.wildid.dao.ProjectPersonRoleDAO;
import org.wildid.dao.ProjectPersonRoleDAOImpl;
import org.wildid.entity.Person;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectPersonRole;
import org.wildid.entity.Role;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class PersonServiceImpl implements PersonService {

    private PersonDAO personDAO = new PersonDAOImpl();
    private ProjectPersonRoleDAO pprDAO = new ProjectPersonRoleDAOImpl();

    @Override
    public void addPerson(Person person) {
        personDAO.addPerson(person);
    }

    @Override
    public List<Person> listPerson() {
        return personDAO.listPerson();
    }

    @Override
    public void removePerson(Integer id) {
        personDAO.removePerson(id);
    }

    @Override
    public void updatePerson(Person person) {
        personDAO.updatePerson(person);
    }

    @Override
    public List<Person> getNonMembers(Project project) {
        List<Person> nonMembers = new ArrayList<Person>();
        Set<ProjectPersonRole> pprs = project.getProjectPersonRoles();
        for (Person person : this.listPerson()) {
            boolean isMember = false;
            for (ProjectPersonRole ppr : pprs) {
                if (ppr.getPerson().getPersonId().intValue() == person.getPersonId().intValue()) {
                    isMember = true;
                    break;
                }
            }

            if (!isMember) {
                nonMembers.add(person);
            }
        }
        return nonMembers;
    }

    public List<Role> getRoles(Person person, Project project) {
        List<Role> roles = new ArrayList<Role>();
        for (ProjectPersonRole ppr : person.getProjectPersonRoles()) {
            if (ppr.getProject().getProjectId().intValue() == project.getProjectId().intValue()) {
                roles.add(ppr.getRole());
            }
        }
        return roles;
    }

    public List<ProjectPersonRole> getProjectPersonRoles(Person person, Project project) {
        List<ProjectPersonRole> roles = new ArrayList<>();
        for (ProjectPersonRole ppr : project.getProjectPersonRoles()) {
            if (ppr.getProject().getProjectId().intValue() == project.getProjectId().intValue()
                    && ppr.getPerson().getPersonId().intValue() == person.getPersonId().intValue()) {
                roles.add(ppr);
            }
        }
        return roles;
    }

    public void removeProjectPersonRoles(List<ProjectPersonRole> pprs) {
        for (ProjectPersonRole ppr : pprs) {
            pprDAO.removeProjectPersonRole(ppr.getProjectPersonRoleId());
        }
    }

    public void addProjectPersonRoles(List<ProjectPersonRole> pprs) {
        for (ProjectPersonRole ppr : pprs) {
            pprDAO.addProjectPersonRole(ppr);
        }

    }

    @Override
    public boolean inProjects(Person person) {
        return personDAO.inProjects(person);
    }

    @Override
    public boolean inDeployments(Person person) {
        return personDAO.inDeployments(person);
    }

    @Override
    public boolean inAnnotations(Person person) {
        return personDAO.inAnnotations(person);
    }

}
