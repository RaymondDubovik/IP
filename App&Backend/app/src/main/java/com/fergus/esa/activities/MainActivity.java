package com.fergus.esa.activities;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.fergus.esa.R;
import com.fergus.esa.adapters.EventPageAdapter;
import com.fergus.esa.connection.ConnectionChecker;
import com.fergus.esa.connection.ConnectionErrorView;
import com.fergus.esa.fragments.BackButtonFragment;
import com.fergus.esa.fragments.NetworkFragment;
import com.fergus.esa.pushNotifications.RegistrationIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.registerable.connection.Connectable;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
	private ConnectionErrorView connectionErrorView;

	private List<NetworkFragment> networkFragments = new ArrayList<>();
	private BackButtonFragment backButtonFragment;

    private Merlin merlin;
	private ViewPager viewPager;
	private EventPageAdapter adapter;


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initToolbar();

        merlin = new Merlin.Builder().withConnectableCallbacks().build(this);
        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
                onInternetConnected();
            }
        });

        connectionErrorView = new ConnectionErrorView(this, findViewById(R.id.linearLayoutConnectionErrorPanel));

		TabLayout tabLayout = (TabLayout) findViewById(R.id.eventTabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("New Events"));
        tabLayout.addTab(tabLayout.newTab().setText("Recommended"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

		viewPager = (ViewPager) findViewById(R.id.eventViewPager);
		adapter = new EventPageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

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


		handleIntent(getIntent());
        gcmRegister();
    }


    @Override
    protected void onResume() {
        merlin.bind();
        super.onResume();
        if (ConnectionChecker.hasInternetConnection(this)) {
            onInternetConnected();
        }
    }


    @Override
    protected void onPause() {
        merlin.unbind();
        super.onPause();
        if (connectionErrorView != null && connectionErrorView.isVisible()) {
            connectionErrorView.hide();
        }
    }


    public void onInternetConnected() {
		for (NetworkFragment fragment : networkFragments) {
			fragment.onInternetConnected();
		}
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


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Events"); // T0D0 remove hardcode
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    @Override
    public void onBackPressed() {
		if (backButtonFragment == null || !backButtonFragment.onBackPressed()) { // if there is no listener or listener didn't consume the event
			super.onBackPressed();
			return;
		}
    }


    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            Intent searchIntent = new Intent(this, SearchResultsActivity.class);
            searchIntent.putExtra(SearchResultsActivity.BUNDLE_PARAM_QUERY, query);
            startActivity(searchIntent);
        }
    }


    private void gcmRegister() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000)
                        .show();
            } else {
                Log.i("", "This device is not supported.");
                finish();
            }
            return;
        }

        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }


	public ConnectionErrorView getConnectionErrorView() {
		return connectionErrorView;
	}


	public void setNetworkFragment(NetworkFragment networkFragment) {
		networkFragments.add(networkFragment);
	}


	public void setBackButtonFragment(BackButtonFragment backButtonFragment) {
		this.backButtonFragment = backButtonFragment;
	}


	public Fragment getCurrentFragment() {
		return adapter.getRegisteredFragment(viewPager.getCurrentItem());
	}
}

