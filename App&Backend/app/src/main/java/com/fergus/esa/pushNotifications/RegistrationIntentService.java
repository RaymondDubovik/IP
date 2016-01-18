/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fergus.esa.pushNotifications;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fergus.esa.R;
import com.fergus.esa.ServerUrls;
import com.fergus.esa.SharedPreferencesKeys;
import com.fergus.esa.backend.esaEventEndpoint.model.UserObject;
import com.fergus.esa.dataObjects.UserObjectWrapper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {
    private static final String REGISTRATION_COMPLETE = "registrationComplete";

    private static final String TAG = "RegIntentService";


    public RegistrationIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously, ensure that they are processed sequentially.
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                if (!token.equals(sharedPreferences.getString(SharedPreferencesKeys.GCM_TOKEN, null))) {
                    int userId = sharedPreferences.getInt(SharedPreferencesKeys.USER_ID, UserObjectWrapper.NO_USER_ID);

                    if (userId == UserObjectWrapper.NO_USER_ID) {
                        try {
                            UserObject user = ServerUrls.endpoint.registerGcmToken(token).execute();
                            sharedPreferences.edit().putInt(SharedPreferencesKeys.USER_ID, user.getId());
                            sharedPreferences.edit().putString(SharedPreferencesKeys.GCM_TOKEN, token).apply();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            ServerUrls.endpoint.updateGcmToken(userId, token).execute();
                            // TODO: verify, that the server updated the token
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d(TAG, "GCM token:" + token);
            }
        } catch (Exception e) {
            sharedPreferences.edit().putString(SharedPreferencesKeys.GCM_TOKEN, null).apply();
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
