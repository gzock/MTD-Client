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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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
        for(int i = 0; i < temp.length; i += 4) {
            TargetListData _data = new TargetListData();

            _data.setId          ( temp[i] );
            _data.setTargetName  ( temp[i + 1] );
            _data.setBeforeAgter ( temp[i + 2] );
            _data.setPicture     ( temp[i + 3] );
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
                                                i.putExtra("TargetName", item.getTargetName());
                                                startActivity(i);

                                            } else if ( switchStr.equals("確認") ) {
                                                Toast.makeText(TargetListView.this, switchStr,Toast.LENGTH_LONG).show();
                                            } else if ( switchStr.equals("追加") ) {
                                                Toast.makeText(TargetListView.this, switchStr,Toast.LENGTH_LONG).show();
                                            } else if ( switchStr.equals("編集") ) {
                                                Toast.makeText(TargetListView.this, switchStr,Toast.LENGTH_LONG).show();
                                            } else if ( switchStr.equals("削除") ) {

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


        Button testBtn = (Button)findViewById(R.id.testBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sm.send("getTargetList," + targetPJ);
            }
        });
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
