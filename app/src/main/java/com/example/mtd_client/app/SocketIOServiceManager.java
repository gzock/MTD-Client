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
import android.support.v7.internal.widget.ActivityChooserModel;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Gzock on 2014/06/01.
 */
public class SocketIOServiceManager{

    private ServiceConnection serviceConnection = null;
    private              ServiceReceiver   receiver          = null;
    private static final String            TAG               = "SocketIOServiceManager";
    private Context _con               = null;
    private SocketIOService socketio = null;
    private SocketIOClient client = null;
    private Intent intent            = null;
    private              String            selectedPj        = null;
    private ViewGroup _vg               = null;
    private              Boolean           serviceState      = false;
    private              String            currentParentId   = null;

    ArrayList<String> parentArray = new ArrayList<String>();
    ArrayList<String> pjNameList   = new ArrayList<String>();
    ArrayList<String> pjRootTargetList = new ArrayList<String>();
    private ArrayList<String> path = null;

    private IRcvTargetListListener mTargetListListener = null;
    private IRcvProjectListListener mProjectListListener = null;
    private IRcvPhotoConfirmListener mPhotoConfirmListener = null;
    private IRcvDisConnectedListener mDisConnectedListener = null;

    private ArrayList<TargetListData> mDataList = new ArrayList<TargetListData>();
    private String mEncPhotoData = null;
    private Activity mActivity = null;
    private IServiceConnectedCallbackListener mServiceConnectedCallback = null;


    public SocketIOServiceManager() {

    }

