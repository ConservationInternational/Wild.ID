package org.wildid.entity;
// Generated Sep 16, 2015 3:35:06 PM by Hibernate Tools 4.3.1

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * ProjectPersonRole generated by hbm2java
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
@Table(name = "project_person_role", catalog = "wild_ID"
)
public class ProjectPersonRole implements java.io.Serializable, Comparable {

    private Integer projectPersonRoleId;
    private Person person;

    @XmlTransient
    private Project project;
    private Role role;
    private Date startTime;
    private Date endTime;

    public ProjectPersonRole() {
    }

    public ProjectPersonRole(Person person, Project project, Role role, Date startTime, Date endTime) {
        this.person = person;
        this.project = project;
        this.role = role;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "project_person_role_id", unique = true, nullable = false)
    public Integer getProjectPersonRoleId() {
        return this.projectPersonRoleId;
    }

    public void setProjectPersonRoleId(Integer projectPersonRoleId) {
        this.projectPersonRoleId = projectPersonRoleId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", nullable = false)
    public Person getPerson() {
        return this.person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time", length = 19)
    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time", length = 19)
    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public int compareTo(Object o) {
        int result = 0;
        if (o instanceof ProjectPersonRole) {
            ProjectPersonRole ppr = (ProjectPersonRole) o;
            result = this.getProject().getName().compareTo(ppr.getProject().getName());
            if (result == 0) {
                String thisName = this.getPerson().getLastName() + " " + this.getPerson().getFirstName();
                String thatName = ppr.getPerson().getLastName() + " " + ppr.getPerson().getFirstName();
                result = thisName.compareTo(thatName);
            }
        }

        return result;

    }

}
