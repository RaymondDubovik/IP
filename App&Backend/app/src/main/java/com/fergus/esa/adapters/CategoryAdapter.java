package com.fergus.esa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.backend.esaEventEndpoint.model.CategoryObject;

import java.util.List;

/**
 * Author: svchosta (https://github.com/svchosta)
 * Date: 08.22.2015
 */
public class CategoryAdapter extends ArrayAdapter<CategoryObject> {
    private LayoutInflater layoutInflater;


    public CategoryAdapter(Context context, List<CategoryObject> objects) {
        super(context, 0, objects);
        layoutInflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.list_item_category, null);
            holder.category = (TextView) convertView.findViewById(R.id.textViewCategory);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.category.setText(getItem(position).getName());

        return convertView;
    }


    private static class ViewHolder {
        TextView category;
    }
}
