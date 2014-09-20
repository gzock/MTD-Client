package com.example.mtd_client.app;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ListView;


public class SendJobDataResultFragment extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_job_data_result_fragment);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_job_data_result, menu);
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
            implements IServiceConnectedCallbackListener {

        private static final String TAG = "SendJobDataResult";
        private SocketIOServiceManager sIoSm = new SocketIOServiceManager();

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_send_job_data_result, container, false);

            sIoSm.setCallback(this);
            sIoSm.init();
            sIoSm.bindSIOService(rootView.getContext());
            return rootView;
        }

        @Override
        public void serviceConnectedCallback() {

            SendJobData job = new SendJobData();
            job.setData("hogehoge");
            job.setTargetName("title");

            sIoSm.getSocketio().addSendJob(job);

            SendJobDataResultAdapter sendJobDataResultAdapter = new SendJobDataResultAdapter(getActivity(), 0, sIoSm.getSendJobs());
            ListView listView = (ListView) getActivity().findViewById(R.id.send_job_data_result_list);
            listView.setAdapter( sendJobDataResultAdapter );
            sendJobDataResultAdapter.notifyDataSetChanged();

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
