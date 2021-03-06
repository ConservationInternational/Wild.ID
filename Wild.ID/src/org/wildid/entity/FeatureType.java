package org.wildid.entity;
// Generated Sep 16, 2015 3:35:06 PM by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * FeatureType generated by hbm2java
 */
@Entity
@Table(name = "feature_type", catalog = "wild_ID"
)
public class FeatureType implements java.io.Serializable {

    private Integer featureTypeId;
    private String name;
    private String methodlogy;
    //private Set deployments = new HashSet(0);

    public FeatureType() {
    }

    public FeatureType(String name) {
        this.name = name;
    }

    public FeatureType(String name, String methodlogy) {
        this.name = name;
        this.methodlogy = methodlogy;
        //this.deployments = deployments;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "feature_type_id", unique = true, nullable = false)
    public Integer getFeatureTypeId() {
        return this.featureTypeId;
    }

    public void setFeatureTypeId(Integer featureTypeId) {
        this.featureTypeId = featureTypeId;
    }

    @Column(name = "name", nullable = false, length = 128)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "methodlogy", length = 65535)
    public String getMethodlogy() {
        return this.methodlogy;
    }

    public void setMethodlogy(String methodlogy) {
        this.methodlogy = methodlogy;
    }

    /*
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "featureType")
    public Set getDeployments() {
        return this.deployments;
    }

    public void setDeployments(Set deployments) {
        this.deployments = deployments;
    }
     */
}
