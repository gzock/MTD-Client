package com.example.mtd_client.app;

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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.lang.annotation.Target;
import java.util.ArrayList;


public class TargetListView extends ActionBarActivity {

    private static final String TAG = "TargetListView";
    private static final int CAMERA_SHOT_ACTIVITY  = 0;

    Handler mHandler;
    private ServiceManager sm = new ServiceManager();
    private String targetList = null;
    private ArrayList<TargetListData> dataList = new ArrayList<TargetListData>();

    private String previousTarget = null;
    private String previousTargetParentId = null;
    private String previousParentId = null;
    private String currentTargetParentId = null;
    private String pjName = null;

    private TargetListAdapter targetListAdapter = null;
    private ListView listView = null;

    TargetListData item = null;
    private Dialog dialog = null;

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

        Bundle extras;
        extras = getIntent().getExtras();
        //値が設定されていない場合にはextrasにはnullが設定されます。
        if(extras != null) {
            //値が設定されている場合
            targetList = extras.getString("TargetList");
            previousTarget = pjName = extras.getString("PJName");
        }

        String[] temp = targetList.split(",");
        for(int i = 0; i < temp.length; i += 11) {
            TargetListData _data = new TargetListData();

            _data.setId               ( temp[i] );
            _data.setParent           ( temp[i + 1] );
            if( i == 0 ) { sm.setCurrentParentId( temp[i + 1] ); }
            _data.setTargetName       ( temp[i + 2] );
            _data.setPhotoBeforeAfter ( temp[i + 3] );
            _data.setPhotoCheck       ( temp[i + 4] );
            _data.setBfrPhotoShotCnt ( temp[i + 5] );
            _data.setBfrPhotoTotalCnt( temp[i + 6] );
            _data.setAftPhotoShotCnt  ( temp[i + 7] );
            _data.setAftPhotoTotalCnt ( temp[i + 8] );
            _data.setType             ( temp[i + 9] );
            _data.setLock             ( temp[i + 10]);
            dataList.add              ( _data );
        }

        targetListAdapter = new TargetListAdapter(this, 0, dataList);
        listView = (ListView) findViewById(R.id.targetListView);
        listView.setAdapter( targetListAdapter );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                item = (TargetListData)listView.getItemAtPosition(position);

