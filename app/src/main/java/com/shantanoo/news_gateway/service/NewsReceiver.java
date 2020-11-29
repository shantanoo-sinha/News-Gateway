package com.shantanoo.news_gateway.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shantanoo.news_gateway.MainActivity;
import com.shantanoo.news_gateway.model.NewsArticle;

import java.util.List;

import static com.shantanoo.news_gateway.MainActivity.ARTICLE_LIST;

public class NewsReceiver extends BroadcastReceiver {

    private static final String TAG = "NewsReceiver";

    private final MainActivity mainActivity;

    public NewsReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        String action = intent.getAction();
        if (action == null)
            return;
        if (MainActivity.ACTION_NEWS_STORY.equals(action)) {
            List<NewsArticle> articles;
            if (intent.hasExtra(ARTICLE_LIST)) {
                articles = (List<NewsArticle>) intent.getSerializableExtra(ARTICLE_LIST);
                mainActivity.updateFragments(articles);
            }
        }
    }
}