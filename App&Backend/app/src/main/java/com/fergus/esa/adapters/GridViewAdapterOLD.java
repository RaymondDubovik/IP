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
import com.fergus.esa.backend.esaEventEndpoint.model.ESAEvent;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
    An adapter class to load images into a GridView
    Adapted from a code sample available at https://github.com/square/picasso/tree/master/picasso-sample/src/main/java/com/example/picasso
  */
public final class GridViewAdapterOLD extends BaseAdapter {
    private final Context context;
    private final List<EventTextImage> eventTextImages = new ArrayList<EventTextImage>();
    private Activity activity;


    public GridViewAdapterOLD(Activity activity, Context context, List<ESAEvent> events) {
        this.activity = activity;
        this.context = context;


        for (ESAEvent e : events) {
            String imgUrl = "https://pixabay.com/static/uploads/photo/2015/03/01/11/16/all-654566_640.jpg";
            if (e.getImageUrls() != null) {
                List<String> imgUrls = e.getImageUrls();
                Random randomizer = new Random();
                imgUrl = imgUrls.get(randomizer.nextInt(imgUrls.size()));
            }
            String eventTitle = e.getEvent();
            EventTextImage eti = new EventTextImage(imgUrl, eventTitle);

            eventTextImages.add(eti);
        }
        //Collections.addAll(eventTextImages);
    }


    @Override
    public int getCount() {
        return eventTextImages.size();
    }


    @Override
    public EventTextImage getItem(int position) {
        return eventTextImages.get(position);
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

        LayoutInflater inflator = activity.getLayoutInflater();

        if (convertView == null) {
            view = new ViewHolder();
            convertView = inflator.inflate(R.layout.activity_main_row, null);

            view.eventTitle = (TextView) convertView.findViewById(R.id.mainTextView);
            view.eventImgView = (SquaredImageView) convertView.findViewById(R.id.mainImageView);

            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }
        view.eventTitle.setText(eventTextImages.get(position).getTitle());

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(eventTextImages.get(position).getUrl()) //
                .placeholder(R.drawable.placeholder) //
                .fit() //
                .tag(context)//
                .into(view.eventImgView);

        return convertView;
    }


    private class EventTextImage {
        String url;
        String title;


        public EventTextImage(String url, String title) {
            this.url = url;
            this.title = title;
        }


        public String getUrl() {
            return url;
        }


        public String getTitle() {
            return title;
        }
    }

}
