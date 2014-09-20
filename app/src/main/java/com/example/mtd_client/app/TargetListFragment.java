package com.example.mtd_client.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class TargetListFragment extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_list_fragment);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.target_list, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
            implements IRcvTargetListListener,
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


        private CharSequence mTitle;

        private              String            currentParentId   = null;

        TargetListData item = null;
        private Boolean firstTargetListUpdate = true;
        CustomBreadCrumbList _bcl = null;
        private SocketIOServiceManager sIoSm = new SocketIOServiceManager();

        private String projectName = null;
        private String projectId = null;

        private String mRootTargetId = null;

        View v = null;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_target_list, container, false);
            v = rootView;
            sIoSm.setCallback(this);
            sIoSm.init();
            sIoSm.bindSIOService(rootView.getContext());
            sIoSm.setRcvTargetListListener(this);

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();

            //sIoSm.bindSIOService(v.getContext());
            sIoSm.setRcvTargetListListener(this);
        }

        @Override
        public void serviceConnectedCallback() {
            try {
                JSONArray jArray = new JSONArray();
                JSONObject jObj = new JSONObject();
                jObj.put("parent", sIoSm.getRootTargetId());
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
            TargetListAdapter targetListAdapter = new TargetListAdapter(getActivity(), 0, sIoSm.getTargetListData());
            ListView listView = (ListView) getActivity().findViewById(R.id.targetListView);
            listView.setAdapter( targetListAdapter );
            targetListAdapter.notifyDataSetChanged();

            if( firstTargetListUpdate ) {
                _bcl = (CustomBreadCrumbList)getActivity().findViewById(R.id.Select_BreadCrumbList);
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
                _bcl.push(sIoSm.getProjectName(), sIoSm.getRootTargetId());
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
                        TargetControlDialogFragment showDialog = new TargetControlDialogFragment();
                        showDialog.show(getFragmentManager(), "hoge");
                    }
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                    ListView listView = (ListView) parent;
                    item = (TargetListData) listView.getItemAtPosition(position);
                    if ( item.getType().equals( 0 ) ) {
                        TargetControlDialogFragment_2 showDialog = new TargetControlDialogFragment_2();
                        showDialog.show(getFragmentManager(), "hoge");
                    }
                    // trueを返さないと、LongClickのあとに普通のclickが動いてしまう
                    return true;
                }
            });
        }

        @Override
        public void rcvPhotoConfirm() {
            try {
                Intent i = new Intent(getActivity(), PhotoConfirm.class);
                //i.putExtra("encPhotoData", jArray.getJSONObject(0).getString("encPhotoData"));
                PassToOtherActivity pass = (PassToOtherActivity)getActivity().getApplication();
                pass.setObj(sIoSm.getEncPhotoData());

                sIoSm.unBindSIOService();

                startActivityForResult(i, PHOTO_CONFIRM_ACTIVITY);

            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }

        }

        @Override
        public void rcvDisConnected() {
            Log.d(TAG, "Socket.IO DisConnected...");
            Toast.makeText(getActivity(), "Socket.IO DisConnected", Toast.LENGTH_LONG).show();
        }

        public class TargetControlDialogFragment extends android.support.v4.app.DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                //idは何個かダイアログがある場合に使う
                // ここはswitch使わないほうがいい
                final CharSequence[] chars = {"撮影", "確認", "編集", "削除"};

                    //showDialogを呼ぶときに１度だけ呼ばれる
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( getActivity() );
                    dialogBuilder.setTitle("操作を選択して下さい");
                    dialogBuilder.setSingleChoiceItems(
                            chars,
                            0, // Initial
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String switchStr = chars[which].toString();

                                    if (switchStr.equals("撮影")) {
                                        Toast.makeText( getActivity(), switchStr, Toast.LENGTH_LONG).show();

                                        Intent i = new Intent( getActivity(), CameraShot.class);
                                        i.putExtra("TargetID", item.getId());
                                        i.putExtra("TargetName", item.getTargetName());

                                        sIoSm.unBindSIOService();

                                        startActivityForResult(i, CAMERA_SHOT_ACTIVITY);

                                    } else if (switchStr.equals("確認")) {
                                        String savePhotoName = "";
                                        for(LinearLayout item : _bcl.getButtonList()) {
                                            Button btn = (Button)item.findViewById(1);
                                            savePhotoName += btn.getText() + "_";
                                        }
                                        savePhotoName += item.getTargetName() + "_" + item.getPhotoBeforeAfter() + "_" + item.getId() + ".jpg";
                                        Toast.makeText( getActivity(), switchStr, Toast.LENGTH_LONG).show();

                                        JSONArray jArray = new JSONArray();
                                        JSONObject jObj = new JSONObject();
                                        try {
                                            jObj.put("projectId", projectId);
                                            jObj.put("savePhotoName", savePhotoName);
                                            jArray.put(jObj);
                                            sIoSm.emit("photoConfirm", jArray);
                                            //client.emit("photoConfirm", jArray);

                                        } catch (Exception e) {
                                            Log.d(TAG, e.toString());
                                        }
                                        Log.d(TAG, "saveName -> " + savePhotoName);
                                        //TODO 画像確認

                                    } else if (switchStr.equals("編集")) {
                                        TargetEditDialogFragment showDialog = new TargetEditDialogFragment();
                                        showDialog.show(getFragmentManager(), "hoge");

                                    } else if (switchStr.equals("削除")) {
                                        TargetDeleteDialogFragment showDialog = new TargetDeleteDialogFragment();
                                        showDialog.show(getFragmentManager(), "hoge");
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
                dialogBuilder.setPositiveButton("OK", null);
                return dialogBuilder.create();
            }
        }
        public class TargetControlDialogFragment_2 extends android.support.v4.app.DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final CharSequence[] chars = {"編集", "削除"};

                //showDialogを呼ぶときに１度だけ呼ばれる
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( getActivity() );
                dialogBuilder.setTitle("操作を選択して下さい");
                dialogBuilder.setSingleChoiceItems(
                        chars,
                        0, // Initial
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String switchStr = chars[which].toString();

                                if (switchStr.equals("編集")) {
                                    TargetEditDialogFragment showDialog = new TargetEditDialogFragment();
                                    showDialog.show(getFragmentManager(), "hoge");

                                } else if (switchStr.equals("削除")) {
                                    TargetDeleteDialogFragment showDialog = new TargetDeleteDialogFragment();
                                    showDialog.show(getFragmentManager(), "hoge");
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
                return dialogBuilder.create();
            }
        }
        public class TargetEditDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                //showDialogを呼ぶときに１度だけ呼ばれる
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( getActivity() );

                LayoutInflater inflater = LayoutInflater.from( getActivity() );
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
                return dialogBuilder.create();
            }
        }

        public class TargetDeleteDialogFragment extends DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( getActivity() );
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
                return dialogBuilder.create();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            if(sIoSm.isSIOServiceState()) {
                sIoSm.unBindSIOService();
            }
        }
    }
}
