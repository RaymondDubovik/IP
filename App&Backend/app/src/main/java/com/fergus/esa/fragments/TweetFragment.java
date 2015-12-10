package com.fergus.esa.fragments;

import android.app.AlertDialog;
import android.content.Context;
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

import com.fergus.esa.EventTabsActivity;
import com.fergus.esa.R;
import com.fergus.esa.adapters.TweetListAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.ESAEvent;
import com.fergus.esa.backend.esaEventEndpoint.model.ESATweet;

import java.util.Collections;
import java.util.List;

/*
    A fragment to display tweets
    filtering code adapted from http://www.survivingwithandroid.com/2012/10/android-listview-custom-filter-and.html
 */

public class TweetFragment extends Fragment {
    private Context context;
    private List<ESATweet> tweets;
    AlertDialog ad;
    TweetListAdapter tla;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        retrieveTweets();

        View view = inflater.inflate(R.layout.tweet_fragment, container, false);


        ListView lv = (ListView) view.findViewById(R.id.tweetList);

        tla = new TweetListAdapter(getActivity(), tweets);

        lv.setAdapter(tla);
        lv.setTextFilterEnabled(true);

        final EditText searchText = (EditText) view.findViewById(R.id.searchText);

        searchText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchText.setHint("");
                return false;
            }

        });

        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchText.setHint(R.string.tweet_search_hint);
                }
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < before) {
                    // We're deleting char so we need to reset the adapter data
                    tla.resetTweets();
                }

                tla.getFilter().filter(s.toString());
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;
    }


    public void retrieveTweets() {
        EventTabsActivity eta = ((EventTabsActivity) getActivity());

        context = eta.getBaseContext();

        ESAEvent esaEvent = eta.getESAEvent();


        tweets = esaEvent.getTweets();

        if (tweets != null) {
            Collections.reverse(tweets);
        }

    }
}
