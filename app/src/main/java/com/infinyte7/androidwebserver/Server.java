package com.infinyte7.androidwebserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import static com.infinyte7.androidwebserver.Constants.*;

class Server extends Thread {
    private ServerSocket listener = null;
    private boolean running = true;
    private final String documentRoot;
    private static Handler mHandler;
    private final Context context;

    private static final LinkedList<Socket> clientList = new LinkedList<>();

    public Server(Handler handler, String documentRoot, String ip, int port, Context context) throws IOException {
        super();
        this.documentRoot = documentRoot;
        this.context = context;
        Server.mHandler = handler;
        InetAddress ipAddress = InetAddress.getByName(ip);
        listener = new ServerSocket(port,0,ipAddress);
    }

    @Override
    public void run() {
        while( running ) {
            try {
                Socket client = listener.accept();
                new ServerHandler(documentRoot, context, client, Server.mHandler).start();
                clientList.add(client);
            } catch (IOException e) {
                // Don't set running=false at this point!
                // Give the server chance to create new socket if it is temporary problem
                // This lead repeating message 'Socket closed' message many (more then 100) times
                // but they suppressed by '... previous string repeated x times' logging functionality.
                MainActivity.putToLogScreen("I: " + e.getMessage(), mHandler);
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    public void stopServer() {
        running = false;
        try {
            listener.close();
        } catch (IOException e) {
            MainActivity.putToLogScreen("E: " + e.getMessage(), mHandler);
            Log.e("SAWS", e.getMessage());
        }
    }

    public synchronized static void remove(Socket s) {
        clientList.remove(s);
    }

}
