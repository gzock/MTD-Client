package com.example.mtd_client.app;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentBreadCrumbs;
import android.support.v4.app.FragmentTransaction;
//import android.support.v4.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
//import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
//import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.http.socketio.SocketIOClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Inflater;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
            IRcvDisConnectedListener,
            IProjectSelectedCallbackListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private static final String TAG = "MainActivity";
    private static final int CAMERA_SHOT_ACTIVITY   = 0;
    private static final int PHOTO_CONFIRM_ACTIVITY = 1;
    private static final int SETTINGS_ACTIVITY      = 10;
    private static final int PROJECT_LIST_DIALOG = 0;
    private static final int TARGET_DIALOG       = 1;
    private static final int TARGET_DIALOG_2     = 2;
    private static final int TARGET_EDIT_DIALOG  = 3;
    private static final int TARGET_DELTE_DIALOG = 4;

    private SocketIOServiceManager sIoSm = new SocketIOServiceManager();

    private Dialog dialog = null;

    Boolean notSleepEnable = false;

    private String projectName = null;
    private String projectId = null;
    private boolean logined = false;

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

        Fragment loginFragment = new LoginFragment.PlaceholderFragment();
        FragmentTransaction ft =  getSupportFragmentManager().beginTransaction();
        // Layout位置先の指定
        ft.replace(R.id.container, loginFragment);
        // Fragmentの変化時のアニメーションを指定
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //ft.addToBackStack(null);
        ft.commit();


        sIoSm.init();
        sIoSm.startSIOService( MainActivity.this );
        sIoSm.bindSIOService( MainActivity.this );

        sIoSm.setRcvDisConnectedListener( this );

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        notSleepEnable = preferences.getBoolean("not_sleep_enable", false);

        if( notSleepEnable ) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

