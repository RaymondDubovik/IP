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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.fergus.esa.adapters.CategoryAdapter;
import com.fergus.esa.adapters.GridViewAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.CategoryObject;
import com.fergus.esa.backend.esaEventEndpoint.model.EventObject;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private boolean loadRequired;
    private ConnectionErrorView connectionErrorView;

    private SlidingUpPanelLayout slidingPanel;
    private View viewListCover;
    private CategoryStorer categoryStorer;

    private ViewGroup categoryLayout;
    private TextView textViewCategory;
    private int textViewCategoryHeight;
    private int textViewCategoryCurrentHeight;
    private ListView listViewCategories;

    private SwipeRefreshLayout swipeContainer;
    private GridView gridViewEvent;
    private GridViewAdapter eventAdapter;
    private List<EventObject> allEvents;
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

        listViewCategories = (ListView) findViewById(R.id.listViewCategories);
        textViewCategory = (TextView) findViewById(R.id.textViewSelectedCategory);
        textViewCategory.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                textViewCategoryHeight = textViewCategory.getHeight();
            }
        });
        categoryLayout = (ViewGroup) findViewById(R.id.categoryPanel);
        categoryStorer = new CategoryStorer(this);

        gridViewEvent = (GridView) findViewById(R.id.gridView);

        connectionErrorView = new ConnectionErrorView(this, findViewById(R.id.linearLayoutConnectionErrorPanel), new RetryListener() {
            @Override
            public void onRetry() {
                getData();
            }
        });

        getData();

        handleIntent(getIntent());
        gcmRegister();
    }


    private void getData() {
        if (!ConnectionChecker.hasInternetConnection(this)) {
            loadRequired = true;
            connectionErrorView.show(R.string.no_internet);

            return;
        }

        loadRequired = false;
        new EventAsyncTask(true).execute();
        new CategoryAsyncTask().execute();
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
        if (connectionErrorView!= null && connectionErrorView.isVisible()) {
            connectionErrorView.hide();
        }
    }


    public void onInternetConnected() {
        if (loadRequired) {
            if (connectionErrorView.isVisible()) {
                connectionErrorView.hide();
            }

            getData();
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
            try {
                return ServerUrls.endpoint.getEvents(0, 10).execute().getItems(); // TODO: change
            } catch (IOException e) {
                if (connectionErrorView.isVisible()) {
                    connectionErrorView.quickHide();
                }

                connectionErrorView.show("Could not get data"); // TODO: remove hardcode

                loadRequired = true;

                e.printStackTrace();
                return Collections.EMPTY_LIST;
            }
        }


        @Override
        protected void onPostExecute(List<EventObject> events) {
            Collections.reverse(events);

            if (allEvents == null) {
                allEvents = new ArrayList<>(events);
                eventAdapter = new GridViewAdapter(MainActivity.this, MainActivity.this, events);
                gridViewEvent.setAdapter(eventAdapter);
            } else {
                eventAdapter.addItems(events);
            }

            allEvents.addAll(events);

            gridViewEvent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int eventId = allEvents.get(position).getId(); // TODO: get rid of allEvents variable
                    Intent intent = new Intent(MainActivity.this, EventActivity.class);
                    Bundle extras = new Bundle();
                    extras.putInt(EventActivity.BUNDLE_PARAM_EVENT_ID, eventId);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });
            gridViewEvent.setOnScrollListener(new GridViewScrollListener());

            if (displayDialog) {
                pd.hide();
            }

            if (swipeContainer != null && swipeContainer.isRefreshing()) {
                swipeContainer.setRefreshing(false);
            }
        }


        private class GridViewScrollListener extends CompositeScrollListener {
            public GridViewScrollListener() {
                addOnScrollListener(new ScrollListener(MainActivity.this));
                addOnScrollListener(new InfiniteScrollListener(6) {
                    @Override
                    public void loadMore(int page, int totalItemsCount) {
                        new EventAsyncTask(true).execute();
                    }
                });
                addOnScrollListener(new PixelScrollDetector(new PixelScrollDetector.PixelScrollListener() {
                    @Override
                    public void onScroll(AbsListView view, float deltaY) {
                        textViewCategoryCurrentHeight -= deltaY;
                        if (textViewCategoryCurrentHeight < 0) {
                            textViewCategoryCurrentHeight = 0;
                        } else if (textViewCategoryCurrentHeight > textViewCategoryHeight) {
                            textViewCategoryCurrentHeight = textViewCategoryHeight;
                        }

                        categoryLayout.setTranslationY(textViewCategoryCurrentHeight);
                    }
                }));
            }
        }
    }


    private class CategoryAsyncTask extends AsyncTask<Void, Void, List<CategoryObject>> {
        @Override
        protected List<CategoryObject> doInBackground(Void... voids) {
            List<CategoryObject> categories;
            try {
                categories = ServerUrls.endpoint.getCategories().execute().getItems();
            } catch (IOException e) {
                if (connectionErrorView.isVisible()) {
                    connectionErrorView.quickHide();
                }

                connectionErrorView.show("Could not get data"); // TODO: remove hardcode

                loadRequired = true;

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
                    getData();
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
            textViewCategoryCurrentHeight = 0;
            categoryLayout.setTranslationY(textViewCategoryCurrentHeight);

            if (wasCollapsed) {
                wasCollapsed = false;
            }
        }


        @Override
        public void onPanelCollapsed(View view) {
            viewListCover.setVisibility(View.INVISIBLE);
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
}

