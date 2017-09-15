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

public class WildIDProperties {

    private String uuid = "";
    private String workingDir = "";
    private boolean enableImageIndividual = false;
    private boolean enableSubspecies = false;
    private int openedProject = -1;
    private boolean registered = false;

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getWorkingDir() {
        if (workingDir == null) {
            workingDir = "";
        }
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public File getWorkingDirObj() {
        if (this.workingDir == null || this.workingDir.equals("")) {
            return null;
        }

        File dirObj = new File(this.workingDir);
        if (dirObj.exists() && dirObj.isDirectory() && dirObj.canRead() && dirObj.canWrite()) {
            return dirObj;
        }

        return null;
    }

    public boolean getEnableImageIndividual() {
        return enableImageIndividual;
    }

    public void setEnableImageIndividual(boolean enableImageIndividual) {
        this.enableImageIndividual = enableImageIndividual;
    }

    public boolean getEnableSubspecies() {
        return enableSubspecies;
    }

    public void setEnableSubspecies(boolean enableSubspecies) {
        this.enableSubspecies = enableSubspecies;
    }

    public int getOpenedProject() {
        return openedProject;
    }

    public void setOpenedProject(int openedProject) {
        this.openedProject = openedProject;
    }

    public boolean getRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

}
