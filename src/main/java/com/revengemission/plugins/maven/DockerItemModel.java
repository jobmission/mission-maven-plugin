package com.revengemission.plugins.maven;

public class DockerItemModel {
    private Long id;
    private String name;
    private Long repository;
    private Long full_size;
    private String last_updated;
    private String tag_last_pushed;

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

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }

    public String getTag_last_pushed() {
        return tag_last_pushed;
    }

    public void setTag_last_pushed(String tag_last_pushed) {
        this.tag_last_pushed = tag_last_pushed;
    }
}
