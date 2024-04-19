package com.revengemission.plugins.maven;

import java.util.List;

public class MavenVersioning {
    /**
     * Aliyun 无此字段
     */
    private String latest;
    private String release;
    private String lastUpdated;
    private List<MavenVersion> versions;


    public String getLatest() {
        return latest;
    }

    public void setLatest(String latest) {
        this.latest = latest;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<MavenVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<MavenVersion> versions) {
        this.versions = versions;
    }

    @Override
    public String toString() {
        return "MavenVersioning{" +
            "latest='" + latest + '\'' +
            ", release='" + release + '\'' +
            ", lastUpdated='" + lastUpdated + '\'' +
            ", versions=" + versions +
            '}';
    }
}
