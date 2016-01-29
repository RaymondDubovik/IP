package com.fergus.esa.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.fergus.esa.activities.EventActivity;
import com.fergus.esa.adapters.LinksListAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.NewsObject;
import com.fergus.esa.dataObjects.ESANewsLink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LinksFragment extends ListFragment {
    private List<NewsObject> news;
    private List<ESANewsLink> newsLinks = new ArrayList<>();


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EventActivity activity = ((EventActivity) getActivity());
        news = activity.getNews();

        for (NewsObject n : news) {
            String title = n.getTitle();
            String url = n.getUrl();
            Date date = new Date(); // n.getTimestamp().getValue() TODO: // FIXME: 18.12.2015
            ESANewsLink newsLink = new ESANewsLink(title, url, date);
            newsLinks.add(newsLink);
        }

        Collections.sort(newsLinks);
        setListAdapter(new LinksListAdapter(getActivity(), newsLinks));
    }
}
