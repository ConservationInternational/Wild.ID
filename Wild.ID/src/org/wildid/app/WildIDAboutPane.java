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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 *
 * @author Kai Lin, Minh Phan

 */
public class WildIDAboutPane extends WildIDDataPane implements LanguageChangable {

    protected LanguageModel language;
    private final Label titleLabel;
    private Hyperlink licenseLink;
    private final int width = 500;
    private final WildIDController controller;
    private Text develTeamText;
    private Text collaborationText;
    private Text copyrightText;
    private TextArea textConfig;
    private String javaVersionStr = "";
    private String osNameStr = "";
    private String osVersionStr = "";
    private String osArchStr = "";
    private String userDirStr = "";

    public WildIDAboutPane(WildIDController controller, LanguageModel language) {
        this.language = language;
        this.controller = controller;

        titleLabel = new Label(language.getString("menu_Wild.ID_about"));
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
        VBox vbox = new VBox(10);
        vbox.setPrefWidth(width);
        vbox.setMaxWidth(width);
        vbox.setMinWidth(width);

        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10));
        ImageView imageView = new ImageView(new Image("resources/icons/wild_id_splash.jpg", width - 20, 0, true, true));

        licenseLink = new Hyperlink(language.getString("wildID_about_pane_license_info"));
        licenseLink.setVisited(true);
        licenseLink.setId("hyperlink_license");
        licenseLink.setOnAction(controller);

        copyrightText = new Text();
        copyrightText.setWrappingWidth(width - 20);
        copyrightText.setText(language.getString("wildID_about_pane_copyright"));

        develTeamText = new Text();
        develTeamText.setWrappingWidth(width - 20);
        develTeamText.setText(language.getString("wildID_about_pane_devel_team") + ": Jorge Ahumada, Eric Fegraus, James Maccarthy, Eileen Larney, Viswanath Nandigam, Minh Phan & Kai Lin.\n");

        collaborationText = new Text();
        collaborationText.setWrappingWidth(width - 20);
        collaborationText.setText(language.getString("wildID_about_pane_collaboration") + " San Diego Supercomputer Center & TEAM Network.\n");

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(2.0);
        dropShadow.setOffsetX(-2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.color(0.4, 0.5, 0.5));

        Text versionLabel = new Text(WildID.VERSION);
        versionLabel.setEffect(dropShadow);
        versionLabel.setCache(true);
        versionLabel.setFill(Color.web("0x62742a"));
        versionLabel.setFont(Font.font(null, FontWeight.MEDIUM, 32));

        StackPane stackScreen = new StackPane();
        stackScreen.getChildren().addAll(imageView, versionLabel);
        StackPane.setAlignment(versionLabel, Pos.TOP_LEFT);
        StackPane.setMargin(versionLabel, new Insets(212, 0, 0, 200));

        javaVersionStr = System.getProperty("java.version");
        osNameStr = System.getProperty("os.name");
        osVersionStr = System.getProperty("os.version");
        osArchStr = System.getProperty("os.arch");
        userDirStr = System.getProperty("user.dir");

        textConfig = new TextArea();
        this.textConfig.setText(
                language.getString("wildID_about_pane_product_version") + ": Wild.ID " + WildID.VERSION
                + "\nJava: " + javaVersionStr
                + "\n" + language.getString("wildID_about_pane_system") + ": " + osNameStr
                + " " + language.getString("wildID_about_pane_version") + " " + osVersionStr
                + " " + language.getString("wildID_about_pane_running_on") + " " + osArchStr
                + "\n" + language.getString("wildID_about_pane_user_dir") + ": " + userDirStr);

        textConfig.setPrefWidth(width - 20);
        textConfig.setPrefHeight(100);
        textConfig.setMaxHeight(100);
        textConfig.setMinHeight(100);
        textConfig.setWrapText(true);
        textConfig.setEditable(false);

        vbox.getChildren().addAll(stackScreen, copyrightText, licenseLink, develTeamText, collaborationText, textConfig);
        return vbox;
    }

    @Override
    /* Don't use this code now, but just leave it here in some case we might need it */
    public void setLanguage(LanguageModel language) {
        this.language = language;
        this.titleLabel.setText(language.getString("menu_Wild.ID_about"));
        this.copyrightText.setText(language.getString("wildID_about_pane_copyright"));
        this.licenseLink.setText(language.getString("wildID_about_pane_license_info"));
        this.develTeamText.setText(language.getString("wildID_about_pane_devel_team"));
        this.collaborationText.setText(language.getString("wildID_about_pane_collaboration"));
        this.textConfig.setText(
                language.getString("wildID_about_pane_product_version") + ": Wild.ID " + WildID.VERSION
                + "\nJava: " + javaVersionStr
                + "\n" + language.getString("wildID_about_pane_system") + ": " + osNameStr
                + " " + language.getString("wildID_about_pane_version") + " " + osVersionStr
                + " " + language.getString("wildID_about_pane_running_on") + " " + osArchStr
                + "\n" + language.getString("wildID_about_pane_user_dir") + ": " + userDirStr);

    }

    @Override
    public void setWildIDController(WildIDController controller) {
        this.licenseLink.setOnAction(controller);
    }
}
