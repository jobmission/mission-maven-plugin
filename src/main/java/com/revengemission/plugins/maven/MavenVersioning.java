package com.revengemission.plugins.maven;

import java.util.List;

public class MavenVersioning {
    private String latest;
    private String release;
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
            ", versions=" + versions +
            '}';
    }
}
