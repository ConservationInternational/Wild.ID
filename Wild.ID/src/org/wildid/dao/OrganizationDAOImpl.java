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

import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.Organization;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectOrganization;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class OrganizationDAOImpl implements OrganizationDAO {

    @Override
    public void addOrganization(Organization organization) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(organization);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<Organization> listOrganization() {
        List<Organization> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("FROM Organization AS organization ORDER BY organization.name").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public void removeOrganization(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Organization c = (Organization) s.load(Organization.class, id);
        for (ProjectOrganization po : c.getProjectOrganizations()) {
            s.delete(po);
        }
        s.delete(c);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void updateOrganization(Organization organization) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(organization);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public ProjectOrganization getProjectOrganization(Organization org, Project project) {
        List<ProjectOrganization> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery(
                "FROM ProjectOrganization AS po WHERE "
                + " po.project.projectId=" + project.getProjectId() + " AND "
                + " po.organization.organizationId=" + org.getOrganizationId()).list();
        s.getTransaction().commit();
        s.close();

        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public void removeProjectOrganization(ProjectOrganization po) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.delete(po);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public boolean inProjects(Organization org) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List list = s.createQuery("FROM ProjectOrganization AS po WHERE po.organization.organizationId=" + org.getOrganizationId()).list();
        s.getTransaction().commit();
        s.close();
        return !list.isEmpty();
    }

    @Override
    public boolean inPersons(Organization org) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List list = s.createQuery("FROM Person AS p WHERE p.organization.organizationId=" + org.getOrganizationId()).list();
        s.getTransaction().commit();
        s.close();
        return !list.isEmpty();
    }
}