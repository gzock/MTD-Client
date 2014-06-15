package com.example.mtd_client.app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Gzock on 2014/06/15.
 */
public class SendJobData {

    private static final String TAG = "SendJobData";

    private String targetId = null;
    private String data = null;
    private String checkSum = null;

    public void setTargetID ( String str ) { targetId = str; }
    public void setData ( String str ) { data = str; }
    public void setCheckSum ( String str ) { checkSum = str; }

    public String getTargetId () { return targetId; }
    public String getData () { return  data; }
    public String getCheckSum() { return checkSum; }

    public JSONArray getSendaDataJson() {

        if( targetId != null && data != null && checkSum != null) {
            JSONArray jArray = new JSONArray();
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("id", targetId);
                jObj.put("data", data);
                jObj.put("check_sum", checkSum);
                jArray.put(jObj);
                return jArray;

            } catch (Exception e) {
                Log.d(TAG, e.toString());
                return null;
            }
        } else {
            Log.d(TAG, "必要な値が不足しています");
            return null;
        }
    }

}