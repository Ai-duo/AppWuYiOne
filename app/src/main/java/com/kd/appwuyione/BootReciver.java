package com.kd.appwuyione;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent activity = new Intent(context,MainActivity.class);
        activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activity);
    }
}
