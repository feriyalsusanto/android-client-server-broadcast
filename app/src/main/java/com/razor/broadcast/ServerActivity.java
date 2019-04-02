package com.razor.broadcast;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerActivity extends AppCompatActivity {

    Thread socketThread;
    Handler serverHandler;
    Runnable serverRunnable;

//    Thread socketListenThread;
//    DatagramSocket mListenSocket;

    int server_port = 50008;
//    int listen_port = 8080;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Server server = Server.getInstance();
        server.init();

        serverHandler = new Handler();
        serverRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String ip_device = ConnectionUtils.getDeviceIp(getApplicationContext());
                    String ip_broadcast = ConnectionUtils.getBroadcastAddress(getApplicationContext()).getHostAddress();

                    JSONObject message = new JSONObject();
                    message.put("broadcast_ip", ip_broadcast);
                    message.put("device_ip", ip_device);
                    message.put("device_name", getDeviceName());

                    Log.d("TAGTAG", "IP BROADCAST: " + ip_broadcast);
                    Log.d("TAGTAG", "MESSAGE BROADCAST: " + message.toString());

                    DatagramSocket socket = new DatagramSocket();
                    InetAddress local = InetAddress.getByName(ip_broadcast);

                    int msg_length = message.toString().length();
                    byte[] msg = message.toString().getBytes();

                    DatagramPacket packet = new DatagramPacket(msg, msg_length, local, server_port);
                    socket.send(packet);
                    Log.d("TAGTAG", "data send");
                } catch (Exception e) {
                    Log.d("TAGTAG", "data failed to send");
                    Log.d("TAGTAG", Log.getStackTraceString(e));
                }

                serverHandler.postDelayed(serverRunnable, 5000);
            }
        };

        socketThread = new Thread(serverRunnable);
        socketThread.start();

//        socketListenThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Log.d("TAGTAG", "Start Listening on Port: " + listen_port);
//
//                    if (mListenSocket == null)
//                        mListenSocket = new DatagramSocket(listen_port);
//
//                    byte[] recvBuffer = new byte[1024];
//
//                    DatagramPacket datagram = new DatagramPacket(recvBuffer, 1024);
//
//                    mListenSocket.receive(datagram);
//                    String message = new String(recvBuffer);
//                    Log.d("TAGTAG", "Received message: " + message);
//                    JSONObject object = new JSONObject(message);
//                    mListenSocket.bind(new InetSocketAddress(object.getString("connect_request_ip"), listen_port));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        socketListenThread.start();
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
