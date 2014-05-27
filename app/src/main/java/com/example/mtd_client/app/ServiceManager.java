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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.lang.Object;
import java.util.Timer;
import java.util.TimerTask;

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
    private              Boolean           serviceState      = false;
    private              String            currentParentId   = null;

    ArrayList<String> parentArray = new ArrayList<String>();

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
        serviceState = true;
    }
    public void unBindWsService() {
        con.unbindService(this.getServiceConnection()); // バインド解除
        con.unregisterReceiver(this.getReceiver()); // レシーバー解除
        serviceState = false;
    }
    public Boolean isWsServiceState() {
        return serviceState;
    }

    public ServiceReceiver getReceiver() { return receiver; }
    public ServiceConnection getServiceConnection() { return serviceConnection; }

    public boolean isWsConnected() {
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

    public void stopWsService() {
        con.stopService(intent);
    }

    // Receiverクラス
    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {

            String rcvMessage = intent.getStringExtra("message");
            final ArrayList<String> strList = toArrayStr(rcvMessage);

            if( strList.get(0).equals("pjList")) {
                Log.d(TAG, "*** Project List ***");
                strList.remove(0);
                final ArrayList<String> pjNameList   = new ArrayList<String>();
                final ArrayList<String> pjRootTarget = new ArrayList<String>();

                if(strList.size() %  2 == 0) {
                    for (int i = 0; i < strList.size(); i++) {
                        if( (i % 2) == 0 || i == 0) {
                            pjNameList.add(strList.get(i));
                        } else {
                            pjRootTarget.add(strList.get(i));
                        }
                    }

                    final CharSequence[] chars = pjNameList.toArray(new CharSequence[pjNameList.size()]);
                    new AlertDialog.Builder(context)
                            .setTitle("案件名を選択して下さい")
                            .setSingleChoiceItems(
                                    chars,
                                    0, // Initial
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.d(TAG, strList.get(which) + " Selected");
                                            //別にpjName使わなくね？
                                            //client.send("getTargetList," + pjNameList.get(which) + "," + pjRootTarget.get(which));
                                            client.send("getTargetList," + pjRootTarget.get(which));
                                            selectedPj = strList.get(which);
                                        }
                                    }
                            )
                            .setPositiveButton("OK", null)
                            .show();
                }

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

                context.startActivity(i);

                //バインド解除
                context.unbindService(serviceConnection);
                context.unregisterReceiver(receiver);

            } else if(strList.get(0).equals("tgtListUpdate")) {

                final ArrayList<TargetListData> dataList = new ArrayList<TargetListData>();
                String[] temp = rcvMessage.split(",");
                for(int i = 1; i < temp.length; i += 11) {
                    TargetListData _data = new TargetListData();
                    _data.setId               ( temp[i] );
                    _data.setParent           ( temp[i + 1] );
                    if( i == 1 ) { currentParentId = temp[i + 1]; }
                    _data.setTargetName       ( temp[i + 2] );
                    _data.setPhotoBeforeAfter ( temp[i + 3] );
                    _data.setPhotoCheck       ( temp[i + 4] );
                    _data.setBfrPhotoShotCnt  ( temp[i + 5] );
                    _data.setBfrPhotoTotalCnt ( temp[i + 6] );
                    _data.setAftPhotoShotCnt  ( temp[i + 7] );
                    _data.setAftPhotoTotalCnt ( temp[i + 8] );
                    _data.setType             ( temp[i + 9] );
                    _data.setLock             ( temp[i + 10]);
                    dataList.add              ( _data );
                }
                if( parentArray.size() > 1 && ( parentArray.get( parentArray.size() - 2 ).equals( currentParentId ) ) ) {
                    parentArray.remove( parentArray.size() - 1 );
                } else {
                    parentArray.add( currentParentId );
                }
                TargetListAdapter targetListAdapter = new TargetListAdapter(con, 0, dataList);
                ListView listView = (ListView) _vg.findViewById(R.id.targetListView);
                listView.setAdapter( targetListAdapter );

            } else if(strList.get(0).equals( "disConnect" )) {
                Toast.makeText(con, "WebSocket接続切断...", Toast.LENGTH_LONG).show();
                Timer mTimer = null;
                mTimer = new Timer(true);
                mTimer.schedule( new TimerTask(){
                    @Override
                    public void run() {
                        Log.d(TAG, "WebSocket接続チェック...");
                        if( !client.isConnected() ) {
                            Log.d(TAG, "WebSocket未接続のため、再接続を試みる");
                            client.connect();
                        } else {
                            Toast.makeText(con, "WebSocket再接続完了", Toast.LENGTH_LONG).show();
                            this.cancel();
                        }

                    }
                }, 5000, 5000);
            } else if(strList.get(0).equals( "Error")) {
                Toast.makeText(con, "WebSocket接続エラー...", Toast.LENGTH_LONG).show();
            }
            //((MySample)context).textview01.setText("count: " + counter);
        }

    }

    public void setCurrentParentId(String str) {
        parentArray.add( str );
    }

    public String getCurrentParentId() {
        if( parentArray.size() > 1 ) {
            Log.d(TAG, "CurrentParentId -> " + parentArray.get( parentArray.size() - 2 ));
            return parentArray.get( parentArray.size() - 2 );
        } else {
            return parentArray.get( parentArray.size() - 1 );
        }
        //Log.d(TAG, "CurrentParentId -> null...");
        //return null;
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
