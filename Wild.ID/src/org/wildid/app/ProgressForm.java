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

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProgressForm {

    private final Stage dialogStage;

    private LanguageModel language;
    private final Label titleLabel = new Label();
    private final ProgressBar pb = new ProgressBar();
    private final Text infoLabel = new Text();
    private VBox vb;

    public ProgressForm(Pane pane, String stageTitle, String displayTitle) {

        this.dialogStage = new Stage();
        this.dialogStage.initStyle(StageStyle.UTILITY);
        this.dialogStage.setResizable(false);
        this.dialogStage.initModality(Modality.APPLICATION_MODAL);
        if (pane != null) {
            this.dialogStage.initOwner(pane.getScene().getWindow());
        }
        this.dialogStage.setTitle(stageTitle);

        // PROGRESS BAR
        this.titleLabel.setText(displayTitle);

        this.pb.setProgress(-1F);
        this.pb.setPrefWidth(400);
        this.infoLabel.setWrappingWidth(400);

        vb = new VBox(10);
        vb.setPadding(new Insets(30, 30, 30, 30));
        vb.setSpacing(10);
        //hb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(titleLabel, pb, infoLabel);

        Scene scene = new Scene(vb);
        dialogStage.setScene(scene);
    }

    public ProgressForm(String stageTitle, String displayTitle) {
        this(null, stageTitle, displayTitle);
    }

    public void activateProgressBar(final Task<?> task) {
        pb.progressProperty().bind(task.progressProperty());
        infoLabel.textProperty().bind(task.messageProperty());
        dialogStage.show();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void removeProgressBar() {
        vb.getChildren().remove(pb);
    }
}
