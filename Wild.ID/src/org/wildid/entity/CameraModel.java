package org.wildid.entity;
// Generated Sep 16, 2015 3:35:06 PM by Hibernate Tools 4.3.1

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * CameraModel generated by hbm2java
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
@Table(name = "camera_model", catalog = "wild_ID"
)
public class CameraModel implements java.io.Serializable {

    private Integer cameraModelId;
    private String name;
    private String maker;

    @XmlTransient
    private Set cameras = new HashSet(0);

    @XmlTransient
    private Set cameraModelExtendTags = new HashSet(0);

    @XmlTransient
    private Set<CameraModelExifFeature> cameraModelExifFeatures = new TreeSet<>(new CameraModelExifFeatureComparator());

    public CameraModel() {
    }

    public CameraModel(String name) {
        this.name = name;
    }

    public CameraModel(String name, String maker) {
        this.name = name;
        this.maker = maker;
    }

    public CameraModel(String name, String maker, Set cameras, Set cameraModelExtendTags) {
        this.name = name;
        this.maker = maker;
        this.cameras = cameras;
        this.cameraModelExtendTags = cameraModelExtendTags;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "camera_model_id", unique = true, nullable = false)
    public Integer getCameraModelId() {
        return this.cameraModelId;
    }

    public void setCameraModelId(Integer cameraModelId) {
        this.cameraModelId = cameraModelId;
    }

    @Column(name = "name", nullable = false, length = 128)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "maker", length = 128)
    public String getMaker() {
        return this.maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cameraModel")
    public Set getCameras() {
        return this.cameras;
    }

    public void setCameras(Set cameras) {
        this.cameras = cameras;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cameraModel")
    public Set getCameraModelExtendTags() {
        return this.cameraModelExtendTags;
    }

    public void setCameraModelExtendTags(Set cameraModelExtendTags) {
        this.cameraModelExtendTags = cameraModelExtendTags;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cameraModel")
    public Set<CameraModelExifFeature> getCameraModelExifFeatures() {
        return cameraModelExifFeatures;
    }

    public void setCameraModelExifFeatures(Set<CameraModelExifFeature> cameraModelExifFeatures) {
        this.cameraModelExifFeatures = cameraModelExifFeatures;
    }

}