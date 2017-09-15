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
package org.wildid.app.plugin;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import org.wildid.app.WildIDMenuBar;
import org.wildid.app.WildIDPane;

public abstract class Plugin extends Menu {

    // change the language
    abstract public void setLanguageCode(String languageCode);

    // get the current language
    abstract public String getLanguageCode();

    // get the current version
    abstract public String getPluginVersion();

    // get the current version
    abstract public String getPluginName();

    // get top WildID Pane
    public WildIDPane getWildIDPane() {
        WildIDMenuBar menubar = (WildIDMenuBar) this.getProperties().get("WildIDMenubar");
        if (menubar != null) {
            Node node = menubar.getParent();
            while (!(node instanceof WildIDPane)) {
                node = node.getParent();
            }
            return (WildIDPane) node;
        } else {
            return null;
        }
    }

}
