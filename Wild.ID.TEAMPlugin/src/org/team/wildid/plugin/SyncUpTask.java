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

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.wildid.app.LanguageModel;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class SyncUpTask extends Task<Void> {

    private final LanguageModel language;
    private boolean success = true;
    private String errorMsg;
    private final TEAMPluginController pluginController;
    private final String site;

    public SyncUpTask(
            LanguageModel language,
            TEAMPluginController pluginController,
            String site) {

        this.language = language;
        this.pluginController = pluginController;
        this.site = site;
    }

    @Override
    public Void call() {
        try {
            Platform.runLater(() -> {
                this.pluginController.syncUp(site);
            });

        } catch (Exception ex) {
            this.errorMsg = ex.getMessage();
            this.success = false;
            ex.printStackTrace();
        }

        if (!this.success) {
            cancel();
        }

        return null;
    }

    public String getErrorMessage() {
        if (this.errorMsg != null) {
            return this.errorMsg;
        } else {
            return "Error: Unknown error";
        }
    }
    
    public void updatePercent(int p) {
        updateProgress(p, 100);
    }
}
