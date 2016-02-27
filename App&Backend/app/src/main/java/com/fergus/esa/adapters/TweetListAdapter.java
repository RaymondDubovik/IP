package com.fergus.esa.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
			viewHolder.tweetUrl = (TextView) convertView.findViewById(R.id.tweetUrl);
			viewHolder.tweetUrl.setPaintFlags(viewHolder.tweetUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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

		String user = ("@" +item.getScreenName() + " (" + item.getUsername() + ")");

		SimpleDateFormat dateFormat;
		long timestamp = item.getTimestamp().getValue();
		if (DateUtils.isToday(timestamp)) {
			dateFormat = new SimpleDateFormat("hh:mm a");
		} else {
			dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		}

		final String url = item.getUrl();

        viewHolder.tweetUser.setText(user);
        viewHolder.tweetText.setText(item.getText());
		viewHolder.tweetDate.setText(dateFormat.format(timestamp));

		if (!TextUtils.isEmpty(url)) {
			viewHolder.tweetUrl.setVisibility(View.VISIBLE);
			viewHolder.tweetUrl.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					context.startActivity(intent);
				}
			});
		} else {
			viewHolder.tweetUrl.setOnClickListener(null);
			viewHolder.tweetUrl.setVisibility(View.INVISIBLE);
		}

        return convertView;
    }


    private static class ViewHolder {
        ImageView userImage;
        TextView tweetUser;
        TextView tweetText;
		TextView tweetUrl;
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