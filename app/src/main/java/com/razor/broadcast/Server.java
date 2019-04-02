package com.razor.broadcast;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {
    static Server _instance;

    private ServerSocket serverSocket;

    Handler responseRequestHandler;

    Thread serverThread = null;
    public static final int SERVERPORT = 6000;

    public static Server getInstance() {
        if (_instance == null) {
            _instance = new Server();
        }

        return _instance;
    }

    public Server() {
        responseRequestHandler = new Handler();
    }

    public void init() {
        serverThread = new Thread(new ServerThread());
        serverThread.start();
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ServerThread extends Thread {
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CommunicationThread extends Thread {
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
                        responseRequestHandler.post(new responseRequestThread(clientSocket, read));
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

    private class responseRequestThread implements Runnable {
        private Socket clientSocket;
        private String request;

        public responseRequestThread(Socket clientSocket, String request) {
            this.clientSocket = clientSocket;
            this.request = request;
        }

        @Override
        public void run() {
            try {
                Log.d("TAGTAG", "message: " + request);
                PrintWriter out;
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream())),
                        true);
                if (request.equals("get:products")) {
                    out.println("product list goes here");
                } else {
                    out.println("Hello, I'm a server");
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
