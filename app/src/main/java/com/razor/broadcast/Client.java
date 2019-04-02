package com.razor.broadcast;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    static Context mContext;
    static Client _instance;
    private Socket socket;

    private static final int SERVERPORT = 6000;
    private static String SERVER_IP;

    public static Client getInstance() {
        return _instance;
    }

    public static Client getInstance(Context context, String server_ip) {
        if (_instance == null) {
            mContext = context;
            _instance = new Client(server_ip);
        }

        return _instance;
    }

    public Client(String server_ip) {
        SERVER_IP = server_ip;
    }

    public boolean init() {
        try {
            ClientThread thread = new ClientThread();
            thread.execute();
            return thread.get();
        } catch (Exception e) {
        }

        return false;
    }

    public void sendMessage(String message) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(message);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean clientThread() {
        boolean success = false;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            socket = new Socket(serverAddr, SERVERPORT);

            success = true;

            CommunicationThread commThread = new CommunicationThread(socket);
            new Thread(commThread).start();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return success;
    }

    class ClientThread extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            return clientThread();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (read != null) {
                        Intent intent = new Intent("MESSAGE_REQUEST:connect");
                        intent.putExtra("message", read);
                        mContext.sendBroadcast(intent);
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
