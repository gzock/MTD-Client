package com.example.mtd_client.app;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Gzock on 2014/09/09.
 */
public class SendJobDataResultAdapter extends ArrayAdapter {

    private ArrayList<SendJobData> items;
    private LayoutInflater inflater;

    public SendJobDataResultAdapter(Context context, int textViewResourceId,
                             ArrayList<SendJobData> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = inflater.inflate(R.layout.send_job_data_result_row, null);
        }

        //viewから必要な要素を取得して値を設定する。
        TextView targetName = (TextView) view.findViewById(R.id.targetName);
        TextView result       = (TextView) view.findViewById(R.id.result);
        TextView size       = (TextView) view.findViewById(R.id.size);
        TextView sendTime       = (TextView) view.findViewById(R.id.send_time);

        if (items != null && items.size() > 0) {

            // ターゲット名設定
            targetName.setText(items.get(position).getTargetName());
            if(items.get(position).getSendJobEnd()) {
                result.setText("送信完了");
            } else {
                result.setText("送信未完了");
            }
            size.setText(Integer.toString(items.get(position).getDataSize()));
            Time mTime = items.get(position).getSendJobEndTime();
            sendTime.setText( mTime.hour + "時 " + mTime.minute + "分" + mTime.second + "秒" );
        } else {
            targetName.setText("送信ジョブが存在していません");
        }

        return view;
    }
}
