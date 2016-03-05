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
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.backend.esaEventEndpoint.model.NewsObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends BaseAdapter implements Filterable {
	private Context context;

	private NewsFilter filter;

	private List<NewsObject> originalItems;
	private List<NewsObject> items;


    public NewsAdapter(Context context, List<NewsObject> news) {
		this.context = context;
		items = news;
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
            convertView = inflater.inflate(R.layout.link_row, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.linkTitle = (TextView) convertView.findViewById(R.id.linkTitle);
            viewHolder.linkUrl = (TextView) convertView.findViewById(R.id.linkUrl);
			viewHolder.linkUrl.setPaintFlags(viewHolder.linkUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            viewHolder.linkDate = (TextView) convertView.findViewById(R.id.linkDate);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (getItem(position) != null) {
            final NewsObject item = (NewsObject) getItem(position);

			String date;
			long timestamp = item.getTimestamp().getValue();
			if (DateUtils.isToday(timestamp)) {
				date = "Today";
			} else {
				date = new SimpleDateFormat("dd/MM/yyyy").format(timestamp);
			}

            viewHolder.linkTitle.setText(item.getTitle());
            viewHolder.linkDate.setText(date);

			String url = item.getUrl().replace("http://www.", "").replace("https://www.", "").replace("https://", "").replace("http://", "");
			url = url.substring(0, url.indexOf("/"));
			viewHolder.linkUrl.setText(url);
			viewHolder.linkUrl.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl()));
					context.startActivity(intent);
				}
			});
        }

        return convertView;
    }


	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new NewsFilter();
		}
		return filter;
	}


	private class NewsFilter extends Filter {
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			items = (List<NewsObject>) results.values; // has the filtered values
			notifyDataSetChanged();  // notifies the data with new filtered values
		}


		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			List<NewsObject> filtered = new ArrayList<>();

			if (originalItems == null) {
				originalItems = new ArrayList<>(items); // make a copy of original values
			}

			if (TextUtils.isEmpty(constraint)) { // if no constraint, restore to original data
				results.count = originalItems.size();
				results.values = originalItems;
			} else {
				constraint = constraint.toString().toLowerCase();
				for (int i = 0; i < originalItems.size(); i++) {
					NewsObject news = originalItems.get(i);
					String searchable = news.getTitle();
					if (searchable.toLowerCase().contains(constraint)) {
						filtered.add(news);
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
        TextView linkTitle;
        TextView linkUrl;
        TextView linkDate;
    }
}