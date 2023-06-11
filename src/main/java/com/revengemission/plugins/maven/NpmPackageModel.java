package com.revengemission.plugins.maven;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NpmPackageModel {
    @JsonProperty(value = "package")
    private NpmPackageDetailModel packageDetailModel;

    private Double searchScore;

    public NpmPackageDetailModel getPackageDetailModel() {
        return packageDetailModel;
    }

    public void setPackageDetailModel(NpmPackageDetailModel packageDetailModel) {
        this.packageDetailModel = packageDetailModel;
    }

    public Double getSearchScore() {
        return searchScore;
    }

    public void setSearchScore(Double searchScore) {
        this.searchScore = searchScore;
    }
}
