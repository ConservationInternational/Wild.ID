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
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import org.wildid.entity.Country;
import org.wildid.entity.Organization;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class OrganizationEditPane extends OrganizationNewPane {

    private Organization org;
    private final Button deleteButton = new Button(language.getString("org_edit_pane_delete_button"));

    public OrganizationEditPane(LanguageModel language, Organization org) {

        super(language);

        this.org = org;

        // change the text of the save button        
        this.saveButton.setText(language.getString("org_edit_pane_save_button"));
        this.saveButton.setId("org_edit_pane_save_button");

        // set id of delete button
        this.deleteButton.setId("org_edit_pane_delete_button");

        this.hbBtn.getChildren().add(deleteButton);

        // change the title
        this.titleLabel.setText(language.getString("org_edit_pane_title"));
        this.imgView.setImage(new Image("resources/icons/page_edit.png"));

        // populate fields
        this.orgNameTextField.setText(org.getName());
        this.orgAddressTextField.setText(org.getAddress());
        this.orgAddress2TextField.setText(org.getAddress2());
        this.orgCityTextField.setText(org.getCity());
        this.orgStateTextField.setText(org.getState());
        this.orgCountryCombo.setValue(org.getCountry());
        this.orgPostalTextField.setText(org.getPostalCode());
        this.orgPhoneTextField.setText(org.getPhone());
        this.orgEmailTextField.setText(org.getEmail());
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        this.titleLabel.setText(language.getString("org_edit_pane_title"));
        this.orgNameLabel.setText(language.getString("org_name"));
        this.orgAddressLabel.setText(language.getString("user_address1"));
        this.orgAddress2Label.setText(language.getString("user_address2"));
        this.orgCityLabel.setText(language.getString("user_city"));
        this.orgStateLabel.setText(language.getString("user_state"));
        this.orgCountryLabel.setText(language.getString("user_country"));
        this.orgPostalLabel.setText(language.getString("user_postal_code"));
        this.orgPhoneLabel.setText(language.getString("user_phone"));
        this.orgEmailLabel.setText(language.getString("user_email"));
        this.saveButton.setText(language.getString("org_edit_pane_save_button"));
        this.deleteButton.setText(language.getString("org_edit_pane_delete_button"));
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        this.deleteButton.setOnAction(controller);
    }

    @Override
    public Organization getOrganization() {

        this.org.setName(orgNameTextField.getText());
        this.org.setAddress(orgAddressTextField.getText());
        this.org.setAddress2(orgAddress2TextField.getText());
        this.org.setCity(orgCityTextField.getText());
        this.org.setState(orgStateTextField.getText());
        this.org.setCountry((Country) orgCountryCombo.getValue());
        this.org.setPostalCode(orgPostalTextField.getText());
        this.org.setPhone(orgPhoneTextField.getText());
        this.org.setEmail(orgEmailTextField.getText());

        return this.org;
    }

    @Override
    public boolean validate(List<Organization> orgs) {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        String name = orgNameTextField.getText();
        if (name == null || name.trim().equals("")) {
            title = language.getString("title_error");
            header = language.getString("empty_org_name_error_header");
            context = language.getString("empty_org_name_error_context");
            ok = false;
        } else {

            // check if there is a person with the same email address
            for (Organization o : orgs) {
                if (o.getOrganizationId().intValue() == org.getOrganizationId().intValue()) {
                    continue;
                }
                if (o.getName().equals(name.trim())) {
                    title = language.getString("title_error");
                    header = language.getString("duplicate_org_name_error_header");
                    context = language.getString("duplicate_org_name_error_context")
                            + ": " + o.getName() + ".";
                    ok = false;
                    break;
                }
            }

        }

        if (ok) {
            String email = orgEmailTextField.getText();
            if (email == null || email.trim().equals("")) {
                /*
                 title = language.getString("title_error");
                 header = language.getString("empty_org_email_error_header");
                 context = language.getString("empty_org_email_error_context");
                 ok = false;
                 */
            } else if (!Util.validateEmailAddress(email)) {
                title = language.getString("title_error");
                header = language.getString("illegal_org_email_error_header");
                context = language.getString("illegal_org_email_error_context") + ": " + email + ".";
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

    public void setOrganization(Organization org) {
        this.org = org;

        //populate fields
        this.orgNameTextField.setText(org.getName());
        this.orgAddressTextField.setText(org.getAddress());
        this.orgAddress2TextField.setText(org.getAddress2());
        this.orgCityTextField.setText(org.getCity());
        this.orgStateTextField.setText(org.getState());
        this.orgCountryCombo.setValue(org.getCountry());
        this.orgPostalTextField.setText(org.getPostalCode());
        this.orgPhoneTextField.setText(org.getPhone());
        this.orgEmailTextField.setText(org.getEmail());
    }

}
