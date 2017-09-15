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
package org.wildid.app;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;
import org.wildid.entity.FamilyGenusSpecies;
import org.wildid.entity.ImageSpecies;

public class ImageSpeciesComboBox extends ComboBox {

    public ImageSpeciesComboBox(List<ImageSpecies> imageSpecieses) {

        ObservableList<ImageSpecies> imageSpeciesList = FXCollections.observableList(imageSpecieses);
        this.setItems(imageSpeciesList);

        if (!imageSpecieses.isEmpty()) {
            this.setValue(imageSpeciesList.get(0));
        }

        this.setCellFactory((comboBox) -> {
            return new ListCell<ImageSpecies>() {
                @Override
                protected void updateItem(ImageSpecies item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        FamilyGenusSpecies fgs = item.getFamilyGenusSpecies();
                        setText(fgs.getGenus() + " " + fgs.getSpecies());                       
                    }
                }
            };
        });

        this.setConverter(new StringConverter<ImageSpecies>() {
            @Override
            public String toString(ImageSpecies imageSpecies) {
                if (imageSpecies == null) {
                    return null;
                } else {
                    FamilyGenusSpecies fgs = imageSpecies.getFamilyGenusSpecies();                   
                    return fgs.getGenus() + " " + fgs.getSpecies();
                }
            }

            @Override
            public ImageSpecies fromString(String ageString) {
                return null; // No conversion fromString needed.
            }
        });
    }

}
