package com.revengemission.plugins.maven;

public class MavenMetaData {
    private String groupId;
    private String artifactId;
    private long lastUpdated;
    private MavenVersioning versioning;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public MavenVersioning getVersioning() {
        return versioning;
    }

    public void setVersioning(MavenVersioning versioning) {
        this.versioning = versioning;
    }

    @Override
    public String toString() {
        return "MavenMetaData{" +
            "groupId='" + groupId + '\'' +
            ", artifactId='" + artifactId + '\'' +
            ", lastUpdated=" + lastUpdated +
            ", versioning=" + versioning +
            '}';
    }
}
