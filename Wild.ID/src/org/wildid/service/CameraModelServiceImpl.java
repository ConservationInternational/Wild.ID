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

import java.util.List;
import org.wildid.dao.CameraModelDAO;
import org.wildid.dao.CameraModelDAOImpl;
import org.wildid.entity.Camera;
import org.wildid.entity.CameraModel;
import org.wildid.entity.CameraModelExifFeature;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class CameraModelServiceImpl implements CameraModelService {

    private CameraModelDAO cameraModelDAO = new CameraModelDAOImpl();

    @Override
    public void addCameraModel(CameraModel cameraModel) {
        cameraModelDAO.addCameraModel(cameraModel);
    }

    @Override
    public boolean isCameraModelExist(CameraModel cameraModel) {
        return cameraModelDAO.isCameraModelExist(cameraModel);
    }

    @Override
    public CameraModel getCameraModel(String modelName, String maker) {
        return cameraModelDAO.getCameraModel(modelName, maker);
    }

    @Override
    public List<CameraModel> listCameraModelByMaker(String maker) {
        return cameraModelDAO.listCameraModelByMaker(maker);
    }

    @Override
    public List<CameraModel> listCameraModel() {
        return cameraModelDAO.listCameraModel();
    }

    @Override
    public void removeCameraModel(Integer id) {
        cameraModelDAO.removeCameraModel(id);
    }

    @Override
    public void updateCameraModel(CameraModel cameraModel) {
        cameraModelDAO.updateCameraModel(cameraModel);
    }

    @Override
    public List<String> getCameraModelNames(String makerName) {
        return cameraModelDAO.getCameraModelNames(makerName);
    }

    @Override
    public List<String> getCameraMakerNames() {
        return cameraModelDAO.getCameraMakerNames();
    }

    @Override
    public void addExifFeatures(CameraModel cameraModel, List<CameraModelExifFeature> modelFeatures) {
        cameraModelDAO.addExifFeatures(cameraModel, modelFeatures);
    }

    @Override
    public void updateExifFeatures(List<CameraModelExifFeature> modelFeatures) {
        cameraModelDAO.updateExifFeatures(modelFeatures);
    }

    @Override
    public void deleteExifFeatures(CameraModel cameraModel) {
        cameraModelDAO.deleteExifFeatures(cameraModel);
    }

    @Override
    public List<Camera> getCameras(CameraModel cameraModel) {
        return cameraModelDAO.getCameras(cameraModel);
    }

}
