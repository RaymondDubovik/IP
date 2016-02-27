package com.fergus.esa.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.backend.esaEventEndpoint.model.NewsObject;

import java.text.SimpleDateFormat;
import java.util.List;

public class LinksListAdapter extends ArrayAdapter<NewsObject> {
    private final Context context;


    public LinksListAdapter(Context context, List<NewsObject> items) {
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
			viewHolder.linkUrl.setPaintFlags(viewHolder.linkUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            viewHolder.linkDate = (TextView) convertView.findViewById(R.id.linkDate);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (getItem(position) != null) {
            final NewsObject item = getItem(position);

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


    private static class ViewHolder {
        TextView linkTitle;
        TextView linkUrl;
        TextView linkDate;
    }
}