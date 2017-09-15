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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import static org.wildid.app.WildIDDataPane.TEXT_STYLE;
import static org.wildid.app.WildIDDataPane.TITLE_STYLE;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ContactUsPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    private WildIDController controller;
    private final int width = 700;
    private final Label titleLabel;

    private Label descLabel;
    private Label emailLabel;

    private final TextArea descTextArea = new TextArea();
    protected TextField emailTextField = new TextField();

    private Button submitButton;
    private Button cancelButton;
    protected HBox hbBtn = new HBox(10);

    public ContactUsPane(WildIDController controller, LanguageModel language) {
        this.language = language;
        this.controller = controller;
        this.setId("ContactUs");

        titleLabel = new Label(language.getString("menu_help_contactUs"));
        titleLabel.setStyle(TITLE_STYLE);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 10));
        titleBox.getChildren().addAll(this.titleLabel);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_CENTER);

        vbox.getChildren().addAll(titleBox, createForm());
        this.getChildren().add(vbox);

        vbox.setMinWidth(width);
        vbox.prefWidthProperty().bind(this.widthProperty());
    }

    private Pane createForm() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(30, 10, 10, 30));
        grid.setHgap(20);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        grid.setPrefWidth(width);
        grid.setMaxWidth(width);
        grid.setMinWidth(width);

        // email
        emailTextField.setMaxWidth(480);
        emailTextField.setPrefWidth(480);
        emailLabel = new Label(language.getString("error_report_user_email"));
        emailTextField.setStyle(REQUIRED_STYLE);
        grid.add(emailLabel, 1, 1);
        grid.add(emailTextField, 2, 1);

        // description
        descTextArea.setPrefRowCount(18);
        descTextArea.setMaxWidth(480);
        descTextArea.setPrefWidth(480);
        descLabel = new Label(language.getString("error_note_textarea_label"));
        GridPane.setValignment(descLabel, VPos.TOP);
        grid.add(descLabel, 1, 2);
        grid.add(descTextArea, 2, 2);

        submitButton = new Button(language.getString("submit_send_mail_button"));
        submitButton.setId("submit_send_mail_button");
        submitButton.setOnAction(controller);

        cancelButton = new Button(language.getString("alert_cancel"));
        cancelButton.setId("cancel_send_mail_button");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Scene scene = cancelButton.getScene();
                scene.getWindow().hide();
            }
        });

        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().addAll(submitButton, cancelButton);
        grid.add(hbBtn, 2, 3, 2, 1);

        return grid;

    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.controller = controller;
        this.submitButton.setOnAction(controller);
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        this.emailLabel.setText(language.getString("error_report_user_email"));
        this.descLabel.setText(language.getString("error_note_textarea_label"));
        this.submitButton.setText(language.getString("submit_send_mail_button"));
        this.cancelButton.setText(language.getString("alert_cancel"));
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    public boolean validate() {
        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        String email = emailTextField.getText();
        if (email == null || email.trim().equals("")) {
            title = language.getString("title_error");
            header = language.getString("empty_user_email_error_header");
            context = language.getString("empty_user_email_error_context");
            ok = false;
        } else if (!Util.validateEmailAddress(email)) {
            title = language.getString("title_error");
            header = language.getString("illegal_user_email_error_header");
            context = language.getString("illegal_user_email_error_context") + ": " + email + ".";
            ok = false;
        }

        if (ok) {
            String desc = descTextArea.getText();
            if (desc == null || desc.trim().equals("")) {
                title = language.getString("title_error");
                header = language.getString("empty_desc_report_error_header");
                context = language.getString("empty_desc_report_error_context");
                ok = false;
            }
        }

        if (!ok) {
            Util.alertErrorPopup(
                    title,
                    header,
                    context,
                    language.getString("alert_ok"));
        }

        return ok;
    }

    public String getEmailAddress() {
        return emailTextField.getText();
    }

    public String getDescription() {
        return descTextArea.getText();
    }

}
