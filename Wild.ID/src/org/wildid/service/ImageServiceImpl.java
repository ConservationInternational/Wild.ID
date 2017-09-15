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

import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import org.wildid.dao.ImageDAO;
import org.wildid.dao.ImageDAOImpl;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.Image;
import org.wildid.entity.ImageIndividual;
import org.wildid.entity.ImageSequence;
import org.wildid.entity.ImageType;
import org.wildid.entity.Person;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ImageServiceImpl implements ImageService {

    private ImageDAO imageDAO = new ImageDAOImpl();

    @Override
    public void addImage(Image image) {
        imageDAO.addImage(image);
    }

    @Override
    public Image getImage(Integer id) {
        return imageDAO.getImage(id);
    }

    @Override
    public void removeImage(Integer id) {
        imageDAO.removeImage(id);
    }

    @Override
    public void updateImage(Image image) {
        imageDAO.updateImage(image);
    }

    @Override
    public void saveAnnotation(Image image) {
        imageDAO.saveAnnotation(image);
    }

    @Override
    public void removeImageSpecies(Image image) {
        imageDAO.removeImageSpecies(image);
    }

    @Override
    public void removeImagesFromSequence(List<Image> images, ImageSequence sequence) {
        imageDAO.removeImagesFromSequence(images, sequence);
    }

    @Override
    public List<Object> getImageMetadataExport(String sql) {
        return imageDAO.getImageMetadataExport(sql);
    }

    @Override
    public int getImageMetadataExportCount(String sql) {
        return imageDAO.getImageMetadataExportCount(sql);
    }

    @Override
    public int getImageCount(Project[] selectedProjects, Date startDate, Date endDate, ImageType imageType, String genus, String species) {
        return imageDAO.getImageCount(selectedProjects, startDate, endDate, imageType, genus, species);
    }

    @Override
    public TreeMap<Project, TreeMap<Event, TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>>>> searchImages(Project[] selectedProjects, Date startDate, Date endDate, ImageType imageType, String genus, String species) {
        return imageDAO.searchImages(selectedProjects, startDate, endDate, imageType, genus, species);
    }

    @Override
    public List<Integer> getOrderedSequences(Deployment deployment) {
        return imageDAO.getOrderedSequences(deployment);
    }

    @Override
    public Image getFirstImageInSequence(Integer image_sequence_id) {
        return imageDAO.getFirstImageInSequence(image_sequence_id);
    }

    @Override
    public List<Image> getImagesIdentifiedBy(Person person) {
        return imageDAO.getImagesIdentifiedBy(person);
    }

    @Override
    public void addImageIndividual(ImageIndividual ind) {
        imageDAO.addImageIndividual(ind);
    }

    @Override
    public ImageIndividual getImageIndividual(Image img, int x, int y) {
        return imageDAO.getImageIndividual(img, x, y);
    }

    @Override
    public void updateImageIndividual(ImageIndividual ind) {
        imageDAO.updateImageIndividual(ind);
    }

    @Override
    public void removeImageIndividual(ImageIndividual ind) {
        imageDAO.removeImageIndividual(ind);
    }
}