    public void init() {
        // ServiceConnectionの用意
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                //socketio = ((SocketIOService.SocketIOBinder)service).getService();
                setSocketio(((SocketIOService.SocketIOBinder)service).getService());
                //client = socketio.getSocketIOClinet();
                if(mServiceConnectedCallback != null) {
                    mServiceConnectedCallback.serviceConnectedCallback();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                //socketio = null;
            }
        };
        receiver = new ServiceReceiver();
    }

    public void setCallback(IServiceConnectedCallbackListener _mServiceConnectedCallback) {
        mServiceConnectedCallback = _mServiceConnectedCallback;
    }

    private void setSocketio( SocketIOService sio) {
        socketio = sio;
    }

    public SocketIOService getSocketio() {
        return socketio;
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
            Log.d(TAG, "ここまで");
            socketio.getSocketIOClinet().emit(str, jArray);
            //client.emit(str, jArray);
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

    public void stopSIOService() {
        _con.stopService(intent);
    }

    // Receiverクラス
    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {

            int eventSwitchNum = intent.getIntExtra("event", 0);
            JSONArray jArray = null;

            try {
                if( (SerializableJSONArray)intent.getSerializableExtra("json") != null ) {
                    SerializableJSONArray rcvMessage = (SerializableJSONArray)intent.getSerializableExtra("json");
                    jArray = rcvMessage.getJSONArray();
                    //Log.d(TAG, jArray.getJSONArray(0).getJSONObject(0).getString("username"))
                }

            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
            final ArrayList<String> strList = toArrayStr("aaa,bbb");

            switch ( eventSwitchNum ) {
                case SocketIOService.PLOJECT_LIST : {
                    Log.d(TAG, "*** Project List ***");
                    pjNameList   = new ArrayList<String>();
                    pjRootTargetList = new ArrayList<String>();

                    try {
                        // JSONArrayはforeach使えない
                        for (int i=0; i <= jArray.length(); i++) {
                            pjNameList.add( jArray.getJSONArray(0).getJSONObject(i).getString("projectName") );
                            pjRootTargetList.add( jArray.getJSONArray(0).getJSONObject(i).getString("root_target") );
                        }

                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }

                    if(mProjectListListener != null) {
                        mProjectListListener.rcvProjectList();
                    }
                    break;
                }
                case SocketIOService.UPDATE_TARGET_LIST : {
                    Log.d(TAG, "*** Update Target List ***");

                    try {
                        // TODO: 暫定対応  previousParentとcurrentParentを使って、階層移動を実現しよう
                        if( jArray.get(0) instanceof JSONObject && jArray.getJSONObject(0).getString("currentParent") != JSONObject.NULL ) {
                            Log.d(TAG, "暫定対応: currentParentみっけた");
                            path = new ArrayList<String>();
                            path.add( jArray.getJSONObject(0).getString("previousParent") );

                        } else {
                            // JSONArrayはforeach使えない
                            for (int i = 0; i < jArray.getJSONArray(0).length(); i++) {
                                TargetListData _data = new TargetListData();
                                _data.setId(jArray.getJSONArray(0).getJSONObject(i).getString("_id"));
                                _data.setParent(jArray.getJSONArray(0).getJSONObject(i).getString("parent"));
                                if (i == 0) {
                                    currentParentId = jArray.getJSONArray(0).getJSONObject(i).getString("parent");
                                }
                                _data.setTargetName(jArray.getJSONArray(0).getJSONObject(i).getString("target_name"));
                                _data.setPhotoBeforeAfter(jArray.getJSONArray(0).getJSONObject(i).getString("photo_before_after"));
                                _data.setPhotoCheck(jArray.getJSONArray(0).getJSONObject(i).getString("photo_check"));
                                _data.setBfrPhotoShotCnt(jArray.getJSONArray(0).getJSONObject(i).getString("bfr_photo_shot_cnt"));
                                _data.setBfrPhotoTotalCnt(jArray.getJSONArray(0).getJSONObject(i).getString("bfr_photo_total_cnt"));
                                _data.setAftPhotoShotCnt(jArray.getJSONArray(0).getJSONObject(i).getString("aft_photo_shot_cnt"));
                                _data.setAftPhotoTotalCnt(jArray.getJSONArray(0).getJSONObject(i).getString("aft_photo_total_cnt"));
                                _data.setType(jArray.getJSONArray(0).getJSONObject(i).getString("type"));
                                _data.setLock(jArray.getJSONArray(0).getJSONObject(i).getString("lock"));
                                mDataList.add(_data);
                            }
                            path = new ArrayList<String>( Arrays.asList(jArray.getJSONArray(0).getJSONObject(0).getString("path").split("#")) );
                        }
                        if(mTargetListListener != null) {
                            mTargetListListener.rcvTargetList();
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }


                    break;
                }
                case SocketIOService.PHOTO_CONFIRM : {
                    try {
                        mEncPhotoData = jArray.getJSONObject(0).getString("encPhotoData");
                        if(mPhotoConfirmListener != null) {
                            mPhotoConfirmListener.rcvPhotoConfirm();
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                    break;
                }
                case SocketIOService.DISCONNECTED :{
                    Log.d(TAG, "Socket.IO DisConnected...");
                    if(mDisConnectedListener != null) {
                        mDisConnectedListener.rcvDisConnected();
                    }
                    //Toast.makeText(MainActivity.this, "Socket.IO DisConnected", Toast.LENGTH_LONG).show();
                    break;
                }
            }
            /*
            //((MySample)context).textview01.setText("count: " + counter);
            */
        }

    }

    public ArrayList<String> getPjNameList() {
        return pjNameList;
    }

    public ArrayList<String> getPjRootTargetList() {
        return pjRootTargetList;
    }

    public void setRootTargetId( String str ) { socketio.setRootTargetId( str ); }
    public String getRootTargetId() { return socketio.getRootTargetId(); }

    public void setProjectName( String str ) { socketio.setProjectName( str ); }
    public String getProjectName() { return socketio.getProjectName(); }

    public ArrayList<TargetListData> getTargetListData() {
        return mDataList;
    }

    public void clearTargetListData() {
        mDataList.clear();
    }

    public ArrayList<String> getTargetPath() {
        return path;
    }

    public String getEncPhotoData() {
        return mEncPhotoData;
    }

    public ArrayList<SendJobData> getSendJobs() { return socketio.getSendJobs(); }

    /**
     * リスナーを追加する
     * @param listener
     */
    public void setRcvTargetListListener(IRcvTargetListListener listener){
        this.mTargetListListener = listener;
    }

    public void setRcvProjectListListener(IRcvProjectListListener listener){
        this.mProjectListListener = listener;
    }

    public void setRcvPhotoConfirmListener(IRcvPhotoConfirmListener listener){
        this.mPhotoConfirmListener = listener;
    }

    public void setRcvDisConnectedListener(IRcvDisConnectedListener listener) {
        this.mDisConnectedListener = listener;
    }

    /**
     * リスナーを削除する
     */
    public void removeRcvTargetListListener() {
        this.mTargetListListener = null;
    }
    public void removeRcvProjectListListener() {
        this.mProjectListListener = null;
    }
    public void removeRcvPhotoConfirmListener() {
        this.mPhotoConfirmListener = null;
    }
    public void removeRcvDisConnectedListener() { this.mDisConnectedListener = null; }

    public void setCurrentParentId(String str) {
        parentArray.add( str );
    }

    public void disConnected() {
        socketio.disConnected();
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
