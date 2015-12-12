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
import com.fergus.esa.backend.esaEventEndpoint.model.ESAEvent;
import com.fergus.esa.dataObjects.CategoryObject;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private SlidingUpPanelLayout slidingPanel;
    private View viewListCover;
    private TextView textViewCategory;
    private ListView listViewCategories;
    private SwipeRefreshLayout swipeContainer;

    private CategoryStorer categoryStorer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryStorer = new CategoryStorer(this);

        setContentView(R.layout.activity_main);

        initToolbar();

        EventAsyncTask eventTask = new EventAsyncTask(true);  //can pass other variables as needed
        eventTask.execute();

        listViewCategories = (ListView) findViewById(R.id.listViewCategories);
        textViewCategory = (TextView) findViewById(R.id.textViewSelectedCategory);

        fetchCategories();
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


    private void fetchCategories() {
        // List<CategoryObject> categories = CategoryParser.parse(jsonObject.getJSONArray(JsonParser.PARAM_DATA));
        List<CategoryObject> categories = new ArrayList<>();
        categories.add(new CategoryObject(1, "Category 1"));
        categories.add(new CategoryObject(2, "Category 2"));
        categories.add(new CategoryObject(3, "Category 3"));
        categories.add(new CategoryObject(4, "Category 4"));
        categories.add(new CategoryObject(5, "Category 5"));
        categories.add(new CategoryObject(6, "Category 6"));
        categories.add(new CategoryObject(7, "Category 7"));
        categories.add(new CategoryObject(8, "Category 8"));
        categories.add(new CategoryObject(9, "Category 9"));
        categories.add(new CategoryObject(10, "Category 10"));
        categories.add(0, new CategoryObject(CategoryObject.ALL_CATEGORIES_ID, CategoryObject.ALL_CATEGORIES_NAME));

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
        int selectedCategory = preferences.getInt(SharedPreferencesKeys.CATEGORY_ID, CategoryObject.ALL_CATEGORIES_ID);

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

        TypedValue typedValue = new  TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        swipeContainer.setColorSchemeColors(typedValue.data);
    }


    private void changeCategory(CategoryObject category) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        preferences.edit().putInt(SharedPreferencesKeys.CATEGORY_ID, category.getId()).apply();
        textViewCategory.setText(category.getName());
        // slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        // TODO: finish;
    }


    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            Intent searchIntent = new Intent(MainActivity.this, SearchResultsActivity.class);
            searchIntent.putExtra("query", query);
            startActivity(searchIntent);
        }
    }


    private class EventAsyncTask extends AsyncTask<Void, Void, List<ESAEvent>> {
        private EsaEventEndpoint myApiService = null;
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
        protected List<ESAEvent> doInBackground(Void... params) {
            if (myApiService == null) {  // Only do this once
                EsaEventEndpoint.Builder builder = new EsaEventEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null).setRootUrl(ServerUrls.ROOT_URL);

                myApiService = builder.build();
            }

            try {
                return myApiService.listEvents().execute().getItems();
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.EMPTY_LIST;
            }
        }


        @Override
        protected void onPostExecute(final List<ESAEvent> events) {
            Collections.reverse(events);

            GridView gv = (GridView) findViewById(R.id.gridView);
            gv.setAdapter(new GridViewAdapter(MainActivity.this, MainActivity.this, events));
            gv.setOnScrollListener(new ScrollListener(MainActivity.this));
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String event = events.get(position).getEvent();
                    Intent intent = new Intent(MainActivity.this, EventTabsActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("event", event);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });
            if (displayDialog) {
                pd.hide();
            }

            if (swipeContainer.isRefreshing()) {
                swipeContainer.setRefreshing(false);
            }
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
        public void onPanelAnchored(View view) {
        }


        @Override
        public void onPanelHidden(View view) {
        }
    }
}

