package com.example.mtd_client.app;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.os.Handler;

/**
 * Created by Gzock on 2014/04/21.
 */
public class ShareWebSocket extends Application {


    List<BasicNameValuePair> extraHeaders = Arrays.asList(
            new BasicNameValuePair("Cookie", "session=abcd")
    );

    WebSocketClient client;

    private static final String TAG = "ShareWebSocket";

    public ShareWebSocket(){}

    public ShareWebSocket(URI wsUrl, WebSocketClient.Listener listener, List<BasicNameValuePair> headers) {
        client = new WebSocketClient(wsUrl, listener, headers);
    }

    public void connect() {
        try {
            client.connect();
            Log.d(TAG, "WebSocket Connect...");
        }catch(Exception e){
            Log.d(TAG, "WebSocket Connect Failed -> " + e.toString());
        }
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void send(String str) {
        client.send(str);
        Log.d(TAG, "send(string) -> " + str);
    }

    public void send(byte[] data) {
        client.send(data);
        Log.d(TAG, "send(byte[]) -> " + data);
    }

    public ArrayList<String> toArrayStr(String message) {
        String[] strs = message.split(",");
        ArrayList<String> strList = new ArrayList<String>();

        for(int i = 0; i < strs.length; i++) {
            strList.add(strs[i]);
        }
        return strList;
    }

}

