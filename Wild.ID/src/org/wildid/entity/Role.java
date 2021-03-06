package org.wildid.entity;
// Generated Sep 16, 2015 3:35:06 PM by Hibernate Tools 4.3.1

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Role generated by hbm2java
 */
@Entity
@Table(name = "role", catalog = "wild_ID"
)
public class Role implements java.io.Serializable {

    private Integer roleId;
    private String name;
    //private Set projectPersonRoles = new HashSet(0);

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Role(String name, Set projectPersonRoles) {
        this.name = name;
        //this.projectPersonRoles = projectPersonRoles;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "role_id", unique = true, nullable = false)
    public Integer getRoleId() {
        return this.roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    @Column(name = "name", nullable = false, length = 128)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "role")
     public Set getProjectPersonRoles() {
     return this.projectPersonRoles;
     }

     public void setProjectPersonRoles(Set projectPersonRoles) {
     this.projectPersonRoles = projectPersonRoles;
     }
     */
}
