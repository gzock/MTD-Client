package com.example.mtd_client.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;


public class LoginFragment extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
            implements IRcvProjectListListener,
                        IRcvDisConnectedListener {

        private static final String TAG = "LoginFragment";
        private IProjectSelectedCallbackListener mProjectSelectedCallback = null;
       private SocketIOServiceManager sIOsm = new SocketIOServiceManager();

        public PlaceholderFragment() {
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            if (activity instanceof IProjectSelectedCallbackListener == false) {
                throw new ClassCastException("activity が IProjectSelectedCallbackListener を実装していません.");
            }

            mProjectSelectedCallback = ((IProjectSelectedCallbackListener) activity);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_login, container, false);


            sIOsm.init();
            //sIOsm.startSIOService( rootView.getContext() );
            sIOsm.bindSIOService( rootView.getContext() );
            sIOsm.setRcvProjectListListener( this );

            // 送信ボタン
            Button testBtn = (Button) rootView.findViewById(R.id.sendBtn);
            testBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("message", "hogehoge");
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }

                    if(sIOsm.isSIOServiceState() && mProjectSelectedCallback != null) {
                        try {
                            // 送信

                            EditText userId = (EditText) rootView.findViewById(R.id.userIdTextBox);
                            EditText password = (EditText) rootView.findViewById(R.id.passTextBox);
                            Log.d(TAG, "userId -> " + userId.getText());
                            Log.d(TAG, "password -> " + password.getText());

                            JSONArray jArray = new JSONArray();
                            JSONObject jObj = new JSONObject();
                            try {
                                jObj.put("username", userId.getText().toString());
                                jObj.put("password", password.getText().toString());
                                jArray.put(jObj);
                                sIOsm.emit("login", jArray);

                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                            }

                        } catch (NotYetConnectedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(rootView.getContext(), "WebSocketサービスが非接続状態です", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return rootView;
        }

        @Override
        public void rcvProjectList() {
            Log.d(TAG, "rcvProjectList");
            ProjectListDialogFragment dialog = new ProjectListDialogFragment();
            dialog.show(getFragmentManager(), "hoge");
        }

        @Override
        public void rcvDisConnected() {
            Log.d(TAG, "Socket.IO DisConnected...");
            Toast.makeText(getActivity(), "Socket.IO DisConnected", Toast.LENGTH_LONG).show();
        }

        public class ProjectListDialogFragment extends android.support.v4.app.DialogFragment {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final ArrayList<String> pjNameList = sIOsm.getPjNameList();
                final ArrayList<String> pjRootTargetList = sIOsm.getPjRootTargetList();
                //pjNameList = sIOsm.getPjNameList();

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( getActivity() );
                dialogBuilder.setTitle("案件名を選択して下さい");

                final CharSequence[] chars = pjNameList.toArray(new CharSequence[pjNameList.size()]);

                dialogBuilder.setSingleChoiceItems(
                        chars,
                        0, // Initial
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sIOsm.unBindSIOService();
                                Log.d(TAG, pjNameList.get(which) + " Selected");
                                sIOsm.setProjectName(pjNameList.get(which));
                                sIOsm.setRootTargetId(pjRootTargetList.get(which));
                                mProjectSelectedCallback.projectSelectedCallback();
                                dialog.dismiss();
                                /*
                                projectName = pjNameList.get(which);
                                projectId = pjRootTargetList.get(which);

                                Intent intent = new Intent(MainActivity.this, TargetListView.class);
                                intent.putExtra("rootTargetId", projectId);
                                intent.putExtra("rootTargetName", projectName);
                                startActivity(intent);
                                */

                            }
                        }
                );
                dialogBuilder.setPositiveButton("OK", null);
                return dialogBuilder.create();
            }
        }

        @Override
        public void onDestroy() {

            if(sIOsm.isSIOServiceState()) {
                sIOsm.unBindSIOService();
                //sIOsm.stopSIOService();
            }

            super.onDestroy();
        }
    }
}
