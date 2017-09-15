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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.ScriptRunner;
import org.wildid.entity.VersionUpdate;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDUpdateTask extends Task<Void> {

    private double val = 0;
    private final double max = 100;
    private final LanguageModel language;
    private String upgradeVersion;
    private final String uuid;
    static Logger log = Logger.getLogger(WildIDUpdateTask.class.getName());
    private boolean success = true;
    private final List<VersionUpdate> versionUpdates;
    private String errorMsg = null;

    public WildIDUpdateTask(
            LanguageModel language,
            WildIDUpdatePane updatePane) {

        this.language = language;
        this.uuid = updatePane.getUUID();
        this.versionUpdates = updatePane.getVersionUpdateList();
    }

    @Override
    public Void call() {
        updateProgress(0, max);
        try {
            Session s = HibernateUtil.getSessionFactory().openSession();
            s.doWork(connection -> runUpdate(connection));
            s.close();

            if (!this.success) {
                cancel();
            }

        } catch (Exception ex) {
            log.info(ex.getMessage());
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

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            log.info(ex.getMessage());
        }
    }

    public void runUpdate(Connection conn) {
        BufferedReader reader = null;
        File tmpDir = null;
        File appDir;
        File tmpRootDir;
        FileOutputStream fos = null;


        try {
            this.finished(language.getString("wildID_updateTask_getting_update_list"));
            // now 5%
            this.increaseDone(5);

            File rootObj = Paths.get("").toFile();
            appDir = new File(rootObj.getAbsolutePath());

            tmpRootDir = new File(appDir, "tmp");
            tmpDir = new File(tmpRootDir, String.valueOf(new Date().getTime()));

            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }

            this.increaseDone(5);

            ScriptRunner runner = new ScriptRunner(conn, false, false);

            this.finished(language.getString("wildID_updateTask_updating_queries"));
            // now 60%
            for (VersionUpdate versionUpdate : versionUpdates) {
                log.info("updating version: " + versionUpdate.getNewVersion());
                String update_sql = versionUpdate.getSqlFile();

                if (update_sql != null) {
                    URL sql_url = new URL(WildID.WILDID_SERVER + "versions/" + versionUpdate.getNewVersion() + "/" + update_sql);
                    log.info(sql_url);

                    reader = new BufferedReader(new InputStreamReader(sql_url.openStream(), "UTF-8"));
                    runner.runScript(reader);
                    log.info("Done update sql: " + versionUpdate.getNewVersion() + "/" + update_sql);
                }
            }

            this.finished(language.getString("wildID_updateTask_updating_jar_file"));
            // now 70%
            this.increaseDone(20);

            for (int i = versionUpdates.size() - 1; i >= 0; i--) {
                VersionUpdate versionUpdate = versionUpdates.get(i);

                String jarFile = versionUpdate.getJarFile();
                if (jarFile != null) {
                    URL jarUrl = new URL(WildID.WILDID_SERVER + "versions/" + versionUpdate.getNewVersion() + "/" + jarFile);
                    log.info(jarUrl);

                    ReadableByteChannel rbc = Channels.newChannel(jarUrl.openStream());
                    fos = new FileOutputStream("Wild.ID.jar");
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fos.close();
                    log.info("Done writing jar: " + versionUpdate.getNewVersion() + "/" + jarFile);

                    break;
                }
            }

            this.finished(language.getString("wildID_updateTask_updating_library_files"));
            // now 80%
            this.increaseDone(20);

            // A lib file could be either: a single .jar file or single .zip that hold multiple .jar
            for (VersionUpdate versionUpdate : versionUpdates) {
                String lib_file = versionUpdate.getLibFile();

                if (lib_file != null) {
                    URL libUrl = new URL(WildID.WILDID_SERVER + "versions/" + versionUpdate.getNewVersion() + "/" + lib_file);
                    log.info(libUrl);

                    ReadableByteChannel rbc = Channels.newChannel(libUrl.openStream());
                    File libFileObj = new File(tmpDir, lib_file);
                    fos = new FileOutputStream(libFileObj);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fos.close();

                    File libDir = new File(appDir, "lib");

                    if (lib_file.endsWith(".zip")) {
                        Util.unzip(libFileObj, libDir);
                        log.info("Done extract zip: " + versionUpdate.getNewVersion() + "/" + lib_file);
                    } else if (lib_file.endsWith(".jar")) {
                        Util.MoveFile(libFileObj, new File(libDir, lib_file), true);
                        log.info("Done copy jar to lib: " + versionUpdate.getNewVersion() + "/" + lib_file);
                    }
                }
            }

            this.finished(language.getString("wildID_updateTask_writing_log"));
            // now 90%
            this.increaseDone(30);

            upgradeVersion = versionUpdates.get(versionUpdates.size() - 1).getNewVersion();

            // No more read version from properties file
            //Util.updateWildIDProperties("version", upgradeVersion);
            // If the current version is "0.9.12" or lower
            if (WildID.VERSION.equals("0.9.13")) {
                log.info("Trying to overwrite Wild.ID.cfg from server");

                boolean windowsOs = System.getProperty("os.name").startsWith("Windows");
                String server_cfg_file = windowsOs ? "Wild.ID_win.cfg" : "Wild.ID_mac.cfg";
                URL cfgUrl = new URL(WildID.WILDID_SERVER + "versions/0.9.13/" + server_cfg_file);
                log.info(cfgUrl);

                ReadableByteChannel rbc = Channels.newChannel(cfgUrl.openStream());
                File tmp_cfg_file_obj = new File(tmpDir, server_cfg_file);
                File final_cfg_file_obj = new File(appDir, "Wild.ID.cfg");

                fos = new FileOutputStream(tmp_cfg_file_obj);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();

                if (tmp_cfg_file_obj.exists()) {
                    if (Util.MoveFile(tmp_cfg_file_obj, final_cfg_file_obj, true)) {
                        log.info("Done copy cfg file to working dir: " + "/" + final_cfg_file_obj.toString());
                    } else {
                        this.success = false;
                        errorMsg = "Error: couldn't move cfg file after tmp dir";
                        log.info(errorMsg);
                    }
                } else {
                    this.success = false;
                    errorMsg = "Error: couldn't see the cfg file after all";
                    log.info(errorMsg);
                }
            }

            this.finished(language.getString("wildID_updateTask_cleaning_up"));
            // now 100%
            this.increaseDone(10);
            this.finished(language.getString("wildID_updateTask_restarting"));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        } catch (MalformedURLException ex) {
            this.success = false;
            errorMsg = ex.getMessage();
            log.info(errorMsg);
        } catch (IOException ex) {
            this.success = false;
            errorMsg = ex.getMessage();
            log.info(errorMsg);
        } catch (SQLException ex) {
            this.success = false;
            errorMsg = ex.getMessage();
            log.info(errorMsg);
        } catch (Exception ex) {
            this.success = false;
            errorMsg = ex.getMessage();
            log.info(errorMsg);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.info(e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.info(e.getMessage());
                }
            }

            // delete the tmp directory
            Util.delete(tmpDir);
            saveLog(errorMsg);
        }
    }

    public void saveLog(String message) {
        try {
            String urlStr = WildID.WILDID_SERVER + "service.jsp?q=savelog&uuid="
                    + uuid + "&old_version=" + WildID.VERSION + "&new_version="
                    + upgradeVersion + "&complete=" + (success ? "true" : "false&message=" + message);
            URL url = new URL(urlStr);
            InputStream is = url.openStream();
            is.close();
        } catch (MalformedURLException ex) {
            log.info(ex.getMessage());
        } catch (IOException ex) {
            log.info(ex.getMessage());
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }
    }

    public String getErrorMessage() {
        return this.errorMsg;
    }
}
