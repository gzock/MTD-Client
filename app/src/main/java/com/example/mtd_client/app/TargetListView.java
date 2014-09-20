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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.koushikdutta.async.http.socketio.SocketIOClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.annotation.Target;
import java.util.ArrayList;


public class TargetListView extends ActionBarActivity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        IRcvTargetListListener,
        IRcvPhotoConfirmListener,
        IRcvDisConnectedListener,
        IServiceConnectedCallbackListener {

    private static final String TAG = "TargetListView";
    private static final int CAMERA_SHOT_ACTIVITY  = 0;
    private static final int PHOTO_CONFIRM_ACTIVITY = 1;
    private static final int SETTINGS_ACTIVITY      = 10;
    private static final int PROJECT_LIST_DIALOG = 0;
    private static final int TARGET_DIALOG       = 1;
    private static final int TARGET_DIALOG_2     = 2;
    private static final int TARGET_EDIT_DIALOG  = 3;
    private static final int TARGET_DELTE_DIALOG = 4;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private              String            currentParentId   = null;

    TargetListData item = null;
    private Dialog dialog = null;
    private Boolean firstTargetListUpdate = true;
    CustomBreadCrumbList _bcl = null;
    private SocketIOServiceManager sIoSm = new SocketIOServiceManager();

    private String projectName = null;
    private String projectId = null;

    private String mRootTargetId = null;


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentViewより前にWindowにActionBar表示を設定
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_target_list_view);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_2);
        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer_2,
                (DrawerLayout) findViewById(R.id.drawer_layout_2));


        Bundle extras;
        extras = getIntent().getExtras();
        //値が設定されていない場合にはextrasにはnullが設定されます。
        if(extras != null) {
            //値が設定されている場合
            projectId = mRootTargetId = extras.getString("rootTargetId");
            projectName = extras.getString("rootTargetName");
        }

        sIoSm.setCallback(TargetListView.this);
        sIoSm.init();
        sIoSm.bindSIOService(TargetListView.this);
        sIoSm.setRcvTargetListListener(this);

    }

    @Override
    public void serviceConnectedCallback() {
        try {
            JSONArray jArray = new JSONArray();
            JSONObject jObj = new JSONObject();
            jObj.put("parent", mRootTargetId);
            jArray.put(jObj);
            sIoSm.emit("getTargetList", jArray);
            sIoSm.clearTargetListData();
            //client.emit("getTargetList", jArray);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public void rcvTargetList() {
        TargetListAdapter targetListAdapter = new TargetListAdapter(TargetListView.this, 0, sIoSm.getTargetListData());
        ListView listView = (ListView) TargetListView.this.findViewById(R.id.targetListView);
        listView.setAdapter( targetListAdapter );
        targetListAdapter.notifyDataSetChanged();

        if( firstTargetListUpdate ) {
            _bcl = (CustomBreadCrumbList)findViewById(R.id.Select_BreadCrumbList);
            _bcl.setOnClickListener(new BreadCrumbList.OnClickListener() {
                @Override
                public void onClick(View v, int position) {
                    try {
                        JSONArray jArray = new JSONArray();
                        JSONObject jObj = new JSONObject();
                        jObj.put("parent", _bcl.getTargetIdList().get(position));
                        jArray.put(jObj);
                        sIoSm.emit("getTargetList", jArray);
                        sIoSm.clearTargetListData();
                        //client.emit("getTargetList", jArray);
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                }

            });
            _bcl.push(projectName, projectId);
            firstTargetListUpdate = false;
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                item = (TargetListData)listView.getItemAtPosition(position);
                //client = socketio.getSocketIOClinet();

                if( item.getType().equals( 0 ) ) {
                    Log.d(TAG, "isConnected -> " + sIoSm.isSIOServiceState());
                    JSONArray jArray = new JSONArray();
                    JSONObject jObj = new JSONObject();
                    try {
                        jObj.put("parent", item.getId());
                        jArray.put(jObj);
                        sIoSm.emit("getTargetList", jArray);
                        sIoSm.clearTargetListData();
                        //client.emit("getTargetList", jArray);
                        _bcl.push(item.getTargetName(), item.getId());

                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                    Log.d(TAG, "selected -> " + item.getTargetName());

                } else {
                    showDialog( TARGET_DIALOG );
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                item = (TargetListData) listView.getItemAtPosition(position);
                if ( item.getType().equals( 0 ) ) {
                    showDialog( TARGET_DIALOG_2 );
                }
                // trueを返さないと、LongClickのあとに普通のclickが動いてしまう
                return true;
            }
        });
    }

    @Override
    public void rcvPhotoConfirm() {
        try {
            Intent i = new Intent(TargetListView.this, PhotoConfirm.class);
            //i.putExtra("encPhotoData", jArray.getJSONObject(0).getString("encPhotoData"));
            PassToOtherActivity pass = (PassToOtherActivity)TargetListView.this.getApplication();
            pass.setObj( sIoSm.getEncPhotoData() );
            dialog.dismiss();

            sIoSm.unBindSIOService();

            startActivityForResult(i, PHOTO_CONFIRM_ACTIVITY);

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

    }

    @Override
    public void rcvDisConnected() {
        Log.d(TAG, "Socket.IO DisConnected...");
        Toast.makeText(TargetListView.this, "Socket.IO DisConnected", Toast.LENGTH_LONG).show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        dialog = super.onCreateDialog(id);
//        client = socketio.getSocketIOClinet();


        //idは何個かダイアログがある場合に使う
        // ここはswitch使わないほうがいい
        if ( id == TARGET_DIALOG ) {
            final CharSequence[] chars = {"撮影", "確認", "編集", "削除"};

            //showDialogを呼ぶときに１度だけ呼ばれる
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( TargetListView.this );
            dialogBuilder.setTitle("操作を選択して下さい");
            dialogBuilder.setSingleChoiceItems(
                    chars,
                    0, // Initial
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String switchStr = chars[which].toString();

                            if (switchStr.equals("撮影")) {
                                Toast.makeText( TargetListView.this, switchStr, Toast.LENGTH_LONG).show();

                                Intent i = new Intent( TargetListView.this, CameraShot.class);
                                i.putExtra("TargetID", item.getId());
                                i.putExtra("TargetName", item.getTargetName());
                                dialog.dismiss();

                                sIoSm.unBindSIOService();

                                startActivityForResult(i, CAMERA_SHOT_ACTIVITY);

                            } else if (switchStr.equals("確認")) {
                                String savePhotoName = "";
                                for(LinearLayout item : _bcl.getButtonList()) {
                                    Button btn = (Button)item.findViewById(1);
                                    savePhotoName += btn.getText() + "_";
                                }
                                savePhotoName += item.getTargetName() + "_" + item.getPhotoBeforeAfter() + "_" + item.getId() + ".jpg";
                                Toast.makeText( TargetListView.this, switchStr, Toast.LENGTH_LONG).show();

                                JSONArray jArray = new JSONArray();
                                JSONObject jObj = new JSONObject();
                                try {
                                    jObj.put("projectId", projectId);
                                    jObj.put("savePhotoName", savePhotoName);
                                    jArray.put(jObj);
                                    sIoSm.emit("photoConfirm", jArray);
                                    //client.emit("photoConfirm", jArray);
                                    dialog.dismiss();

                                } catch (Exception e) {
                                    Log.d(TAG, e.toString());
                                }
                                Log.d(TAG, "saveName -> " + savePhotoName);
                                //TODO 画像確認

                            } else if (switchStr.equals("編集")) {
                                Toast.makeText( TargetListView.this, switchStr, Toast.LENGTH_LONG).show();
                                showDialog( TARGET_EDIT_DIALOG );

                            } else if (switchStr.equals("削除")) {
                                Toast.makeText( TargetListView.this, switchStr, Toast.LENGTH_LONG).show();
                                showDialog( TARGET_DELTE_DIALOG );
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

        } else if( id == TARGET_DIALOG_2 ) {
            final CharSequence[] chars = {"編集", "削除"};

            //showDialogを呼ぶときに１度だけ呼ばれる
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( TargetListView.this );
            dialogBuilder.setTitle("操作を選択して下さい");
            dialogBuilder.setSingleChoiceItems(
                    chars,
                    0, // Initial
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String switchStr = chars[which].toString();

                            if (switchStr.equals("編集")) {
                                Toast.makeText(TargetListView.this, switchStr, Toast.LENGTH_LONG).show();
                                showDialog(TARGET_EDIT_DIALOG);

                            } else if (switchStr.equals("削除")) {
                                Toast.makeText(TargetListView.this, switchStr, Toast.LENGTH_LONG).show();
                                showDialog(TARGET_DELTE_DIALOG);
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

        } else if( id == TARGET_EDIT_DIALOG ) {
            //showDialogを呼ぶときに１度だけ呼ばれる
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( TargetListView.this );

            LayoutInflater inflater = LayoutInflater.from( TargetListView.this );
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
                                sIoSm.emit("editTarget", jArray);
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

        } else if( id == TARGET_DELTE_DIALOG ) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( TargetListView.this );
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
                                sIoSm.emit("deleteTarget", jArray);
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

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        //showDialogを呼ぶとき毎回呼ばれる
        //ダイアログの状態を変化させることができる
        if ( id == 0 )
        {
            //今回なんもしない
        }
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
                        Toast.makeText(TargetListView.this, "階層トップです",Toast.LENGTH_SHORT).show();
                        //TODO: 終了処理書く
                        //return false
                    case 2:
                        jObj.put("parent", path.get(0));
                        break;
                    default :
                        jObj.put("parent", path.get( path.size() - 3 ));
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
            Intent i = new Intent( TargetListView.this, SettingsActivity.class);
            startActivityForResult(i, SETTINGS_ACTIVITY);
            return true;
        }

        if(item.getTitle().equals( "TargetAdd" )) {
            //client = socketio.getSocketIOClinet();
            LayoutInflater inflater = LayoutInflater.from(TargetListView.this);
            View view = inflater.inflate(R.layout.add_target_dialog, null);
            final EditText addTargetName = (EditText)view.findViewById(R.id.add_target_name);
            final Spinner addTargetGenre = (Spinner)view.findViewById(R.id.add_target_genre_spinner);
            final Spinner addTargetBeforeAfter = (Spinner)view.findViewById(R.id.add_target_before_after_spinner);

            new AlertDialog.Builder(TargetListView.this)
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
        } else if(item.getTitle().equals( "TargetReflesh" )) {
            JSONArray jArray = new JSONArray();
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("parent", currentParentId);
                jArray.put(jObj);
                sIoSm.emit("getTargetList", jArray);
                return true;
            } catch (Exception e) {
                Log.d(TAG, e.toString());
                finish();
            }
        } else if(item.getTitle().equals( "CloseApp" )) {
            sIoSm.stopSIOService();
            this.finish();
            this.moveTaskToBack(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        //if(sIoSm.isSIOServiceState()) {
          //  sIoSm.unBindSIOService();
            //sIOsm.stopSIOService();
        //}
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        deleteFile(TargetListView.this.getCacheDir());
        if(sIoSm.isSIOServiceState()) {
            sIoSm.unBindSIOService();
            //sIOsm.stopSIOService();
        }

        super.onDestroy();

    }

    // 感謝 : http://www.syboos.jp/java/doc/delete-file-or-folder.html
    public static boolean deleteFile(File dirOrFile) {
        if (dirOrFile.isDirectory()) {//ディレクトリの場合
            String[] children = dirOrFile.list();//ディレクトリにあるすべてのファイルを処理する
            for (int i=0; i<children.length; i++) {
                boolean success = deleteFile(new File(dirOrFile, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // 削除
        return dirOrFile.delete();
    }
    @Override
    public void onUserLeaveHint(){
        //ホームボタンが押された時や、他のアプリが起動した時に呼ばれる
        //戻るボタンが押された場合には呼ばれない
        //Toast.makeText(getApplicationContext(), TAG + " Good bye!" , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container_2, PlaceholderFragment.newInstance(position + 1))
                .commit();
        // 元々、ナビゲーションバーから選んだアイテムの数字を表示させる処理が入ってた
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

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
            case 6:
                mTitle = getString(R.string.title_send_job_data_result);
                break;
        }
    }
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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

        private Activity mActivity = null;

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

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((TargetListView) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));

            mActivity = activity;
        }
    }

}
