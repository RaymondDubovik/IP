package com.fergus.esa.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fergus.esa.R;
import com.fergus.esa.SquaredImageView;
import com.fergus.esa.backend.esaEventEndpoint.model.ImageObject;
import com.squareup.picasso.Picasso;

import java.util.List;

/*
    An adapter class to load images into a GridView
    Adapted from a code sample available at https://github.com/square/picasso/tree/master/picasso-sample/src/main/java/com/example/picasso
  */
public final class ImageAdapter extends BaseAdapter {
	private List<ImageObject> images;
	private Activity activity;
	private final boolean hasText;


	public ImageAdapter(Activity activity, List<ImageObject> images) {
		this(activity, images, true);
	}


	public ImageAdapter(Activity activity, List<ImageObject> images, boolean hasText) {
		this.activity = activity;
		this.images = images;
		this.hasText = hasText;
	}


	@Override
	public int getCount() {
		return (images == null) ? 0 : images.size();
	}


	@Override
	public ImageObject getItem(int position) {
		return images.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder view;

		LayoutInflater inflater = activity.getLayoutInflater();

		if (convertView == null) {
			view = new ViewHolder();
			convertView = inflater.inflate(R.layout.activity_main_row, null);

			if (!hasText) {
				convertView.findViewById(R.id.mainTextView).setVisibility(View.GONE);
			}

			view.imageView = (SquaredImageView) convertView.findViewById(R.id.mainImageView);

			convertView.setTag(view);
		} else {
			view = (ViewHolder) convertView.getTag();
		}

		ImageObject image = images.get(position);

		String imgUrl = image.getUrl();

		// Trigger the download of the URL asynchronously into the image view.
		Picasso.with(activity)
				.load(imgUrl)
				.placeholder(R.drawable.placeholder)
				.fit()
				.tag(activity)
				.into(view.imageView);

		return convertView;
	}


	private static class ViewHolder {
		public SquaredImageView imageView;
	}
}
