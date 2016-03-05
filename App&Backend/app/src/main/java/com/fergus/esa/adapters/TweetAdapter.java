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
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.backend.esaEventEndpoint.model.TweetObject;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class TweetAdapter extends BaseAdapter implements Filterable {
	private Context context;

	private TweetFilter filter;

	private List<TweetObject> originalItems;
	private List<TweetObject> items;


    public TweetAdapter(Context context, List<TweetObject> tweets) {
		this.context = context;
		items = tweets;
    }


	@Override
	public int getCount() {
		return items.size();
	}


	@Override
	public Object getItem(int position) {
		return items.get(position);
	}


	@Override
	public long getItemId(int position) {
		return items.get(position).getId();
	}


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(context);
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
        TweetObject item = (TweetObject) getItem(position);
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


	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new TweetFilter();
		}
		return filter;
	}


	private class TweetFilter extends Filter {
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			items = (List<TweetObject>) results.values; // has the filtered values
			notifyDataSetChanged();  // notifies the data with new filtered values
		}


		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			List<TweetObject> filtered = new ArrayList<>();

			if (originalItems == null) {
				originalItems = new ArrayList<>(items); // make a copy of original values
			}

			if (TextUtils.isEmpty(constraint)) { // if no constraint, restore to original data
				results.count = originalItems.size();
				results.values = originalItems;
			} else {
				constraint = constraint.toString().toLowerCase();
				for (int i = 0; i < originalItems.size(); i++) {
					TweetObject tweet = originalItems.get(i);
					String searchable = tweet.getText();
					if (searchable.toLowerCase().contains(constraint)) {
						filtered.add(tweet);
					}
				}
				// set the Filtered result to return
				results.count = filtered.size();
				results.values = filtered;
			}

			return results;
		}
	}


    private static class ViewHolder {
        ImageView userImage;
        TextView tweetUser;
        TextView tweetText;
		TextView tweetUrl;
        TextView tweetDate;
    }
}