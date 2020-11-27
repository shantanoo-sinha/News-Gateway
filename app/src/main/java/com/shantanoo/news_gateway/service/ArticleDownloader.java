package com.shantanoo.news_gateway.service;

import android.net.Uri;
import android.util.Log;

import com.shantanoo.news_gateway.model.NewsArticle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shantanoo on 11/23/2020.
 */
public class ArticleDownloader implements Runnable {

    private static final String TAG = "ArticleDownloader";
    private static final String API_KEY = "5b944cc9c74f46bfae60f39673108944";
    private static final String BASE_URL = "https://newsapi.org/v2/top-headlines?language=en&pageSize=10";
    private static final String SOURCE_TOKEN = "&sources=";
    private static final String API_KEY_TOKEN = "&apiKey=";

    private List<NewsArticle> newsArticles;
    private String sourceId;
    private NewsService newsService;

    public ArticleDownloader(NewsService newsService, String sourceId) {
        this.newsService = newsService;
        this.sourceId = sourceId;

        newsArticles = new ArrayList<>();
    }

    @Override
    public void run() {
        Uri uri = Uri.parse(initURL());

        String line;
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(uri.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", "");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (Exception e) {
            Log.e(TAG, "run: Exception: ", e);
        }
        handleResults(sb.toString());
    }

    private String initURL() {
        return BASE_URL + SOURCE_TOKEN + sourceId + API_KEY_TOKEN + API_KEY;
    }

    public void handleResults(final String jsonString) {
        parseJSON(jsonString);
        newsService.populateArticles(newsArticles);
    }

    private void parseJSON(String input) {
        try {
            JSONObject jsonObject = new JSONObject(input);
            JSONArray jsonArray = jsonObject.getJSONArray("articles");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject articleJsonObject = jsonArray.getJSONObject(i);
                NewsArticle article = new NewsArticle();
                article.setAuthor(articleJsonObject.getString("author"));
                article.setTitle(articleJsonObject.getString("title"));
                article.setDescription(articleJsonObject.getString("description"));
                article.setUrl(articleJsonObject.getString("url"));
                article.setUrlToImage(articleJsonObject.getString("urlToImage"));
                article.setPublishedAt(articleJsonObject.getString("publishedAt"));

                newsArticles.add(article);
            }
        } catch (Exception e) {
            Log.e(TAG, "parseJSON: Failed to parse JSON", e);
        }
    }
}