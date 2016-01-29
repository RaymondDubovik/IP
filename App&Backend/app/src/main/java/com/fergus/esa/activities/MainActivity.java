package com.fergus.esa.activities;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fergus.esa.CategoryStorer;
import com.fergus.esa.R;
import com.fergus.esa.ServerUrls;
import com.fergus.esa.adapters.CategoryAdapter;
import com.fergus.esa.adapters.GridViewAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.CategoryObject;
import com.fergus.esa.backend.esaEventEndpoint.model.EventObject;
import com.fergus.esa.backend.esaEventEndpoint.model.EventObjectCollection;
import com.fergus.esa.connection.ConnectionChecker;
import com.fergus.esa.connection.ConnectionErrorView;
import com.fergus.esa.connection.RetryListener;
import com.fergus.esa.dataObjects.CategoryObjectWrapper;
import com.fergus.esa.listeners.CompositeScrollListener;
import com.fergus.esa.listeners.InfiniteScrollListener;
import com.fergus.esa.listeners.PixelScrollDetector;
import com.fergus.esa.listeners.ScrollListener;
import com.fergus.esa.pushNotifications.RegistrationIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.registerable.connection.Connectable;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;


public class MainActivity extends ActionBarActivity {
	private ConnectionErrorView connectionErrorView;

    private Merlin merlin;


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


        connectionErrorView = new ConnectionErrorView(this, findViewById(R.id.linearLayoutConnectionErrorPanel), new RetryListener() {
            @Override
            public void onRetry() {
                getData();
				// TODO: interface. here
            }
        });




		/*


		 TabLayout tabLayout = (TabLayout) findViewById(R.id.eventDataTabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Event Summary"));
		tabLayout.addTab(tabLayout.newTab().setText("Images"));
        tabLayout.addTab(tabLayout.newTab().setText("Related Tweets"));
        tabLayout.addTab(tabLayout.newTab().setText("News Articles"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.eventDateViewPager);
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

		 */

		handleIntent(getIntent());
        gcmRegister();
    }



    @Override
    protected void onResume() {
        merlin.bind();
        super.onResume();
        if (loadRequired && ConnectionChecker.hasInternetConnection(this)) {
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
        // TODO: on internet connected
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


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.recent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    @Override
    public void onBackPressed() {
        if (slidingPanel != null && (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }


    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            Intent searchIntent = new Intent(MainActivity.this, SearchResultsActivity.class);
            searchIntent.putExtra("query", query);
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
}

