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
import org.hibernate.Session;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.Age;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class AgeDAOImpl implements AgeDAO {

    @Override
    public void addAge(Age age) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(age);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<Age> listAge() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Age> list = s.createQuery("FROM Age AS age ORDER BY age.ageId ").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public void removeAge(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Age c = (Age) s.load(Age.class, id);
        s.delete(c);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void updateAge(Age age) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(age);
        s.getTransaction().commit();
        s.close();
    }
}
