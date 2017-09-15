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

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import org.wildid.entity.Country;
import org.wildid.entity.Organization;
import org.wildid.entity.Person;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class PersonNewPane extends ProjectNewPersonPane {

    public PersonNewPane(LanguageModel language) {
        super(language, null, new ArrayList<>(), new ArrayList<>());

        // hide the select checkboxes
        super.removeRow(super.grid, 0);
        super.removeRow(super.grid, 11);

        // change the text of the save button        
        this.saveButton.setText(language.getString("person_create_new_save_button"));
        this.saveButton.setId("person_new_pane_save_button");
        this.hbBtn.setPadding(new Insets(10, 0, 0, 0));

        // change the title
        this.titleLabel.setText(language.getString("person_new_pane_title"));

    }

    @Override
    public void setLanguage(LanguageModel language) {

        this.language = language;
        this.titleLabel.setText(language.getString("person_new_pane_title"));
        this.userFirstNameLabel.setText(language.getString("user_first_name"));
        this.userLastNameLabel.setText(language.getString("user_last_name"));
        this.userAddressLabel.setText(language.getString("user_address1"));
        this.userAddress2Label.setText(language.getString("user_address2"));
        this.userCityLabel.setText(language.getString("user_city"));
        this.userStateLabel.setText(language.getString("user_state"));
        this.userCountryLabel.setText(language.getString("user_country"));
        this.userPostalLabel.setText(language.getString("user_postal_code"));
        this.userPhoneLabel.setText(language.getString("user_phone"));
        this.userEmailLabel.setText(language.getString("user_email"));
        this.userOrganizationLabel.setText(language.getString("user_organization"));
        this.saveButton.setText(language.getString("person_create_new_save_button"));

    }

    @Override
    public boolean validate(List<Person> persons) {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        String firstName = userFirstNameTextField.getText();
        if (firstName == null || firstName.trim().equals("")) {
            title = language.getString("title_error");
            header = language.getString("empty_user_firstname_error_header");
            context = language.getString("empty_user_firstname_error_context");
            ok = false;
        }

        if (ok) {
            String lastName = userLastNameTextField.getText();
            if (lastName == null || lastName.trim().equals("")) {
                title = language.getString("title_error");
                header = language.getString("empty_user_lastname_error_header");
                context = language.getString("empty_user_lastname_error_context");
                ok = false;
            }
        }

        if (ok) {
            String email = userEmailTextField.getText();
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
            } else {
                // check if there is a person with the same email address
                for (Person person : persons) {
                    if (person.getEmail().equals(email.trim())) {
                        title = language.getString("title_error");
                        header = language.getString("duplicate_user_email_error_header");
                        context = language.getString("duplicate_user_email_error_context")
                                + ": " + person.getFirstName() + " " + person.getLastName() + ".";
                        ok = false;
                        break;
                    }
                }
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

    @Override
    public Person getPerson() {

        Person person = new Person();
        person.setFirstName(userFirstNameTextField.getText().trim());
        person.setLastName(userLastNameTextField.getText().trim());
        person.setAddress(userAddressTextField.getText());
        person.setAddress2(userAddress2TextField.getText());
        person.setCity(userCityTextField.getText());
        person.setState(userStateTextField.getText());
        person.setCountry((Country) userCountryCombo.getValue());
        person.setPostalCode(userPostalTextField.getText());
        person.setPhone(userPhoneTextField.getText());
        person.setEmail(userEmailTextField.getText().trim());
        person.setOrganization((Organization) userOrganizationCombo.getValue());
        return person;

    }

}
