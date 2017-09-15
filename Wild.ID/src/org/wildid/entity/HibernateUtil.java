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
package org.wildid.entity;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.util.QueryUtil;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author Kai Lin, Minh Phan
 */
public class HibernateUtil {

    private static final SessionFactory sessionFactory;
    private static final int PORT = 3307;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String DBNAME = "Wild_ID";
    private static MysqldResource mysqldResource;
    private static Logger log = Logger.getLogger(org.wildid.app.Util.class.getName());

    public static boolean alreadyRunning;

    static {
        try {
            String appId = "wildid";
            try {
                JUnique.acquireLock(appId, new MessageHandler() {
                    @Override
                    public String handle(String message) {
                        // A brand new argument received! Handle it!
                        return null;
                    }
                });
                alreadyRunning = false;
            } catch (AlreadyLockedException e) {
                alreadyRunning = true;
            }

            if (!alreadyRunning) {
                // Start sequence here

                File ourAppDir = Paths.get("").toFile();
                File databaseDir = new File(ourAppDir.getAbsolutePath() + File.separatorChar + "mysql-mxj");

                mysqldResource = startDatabase(databaseDir, PORT, USERNAME, PASSWORD);

                Connection conn = null;
                try {
                    String url = "jdbc:mysql://localhost:" + PORT + "/" + DBNAME
                            + "?" + "createDatabaseIfNotExist=true&tmpTableSize=1G";
                    conn = DriverManager.getConnection(url, USERNAME, PASSWORD);

                    //String sql = "SELECT VERSION()";
                    String sql = "SET GLOBAL tmp_table_size = 1024 * 1024 * 1024;";
                    conn.createStatement().execute(sql);

                    sql = "SET GLOBAL max_heap_table_size = 1024 * 1024 * 1024;";
                    conn.createStatement().execute(sql);

                    sql = "SELECT count(*) FROM information_schema.TABLES WHERE (TABLE_SCHEMA = 'WILD_ID')";
                    String queryForString = new QueryUtil(conn).queryForString(sql);

                    //log.info(sql);
                    log.info(queryForString);

                    if (queryForString.equals("0")) {
                        ScriptRunner runner = new ScriptRunner(conn, false, false);
                        String[] filenames = new String[]{
                            "Wild_ID_age.sql",
                            "Wild_ID_age_data.sql",
                            "Wild_ID_continent.sql",
                            "Wild_ID_country.sql",
                            "Wild_ID_role.sql",
                            "Wild_ID_organization.sql",
                            "Wild_ID_person.sql",
                            "Wild_ID_project_status.sql",
                            "Wild_ID_project.sql",
                            "Wild_ID_project_organization.sql",
                            "Wild_ID_project_person_role.sql",
                            "Wild_ID_camera_trap_array.sql",
                            "Wild_ID_camera_trap.sql",
                            "Wild_ID_camera_model.sql",
                            "Wild_ID_camera.sql",
                            "Wild_ID_event.sql",
                            "Wild_ID_bait_type.sql",
                            "Wild_ID_feature_type.sql",
                            "Wild_ID_failure_type.sql",
                            "Wild_ID_deployment.sql",
                            "Wild_ID_dual_deployment.sql",
                            "Wild_ID_image_feature.sql",
                            "Wild_ID_camera_model_exif_feature.sql",
                            "Wild_ID_image_type.sql",
                            "Wild_ID_image_uncertainty_type.sql",
                            "Wild_ID_image_sequence.sql",
                            "Wild_ID_image.sql",
                            "Wild_ID_family_genus_species.sql",
                            "Wild_ID_taxa_common_name_eng_table.sql",
                            "Wild_ID_taxa_common_name_eng_data.sql",
                            "Wild_ID_homo_sapiens_type.sql",
                            "Wild_ID_image_exif.sql",
                            "Wild_ID_camera_model_extend_tag.sql",
                            "Wild_ID_image_exif_extend.sql",
                            "Wild_ID_taxa.sql",
                            "Wild_ID_taxa_unknown.sql",
                            "Wild_ID_preference.sql",
                            "Wild_ID_preference_data.sql",
                            "Wild_ID_image_species.sql",
                            "Wild_ID_image_individual.sql",
                            "Wild_ID_country_data.sql",
                            "Wild_ID_homo_sapiens_type_data.sql",
                            "Wild_ID_project_status_data.sql",
                            "Wild_ID_role_data.sql",
                            "Wild_ID_bait_type_data.sql",
                            "Wild_ID_failure_type_data.sql",
                            "Wild_ID_feature_type_data.sql",
                            "Wild_ID_image_feature_data.sql",
                            "Wild_ID_image_type_data.sql",
                            "Wild_ID_image_uncertainty_type_data.sql",
                            "Wild_ID_camera_model_data.sql"
                        };

                        for (String filename : filenames) {
                            Reader reader = new InputStreamReader(HibernateUtil.class.getResourceAsStream("/resources/sql/" + filename), "UTF-8");
                            runner.runScript(new BufferedReader(reader));
                        }
                    }

                } finally {
                    try {
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            // Create the SessionFactory from standard (hibernate.cfg.xml) 
            // config file.
            AnnotationConfiguration config = new AnnotationConfiguration();
            config.setNamingStrategy(new ImprovedNamingStrategy());
            sessionFactory = config.configure().buildSessionFactory();

        } catch (Throwable ex) {
            ex.printStackTrace();
            // Log the exception. 
            log.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static MysqldResource startDatabase(File databaseDir, int port, String userName, String password) {
        mysqldResource = new MysqldResource(databaseDir);
        Map database_options = new HashMap();
        database_options.put(MysqldResourceI.PORT, Integer.toString(port));
        database_options.put(MysqldResourceI.INITIALIZE_USER, "true");
        database_options.put(MysqldResourceI.INITIALIZE_USER_NAME, userName);
        database_options.put(MysqldResourceI.INITIALIZE_PASSWORD, password);

        mysqldResource.start("test-mysqld-thread", database_options);
        if (!mysqldResource.isRunning()) {
            throw new RuntimeException("Could not start MySQL.");
        }
        log.info("MySQL is running.");
        return mysqldResource;
    }

    public static void shutdown() {
        mysqldResource.shutdown();
    }

}
