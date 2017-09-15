package org.wildid.entity;
// Generated Sep 16, 2015 3:35:06 PM by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement
@Entity
@Table(name = "image_individual", catalog = "wild_ID"
)
public class ImageIndividual implements java.io.Serializable {

    private Integer imageIndividualId;
    private ImageSpecies imageSpecies;
    private String name;
    private String sex;
    private Age age;
    private String note;
    private Integer x;
    private Integer y;

    public ImageIndividual() {
    }

    public ImageIndividual(ImageSpecies imageSpecies) {
        this.imageSpecies = imageSpecies;
    }

    public ImageIndividual(ImageSpecies imageSpecies, String name, String sex, Age age, String note) {
        this.imageSpecies = imageSpecies;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.note = note;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "image_individual_id", unique = true, nullable = false)
    public Integer getImageIndividualId() {
        return this.imageIndividualId;
    }

    public void setImageIndividualId(Integer imageIndividualId) {
        this.imageIndividualId = imageIndividualId;
    }

    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_species_id", nullable = false)
    public ImageSpecies getImageSpecies() {
        return this.imageSpecies;
    }

    public void setImageSpecies(ImageSpecies imageSpecies) {
        this.imageSpecies = imageSpecies;
    }

    @Column(name = "name", length = 32)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "sex", length = 8)
    public String getSex() {
        return this.sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "age_id", nullable = true)
    public Age getAge() {
        return this.age;
    }

    public void setAge(Age age) {
        this.age = age;
    }

    @Column(name = "note", length = 65535)
    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Column(name = "x")
    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    @Column(name = "y")
    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
