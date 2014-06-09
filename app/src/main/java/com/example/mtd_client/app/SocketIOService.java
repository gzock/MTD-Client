package com.example.mtd_client.app;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.DisconnectCallback;
import com.koushikdutta.async.http.socketio.ErrorCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.JSONCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.koushikdutta.async.http.socketio.SocketIORequest;
import com.koushikdutta.async.http.socketio.StringCallback;
import com.koushikdutta.async.parser.JSONArrayParser;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SocketIOService extends Service {

    private final static String TAG = "SocketIOService";
    public static final String ACTION = "SocketIOService";
    public static final int PLOJECT_LIST = 1;
    public static final int UPDATE_TARGET_LIST = 2;

    private final static SocketIORequest req = new SocketIORequest("https:/192.168.0.5:443");
    private SocketIOClient socketio = null;
    private final IBinder mBinder = new SocketIOBinder();
    private SerializableJSONArray serialJArray = null;

    public SocketIOService() {
    }

    @Override
    public int onStartCommand  (Intent intent, int flags, int startId) {
        //super.onStart(intent, startId);

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            InputStream caInput = getResources().openRawResource(R.raw.server);
            Certificate ca;

            ca = cf.generateCertificate(caInput);
            Log.d(TAG, "ca=" + ((X509Certificate) ca).getSubjectDN());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setSSLContext(context);
            AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setTrustManagers(tmf.getTrustManagers());


            SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), "https://192.168.0.5:443", new ConnectCallback() {
            //client.connect(AsyncHttpClient.getDefaultInstance(), req, new ConnectCallback() {
                @Override
                public void onConnectCompleted(Exception ex, SocketIOClient client) {
                    if (ex != null) {
                        return;
                    }
                    Log.d(TAG, "Socket.io Connected");
                    socketio = client;

                    //Save the returned SocketIOClient instance into a variable so you can disconnect it later
                    client.setDisconnectCallback(new DisconnectCallback() {
                        @Override
                        public void onDisconnect(Exception e) {
                            Log.d(TAG, "Disconnected -> " + e.toString());
                        }
                    });
                    client.setErrorCallback(new ErrorCallback() {
                        @Override
                        public void onError(String error) {
                            Log.d(TAG, "Error -> " + error);
                        }
                    });
                    client.setJSONCallback(new JSONCallback() {
                        @Override
                        public void onJSON(JSONObject json, Acknowledge acknowledge) {

                        }
                    });
                    client.setStringCallback(new StringCallback() {
                        @Override
                        public void onString(String string, Acknowledge acknowledge) {

                        }
                    });

                    client.on("projectList", new EventCallback() {
                        @Override
                        public void onEvent(JSONArray argument, Acknowledge acknowledge) {

                            serialJArray = new SerializableJSONArray(argument);
                            Intent intent = new Intent(ACTION);
                            intent.putExtra("event", PLOJECT_LIST);
                            intent.putExtra("json", serialJArray);
                            sendBroadcast(intent);

                        }
                    });

                    client.on("updateTargetList", new EventCallback() {
                        @Override
                        public void onEvent(JSONArray argument, Acknowledge acknowledge) {

                            serialJArray = new SerializableJSONArray(argument);
                            Intent intent = new Intent(ACTION);
                            intent.putExtra("event", UPDATE_TARGET_LIST);
                            intent.putExtra("json", serialJArray);
                            sendBroadcast(intent);

                        }
                    });


                    //You need to explicitly specify which events you are interested in receiving
                    client.addListener("news", new EventCallback() {
                        @Override
                        public void onEvent(JSONArray argument, Acknowledge acknowledge) {

                        }
                    });

                }

            });
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }


        return START_STICKY_COMPATIBILITY;

    }

    private HashMap<String, JSONArray> toSplitArray(JSONArray array) {
        String mergeStr = "";
        JSONArray jObj = new JSONArray();
        String tempStr = null;
        String tempStr2 = null;
        int n = 0;
        int j = 0;
        HashMap<String, JSONArray> map = new HashMap<String, JSONArray>();

        try {
           // map.put("json", array.getJSONArray(0));
            /*
            for (int i = 0; i <= array.length(); i++) {
                jObj = array.getJSONArray(i);
                tempStr += jObj.toString();
                tempStr2 = array.get(0).toString();
            }
            */

        }catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        return map;
    }

    public SocketIOClient getSocketIOClinet() {
        return socketio;
    }

    // Binder内部クラス
    class SocketIOBinder extends Binder {

        SocketIOService getService() {
            return SocketIOService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
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
