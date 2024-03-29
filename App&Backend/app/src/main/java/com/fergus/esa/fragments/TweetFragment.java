package com.fergus.esa.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fergus.esa.R;
import com.fergus.esa.activities.EventActivity;
import com.fergus.esa.adapters.TweetAdapter;

/*
    A fragment to display tweets
    filtering code adapted from http://www.survivingwithandroid.com/2012/10/android-listview-custom-filter-and.html
 */

public class TweetFragment extends Fragment implements SearchableFragment {
	private TweetAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweet, container, false);

		EventActivity activity = ((EventActivity) getActivity());

        ListView listView = (ListView) view.findViewById(R.id.tweetList);
        adapter = new TweetAdapter(activity, activity.getTweets());
        listView.setAdapter(adapter);

		activity.registerSearchFragment(this);
        return view;
    }


	@Override
	public void onSearch(String query) {
		adapter.getFilter().filter(query);
	}
}
