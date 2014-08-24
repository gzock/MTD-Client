package com.example.mtd_client.app;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.libcore.RawHeaders;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.DisconnectCallback;
import com.koushikdutta.async.http.socketio.ErrorCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.JSONCallback;
import com.koushikdutta.async.http.socketio.ReconnectCallback;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SocketIOService extends Service {

    private final static String TAG = "SocketIOService";
    public static final String ACTION = "SocketIOService";
    public static final int PLOJECT_LIST = 1;
    public static final int UPDATE_TARGET_LIST = 2;
    public static final int PHOTO_CONFIRM = 3;
    public static final int DISCONNECTED= 10;

    private final static SocketIORequest req = new SocketIORequest("https:/192.168.0.5:443");
    private SocketIOClient socketio = null;
    private final IBinder mBinder = new SocketIOBinder();
    private SerializableJSONArray serialJArray = null;
    Timer mTimer = null;

    ArrayList<SendJobData> jobManageArray = new ArrayList<SendJobData>();
    private String authId = null;
    private SSLContext sslContext = null;
    private TrustManagerFactory tmf = null;

    public SocketIOService() {
    }

    @Override
    public int onStartCommand  (Intent intent, int flags, int startId) {
        //super.onStart(intent, startId);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        final String serverIpaddress   = preferences.getString("connect_server_ipaddress", "192.168.0.5");
        final int serverPort           = Integer.parseInt(preferences.getString("connect_server_port", "443"));
        int reconnectInterval          = Integer.parseInt(preferences.getString("reconnect_interval", "10")) * 1000;

        startSocketIO(serverIpaddress, serverPort);

        mTimer = new Timer(true);
        mTimer.schedule( new TimerTask(){
            @Override
            public void run() {
                Log.d(TAG, "Socket.IO Connect Check...");

                if( socketio != null && socketio.isConnected() ) {
                    Log.d(TAG, "Socket.IO Connected. Start the send of the send waiting job");
                    if (0 < jobManageArray.size()) {
                        Log.d(TAG, "Send Waiting Job Count -> " + jobManageArray.size());
                        for (SendJobData item : jobManageArray) {
                            if (socketio.isConnected()) {
                               socketio.emit("addPhoto", item.getSendaDataJson());
                            } else {
                                Log.d(TAG, "Socket.IO Not Connected");
                                break;
                            }
                        }
                    } else {
                        Log.d(TAG, "No Send Waiting Job...");
                    }
                } else {
                    Log.d(TAG, "Socket.IO Not Connected... ReConnection Process Start");
                    Intent intent = new Intent(ACTION);
                    intent.putExtra("event", DISCONNECTED);
                    sendBroadcast(intent);

                    startSocketIO(serverIpaddress, serverPort);
                }
            }
        }, reconnectInterval, reconnectInterval);


        return START_STICKY_COMPATIBILITY;

    }

    private TrustManagerFactory getSslTrustManager() {
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

            return tmf;

        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return null;
        }
    }

    private void startSocketIO(String ipaddress, int port) {
        try {

            if( sslContext == null || tmf == null ) {
                sslContext = SSLContext.getInstance("TLS");
                tmf = this.getSslTrustManager();
                sslContext.init(null, tmf.getTrustManagers(), null);
            }
            AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setSSLContext(sslContext);
            AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setTrustManagers(tmf.getTrustManagers());

            SocketIORequest _req = new SocketIORequest( "https://" + ipaddress + ":" + port );
            if( authId != null ) {
                Log.d(TAG, "Found AuthID");
                Log.d(TAG, "AuthID -> " + authId);
                _req.addHeader("auth_id", authId);
            } else {
                Log.d(TAG, "Not Found AuthID...");
            }
            SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), _req, new ConnectCallback() {
                //client.connect(AsyncHttpClient.getDefaultInstance(), req, new ConnectCallback() {
                @Override
                public void onConnectCompleted(Exception ex, final SocketIOClient client) {
                    if (ex != null) {
                        return;
                    }
                    Log.d(TAG, "Socket.io Connected");
                    socketio = client;


                    client.setReconnectCallback(new ReconnectCallback() {
                        @Override
                        public void onReconnect() {
                            Log.d(TAG, "Socket.IO ReConnected !");

                        }
                    });

                    //Save the returned SocketIOClient instance into a variable so you can disconnect it later
                    client.setDisconnectCallback(new DisconnectCallback() {
                        @Override
                        public void onDisconnect(Exception e) {
                            Log.d(TAG, "Disconnected -> " + e.toString());
                            Intent intent = new Intent(ACTION);
                            intent.putExtra("event", DISCONNECTED);
                            sendBroadcast(intent);
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

                    client.on("authId", new EventCallback() {
                        @Override
                        public void onEvent(JSONArray argument, Acknowledge acknowledge) {

                            try {
                                Log.d(TAG, "AuthID Taught");
                                authId = argument.getJSONObject(0).getString("auth_id");
                                Log.d(TAG, "Auth ID -> " + authId);
                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        }
                    });

                    client.on("addPhotoSuccess", new EventCallback() {
                        @Override
                        public void onEvent(JSONArray argument, Acknowledge acknowledge) {
                            try {
                                String str = argument.getJSONObject(0).getString("target_id");
                                Log.d(TAG, "addPhotoSuccess -> " + str);
                                for( int i = 0; i < jobManageArray.size(); i++ ) {
                                    if( str.equals( jobManageArray.get(i).getTargetId() ) ) {
                                        jobManageArray.remove(i);
                                        Log.d(TAG, "JobRemove -> " + str);
                                    }
                                }
                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        }
                    });

                    client.on("photoConfirmTransfer", new EventCallback() {
                        @Override
                        public void onEvent(JSONArray argument, Acknowledge acknowledge) {
                            serialJArray = new SerializableJSONArray(argument);
                            Intent intent = new Intent(ACTION);
                            intent.putExtra("event", PHOTO_CONFIRM);
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
    }

    public void addSendJob( SendJobData item ) {
        jobManageArray.add( item );
        if( socketio.isConnected() ) {
            if( jobManageArray.size() == 1 ) {
               socketio.emit( "addPhoto", item.getSendaDataJson() );
            }
        }
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
