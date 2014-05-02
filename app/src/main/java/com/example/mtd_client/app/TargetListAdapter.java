package com.example.mtd_client.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Gzock on 2014/05/02.
 */
public class TargetListAdapter extends ArrayAdapter {

    private ArrayList<TargetListData> items;
    private LayoutInflater inflater;

    public TargetListAdapter(Context context, int textViewResourceId,
                       ArrayList<TargetListData> items) {
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
            view = inflater.inflate(R.layout.target_row, null);
        }

        //viewから必要な要素を取得して値を設定する。
        TextView targetName = (TextView) view.findViewById(R.id.targetName);
        TextView per1       = (TextView) view.findViewById(R.id.per1);
        TextView per2       = (TextView) view.findViewById(R.id.per2);

        ImageView image = (ImageView) view.findViewById(R.id.checkImage);

        if (items != null && items.size() >= position) {
            targetName.setText(items.get(position).getTargetName());
            //message.setText(items.get(position).getMessage());
            //image.setImageResource(R.drawable.ic_launcher);
        }

        return view;
    }
}
