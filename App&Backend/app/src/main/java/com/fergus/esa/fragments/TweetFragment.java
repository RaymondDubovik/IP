package com.fergus.esa.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fergus.esa.R;
import com.fergus.esa.activities.EventActivity;
import com.fergus.esa.adapters.TweetListAdapter;

/*
    A fragment to display tweets
    filtering code adapted from http://www.survivingwithandroid.com/2012/10/android-listview-custom-filter-and.html
 */

public class TweetFragment extends Fragment {
	private TweetListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweet, container, false);

		EventActivity activity = ((EventActivity) getActivity());

        ListView listView = (ListView) view.findViewById(R.id.tweetList);
        adapter = new TweetListAdapter(activity, activity.getTweets());
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);

		SearchView searchView = (SearchView) view.findViewById(R.id.searchText);
		searchView.setIconifiedByDefault(false);
		searchView.setQueryHint("Filter tweets..."); // T0D0 remove hardcode
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String newText) {
				adapter.resetTweets();
				adapter.getFilter().filter(newText);
				return true;
			}


			@Override
			public boolean onQueryTextSubmit(String query) {return false;}
		});

        return view;
    }
}
