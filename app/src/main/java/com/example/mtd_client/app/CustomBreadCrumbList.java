package com.example.mtd_client.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Gzock on 2014/07/26.
 */
/**
 *  http://relog.xii.jp/mt5r/2011/01/android-5.html 大感謝!!!
*/
public class CustomBreadCrumbList extends BreadCrumbList {

    private OnClickListener _onClickListener = null;		//リスナー
    private ArrayList<String> targetIdList = null;

    public ArrayList<String> getTargetIdList() {
        if(targetIdList == null){
            targetIdList = new ArrayList<String>();
        }
        return targetIdList;
    }

    public CustomBreadCrumbList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void push(String name, String id){
        //レイアウト作成
        LinearLayout layout = new LinearLayout(getContext());

        //セパレータが指定されていた場合は追加
        if(getButtonList().size() == 0){
        }else if(getSeparatorResourceId() < 0){
        }else{
            ImageView image = new ImageView(getContext());
            image.setBackgroundResource(getSeparatorResourceId());
            layout.addView(image);
        }

        //ボタン作成
        Button button = new Button(getContext());
        button.setText(name);
        button.setOnClickListener(this);
        button.setId(1);
        button.setTextColor(getTextColor());
        if(getButtonBackgroundResourceId() >= 0){
            button.setBackgroundResource(getButtonBackgroundResourceId());
        }
        layout.addView(button);

        //真ん中寄せにする
        layout.setGravity(Gravity.CENTER);

        //親に追加
        getButtonList().add(layout);
        getButtonArea().addView(layout);
        //追加
        getTargetIdList().add(id);
    }

    public LinearLayout pop(){
        int del_index = getButtonList().size() - 1;
        LinearLayout ret = getButtonList().get(del_index);
        //リストから削除
        getButtonList().remove(del_index);
        //レイアウトから削除
        getButtonArea().removeView(ret);
        //追加
        getTargetIdList().remove(del_index);

        return ret;
    }

}
