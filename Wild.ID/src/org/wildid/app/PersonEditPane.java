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
import org.wildid.entity.Person;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class PersonEditPane extends PersonNewPane {

    private Person person;
    private Button deleteButton = new Button(language.getString("person_edit_pane_delete_button"));

    public PersonEditPane(LanguageModel language, Person person) {
        super(language);

        this.person = person;

        // change the text of the save button        
        this.saveButton.setText(language.getString("person_edit_pane_save_button"));
        this.saveButton.setId("person_edit_pane_save_button");

        // set id of delete button
        this.deleteButton.setId("person_edit_pane_delete_button");

        this.hbBtn.getChildren().add(deleteButton);

        // change the title
        this.titleLabel.setText(language.getString("person_edit_pane_title"));
        this.imgView.setImage(new Image("resources/icons/page_edit.png"));

        //populate fields
        this.userFirstNameTextField.setText(person.getFirstName());
        this.userLastNameTextField.setText(person.getLastName());
        this.userAddressTextField.setText(person.getAddress());
        this.userAddress2TextField.setText(person.getAddress2());
        this.userCityTextField.setText(person.getCity());
        this.userStateTextField.setText(person.getState());
        this.userCountryCombo.setValue(person.getCountry());
        this.userPostalTextField.setText(person.getPostalCode());
        this.userPhoneTextField.setText(person.getPhone());
        this.userEmailTextField.setText(person.getEmail());
        this.userOrganizationCombo.setValue(person.getOrganization());

    }

    @Override
    public void setLanguage(LanguageModel language) {

        this.language = language;
        this.titleLabel.setText(language.getString("person_edit_pane_title"));
        this.createUserRadio.setText(language.getString("user_create_radio"));
        this.selectUserRadio.setText(language.getString("user_select_radio"));
        this.userChooseLabel.setText(language.getString("user_choose_type"));
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
        this.saveButton.setText(language.getString("person_edit_pane_save_button"));
        this.deleteButton.setText(language.getString("person_edit_pane_delete_button"));
    }

    @Override
    public Person getPerson() {
        this.person.setFirstName(userFirstNameTextField.getText());
        this.person.setLastName(userLastNameTextField.getText());
        this.person.setAddress(userAddressTextField.getText());
        this.person.setAddress2(userAddress2TextField.getText());
        this.person.setCity(userCityTextField.getText());
        this.person.setState(userStateTextField.getText());
        this.person.setCountry((Country) userCountryCombo.getValue());
        this.person.setPostalCode(userPostalTextField.getText());
        this.person.setPhone(userPhoneTextField.getText());
        this.person.setEmail(userEmailTextField.getText());
        this.person.setOrganization((Organization) userOrganizationCombo.getValue());
        return this.person;
    }

    public LanguageModel getLanguage() {
        return this.language;
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
        this.deleteButton.setOnAction(controller);
    }

    public void setPerson(Person person) {
        this.person = person;

        //populate fields
        this.userFirstNameTextField.setText(person.getFirstName());
        this.userLastNameTextField.setText(person.getLastName());
        this.userAddressTextField.setText(person.getAddress());
        this.userAddress2TextField.setText(person.getAddress2());
        this.userCityTextField.setText(person.getCity());
        this.userStateTextField.setText(person.getState());
        this.userCountryCombo.setValue(person.getCountry());
        this.userPostalTextField.setText(person.getPostalCode());
        this.userPhoneTextField.setText(person.getPhone());
        this.userEmailTextField.setText(person.getEmail());
        this.userOrganizationCombo.setValue(person.getOrganization());
    }

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
                for (Person p : persons) {
                    if (p.getPersonId().intValue() == this.person.getPersonId().intValue()) {
                        continue;
                    }
                    if (p.getEmail().equals(email.trim())) {
                        title = language.getString("title_error");
                        header = language.getString("duplicate_user_email_error_header");
                        context = language.getString("duplicate_user_email_error_context")
                                + ": " + p.getFirstName() + " " + p.getLastName() + ".";
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
}
