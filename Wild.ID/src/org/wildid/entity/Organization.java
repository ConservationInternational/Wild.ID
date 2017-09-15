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
 * Organization generated by hbm2java
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
@Table(name = "organization", catalog = "wild_ID"
)
public class Organization implements java.io.Serializable, Comparable {

    private Integer organizationId;
    private Country country;
    private String name;
    private String address;
    private String address2;
    private String city;
    private String state;
    private String postalCode;
    private String phone;
    private String email;

    @XmlTransient
    private Set<ProjectOrganization> projectOrganizations = new HashSet(0);

    @XmlTransient
    private Set persons = new HashSet(0);

    public Organization() {
    }

    public Organization(String name) {
        this.name = name;
    }

    public Organization(Country country, String name, String address, String address2, String city, String state, String postalCode, String phone, String email, Set projectOrganizations, Set persons) {
        this.country = country;
        this.name = name;
        this.address = address;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.phone = phone;
        this.email = email;
        this.projectOrganizations = projectOrganizations;
        this.persons = persons;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "organization_id", unique = true, nullable = false)
    public Integer getOrganizationId() {
        return this.organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    public Country getCountry() {
        return this.country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    @Column(name = "name", nullable = false, length = 200)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "address", length = 200)
    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "address2")
    public String getAddress2() {
        return this.address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    @Column(name = "city", length = 50)
    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Column(name = "state", length = 50)
    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "postal_code", length = 16)
    public String getPostalCode() {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Column(name = "phone", length = 32)
    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Column(name = "email", length = 64)
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "organization")
    public Set<ProjectOrganization> getProjectOrganizations() {
        return this.projectOrganizations;
    }

    public void setProjectOrganizations(Set<ProjectOrganization> projectOrganizations) {
        this.projectOrganizations = projectOrganizations;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "organization")
    public Set getPersons() {
        return this.persons;
    }

    public void setPersons(Set persons) {
        this.persons = persons;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Organization) {
            Organization p = (Organization) o;
            String thisName = this.name;
            String thatName = p.getName();
            return thisName.compareTo(thatName);
        } else {
            return 0;
        }
    }

}