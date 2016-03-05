package com.fergus.esa.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.fergus.esa.activities.EventActivity;
import com.fergus.esa.adapters.NewsAdapter;

public class NewsFragment extends ListFragment implements SearchableFragment {


	private NewsAdapter adapter;


	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EventActivity activity = ((EventActivity) getActivity());

		adapter = new NewsAdapter(activity, activity.getNews());
		setListAdapter(adapter);

		activity.registerSearchFragment(this);
    }


	@Override
	public void onSearch(String query) {
		adapter.getFilter().filter(query);
	}
}
