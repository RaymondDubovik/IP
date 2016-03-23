package com.fergus.esa.adapters;

import android.content.Context;
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
import com.fergus.esa.backend.esaEventEndpoint.model.SummaryObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SummaryAdapter extends BaseAdapter  implements Filterable {
	private Context context;

	private SummaryFilter filter;

	private List<SummaryObject> originalItems;
	private List<SummaryObject> items;


	public SummaryAdapter(Context context, List<SummaryObject> summaries) {
		this.context = context;
		items = summaries;
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
		return position;
	}


	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.summary_row, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.summaryDate = (TextView) convertView.findViewById(R.id.summaryDate);
            viewHolder.summaryText = (TextView) convertView.findViewById(R.id.summaryText);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (getItem(position) != null) {
            SummaryObject summary = (SummaryObject) getItem(position);

            viewHolder.summaryText.setText(summary.getText());

			String date;
			long timestamp = summary.getTimestamp().getValue();
			if (DateUtils.isToday(timestamp)) {
				date = "Today";
			} else {
				date = new SimpleDateFormat("dd/MM/yyyy").format(timestamp);
			}

            viewHolder.summaryDate.setText(date);
        }

        return convertView;
    }


	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new SummaryFilter();
		}
		return filter;
	}


	private class SummaryFilter extends Filter {
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			items = (List<SummaryObject>) results.values; // has the filtered values
			notifyDataSetChanged();  // notifies the data with new filtered values
		}


		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			List<SummaryObject> filtered = new ArrayList<>();

			if (originalItems == null) {
				originalItems = new ArrayList<>(items); // make a copy of original values
			}

			if (TextUtils.isEmpty(constraint)) { // if no constraint, restore to original data
				results.count = originalItems.size();
				results.values = originalItems;
			} else {
				constraint = constraint.toString().toLowerCase();
				for (int i = 0; i < originalItems.size(); i++) {
					SummaryObject summary = originalItems.get(i);
					String searchable = summary.getText();
					if (searchable.toLowerCase().contains(constraint)) {
						filtered.add(summary);
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
		TextView summaryDate;
		TextView summaryText;
	}
}