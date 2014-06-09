package com.example.mtd_client.app;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;



import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebSocketService extends Service {

    private static final String TAG = "WebSocketService";

    WebSocketClient client;

    public static final String ACTION = "WebSocketService";

    private final IBinder mBinder = new WebSocketBinder();

    public WebSocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand  (Intent intent, int flags, int startId) {
        //super.onStart(intent, startId);

        List<BasicNameValuePair> extraHeaders = Arrays.asList(
                new BasicNameValuePair("Cookie", "session=abcd")
        );

        //mHandler = new Handler();

        client = new WebSocketClient(URI.create("ws://192.168.0.5:8000"), new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
                Log.d(TAG, "Connected!");
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, String.format("Got string message! %s", message));

                Intent intent = new Intent(ACTION);
                intent.putExtra("message", message);
                sendBroadcast(intent);

            }

            @Override
            public void onMessage(byte[] data) {
                Log.d(TAG, String.format("Got binary message! %s", new String(data)));
            }

            @Override
            public void onDisconnect(int code, String reason) {
                Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));

                Intent intent = new Intent(ACTION);
                intent.putExtra("message", "disConnect");
                sendBroadcast(intent);
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error!", error);

                Intent intent = new Intent(ACTION);
                intent.putExtra("message", "error");
                sendBroadcast(intent);
            }

        }, extraHeaders);

        client.connect();

        return START_STICKY_COMPATIBILITY;

    }

    public void connect() { client.connect(); };
    public boolean isConnected() {
        return client.isConnected();
    }
    public void disConnect() { client.disconnect(); };

    public void send(String str) {
        client.send(str);
    }

    public void send(byte[] data) {
        client.send(data);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        //return new WebSocketBinder();
        return mBinder;
    }

    // Binder内部クラス
    class WebSocketBinder extends Binder {

        WebSocketService getService() {
            return WebSocketService.this;
        }

    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //client.disconnect();
        return true;
    }


}

