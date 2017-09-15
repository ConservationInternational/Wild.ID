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
package org.team.wildid.plugin;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.wildid.app.LanguageModel;
import org.wildid.app.WildID;
import org.wildid.app.plugin.Plugin;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class TEAMPlugin extends Plugin {

    private LanguageModel language;
    private final MenuItem syncMenuItem;

    public TEAMPlugin() {
        this.language = new LanguageModel("en");
        this.language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale("en")));
        this.setText(language.getString("TEAM-plugin-name"));
        this.setGraphic(new ImageView(new Image("resources/icons/teamnetwork.png")));
        // setup the sync up menu item
        this.syncMenuItem = new MenuItem(language.getString("TEAM-sync-up"),
                new ImageView(new Image("resources/icons/arrow_refresh.png", 16, 16, false, false)));

        this.syncMenuItem.setId("sync");
        this.getItems().add(syncMenuItem);

        // setup controller
        TEAMPluginController controller = new TEAMPluginController();
        this.syncMenuItem.setOnAction(controller);
    }

    @Override
    public String getLanguageCode() {
        return language.getLanguageCode();
    }

    @Override
    public void setLanguageCode(String languageCode) {
        this.language = new LanguageModel(languageCode);
        this.language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
        this.setText(this.language.getString("TEAM-plugin-name"));
        this.syncMenuItem.setText(this.language.getString("TEAM-sync-up"));
    }

    @Override
    public String getPluginVersion() {
        return "0.9.6";
    }

    @Override
    public String getPluginName() {
        return "Wild.ID.TEAMPlugin";
    }

    static public void main(String[] args) throws Exception {
        WildID.main(args);
    }

}
