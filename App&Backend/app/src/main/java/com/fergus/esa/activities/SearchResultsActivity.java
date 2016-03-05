package com.fergus.esa.activities;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.ServerUrls;
import com.fergus.esa.SharedPreferencesKeys;
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
		handleIntent(getIntent());
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar, menu);

		// http://stackoverflow.com/questions/27378981/how-to-use-searchview-in-toolbar-android
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (searchItem != null) ? (SearchView) searchItem.getActionView() : null;
		if (searchView != null) {
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		}

		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingActivity.class);
				startActivity(intent);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}


	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			Intent searchIntent = new Intent(this, SearchResultsActivity.class);
			searchIntent.putExtra(SearchResultsActivity.BUNDLE_PARAM_QUERY, query);
			startActivity(searchIntent);
		}
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
				int summaryLength = PreferenceManager.getDefaultSharedPreferences(context).getInt(SharedPreferencesKeys.SUMMARY_LENGTH, 75); // T0D0 remove hardcode
                return ServerUrls.endpoint.listSearchedEvents(query, summaryLength).execute().getItems();
            } catch (IOException | IllegalArgumentException e) {
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
            }

            pd.dismiss();
        }
    }
}
