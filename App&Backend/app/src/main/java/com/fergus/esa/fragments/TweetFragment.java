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
import com.fergus.esa.backend.esaEventEndpoint.model.TweetObject;

import java.util.Collections;
import java.util.List;

/*
    A fragment to display tweets
    filtering code adapted from http://www.survivingwithandroid.com/2012/10/android-listview-custom-filter-and.html
 */

public class TweetFragment extends Fragment {
    private SearchView searchView;
    private List<TweetObject> tweets;
    private TweetListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        retrieveTweets();

        View view = inflater.inflate(R.layout.fragment_tweet, container, false);

        ListView lv = (ListView) view.findViewById(R.id.tweetList);

        adapter = new TweetListAdapter(getActivity(), tweets);

        lv.setAdapter(adapter);
        lv.setTextFilterEnabled(true);

        searchView = (SearchView) view.findViewById(R.id.searchText);
		searchView.setIconifiedByDefault(false);
		searchView.setQueryHint("Filter tweets...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {return false;}


			@Override
			public boolean onQueryTextChange(String newText) {
				adapter.resetTweets();

				adapter.getFilter().filter(newText);
				return true;
			}
		});

        return view;
    }
	

    public void retrieveTweets() {
        EventActivity activity = ((EventActivity) getActivity());

        tweets = activity.getTweets();

        if (tweets != null) {
            Collections.reverse(tweets);
        }

    }
}
