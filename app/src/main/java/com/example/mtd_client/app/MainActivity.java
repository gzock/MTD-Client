package com.example.mtd_client.app;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.http.socketio.SocketIOClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private static final String TAG = "MainActivity";
    private static final int CAMERA_SHOT_ACTIVITY  = 0;

    private ServiceManager sm = new ServiceManager();
    //private SocketIOServiceManager sIOsm = new SocketIOServiceManager();

    private Dialog dialog = null;

    private TargetListAdapter targetListAdapter = null;
    private ListView listView = null;
    TargetListData item = null;

    private SocketIOService socketio            = null;
    private SocketIOClient client = null;

    private              ServiceReceiver   receiver          =  new ServiceReceiver();
    private              String            currentParentId   = null;


    ArrayList<String> parentArray = new ArrayList<String>();
    //SocketIO socket = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //sm.startWsService(MainActivity.this);
        //sm.bindWsService(MainActivity.this);
        //sIOsm.startSIOService( MainActivity.this );
        //sIOsm.bindSIOService( MainActivity.this );

        /*
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        switch(config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                Intent intent = new Intent(MainActivity.this, SocketIOService.class);
                ComponentName component = MainActivity.this.startService(intent);

                IntentFilter filter = new IntentFilter(SocketIOService.ACTION);
                MainActivity.this.registerReceiver(receiver, filter);
                Boolean bool = MainActivity.this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

                break;

            case Configuration.ORIENTATION_LANDSCAPE:
                // 縦向きに固定
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
        */
        Intent intent = new Intent(MainActivity.this, SocketIOService.class);
        ComponentName component = MainActivity.this.startService(intent);

        IntentFilter filter = new IntentFilter(SocketIOService.ACTION);
        MainActivity.this.registerReceiver(receiver, filter);
        Boolean bool = MainActivity.this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);



        // 送信ボタン
        Button testBtn = (Button) findViewById(R.id.sendBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("message", "hogehoge");
                    //socket.emit("message:send", json);
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
                if( client.isConnected() ) {
                    try {
                        // 送信
                        Log.d(TAG, "Send...");
                        byte[] buf = {0x40, 0x41, 0x42, 0x43, 0x44, 0x45};
                        //sm.send("hello...?");

                        EditText userId = (EditText) findViewById(R.id.userIdTextBox);
                        EditText password = (EditText) findViewById(R.id.passTextBox);
                        Log.d(TAG, "userId -> " + userId.getText());
                        Log.d(TAG, "password -> " + password.getText());

                        String[] strs = {userId.getText().toString(), password.getText().toString()};
                        //sm.send("login," + userId.getText().toString() + "," + password.getText().toString());
                        JSONArray jArray = new JSONArray();
                        JSONObject jObj = new JSONObject();
                        try {
                            jObj.put("username", userId.getText().toString());
                            jObj.put("password", password.getText().toString());
                            jArray.put(jObj);
                            client.emit("login", jArray);
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }
                        //client.send(buf);

                    } catch (NotYetConnectedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "WebSocketサービスが非接続状態です", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
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
                    new AlertDialog.Builder( MainActivity.this )
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
                                            //selectedPj = strList.get(which);
                                            MainActivity.this.setContentView(R.layout.activity_target_list_view);

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
                        if(jArray.getJSONArray(0).length() == 0) {
                            Toast.makeText(MainActivity.this, "何も登録されていません", Toast.LENGTH_LONG).show();
                            currentParentId = item.getId();
                        } else {
                            // JSONArrayはforeach使えない
                            for (int i = 0; i <= jArray.length(); i++) {
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
                                dataList.add(_data);
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }

                    if( parentArray.size() > 1 && ( parentArray.get( parentArray.size() - 2 ).equals( currentParentId ) ) ) {
                        parentArray.remove( parentArray.size() - 1 );
                    } else {
                        parentArray.add( currentParentId );
                    }

                    TargetListAdapter targetListAdapter = new TargetListAdapter(MainActivity.this, 0, dataList);
                    ListView listView = (ListView) MainActivity.this.findViewById(R.id.targetListView);
                    listView.setAdapter( targetListAdapter );
                    targetListAdapter.notifyDataSetChanged();

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView parent, View view, int position, long id) {
                            ListView listView = (ListView) parent;
                            item = (TargetListData)listView.getItemAtPosition(position);

                            if( item.getType().equals( 0 ) ) {
                                JSONArray jArray = new JSONArray();
                                JSONObject jObj = new JSONObject();
                                try {
                                    jObj.put("parent", item.getId());
                                    jArray.put(jObj);
                                    client.emit("getTargetList", jArray);

                                } catch (Exception e) {
                                    Log.d(TAG, e.toString());
                                }
                                //sm.send( "getTargetListUpdate," + item.getId());
                                Log.d(TAG, "selected -> " + item.getTargetName());
                                //previousTarget = item.getId();
                                //previousTargetParentId = item.getParent();

                            } else {
                                showDialog(0);
                            }
                        }
                    });
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                            ListView listView = (ListView) parent;
                            item = (TargetListData) listView.getItemAtPosition(position);
                            if ( item.getType() == 0 ) {
                                showDialog(1);
                            }
                            // trueを返さないと、LongClickのあとに普通のclickが動いてしまう
                            return true;
                        }
                    });



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

    private String getCurrentParentId() {
        if( parentArray.size() > 1 ) {
            Log.d(TAG, "CurrentParentId -> " + parentArray.get( parentArray.size() - 2 ));
            return parentArray.get( parentArray.size() - 2 );
        } else {
            return parentArray.get( parentArray.size() - 1 );
        }
        //Log.d(TAG, "CurrentParentId -> null...");
        //return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            if( !getCurrentParentId().equals(null) ) {
                JSONArray jArray = new JSONArray();
                JSONObject jObj = new JSONObject();
                try {
                    jObj.put("parent", getCurrentParentId());
                    jArray.put(jObj);
                    client.emit("getTargetList", jArray);
                    return true;
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                    finish();
                }

            } else {
                return false;
            }
        }
        return false;
    }





    @Override
    protected void onDestroy() {
        //MainActivity.this.unregisterReceiver( receiver );
        //MainActivity.this.unbindService( serviceConnection );
        super.onDestroy();
    }


    public ArrayList<String> toArrayStr(String message) {
        String[] strs = message.split(",");
        ArrayList<String> strList = new ArrayList<String>();

        for(int i = 0; i < strs.length; i++) {
            strList.add(strs[i]);
        }
        return strList;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_target_list);
                break;
            case 2:
                mTitle = getString(R.string.title_const_id_manage);
                break;
            case 3:
                mTitle = getString(R.string.title_ky);
                break;
            case 4:
                mTitle = getString(R.string.title_task);
                break;
            case 5:
                mTitle = getString(R.string.title_chat);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "OnActivityResult");

        switch( requestCode ) {
            case CAMERA_SHOT_ACTIVITY :
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                Intent intent = new Intent(MainActivity.this, SocketIOService.class);
                IntentFilter filter = new IntentFilter(SocketIOService.ACTION);

                MainActivity.this.registerReceiver(receiver, filter);
                MainActivity.this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

                switch ( resultCode ) {
                    case CameraShot.CAMERA_SHOT :
                        Log.d(TAG, "CameraShot Result");
                        JSONArray jArray = new JSONArray();
                        JSONObject jObj = new JSONObject();
                        try {
                            jObj.put("parent", currentParentId);
                            jArray.put(jObj);
                            client.emit("getTargetList", jArray);
                            parentArray.remove( parentArray.size() - 1 );
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                            finish();
                        }
                        break;
                    case CameraShot.NON_CAMERA_SHOT :
                        break;
                }
                break;
        }
        if(requestCode == CAMERA_SHOT_ACTIVITY) {
            if(resultCode == 1 ){

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();

            // メニューの要素を追加
            menu.add("Normal item");

            // メニューの要素を追加して取得
            MenuItem addTargetItem = menu.add("TargetAdd");
            MenuItem refleshTargetItem = menu.add("TargetReflesh");

            // SHOW_AS_ACTION_IF_ROOM:余裕があれば表示
            addTargetItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            refleshTargetItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            // アイコンを設定
            addTargetItem.setIcon(android.R.drawable.ic_menu_add);
            refleshTargetItem.setIcon(R.drawable.ic_menu_refresh);
            return true;
        }


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if(item.getTitle().equals( "TargetAdd" )) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.add_target_dialog, null);
            final EditText addTargetName = (EditText)view.findViewById(R.id.add_target_name);
            final Spinner addTargetGenre = (Spinner)view.findViewById(R.id.add_target_genre_spinner);
            final Spinner addTargetBeforeAfter = (Spinner)view.findViewById(R.id.add_target_before_after_spinner);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("項目の追加")
                    .setView(view)
                    .setPositiveButton(
                            "決定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Log.d(TAG, "Add Target Name -> "       + addTargetName.getText().toString());
                                    Log.d(TAG, "Add Target Genre -> "      + addTargetGenre.getSelectedItem().toString());
                                    Log.d(TAG, "Add Target BeforAfter -> " + addTargetBeforeAfter.getSelectedItem().toString());

                                    JSONArray jArray = new JSONArray();
                                    JSONObject jObj = new JSONObject();
                                    try {
                                        jObj.put("parent", currentParentId);
                                        jObj.put("addTargetName", addTargetName.getText().toString());
                                        jObj.put("addTargetType", addTargetGenre.getSelectedItem().toString());
                                        jObj.put("addTargetBeforeAfter", addTargetBeforeAfter.getSelectedItem().toString());
                                        jArray.put(jObj);
                                        client.emit("addTarget", jArray);
                                        parentArray.remove( parentArray.size() - 1 );
                                    } catch (Exception e) {
                                        Log.d(TAG, e.toString());
                                    }
                                    //sm.send("addTarget," + pjName + "," + previousTargetParentId + "," + addTargetName.getText().toString() + "," +
                                    //        addTargetGenre.getSelectedItem().toString() + "," + addTargetBeforeAfter.getSelectedItem().toString());
                                }
                            }
                    )
                    .setNegativeButton("キャンセル", null)
                    .show();
        } else if(item.getTitle().equals( "TargetReflesh" )) {
            JSONArray jArray = new JSONArray();
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("parent", currentParentId);
                jArray.put(jObj);
                client.emit("getTargetList", jArray);
                return true;
            } catch (Exception e) {
                Log.d(TAG, e.toString());
                finish();
            }
        }
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    @Override
    public void onUserLeaveHint(){
        //ホームボタンが押された時や、他のアプリが起動した時に呼ばれる
        //戻るボタンが押された場合には呼ばれない
        //Toast.makeText(getApplicationContext(), TAG + " Good bye!" , Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        dialog = super.onCreateDialog(id);

        //idは何個かダイアログがある場合に使う
        if ( id == 0 )
        {
            final CharSequence[] chars = {"撮影", "確認", "編集", "削除"};

            //showDialogを呼ぶときに１度だけ呼ばれる
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( MainActivity.this );
            dialogBuilder.setTitle("操作を選択して下さい");
            dialogBuilder.setSingleChoiceItems(
                    chars,
                    0, // Initial
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String switchStr = chars[which].toString();

                            if (switchStr.equals("撮影")) {
                                Toast.makeText( MainActivity.this, switchStr, Toast.LENGTH_LONG).show();

                                Intent i = new Intent( MainActivity.this, CameraShot.class);
                                i.putExtra("TargetID", item.getId());
                                i.putExtra("TargetName", item.getTargetName());
                                dialog.dismiss();

                                MainActivity.this.unregisterReceiver( receiver );
                                MainActivity.this.unbindService(serviceConnection);

                                startActivityForResult(i, CAMERA_SHOT_ACTIVITY);

                            } else if (switchStr.equals("確認")) {
                                Toast.makeText( MainActivity.this, switchStr, Toast.LENGTH_LONG).show();
                                //TODO 画像確認

                            } else if (switchStr.equals("編集")) {
                                Toast.makeText( MainActivity.this, switchStr, Toast.LENGTH_LONG).show();
                                showDialog(2);

                            } else if (switchStr.equals("削除")) {
                                Toast.makeText( MainActivity.this, switchStr, Toast.LENGTH_LONG).show();
                                showDialog(3);
                            }
                        }
                    }
            );
            dialogBuilder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dialogBuilder.create();

        } else if( id == 1 ) {
            final CharSequence[] chars = {"編集", "削除"};

            //showDialogを呼ぶときに１度だけ呼ばれる
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( MainActivity.this );
            dialogBuilder.setTitle("操作を選択して下さい");
            dialogBuilder.setSingleChoiceItems(
                    chars,
                    0, // Initial
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String switchStr = chars[which].toString();

                            if (switchStr.equals("編集")) {
                                Toast.makeText( MainActivity.this, switchStr, Toast.LENGTH_LONG).show();
                                showDialog(2);

                            } else if (switchStr.equals("削除")) {
                                Toast.makeText( MainActivity.this, switchStr, Toast.LENGTH_LONG).show();
                                showDialog(3);
                            }
                        }
                    }
            );
            dialogBuilder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dialogBuilder.create();

        } else if( id == 2 ) {
            //showDialogを呼ぶときに１度だけ呼ばれる
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( MainActivity.this );

            LayoutInflater inflater = LayoutInflater.from( MainActivity.this );
            View view = inflater.inflate(R.layout.edit_target_dialog, null);
            final EditText editTargetName = (EditText) view.findViewById(R.id.edit_target_name);
            editTargetName.setText(item.getTargetName());

            dialogBuilder.setTitle("項目の編集");
            dialogBuilder.setView(view);
            dialogBuilder.setPositiveButton(
                    "決定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.d(TAG, "ProjectName -> " + pjName);
                            Log.d(TAG, "Edit Target ID -> " + item.getId());
                            Log.d(TAG, "Edit Target Name -> " + editTargetName.getText().toString());
                            Log.d(TAG, "Edit TargetParen ID -> " + item.getParent());

                            JSONArray jArray = new JSONArray();
                            JSONObject jObj = new JSONObject();
                            try {
                                jObj.put("id", item.getId());
                                jObj.put("target_name", editTargetName.getText().toString());
                                jObj.put("parent", item.getParent());
                                jArray.put(jObj);
                                client.emit("editTarget", jArray);
                                parentArray.remove( parentArray.size() - 1 );
                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                            }
                            dialog.dismiss();
                        }
                    }
            );
            dialogBuilder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dialogBuilder.create();

        } else if( id == 3 ) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( MainActivity.this );
            dialogBuilder.setTitle("項目の削除");
            dialogBuilder.setMessage(item.getTargetName() + "を削除しますか? \n対象配下に機器や建物が存在する場合は、それらも削除されてしまいますので、ご注意下さい");
            dialogBuilder.setPositiveButton(
                    "決定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Delete Target ID -> " + item.getId());
                            Log.d(TAG, "Delete TargetParent -> " + item.getParent());
                            JSONArray jArray = new JSONArray();
                            JSONObject jObj = new JSONObject();
                            try {
                                jObj.put("id", item.getId());
                                jObj.put("parent", item.getParent());
                                jArray.put(jObj);
                                client.emit("deleteTarget", jArray);
                                parentArray.remove( parentArray.size() - 1 );
                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                            }
                            dialog.dismiss();
                        }
                    }
            );
            dialogBuilder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dialogBuilder.create();
        }

        return dialog;
    }


}
