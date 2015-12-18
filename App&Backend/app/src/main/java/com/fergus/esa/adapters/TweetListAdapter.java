package com.fergus.esa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.backend.esaEventEndpoint.model.ESATweet;
import com.fergus.esa.backend.esaEventEndpoint.model.TweetObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TweetListAdapter extends ArrayAdapter<TweetObject> {
    private final Context context;
    private List<TweetObject> tweets;
    private List<TweetObject> allTweets;
    private Filter searchFilter;


    public TweetListAdapter(Context context, List<TweetObject> tweets) {
        super(context, R.layout.tweet_row, tweets);
        this.context = context;
        this.tweets = tweets;
        this.allTweets = tweets;
    }


    public int getTweetCount() {
        return tweets.size();
    }


    public TweetObject getTweet(int position) {
        return tweets.get(position);
    }


    public long getTweetId(int position) {
        return tweets.get(position).getId();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.tweet_row, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.userImage = (ImageView) convertView.findViewById(R.id.userImage);
            viewHolder.tweetUser = (TextView) convertView.findViewById(R.id.tweetUser);
            viewHolder.tweetText = (TextView) convertView.findViewById(R.id.tweetText);
            viewHolder.tweetDate = (TextView) convertView.findViewById(R.id.tweetDate);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        TweetObject item = getItem(position);
        Picasso.with(context) //
                .load(item.getProfileImgUrl()) //
                .placeholder(R.drawable.placeholder) //
                .fit() //
                .tag(context)//
                .into(viewHolder.userImage);

        String user = (item.getUsername() + " · @" + item.getScreenName());
        viewHolder.tweetUser.setText(user);
        viewHolder.tweetText.setText(item.getText());
        viewHolder.tweetDate.setText(new Date().toString()); // TODO: fix this one! item.getTimestamp().toString()

        return convertView;
    }


    private static class ViewHolder {
        ImageView userImage;
        TextView tweetUser;
        TextView tweetText;
        TextView tweetDate;
    }


    public void resetTweets() {
        tweets = allTweets;
    }


    public Filter getSearchFilter() {
        if (searchFilter == null)
            searchFilter = new searchFilter();

        return searchFilter;
    }


    private class searchFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults matchingTweets = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                // no text entered so the full list is shown
                matchingTweets.values = allTweets;
                matchingTweets.count = allTweets.size();
            } else {
                // We perform filtering operation
                List<ESATweet> matchingTweetsList = new ArrayList<ESATweet>();

                for (ESATweet t : matchingTweetsList) {
                    if (t.getText().toUpperCase().contains(constraint.toString().toUpperCase()))
                        matchingTweetsList.add(t);
                }

                matchingTweets.values = matchingTweetsList;
                matchingTweets.count = matchingTweetsList.size();

            }
            return matchingTweets;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                tweets = (List<TweetObject>) results.values;
                notifyDataSetChanged();
            }
        }
    }
}