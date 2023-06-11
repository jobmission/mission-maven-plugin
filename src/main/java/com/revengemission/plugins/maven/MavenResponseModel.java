package com.revengemission.plugins.maven;

import java.util.List;

public class MavenResponseModel {
    private Integer numFound;
    private List<MavenDocModel> docs;

    public Integer getNumFound() {
        return numFound;
    }

    public void setNumFound(Integer numFound) {
        this.numFound = numFound;
    }

    public List<MavenDocModel> getDocs() {
        return docs;
    }

    public void setDocs(List<MavenDocModel> docs) {
        this.docs = docs;
    }
}
