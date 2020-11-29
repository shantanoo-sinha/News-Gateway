package com.shantanoo.news_gateway.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shantanoo.news_gateway.MainActivity;

/**
 * Created by Shantanoo on 11/22/2020.
 */
public class NewsServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "NewsServiceReceiver";

    private final NewsService newsService;

    public NewsServiceReceiver(NewsService newsService) {
        this.newsService = newsService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        String action = intent.getAction();
        if (action == null)
            return;
        if (MainActivity.ACTION_SERVICE.equals(action)) {
            String sourceId = null;
            if (intent.hasExtra(MainActivity.SOURCE_ID)) {
                sourceId = intent.getStringExtra(MainActivity.SOURCE_ID);
                sourceId = sourceId.replaceAll(" ", "-");
            }
            Log.d(TAG, "onReceive: Starting ArticleDownloader thread");
            new Thread(new ArticleDownloader(newsService, sourceId)).start();
        }
    }
}