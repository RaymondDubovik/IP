package com.fergus.esa.pushNotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.fergus.esa.SharedPreferencesKeys;

/**
 * Created by svchost on 2015.06.26..
 */
public class InternetStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (isConnected(context) && sharedPreferences.getString(SharedPreferencesKeys.GCM_TOKEN, null) == null) {
            Intent service = new Intent(context, RegistrationIntentService.class);
            context.startService(service);
        }
    }


    public boolean isConnected(Context context) {
        NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
    }
}
