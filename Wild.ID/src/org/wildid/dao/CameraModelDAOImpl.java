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
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraModelExifFeature;
import org.wildid.entity.HibernateUtil;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class CameraModelDAOImpl implements CameraModelDAO {

    @Override
    public void addCameraModel(CameraModel cameraModel) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(cameraModel);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public boolean isCameraModelExist(CameraModel cameraModel) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<CameraModel> list = s.createQuery("FROM CameraModel AS cm WHERE cm.maker='" + cameraModel.getMaker() + "' AND cm.name='" + cameraModel.getName() + "'").list();
        s.getTransaction().commit();
        s.close();
        return list != null && !list.isEmpty();
    }

    @Override
    public CameraModel getCameraModel(String modelName, String maker) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<CameraModel> list = s.createQuery("FROM CameraModel AS cm WHERE cm.maker='" + maker + "' AND cm.name='" + modelName + "'").list();
        s.getTransaction().commit();
        s.close();
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    @Override
    public List<CameraModel> listCameraModelByMaker(String maker) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<CameraModel> list = s.createQuery("FROM CameraModel AS cm WHERE cm.maker='" + maker + "'").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public List<CameraModel> listCameraModel() {
        List<CameraModel> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("FROM CameraModel AS cm ORDER BY cm.name, cm.maker").list();
        for (CameraModel model : list) {
            for (CameraModelExifFeature feature : model.getCameraModelExifFeatures()) {
                Hibernate.initialize(feature.getImageFeature());
            }
        }
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public void removeCameraModel(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = s.beginTransaction();
        try {
            CameraModel cameraModel = (CameraModel) s.load(CameraModel.class, id);
            s.delete(cameraModel);
            s.getTransaction().commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        } finally {
            s.close();
        }
    }

    @Override
    public void updateCameraModel(CameraModel c) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(c);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<String> getCameraModelNames(String makerName) {
        List<String> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("SELECT DISTINCT cm.name FROM CameraModel AS cm WHERE cm.maker='" + makerName + "' ORDER BY cm.name").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public List<String> getCameraMakerNames() {
        List<String> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("SELECT DISTINCT cm.maker FROM CameraModel AS cm ORDER BY cm.maker").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public void addExifFeatures(CameraModel cameraModel, List<CameraModelExifFeature> modelFeatures) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();

        // delete old camera model exif features
        List<CameraModelExifFeature> list = s.createQuery("FROM CameraModelExifFeature AS feature WHERE feature.cameraModel.cameraModelId=" + cameraModel.getCameraModelId()).list();
        for (CameraModelExifFeature modelFeature : list) {
            s.delete(modelFeature);
        }

        // add new camera model exif features
        for (CameraModelExifFeature modelFeature : modelFeatures) {
            s.save(modelFeature);
        }
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void updateExifFeatures(List<CameraModelExifFeature> modelFeatures) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        for (CameraModelExifFeature modelFeature : modelFeatures) {
            s.update(modelFeature);
        }
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void deleteExifFeatures(CameraModel cameraModel) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();

        // delete old camera model exif features
        List<CameraModelExifFeature> list = s.createQuery("FROM CameraModelExifFeature AS feature WHERE feature.cameraModel.cameraModelId=" + cameraModel.getCameraModelId()).list();
        for (CameraModelExifFeature modelFeature : list) {
            s.delete(modelFeature);
        }
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<Camera> getCameras(CameraModel cameraModel) {
        List<Camera> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("FROM Camera AS camera WHERE camera.cameraModel.cameraModelId=" + cameraModel.getCameraModelId()).list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

}
