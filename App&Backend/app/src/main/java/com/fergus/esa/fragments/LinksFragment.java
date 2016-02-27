package com.fergus.esa.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.fergus.esa.activities.EventActivity;
import com.fergus.esa.adapters.LinksListAdapter;

public class LinksFragment extends ListFragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EventActivity activity = ((EventActivity) getActivity());

        setListAdapter(new LinksListAdapter(getActivity(), activity.getNews()));
    }
}