/*
        // 送信ボタン
        Button testBtn = (Button) findViewById(R.id.sendBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("message", "hogehoge");
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }

                if(sIoSm.isSIOServiceState()) {
                    try {
                        // 送信

                        EditText userId = (EditText) findViewById(R.id.userIdTextBox);
                        EditText password = (EditText) findViewById(R.id.passTextBox);
                        Log.d(TAG, "userId -> " + userId.getText());
                        Log.d(TAG, "password -> " + password.getText());

                        JSONArray jArray = new JSONArray();
                        JSONObject jObj = new JSONObject();
                        try {
                            jObj.put("username", userId.getText().toString());
                            jObj.put("password", password.getText().toString());
                            jArray.put(jObj);
                            sIoSm.emit("login", jArray);
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }

                    } catch (NotYetConnectedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "WebSocketサービスが非接続状態です", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */

    }

    @Override
    public void projectSelectedCallback() {
        // フラグメントのインスタンスを生成する。
        Fragment newFragment = new TargetListFragment.PlaceholderFragment();

        // ActivityにFragmentを登録する。
        FragmentTransaction ft =  getSupportFragmentManager().beginTransaction();
        // Layout位置先の指定
        ft.replace(R.id.container, newFragment);
        // Fragmentの変化時のアニメーションを指定
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //ft.addToBackStack(null);
        ft.commit();

        logined = true;
    }


    @Override
    public void rcvDisConnected() {
        Log.d(TAG, "Socket.IO DisConnected...");
        Toast.makeText(MainActivity.this, "Socket.IO DisConnected", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onStop() {
        super.onStop();
        sIoSm.disConnected();
    }
    @Override
    protected void onDestroy() {

        if(sIoSm.isSIOServiceState()) {
            sIoSm.unBindSIOService();
            sIoSm.stopSIOService();
        }

        if( notSleepEnable ) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        super.onDestroy();
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();

        int switchNum = position + 1;
        Log.d(TAG, Integer.toString(switchNum));
        if(switchNum > 0 && switchNum < 7) {
            Fragment newFragment = null;
            switch (switchNum) {
                case 1:
                    newFragment = new TargetListFragment.PlaceholderFragment();
                    break;
                case 6:
                    newFragment = new SendJobDataResultFragment.PlaceholderFragment();
                    break;
            }
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            // Layout位置先の指定
            ft.replace(R.id.container, newFragment);
            // Fragmentの変化時のアニメーションを指定
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(null);
            ft.commit();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();

            // メニューの要素を追加
            //menu.add("Normal item");

            // メニューの要素を追加して取得
            MenuItem addTargetItem = menu.add("TargetAdd");
            MenuItem refleshTargetItem = menu.add("TargetReflesh");
            MenuItem closeApp = menu.add("CloseApp");


            // ハニカム未満は振り分けないと落ちる
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                // SHOW_AS_ACTION_IF_ROOM:余裕があれば表示
                addTargetItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                refleshTargetItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                // アイコンを設定
                addTargetItem.setIcon(android.R.drawable.ic_menu_add);
                addTargetItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                refleshTargetItem.setIcon(R.drawable.ic_menu_refresh);
                refleshTargetItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                closeApp.setIcon(R.drawable.power);
                closeApp.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
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
            Intent i = new Intent( MainActivity.this, SettingsActivity.class);
            startActivityForResult(i, SETTINGS_ACTIVITY);
            return true;
        }

        if(logined && item.getTitle().equals( "TargetAdd" )) {
            //client = socketio.getSocketIOClinet();
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.add_target_dialog, null);
            final EditText addTargetName = (EditText)view.findViewById(R.id.add_target_name);
            final Spinner addTargetGenre = (Spinner)view.findViewById(R.id.add_target_genre_spinner);
            final Spinner addTargetBeforeAfter = (Spinner)view.findViewById(R.id.add_target_before_after_spinner);

            new AlertDialog.Builder(this)
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

                                    Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
                                    CustomBreadCrumbList _bcl = (CustomBreadCrumbList)f.getView().findViewById(R.id.Select_BreadCrumbList);

                                    JSONArray jArray = new JSONArray();
                                    JSONObject jObj = new JSONObject();
                                    try {
                                        String currentParentId = _bcl.getTargetIdList().get(_bcl.size() - 1);
                                        jObj.put("parent", currentParentId);
                                        jObj.put("addTargetName", addTargetName.getText().toString());
                                        jObj.put("addTargetType", addTargetGenre.getSelectedItem().toString());
                                        jObj.put("addTargetBeforeAfter", addTargetBeforeAfter.getSelectedItem().toString());
                                        jArray.put(jObj);
                                        sIoSm.emit("addTarget", jArray);
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
        } else if( logined && item.getTitle().equals( "TargetReflesh" )) {
            JSONArray jArray = new JSONArray();
            JSONObject jObj = new JSONObject();
            try {
                //TODO: 更新処理、修正必須
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
                CustomBreadCrumbList _bcl = (CustomBreadCrumbList)f.getView().findViewById(R.id.Select_BreadCrumbList);
                String currentParentId = _bcl.getTargetIdList().get(_bcl.size() - 1);
                jObj.put("parent", currentParentId);
                jArray.put(jObj);
                sIoSm.emit("getTargetList", jArray);
                return true;
            } catch (Exception e) {
                Log.d(TAG, e.toString());
                finish();
            }
        } else if(item.getTitle().equals( "CloseApp" )) {
            finish();
            //moveTaskToBack(true);

        } else if(!logined) {
            Toast.makeText(MainActivity.this, "この操作はログイン後にしか行えません", Toast.LENGTH_SHORT).show();
        }

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

            // 元々、ナビゲーションバーから選んだアイテムの数字を表示させる処理が入ってた
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
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
//        client = socketio.getSocketIOClinet();


        //idは何個かダイアログがある場合に使う
        // ここはswitch使わないほうがいい
        if( id == PROJECT_LIST_DIALOG) {
            final ArrayList<String> pjNameList = sIoSm.getPjNameList();
            final ArrayList<String> pjRootTargetList = sIoSm.getPjRootTargetList();
            //pjNameList = sIoSm.getPjNameList();

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( MainActivity.this );
            dialogBuilder.setTitle("案件名を選択して下さい");

            final CharSequence[] chars = pjNameList.toArray(new CharSequence[pjNameList.size()]);

            dialogBuilder.setSingleChoiceItems(
                chars,
                0, // Initial
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sIoSm.unBindSIOService();
                        Log.d(TAG, pjNameList.get(which) + " Selected");
                        projectName = pjNameList.get(which);
                        projectId = pjRootTargetList.get(which);

                        Intent intent = new Intent(MainActivity.this, TargetListView.class);
                        intent.putExtra("rootTargetId", projectId);
                        intent.putExtra("rootTargetName", projectName);
                        startActivity(intent);

                    }
                }
            );
            dialogBuilder.setPositiveButton("OK", null);
            dialog = dialogBuilder.create();

        }

        return dialog;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            ArrayList<String> path = sIoSm.getTargetPath();
            try {
                JSONArray jArray = new JSONArray();
                JSONObject jObj = new JSONObject();
                int size = path.size();
                switch ( size ) {
                    case 1:
                        Toast.makeText(MainActivity.this, "階層トップです",Toast.LENGTH_SHORT).show();
                        if(sIoSm != null && sIoSm.isSIOServiceState()) {
                            sIoSm.unBindSIOService();
                            sIoSm.stopSIOService();
                            finish();
                        }
                        //TODO: 終了処理書く
                        //return false
                    case 2:
                        jObj.put("parent", path.get(0));
                        break;
                    default :
                        jObj.put("parent", path.get( path.size() - 3 ));
                        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
                        CustomBreadCrumbList _bcl = (CustomBreadCrumbList)f.getView().findViewById(R.id.Select_BreadCrumbList);
                        _bcl.pop();
                }
                jArray.put(jObj);
                sIoSm.emit("getTargetList", jArray);
                return true;

            } catch (Exception e) {
                Log.d(TAG, e.toString());
                finish();
            }
        }
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "OnActivityResult");

        switch( requestCode ) {
            case PHOTO_CONFIRM_ACTIVITY:
                resultCode = CameraShot.NON_CAMERA_SHOT;
            case CAMERA_SHOT_ACTIVITY :
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                sIoSm.bindSIOService( this );

                switch ( resultCode ) {
                    case CameraShot.CAMERA_SHOT :
                        Log.d(TAG, "CameraShot Result");
                        JSONArray jArray = new JSONArray();
                        JSONObject jObj = new JSONObject();
                        try {
                            Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
                            CustomBreadCrumbList _bcl = (CustomBreadCrumbList)f.getView().findViewById(R.id.Select_BreadCrumbList);
                            String currentParentId = _bcl.getTargetIdList().get(_bcl.size() - 1);
                            jObj.put("parent", currentParentId);
                            jArray.put(jObj);
                            sIoSm.emit("getTargetList", jArray);
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                            finish();
                        }
                        break;
                    case CameraShot.NON_CAMERA_SHOT :
                        sIoSm.bindSIOService( this );
                        break;
                    default:
                        break;
                }
                break;

            case SETTINGS_ACTIVITY:
                //TODO: 設定画面から戻ってきた時にサービス再起動?
                //TODO: 送信待ちジョブが存在しないことを確認しないとね
                break;
        }
        if(requestCode == CAMERA_SHOT_ACTIVITY) {
            if(resultCode == 1 ){

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}
