package com.shantanoo.news_gateway.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shantanoo.news_gateway.R;
import com.shantanoo.news_gateway.model.Drawer;

import java.util.List;

/**
 * Created by Shantanoo on 11/27/2020.
 */
public class SourceAdapter extends BaseAdapter {

    private final Context context;
    private final List<Drawer> drawerList;

    public SourceAdapter(Context context, List<Drawer> drawerList) {
        this.context = context;
        this.drawerList = drawerList;
    }

    @Override
    public int getCount() {
        return drawerList.size();
    }

    @Override
    public Object getItem(int position) {
        return drawerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = (LayoutInflater.from(context).inflate(R.layout.drawer_list_item, parent, false));

        Drawer drawer = drawerList.get(position);
        TextView textView = convertView.findViewById(R.id.listItem);
        textView.setText(drawer.getItemName());
        return convertView;
    }
}
