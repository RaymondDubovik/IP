package com.fergus.esa.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.fergus.esa.ErrorAsyncTask;
import com.fergus.esa.R;
import com.fergus.esa.ServerUrls;
import com.fergus.esa.SharedPreferencesKeys;
import com.fergus.esa.adapters.EventDataPageAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.ImageObject;
import com.fergus.esa.backend.esaEventEndpoint.model.ImageObjectCollection;
import com.fergus.esa.backend.esaEventEndpoint.model.NewsObject;
import com.fergus.esa.backend.esaEventEndpoint.model.NewsObjectCollection;
import com.fergus.esa.backend.esaEventEndpoint.model.SummaryObject;
import com.fergus.esa.backend.esaEventEndpoint.model.SummaryObjectCollection;
import com.fergus.esa.backend.esaEventEndpoint.model.TweetObject;
import com.fergus.esa.backend.esaEventEndpoint.model.TweetObjectCollection;
import com.fergus.esa.dataObjects.UserObjectWrapper;
import com.fergus.esa.fragments.SearchableFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
    An activity which displays an event to the user in a tabbed layout
 */
public class EventActivity extends AppCompatActivity {
	public static final String BUNDLE_PARAM_EVENT_ID = "eventId";
	public static final String BUNDLE_PARAM_EVENT_HEADING = "eventHeading";

	private String eventTitle;
    private int eventId;

    private ProgressDialog progressDialog;
    private int progressDialogCounter;

    private List<ImageObject> images;
    private List<TweetObject> tweets;
    private List<NewsObject> news;
    private List<SummaryObject> summaries;

	private String searchQuery;
	private List<SearchableFragment> searchableFragments;

