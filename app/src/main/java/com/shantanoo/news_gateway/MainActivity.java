package com.shantanoo.news_gateway;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.shantanoo.news_gateway.model.NewsSource;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Set<String> newsCategories;
    private Menu categoryMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categories, menu);
        categoryMenu = menu;
        categoryMenu.add("all");
        for (String category : newsCategories)
            categoryMenu.add(category);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void populateSourceAndCategory(Set<String> newsCategories, List<NewsSource> newsSources) {
    }
}