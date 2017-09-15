/*
Copyright (c) 2007 The Regents of the University of California

Permission to use, copy, modify, and distribute this software and its documentation
for educational, research and non-profit purposes, without fee, and without a written
agreement is hereby granted, provided that the above copyright notice, this
paragraph and the following three paragraphs appear in all copies.

Permission to make commercial use of this software may be obtained
by contacting:
Technology Transfer Office
9500 Gilman Drive, Mail Code 0910
University of California
La Jolla, CA 92093-0910
(858) 534-5815
invent@ucsd.edu

THIS SOFTWARE IS PROVIDED BY THE REGENTS OF THE UNIVERSITY OF CALIFORNIA AND
CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
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

@Entity
@Table(name = "preference", catalog = "wild_ID"
)
public class Preference implements java.io.Serializable {

    private Integer preferenceId;
    private Person defaultAnnotationPerson;
    private String language;
    private String style;
    private Integer timeGroupInterval;
    private String speciesNaming;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "preference_id", unique = true, nullable = false)
    public Integer getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(Integer preferenceId) {
        this.preferenceId = preferenceId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "annotation_default_person_id", nullable = true)
    public Person getDefaultAnnotationPerson() {
        return defaultAnnotationPerson;
    }

    public void setDefaultAnnotationPerson(Person defaultAnnotationPerson) {
        this.defaultAnnotationPerson = defaultAnnotationPerson;
    }

    @Column(name = "language", nullable = false)
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Column(name = "style", nullable = false)
    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Column(name = "annotation_second_group_interval", nullable = false)
    public Integer getTimeGroupInterval() {
        return timeGroupInterval;
    }

    public void setTimeGroupInterval(Integer timeGroupInterval) {
        this.timeGroupInterval = timeGroupInterval;
    }

    @Column(name = "annotation_species_naming", nullable = false)
    public String getSpeciesNaming() {
        return speciesNaming;
    }

    public void setSpeciesNaming(String speciesNaming) {
        this.speciesNaming = speciesNaming;
    }
}