	private double startTime;
	private double milliSeconds = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_tabs);
        setTitle("Loading...");

		// TODO: init toolbar here
        initToolbar();

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        eventId = extras.getInt(BUNDLE_PARAM_EVENT_ID);
        eventTitle = extras.getString(BUNDLE_PARAM_EVENT_HEADING);

		if (eventId == 0) {
			System.out.println("could not get event id from the push notification");

			for (String key : extras.keySet()) {
				System.out.println(key);
			}

			System.out.println(extras.getString("com.fergus.esa"));
		}

		// in separate async tasks, so that it is easier to implement infinite scrolling for each of the fragments in the future
		new SummaryAsyncTask(this).execute();
		new ImageAsyncTask().execute();
		new TweetAsyncTask().execute();
        new NewsAsyncTask().execute();
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar, menu);

		// http://stackoverflow.com/questions/27378981/how-to-use-searchview-in-toolbar-android
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (searchItem != null) ? (SearchView) searchItem.getActionView() : null;
		if (searchView != null) {
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			// searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					// hiding keyboard
					View view = EventActivity.this.getCurrentFocus();
					if (view != null) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
					}

					callSearch(query);
					return true;
				}


				@Override
				public boolean onQueryTextChange(String newText) {
					callSearch(newText);
					return true;
				}


				public void callSearch(String query) {
					searchQuery = query;
					// notify observers
					if (searchableFragments != null) {
						for (SearchableFragment fragment : searchableFragments) {
							fragment.onSearch(query);
						}
					}
				}
			});
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


	private void initToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// getSupportActionBar().setTitle("Events"); // T0D0 remove hardcode
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}


	@Override
	protected void onResume() {
		super.onResume();
		startTime = SystemClock.elapsedRealtime();
	}


	@Override
	protected void onPause() {
		super.onPause();
		milliSeconds += SystemClock.elapsedRealtime() - startTime;

		if (isFinishing()) {
			final int userId = PreferenceManager.getDefaultSharedPreferences(EventActivity.this).getInt(SharedPreferencesKeys.USER_ID, UserObjectWrapper.NO_USER_ID);
			if (userId != UserObjectWrapper.NO_USER_ID) {
				new RegistrationAsyncTask(userId, eventId, milliSeconds).execute();
			}
		}
	}


	private void onDataLoaded() {
		startTime = SystemClock.elapsedRealtime();
        setTitle(eventTitle);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.eventDataTabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Event Summary"));
		tabLayout.addTab(tabLayout.newTab().setText("Images"));
        tabLayout.addTab(tabLayout.newTab().setText("Related Tweets"));
        tabLayout.addTab(tabLayout.newTab().setText("News Articles"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.eventDataViewPager);
        final EventDataPageAdapter adapter = new EventDataPageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }


	public void registerSearchFragment(SearchableFragment searchableFragment) {
		if (searchableFragments == null) {
			searchableFragments = new ArrayList<>();
		}

		for (SearchableFragment fragment : searchableFragments) {
			if (fragment == searchableFragment) { // if is already registered, no need to register it once again
				return;
			}
		}

		searchableFragments.add(searchableFragment);
		if (!TextUtils.isEmpty(searchQuery)) { // if search was already performed, but the fragment just registered
			searchableFragment.onSearch(searchQuery); // tell him to search straight away
		}
	}


	private class TweetAsyncTask extends CounterAsyncTask {
        @Override
        protected Void doInBackground(Void... params) {
            try {
				TweetObjectCollection collection = ServerUrls.endpoint.getTweets(eventId).execute();
				tweets = (collection == null) ? Collections.<TweetObject>emptyList() : collection.getItems();
            } catch (IOException | IllegalArgumentException ignored) {}

            return null;
        }
    }


    private class NewsAsyncTask extends CounterAsyncTask {
        @Override
        protected Void doInBackground(Void... params) {
            try {
				NewsObjectCollection collection = ServerUrls.endpoint.getNews(eventId).execute();
				news = (collection == null) ? Collections.<NewsObject>emptyList() : collection.getItems();
            } catch (IOException | IllegalArgumentException ignored) {}

            return null;
        }
    }


    private class ImageAsyncTask extends CounterAsyncTask {
        @Override
        protected Void doInBackground(Void... params) {
            try {
				ImageObjectCollection collection = ServerUrls.endpoint.getImages(eventId).execute();
				images = (collection == null) ? Collections.<ImageObject>emptyList() : collection.getItems();
            } catch (IOException | IllegalArgumentException ignored) {}

            return null;
        }
    }


    private class SummaryAsyncTask extends CounterAsyncTask {
		private int summaryLength;


		public SummaryAsyncTask(Context context) {
			summaryLength = PreferenceManager.getDefaultSharedPreferences(context).getInt(SharedPreferencesKeys.SUMMARY_LENGTH, 75); // remove hardcode from here
		}


		@Override
        protected Void doInBackground(Void... params) {
            try {

				SummaryObjectCollection collection = ServerUrls.endpoint.getSummaries(eventId, summaryLength).execute();
				summaries = (collection == null) ? Collections.<SummaryObject>emptyList() : collection.getItems();
            } catch (IOException | IllegalArgumentException ignored) {}

            return null;
        }
    }


    private abstract class CounterAsyncTask extends ErrorAsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(EventActivity.this);
                progressDialog.setTitle("Please Wait...");
                progressDialog.setMessage("Downloading Event Data...");
                progressDialog.show();
            }

            progressDialogCounter++;
        }


        @Override
        protected void onPostExecute(Void v) {
            progressDialogCounter--;

            if (progressDialogCounter == 0) {
                onDataLoaded();
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }


	private class RegistrationAsyncTask extends AsyncTask<Void, Void, Void> {
		private final int userId;
		private final int eventId;
		private final double milliseconds;


		public RegistrationAsyncTask(int userId, int eventId, double milliseconds) {
			this.userId = userId;
			this.eventId = eventId;
			this.milliseconds = milliseconds;
		}


		@Override
		protected Void doInBackground(Void... params) {
			try {
				ServerUrls.endpoint.registerHit(this.userId, this.eventId, this.milliseconds).execute();
			} catch (IOException | IllegalArgumentException e) {
				e.printStackTrace();
			}
			return null;
		}
	}


    public List<NewsObject> getNews() {
        return news;
    }


    public void setNews(List<NewsObject> news) {
        this.news = news;
    }


    public List<TweetObject> getTweets() {
        return tweets;
    }


    public void setTweets(List<TweetObject> tweets) {
        this.tweets = tweets;
    }


    public List<ImageObject> getImages() {
        return images;
    }


    public void setImages(List<ImageObject> images) {
        this.images = images;
    }


    public List<SummaryObject> getSummaries() {
        return summaries;
    }


    public void setSummaries(List<SummaryObject> summaries) {
        this.summaries = summaries;
    }
}