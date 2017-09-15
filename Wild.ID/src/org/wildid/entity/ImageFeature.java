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
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ImageFeature generated by hbm2java
 */
@Entity
@Table(name = "image_feature", catalog = "wild_ID"
)
public class ImageFeature implements java.io.Serializable {

    private Integer imageFeatureId;
    private String name;
    private String defaultExifTagName;

    public ImageFeature() {
    }

    public ImageFeature(String name, String defaultExifTagName) {
        this.name = name;
        this.defaultExifTagName = defaultExifTagName;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "image_feature_id", unique = true, nullable = false)
    public Integer getImageFeatureId() {
        return this.imageFeatureId;
    }

    public void setImageFeatureId(Integer imageFeatureId) {
        this.imageFeatureId = imageFeatureId;
    }

    @Column(name = "name", nullable = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "default_exif_tag_name", nullable = false, length = 64)
    public String getDefaultExifTagName() {
        return this.defaultExifTagName;
    }

    public void setDefaultExifTagName(String defaultExifTagName) {
        this.defaultExifTagName = defaultExifTagName;
    }

}