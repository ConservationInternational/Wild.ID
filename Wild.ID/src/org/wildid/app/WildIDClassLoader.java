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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

/**
 *
 * @author Kai Lin, Minh Phan
 */
public class WildIDClassLoader extends ClassLoader {

    /**
     * This constructor is used to set the parent ClassLoader
     *
     * @param parent
     */
    public WildIDClassLoader(ClassLoader parent) {

        super(parent);
        File ourAppDir = Paths.get("").toFile();
        File pluginDir = new File(ourAppDir.getAbsolutePath() + File.separatorChar + "plugin");

        if (pluginDir.exists()) {
            String[] jarNames = pluginDir.list();
            if (jarNames != null) {
                for (String jarName : jarNames) {

                    if (!jarName.endsWith(".jar")) {
                        continue;
                    }

                    File pluginJarFileObj = new File(pluginDir, jarName);
                    File uninstallJarFileObj = new File(pluginDir, jarName + ".uninstall");
                    File updateJarFileObj = new File(pluginDir, jarName + ".update");

                    if (uninstallJarFileObj.exists()) {
                        if (pluginJarFileObj.exists()) {
                            pluginJarFileObj.delete();
                        }

                        uninstallJarFileObj.delete();
                        continue;
                    }

                    if (updateJarFileObj.exists()) {
                        if (pluginJarFileObj.exists()) {
                            pluginJarFileObj.delete();
                        }

                        updateJarFileObj.renameTo(pluginJarFileObj);
                    }

                    try {
                        URLClassLoader urlClassLoader = (URLClassLoader) parent;
                        Method m = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
                        m.setAccessible(true);
                        m.invoke(urlClassLoader, pluginJarFileObj.toURI().toURL());

                        String cp = System.getProperty("java.class.path");
                        if (cp != null) {
                            try {
                                cp += File.pathSeparatorChar + pluginJarFileObj.getCanonicalPath();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            cp = pluginJarFileObj.toURI().getPath();
                        }
                        System.setProperty("java.class.path", cp);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Every request for a class passes through this method.
     *
     *
     * @param name Full class name
     * @return
     * @throws java.lang.ClassNotFoundException
     */
    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }
}
