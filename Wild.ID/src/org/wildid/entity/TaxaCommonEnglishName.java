package org.wildid.entity;

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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
@Table(name = "common_name_eng", catalog = "wild_ID"
)
public class TaxaCommonEnglishName implements java.io.Serializable {

    private Integer taxaCommonEnglishNameId;

    @XmlTransient
    private FamilyGenusSpecies species;

    private String name;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "common_name_eng_id", unique = true, nullable = false)
    public Integer getTaxaCommonEnglishNameId() {
        return taxaCommonEnglishNameId;
    }

    public void setTaxaCommonEnglishNameId(Integer taxaCommonEnglishNameId) {
        this.taxaCommonEnglishNameId = taxaCommonEnglishNameId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "family_genus_species_id", nullable = false)
    public FamilyGenusSpecies getSpecies() {
        return species;
    }

    public void setSpecies(FamilyGenusSpecies species) {
        this.species = species;
    }

    @Column(name = "name", nullable = false, length = 64)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
