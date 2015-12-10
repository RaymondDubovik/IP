package com.fergus.esa.pushNotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by svchost on 2015.07.01..
 */
public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent pushIntent = new Intent(context, RegistrationIntentService.class);
        context.startService(pushIntent);
    }
}
