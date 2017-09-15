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
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.CameraTrapArrayComparator;

public class CameraTrapArrayComboBox extends ComboBox {

    public CameraTrapArrayComboBox(List<CameraTrapArray> arrays) {

        ObservableList<CameraTrapArray> arrayList = FXCollections.observableList(arrays);
        SortedList<CameraTrapArray> sortedTrapList = new SortedList<>(arrayList, new CameraTrapArrayComparator());
        this.setItems(sortedTrapList);

        if (arrays.size() == 1) {
            this.setValue(sortedTrapList.get(0));
        }

        this.setCellFactory((comboBox) -> {
            return new ListCell<CameraTrapArray>() {
                @Override
                protected void updateItem(CameraTrapArray item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            };
        });

        this.setConverter(new StringConverter<CameraTrapArray>() {
            @Override
            public String toString(CameraTrapArray array) {
                if (array == null) {
                    return null;
                } else {
                    return array.getName();
                }
            }

            @Override
            public CameraTrapArray fromString(String arrayString) {
                return null; // No conversion fromString needed.
            }
        });
    }

}
