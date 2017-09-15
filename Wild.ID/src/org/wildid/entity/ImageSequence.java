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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * ImageSequence generated by hbm2java
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
@Table(name = "image_sequence", catalog = "wild_ID"
)
public class ImageSequence implements java.io.Serializable {

    private Integer imageSequenceId;

    @XmlTransient
    private Deployment deployment;

    @XmlElementWrapper(name = "images")
    @XmlElement(name = "image")
    private Set<Image> images = new HashSet(0);

    public ImageSequence() {
    }

    public ImageSequence(Deployment deployment) {
        this.deployment = deployment;
    }

    public ImageSequence(Deployment deployment, Set images) {
        this.deployment = deployment;
        this.images = images;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "image_sequence_id", unique = true, nullable = false)
    public Integer getImageSequenceId() {
        return this.imageSequenceId;
    }

    public void setImageSequenceId(Integer imageSequenceId) {
        this.imageSequenceId = imageSequenceId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_id", nullable = false)
    public Deployment getDeployment() {
        return this.deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "imageSequence")
    public Set<Image> getImages() {
        return this.images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

}
