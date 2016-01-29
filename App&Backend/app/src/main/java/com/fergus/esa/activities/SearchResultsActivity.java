package com.fergus.esa.activities;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.adapters.SearchGridViewAdapter;
import com.fergus.esa.backend.esaEventEndpoint.EsaEventEndpoint;
import com.fergus.esa.backend.esaEventEndpoint.model.ESAEvent;
import com.fergus.esa.listeners.ScrollListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SearchResultsActivity extends ActionBarActivity {
    private String query;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        Intent intent = getIntent();
        query = intent.getStringExtra("query");
        EventSearchAsyncTask eventSearchTask = new EventSearchAsyncTask(this, query);
        eventSearchTask.execute();
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }


    private class EventSearchAsyncTask extends AsyncTask<Void, Void, List<ESAEvent>> {
        EsaEventEndpoint myApiService = null;
        private Context context;
        private String query;
        ProgressDialog pd;


        EventSearchAsyncTask(Context context, String query) {
            this.context = context;
            this.query = query;
        }


        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(SearchResultsActivity.this);
            pd.setTitle("Please Wait...");
            pd.setMessage("Downloading Events...");
            pd.show();
        }


        @Override
        protected List<ESAEvent> doInBackground(Void... params) {
            if (myApiService == null) {  // Only do this once
                EsaEventEndpoint.Builder builder = new EsaEventEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://esabackend-1049.appspot.com/_ah/api/");

                myApiService = builder.build();
            }

            try {
                return myApiService.listSearchedEvents(query).execute().getItems();
            } catch (IOException e) {
                return Collections.EMPTY_LIST;
            }
        }


        @Override
        protected void onPostExecute(final List<ESAEvent> events) {

            TextView searchResults = (TextView) findViewById(R.id.results);
            if (events != null) {
                searchResults.setText(events.size() + " result(s) found for \"" + query + "\":");
            } else {
                searchResults.setText("No results found for \"" + query + "\"");
            }
            GridView gv = (GridView) findViewById(R.id.gridView);
            if (events != null) {
                gv.setAdapter(new SearchGridViewAdapter(SearchResultsActivity.this, context, events, query));
                gv.setOnScrollListener(new ScrollListener(context));
                gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String eventString = events.get(position).getEvent();
                        String[] eventWithScore = eventString.split("esaseparator");
                        String event = eventWithScore[0];
                        Intent intent = new Intent(SearchResultsActivity.this, EventActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("event", event);
                        intent.putExtras(extras);
                        startActivity(intent);
                    }
                });
            } else {
                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(SearchResultsActivity.this)
                                .setTitle("No Events Found")
                                .setMessage("No events matching query: " + query + " found.")
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.show();
            }

            pd.hide();


        }
    }
}
