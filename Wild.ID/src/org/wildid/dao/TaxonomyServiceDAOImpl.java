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
import org.hibernate.Query;
import org.hibernate.Session;
import org.wildid.entity.FamilyGenusSpecies;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.HomoSapiensType;
import org.wildid.entity.ImageHomoSapiensType;
import org.wildid.entity.TaxaCommonEnglishName;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class TaxonomyServiceDAOImpl implements TaxonomyServiceDAO {

    @Override
    public List<String> getGenusSuggestion(String string) {

        List<String> list;
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Query query = s.createQuery("SELECT DISTINCT fgs.genus FROM FamilyGenusSpecies as fgs "
                + "WHERE lower(fgs.genus) like :genus order by fgs.genus");
        query.setParameter("genus", string.toLowerCase() + "%");
        list = query.list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public List<String> getSpeciesSuggestion(String genus, String string) {

        List<String> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Query query = s.createQuery("SELECT DISTINCT fgs.species FROM FamilyGenusSpecies as fgs "
                + "WHERE lower(fgs.genus) = :genus "
                + "  AND lower(fgs.species) like :species  order by fgs.species");
        query.setParameter("genus", genus.toLowerCase());
        query.setParameter("species", string.toLowerCase() + "%");
        list = query.list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public List<String> getCommonNameSuggestion(String string) {

        List<String> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Query query = s.createQuery("SELECT DISTINCT tcen.name FROM TaxaCommonEnglishName as tcen "
                + "WHERE lower(tcen.name) like :commonname  order by tcen.name");
        query.setParameter("commonname", string.toLowerCase() + "%");
        list = query.list();
        s.getTransaction().commit();
        s.close();
        return list;

    }

    @Override
    public FamilyGenusSpecies getFamilyGenusSpecies(String genus, String species) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<FamilyGenusSpecies> list = s.createQuery("FROM FamilyGenusSpecies as fgs "
                + "WHERE lower(fgs.genus) = '" + genus.toLowerCase() + "' "
                + "  AND lower(fgs.species) = '" + species.toLowerCase() + "'").list();
        s.getTransaction().commit();
        s.close();

        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public void addHomoSapiensType(HomoSapiensType homoSapiensType) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.save(homoSapiensType);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<HomoSapiensType> listHomoSapiensType() {
        List<HomoSapiensType> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("FROM HomoSapiensType AS homoSapiensType ORDER BY homoSapiensType.homoSapiensTypeId").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    @Override
    public void removeHomoSapiensType(Integer id) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        ImageHomoSapiensType c = (ImageHomoSapiensType) s.load(ImageHomoSapiensType.class, id);
        s.delete(c);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public void updateHomoSapiensType(HomoSapiensType homoSapiensType) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        s.update(homoSapiensType);
        s.getTransaction().commit();
        s.close();
    }

    @Override
    public List<TaxaCommonEnglishName> getCommonEnglishNames(FamilyGenusSpecies fgs) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        List<TaxaCommonEnglishName> list = s.createQuery("FROM TaxaCommonEnglishName as cname "
                + "WHERE cname.species=" + fgs.getFamilyGenusSpeciesId()).list();
        s.getTransaction().commit();
        s.close();

        return list;

    }

    @Override
    public TaxaCommonEnglishName getCommonEnglishNames(String commonName) {

        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();

        Query query = s.createQuery("FROM TaxaCommonEnglishName as cname "
                + "WHERE lower(cname.name)= :cname ");
        query.setParameter("cname", commonName.toLowerCase());

        List<TaxaCommonEnglishName> list = query.list();

        TaxaCommonEnglishName cn = null;

        if (!list.isEmpty()) {
            cn = list.get(0);
            Hibernate.initialize(cn.getSpecies());
            s.getTransaction().commit();
        }

        s.close();
        return cn;
    }

    @Override
    public List<String> loadUsedGenus() {

        List<String> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("SELECT DISTINCT is.familyGenusSpecies.genus FROM ImageSpecies as is ").list();
        s.getTransaction().commit();
        s.close();
        return list;

    }

    @Override
    public List<String> loadUsedSpecies(String genus) {

        List<String> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("SELECT DISTINCT is.familyGenusSpecies.species FROM ImageSpecies as is WHERE is.familyGenusSpecies.genus='" + genus + "'").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

    public List<String> loadUsedCommonNames() {

        List<String> list = new ArrayList<>();
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        list = s.createQuery("SELECT DISTINCT is.engishCommonName.name FROM ImageSpecies as is").list();
        s.getTransaction().commit();
        s.close();
        return list;
    }

}
