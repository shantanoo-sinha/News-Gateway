package com.shantanoo.news_gateway;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.shantanoo.news_gateway.adapter.ColorsAdapter;
import com.shantanoo.news_gateway.fragment.NewsFragment;
import com.shantanoo.news_gateway.model.Drawer;
import com.shantanoo.news_gateway.model.LayoutManager;
import com.shantanoo.news_gateway.model.NewsArticle;
import com.shantanoo.news_gateway.model.NewsSource;
import com.shantanoo.news_gateway.service.NewsReceiver;
import com.shantanoo.news_gateway.service.NewsService;
import com.shantanoo.news_gateway.service.SourceDownloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_SERVICE = "ACTION_SERVICE";
    public static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";
    public static final String ARTICLE_LIST = "ARTICLE_LIST";
    public static final String SOURCE_ID = "SOURCE_ID";
    private static final String TAG = "MainActivity";
    private List<Drawer> drawerList;
    private String newsSource;
    private int currentSourcePointer;
    private boolean serviceStatus = false;
    private boolean appState;
    private Map<String, NewsSource> sourceStore;
    private List<String> sourceList;
    private List<NewsSource> sources;
    private List<String> categories;
    private List<NewsFragment> newsFragments;
    private List<NewsArticle> articles;

    private NewsReceiver receiver;

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ColorsAdapter adapter;
    private Menu categoryMenu;
    private ActionBarDrawerToggle drawerToggle;
    private PageAdapter pageAdapter;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceList = new ArrayList<>();
        sources = new ArrayList<>();
        categories = new ArrayList<>();
        articles = new ArrayList<>();

        drawerList = new ArrayList<>();
        newsFragments = new ArrayList<>();

        sourceStore = new HashMap<>();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        receiver = new NewsReceiver(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerListView = findViewById(R.id.drawerList);
        adapter = new ColorsAdapter(this, drawerList);
        drawerListView.setAdapter(adapter);
        pageAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pageAdapter);

        // Start service if not started
        if (savedInstanceState == null && !serviceStatus) {
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
        if (drawerToggle.onOptionsItemSelected(item)) {  // <== Important!
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }
        new Thread(new SourceDownloader(this, item.getTitle().toString())).start();
        drawerLayout.openDrawer(drawerListView);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: STARTED");
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
        Log.d(TAG, "onSaveInstanceState: COMPLETED");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: STARTED");
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
        Log.d(TAG, "onRestoreInstanceState: COMPLETED");
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        Intent intent = new Intent(MainActivity.this, NewsReceiver.class);
        stopService(intent);
        super.onDestroy();
    }

    public void populateSourceAndCategory(List<String> newsCategories, List<NewsSource> newsSources) {
        Log.d(TAG, "init: STARTED");
        Log.d(TAG, "newsSources size : " + newsSources.size());
        Log.d(TAG, "newsCategories size : " + newsCategories.size());
        sourceStore.clear();
        sourceList.clear();
        this.sources.clear();
        drawerList.clear();
        this.sources.addAll(newsSources);

        for (int index = 0; index < newsSources.size(); index++) {
            sourceList.add(newsSources.get(index).getName());
            sourceStore.put(newsSources.get(index).getName(), newsSources.get(index));
        }

        // Sort and update category list in the options menu
        if (!categoryMenu.hasVisibleItems()) {
            this.categories.clear();
            this.categories = newsCategories;
            categoryMenu.add("all");
            Collections.sort(newsCategories);
            for (String category : newsCategories)
                categoryMenu.add(category);
        }

        // Update the drawer
        for (NewsSource s : newsSources) {
            Drawer drawerContent = new Drawer();
            drawerContent.setItemName(s.getName());
            drawerList.add(drawerContent);
        }
        adapter.notifyDataSetChanged();

        Log.d(TAG, "init: COMPLETED");
    }

    public void updateFragments(List<NewsArticle> articles) {
        Log.d(TAG, "updateFragments: STARTED");
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
        Log.d(TAG, "updateFragments: COMPLETED");
    }

    private void selectListItem(int position) {
        Log.d(TAG, "selected pos is : " + position + " sourceList size is: " + sourceList.size());
        newsSource = sourceList.get(position);
        Intent intent = new Intent(MainActivity.ACTION_SERVICE);
        intent.putExtra(SOURCE_ID, newsSource);
        sendBroadcast(intent);
        drawerLayout.closeDrawer(drawerListView);
    }

    private class PageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return newsFragments.get(position);
        }

        @Override
        public int getCount() {
            return newsFragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         *
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }
    }
}