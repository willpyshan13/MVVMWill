package com.will.habit.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 电话广播接收
 */
public class PhoneListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("PhoneListener", action);
        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {

        } else {

        }
    }
}