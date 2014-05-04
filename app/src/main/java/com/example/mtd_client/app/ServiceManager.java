package com.example.mtd_client.app;

import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.lang.Object;

/**
 * Created by Gzock on 2014/04/28.
 */
public class ServiceManager {

    private              ServiceConnection serviceConnection = null;
    private              ServiceReceiver   receiver          = null;
    private static final String            TAG               = "ServiceManager";
    private              Context           con               = null;
    private              WebSocketService  client            = null;
    private              Intent            intent            = null;
    private              String            selectedPj        = null;
    private              ViewGroup         _vg               = null;

    public ServiceManager() {
        // ServiceConnectionの用意
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                client = ((WebSocketService.WebSocketBinder)service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                client = null;
            }
        };
        receiver = new ServiceReceiver();
    }

    public void startWsService(Context context) {
        con = context;
        intent = new Intent(con, WebSocketService.class);
        ComponentName component = con.startService(intent);

    }

    public void bindWsService(Context context) {

        con = context;
        intent = new Intent(con, WebSocketService.class);

        IntentFilter filter = new IntentFilter(WebSocketService.ACTION);
        con.registerReceiver(receiver, filter);
        Boolean bool = con.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService Result ->" + bool.toString());
    }

    public ServiceReceiver getReceiver() { return receiver; }
    public ServiceConnection getServiceConnection() { return serviceConnection; }

    public boolean isConnected() {
        return client.isConnected();
    }
    public boolean isState() {
        if (client != null) {
            return true;
        } else {
            return false;
        }
    }

    //TODO ここは考えなおそう。contextからviewを取得する方法はない？
    public void setView (ViewGroup vg) {
        _vg = vg;
    }

    public void send(String str) {
        client.send(str);
        Log.d(TAG, "send(string) -> " + str);
    }

    public void send(byte[] data) {
        client.send(data);
        Log.d(TAG, "send(byte[]) -> " + data);
    }

    public void disConnect() {
        // サービス終了
        client.disConnect();
    }
    public void unBindWsService() {
        con.unbindService(this.getServiceConnection()); // バインド解除
        con.unregisterReceiver(this.getReceiver()); // レシーバー解除
    }
    public void stopWsService() {
        con.stopService(intent);
    }

    // Receiverクラス
    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {

            String rcvMessage = intent.getStringExtra("rcvMessage");
            final ArrayList<String> strList = toArrayStr(rcvMessage);

            if( strList.get(0).equals("pjList")) {
                Log.d(TAG, "*** Project List ***");
                strList.remove(0);

                final CharSequence[] chars = strList.toArray(new CharSequence[strList.size()]);

                new AlertDialog.Builder(context)
                        .setTitle("案件名を選択して下さい")
                        .setSingleChoiceItems(
                                chars,
                                0, // Initial
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d(TAG, strList.get(which) + " Selected");
                                        client.send("getTargetList," + strList.get(which));
                                        selectedPj = strList.get(which);
                                    }
                                }
                        )
                        .setPositiveButton("OK", null)
                        .show();
            } else if (strList.get(0).equals("tgtList")) {
                strList.remove(0);
                Intent i = new Intent(context, TargetListView.class);
                //Intent i = new Intent();
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //i.setClassName("com.example.mtd_client.app", "com.example.mtd_client.app.TargetListView");

                //ターゲットリスト詰め込む
                i.putExtra("TargetList", toSplitArray(strList));
                //選んだPJ名詰め込む
                i.putExtra("PJName", selectedPj );

                //unBindWsService();
                context.startActivity(i);

                //バインド解除
                context.unbindService(serviceConnection);
                context.unregisterReceiver(receiver);

            } else if(strList.get(0).equals("tgtListUpdate")) {

                final ArrayList<TargetListData> dataList = new ArrayList<TargetListData>();
                String[] temp = rcvMessage.split(",");
                for(int i = 1; i < temp.length; i += 4) {
                    TargetListData _data = new TargetListData();
                    _data.setId          ( temp[i] );
                    _data.setTargetName  ( temp[i + 1] );
                    _data.setBeforeAgter ( temp[i + 2] );
                    _data.setPicture     ( temp[i + 3] );
                    dataList.add         ( _data );
                }

                TargetListAdapter targetListAdapter = new TargetListAdapter(con, 0, dataList);
                ListView listView = (ListView) _vg.findViewById(R.id.targetListView);
                listView.setAdapter( targetListAdapter );

            }
            //((MySample)context).textview01.setText("count: " + counter);
        }

    }


    private ArrayList<String> toArrayStr(String message) {
        String[] strs = message.split(",");
        ArrayList<String> strList = new ArrayList<String>();

        for(int i = 0; i < strs.length; i++) {
            strList.add(strs[i]);
        }
        return strList;
    }

    private String toSplitArray(ArrayList<String> array) {
        String mergeStr = "";
        for (String str : array) {
            mergeStr += str + ",";
        }
        return mergeStr;
    }

}
