package com.shantanoo.news_gateway.service;

import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;

import com.shantanoo.news_gateway.MainActivity;
import com.shantanoo.news_gateway.model.NewsSource;

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
 * Created by Shantanoo on 11/22/2020.
 */
public class SourceDownloader implements Runnable {
    private static final String TAG = "SourceDownloader";

    private static final String API_KEY = "5b944cc9c74f46bfae60f39673108944";
    private static final String BASE_URL = "https://newsapi.org/v2/sources?language=en&country=us";
    private static final String CATEGORY_TOKEN = "&category=";
    private static final String API_KEY_TOKEN = "&apiKey=";

    private String newsCategory;
    private List<String> newsCategories;
    private List<NewsSource> newsSources;

    private MainActivity mainActivity;

    public SourceDownloader(MainActivity mainActivity, String category) {
        this.mainActivity = mainActivity;
        newsCategory = category;

        newsSources = new ArrayList<>();
        newsCategories = new ArrayList<>();
    }

    @Override
    public void run() {
        Log.d(TAG, "run: Downloading Sources");
        String DOWNLOAD_LINK = initURL();

        Uri uri = Uri.parse(DOWNLOAD_LINK);
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
        String DOWNLOAD_LINK = BASE_URL + CATEGORY_TOKEN;

        if (!TextUtils.isEmpty(newsCategory) && !"all".equalsIgnoreCase(newsCategory))
            DOWNLOAD_LINK += newsCategory;

        DOWNLOAD_LINK += API_KEY_TOKEN + API_KEY;
        return DOWNLOAD_LINK;
    }

    public void handleResults(final String jsonString) {
        Log.d(TAG, "handleResults: Populating Sources and Categories in MainActivity");
        parseJSON(jsonString);
        mainActivity.runOnUiThread(() -> mainActivity.populateSourceAndCategory(newsCategories, newsSources));
    }

    private void parseJSON(String input) {
        Log.d(TAG, "parseJSON: Parsing Source JSON data");
        try {
            JSONObject jsonObject = new JSONObject(input);
            JSONArray jsonArray = jsonObject.getJSONArray("sources");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject sourceJsonObject = jsonArray.getJSONObject(i);
                NewsSource source = new NewsSource();
                source.setId(sourceJsonObject.getString("id"));
                source.setName(sourceJsonObject.getString("name"));
                source.setCategory(sourceJsonObject.getString("category"));
                source.setUrl(sourceJsonObject.getString("url"));
                source.setColoredName(new SpannableString(source.getName()));

                newsSources.add(source);
                if (!newsCategories.contains(source.getCategory()))
                    newsCategories.add(source.getCategory());
            }
        } catch (Exception e) {
            Log.e(TAG, "parseJSON: Failed to parse JSON", e);
        }
    }
}
