package org.wildid.entity;
// Generated Sep 16, 2015 3:35:06 PM by Hibernate Tools 4.3.1

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
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Camera generated by hbm2java
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
@Table(name = "camera", catalog = "wild_ID"
)
public class Camera implements java.io.Serializable {

    private Integer cameraId;
    private CameraModel cameraModel;
    private String serialNumber;
    private Integer yearPurchased;
    //private int projectId;

    @XmlTransient
    private Project project;

    @XmlTransient
    private Set deployments = new HashSet(0);

    public Camera() {
    }

    public Camera(CameraModel cameraModel, Project project) {
        this.cameraModel = cameraModel;
        this.project = project;
    }

    public Camera(CameraModel cameraModel, String serialNumber, Integer yearPurchased, Project project, Set deployments) {
        this.cameraModel = cameraModel;
        this.serialNumber = serialNumber;
        this.yearPurchased = yearPurchased;
        this.project = project;
        this.deployments = deployments;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "camera_id", unique = true, nullable = false)
    public Integer getCameraId() {
        return this.cameraId;
    }

    public void setCameraId(Integer cameraId) {
        this.cameraId = cameraId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "camera_model_id", nullable = false)
    public CameraModel getCameraModel() {
        return this.cameraModel;
    }

    public void setCameraModel(CameraModel cameraModel) {
        this.cameraModel = cameraModel;
    }

    @Column(name = "serial_number", length = 128)
    public String getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Column(name = "year_purchased")
    public Integer getYearPurchased() {
        return this.yearPurchased;
    }

    public void setYearPurchased(Integer yearPurchased) {
        this.yearPurchased = yearPurchased;
    }

    @JoinColumn(name = "project_id", nullable = false)
    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "camera")
    public Set getDeployments() {
        return this.deployments;
    }

    public void setDeployments(Set deployments) {
        this.deployments = deployments;
    }

}
