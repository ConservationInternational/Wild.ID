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
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.Locale;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.JOptionPane;
import org.wildid.entity.CameraModel;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.Organization;
import org.wildid.entity.Person;
import org.wildid.entity.Preference;
import org.wildid.entity.Project;
import org.wildid.service.CameraModelService;
import org.wildid.service.CameraModelServiceImpl;
import org.wildid.service.OrganizationService;
import org.wildid.service.OrganizationServiceImpl;
import org.wildid.service.PersonService;
import org.wildid.service.PersonServiceImpl;
import org.wildid.service.PreferenceService;
import org.wildid.service.PreferenceServiceImpl;
import org.wildid.service.ProjectService;
import org.wildid.service.ProjectServiceImpl;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildID extends Application {

    public static Preference preference;
    public static WildIDProperties wildIDProperties;
    public static final String VERSION = "0.9.28";
    public static final String WILDID_SERVER = "http://wildid.teamnetwork.org/";

    static {
        PreferenceService prefService = new PreferenceServiceImpl();
        preference = prefService.listPreference();
        wildIDProperties = new WildIDProperties();
        Util.getProperties();
    }

    // language used in the display 
    private final LanguageModel language = new LanguageModel(preference.getLanguage());
    private final ProjectService projectService = new ProjectServiceImpl();
    private final PersonService personService = new PersonServiceImpl();
    private final OrganizationService orgService = new OrganizationServiceImpl();
    private final CameraModelService cameraModelService = new CameraModelServiceImpl();

    // model of the display
    private final ObservableList<Project> projects = FXCollections.observableList(projectService.listProject());
    private final ObservableList<Person> persons = FXCollections.observableList(personService.listPerson());
    private final ObservableList<Organization> orgs = FXCollections.observableList(orgService.listOrganization());
    private final ObservableList<CameraModel> cameraModels = FXCollections.observableList(cameraModelService.listCameraModel());

    // view of the display
    private final WildIDPane mainPane = new WildIDPane(language, projects, orgs, persons, cameraModels);

    // controller of the display
    private final WildIDController controller = new WildIDController(mainPane, projects, orgs, persons);

    public static void main(String[] args) throws Exception {

        // A Workaround to fix the issue with ComboBox freezing on Win10, touch screen
        // http://stackoverflow.com/questions/31786980/javafx-windows-10-combobox-error
        System.setProperty("glass.accessible.force", "false");

        if (!HibernateUtil.alreadyRunning) {
            if (!ExifTool.exists()) {
                ExifTool.installExifTool();
            }
            Application.launch(args);
        } else {
            String langCode = Locale.getDefault().getLanguage();
            if (!langCode.equals("cn") && !langCode.equals("en")
                    && !langCode.equals("es") && !langCode.equals("pt") && !langCode.equals("fr")) {
                langCode = "en";
            }

            LanguageModel lang = new LanguageModel(langCode);

            JOptionPane.showMessageDialog(null,
                    lang.getString("wild_id_running_error_text"),
                    lang.getString("title_error"),
                    JOptionPane.ERROR_MESSAGE);
            Platform.exit();
            System.exit(0);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {

        //Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {});
        // Get current screen of the stage      
        // Screen screen = Screen.getPrimary();
        // Rectangle2D bounds = screen.getVisualBounds();
        stage.setTitle("Wild.ID " + VERSION);
        stage.getIcons().add(new javafx.scene.image.Image("resources/icons/wildId32.png"));
        Scene scene = new Scene(mainPane, 1280, 720);
        stage.setScene(scene);

        String style = preference.getStyle();
        if (style.equals("Caspian")) {
            Application.setUserAgentStylesheet(STYLESHEET_CASPIAN);
        } else {
            Application.setUserAgentStylesheet(STYLESHEET_MODENA);
        }

        // shutdown when the stage is close
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                shutdown();
            }
        });

        stage.show();

        if (!wildIDProperties.getRegistered()) {
            controller.register();
        }
    }

    public static void shutdown() {
        Platform.exit();
        HibernateUtil.shutdown();
        System.exit(0);
    }

    public static void restart() {

        try {
            StringBuilder cmd = new StringBuilder();
            String os = System.getProperty("os.name");

            if (os.contains("Mac") && Paths.get("").toFile().getCanonicalPath().startsWith("/Applications/Wild.ID")) {
                cmd.append(Paths.get("").toFile().getCanonicalPath());
                cmd.append("/../MacOS/Wild.ID");
            } else if (os.contains("Windows") && Paths.get("").toFile().getCanonicalPath().endsWith("Wild.ID\\app")) {
                //C:\Users\USER_ACCOUNT\AppData\Local\Wild.ID\app\
                String appDir = Paths.get("").toFile().getCanonicalPath();
                String exe_path = appDir.substring(0, appDir.length() - 3) + "Wild.ID.exe";
                cmd.append(exe_path);
            } else {
                cmd.append(System.getProperty("java.home")).append(File.separator).append("bin").append(File.separator).append("java ");

                for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    cmd.append(jvmArg).append(" ");
                }

                cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
                cmd.append(WildID.class.getName()).append(" ");
            }
            Runtime.getRuntime().exec(cmd.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

}
