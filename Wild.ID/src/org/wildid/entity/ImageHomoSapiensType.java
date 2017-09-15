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

/**
 * ImageHomoSapiensType generated by hbm2java
 */
@Entity
@Table(name = "image_homo_sapiens_type", catalog = "wild_ID"
)
public class ImageHomoSapiensType implements java.io.Serializable {

    private Integer imageHomoSapiensTypeId;
    private HomoSapiensType homoSapiensType;
    private Image image;

    public ImageHomoSapiensType() {
    }

    public ImageHomoSapiensType(HomoSapiensType homoSapiensType, Image image) {
        this.homoSapiensType = homoSapiensType;
        this.image = image;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "image_homo_sapiens_type_id", unique = true, nullable = false)
    public Integer getImageHomoSapiensTypeId() {
        return this.imageHomoSapiensTypeId;
    }

    public void setImageHomoSapiensTypeId(Integer imageHomoSapiensTypeId) {
        this.imageHomoSapiensTypeId = imageHomoSapiensTypeId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homo_sapiens_type_id", nullable = false)
    public HomoSapiensType getHomoSapiensType() {
        return this.homoSapiensType;
    }

    public void setHomoSapiensType(HomoSapiensType homoSapiensType) {
        this.homoSapiensType = homoSapiensType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    public Image getImage() {
        return this.image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

}
