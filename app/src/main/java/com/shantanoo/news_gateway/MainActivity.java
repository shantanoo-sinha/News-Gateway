package com.shantanoo.news_gateway;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.shantanoo.news_gateway.adapter.PageAdapter;
import com.shantanoo.news_gateway.adapter.SourceAdapter;
import com.shantanoo.news_gateway.fragment.NewsFragment;
import com.shantanoo.news_gateway.model.Drawer;
import com.shantanoo.news_gateway.model.LayoutManager;
import com.shantanoo.news_gateway.model.NewsArticle;
import com.shantanoo.news_gateway.model.NewsSource;
import com.shantanoo.news_gateway.receiver.MainActivityReceiver;
import com.shantanoo.news_gateway.service.NewsService;
import com.shantanoo.news_gateway.service.SourceDownloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";
    public static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";
    public static final String ARTICLE_LIST = "ARTICLE_LIST";
    public static final String SOURCE_ID = "SOURCE_ID";
    private static final String TAG = "MainActivity";
    private String newsSource;

    private int currentSourcePointer;
    private boolean appState;
    private boolean serviceStatus = false;

    private List<String> sourceList;
    private List<NewsSource> sources;
    private List<String> categories;
    private List<NewsArticle> articles;
    private List<NewsFragment> newsFragments;
    private Map<String, NewsSource> sourceStore;

    private MainActivityReceiver receiver;
    private SourceAdapter adapter;

    private Menu categoryMenu;

    private List<Drawer> drawerList;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private PageAdapter pageAdapter;
    private ViewPager viewPager;

    private int[] topicColors;
    private Map<String, Integer> topicIntMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceList = new ArrayList<>();
        sources = new ArrayList<>();
        categories = new ArrayList<>();
        articles = new ArrayList<>();

        drawerList = new ArrayList<>();
        newsFragments = new ArrayList<>();

        sourceStore = new HashMap<>();

        topicIntMap = new HashMap<>();
        topicColors = getResources().getIntArray(R.array.topicColors);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        receiver = new MainActivityReceiver(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerListView = findViewById(R.id.drawerList);
        adapter = new SourceAdapter(this, drawerList);
        drawerListView.setAdapter(adapter);
        pageAdapter = new PageAdapter(getSupportFragmentManager(), newsFragments);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pageAdapter);

        // Start service if not started
        if (savedInstanceState == null && !serviceStatus) {
            Log.d(TAG, "onCreate: Starting News Service");
            Intent intent = new Intent(MainActivity.this, NewsService.class);
            startService(intent);
            serviceStatus = true;
        }

        IntentFilter filter = new IntentFilter(MainActivity.ACTION_NEWS_STORY);
        registerReceiver(receiver, filter);

        // if no data is there to restore
        if (sourceStore.isEmpty() && savedInstanceState == null)
            new Thread(new SourceDownloader(this, "")).start();

        // add click listener to drawer list view
        drawerListView.setOnItemClickListener((parent, view, position, id) -> {
            viewPager.setBackgroundResource(0);
            currentSourcePointer = position;
            selectListItem(position);
        });

        // update the drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_navigation_drawer, R.string.close_navigation_drawer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.categories, menu);
        categoryMenu = menu;
        if (appState) {
            categoryMenu.add("all");
            for (String category : categories)
                categoryMenu.add(category);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        if (drawerToggle.onOptionsItemSelected(item)) {  // <== Important!
            Log.d(TAG, "onOptionsItemSelected: drawerToggle " + item);
            return true;
        }
        Log.d(TAG, "onOptionsItemSelected: Starting Source Downloader thread");
        new Thread(new SourceDownloader(this, item.getTitle().toString())).start();
        drawerLayout.openDrawer(drawerListView);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onPostCreate: ");
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged: ");
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        LayoutManager layoutRestore = new LayoutManager();

        Log.d(TAG, "categories: " + categories);
        layoutRestore.setCategories(categories);

        Log.d(TAG, "sources: " + sources);
        layoutRestore.setSources(sources);

        layoutRestore.setArticle(viewPager.getCurrentItem());

        Log.d(TAG, "currentSourcePointer : " + currentSourcePointer);
        layoutRestore.setSource(currentSourcePointer);

        Log.d(TAG, "articles : " + articles);
        layoutRestore.setArticles(articles);

        outState.putSerializable("state", layoutRestore);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: ");
        super.onRestoreInstanceState(savedInstanceState);

        setTitle(R.string.app_name);
        LayoutManager layoutManager = (LayoutManager) savedInstanceState.getSerializable("state");
        appState = true;

        articles = layoutManager.getArticles();
        Log.d(TAG, "articles: " + articles);

        categories = layoutManager.getCategories();
        Log.d(TAG, "categories: " + categories);

        sources = layoutManager.getSources();
        Log.d(TAG, "sources: " + sources);

        for (int i = 0; i < sources.size(); i++) {
            sourceList.add(sources.get(i).getName());
            sourceStore.put(sources.get(i).getName(), sources.get(i));
        }

        drawerListView.clearChoices();
        adapter.notifyDataSetChanged();
        drawerListView.setOnItemClickListener((parent, view, position, id) -> {
                    viewPager.setBackgroundResource(0);
                    currentSourcePointer = position;
                    selectListItem(position);
                }
        );
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Stopping News Service");
        unregisterReceiver(receiver);
        Intent intent = new Intent(MainActivity.this, MainActivityReceiver.class);
        stopService(intent);
        super.onDestroy();
    }

    public void populateSourceAndCategory(List<String> newsCategories, List<NewsSource> newsSources) {
        Log.d(TAG, "populateSourceAndCategory: ");
        Log.d(TAG, "newsSources size: " + newsSources.size() + ", newsCategories size: " + newsCategories.size());
        sourceStore.clear();
        sourceList.clear();
        sources.clear();
        drawerList.clear();
        sources.addAll(newsSources);

        // Sort and update category list in the options menu
        if (!categoryMenu.hasVisibleItems()) {
            categories.clear();
            categories = newsCategories;
            categoryMenu.add("all");
            Collections.sort(newsCategories);
            int i = 0;
            for (String category : newsCategories) {
                SpannableString categoryString = new SpannableString(category);
                categoryString.setSpan(new ForegroundColorSpan(topicColors[i]), 0, categoryString.length(), 0);
                topicIntMap.put(category, topicColors[i++]);
                categoryMenu.add(categoryString);
            }
        }
        for (NewsSource source : newsSources) {
            if (topicIntMap.containsKey(source.getCategory())) {
                int color = topicIntMap.get(source.getCategory());
                SpannableString coloredString = new SpannableString(source.getName());
                coloredString.setSpan(new ForegroundColorSpan(color), 0, source.getName().length(), 0);
                source.setColoredName(coloredString);
                sourceList.add(source.getName());
                sourceStore.put(source.getName(), source);
            }
        }

        // Update the drawer
        for (NewsSource source : newsSources) {
            Drawer drawerContent = new Drawer();
            drawerContent.setItemName(source.getColoredName());
            drawerList.add(drawerContent);
        }
        adapter.notifyDataSetChanged();
    }

    public void updateFragments(List<NewsArticle> articles) {
        Log.d(TAG, "updateFragments: ");
        setTitle(newsSource);

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);

        newsFragments.clear();

        for (int article = 0; article < articles.size(); article++) {
            newsFragments.add(NewsFragment.newInstance(articles.get(article), article, articles.size()));
        }
        pageAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(0);
        this.articles = articles;
    }

    private void selectListItem(int position) {
        Log.d(TAG, "selectListItem => selected pos: " + position + ", sourceList size: " + sourceList.size());
        newsSource = sourceList.get(position);
        Intent intent = new Intent(MainActivity.ACTION_MSG_TO_SERVICE);
        intent.putExtra(SOURCE_ID, newsSource);
        sendBroadcast(intent);
        drawerLayout.closeDrawer(drawerListView);
    }
}