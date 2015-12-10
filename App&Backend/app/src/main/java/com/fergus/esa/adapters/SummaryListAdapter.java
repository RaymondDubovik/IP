package com.fergus.esa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fergus.esa.dataObjects.ESASummary;
import com.fergus.esa.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class SummaryListAdapter extends ArrayAdapter<ESASummary> {
    private final Context context;


    public SummaryListAdapter(Context context, List<ESASummary> summaries) {
        super(context, R.layout.summary_row, summaries);
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
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
            ESASummary summary = getItem(position);

            viewHolder.summaryText.setText(summary.getSummary());

            DateFormat dateformat = new SimpleDateFormat("EEE, dd MMM yyyy");
            String sumDate = dateformat.format(summary.getDate());
            viewHolder.summaryDate.setText(sumDate);


        }

        return convertView;
    }


    private static class ViewHolder {
        TextView summaryDate;
        TextView summaryText;
    }
}