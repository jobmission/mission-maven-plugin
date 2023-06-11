package com.revengemission.plugins.maven;

import java.util.List;

public class DockerReleaseModel {
    private Integer count;
    List<DockerItemModel> results;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<DockerItemModel> getResults() {
        return results;
    }

    public void setResults(List<DockerItemModel> results) {
        this.results = results;
    }
}
