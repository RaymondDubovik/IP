package com.fergus.esa;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.fergus.esa.adapters.PageAdapter;
import com.fergus.esa.backend.esaEventEndpoint.EsaEventEndpoint;
import com.fergus.esa.backend.esaEventEndpoint.model.ESAEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

/*
    An activity which displays an event to the user in a tabbed layout
 */
public class EventTabsActivity extends AppCompatActivity {
    private String event;
    private ESAEvent esaEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_tabs);
        setTitle("Loading...");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        event = extras.getString("event");

        EventDataAsyncTask eventDataTask = new EventDataAsyncTask();
        eventDataTask.execute();
    }


    // Asynchronous task to download the ESAEvent Object from the datastore usin the Endpoints API
    private class EventDataAsyncTask extends AsyncTask<Void, Void, ESAEvent> {
        EsaEventEndpoint myApiService = null;
        ProgressDialog pd;
        AlertDialog ad;


        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(EventTabsActivity.this);
            pd.setTitle("Please Wait...");
            pd.setMessage("Downloading Event Data...");
            pd.show();
        }


        @Override
        protected ESAEvent doInBackground(Void... params) {
            if (myApiService == null) {  // Only do this once
                EsaEventEndpoint.Builder builder = new EsaEventEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null).setRootUrl("https://esabackend-1049.appspot.com/_ah/api/");
                myApiService = builder.build();
            }

            try {
                return myApiService.returnESAEvent(event).execute();
            } catch (IOException e) {
                return null;
            }
        }


        // A method to create a tabbed layout
        @Override
        protected void onPostExecute(final ESAEvent ev) {

            esaEvent = ev;

            if (esaEvent.getNews() == null || esaEvent.getTweets() == null || esaEvent.getSummaries() == null) {
                pd.hide();
                ad.setTitle("No Data");
                ad.setMessage("No tweets or news articles associated with this event");
                ad.show();
            } else {
                setTitle(esaEvent.getEvent());

                TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
                tabLayout.addTab(tabLayout.newTab().setText("Event Summary"));
                tabLayout.addTab(tabLayout.newTab().setText("Related Tweets"));
                tabLayout.addTab(tabLayout.newTab().setText("News Articles"));
                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


                final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
                final PageAdapter adapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

                viewPager.setAdapter(adapter);
                viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        viewPager.setCurrentItem(tab.getPosition());
                    }


                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }


                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
                pd.hide();
            }

        }
    }


    public ESAEvent getESAEvent() {
        return esaEvent;
    }
}