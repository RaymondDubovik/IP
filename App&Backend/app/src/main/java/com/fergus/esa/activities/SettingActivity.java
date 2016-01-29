package com.fergus.esa.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fergus.esa.R;
import com.fergus.esa.SharedPreferencesKeys;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 29/01/2016
 */
public class SettingActivity extends ActionBarActivity {


	private static final int STEP = 5;


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
					Toast.makeText(SettingActivity.this, "enabled", Toast.LENGTH_SHORT).show();
					preferences.edit().putBoolean(SharedPreferencesKeys.NOTIFICATIONS_ALLOWED, true).apply();
				} else{
					preferences.edit().putBoolean(SharedPreferencesKeys.NOTIFICATIONS_ALLOWED, false).apply();
				}
			}
		});


		int summaryLength = preferences.getInt(SharedPreferencesKeys.SUMMARY_LENGTH, 80);

		final TextView textViewSummaryLength = (TextView) findViewById(R.id.textViewSummaryLength);
		textViewSummaryLength.setText(String.valueOf(summaryLength));
		SeekBar seekBarSummaryLength = (SeekBar) findViewById(R.id.seekBarSummaryLength);

		seekBarSummaryLength.setProgress(summaryLength - 80);
		seekBarSummaryLength.incrementProgressBy(20);
		seekBarSummaryLength.setMax(30);
		seekBarSummaryLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				progress = progress / STEP;
				progress = progress * STEP + 80;
				textViewSummaryLength.setText(String.valueOf(progress));
				preferences.edit().putInt(SharedPreferencesKeys.SUMMARY_LENGTH, progress).apply();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}


	private void initToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Settings");
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}
}
