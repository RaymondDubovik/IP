package com.fergus.esa.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fergus.esa.CategoryStorer;
import com.fergus.esa.ErrorAsyncTask;
import com.fergus.esa.R;
import com.fergus.esa.ServerUrls;
import com.fergus.esa.SharedPreferencesKeys;
import com.fergus.esa.activities.EventActivity;
import com.fergus.esa.activities.MainActivity;
import com.fergus.esa.adapters.CategoryAdapter;
import com.fergus.esa.adapters.GridViewAdapter;
import com.fergus.esa.backend.esaEventEndpoint.model.CategoryObject;
import com.fergus.esa.backend.esaEventEndpoint.model.EventObject;
import com.fergus.esa.backend.esaEventEndpoint.model.EventObjectCollection;
import com.fergus.esa.connection.ConnectionChecker;
import com.fergus.esa.connection.ConnectionErrorView;
import com.fergus.esa.connection.RetryListener;
import com.fergus.esa.dataObjects.CategoryObjectWrapper;
import com.fergus.esa.dataObjects.UserObjectWrapper;
import com.fergus.esa.listeners.CompositeScrollListener;
import com.fergus.esa.listeners.InfiniteScrollListener;
import com.fergus.esa.listeners.PixelScrollDetector;
import com.fergus.esa.listeners.ScrollListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 29/01/2016
 */
// TODO: rewrite using the inheritance instead of switch/case statements
public class EventsFragment extends Fragment implements NetworkFragment, BackButtonFragment {
	public static final int TYPE_EVENTS_NEW = 1;
	public static final int TYPE_EVENTS_RECOMMENDED = 2;
	public static final int TYPE_EVENTS_DEFAULT = TYPE_EVENTS_NEW;

	private int type = TYPE_EVENTS_DEFAULT;

	private boolean wasLaunched = false;
	// quick and dirty....
	private ProgressDialog progressDialog;

	private boolean loadRequired;

	private SlidingUpPanelLayout slidingPanel;
	private View viewListCover;
	private CategoryStorer categoryStorer;

	private ViewGroup categoryLayout;
	private TextView textViewCategory;
	private int textViewCategoryHeight;
	private int textViewCategoryCurrentHeight;
	private ListView listViewCategories;

	private ProgressBar progressBarEvents;

	private long currentTimestamp = new Date().getTime();

