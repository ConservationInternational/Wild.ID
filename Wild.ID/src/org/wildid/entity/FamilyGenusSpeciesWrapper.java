package org.wildid.entity;
// Generated Sep 16, 2015 3:35:06 PM by Hibernate Tools 4.3.1

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class FamilyGenusSpeciesWrapper {

    private Integer familyGenusSpeciesId;
    private String class_;
    private String order;
    private String family;
    private String genus;
    private String species;
    private String iucnSpeciesId;

    public FamilyGenusSpeciesWrapper() {
    }

    public FamilyGenusSpeciesWrapper(
            Integer familyGenusSpeciesId,
            String class_,
            String order,
            String family,
            String genus,
            String species,
            String iucnSpeciesId) {
        this.familyGenusSpeciesId = familyGenusSpeciesId;
        this.class_ = class_;
        this.order = order;
        this.family = family;
        this.genus = genus;
        this.species = species;
        this.iucnSpeciesId = iucnSpeciesId;
    }

    public Integer getFamilyGenusSpeciesId() {
        return this.familyGenusSpeciesId;
    }

    public void setFamilyGenusSpeciesId(Integer familyGenusSpeciesId) {
        this.familyGenusSpeciesId = familyGenusSpeciesId;
    }

    public String getClass_() {
        return this.class_;
    }

    public void setClass_(String class_) {
        this.class_ = class_;
    }

    public String getOrder() {
        return this.order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getFamily() {
        return this.family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getGenus() {
        return this.genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return this.species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getIucnSpeciesId() {
        return this.iucnSpeciesId;
    }

    public void setIucnSpeciesId(String iucnSpeciesId) {
        this.iucnSpeciesId = iucnSpeciesId;
    }

}
