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
public interface ImageService {

    public void addImage(Image image);

    public Image getImage(Integer id);

    public void removeImage(Integer id);

    public void updateImage(Image image);

    public void saveAnnotation(Image image);

    public void removeImageSpecies(Image image);

    public void removeImagesFromSequence(List<Image> images, ImageSequence sequence);

    public List<Object> getImageMetadataExport(String sql);

    public int getImageMetadataExportCount(String sql);

    public int getImageCount(Project[] selectedProjects, Date startDate, Date endDate, ImageType imageType, String genus, String species);

    public TreeMap<Project, TreeMap<Event, TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>>>> searchImages(Project[] selectedProjects, Date startDate, Date endDate, ImageType imageType, String genus, String species);

    public List<Integer> getOrderedSequences(Deployment deployment);

    public Image getFirstImageInSequence(Integer image_sequence_id);

    public List<Image> getImagesIdentifiedBy(Person person);
       
    public void addImageIndividual(ImageIndividual ind);

    public ImageIndividual getImageIndividual(Image img, int x, int y);

    public void updateImageIndividual(ImageIndividual ind);

    public void removeImageIndividual(ImageIndividual ind);
       
}
