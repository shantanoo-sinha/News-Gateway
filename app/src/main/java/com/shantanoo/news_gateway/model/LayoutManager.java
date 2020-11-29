package com.shantanoo.news_gateway.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shantanoo on 11/27/2020.
 */
public class LayoutManager implements Serializable {

    private int source;
    private int article;
    private List<String> categories = new ArrayList<>();
    private List<NewsSource> sources = new ArrayList<>();
    private List<NewsArticle> articles = new ArrayList<>();

    public void setSource(int source) {
        this.source = source;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<NewsSource> getSources() {
        return sources;
    }

    public void setSources(List<NewsSource> sources) {
        this.sources = sources;
    }

    public List<NewsArticle> getArticles() {
        return articles;
    }

    public void setArticles(List<NewsArticle> articles) {
        this.articles = articles;
    }
}
