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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.Set;
import javafx.concurrent.Task;
import javafx.scene.control.Menu;
import org.apache.log4j.Logger;
import org.wildid.app.plugin.Plugin;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDPluginTask extends Task<Void> {

    private double val = 0;
    private final double max = 100;
    private final LanguageModel language;
    static Logger log = Logger.getLogger(WildIDPluginTask.class.getName());
    private boolean success = true;
    private final String pluginName;
    private final WildIDPluginPane pluginPane;
    private final String action;
    private String errorMsg;

    public WildIDPluginTask(
            LanguageModel language,
            WildIDPluginPane pluginPane,
            String pluginName,
            String action) {

        this.language = language;
        this.pluginName = pluginName;
        this.pluginPane = pluginPane;
        this.action = action;
    }

    @Override
    public Void call() {
        updateProgress(0, max);
        try {
            if (this.action.equals("install")) {
                installPlugin();
            } else if (this.action.equals("uninstall")) {
                uninstallPlugin();
            } else if (this.action.equals("update")) {
                updatePlugin();
            } else {
                this.errorMsg = "Unknown action with plugin";
                log.error(this.errorMsg);
                cancel();
            }

            if (!this.success) {
                log.error(this.errorMsg);
                cancel();
            }

        } catch (Exception ex) {
            this.errorMsg = ex.getMessage();
            log.error(this.errorMsg);
            cancel();
        }

        return null;
    }

    public void increaseDone(double percent) {
        this.val += percent;
        if (this.val > this.max) {
            this.val = this.max;
        }

        updateProgress(this.val, this.max);
    }

    public void finished(String text) {
        updateMessage(text);
    }

    public void uninstallPlugin() {
        try {
            // now 5%
            this.increaseDone(5);

            File appDir = Paths.get("").toFile();
            File pluginDir = new File(appDir.getAbsolutePath() + File.separatorChar + "plugin");

            if (pluginDir.exists()) {
                String jarFile = this.pluginName + ".jar";
                File jarFileObj = new File(pluginDir, jarFile);

                boolean ok = false;

                if (jarFileObj.delete()) {
                    ok = true;
                    log.info(jarFile + " file deleted");
                } else {
                    log.info(jarFile + " file could not be deleted, do work around");

                    // Workaround for Windows OS
                    String uninstalljarFile = jarFile + ".uninstall";
                    File uninstalljarFileObj = new File(pluginDir, uninstalljarFile);
                    new FileOutputStream(uninstalljarFileObj).close();

                    if (uninstalljarFileObj.exists()) {
                        ok = true;
                        log.info(uninstalljarFile + " file created");
                    }
                }

                if (ok) {
                    this.increaseDone(90);

                    WildIDMenuBar menubar = this.pluginPane.getWildIDMenuBar();
                    Menu menuPlugin = menubar.getMenuPlugin();

                    Set<Plugin> plugins = menubar.getInstalledPlugins();
                    for (Plugin plugin : plugins) {
                        if (plugin.getPluginName().equals(this.pluginName)) {
                            plugins.remove(plugin);
                            menuPlugin.getItems().remove(plugin);
                            break;
                        }
                    }

                } else {
                    this.success = false;
                    this.errorMsg = "Error: Could not uninstall plugin.";
                }
            }
        } catch (Exception ex) {
            this.success = false;
            this.errorMsg = ex.getMessage();
        }
    }

    public void updatePlugin() {
        FileOutputStream fos = null;

        try {
            // now 5%
            this.increaseDone(5);

            File appDir = Paths.get("").toFile();
            File pluginDir = new File(appDir.getAbsolutePath() + File.separatorChar + "plugin");

            if (!pluginDir.exists()) {
                pluginDir.mkdirs();
            }

            // now 10%
            this.increaseDone(5);

            String jarFile = this.pluginName + ".jar";
            File jarFileObj = new File(pluginDir, jarFile);

            String tmpJarFile = this.pluginName + ".jar.update";
            File tmpJarFileObj = new File(pluginDir, tmpJarFile);

            boolean ok = false;

            URL jarUrl = new URL(WildID.WILDID_SERVER + "plugin/" + this.pluginName + "/" + jarFile);
            log.info(jarUrl);

            ReadableByteChannel rbc = Channels.newChannel(jarUrl.openStream());
            fos = new FileOutputStream(tmpJarFileObj);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            log.info("Done writing jar: " + tmpJarFileObj.getAbsolutePath());

            if (tmpJarFileObj.exists()) {
                ok = true;

                if (jarFileObj.exists()) {
                    if (jarFileObj.delete()) {
                        if (tmpJarFileObj.renameTo(jarFileObj)) {
                            log.info("Successful update jar file: " + jarFileObj.getAbsolutePath());
                        }
                    }
                }
            }

            if (!ok) {
                this.success = false;
                this.errorMsg = "Error: Could not update plugin.";
            }
            // now 100%
            this.increaseDone(90);
        } catch (Exception ex) {
            this.success = false;
            this.errorMsg = ex.getMessage();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.info(e.getMessage());
                }
            }
        }
    }

    public void installPlugin() {
        FileOutputStream fos = null;

        try {
            // now 5%
            this.increaseDone(5);

            File appDir = Paths.get("").toFile();
            File pluginDir = new File(appDir.getAbsolutePath() + File.separatorChar + "plugin");

            if (!pluginDir.exists()) {
                pluginDir.mkdirs();
            }

            // now 10%
            this.increaseDone(5);

            String jarFile = this.pluginName + ".jar";
            File jarFileObj = new File(pluginDir, jarFile);

            URL jarUrl = new URL(WildID.WILDID_SERVER + "plugin/" + this.pluginName + "/" + jarFile);
            log.info(jarUrl);

            ReadableByteChannel rbc = Channels.newChannel(jarUrl.openStream());
            fos = new FileOutputStream(jarFileObj);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            log.info("Done writing jar: " + jarFileObj.getAbsolutePath());

            String updateJarFile = this.pluginName + ".jar.update";
            File updateJarFileObj = new File(pluginDir, updateJarFile);
            if (updateJarFileObj.exists()) {
                updateJarFileObj.delete();
            }

            String uninstallJarFile = this.pluginName + ".jar.uninstall";
            File uninstallJarFileObj = new File(pluginDir, uninstallJarFile);
            if (uninstallJarFileObj.exists()) {
                uninstallJarFileObj.delete();
            }

            // now 100%
            this.increaseDone(90);
        } catch (Exception ex) {
            this.success = false;
            this.errorMsg = ex.getMessage();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.info(e.getMessage());
                }
            }
        }
    }

    public String getErrorMessage() {
        if (this.errorMsg != null) {
            return this.errorMsg;
        } else {
            return "Error: Unknown error";
        }
    }
}
