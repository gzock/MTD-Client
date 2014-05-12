package com.example.mtd_client.app;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;


public class TargetListView extends ActionBarActivity {

    private static final String TAG = "TargetListView";
    Handler mHandler;
    private ServiceManager sm = new ServiceManager();
    private String targetList = null;
    private ArrayList<TargetListData> dataList = new ArrayList<TargetListData>();
    private ArrayList<TargetListData> previousDataList = new ArrayList<TargetListData>();

    private String previousTarget = null;
    private String pjName = null;

    private TargetListAdapter targetListAdapter = null;
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        for(int i = 0; i < temp.length; i += 6) {
            TargetListData _data = new TargetListData();

            _data.setId          ( temp[i] );
            _data.setProjectName ( temp[i + 1] );
            _data.setParent      ( temp[i + 2] );
            _data.setTargetName  ( temp[i + 3] );
            _data.setBeforeAgter ( temp[i + 4] );
            _data.setPicture     ( temp[i + 5] );
            dataList.add         ( _data );
        }

        targetListAdapter = new TargetListAdapter(this, 0, dataList);
        listView = (ListView) findViewById(R.id.targetListView);
        listView.setAdapter( targetListAdapter );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                final TargetListData item = (TargetListData)listView.getItemAtPosition(position);

                //施工前/後があるかないかで、ターゲットの種類が建物か機器か判定する
                if( item.getBeforeAfter().equals("") ) {
                    previousDataList = dataList;
                    sm.send( "getTargetListUpdate," + item.getId());
                    Log.d(TAG, "selected -> " + item.getTargetName());
                    previousTarget = item.getId();

                } else {
                    final CharSequence[] chars = {"撮影", "確認", "追加", "編集", "削除"};
                    new AlertDialog.Builder(TargetListView.this)
                            .setTitle("操作を選択して下さい")
                            .setSingleChoiceItems(
                                    chars,
                                    0, // Initial
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String switchStr = chars[ which ].toString();
                                            if ( switchStr.equals("撮影") ) {
                                                Toast.makeText(TargetListView.this, switchStr,Toast.LENGTH_LONG).show();
                                                Intent i = new Intent(TargetListView.this, CameraShot.class);
                                                i.putExtra("TargetID", item.getId());
                                                i.putExtra("ProjectName", item.getProjectName());
                                                i.putExtra("TargetName", item.getTargetName());
                                                startActivity(i);

                                            } else if ( switchStr.equals("確認") ) {
                                                Toast.makeText(TargetListView.this, switchStr,Toast.LENGTH_LONG).show();
                                                //TODO 画像確認
                                            } else if ( switchStr.equals("追加") ) {
                                                Toast.makeText(TargetListView.this, switchStr,Toast.LENGTH_LONG).show();

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
                                                                        sm.send("addTarget," + pjName + "," + previousTarget + "," + addTargetName.getText().toString() + "," +
                                                                                addTargetGenre.getSelectedItem().toString() + "," + addTargetBeforeAfter.getSelectedItem().toString());
                                                                    }
                                                                }
                                                        )
                                                        .setNegativeButton("キャンセル", null)
                                                        .show();

                                            } else if ( switchStr.equals("編集") ) {
                                                Toast.makeText(TargetListView.this, switchStr,Toast.LENGTH_LONG).show();

                                                LayoutInflater inflater = LayoutInflater.from(TargetListView.this);
                                                View view = inflater.inflate(R.layout.edit_target_dialog, null);
                                                final EditText editTargetName = (EditText)view.findViewById(R.id.edit_target_name);
                                                editTargetName.setText( item.getTargetName() );

                                                new AlertDialog.Builder(TargetListView.this)
                                                        .setTitle("項目の編集")
                                                        .setView(view)
                                                        .setPositiveButton(
                                                                "決定",
                                                                new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        Log.d(TAG, "ProjectName -> "      + pjName);
                                                                        Log.d(TAG, "Edit Target ID -> "      + item.getId());
                                                                        Log.d(TAG, "Edit Target Name -> " + editTargetName.getText().toString());
                                                                        sm.send("editTarget," + item.getId() + "," + editTargetName.getText().toString());
                                                                    }
                                                                }
                                                        )
                                                        .setNegativeButton("キャンセル", null)
                                                        .show();

                                            } else if ( switchStr.equals("削除") ) {
                                                // TODO 項目削除
                                            }
                                        }
                                    }
                            )
                            .setPositiveButton("OK", null)
                            .show();
                }


            }
        });


//        sm.bindWsService(TargetListView.this);
        sm.bindWsService( getApplicationContext() );
        sm.setView( (ViewGroup)this.getWindow().getDecorView() );

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            if( previousDataList != null ) {
                targetListAdapter = new TargetListAdapter(this, 0, previousDataList);
                listView.setAdapter( targetListAdapter );
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.target_list_view, menu);
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

}
