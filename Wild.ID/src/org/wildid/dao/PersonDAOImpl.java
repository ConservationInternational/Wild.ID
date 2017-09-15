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
import org.wildid.entity.Person;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.ProjectPersonRole;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class PersonDAOImpl implements PersonDAO {

    @Override
    public void addPerson(Person person) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(person);
        for (Object ppr : person.getProjectPersonRoles()) {
            s.save(ppr);
        }
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<Person> listPerson() {
        List<Person> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("FROM Person AS person ORDER BY person.lastName, person.firstName").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public void removePerson(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Person c = (Person) s.load(Person.class, id);
        for (ProjectPersonRole ppr : c.getProjectPersonRoles()) {
            s.delete(ppr);
        }
        s.delete(c);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void updatePerson(Person person) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(person);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public boolean inProjects(Person person) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List list = s.createQuery("FROM ProjectPersonRole AS ppr WHERE ppr.person.personId=" + person.getPersonId()).list();
        s.getTransaction().commit();
        s.close();
        return !list.isEmpty();
    }

    @Override
    public boolean inDeployments(Person person) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List list = s.createQuery("FROM Deployment AS deployment WHERE "
                + "deployment.setupPerson.personId=" + person.getPersonId() + " OR "
                + "deployment.pickupPerson.personId=" + person.getPersonId()).list();
        s.getTransaction().commit();
        s.close();
        return !list.isEmpty();
    }

    @Override
    public boolean inAnnotations(Person person) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List list = s.createQuery("FROM Image AS image WHERE image.person.personId=" + person.getPersonId()).list();
        if (list.isEmpty()) {
            list = s.createQuery("FROM ImageSpecies AS is WHERE is.person.personId=" + person.getPersonId()).list();
        }

        s.getTransaction().commit();
        s.close();

        return !list.isEmpty();
    }
}
