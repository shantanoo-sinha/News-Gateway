package com.shantanoo.news_gateway.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shantanoo.news_gateway.MainActivity;
import com.shantanoo.news_gateway.R;

public class NewsServiceReceiver extends BroadcastReceiver {

    private final NewsService newsService;

    public NewsServiceReceiver(NewsService newsService) {
        this.newsService = newsService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;
        if(MainActivity.ACTION_SERVICE.equals(action)) {
            String sourceId = "";
            if (intent.hasExtra(MainActivity.SOURCE_ID)) {
                sourceId = intent.getStringExtra(MainActivity.SOURCE_ID);
                sourceId = sourceId.replaceAll(context.getString(R.string.space), context.getString(R.string.hyphen));
            }
            new Thread(new ArticleDownloader(newsService, sourceId)).start();
        }
    }
}