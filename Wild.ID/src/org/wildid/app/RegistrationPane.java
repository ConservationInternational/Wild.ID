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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import static org.wildid.app.WildIDDataPane.TEXT_STYLE;
import static org.wildid.app.WildIDDataPane.TITLE_STYLE;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class RegistrationPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    private WildIDController controller;
    private final int width = 700;
    private final Label titleLabel;

    private Label firstNameLabel;
    private Label lastNameLabel;
    private Label organizationLabel;
    private Label emailLabel;
    private Label affiliationLabel;
    private Text general_text;
    private Text consent_text;

    protected TextField emailTextField = new TextField();
    protected TextField firstNameTextField = new TextField();
    protected TextField lastNameTextField = new TextField();
    protected TextField organizationTextField = new TextField();
    protected ComboBox affiliationComboBox = new ComboBox();

    private Button submitButton;
    private Button cancelButton;
    protected HBox hbBtn = new HBox(10);
    static Logger log = Logger.getLogger(RegistrationPane.class.getName());

    public RegistrationPane(WildIDController controller, LanguageModel language) {
        this.language = language;
        this.controller = controller;
        //this.getChildren().add(createForm());
        this.setId("Registration");

        titleLabel = new Label(language.getString("registration_title"));
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

        int row_count = 0;

        general_text = new Text(language.getString("registration_text_general"));
        general_text.setWrappingWidth(width - 100);

        grid.add(general_text, 1, row_count++, 2, 1);
        row_count++;

        // firstName
        firstNameTextField.setMaxWidth(500);
        firstNameLabel = new Label(language.getString("registration_firstName"));
        //firstNameTextField.setStyle(requiredStyle);
        grid.add(firstNameLabel, 1, ++row_count);
        grid.add(firstNameTextField, 2, row_count);

        // lastName
        lastNameTextField.setMaxWidth(500);
        lastNameLabel = new Label(language.getString("registration_lastName"));
        //lastNameTextField.setStyle(requiredStyle);
        grid.add(lastNameLabel, 1, ++row_count);
        grid.add(lastNameTextField, 2, row_count);

        // email
        emailTextField.setPrefWidth(500);
        emailTextField.setMaxWidth(500);
        emailLabel = new Label(language.getString("registration_email"));

        //emailTextField.setStyle(requiredStyle);
        grid.add(emailLabel, 1, ++row_count);
        grid.add(emailTextField, 2, row_count);

        // organization
        organizationTextField.setMaxWidth(500);
        organizationLabel = new Label(language.getString("registration_organization"));
        //organizationTextField.setStyle(requiredStyle);
        grid.add(organizationLabel, 1, ++row_count);
        grid.add(organizationTextField, 2, row_count);

        affiliationLabel = new Label(language.getString("registration_affiliation"));

        String[] affiliationIds = {
            language.getString("registration_type_Academia"),
            language.getString("registration_type_Commercial"),
            language.getString("registration_type_Government"),
            language.getString("registration_type_Military"),
            language.getString("registration_type_Non_Profit"),
            language.getString("registration_type_Independent"),
            language.getString("registration_type_Other")
        };

        affiliationComboBox.getItems().addAll((Object[]) affiliationIds);
        affiliationComboBox.setPrefWidth(200);
        grid.add(affiliationLabel, 1, ++row_count);
        grid.add(affiliationComboBox, 2, row_count++);

        consent_text = new Text(language.getString("registration_submit_consent"));
        consent_text.setWrappingWidth(width - 100);
        grid.add(consent_text, 1, ++row_count, 2, 1);

        // button
        submitButton = new Button(language.getString("registration_submit_button"));
        submitButton.setId("registration_submit_button");
        submitButton.setOnAction(controller);

        cancelButton = new Button(language.getString("registration_cancel_button"));
        cancelButton.setId("registration_cancel_button");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Scene scene = cancelButton.getScene();
                scene.getWindow().hide();
            }
        });

        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().addAll(submitButton, cancelButton);
        grid.add(hbBtn, 1, ++row_count, 2, 1);

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
        this.firstNameLabel.setText(language.getString("registration_firstName"));
        this.lastNameLabel.setText(language.getString("registration_lastName"));
        this.emailLabel.setText(language.getString("registration_email"));
        this.organizationLabel.setText(language.getString("registration_organization"));
        this.affiliationLabel.setText(language.getString("registration_affiliation"));
        this.submitButton.setText(language.getString("registration_submit_button"));
        this.cancelButton.setText(language.getString("registration_cancel_button"));
        this.consent_text.setText(language.getString("registration_submit_consent"));
        this.general_text.setText(language.getString("registration_text_general"));
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    public boolean validate() {
        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        if (ok) {
            String fname = firstNameTextField.getText();
            if (fname == null || fname.trim().equals("")) {
                title = language.getString("title_error");
                header = language.getString("registration_empty_fname_error_header");
                context = language.getString("registration_empty_error_context");
                ok = false;
            }
        }

        if (ok) {
            String lname = lastNameTextField.getText();
            if (lname == null || lname.trim().equals("")) {
                title = language.getString("title_error");
                header = language.getString("registration_empty_lname_error_header");
                context = language.getString("registration_empty_error_context");
                ok = false;
            }
        }

        if (ok) {
            String email = emailTextField.getText();
            if (email == null || email.trim().equals("")) {
                title = language.getString("title_error");
                header = language.getString("registration_empty_email_error_header");
                context = language.getString("registration_empty_error_context");
                ok = false;
            } else if (!Util.validateEmailAddress(email)) {
                title = language.getString("title_error");
                header = language.getString("illegal_user_email_error_header");
                context = language.getString("illegal_user_email_error_context") + ": " + email + ".";
                ok = false;
            }
        }

        if (ok) {
            String org = organizationTextField.getText();
            if (org == null || org.trim().equals("")) {
                title = language.getString("title_error");
                header = language.getString("registration_empty_org_error_header");
                context = language.getString("registration_empty_error_context");
                ok = false;
            }
        }

        if (ok) {
            String affiliation = (String) affiliationComboBox.getValue();

            if (affiliation == null || affiliation.trim().equals("")) {
                title = language.getString("title_error");
                header = language.getString("registration_empty_affiliation_error_header");
                context = language.getString("registration_empty_error_context");
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

    public boolean submitRegister() throws UnsupportedEncodingException {
        String fname = firstNameTextField.getText();
        String lname = lastNameTextField.getText();
        String email = emailTextField.getText();
        String org = organizationTextField.getText();

        // Only use English to submit registration
        String[] affiliationIds = {
            "Academia",
            "Commercial",
            "Government",
            "Military",
            "Non-Profit",
            "Independent",
            "Other"
        };

        String affiliation = affiliationIds[affiliationComboBox.getSelectionModel().getSelectedIndex()];

        String os = System.getProperty("os.name");
        String os_version = System.getProperty("os.version");
        String uuid = WildID.wildIDProperties.getUUID();

        String urlStr = WildID.WILDID_SERVER + "service.jsp?"
                + "q=register"
                + "&uuid=" + URLEncoder.encode(uuid, "UTF-8")
                + "&fname=" + URLEncoder.encode(fname, "UTF-8")
                + "&lname=" + URLEncoder.encode(lname, "UTF-8")
                + "&email=" + URLEncoder.encode(email, "UTF-8")
                + "&org=" + URLEncoder.encode(org, "UTF-8")
                + "&affiliation=" + URLEncoder.encode(affiliation, "UTF-8")
                + "&os=" + URLEncoder.encode(os, "UTF-8")
                + "&os_version=" + URLEncoder.encode(os_version, "UTF-8");

        boolean success;
        String message = language.getString("registration_register_failed_content");

        try {
            URL url = new URL(urlStr);
            InputStream is = url.openStream();

            // read from the URL
            Scanner scan = new Scanner(is);
            String jsonData = new String();
            while (scan.hasNext()) {
                jsonData += scan.nextLine();
            }
            is.close();
            scan.close();

            // build a JSON object
            JSONObject obj = new JSONObject(jsonData);
            success = (Boolean) obj.get("success");

            if (!success) {
                message += (String) obj.get("message");
            }

        } catch (MalformedURLException ex) {
            log.info("MalformedURLException: " + ex.toString());
            success = false;
        } catch (IOException ex) {
            log.info("IOException: " + ex.toString());
            success = false;
        } catch (JSONException ex) {
            log.info("JSONException: " + ex.toString());
            success = false;
        } catch (Exception ex) {
            log.info("JSONException: " + ex.toString());
            success = false;
        }

        Scene scene = submitButton.getScene();
        scene.getWindow().hide();
        String title;
        String header;
        String context;

        if (success) {
            Util.updateWildIDProperties("registered", "true");

            Util.alertInformationPopup(
                    language.getString("title_success"),
                    language.getString("registration_register_success_header"),
                    language.getString("registration_register_success_content"),
                    language.getString("alert_ok"));
        } else {

            if (!Util.isOnline()) {
                message += language.getString("registration_register_failed_no_network");
            }

            title = language.getString("title_error");
            header = language.getString("registration_register_failed_header");
            context = message;

            Util.alertErrorPopup(
                    title,
                    header,
                    context,
                    language.getString("alert_ok"));
        }

        return success;
    }
}
