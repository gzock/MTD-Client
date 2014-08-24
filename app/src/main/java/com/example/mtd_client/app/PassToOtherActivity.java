package com.example.mtd_client.app;

import android.app.Application;
import android.util.Log;

/**
 * Created by Gzock on 2014/07/30.
 */
public class PassToOtherActivity extends Application {
    private final String TAG = "PassToOtherActivity";
    Object passObj = null;

    @Override
    public void onCreate() {
    }

    @Override
    public void onTerminate() {
    }

    public void setObj(Object obj){
        passObj = obj;
    }

    public Object getObj(){
        return passObj;
    }

    public void clearObj(){
        passObj = null;
    }
}
