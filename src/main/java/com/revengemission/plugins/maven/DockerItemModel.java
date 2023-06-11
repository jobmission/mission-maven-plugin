package com.revengemission.plugins.maven;

public class DockerItemModel {
    private Long id;
    private String name;
    private Long repository;
    private Long full_size;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRepository() {
        return repository;
    }

    public void setRepository(Long repository) {
        this.repository = repository;
    }

    public Long getFull_size() {
        return full_size;
    }

    public void setFull_size(Long full_size) {
        this.full_size = full_size;
    }
}
