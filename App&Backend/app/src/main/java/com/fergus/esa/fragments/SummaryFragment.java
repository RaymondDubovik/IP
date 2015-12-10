package com.fergus.esa.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.fergus.esa.dataObjects.ESASummary;
import com.fergus.esa.EventTabsActivity;
import com.fergus.esa.R;
import com.fergus.esa.adapters.SummaryListAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.ESAEvent;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class SummaryFragment extends Fragment {
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.summary_fragment, container, false);


        EventTabsActivity eta = ((EventTabsActivity) getActivity());

        ESAEvent esaEvent = eta.getESAEvent();

        List<String> summaries = esaEvent.getSummaries();

        List<ESASummary> summaryObjects = getSummaryObjects(summaries);

        Collections.sort(summaryObjects);


        ImageView iv = (ImageView) view.findViewById(R.id.imageView);

        if (iv == null) {
            iv = new ImageView(context);
            iv.setScaleType(CENTER_CROP);
        }

        String imgUrl = "https://pixabay.com/static/uploads/photo/2015/03/01/11/16/all-654566_640.jpg";
        if (esaEvent.getImageUrls() != null) {
            List<String> imgUrls = esaEvent.getImageUrls();
            Random randomizer = new Random();
            imgUrl = imgUrls.get(randomizer.nextInt(imgUrls.size()));
        }

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(eta) //
                .load(imgUrl) //
                .placeholder(R.drawable.placeholder)
                .fit() //
                .tag(eta)//
                .into(iv);


        ListView lv = (ListView) view.findViewById(R.id.summaries);

        lv.setAdapter(new SummaryListAdapter(getActivity(), summaryObjects));

        return view;
    }


    public List<ESASummary> getSummaryObjects(List<String> summaries) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.UK);
        List<ESASummary> summaryObjects = new ArrayList<>();

        for (String sum : summaries) {
            if (!sum.startsWith("esaseparator")) {
                String sumText = "";
                Date sumDate = new Date();

                String[] sumArray = sum.split("esaseparator");
                sumText = sumArray[0];
                String sumDateString = sumArray[1];

                try {
                    sumDate = dateFormatter.parse(sumDateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                ESASummary sumObj = new ESASummary(sumText, sumDate);
                summaryObjects.add(sumObj);

            }
        }

        return summaryObjects;
    }
}