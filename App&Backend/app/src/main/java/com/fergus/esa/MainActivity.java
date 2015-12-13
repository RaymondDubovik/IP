package com.fergus.esa;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.fergus.esa.adapters.CategoryAdapter;
import com.fergus.esa.adapters.GridViewAdapter;
import com.fergus.esa.backend.esaEventEndpoint.EsaEventEndpoint;
import com.fergus.esa.backend.esaEventEndpoint.model.CategoryObject;
import com.fergus.esa.backend.esaEventEndpoint.model.EventObject;
import com.fergus.esa.dataObjects.CategoryObjectWrapper;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private SlidingUpPanelLayout slidingPanel;
    private View viewListCover;
    private TextView textViewCategory;
    private ListView listViewCategories;
    private GridView gridViewEvent;
    private SwipeRefreshLayout swipeContainer;

    private CategoryStorer categoryStorer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initToolbar();

        listViewCategories = (ListView) findViewById(R.id.listViewCategories);
        textViewCategory = (TextView) findViewById(R.id.textViewSelectedCategory);
        gridViewEvent = (GridView) findViewById(R.id.gridView);

        new EventAsyncTask(true).execute();

        categoryStorer = new CategoryStorer(this);
        new CategoryAsyncTask().execute();

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


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.recent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    private void changeCategory(CategoryObject category) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        preferences.edit().putInt(SharedPreferencesKeys.CATEGORY_ID, category.getId()).apply();
        textViewCategory.setText(category.getName());
        // slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        // TODO: finish;
    }


    private class EventAsyncTask extends AsyncTask<Void, Void, List<EventObject>> {
        private EsaEventEndpoint endpoint = null;
        private ProgressDialog pd;
        private boolean displayDialog;


        EventAsyncTask(boolean displayDialog) {
            this.displayDialog = displayDialog;
        }


        @Override
        protected void onPreExecute() {
            if (displayDialog) {
                pd = new ProgressDialog(MainActivity.this);
                pd.setTitle("Please Wait...");
                pd.setMessage("Downloading Events...");
                pd.show();
            }
        }


        @Override
        protected List<EventObject> doInBackground(Void... voids) {
            if (endpoint == null) {  // Only do this once
                endpoint = new EsaEventEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null).setRootUrl(ServerUrls.ROOT_URL).build();
            }

            try {
                return endpoint.getEvents(0, 10).execute().getItems(); // TODO: change
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.EMPTY_LIST;
            }
        }


        @Override
        protected void onPostExecute(final List<EventObject> events) {
            Collections.reverse(events);

            gridViewEvent.setAdapter(new GridViewAdapter(MainActivity.this, MainActivity.this, events));
            gridViewEvent.setOnScrollListener(new ScrollListener(MainActivity.this));
            gridViewEvent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int eventId = events.get(position).getId();
                    Intent intent = new Intent(MainActivity.this, EventTabsActivity.class);
                    Bundle extras = new Bundle();
                    extras.putInt(EventTabsActivity.BUNDLE_PARAM_EVENT_ID , eventId);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });

            if (displayDialog) {
                pd.hide();
            }

            if (swipeContainer != null && swipeContainer.isRefreshing()) {
                swipeContainer.setRefreshing(false);
            }
        }
    }


    private class CategoryAsyncTask extends AsyncTask<Void, Void, List<CategoryObject>> {
        @Override
        protected List<CategoryObject> doInBackground(Void... voids) {

            EsaEventEndpoint endpoint = new EsaEventEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null).setRootUrl(ServerUrls.ROOT_URL).build();
            List<CategoryObject> categories;
            try {
                categories = endpoint.getCategories().execute().getItems();
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }

            categories.add(0, new CategoryObject().setId(CategoryObjectWrapper.ALL_CATEGORIES_ID).setName(CategoryObjectWrapper.ALL_CATEGORIES_NAME));

            return categories;
        }


        @Override
        protected void onPostExecute(List<CategoryObject> categories) {
            CategoryAdapter categoryAdapter = new CategoryAdapter(MainActivity.this, categories);
            listViewCategories.setAdapter(categoryAdapter);
            listViewCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (listViewCategories.isItemChecked(position)) {
                        CategoryObject category = (CategoryObject) listViewCategories.getItemAtPosition(position);
                        categoryStorer.addCategory(category);
                        changeCategory(category);
                    } else {
                        CategoryObject category = (CategoryObject) listViewCategories.getItemAtPosition(position);
                        categoryStorer.removeCategory(category);
                        // TODO: implement
                    }

                    Log.d("teswt", categoryStorer.getCount() + "");
                }
            });

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            int selectedCategory = preferences.getInt(SharedPreferencesKeys.CATEGORY_ID, CategoryObjectWrapper.ALL_CATEGORIES_ID);

            // listViewCategories.setItemChecked(position, true);

            for (CategoryObject category : categories) {
                if (category.getId() == selectedCategory) {
                    category.getName();
                    textViewCategory.setText(category.getName());
                    break;
                }
            }

            slidingPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            slidingPanel.setPanelSlideListener(new SlidingPanelListener());
            viewListCover = findViewById(R.id.viewEventListCover);

            swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    EventAsyncTask eventTask = new EventAsyncTask(false);  //can pass other variables as needed
                    eventTask.execute();
                }
            });

            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            swipeContainer.setColorSchemeColors(typedValue.data);
        }
    }


    private class SlidingPanelListener implements SlidingUpPanelLayout.PanelSlideListener {
        private boolean wasCollapsed = true;


        @Override
        public void onPanelSlide(View view, float v) {
            if (wasCollapsed) {
                wasCollapsed = false;
            }
        }


        @Override
        public void onPanelCollapsed(View view) {
            viewListCover.setVisibility(View.GONE);
            wasCollapsed = true;
        }


        @Override
        public void onPanelExpanded(View view) {
            viewListCover.setVisibility(View.VISIBLE);
            viewListCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            });
        }


        @Override
        public void onPanelAnchored(View view) {}


        @Override
        public void onPanelHidden(View view) {}
    }


    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            Intent searchIntent = new Intent(MainActivity.this, SearchResultsActivity.class);
            searchIntent.putExtra("query", query);
            startActivity(searchIntent);
        }
    }
}

