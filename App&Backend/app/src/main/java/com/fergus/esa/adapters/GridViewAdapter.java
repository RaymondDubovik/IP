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
import com.fergus.esa.backend.esaEventEndpoint.model.ImageObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
    An adapter class to load images into a GridView
    Adapted from a code sample available at https://github.com/square/picasso/tree/master/picasso-sample/src/main/java/com/example/picasso
  */
public final class GridViewAdapter extends BaseAdapter {
    private final Context context;
    private final List<EventTextImage> eventTextImages = new ArrayList<>();
    private Activity activity;


    public GridViewAdapter(Activity activity, Context context, List<EventObject> events) {
        this.activity = activity;
        this.context = context;


        addItems(events);
    }


    public void addItems(List<EventObject> items) {
        for (EventObject event : items) {
            String imgUrl = "https://pixabay.com/static/uploads/photo/2015/03/01/11/16/all-654566_640.jpg";

            List<ImageObject> images = event.getImages();
            // TODO: fix
            if (images != null) {
                Random randomizer = new Random();
                imgUrl = images.get(randomizer.nextInt(images.size())).getUrl();
            }
            String eventTitle = event.getHeading();
            EventTextImage eti = new EventTextImage(imgUrl, eventTitle);

            eventTextImages.add(eti);
        }
        //Collections.addAll(eventTextImages);

        notifyDataSetChanged();
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
