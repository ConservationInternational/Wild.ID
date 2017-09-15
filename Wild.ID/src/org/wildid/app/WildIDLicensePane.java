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

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDLicensePane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    private final Label titleLabel;
    private final int width = 600;

    public WildIDLicensePane(LanguageModel language) {
        this.language = language;

        titleLabel = new Label(language.getString("menu_Wild.ID_license"));
        titleLabel.setStyle(TITLE_STYLE);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 10));
        titleBox.getChildren().addAll(this.titleLabel);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_CENTER);

        vbox.getChildren().addAll(titleBox, createForm());

        vbox.prefWidthProperty().bind(this.widthProperty());
        vbox.prefHeightProperty().bind(this.heightProperty());

        this.getChildren().add(vbox);
    }

    private VBox createForm() {
        VBox vbox = new VBox(20);
        vbox.setPrefWidth(width);
        vbox.setMinWidth(width);
        vbox.setPrefHeight(width);
        vbox.setMinHeight(width);

        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10));

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefHeight(600);

        Reader reader;
        try {
            reader = new InputStreamReader(WildID.class.getResourceAsStream("/resources/license.txt"), "UTF8");
            String newLine = System.getProperty("line.separator");

            Scanner s = new Scanner(reader);
            while (s.hasNext()) {
                if (s.hasNextInt()) {
                    textArea.appendText(s.nextInt() + " ");
                } else if (s.hasNextLine()) {
                    textArea.appendText(s.nextLine() + newLine);
                } else {
                    textArea.appendText(s.next() + " ");
                }
            }
            textArea.positionCaret(0);
            textArea.setEditable(false);

            vbox.getChildren().addAll(textArea);
        } catch (Exception ex) {
        }
        return vbox;
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        titleLabel.setText(language.getString("menu_Wild.ID_license"));
    }

    @Override
    public void setWildIDController(WildIDController controller) {
    }
}
