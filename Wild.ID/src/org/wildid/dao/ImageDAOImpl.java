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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.CameraTrapArrayComparator;
import org.wildid.entity.CameraTrapComparator;
import org.wildid.entity.Deployment;
import org.wildid.entity.Event;
import org.wildid.entity.EventComparator;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.Image;
import org.wildid.entity.ImageComparator;
import org.wildid.entity.ImageExif;
import org.wildid.entity.ImageIndividual;
import org.wildid.entity.ImageSequence;
import org.wildid.entity.ImageSpecies;
import org.wildid.entity.ImageType;
import org.wildid.entity.Person;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectComparator;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ImageDAOImpl implements ImageDAO {

    @Override
    public void addImage(Image image) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(image);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public Image getImage(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<Image> list = s.createQuery("FROM Image AS image WHERE image.imageId=" + id).list();
        for (Image image : list) {
            for (ImageExif exif : image.getImageExifs()) {
                Hibernate.initialize(exif);
            }
            Hibernate.initialize(image.getImageSequence());
            Hibernate.initialize(image.getImageSequence().getDeployment());
            Hibernate.initialize(image.getImageSequence().getDeployment().getEvent());
            Hibernate.initialize(image.getImageSequence().getDeployment().getCameraTrap());

            for (Image img : image.getImageSequence().getImages()) {
                Hibernate.initialize(img);
                Hibernate.initialize(img.getImageSpecieses());
            }

            for (Object object : image.getImageSpecieses()) {
                ImageSpecies is = (ImageSpecies) object;
                Hibernate.initialize(is);
            }

            Hibernate.initialize(image.getImageSequence().getDeployment());
            Hibernate.initialize(image.getImageSequence().getDeployment().getEvent());
            Hibernate.initialize(image.getImageSequence().getDeployment().getCameraTrap());
        }
        s.getTransaction().commit();
        s.close();
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public void removeImage(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Image c = (Image) s.load(Image.class, id);
        s.delete(c);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void updateImage(Image image) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(image);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void saveAnnotation(Image image) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(image);
        for (Object object : image.getImageSpecieses()) {
            ImageSpecies imageSpecies = (ImageSpecies) object;
            s.save(imageSpecies);
            for (Object obj : imageSpecies.getImageIndividuals()) {
                ImageIndividual ind = (ImageIndividual) obj;
                s.save(ind);
            }
        }
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void removeImageSpecies(Image image) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        for (Object object : image.getImageSpecieses()) {
            ImageSpecies imageSpecies = (ImageSpecies) object;
            for (Object obj : imageSpecies.getImageIndividuals()) {
                ImageIndividual ind = (ImageIndividual)obj;
                s.delete(ind);
            }
            s.delete(imageSpecies);
        }
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void removeImagesFromSequence(List<Image> images, ImageSequence old_sequence) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();

        Deployment deployment = old_sequence.getDeployment();
        ImageSequence new_sequence = new ImageSequence();
        new_sequence.setDeployment(deployment);

        for (Image image : images) {
            old_sequence.getImages().remove(image);
            new_sequence.getImages().add(image);
            image.setImageSequence(new_sequence);

            s.update(image);
        }
        s.update(old_sequence);
        s.save(new_sequence);

        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<Object> getImageMetadataExport(String sql) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();

        List<Object> resultList = s.createSQLQuery(sql).list();

        s.getTransaction().commit();
        s.close();

        return resultList;
    }

    @Override
    public int getImageMetadataExportCount(String sql) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();

        List<Object> resultList = s.createSQLQuery(sql).list();

        s.getTransaction().commit();
        s.close();

        java.math.BigInteger bigI = (java.math.BigInteger) resultList.get(0);
        return bigI.intValue();
    }

    @Override
    public int getImageCount(Project[] selectedProjects, Date startDate, Date endDate, ImageType imageType, String genus, String species) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        String query = getImageCountQuery(selectedProjects, startDate, endDate, imageType, genus, species);
        List<Object> resultList = s.createQuery(query).list();
        s.getTransaction().commit();
        s.close();
        Long count = (Long) resultList.get(0);
        return count.intValue();
    }

    private String getImageCountQuery(Project[] selectedProjects, Date startDate, Date endDate, ImageType imageType, String genus, String species) {
        String query = "SELECT count(DISTINCT img.imageId) ";
        return query + getQueryBase(selectedProjects, startDate, endDate, imageType, genus, species);
    }

    private String getQueryBase(Project[] selectedProjects, Date startDate, Date endDate, ImageType imageType, String genus, String species) {
        String from = "FROM Image as img ";
        String condition = "WHERE ";

        String projectPart = "";
        if (selectedProjects.length > 0) {
            projectPart = "(";
            for (int i = 0; i < selectedProjects.length; i++) {
                if (i > 0) {
                    projectPart += " OR";
                }
                projectPart += " img.imageSequence.deployment.event.project.projectId = " + selectedProjects[i].getProjectId();
            }
            projectPart += ")";
        }

        // process start date
        String startDatePart = "";
        if (startDate != null) {
            if (projectPart.length() > 0) {
                startDatePart = " AND ";
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startDatePart += " img.timeCaptured >= '" + formatter.format(startDate) + "'";
        }

        // process end date
        String endDatePart = "";
        if (endDate != null) {
            if (projectPart.length() > 0 || startDatePart.length() > 0) {
                endDatePart = " AND ";
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            endDatePart += " img.timeCaptured <= '" + formatter.format(endDate) + "'";
        }

        // process image type
        String imgTypePart = "";
        if (imageType != null) {
            if (projectPart.length() > 0 || startDatePart.length() > 0 || endDatePart.length() > 0) {
                imgTypePart = " AND ";
            }
            imgTypePart += " img.imageType.name = '" + imageType.getName() + "'";
        }

        // process genus species
        String genusPart = "";
        String speciesPart = "";
        if (imageType != null && imageType.getName().equals("Animal")) {
            if (genus != null) {
                if (projectPart.length() > 0 || startDatePart.length() > 0 || endDatePart.length() > 0 || imgTypePart.length() > 0) {
                    genusPart = " AND ";
                }

                from += " INNER JOIN img.imageSpecieses as is ";
                genusPart += "is.familyGenusSpecies.genus='" + genus + "'";
            }

            if (species != null) {
                if (projectPart.length() > 0 || startDatePart.length() > 0 || endDatePart.length() > 0 || imgTypePart.length() > 0 || genusPart.length() > 0) {
                    speciesPart = " AND ";
                }

                if (genus == null) {
                    from += " INNER JOIN img.imageSpecieses as is ";
                }

                speciesPart += "is.familyGenusSpecies.species='" + species + "'";
            }
        }

        return from + condition + projectPart + startDatePart + endDatePart + imgTypePart + genusPart + speciesPart;
    }

    @Override
    public TreeMap<Project, TreeMap<Event, TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>>>> searchImages(Project[] selectedProjects, Date startDate, Date endDate, ImageType imageType, String genus, String species) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        String query = getSearchImageQuery(selectedProjects, startDate, endDate, imageType, genus, species);
        List<Image> images = s.createQuery(query).list();
        for (Image image : images) {
            for (ImageExif exif : image.getImageExifs()) {
                Hibernate.initialize(exif);
            }
            Hibernate.initialize(image.getImageSequence());
            Hibernate.initialize(image.getImageSequence().getDeployment());
            Hibernate.initialize(image.getImageSequence().getDeployment().getEvent());
            Hibernate.initialize(image.getImageSequence().getDeployment().getCameraTrap());

            for (Image img : image.getImageSequence().getImages()) {
                Hibernate.initialize(img);
                Hibernate.initialize(img.getImageSpecieses());
            }

            for (Object object : image.getImageSpecieses()) {
                ImageSpecies is = (ImageSpecies) object;
                Hibernate.initialize(is);
            }

            Hibernate.initialize(image.getImageSequence().getDeployment());
            Hibernate.initialize(image.getImageSequence().getDeployment().getEvent());
            Hibernate.initialize(image.getImageSequence().getDeployment().getCameraTrap());
            Hibernate.initialize(image.getImageSequence().getDeployment().getCameraTrap().getCameraTrapArray());
        }
        s.getTransaction().commit();
        s.close();

        TreeMap<Project, TreeMap<Event, TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>>>> projectMap
                = new TreeMap<>(new ProjectComparator());
        for (Image image : images) {
            Project project = image.getImageSequence().getDeployment().getEvent().getProject();
            Event event = image.getImageSequence().getDeployment().getEvent();
            CameraTrapArray array = image.getImageSequence().getDeployment().getCameraTrap().getCameraTrapArray();
            CameraTrap trap = image.getImageSequence().getDeployment().getCameraTrap();

            TreeMap<Event, TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>>> eventMap
                    = new TreeMap<>(new EventComparator());
            boolean foundProject = false;
            for (Project proj : projectMap.keySet()) {
                if (proj.getProjectId().intValue() == project.getProjectId().intValue()) {
                    project = proj;
                    eventMap = projectMap.get(proj);
                    foundProject = true;
                    break;
                }
            }

            TreeMap<CameraTrapArray, TreeMap<CameraTrap, TreeSet<Image>>> arrayMap
                    = new TreeMap<>(new CameraTrapArrayComparator());
            boolean foundEvent = false;
            for (Event evt : eventMap.keySet()) {
                if (evt.getEventId().intValue() == event.getEventId().intValue()) {
                    event = evt;
                    arrayMap = eventMap.get(evt);
                    foundEvent = true;
                    break;
                }
            }

            TreeMap<CameraTrap, TreeSet<Image>> trapMap = new TreeMap<>(new CameraTrapComparator());
            boolean foundArray = false;
            for (CameraTrapArray arr : arrayMap.keySet()) {
                if (arr.getCameraTrapArrayId().intValue() == array.getCameraTrapArrayId().intValue()) {
                    array = arr;
                    trapMap = arrayMap.get(arr);
                    foundArray = true;
                    break;
                }
            }

            TreeSet imgs = new TreeSet<>(new ImageComparator());
            for (CameraTrap trp : trapMap.keySet()) {
                if (trp.getCameraTrapId().intValue() == trap.getCameraTrapId().intValue()) {
                    trap = trp;
                    imgs = trapMap.get(trp);
                    break;
                }
            }

            imgs.add(image);
            trapMap.put(trap, imgs);
            arrayMap.put(array, trapMap);
            eventMap.put(event, arrayMap);
            projectMap.put(project, eventMap);
        }

        return projectMap;

    }

    private String getSearchImageQuery(Project[] selectedProjects, Date startDate, Date endDate, ImageType imageType, String genus, String species) {
        String query = "SELECT img ";
        query += getQueryBase(selectedProjects, startDate, endDate, imageType, genus, species);
        return query;
    }

    @Override
    public List<Integer> getOrderedSequences(Deployment deployment) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        String query = "select s.image_sequence_id from image_sequence s, image i where deployment_id = " + deployment.getDeploymentId() + " and s.image_sequence_id = i.image_sequence_id group by s.image_sequence_id order by min(time_captured)";
        List<Integer> resultList = s.createSQLQuery(query).list();
        s.close();
        return resultList;
    }

    @Override
    public Image getFirstImageInSequence(Integer image_sequence_id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Image image = null;

        String query = "FROM Image AS img WHERE img.imageSequence.imageSequenceId = " + image_sequence_id + " ORDER BY img.timeCaptured ";
        List<Image> list = s.createQuery(query).list();
        if (!list.isEmpty()) {
            image = list.get(0);
            ImageSequence sequence = image.getImageSequence();
            Hibernate.initialize(sequence);
            Deployment deployment = sequence.getDeployment();
            Hibernate.initialize(deployment);
        }
        s.close();

        return image;
    }

    @Override
    public List<Image> getImagesIdentifiedBy(Person person) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        String query = "FROM Image AS img WHERE img.person.personId = " + person.getPersonId();
        List<Image> images = s.createQuery(query).list();

        query = "SELECT DISTINCT img FROM Image AS img INNER JOIN img.imageSpecieses as is "
                + "WHERE is.person = " + person.getPersonId();
        List<Image> list = s.createQuery(query).list();

        // merge two lists
        List<Image> newImages = new ArrayList<>();
        for (Image image : list) {
            boolean found = false;
            for (Image img : images) {
                if (img.getImageId().intValue() == image.getImageId().intValue()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newImages.add(image);
            }
        }
        images.addAll(newImages);

        s.close();
        return images;
    }

    @Override
    public void addImageIndividual(ImageIndividual ind) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(ind);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public ImageIndividual getImageIndividual(Image img, int x, int y) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<ImageIndividual> list = s.createQuery("FROM ImageIndividual AS ind "
                + " WHERE ind.imageSpecies.image.imageId=" + img.getImageId()
                + "   AND ind.x=" + x
                + "   AND ind.y=" + y).list();
        s.getTransaction().commit();
        s.close();
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public void updateImageIndividual(ImageIndividual ind) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(ind);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void removeImageIndividual(ImageIndividual ind) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        ind.getImageSpecies().getImageIndividuals().remove(ind);
        s.delete(ind);
        s.getTransaction().commit();
        s.close();
    }
}
