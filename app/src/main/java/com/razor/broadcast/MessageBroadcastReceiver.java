package com.razor.broadcast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MessageBroadcastReceiver extends BroadcastReceiver {

    private Activity activity;

    public MessageBroadcastReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAGTAG", "action: " + intent.getAction());

        switch (intent.getAction()) {
            case "MESSAGE_REQUEST:api":
                Toast.makeText(context, "return api", Toast.LENGTH_LONG).show();
                break;
            case "MESSAGE_REQUEST:connect":
                ((MessageTestActivity) activity).setMessageText(intent.getStringExtra("message"));
                break;
            default:
                Toast.makeText(context, intent.getAction() + " received", Toast.LENGTH_LONG).show();
        }
    }
}
