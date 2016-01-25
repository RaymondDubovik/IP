package com.fergus.esa.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.SquaredImageView;
import com.fergus.esa.backend.esaEventEndpoint.model.EventObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/*
    An adapter class to load images into a GridView
    Adapted from a code sample available at https://github.com/square/picasso/tree/master/picasso-sample/src/main/java/com/example/picasso
  */
public final class GridViewAdapter extends BaseAdapter {
    private static final String PLACEHOLDER_IMAGE_URL = "https://pixabay.com/static/uploads/photo/2015/03/01/11/16/all-654566_640.jpg";

    private final Context context;
    private List<EventObject> events;
    private Activity activity;


    public GridViewAdapter(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
        events = new ArrayList<>();
    }


    @Override
    public int getCount() {
        return (events == null) ? 0 : events.size();
    }


    @Override
    public EventObject getItem(int position) {
        return events.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    public static class ViewHolder {
        public SquaredImageView eventImgView;
        public TextView eventTitle;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;

        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {
            view = new ViewHolder();
            convertView = inflater.inflate(R.layout.activity_main_row, null);

            view.eventTitle = (TextView) convertView.findViewById(R.id.mainTextView);
            view.eventImgView = (SquaredImageView) convertView.findViewById(R.id.mainImageView);

            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        EventObject event = events.get(position);
        view.eventTitle.setText(event.getHeading());

        String imgUrl = event.getImageUrl();
        if (imgUrl == null) {
            imgUrl = PLACEHOLDER_IMAGE_URL;
        }

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context)
                .load(imgUrl)
                .placeholder(R.drawable.placeholder)
                .fit()
                .tag(context)
                .into(view.eventImgView);

        return convertView;
    }


    public void addItems(List<EventObject> events) {
        this.events.addAll(events);
        notifyDataSetChanged();
    }
}
