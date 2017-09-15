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
import org.wildid.dao.OrganizationDAOImpl;
import org.wildid.entity.Organization;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectOrganization;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class OrganizationServiceImpl implements OrganizationService {

    private OrganizationDAOImpl organizationDAO = new OrganizationDAOImpl();

    @Override
    public void addOrganization(Organization organization) {
        organizationDAO.addOrganization(organization);
    }

    @Override
    public List<Organization> listOrganization() {
        return organizationDAO.listOrganization();
    }

    @Override
    public void removeOrganization(Integer id) {
        organizationDAO.removeOrganization(id);
    }

    @Override
    public void updateOrganization(Organization organization) {
        organizationDAO.updateOrganization(organization);
    }

    @Override
    public List<Organization> getNonMemberOrganizations(Project project) {
        List<Organization> nonMembers = new ArrayList<Organization>();
        Set<ProjectOrganization> pos = project.getProjectOrganizations();

        for (Organization org : this.listOrganization()) {
            boolean isMember = false;
            for (ProjectOrganization po : pos) {
                if (po.getOrganization().getOrganizationId().intValue() == org.getOrganizationId().intValue()) {
                    isMember = true;
                    break;
                }
            }

            if (!isMember) {
                nonMembers.add(org);
            }
        }
        return nonMembers;
    }

    @Override
    public ProjectOrganization getProjectOrganization(Organization org, Project project) {
        return organizationDAO.getProjectOrganization(org, project);
    }

    @Override
    public void removeProjectOrganization(ProjectOrganization po) {
        organizationDAO.removeProjectOrganization(po);
    }

    @Override
    public boolean inProjects(Organization org) {
        return organizationDAO.inProjects(org);
    }

    @Override
    public boolean inPersons(Organization org) {
        return organizationDAO.inPersons(org);
    }

}