	private SwipeRefreshLayout swipeContainer;
	private GridViewWithHeaderAndFooter gridViewEvent;
	private GridViewAdapter eventAdapter;
	private MainActivity activity;
	private ConnectionErrorView connectionErrorView;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_new_events, container, false);

		activity = (MainActivity) getActivity();
		activity.setNetworkFragment(this);
		activity.setBackButtonFragment(this);

		connectionErrorView = activity.getConnectionErrorView();
		connectionErrorView.registerOnRetryListerner(new RetryListener() {
			@Override
			public void onRetry() {
				getData(true);
			}
		});

		categoryStorer = new CategoryStorer(activity);

		gridViewEvent = (GridViewWithHeaderAndFooter) view.findViewById(R.id.gridView);
		View footerView = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_event_footer, null, false);
		gridViewEvent.addFooterView(footerView);
		progressBarEvents = (ProgressBar) footerView.findViewById(R.id.progressBarEvents);
		if (type == TYPE_EVENTS_RECOMMENDED) {
			progressBarEvents.setVisibility(View.GONE);
		}

		slidingPanel = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
		slidingPanel.setPanelSlideListener(new SlidingPanelListener());
		viewListCover = view.findViewById(R.id.viewEventListCover);

		listViewCategories = (ListView) view.findViewById(R.id.listViewCategories);
		textViewCategory = (TextView) view.findViewById(R.id.textViewSelectedCategory);
		textViewCategory.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				textViewCategoryHeight = textViewCategory.getHeight();
			}
		});
		categoryLayout = (ViewGroup) view.findViewById(R.id.categoryPanel);

		if (type == TYPE_EVENTS_RECOMMENDED) {
			categoryLayout.setVisibility(View.GONE);
		}

		swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
		swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				currentTimestamp = new Date().getTime();
				getData(false);
			}
		});

		TypedValue typedValue = new TypedValue();
		activity.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
		swipeContainer.setColorSchemeColors(typedValue.data);

		getData();

		return view;
	}


	@Override
	public void onPause() {
		super.onPause();
		hideUi();
	}


	private void getData(boolean displayDialog) {
		if (!ConnectionChecker.hasInternetConnection(activity)) {
			loadRequired = true;
			if (wasLaunched || isFragmentVisible()) {
				connectionErrorView.show(R.string.no_internet);
			}

			return;
		}

		loadRequired = false;
		new EventAsyncTask(displayDialog, true).execute();
		new CategoryAsyncTask().execute();
	}


	private boolean isFragmentVisible() {
		return activity.getCurrentFragment() == this;
	}


	private void getData() {
		getData(true);
	}


	private void changeActiveCategory() {
		int count = categoryStorer.getCount();

		if (count <= 0) {
			textViewCategory.setText(CategoryObjectWrapper.ALL_CATEGORIES_NAME);
			return;
		}

		if (count == 1) {
			String text = null;
			CategoryAdapter adapter = (CategoryAdapter) listViewCategories.getAdapter();
			for (int i = 0; i < adapter.getCount(); i++) {
				CategoryObject category = adapter.getItem(i);
				if (categoryStorer.hasCategory(category)) {
					text = category.getName();
					break;
				}
			}

			if (text == null) {
				textViewCategory.setText(CategoryObjectWrapper.ALL_CATEGORIES_NAME);
			} else {
				textViewCategory.setText(text);
			}
			return;
		}

		textViewCategory.setText(count + " categories");
	}


	private void hideUi() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}

		if (swipeContainer != null && swipeContainer.isRefreshing()) {
			swipeContainer.setRefreshing(false);
		}
	}


	@Override
	public void onInternetConnected() {
		if (loadRequired) {
			hideUi();
			if (connectionErrorView.isVisible()) {
				connectionErrorView.hide();
			}

			getData(true);
		}
	}


	@Override
	public boolean onBackPressed() {
		if (slidingPanel != null && (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
			slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			return true;
		}

		return false;
	}


	private class EventAsyncTask extends ErrorAsyncTask<Void, Void, List<EventObject>> {
		private static final int EVENT_COUNT_PER_PAGE = 10;
		private boolean displayDialog;
		private boolean replaceContent = false;


		EventAsyncTask(boolean displayDialog) {
			this(displayDialog, false);
		}


		EventAsyncTask(boolean displayDialog, boolean replaceContent) {
			this.displayDialog = displayDialog;
			this.replaceContent = replaceContent;
		}


		@Override
		protected void onPreExecute() {
			if (displayDialog && (wasLaunched || isFragmentVisible())) {
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				progressDialog = new ProgressDialog(activity);
				progressDialog.setTitle("Please Wait...");
				progressDialog.setMessage("Downloading Events...");
				progressDialog.show();
			}
		}


		@Override
		protected List<EventObject> doInBackground(Void... voids) {
			try {
				EventObjectCollection collection = null;

				switch (type) {
					case TYPE_EVENTS_NEW:
						collection = ServerUrls.endpoint.getNewEvents(currentTimestamp, EVENT_COUNT_PER_PAGE, categoryStorer.getSelectedCategoryIds()).execute();
						break;
					case TYPE_EVENTS_RECOMMENDED:
						int userId = PreferenceManager.getDefaultSharedPreferences(activity).getInt(SharedPreferencesKeys.USER_ID, UserObjectWrapper.NO_USER_ID);
						collection = ServerUrls.endpoint.getRecommendedEvents(userId, categoryStorer.getSelectedCategoryIds()).execute();
						break;
				}

				return (collection == null) ? null : collection.getItems();
			} catch (IOException | IllegalArgumentException e) {
				setError(true);
				// e.printStackTrace();
				Log.d("", "could not retrieve data");
				return Collections.EMPTY_LIST;
			}
		}


		@Override
		protected void onPostExecute(List<EventObject> events) {
			hideUi();
			wasLaunched = true;
			if (hasError()) {
				if (isFragmentVisible()) {
					if (connectionErrorView.isVisible()) {
						connectionErrorView.quickHide();
					}

					connectionErrorView.show("Could not get data"); // T0D0 remove hardcode
				}

				loadRequired = true;
				return;
			}

			if (events == null) {
				progressBarEvents.setVisibility(View.GONE);
				return;
			}

			if (eventAdapter == null || type == TYPE_EVENTS_RECOMMENDED || replaceContent) {
				eventAdapter = new GridViewAdapter(activity);
				gridViewEvent.setAdapter(eventAdapter);
			}

			eventAdapter.addItems(events);

			long minTimestamp = new Date().getTime();
			for (EventObject event : events) {
				long timestamp = event.getTimestamp().getValue();
				if (minTimestamp > timestamp) {
					minTimestamp = timestamp;
				}
			}

			currentTimestamp = minTimestamp;

			gridViewEvent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					EventObject event = eventAdapter.getItem(position);
					Intent intent = new Intent(activity, EventActivity.class);
					Bundle extras = new Bundle();
					extras.putInt(EventActivity.BUNDLE_PARAM_EVENT_ID, event.getId());
					extras.putString(EventActivity.BUNDLE_PARAM_EVENT_HEADING, event.getHeading());
					intent.putExtras(extras);
					startActivity(intent);
				}
			});

			gridViewEvent.setOnScrollListener(new GridViewScrollListener());

			if (type != TYPE_EVENTS_RECOMMENDED) {
				progressBarEvents.setVisibility(View.VISIBLE);
			}
		}


		private class GridViewScrollListener extends CompositeScrollListener {
			public GridViewScrollListener() {
				addOnScrollListener(new ScrollListener(activity));
				if (type != TYPE_EVENTS_RECOMMENDED) { // this disables infinite scrolling for recommended events
					addOnScrollListener(new InfiniteScrollListener(6) {
						@Override
						public void loadMore(int page, int totalItemsCount) {
							new EventAsyncTask(false).execute();
						}
					});
				}
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


	private class CategoryAsyncTask extends ErrorAsyncTask<Void, Void, List<CategoryObject>> {
		@Override
		protected List<CategoryObject> doInBackground(Void... voids) {
			List<CategoryObject> categories;
			try {
				categories = ServerUrls.endpoint.getCategories().execute().getItems();
			} catch (IOException | IllegalArgumentException e) {
				setError(true);
				// e.printStackTrace();
				Log.d("", "Could not retrieve data");
				return Collections.emptyList();
			}

			categories.add(0, new CategoryObject().setId(CategoryObjectWrapper.ALL_CATEGORIES_ID).setName(CategoryObjectWrapper.ALL_CATEGORIES_NAME));

			return categories;
		}


		@Override
		protected void onPostExecute(final List<CategoryObject> categories) {
			if (hasError()) {
				loadRequired = true;
				return;
			}

			CategoryAdapter categoryAdapter = new CategoryAdapter(activity, categories);
			listViewCategories.setAdapter(categoryAdapter);
			listViewCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					CategoryObject category =  (CategoryObject) listViewCategories.getItemAtPosition(position);

					if (listViewCategories.isItemChecked(position)) {
						categoryStorer.addCategory(category);

						// TODO: check if it is all categories here
						if (category.getId() == CategoryObjectWrapper.ALL_CATEGORIES_ID) {
							for (int i = 0; i < categories.size(); i++) {
								CategoryObject currentCategory = categories.get(i);
								if (currentCategory.getId() != CategoryObjectWrapper.ALL_CATEGORIES_ID) {
									categoryStorer.removeCategory(currentCategory);
								}
							}
						} else if (categoryStorer.hasCategory(new CategoryObject().setId(CategoryObjectWrapper.ALL_CATEGORIES_ID))) {
							for (int i = 0; i < categories.size(); i++) {
								CategoryObject currentCategory = categories.get(i);
								if (currentCategory.getId() == CategoryObjectWrapper.ALL_CATEGORIES_ID) {
									categoryStorer.removeCategory(currentCategory);
								}
							}
						}
					} else {
						categoryStorer.removeCategory(category);
					}

					changeActiveCategory();
					eventAdapter = null;
					currentTimestamp = new Date().getTime();
					getData(false);
				}
			});

			for (int i = 0; i < categories.size(); i++) {
				if (categoryStorer.hasCategory(categories.get(i))) {
					listViewCategories.setItemChecked(i, true);
				}
			}

			changeActiveCategory();
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


	public EventsFragment setType(int type) {
		this.type = type;
		return this;
	}
}
