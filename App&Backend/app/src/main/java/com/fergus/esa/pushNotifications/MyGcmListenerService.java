/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fergus.esa.pushNotifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fergus.esa.MainActivity;
import com.fergus.esa.R;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;


public class MyGcmListenerService extends GcmListenerService {
    public static final String DEFAULT_TITLE = "ESA";

    public static final String BUNDLE_TITLE_KEY = "push_title";
    public static final String BUNDLE_MESSAGE_KEY = "body";
    public static final String BUNDLE_SOUND_KEY = "sound";
    public static final String BUNDLE_DATA = "data";
    public static final String JSON_ID = "id";
    private static final String BUNDLE_NOTIFICATION = "notification";


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        data = data.getBundle(BUNDLE_NOTIFICATION);

        if (data == null) {
            return;
        }

        String title = data.getString(BUNDLE_TITLE_KEY, DEFAULT_TITLE);
        String message = data.getString(BUNDLE_MESSAGE_KEY);
        boolean playSound = (data.getString(BUNDLE_SOUND_KEY, null) != null);

        int id;
        JSONObject json;
        try {
            json = new JSONObject(data.getString(BUNDLE_DATA, "{\"id\":0}"));
            id = json.getInt(JSON_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Log.d("", Integer.toString(id));
        // TODO: implement storage in shared preferences

        showNotification(title, message, playSound);
    }


    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param title Title of the push notification. If null, then default title is displayed.
     * @param message GCM message received.
     * @param playSound Plays sound, if not null
     */
    private void showNotification(String title, String message, boolean playSound) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_small)
                // .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (playSound) {
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}