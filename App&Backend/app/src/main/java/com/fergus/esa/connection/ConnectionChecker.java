package com.fergus.esa.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 20.01.2016
 */
public class ConnectionChecker {
    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
