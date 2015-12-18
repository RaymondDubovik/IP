package com.fergus.esa.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.fergus.esa.EventActivity;
import com.fergus.esa.R;
import com.fergus.esa.adapters.TweetListAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.TweetObject;

import java.util.Collections;
import java.util.List;

/*
    A fragment to display tweets
    filtering code adapted from http://www.survivingwithandroid.com/2012/10/android-listview-custom-filter-and.html
 */

public class TweetFragment extends Fragment {
    private EditText editTextSearch;
    private List<TweetObject> tweets;
    private AlertDialog alertDialog;
    private TweetListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        retrieveTweets();

        View view = inflater.inflate(R.layout.tweet_fragment, container, false);

        ListView lv = (ListView) view.findViewById(R.id.tweetList);

        adapter = new TweetListAdapter(getActivity(), tweets);

        lv.setAdapter(adapter);
        lv.setTextFilterEnabled(true);

        editTextSearch = (EditText) view.findViewById(R.id.searchText);

        editTextSearch.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editTextSearch.setHint("");
                return false;
            }

        });

        editTextSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    editTextSearch.setHint(R.string.tweet_search_hint);
                }
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < before) {
                    // We're deleting char so we need to reset the adapter data
                    adapter.resetTweets();
                }

                adapter.getFilter().filter(s.toString());
            }


            @Override
            public void afterTextChanged(Editable s) {

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
