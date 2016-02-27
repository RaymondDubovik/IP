package com.fergus.esa.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fergus.esa.activities.EventActivity;
import com.fergus.esa.R;
import com.fergus.esa.adapters.SummaryListAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.SummaryObject;

import java.util.List;

public class SummaryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        EventActivity activity = ((EventActivity) getActivity());
        List<SummaryObject> summaries = activity.getSummaries();

        ListView listView = (ListView) view.findViewById(R.id.summaries);

        listView.setAdapter(new SummaryListAdapter(getActivity(), summaries));

        return view;
    }
}