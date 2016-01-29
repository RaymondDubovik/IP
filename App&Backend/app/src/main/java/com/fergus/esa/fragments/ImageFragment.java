package com.fergus.esa.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.fergus.esa.EventActivity;
import com.fergus.esa.R;
import com.fergus.esa.adapters.ImageAdapter;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 29/01/2016
 */
public class ImageFragment extends Fragment {
	private GridView gridView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_images, container, false);

		EventActivity activity = ((EventActivity) getActivity());

		gridView = (GridView) view.findViewById(R.id.gridViewImages);
		ImageAdapter imageAdapter = new ImageAdapter(activity, activity.getImages());
		gridView.setAdapter(imageAdapter);

		return view;
	}
}
