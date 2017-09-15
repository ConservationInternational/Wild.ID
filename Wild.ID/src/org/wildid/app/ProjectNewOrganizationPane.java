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

import java.util.LinkedList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.wildid.entity.Country;
import org.wildid.entity.Organization;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectNewOrganizationPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    protected Project project;
    protected List<Organization> nonMembers;

    protected GridPane grid;

    protected Label titleLabel;
    protected Label orgChooseLabel;
    protected Label orgNameLabel;
    protected Label orgAddressLabel;
    protected Label orgAddress2Label;
    protected Label orgCityLabel;
    protected Label orgStateLabel;
    protected Label orgCountryLabel;
    protected Label orgPostalLabel;
    protected Label orgPhoneLabel;
    protected Label orgEmailLabel;
    protected Label orgsLabel;

    protected RadioButton createOrgRadio;
    protected RadioButton selectOrgRadio;
    protected TextField orgNameTextField = new TextField();
    protected TextField orgAddressTextField = new TextField();
    protected TextField orgAddress2TextField = new TextField();
    protected TextField orgCityTextField = new TextField();
    protected TextField orgStateTextField = new TextField();
    protected TextField orgPostalTextField = new TextField();
    protected TextField orgPhoneTextField = new TextField();
    protected TextField orgEmailTextField = new TextField();
    protected CountryComboBox orgCountryCombo;
    protected OrganizationComboBox orgsCombo;

    protected Button saveButton;
    protected HBox hbBtn = new HBox(10);
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");

    public ProjectNewOrganizationPane(LanguageModel language, Project project, List<Organization> nonMembers) {
        this.language = language;
        this.project = project;
        this.nonMembers = nonMembers;
        this.setStyle(BG_COLOR_STYLE);

        if (project != null) {
            this.titleLabel = new Label(language.getString("project_new_org_pane_title") + " : " + project.getName());
        } else {
            this.titleLabel = new Label("");
        }
        this.titleLabel.setStyle(TITLE_STYLE);

        this.imgView = new ImageView(pageImg);
        this.imgView.setVisible(true);

        HBox titleBox = new HBox(15);
        titleBox.setStyle(WildIDDataPane.BG_TITLE_STYLE);
        titleBox.setPadding(new Insets(10, 0, 10, 30));
        titleBox.getChildren().addAll(this.titleLabel, this.imgView);

        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.getChildren().addAll(titleBox, createForm());
        vbox.prefWidthProperty().bind(this.widthProperty());

        this.getChildren().add(vbox);
        //this.getChildren().add(createForm());
    }

    private Pane createForm() {

        grid = new GridPane();
        grid.setPadding(new Insets(30, 10, 10, 30));
        grid.setHgap(20);
        grid.setStyle(TEXT_STYLE);
        grid.setVgap(10);

        orgChooseLabel = new Label(language.getString("org_choose_type"));
        grid.add(orgChooseLabel, 1, 0);

        final ToggleGroup group = new ToggleGroup();
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {
                    String data = (String) group.getSelectedToggle().getUserData();
                    if (data != null && data.equals("Create")) {
                        showCreateUI(grid);
                    } else if (data != null && data.equals("Select")) {
                        showSelectUI(grid);
                    }
                }
            }
        });

        createOrgRadio = new RadioButton(language.getString("org_create_radio"));
        createOrgRadio.setToggleGroup(group);
        createOrgRadio.setSelected(true);
        createOrgRadio.setUserData("Create");

        selectOrgRadio = new RadioButton(language.getString("org_select_radio"));
        selectOrgRadio.setToggleGroup(group);
        selectOrgRadio.setUserData("Select");

        if (nonMembers.isEmpty()) {
            selectOrgRadio.setDisable(true);
        }

        HBox typeHbox = new HBox(20);
        typeHbox.getChildren().add(createOrgRadio);
        typeHbox.getChildren().add(selectOrgRadio);
        grid.add(typeHbox, 2, 0);

        // name
        orgNameTextField.setMinWidth(400);
        orgNameTextField.setStyle(REQUIRED_STYLE);
        orgNameLabel = new Label(language.getString("org_name"));
        grid.add(orgNameLabel, 1, 1);
        grid.add(orgNameTextField, 2, 1);

        // address 1
        orgAddressTextField.setMinWidth(400);
        orgAddressLabel = new Label(language.getString("user_address1"));
        grid.add(orgAddressLabel, 1, 2);
        grid.add(orgAddressTextField, 2, 2);

        // address 2
        orgAddress2TextField.setMinWidth(400);
        orgAddress2Label = new Label(language.getString("user_address2"));
        grid.add(orgAddress2Label, 1, 3);
        grid.add(orgAddress2TextField, 2, 3);

        // city 
        orgCityTextField.setMaxWidth(200);
        orgCityLabel = new Label(language.getString("user_city"));
        grid.add(orgCityLabel, 1, 4);
        grid.add(orgCityTextField, 2, 4);

        // state 
        orgStateTextField.setMaxWidth(200);
        orgStateLabel = new Label(language.getString("user_state"));
        grid.add(orgStateLabel, 1, 5);
        grid.add(orgStateTextField, 2, 5);

        // country combo
        orgCountryCombo = new CountryComboBox(this.language);
        orgCountryCombo.setPrefWidth(200);
        orgCountryLabel = new Label(language.getString("user_country"));
        grid.add(orgCountryLabel, 1, 6);
        grid.add(orgCountryCombo, 2, 6);

        //postalCode;
        orgPostalTextField.setMaxWidth(200);
        orgPostalLabel = new Label(language.getString("user_postal_code"));
        grid.add(orgPostalLabel, 1, 7);
        grid.add(orgPostalTextField, 2, 7);

        //phone;
        orgPhoneTextField.setMaxWidth(200);
        orgPhoneLabel = new Label(language.getString("user_phone"));
        grid.add(orgPhoneLabel, 1, 8);
        grid.add(orgPhoneTextField, 2, 8);

        //email;
        orgEmailTextField.setMaxWidth(200);
        orgEmailLabel = new Label(language.getString("user_email"));
        //orgEmailTextField.setStyle(requiredStyle);
        grid.add(orgEmailLabel, 1, 9);
        grid.add(orgEmailTextField, 2, 9);

        // button
        saveButton = new Button(language.getString("org_save_button"));
        saveButton.setId("new_org_save");
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().add(saveButton);
        hbBtn.setPadding(new Insets(10, 0, 0, 0));
        grid.add(hbBtn, 2, 10);

        orgsLabel = new Label(language.getString("org_existing_orgs"));
        //orgsCombo = new PersonComboBox(nonMembers);
        orgsCombo = new OrganizationComboBox(this.nonMembers);
        orgsCombo.setPrefWidth(400);
        orgsCombo.setStyle(REQUIRED_STYLE);

        return grid;
    }

    private void showCreateUI(GridPane grid) {
        //removeRow(grid, 3);
        removeRow(grid, 2);
        removeRow(grid, 1);
        grid.add(orgNameLabel, 1, 1);
        grid.add(orgNameTextField, 2, 1);
        grid.add(orgAddressLabel, 1, 2);
        grid.add(orgAddressTextField, 2, 2);
        grid.add(orgAddress2Label, 1, 3);
        grid.add(orgAddress2TextField, 2, 3);
        grid.add(orgCityLabel, 1, 4);
        grid.add(orgCityTextField, 2, 4);
        grid.add(orgStateLabel, 1, 5);
        grid.add(orgStateTextField, 2, 5);
        grid.add(orgCountryLabel, 1, 6);
        grid.add(orgCountryCombo, 2, 6);
        grid.add(orgPostalLabel, 1, 7);
        grid.add(orgPostalTextField, 2, 7);
        grid.add(orgPhoneLabel, 1, 8);
        grid.add(orgPhoneTextField, 2, 8);
        grid.add(orgEmailLabel, 1, 9);
        grid.add(orgEmailTextField, 2, 9);
        grid.add(hbBtn, 2, 10);

        saveButton.setId("new_org_save");
        this.titleLabel.setText(language.getString("project_new_org_pane_title") + " : " + project.getName());
    }

    private void showSelectUI(GridPane grid) {
        removeRow(grid, 11);
        removeRow(grid, 10);
        removeRow(grid, 9);
        removeRow(grid, 8);
        removeRow(grid, 7);
        removeRow(grid, 6);
        removeRow(grid, 5);
        removeRow(grid, 4);
        removeRow(grid, 3);
        removeRow(grid, 2);
        removeRow(grid, 1);
        grid.add(orgsLabel, 1, 1);
        grid.add(orgsCombo, 2, 1);
        grid.add(hbBtn, 2, 2);

        saveButton.setId("new_selected_org_save");
        this.titleLabel.setText(language.getString("project_new_org_pane_select_title") + " : " + project.getName());

    }

    protected void removeRow(GridPane gridPane, int row) {
        List<Node> children = new LinkedList<>(gridPane.getChildren());
        for (Node node : children) {
            int nodeRow = GridPane.getRowIndex(node);
            if (nodeRow == row) {
                gridPane.getChildren().remove(node);
            } else if (nodeRow > row) {
                GridPane.setRowIndex(node, nodeRow - 1);
            }
        }
    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
    }

    @Override
    public void setLanguage(LanguageModel language) {
        this.language = language;
        if (project != null) {
            this.titleLabel.setText(language.getString("project_new_org_pane_title") + " : " + project.getName());
        }
        this.createOrgRadio.setText(language.getString("org_create_radio"));
        this.selectOrgRadio.setText(language.getString("org_select_radio"));
        this.orgChooseLabel.setText(language.getString("org_choose_type"));
        this.orgNameLabel.setText(language.getString("org_name"));
        this.orgAddressLabel.setText(language.getString("user_address1"));
        this.orgAddress2Label.setText(language.getString("user_address2"));
        this.orgCityLabel.setText(language.getString("user_city"));
        this.orgStateLabel.setText(language.getString("user_state"));
        this.orgCountryLabel.setText(language.getString("user_country"));
        this.orgPostalLabel.setText(language.getString("user_postal_code"));
        this.orgPhoneLabel.setText(language.getString("user_phone"));
        this.orgEmailLabel.setText(language.getString("user_email"));
        this.orgsLabel.setText(language.getString("org_existing_orgs"));
        this.saveButton.setText(language.getString("org_save_button"));
    }

    public Project getProject() {
        return this.project;
    }

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
            for (Organization org : orgs) {
                if (org.getName().equals(name.trim())) {
                    title = language.getString("title_error");
                    header = language.getString("duplicate_org_name_error_header");
                    context = language.getString("duplicate_org_name_error_context")
                            + ": " + org.getName() + ".";
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

    public Organization getOrganization() {

        Organization org = new Organization();
        org.setName(orgNameTextField.getText().trim());
        org.setAddress(orgAddressTextField.getText());
        org.setAddress2(orgAddress2TextField.getText());
        org.setCity(orgCityTextField.getText());
        org.setState(orgStateTextField.getText());
        org.setCountry((Country) orgCountryCombo.getValue());
        org.setPostalCode(orgPostalTextField.getText());
        org.setPhone(orgPhoneTextField.getText());
        org.setEmail(orgEmailTextField.getText().trim());

        return org;
    }

    public Organization getSelectedOrganization() {
        return (Organization) this.orgsCombo.getValue();
    }

    public boolean validateSelectedOrganization() {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        Object object = orgsCombo.getValue();
        if (object == null) {
            title = language.getString("title_error");
            header = language.getString("empty_selected_org_error_header");
            context = language.getString("empty_selected_org_error_context");
            ok = false;
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