                if( item.getType().equals( 0 ) ) {
                    sm.send( "getTargetListUpdate," + item.getId());
                    Log.d(TAG, "selected -> " + item.getTargetName());
                    previousTarget = item.getId();
                    previousTargetParentId = item.getParent();

                } else {
                    showDialog(0);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                item = (TargetListData) listView.getItemAtPosition(position);
                if ( item.getType() == 0 ) {
                    showDialog(1);
                }
                // trueを返さないと、LongClickのあとに普通のclickが動いてしまう
                return true;
            }
        });

        if(!sm.isWsServiceState()) {
            sm.bindWsService(getApplicationContext());
        }
        sm.setView( (ViewGroup)this.getWindow().getDecorView() );

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        dialog = super.onCreateDialog(id);

        //idは何個かダイアログがある場合に使う
        if ( id == 0 )
        {
            final CharSequence[] chars = {"撮影", "確認", "編集", "削除"};

            //showDialogを呼ぶときに１度だけ呼ばれる
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TargetListView.this);
            dialogBuilder.setTitle("操作を選択して下さい");
            dialogBuilder.setSingleChoiceItems(
                    chars,
                    0, // Initial
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String switchStr = chars[which].toString();

                            if (switchStr.equals("撮影")) {
                                Toast.makeText(TargetListView.this, switchStr, Toast.LENGTH_LONG).show();

                                sm.unBindWsService();
                                Intent i = new Intent(TargetListView.this, CameraShot.class);
                                i.putExtra("TargetID", item.getId());
                                i.putExtra("TargetName", item.getTargetName());
                                dialog.dismiss();
                                startActivityForResult(i, CAMERA_SHOT_ACTIVITY);

                            } else if (switchStr.equals("確認")) {
                                Toast.makeText(TargetListView.this, switchStr, Toast.LENGTH_LONG).show();
                                //TODO 画像確認

                            } else if (switchStr.equals("編集")) {
                                Toast.makeText(TargetListView.this, switchStr, Toast.LENGTH_LONG).show();
                                showDialog(2);

                            } else if (switchStr.equals("削除")) {
                                Toast.makeText(TargetListView.this, switchStr, Toast.LENGTH_LONG).show();
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
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TargetListView.this);
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
                                showDialog(2);

                            } else if (switchStr.equals("削除")) {
                                Toast.makeText(TargetListView.this, switchStr, Toast.LENGTH_LONG).show();
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
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TargetListView.this);

            LayoutInflater inflater = LayoutInflater.from(TargetListView.this);
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
                                    Log.d(TAG, "ProjectName -> " + pjName);
                                    Log.d(TAG, "Edit Target ID -> " + item.getId());
                                    Log.d(TAG, "Edit Target Name -> " + editTargetName.getText().toString());
                                    Log.d(TAG, "Edit TargetParen ID -> " + item.getParent());

                                    sm.send("editTarget," + item.getId()  + "," + editTargetName.getText().toString() + "," + item.getParent() );
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
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TargetListView.this);
            dialogBuilder.setTitle("項目の削除");
            dialogBuilder.setMessage(item.getTargetName() + "を削除しますか? \n対象配下に機器や建物が存在する場合は、それらも削除されてしまいますので、ご注意下さい");
            dialogBuilder.setPositiveButton(
                            "決定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "Delete Target ID -> " + item.getId());
                                    Log.d(TAG, "Delete TargetParent -> " + item.getParent());
                                    sm.send("deleteTarget," + item.getId() + "," + item.getParent());
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
        if(keyCode== KeyEvent.KEYCODE_BACK){
            if( !sm.getCurrentParentId().equals(null) ) {
                //sm.send("getTargetListUpdate," + previousTargetParentId);
                sm.send("getTargetListUpdate," + sm.getCurrentParentId());
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "OnActivityResult");

        switch( requestCode ) {
            case CAMERA_SHOT_ACTIVITY :
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                //if(!sm.isWsServiceState()) {
                    sm.bindWsService(TargetListView.this);
                //}

                switch ( resultCode ) {
                    case CameraShot.CAMERA_SHOT :
                        /*
                        if(sm.isWsConnected()) {
                            sm.send("getTargetListUpdate," + currentTargetParentId);
                        }
                        */
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
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.target_list_view, menu);

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
                                    Log.d(TAG, "ProjectName -> "           + pjName);
                                    Log.d(TAG, "Add Target Name -> "       + addTargetName.getText().toString());
                                    Log.d(TAG, "Add Target Genre -> "      + addTargetGenre.getSelectedItem().toString());
                                    Log.d(TAG, "Add Target BeforAfter -> " + addTargetBeforeAfter.getSelectedItem().toString());
                                    sm.send("addTarget," + pjName + "," + previousTargetParentId + "," + addTargetName.getText().toString() + "," +
                                            addTargetGenre.getSelectedItem().toString() + "," + addTargetBeforeAfter.getSelectedItem().toString());
                                }
                            }
                    )
                    .setNegativeButton("キャンセル", null)
                    .show();
        } else if(item.getTitle().equals( "TargetReflesh" )) {
            sm.send("getTargetListUpdate," + sm.getCurrentParentId());
        }
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }


    private ArrayList<String> toArrayStr(String message) {
        String[] strs = message.split(",");
        ArrayList<String> strList = new ArrayList<String>();

        for(int i = 0; i < strs.length; i++) {
            strList.add(strs[i]);
        }
        return strList;
    }

    @Override
    protected void onDestroy() {
        deleteFile(TargetListView.this.getCacheDir());
        //if(sm.isWsConnected()) {
        //    sm.disConnect();
        //}
        if(sm.isWsServiceState()) {
            sm.unBindWsService();
        }
        //sm.stopWsService();

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

}
