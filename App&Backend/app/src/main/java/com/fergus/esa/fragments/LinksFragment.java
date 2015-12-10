package com.fergus.esa.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.fergus.esa.dataObjects.ESANewsLink;
import com.fergus.esa.EventTabsActivity;
import com.fergus.esa.adapters.LinksListAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.ESAEvent;
import com.fergus.esa.backend.esaEventEndpoint.model.ESANews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LinksFragment extends ListFragment {
    private Context context;
    private List<ESANews> news;
    private List<ESANewsLink> newsLinks = new ArrayList<>();


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        retrieveNews();

        for (ESANews en : news) {
            String title = en.getTitle();
            String url = en.getUrl();
            Date date = new Date(en.getDate().getValue());
            ESANewsLink newsLink = new ESANewsLink(title, url, date);
            newsLinks.add(newsLink);
        }

        Collections.sort(newsLinks);
        setListAdapter(new LinksListAdapter(getActivity(), newsLinks));
    }


    public void retrieveNews() {
        EventTabsActivity eta = ((EventTabsActivity) getActivity());

        context = eta.getBaseContext();

        ESAEvent esaEvent = eta.getESAEvent();

        news = esaEvent.getNews();
    }
}
