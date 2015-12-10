package com.fergus.esa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fergus.esa.dataObjects.ESANewsLink;
import com.fergus.esa.R;

import java.util.List;

public class LinksListAdapter extends ArrayAdapter<ESANewsLink> {
    private final Context context;


    public LinksListAdapter(Context context, List<ESANewsLink> items) {
        super(context, R.layout.link_row, items);
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.link_row, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.linkTitle = (TextView) convertView.findViewById(R.id.linkTitle);
            viewHolder.linkUrl = (TextView) convertView.findViewById(R.id.linkUrl);
            viewHolder.linkDate = (TextView) convertView.findViewById(R.id.linkDate);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (getItem(position) != null) {
            ESANewsLink item = getItem(position);
            viewHolder.linkTitle.setText(item.getTitle());
            viewHolder.linkUrl.setText(item.getUrl());
            viewHolder.linkDate.setText(item.getDate().toString());

        }

        return convertView;
    }


    private static class ViewHolder {
        TextView linkTitle;
        TextView linkUrl;
        TextView linkDate;
    }
}