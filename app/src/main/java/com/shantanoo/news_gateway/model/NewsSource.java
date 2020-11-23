package com.shantanoo.news_gateway.model;

import java.io.Serializable;

/**
 * Created by Shantanoo on 11/22/2020.
 */
public class NewsSource implements Serializable {

    private String id;
    private String name;
    private String category;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "NewsSource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}