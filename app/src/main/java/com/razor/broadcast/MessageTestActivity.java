package com.razor.broadcast;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MessageTestActivity extends AppCompatActivity {

    MessageBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    TextView txvw_message;
    EditText edtx_message;
    ImageButton imbt_send;

    Client mClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_test);

        mClient = Client.getInstance();

        mReceiver = new MessageBroadcastReceiver(this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("MESSAGE_REQUEST:api");
        mIntentFilter.addAction("MESSAGE_REQUEST:connect");

        txvw_message = findViewById(R.id.txvw_message);
        edtx_message = findViewById(R.id.edtx_message);
        imbt_send = findViewById(R.id.imbt_send);

        imbt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = edtx_message.getText().toString();
                mClient.sendMessage(message);

                txvw_message.setText(txvw_message.getText().toString() + "\n" + message);
                edtx_message.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void setMessageText(String message) {
        txvw_message.setText(txvw_message.getText().toString() + "\n" + message);
    }
}
