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

import java.io.File;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.wildid.entity.Person;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.wildid.app.LanguageModel;
import org.wildid.app.OrganizationEditPane;
import org.wildid.app.ProjectCameraPane;
import org.wildid.app.ProjectCameraTrapArrayPane;
import org.wildid.app.ProjectCameraTrapPane;
import org.wildid.app.ProjectEditOrganizationPane;
import org.wildid.app.ProjectEditPane;
import org.wildid.app.ProjectEditPersonPane;
import org.wildid.app.ProjectEventPane;
import org.wildid.app.ProjectNewOrganizationPane;
import org.wildid.app.ProjectNewPersonPane;
import org.wildid.app.WildID;
import org.wildid.app.plugin.PluginException;
import org.wildid.entity.Deployment;
import org.wildid.entity.Organization;
import org.wildid.entity.Project;

/**
 *
 * @author Kai Lin, Minh Phan
 */

@Aspect
public class TEAMAspects {

    @Around("execution(private void org.wildid.app.WildIDController.updateProject(org.wildid.app.ProjectEditPane)) ")
    public void updateProject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectEditPane pane = (ProjectEditPane) args[0];
        Project project = pane.getOriginalProject();
        if (project.getProjectId() > 0 || fromTEAMPlugin()) {
            joinPoint.proceed();
        } else {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        }
    }

    @Around("execution(private void org.wildid.app.WildIDController.createNewPersonInProject(org.wildid.app.ProjectNewPersonPane))")
    public void createNewPersonInProject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectNewPersonPane pane = (ProjectNewPersonPane) args[0];
        Project project = pane.getProject();
        if (project.getProjectId() > 0 || fromTEAMPlugin()) {
            joinPoint.proceed();
        } else {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        }
    }

    @Around("execution(private void org.wildid.app.WildIDController.addPersonToProject(org.wildid.app.ProjectNewPersonPane))")
    public void addPersonToProject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectNewPersonPane pane = (ProjectNewPersonPane) args[0];
        Project project = pane.getProject();
        if (project.getProjectId() > 0 || fromTEAMPlugin()) {
            joinPoint.proceed();
        } else {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        }
    }

    @Around("execution(private void org.wildid.app.WildIDController.updatePersonInProject(org.wildid.app.ProjectEditPersonPane))")
    public void updatePersonInProject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectEditPersonPane pane = (ProjectEditPersonPane) args[0];
        Project project = pane.getProject();
        if (project.getProjectId() > 0 || fromTEAMPlugin()) {
            joinPoint.proceed();
        } else {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        }
    }

    @Around("execution(private void org.wildid.app.WildIDController.deletePersonFromProject(org.wildid.app.ProjectEditPersonPane))")
    public void removeProjectPersonRoles(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectEditPersonPane pane = (ProjectEditPersonPane) args[0];
        Project project = pane.getProject();
        Person person = pane.getOriginalPerson();
        if (project.getProjectId() < 0 && person.getPersonId() < 0 && !fromTEAMPlugin()) {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        } else {
            joinPoint.proceed();
        }
    }

    @Around("execution(public void org.wildid.app.WildIDController.*(org.wildid.app.ProjectEventPane))")
    public void editEvent(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectEventPane pane = (ProjectEventPane) args[0];
        Project project = pane.getProject();
        if (project.getProjectId() < 0 && !fromTEAMPlugin()) {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        } else {
            joinPoint.proceed();
        }
    }

    @Around("execution(private void org.wildid.app.WildIDController.*(org.wildid.app.ProjectCameraTrapArrayPane))")
    public void editCameraTrapArray(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectCameraTrapArrayPane pane = (ProjectCameraTrapArrayPane) args[0];
        Project project = pane.getProject();
        if (project.getProjectId() < 0 && !fromTEAMPlugin()) {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        } else {
            joinPoint.proceed();
        }
    }

    @Around("execution(private void org.wildid.app.WildIDController.*(org.wildid.app.ProjectCameraTrapPane))")
    public void editCameraTrap(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectCameraTrapPane pane = (ProjectCameraTrapPane) args[0];
        Project project = pane.getCameraTrapArray().getProject();
        if (project.getProjectId() < 0 && !fromTEAMPlugin()) {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        } else {
            joinPoint.proceed();
        }
    }

    @Around("execution(private void org.wildid.app.WildIDController.*(org.wildid.app.ProjectCameraPane))")
    public void editCamera(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectCameraPane pane = (ProjectCameraPane) args[0];
        Project project = pane.getCamera().getProject();
        if (project.getProjectId() < 0 && !fromTEAMPlugin()) {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        } else {
            joinPoint.proceed();
        }
    }

    @Around("execution(private void org.wildid.app.WildIDController.*(org.wildid.app.ProjectNewOrganizationPane))")
    public void addNewOrganizationToProject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectNewOrganizationPane pane = (ProjectNewOrganizationPane) args[0];
        Project project = pane.getProject();
        if (project.getProjectId() < 0 && !fromTEAMPlugin()) {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        } else {
            joinPoint.proceed();
        }
    }

    @Around("execution(private void org.wildid.app.WildIDController.*(org.wildid.app.ProjectEditOrganizationPane))")
    public void editOrganizationInProject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ProjectEditOrganizationPane pane = (ProjectEditOrganizationPane) args[0];
        Project project = pane.getProject();
        if (project.getProjectId() < 0 && !fromTEAMPlugin()) {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-project-edit-error", language);
        } else {
            joinPoint.proceed();
        }
    }

    @Around("execution(public void org.wildid.app.WildIDController.updateOrganization(org.wildid.app.OrganizationEditPane))")
    public void editOrganization(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        OrganizationEditPane pane = (OrganizationEditPane) args[0];
        Organization org = pane.getOrganization();
        if (org.getOrganizationId() < 0 && !fromTEAMPlugin()) {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-organization-edit-error", language);
        } else {
            joinPoint.proceed();
        }
    }

    @Around("execution(public void org.wildid.service.PersonService.updatePerson(org.wildid.entity.Person))")
    public void updatePerson(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Person person = (Person) args[0];
        if (person.getPersonId() > 0 || fromTEAMPlugin()) {
            joinPoint.proceed();
        } else {
            LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
            language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
            throw new PluginException("TEAM-person-edit-error", language);
        }
    }

    @Around("execution(org.wildid.app.LoadZipInCurrentProjectTask.new(..))")
    public void loadTransferFile(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        File zipFile = (File) args[1];
        Project project = (Project) args[3];
        if (project.getProjectId() < 0) {
            Integer projectIdInZip = null;
            ZipFile archive = new ZipFile(zipFile);
            Enumeration e = archive.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                String name = entry.getName();
                if (name.endsWith(".xml")) {
                    JAXBElement<Deployment> df;
                    JAXBContext context = JAXBContext.newInstance(Deployment.class);
                    Unmarshaller unmarshaller = context.createUnmarshaller();
                    df = unmarshaller.unmarshal(new StreamSource(new InputStreamReader(archive.getInputStream(entry), "UTF8")),
                            Deployment.class);
                    Deployment deployment = df.getValue();
                    projectIdInZip = deployment.getEvent().getProject().getProjectId();
                    break;
                }
            }

            if (projectIdInZip != null && projectIdInZip.intValue() == project.getProjectId().intValue()) {
                joinPoint.proceed();
            } else {
                LanguageModel language = new LanguageModel(WildID.preference.getLanguage());
                language.setResourceBundle(ResourceBundle.getBundle("org.team.wildid.plugin.label", new Locale(language.getLanguageCode())));
                throw new PluginException("TEAM-load-transfer-file-error", language);
            }
        } else {
            joinPoint.proceed();
        }
    }

    private boolean fromTEAMPlugin() {
        boolean fromTEAMPlugin = false;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : elements) {
            if (element.getClassName().equals("org.team.wildid.plugin.TEAMPluginController")) {
                fromTEAMPlugin = true;
                break;
            }
        }
        return fromTEAMPlugin;
    }

}
