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
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.Deployment;
import org.wildid.entity.Image;
import org.wildid.entity.ImageExif;
import org.wildid.entity.ImageIndividual;
import org.wildid.entity.ImageSequence;
import org.wildid.entity.ImageSpecies;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class DeploymentDAOImpl implements DeploymentDAO {

    @Override
    public void addDeployment(Deployment deployment, List<Image> images) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(deployment);
        for (Image image : images) {
            s.save(image.getImageSequence());
            s.save(image);
            for (ImageExif exif : image.getImageExifs()) {
                s.save(exif);
            }
            for (Object object : image.getImageSpecieses()) {
                ImageSpecies is = (ImageSpecies) object;
                s.save(is);

                for (ImageIndividual ind : is.getImageIndividuals()) {
                    s.save(ind);
                }
            }
        }
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<Deployment> listDeployment() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Deployment> list = s.createQuery("FROM Deployment AS deployment").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public void removeDeployment(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Deployment deployment = (Deployment) s.load(Deployment.class, id);
        for (ImageSequence sequence : deployment.getImageSequences()) {
            Hibernate.initialize(sequence);
            for (Image image : sequence.getImages()) {
                for (ImageExif exif : image.getImageExifs()) {
                    s.delete(exif);
                }
                for (Object object : image.getImageSpecieses()) {
                    ImageSpecies is = (ImageSpecies) object;
                    for (ImageIndividual ind : is.getImageIndividuals()) {
                        s.delete(ind);
                    }
                    s.delete(is);
                }
                s.delete(image);
            }
            s.delete(sequence);
        }
        s.delete(deployment);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void updateDeployment(Deployment deployment) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(deployment);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<Image> getImages(Deployment deployment) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Image> list = s.createQuery(
                "FROM Image AS img WHERE img.imageSequence.deployment.deploymentId=" + deployment.getDeploymentId() + " ORDER BY img.rawName").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public boolean hasDeloymentWithName(String deploymentName) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List list = s.createQuery("FROM Deployment AS deployment WHERE deployment.name='" + deploymentName + "'").list();
        s.getTransaction().commit();
        s.close();
        return !list.isEmpty();
    }

    @Override
    public boolean hasAnotherDeloymentWithName(String deploymentName, Deployment deployment) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List list = s.createQuery("FROM Deployment AS deployment "
                + "WHERE deployment.name='" + deploymentName + "' "
                + "  AND NOT deployment.id=" + deployment.getDeploymentId()).list();
        s.getTransaction().commit();
        s.close();
        return !list.isEmpty();
    }
}
