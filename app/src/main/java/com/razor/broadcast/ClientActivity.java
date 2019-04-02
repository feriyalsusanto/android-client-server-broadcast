package com.razor.broadcast;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    Thread clientThread;
    DatagramSocket mSocket;
    int server_port = 50008;
    int listen_port = 8080;

    private ListView lsvw_data;
    private Button btn_search;

    private ServerAdapter mAdapter;
    private List<List<String>> mServers;

    private Client mClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {

        } catch (Exception e) {
            Log.d("TAGTAG", Log.getStackTraceString(e));
        }

        lsvw_data = findViewById(R.id.lsvw_data);
        btn_search = findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchServer();
            }
        });

        lsvw_data.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ClientActivity.this);
                alert.setMessage("Apakah anda ingin terhubung dengan perangkat ini ?");
                alert.setTitle("Menghubungkan Perangkat");
                alert.setIcon(R.drawable.ic_connect);
                alert.setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.setPositiveButton("HUBUNGKAN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip = mServers.get(position).get(1);
                        Log.d("TAGTAG", "ip selected " + ip);

                        connect(ip);

                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = alert.create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clientThread.interrupt();
    }

    private void searchServer() {
        mServers = new ArrayList<>();
        progressDialog = new ProgressDialog(ClientActivity.this);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                clientThread.interrupt();
                dialog.dismiss();
            }
        });

        progressDialog.setMessage("Searching Server. . .");
        progressDialog.show();

        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isRepeat = true;
                try {
                    while (isRepeat) {
                        if (Thread.currentThread().isInterrupted())
                            return;

                        if (mSocket == null)
                            mSocket = new DatagramSocket(server_port);
                        else if (mSocket != null && mSocket.isClosed()) {
                            mSocket = new DatagramSocket(server_port);
                        }

                        String message;
                        byte[] messageByte = new byte[1500];

                        DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length);
                        mSocket.receive(packet);
                        message = new String(messageByte, 0, packet.getLength());
                        Log.d("TAGTAG", "message:" + message);
                        if (message != null && message.length() > 0) {
                            isRepeat = false;
                            List<String> server = new ArrayList<>();
                            JSONObject object = new JSONObject(message);
                            server.add(object.getString("device_name"));
                            server.add(object.getString("device_ip"));
                            mServers.add(server);

                            mAdapter = new ServerAdapter(ClientActivity.this, mServers);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lsvw_data.setAdapter(mAdapter);
                                }
                            });

                            mSocket.close();
                            progressDialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    Log.d("TAGTAG", Log.getStackTraceString(e));
                }
            }
        });
        clientThread.start();
    }

    private void connect(String address) {
        mClient = Client.getInstance(getApplicationContext(), address);
        boolean connected = mClient.init();
        if (connected) {
            startActivity(new Intent(ClientActivity.this, MessageTestActivity.class));
            Toast.makeText(ClientActivity.this, "Berhasil terhubung dengan perangkat", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ClientActivity.this, "Gagal terhubung dengan perangkat", Toast.LENGTH_SHORT).show();
        }
    }

    public class ServerAdapter extends ArrayAdapter {

        public ServerAdapter(Context context, List<List<String>> servers) {
            super(context, 0, servers);
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            List<String> servers = (List<String>) getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_server, parent, false);
            }

            TextView txvw_name = convertView.findViewById(R.id.txvw_name);
            TextView txvw_ip = convertView.findViewById(R.id.txvw_ip);

            txvw_name.setText(servers.get(0));
            txvw_ip.setText(servers.get(1));

            return convertView;
        }
    }
}
