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
import org.wildid.entity.CameraTrap;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class CameraTrapArrayDAOImpl implements CameraTrapArrayDAO {

    @Override
    public void addCameraTrapArray(CameraTrapArray array) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(array);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<CameraTrapArray> listCameraTrapArray() {
        List<CameraTrapArray> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("FROM CameraTrapArray AS array ORDER BY array.name").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public CameraTrapArray getCameraTrapArrayByProjectAndName(Project project, String arrayName) {
        List<CameraTrapArray> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("FROM CameraTrapArray AS array WHERE array.project.projectId=" + project.getProjectId() + " AND array.name='" + arrayName + "' ORDER BY array.name").list();
        s.getTransaction().commit();
        s.close();

        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    @Override
    public void removeCameraTrapArray(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        CameraTrapArray c = (CameraTrapArray) s.load(CameraTrapArray.class, id);
        c.getCameraTraps().stream().map((object) -> (CameraTrap) object).forEach((trap) -> {
            s.delete(trap);
        });
        s.delete(c);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void updateCameraTrapArray(CameraTrapArray array) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(array);
        s.getTransaction().commit();
        s.close();
    }

}
