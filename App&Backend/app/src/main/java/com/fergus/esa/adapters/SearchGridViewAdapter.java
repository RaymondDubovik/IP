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
public final class SearchGridViewAdapter extends BaseAdapter {
    private final Context context;
    private final List<EventTextImageScore> eventTextImageScores = new ArrayList<EventTextImageScore>();
    private Activity activity;
    private String query;


    public SearchGridViewAdapter(Activity activity, Context context, List<ESAEvent> events, String query) {
        this.activity = activity;
        this.context = context;
        this.query = query;


        for (ESAEvent e : events) {
            String imgUrl = "https://pixabay.com/static/uploads/photo/2015/03/01/11/16/all-654566_640.jpg";
            if (e.getImageUrls() != null) {
                List<String> imgUrls = e.getImageUrls();
                Random randomizer = new Random();
                imgUrl = imgUrls.get(randomizer.nextInt(imgUrls.size()));
            }

            String eventWithScore = e.getEvent();
            String[] eventScoreDetails = eventWithScore.split("esaseparator");
            String eventTitle = eventScoreDetails[0];
            double score = Double.parseDouble(eventScoreDetails[1]);

            EventTextImageScore etis = new EventTextImageScore(imgUrl, eventTitle, score);

            eventTextImageScores.add(etis);
        }

    }


    @Override
    public int getCount() {
        return eventTextImageScores.size();
    }


    @Override
    public EventTextImageScore getItem(int position) {
        return eventTextImageScores.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    public static class ViewHolder {
        public SquaredImageView eventImgView;
        public TextView eventTitle;
        public TextView eventScore;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder view;

        LayoutInflater inflator = activity.getLayoutInflater();

        if (convertView == null) {
            view = new ViewHolder();
            convertView = inflator.inflate(R.layout.activity_search_results_row, null);

            view.eventTitle = (TextView) convertView.findViewById(R.id.searchEventTextView);
            view.eventImgView = (SquaredImageView) convertView.findViewById(R.id.searchImageView);
            view.eventScore = (TextView) convertView.findViewById(R.id.searchScoreTextView);

            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }
        view.eventTitle.setText(eventTextImageScores.get(position).getTitle());

        String[] queryTerms = query.split(" ");
        double resultPercentage = (eventTextImageScores.get(position).getScore() / queryTerms.length) * 100;
        String percentageString = String.format("%.2f", resultPercentage);

        view.eventScore.setText("Query Match: " + percentageString + "%");

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(eventTextImageScores.get(position).getUrl()) //
                .placeholder(R.drawable.placeholder) //
                .fit() //
                .tag(context)//
                .into(view.eventImgView);

        return convertView;
    }


    private class EventTextImageScore {
        String url;
        String title;
        double score;


        public EventTextImageScore(String url, String title, double score) {
            this.url = url;
            this.title = title;
            this.score = score;
        }


        public String getUrl() {
            return url;
        }


        public String getTitle() {
            return title;
        }


        public double getScore() {
            return score;
        }
    }

}
