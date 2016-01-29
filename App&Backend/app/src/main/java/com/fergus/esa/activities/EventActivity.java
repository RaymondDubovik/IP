package com.fergus.esa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.fergus.esa.R;
import com.fergus.esa.ServerUrls;
import com.fergus.esa.SharedPreferencesKeys;
import com.fergus.esa.adapters.PageAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.ImageObject;
import com.fergus.esa.backend.esaEventEndpoint.model.NewsObject;
import com.fergus.esa.backend.esaEventEndpoint.model.SummaryObject;
import com.fergus.esa.backend.esaEventEndpoint.model.TweetObject;
import com.fergus.esa.dataObjects.UserObjectWrapper;

import java.io.IOException;
import java.util.List;

/*
    An activity which displays an event to the user in a tabbed layout
 */
public class EventActivity extends AppCompatActivity {
    public static final String BUNDLE_PARAM_EVENT_ID = "eventId";

    private String eventTitle;
    private int eventId;

    private ProgressDialog progressDialog;
    private int progressDialogCounter;

    private List<ImageObject> images;
    private List<TweetObject> tweets;
    private List<NewsObject> news;
    private List<SummaryObject> summaries;


	private double startTime;
	private double milliSeconds = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_tabs);
        setTitle("Loading...");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        eventId = extras.getInt(BUNDLE_PARAM_EVENT_ID);
        eventTitle = "Event Title"; // TODO: change to the real thing

		// in separate async tasks, so that it is easier to implement infinite scrolling for each of the fragments in the future
		new SummaryAsyncTask().execute();
		new ImageAsyncTask().execute();
		new TweetAsyncTask().execute();
        new NewsAsyncTask().execute();
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
		// TODO: title
        setTitle(eventTitle);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Event Summary"));
		tabLayout.addTab(tabLayout.newTab().setText("Images"));
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
    }


    private class TweetAsyncTask extends CounterAsyncTask {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                tweets = ServerUrls.endpoint.getTweets(eventId).execute().getItems();
            } catch (IOException e) {
                // TODO: do something
            }

            return null;
        }
    }


    private class NewsAsyncTask extends CounterAsyncTask {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                news = ServerUrls.endpoint.getNews(eventId).execute().getItems();
            } catch (IOException e) {
                // TODO: do something
            }

            return null;
        }
    }


    private class ImageAsyncTask extends CounterAsyncTask {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                images = ServerUrls.endpoint.getImages(eventId).execute().getItems();
            } catch (IOException e) {
                // TODO: do something
            }

            return null;
        }
    }


    private class SummaryAsyncTask extends CounterAsyncTask {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                summaries = ServerUrls.endpoint.getSummaries(eventId).execute().getItems();
            } catch (IOException e) {
                // TODO: do something
            }

            return null;
        }
    }


    private abstract class CounterAsyncTask extends AsyncTask<Void, Void, Void> {
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
                progressDialog.hide();
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
			} catch (IOException e) {
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