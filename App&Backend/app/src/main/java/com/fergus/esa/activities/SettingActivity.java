package com.fergus.esa.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.fergus.esa.R;
import com.fergus.esa.SharedPreferencesKeys;
import com.github.channguyen.rsv.RangeSliderView;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 29/01/2016
 */
public class SettingActivity extends ActionBarActivity {
	private static final int MIN_VALUE = 75;
	private static final int STEP = 15;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);
		initToolbar();

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		boolean notificationsAllowed = preferences.getBoolean(SharedPreferencesKeys.NOTIFICATIONS_ALLOWED, true);

		Switch switchNotifications = (Switch) findViewById(R.id.switchNotification);

		if (notificationsAllowed) {
			switchNotifications.setChecked(true);
		}
		switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					preferences.edit().putBoolean(SharedPreferencesKeys.NOTIFICATIONS_ALLOWED, true).apply();
				} else{
					preferences.edit().putBoolean(SharedPreferencesKeys.NOTIFICATIONS_ALLOWED, false).apply();
				}
			}
		});


		int summaryLength = preferences.getInt(SharedPreferencesKeys.SUMMARY_LENGTH, MIN_VALUE);

		final TextView textViewSummaryLength = (TextView) findViewById(R.id.textViewSummaryLength);
		textViewSummaryLength.setText(String.valueOf(summaryLength));

		RangeSliderView rangeSliderSummaryLength = (RangeSliderView) findViewById(R.id.rangeSliderSummaryLength);
		rangeSliderSummaryLength.setInitialIndex((summaryLength - MIN_VALUE) / STEP);

		final RangeSliderView.OnSlideListener listener = new RangeSliderView.OnSlideListener() {
			@Override
			public void onSlide(int index) {
				int progress = MIN_VALUE + STEP * index;
				textViewSummaryLength.setText(String.valueOf(progress));
				preferences.edit().putInt(SharedPreferencesKeys.SUMMARY_LENGTH, progress).apply();
			}
		};
		rangeSliderSummaryLength.setOnSlideListener(listener);
	}


	private void initToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Settings");
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}
}
