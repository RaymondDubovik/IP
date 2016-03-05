package com.fergus.esa.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fergus.esa.R;
import com.fergus.esa.activities.EventActivity;
import com.fergus.esa.adapters.SummaryAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.SummaryObject;

import java.util.List;

public class SummaryFragment extends Fragment implements SearchableFragment {


	private SummaryAdapter adapter;


	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        EventActivity activity = ((EventActivity) getActivity());

        List<SummaryObject> summaries = activity.getSummaries();

        ListView listView = (ListView) view.findViewById(R.id.summaries);

		adapter = new SummaryAdapter(activity, summaries);
		listView.setAdapter(adapter);

		activity.registerSearchFragment(this);

		return view;
    }


	@Override
	public void onSearch(String query) {
		if (adapter != null) {
			adapter.getFilter().filter(query);
		}
	}
}