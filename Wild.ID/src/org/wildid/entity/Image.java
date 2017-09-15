package org.wildid.entity;
// Generated Sep 16, 2015 3:35:06 PM by Hibernate Tools 4.3.1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Image generated by hbm2java
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@XmlSeeAlso(ImageSpecies.class)
@Entity
@Table(name = "image", catalog = "wild_ID"
)
public class Image implements java.io.Serializable {

    private Integer imageId;

    @XmlTransient
    private ImageSequence imageSequence;

    private ImageType imageType;
    private Person person;
    private Date timeCaptured;
    private String rawName;
    private String systemName;
    private String note;
    private String restrictionsOnAccess;
    private String displayFlag;
    private String displayFlagReason;
    private String embargoPeriod;

    @XmlElementWrapper(name = "Exifs")
    @XmlElement(name = "Exif")
    private Set<ImageExif> imageExifs = new HashSet<>(0);

    @XmlTransient
    private Set imageExifExtends = new HashSet(0);

    //@XmlElementWrapper(name = "animals")
    @XmlElement(name = "animal")
    private Set imageSpecieses = new HashSet(0);

    /*
     @XmlTransient
     private Set imageHomoSapiensTypes = new HashSet(0);
     */
    public Image() {
    }

    public Image(ImageSequence imageSequence, ImageType imageType, Person person, Date timeCaptured, String rawName) {
        this.imageSequence = imageSequence;
        this.imageType = imageType;
        this.person = person;
        this.timeCaptured = timeCaptured;
        this.rawName = rawName;
    }

    public Image(ImageSequence imageSequence, ImageType imageType, Person person, Date timeCaptured, String rawName, String systemName, String note, String restrictionsOnAccess, String displayFlag, String displayFlagReason, String embargoPeriod, Integer iucnSpeciesId, Set imageExifs, Set imageExifExtends, Set imageSpecieses, Set imageHomoSapiensTypes) {
        this.imageSequence = imageSequence;
        this.imageType = imageType;
        this.person = person;
        this.timeCaptured = timeCaptured;
        this.rawName = rawName;
        this.systemName = systemName;
        this.note = note;
        this.restrictionsOnAccess = restrictionsOnAccess;
        this.displayFlag = displayFlag;
        this.displayFlagReason = displayFlagReason;
        this.embargoPeriod = embargoPeriod;
        //this.iucnSpeciesId = iucnSpeciesId;
        this.imageExifs = imageExifs;
        this.imageExifExtends = imageExifExtends;
        this.imageSpecieses = imageSpecieses;
        //this.imageHomoSapiensTypes = imageHomoSapiensTypes;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "image_id", unique = true, nullable = false)
    public Integer getImageId() {
        return this.imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_sequence_id", nullable = true)
    public ImageSequence getImageSequence() {
        return this.imageSequence;
    }

    public void setImageSequence(ImageSequence imageSequence) {
        this.imageSequence = imageSequence;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "image_type_id", nullable = true)
    public ImageType getImageType() {
        return this.imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "image_type_identify_person_id", nullable = true)
    public Person getPerson() {
        return this.person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_captured", nullable = false, length = 19)
    public Date getTimeCaptured() {
        return this.timeCaptured;
    }

    public void setTimeCaptured(Date timeCaptured) {
        this.timeCaptured = timeCaptured;
    }

    @Column(name = "raw_name", nullable = false, length = 128)
    public String getRawName() {
        return this.rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    @Column(name = "system_name", length = 128)
    public String getSystemName() {
        return this.systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    @Column(name = "note", length = 65535)
    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Column(name = "restrictions_on_access", length = 65535)
    public String getRestrictionsOnAccess() {
        return this.restrictionsOnAccess;
    }

    public void setRestrictionsOnAccess(String restrictionsOnAccess) {
        this.restrictionsOnAccess = restrictionsOnAccess;
    }

    @Column(name = "display_flag", length = 128)
    public String getDisplayFlag() {
        return this.displayFlag;
    }

    public void setDisplayFlag(String displayFlag) {
        this.displayFlag = displayFlag;
    }

    @Column(name = "display_flag_reason", length = 65535)
    public String getDisplayFlagReason() {
        return this.displayFlagReason;
    }

    public void setDisplayFlagReason(String displayFlagReason) {
        this.displayFlagReason = displayFlagReason;
    }

    @Column(name = "embargo_period", length = 128)
    public String getEmbargoPeriod() {
        return this.embargoPeriod;
    }

    public void setEmbargoPeriod(String embargoPeriod) {
        this.embargoPeriod = embargoPeriod;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "image")
    public Set<ImageExif> getImageExifs() {
        return this.imageExifs;
    }

    public void setImageExifs(Set<ImageExif> imageExifs) {
        this.imageExifs = imageExifs;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "image")
    public Set getImageExifExtends() {
        return this.imageExifExtends;
    }

    public void setImageExifExtends(Set imageExifExtends) {
        this.imageExifExtends = imageExifExtends;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "image")
    @OrderBy("imageSpeciesId")
    public Set getImageSpecieses() {
        return this.imageSpecieses;
    }

    public void setImageSpecieses(Set imageSpecieses) {
        this.imageSpecieses = imageSpecieses;
    }

    /*
     @OneToMany(fetch = FetchType.EAGER, mappedBy = "image")
     public Set getImageHomoSapiensTypes() {
     return this.imageHomoSapiensTypes;
     }

     public void setImageHomoSapiensTypes(Set imageHomoSapiensTypes) {
     this.imageHomoSapiensTypes = imageHomoSapiensTypes;
     }
     */
}