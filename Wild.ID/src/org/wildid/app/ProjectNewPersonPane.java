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
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import org.wildid.entity.Person;
import org.wildid.entity.PersonComparator;
import org.wildid.entity.Project;
import org.wildid.entity.ProjectPersonRole;
import org.wildid.entity.Role;
import org.wildid.service.OrganizationService;
import org.wildid.service.OrganizationServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class ProjectNewPersonPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    protected Project project;
    protected List<Person> nonMembers;
    protected List<Role> roles;

    protected Label titleLabel;
    protected Label userChooseLabel;
    protected Label userFirstNameLabel;
    protected Label userLastNameLabel;
    protected Label userAddressLabel;
    protected Label userAddress2Label;
    protected Label userCityLabel;
    protected Label userStateLabel;
    protected Label userCountryLabel;
    protected Label userPostalLabel;
    protected Label userPhoneLabel;
    protected Label userEmailLabel;
    protected Label userOrganizationLabel;
    protected Label userRolesLabel;
    protected Label usersLabel;

    protected GridPane grid;
    protected RadioButton createUserRadio;
    protected RadioButton selectUserRadio;
    protected TextField userFirstNameTextField = new TextField();
    protected TextField userLastNameTextField = new TextField();
    protected TextField userAddressTextField = new TextField();
    protected TextField userAddress2TextField = new TextField();
    protected TextField userCityTextField = new TextField();
    protected TextField userStateTextField = new TextField();
    protected TextField userPostalTextField = new TextField();
    protected TextField userPhoneTextField = new TextField();
    protected TextField userEmailTextField = new TextField();
    protected CountryComboBox userCountryCombo;
    protected OrganizationComboBox userOrganizationCombo;
    protected PersonComboBox usersCombo;
    protected GridPane roleGrid = new GridPane();
    protected List<CheckBox> roleCheckBoxes = new ArrayList<>();

    protected Button saveButton;
    protected HBox hbBtn = new HBox(10);
    protected ImageView imgView;
    protected Image pageImg = new Image("resources/icons/page.png");

    public ProjectNewPersonPane(LanguageModel language, Project project,
            List<Person> nonMembers, List<Role> roles) {

        this.language = language;
        this.project = project;
        this.nonMembers = nonMembers;
        this.roles = roles;
        this.setStyle(BG_COLOR_STYLE);

        if (this.project != null) {
            this.titleLabel = new Label(language.getString("project_new_person_pane_title") + " : " + this.project.getName());
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
        //grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(30, 10, 10, 30));
        grid.setHgap(20);
        grid.setStyle(WildIDDataPane.TEXT_STYLE);
        grid.setVgap(10);

        userChooseLabel = new Label(language.getString("user_choose_type"));
        grid.add(userChooseLabel, 1, 0);

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

        createUserRadio = new RadioButton(language.getString("user_create_radio"));
        createUserRadio.setToggleGroup(group);
        createUserRadio.setSelected(true);
        createUserRadio.setUserData("Create");

        selectUserRadio = new RadioButton(language.getString("user_select_radio"));
        selectUserRadio.setToggleGroup(group);
        selectUserRadio.setUserData("Select");

        if (nonMembers.isEmpty()) {
            selectUserRadio.setDisable(true);
        }

        HBox typeHbox = new HBox(20);
        typeHbox.getChildren().add(createUserRadio);
        typeHbox.getChildren().add(selectUserRadio);
        grid.add(typeHbox, 2, 0);

        // first name
        userFirstNameTextField.setMaxWidth(200);
        userFirstNameTextField.setStyle(REQUIRED_STYLE);
        userFirstNameLabel = new Label(language.getString("user_first_name"));
        grid.add(userFirstNameLabel, 1, 1);
        grid.add(userFirstNameTextField, 2, 1);

        // last name
        userLastNameTextField.setMaxWidth(200);
        userLastNameTextField.setStyle(REQUIRED_STYLE);
        userLastNameLabel = new Label(language.getString("user_last_name"));
        grid.add(userLastNameLabel, 1, 2);
        grid.add(userLastNameTextField, 2, 2);

        // address 1
        userAddressTextField.setMinWidth(400);
        userAddressLabel = new Label(language.getString("user_address1"));
        grid.add(userAddressLabel, 1, 3);
        grid.add(userAddressTextField, 2, 3);

        // address 2
        userAddress2TextField.setMinWidth(400);
        userAddress2Label = new Label(language.getString("user_address2"));
        grid.add(userAddress2Label, 1, 4);
        grid.add(userAddress2TextField, 2, 4);

        // city 
        userCityTextField.setMaxWidth(200);
        userCityLabel = new Label(language.getString("user_city"));
        grid.add(userCityLabel, 1, 5);
        grid.add(userCityTextField, 2, 5);

        // state 
        userStateTextField.setMaxWidth(200);
        userStateLabel = new Label(language.getString("user_state"));
        grid.add(userStateLabel, 1, 6);
        grid.add(userStateTextField, 2, 6);

        // country combo
        userCountryCombo = new CountryComboBox(this.language);
        userCountryCombo.setPrefWidth(200);
        userCountryLabel = new Label(language.getString("user_country"));
        grid.add(userCountryLabel, 1, 7);
        grid.add(userCountryCombo, 2, 7);

        //postalCode;
        userPostalTextField.setMaxWidth(200);
        userPostalLabel = new Label(language.getString("user_postal_code"));
        grid.add(userPostalLabel, 1, 8);
        grid.add(userPostalTextField, 2, 8);

        //phone;
        userPhoneTextField.setMaxWidth(200);
        userPhoneLabel = new Label(language.getString("user_phone"));
        grid.add(userPhoneLabel, 1, 9);
        grid.add(userPhoneTextField, 2, 9);

        //email;
        userEmailTextField.setMaxWidth(200);
        userEmailLabel = new Label(language.getString("user_email"));
        userEmailTextField.setStyle(REQUIRED_STYLE);
        grid.add(userEmailLabel, 1, 10);
        grid.add(userEmailTextField, 2, 10);

        // project status
        OrganizationService orgService = new OrganizationServiceImpl();
        List<Organization> orgs = orgService.listOrganization();
        userOrganizationCombo = new OrganizationComboBox(orgs);
        userOrganizationCombo.getItems().add(null);   // could be null
        userOrganizationCombo.setValue(null);         // is null by default
        userOrganizationCombo.setMinWidth(450);
        userOrganizationLabel = new Label(language.getString("user_organization"));
        grid.add(userOrganizationLabel, 1, 11);
        grid.add(userOrganizationCombo, 2, 11);

        // roles
        userRolesLabel = new Label(language.getString("user_roles"));
        userRolesLabel.setStyle("-fx-padding: 0 0 0 0");
        typeHbox.getChildren().add(userRolesLabel);
        GridPane.setValignment(userRolesLabel, VPos.TOP);
        grid.add(userRolesLabel, 1, 12);

        roleGrid.setAlignment(Pos.TOP_LEFT);
        roleGrid.setPadding(new Insets(0, 0, 10, -20));
        roleGrid.setHgap(20);
        roleGrid.setStyle(TEXT_STYLE);
        roleGrid.setVgap(10);

        int row = 0;
        int col = 1;
        for (Role role : roles) {
            String roleName = language.getString("role_" + role.getName().replaceAll(" ", "_"));
            CheckBox cb = new CheckBox(roleName);
            roleCheckBoxes.add(cb);
            if (col < 4) {
                roleGrid.add(cb, col++, row);
            } else {
                col = 1;
                row++;
                roleGrid.add(cb, col++, row);
            }
        }

        grid.add(roleGrid, 2, 12);

        // button
        saveButton = new Button(language.getString("user_save_button"));
        saveButton.setId("new_person_save");
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.getChildren().add(saveButton);
        //grid.setPadding(new Insets(20, 0, 0, 0));
        grid.add(hbBtn, 2, 13);

        usersLabel = new Label(language.getString("user_existing_users"));
        usersCombo = new PersonComboBox(nonMembers);
        usersCombo.setPrefWidth(400);
        usersCombo.setStyle(REQUIRED_STYLE);

        return grid;
    }

    private void showCreateUI(GridPane grid) {
        removeRow(grid, 3);
        removeRow(grid, 2);
        removeRow(grid, 1);
        grid.add(userFirstNameLabel, 1, 1);
        grid.add(userFirstNameTextField, 2, 1);
        grid.add(userLastNameLabel, 1, 2);
        grid.add(userLastNameTextField, 2, 2);
        grid.add(userAddressLabel, 1, 3);
        grid.add(userAddressTextField, 2, 3);
        grid.add(userAddress2Label, 1, 4);
        grid.add(userAddress2TextField, 2, 4);
        grid.add(userCityLabel, 1, 5);
        grid.add(userCityTextField, 2, 5);
        grid.add(userStateLabel, 1, 6);
        grid.add(userStateTextField, 2, 6);
        grid.add(userCountryLabel, 1, 7);
        grid.add(userCountryCombo, 2, 7);
        grid.add(userPostalLabel, 1, 8);
        grid.add(userPostalTextField, 2, 8);
        grid.add(userPhoneLabel, 1, 9);
        grid.add(userPhoneTextField, 2, 9);
        grid.add(userEmailLabel, 1, 10);
        grid.add(userEmailTextField, 2, 10);
        grid.add(userOrganizationLabel, 1, 11);
        grid.add(userOrganizationCombo, 2, 11);
        grid.add(userRolesLabel, 1, 12);
        grid.add(roleGrid, 2, 12);
        grid.add(hbBtn, 2, 13);

        saveButton.setId("new_person_save");
        this.titleLabel.setText(language.getString("project_new_person_pane_title") + " : " + project.getName());
    }

    private void showSelectUI(GridPane grid) {
        removeRow(grid, 13);
        removeRow(grid, 12);
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
        grid.add(usersLabel, 1, 1);
        grid.add(usersCombo, 2, 1);
        grid.add(userRolesLabel, 1, 2);
        grid.add(roleGrid, 2, 2);
        grid.add(hbBtn, 2, 3);

        saveButton.setId("new_selected_person_save");
        this.titleLabel.setText(language.getString("project_new_person_pane_select_title") + " : " + project.getName());

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

    private int getRowCount(GridPane pane) {
        int numRows = pane.getRowConstraints().size();
        for (int i = 0; i < pane.getChildren().size(); i++) {
            Node child = pane.getChildren().get(i);
            if (child.isManaged()) {
                Integer rowIndex = GridPane.getRowIndex(child);
                if (rowIndex != null) {
                    numRows = Math.max(numRows, rowIndex + 1);
                }
            }
        }
        return numRows;
    }

    @Override
    public void setLanguage(LanguageModel language) {

        this.language = language;
        this.titleLabel.setText(language.getString("project_new_person_pane_title") + " : " + project.getName());
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
        this.userRolesLabel.setText(language.getString("user_roles"));
        this.usersLabel.setText(language.getString("user_existing_users"));
        this.saveButton.setText(language.getString("user_save_button"));

        for (int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);
            String roleName = language.getString("role_" + role.getName().replaceAll(" ", "_"));
            roleCheckBoxes.get(i).setText(roleName);
        }

    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.saveButton.setOnAction(controller);
    }

    public Project getProject() {
        return this.project;
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

        // check that a role is selected
        if (ok) {
            boolean checked = false;
            for (CheckBox cb : this.roleCheckBoxes) {
                if (cb.isSelected()) {
                    checked = true;
                    break;
                }
            }
            if (!checked) {
                title = language.getString("title_error");
                header = language.getString("empty_user_role_error_header");
                context = language.getString("empty_user_role_error_context");
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

    public boolean validateSelectedPerson() {

        boolean ok = true;
        String title = null;
        String header = null;
        String context = null;

        Object object = usersCombo.getValue();
        if (object == null) {
            title = language.getString("title_error");
            header = language.getString("empty_selected_user_error_header");
            context = language.getString("empty_selected_user_error_context");
            ok = false;
        }

        // check that a role is selected
        if (ok) {
            boolean checked = false;
            for (CheckBox cb : this.roleCheckBoxes) {
                if (cb.isSelected()) {
                    checked = true;
                    break;
                }
            }
            if (!checked) {
                title = language.getString("title_error");
                header = language.getString("empty_user_role_error_header");
                context = language.getString("empty_user_role_error_context");
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

        Set<ProjectPersonRole> pprs = new HashSet<>();
        for (int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);
            if (roleCheckBoxes.get(i).isSelected()) {
                ProjectPersonRole ppr = new ProjectPersonRole(person, this.project, role, new Date(), null);
                pprs.add(ppr);
            }
        }
        person.setProjectPersonRoles(pprs);

        return person;
    }

    public List<ProjectPersonRole> createNewProjectPersonRolesForSelectedPerson() {

        Person person = (Person) usersCombo.getValue();
        List<ProjectPersonRole> pprs = new ArrayList<>();
        for (int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);
            if (roleCheckBoxes.get(i).isSelected()) {
                ProjectPersonRole ppr = new ProjectPersonRole(person, this.project, role, new Date(), null);
                pprs.add(ppr);
            }
        }

        return pprs;

    }

    public Person getSelectedPerson() {
        return (Person) this.usersCombo.getValue();
    }

    public void addPersonForSelection(Person person) {

        TreeSet<Person> personSet = new TreeSet<>(new PersonComparator());
        personSet.addAll(nonMembers);
        personSet.add(person);

        nonMembers.clear();
        nonMembers.addAll(personSet);
        usersCombo.setItems(FXCollections.observableList(nonMembers));

        selectUserRadio.setDisable(false);

    }

    public void updatePersonForSelection(Person person) {
        Person aPerson = null;
        for (Person p : nonMembers) {
            if (p.getPersonId().intValue() == person.getPersonId()) {
                aPerson = p;
                break;
            }
        }
        if (aPerson != null) {
            nonMembers.remove(aPerson);
            nonMembers.add(person);
        }

        usersCombo.setItems(FXCollections.observableList(nonMembers));
    }

}
