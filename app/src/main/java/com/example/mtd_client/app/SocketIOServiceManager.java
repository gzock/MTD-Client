package com.example.mtd_client.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mtd_client.app.R;
import com.example.mtd_client.app.SocketIOService;
import com.example.mtd_client.app.TargetListAdapter;
import com.example.mtd_client.app.TargetListData;
import com.example.mtd_client.app.TargetListView;
import com.koushikdutta.async.http.socketio.SocketIOClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Gzock on 2014/06/01.
 */
public class SocketIOServiceManager {

    private ServiceConnection serviceConnection = null;
    private              ServiceReceiver   receiver          = null;
    private static final String            TAG               = "SocketIOServiceManager";
    private Context _con               = null;
    private SocketIOService socketio            = null;
    private SocketIOClient client = null;
    private Intent intent            = null;
    private              String            selectedPj        = null;
    private ViewGroup _vg               = null;
    private              Boolean           serviceState      = false;
    private              String            currentParentId   = null;

    ArrayList<String> parentArray = new ArrayList<String>();

    public SocketIOServiceManager() {
        // ServiceConnectionの用意
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                socketio = ((SocketIOService.SocketIOBinder)service).getService();
                client = socketio.getSocketIOClinet();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                socketio = null;
            }
        };
        receiver = new ServiceReceiver();

    }

    public void startSIOService(Context context) {
        _con = context;
        intent = new Intent(_con, SocketIOService.class);
        ComponentName component = _con.startService(intent);

    }

    public void bindSIOService(Context context) {

        _con = context;
        intent = new Intent(_con, SocketIOService.class);

        IntentFilter filter = new IntentFilter(SocketIOService.ACTION);
        _con.registerReceiver(receiver, filter);
        Boolean bool = _con.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService Result ->" + bool.toString());
        serviceState = true;
    }
    public void unBindSIOService() {
        _con.unbindService(this.getServiceConnection()); // バインド解除
        _con.unregisterReceiver(this.getReceiver()); // レシーバー解除
        serviceState = false;
    }
    public Boolean isSIOServiceState() {
        return serviceState;
    }

    public ServiceReceiver getReceiver() { return receiver; }
    public ServiceConnection getServiceConnection() { return serviceConnection; }


    public boolean isState() {
        if (socketio != null) {
            return true;
        } else {
            return false;
        }
    }

    //TODO ここは考えなおそう。contextからviewを取得する方法はない？
    public void setView (ViewGroup vg) {
        _vg = vg;
    }

    public void emit(String str) {
        try {
            client.emit(str);
            Log.d(TAG, "emit -> " + str);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void emit(String str, JSONArray jArray) {
        try {
            client.emit(str, jArray);
            Log.d(TAG, "emit name -> " + str + "JSONArray -> " + jArray);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public Boolean isSIOConnected() {
        try {
            Log.d(TAG, "Connect State -> " + client.isConnected());
            return client.isConnected();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return false;
        }
    }

    public void stopWsService() {
        _con.stopService(intent);
    }

    // Receiverクラス
    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {

            int eventSwitchNum = intent.getIntExtra("event", 0);
            JSONArray jArray = null;

            try {
                SerializableJSONArray rcvMessage = (SerializableJSONArray)intent.getSerializableExtra("json");
                jArray = rcvMessage.getJSONArray();
                //Log.d(TAG, jArray.getJSONArray(0).getJSONObject(0).getString("username"));

            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
            final ArrayList<String> strList = toArrayStr("aaa,bbb");

            switch ( eventSwitchNum ) {
                case SocketIOService.PLOJECT_LIST : {
                    Log.d(TAG, "*** Project List ***");
                    final ArrayList<String> pjNameList   = new ArrayList<String>();
                    final ArrayList<String> pjRootTargetList = new ArrayList<String>();

                    try {
                        // JSONArrayはforeach使えない
                        for (int i=0; i <= jArray.length(); i++) {
                            pjNameList.add( jArray.getJSONArray(0).getJSONObject(i).getString("projectName") );
                            pjRootTargetList.add( jArray.getJSONArray(0).getJSONObject(i).getString("root_target") );
                        }

                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
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
                                        Log.d(TAG, pjNameList.get(which) + " Selected");
                                        JSONArray jArray = new JSONArray();
                                        JSONObject jObj = new JSONObject();
                                        try {
                                            jObj.put("parent", pjRootTargetList.get(which));
                                            jArray.put(jObj);
                                            client.emit("getTargetList", jArray);

                                        } catch (Exception e) {
                                            Log.d(TAG, e.toString());
                                        }
                                        selectedPj = strList.get(which);
                                       ((Activity)_con).setContentView(R.layout.activity_target_list_view);

                                    }
                                }
                        )
                        .setPositiveButton("OK", null)
                        .show();
                    break;
                }
                case SocketIOService.UPDATE_TARGET_LIST : {
                    Log.d(TAG, "*** Update Target List ***");

                    final ArrayList<TargetListData> dataList = new ArrayList<TargetListData>();

                    try {
                        // JSONArrayはforeach使えない
                        for (int i = 0; i <= jArray.length(); i++) {
                            TargetListData _data = new TargetListData();
                            _data.setId               ( jArray.getJSONArray(0).getJSONObject(i).getString("_id") );
                            _data.setParent           ( jArray.getJSONArray(0).getJSONObject(i).getString("parent") );
                            //if( i == 1 ) { currentParentId = temp[i + 1]; }
                            _data.setTargetName       ( jArray.getJSONArray(0).getJSONObject(i).getString("target_name") );
                            _data.setPhotoBeforeAfter ( jArray.getJSONArray(0).getJSONObject(i).getString("photo_before_after") );
                            _data.setPhotoCheck       ( jArray.getJSONArray(0).getJSONObject(i).getString("photo_check") );
                            _data.setBfrPhotoShotCnt  ( jArray.getJSONArray(0).getJSONObject(i).getString("bfr_photo_shot_cnt") );
                            _data.setBfrPhotoTotalCnt ( jArray.getJSONArray(0).getJSONObject(i).getString("bfr_photo_total_cnt") );
                            _data.setAftPhotoShotCnt  ( jArray.getJSONArray(0).getJSONObject(i).getString("aft_photo_shot_cnt") );
                            _data.setAftPhotoTotalCnt ( jArray.getJSONArray(0).getJSONObject(i).getString("aft_photo_total_cnt") );
                            _data.setType             ( jArray.getJSONArray(0).getJSONObject(i).getString("type") );
                            _data.setLock             ( jArray.getJSONArray(0).getJSONObject(i).getString("lock") );
                            dataList.add              ( _data );
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                    /*
                    if( parentArray.size() > 1 && ( parentArray.get( parentArray.size() - 2 ).equals( currentParentId ) ) ) {
                        parentArray.remove( parentArray.size() - 1 );
                    } else {
                        parentArray.add( currentParentId );
                    }
                    */
                    TargetListAdapter targetListAdapter = new TargetListAdapter(_con, 0, dataList);
                    ListView listView = (ListView) ((Activity)_con).findViewById(R.id.targetListView);
                    listView.setAdapter( targetListAdapter );



                    break;
                }
            }
            /*
            if( strList.get(0).equals("pjList")) {

            } else if (strList.get(0).equals("tgtList")) {

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
                TargetListAdapter targetListAdapter = new TargetListAdapter(_con, 0, dataList);
                ListView listView = (ListView) _vg.findViewById(R.id.targetListView);
                listView.setAdapter( targetListAdapter );

            } else if(strList.get(0).equals( "disConnect" )) {
                Toast.makeText(_con, "WebSocket接続切断...", Toast.LENGTH_LONG).show();
                Timer mTimer = null;
                mTimer = new Timer(true);
                mTimer.schedule( new TimerTask(){
                    @Override
                    public void run() {
                        Log.d(TAG, "WebSocket接続チェック...");
                        if( !client.isConnected() ) {
                            Log.d(TAG, "WebSocket未接続のため、再接続を試みる");
                            //client.connect();
                        } else {
                            Toast.makeText(_con, "WebSocket再接続完了", Toast.LENGTH_LONG).show();
                            this.cancel();
                        }

                    }
                }, 5000, 5000);
            } else if(strList.get(0).equals( "Error")) {
                Toast.makeText(_con, "WebSocket接続エラー...", Toast.LENGTH_LONG).show();
            }
            //((MySample)context).textview01.setText("count: " + counter);
            */
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
