package com.fergus.esa.activities;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.ServerUrls;
import com.fergus.esa.adapters.GridViewAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.EventObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SearchResultsActivity extends ActionBarActivity {
	public static final String BUNDLE_PARAM_QUERY = "query";


	private GridView gridView;
	private GridViewAdapter eventAdapter;


	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		Intent intent = getIntent();
		String query = intent.getStringExtra(BUNDLE_PARAM_QUERY);

		if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(query);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

		gridView = (GridView) findViewById(R.id.gridView);

        EventSearchAsyncTask eventSearchTask = new EventSearchAsyncTask(this, query);
        eventSearchTask.execute();
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }


    private class EventSearchAsyncTask extends AsyncTask<Void, Void, List<EventObject>> {
        private Context context;
        private String query;
        private ProgressDialog pd;


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
        protected List<EventObject> doInBackground(Void... params) {
            try {
				// TODO: get real value here
                return ServerUrls.endpoint.listSearchedEvents(query, 75).execute().getItems();
            } catch (IOException e) {
                return Collections.EMPTY_LIST;
            }
        }


        @Override
        protected void onPostExecute(final List<EventObject> events) {
            TextView searchResults = (TextView) findViewById(R.id.results);
            if (events != null) {
                searchResults.setText(events.size() + " result(s) found for \"" + query + "\":");
            } else {
                searchResults.setText("No results found for \"" + query + "\"");
            }

            if (events != null) {
				eventAdapter = new GridViewAdapter(SearchResultsActivity.this);
				gridView.setAdapter(eventAdapter);

				eventAdapter.addItems(events);

				gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						EventObject event = eventAdapter.getItem(position);
						Intent intent = new Intent(SearchResultsActivity.this, EventActivity.class);
						Bundle extras = new Bundle();
						extras.putInt(EventActivity.BUNDLE_PARAM_EVENT_ID, event.getId());
						extras.putString(EventActivity.BUNDLE_PARAM_EVENT_HEADING, event.getHeading());
						intent.putExtras(extras);
						startActivity(intent);
					}
				});


				/*gv.setAdapter(new SearchGridViewAdapter(SearchResultsActivity.this, context, events, query));
                gv.setOnScrollListener(new ScrollListener(context));
                gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // TODO: put correct data int the intent
						String heading = events.get(position).getHeading();
                        Intent intent = new Intent(SearchResultsActivity.this, EventActivity.class);
                        Bundle extras = new Bundle();

                        intent.putExtras(extras);
                        startActivity(intent);
                    }
                });
                */
            }

            pd.hide();
        }
    }
}
