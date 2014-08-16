package jp.fkmsoft.demo.fitdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Receiver for session request
 */
public class SessionReceiver extends BroadcastReceiver {
    private static final String TAG = "sessionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        Toast.makeText(context, "Session is started.", Toast.LENGTH_LONG).show();
    }
}
