package org.wildid.entity;

public class VersionUpdate {

    private Integer updateId;
    private String oldVersion;
    private String newVersion;
    private String sqlFile;
    private String jarFile;
    private String libFile;
    private String logFile;
    private String startTimestamp;
    private boolean canUpgrade;

    public Integer getUpdateId() {
        return this.updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    public String getOldVersion() {
        return this.oldVersion;
    }

    public void setOldVersion(String oldVersion) {
        this.oldVersion = oldVersion;
    }

    public String getNewVersion() {
        return this.newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public String getSqlFile() {
        return this.sqlFile;
    }

    public void setSqlFile(String sqlFile) {
        this.sqlFile = sqlFile;
    }

    public String getJarFile() {
        return this.jarFile;
    }

    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
    }

    public String getLibFile() {
        return this.libFile;
    }

    public void setLibFile(String libFile) {
        this.libFile = libFile;
    }

    public String getLogFile() {
        return this.logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public String getStartTimestamp() {
        return this.startTimestamp;
    }

    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public boolean getCanUpgrade() {
        return this.canUpgrade;
    }

    public void setCanUpgrade(boolean canUpgrade) {
        this.canUpgrade = canUpgrade;
    }
}
