package com.revengemission.plugins.maven;

import java.util.List;

public class NpmReleaseModel {
    private List<NpmPackageModel> objects;

    public List<NpmPackageModel> getObjects() {
        return objects;
    }

    public void setObjects(List<NpmPackageModel> objects) {
        this.objects = objects;
    }
}
