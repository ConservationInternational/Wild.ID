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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * CameraTrapArray generated by hbm2java
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
@Table(name = "camera_trap_array", catalog = "wild_ID"
)
public class CameraTrapArray implements java.io.Serializable {

    @XmlElement
    private Integer cameraTrapArrayId;

    @XmlTransient
    private Project project;

    @XmlElement
    private String name;

    @XmlTransient
    private Set<CameraTrap> cameraTraps = new HashSet<>();

    public CameraTrapArray() {
    }

    public CameraTrapArray(Project project, String name) {
        this.project = project;
        this.name = name;
    }

    public CameraTrapArray(Project project, String name, Set<CameraTrap> cameraTraps) {
        this.project = project;
        this.name = name;
        this.cameraTraps = cameraTraps;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "camera_trap_array_id", unique = true, nullable = false)
    public Integer getCameraTrapArrayId() {
        return this.cameraTrapArrayId;
    }

    public void setCameraTrapArrayId(Integer cameraTrapArrayId) {
        this.cameraTrapArrayId = cameraTrapArrayId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Column(name = "name", nullable = false, length = 128)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cameraTrapArray")
    public Set<CameraTrap> getCameraTraps() {
        return this.cameraTraps;
    }

    public void setCameraTraps(Set<CameraTrap> cameraTraps) {
        this.cameraTraps = cameraTraps;
    }

}