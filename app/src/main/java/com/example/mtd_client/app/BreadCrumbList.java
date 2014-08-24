/*
 * Copyright 2011 IoriAYANE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.mtd_client.app;

import java.util.ArrayList;

import android.R.color;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class BreadCrumbList extends HorizontalScrollView
	implements OnClickListener{

	private LinearLayout _buttonArea = null;				//リストを配置するレイアウト
	private ArrayList<LinearLayout> _buttonList = null;		//パンくずリストのボタン
	private OnClickListener _onClickListener = null;		//リスナー
	private int _textColor = 0xff000000;					//テキストカラー
	private int _buttonBackgroundResourceId = -1;			//ボタン背景のリソースID
	private int _separatorResourceId = -1;					//セパレータのリソースID
	private boolean _isUnResponse = false;					//ボタンを押しても反応しないようにする
	
	/**
	 * リストを配置するレイアウト
	 * @return
	 */
	public LinearLayout getButtonArea(){
		if(_buttonArea == null){
			_buttonArea = new LinearLayout(getContext());
		}
		return _buttonArea;
	}
	
	/**
	 * パンくずリストのボタン
	 * @return the _buttonList
	 */
	public ArrayList<LinearLayout> getButtonList() {
		if(_buttonList == null){
			_buttonList = new ArrayList<LinearLayout>();
		}
		return _buttonList;
	}

	/**
	 * リスナーの登録
	 * @param listner
	 */
	public void setOnClickListener(OnClickListener listner){
		_onClickListener = listner;
	}


	/**
	 * テキストカラー
	 * @param _textColor the _textColor to set
	 */
	public void setTextColor(int _textColor) {
		this._textColor = _textColor;
	}

	/**
	 * テキストカラー
	 * @return the _textColor
	 */
	public int getTextColor() {
		return _textColor;
	}

	/**
	 * ボタン背景のリソースID
	 * @param _buttonBackgroundResourceId the _buttonBackgroundResourceId to set
	 */
	public void setButtonBackgroundResourceId(int _buttonBackgroundResourceId) {
		this._buttonBackgroundResourceId = _buttonBackgroundResourceId;
	}

	/**
	 * ボタン背景のリソースID
	 * @return the _buttonBackgroundResourceId
	 */
	public int getButtonBackgroundResourceId() {
		return _buttonBackgroundResourceId;
	}

	/**
	 * セパレータのリソースID
	 * @param _separatorResourceId the _separatorResourceId to set
	 */
	public void setSeparatorResourceId(int _separatorResourceId) {
		this._separatorResourceId = _separatorResourceId;
	}

	/**
	 * セパレータのリソースID
	 * @return the _separatorResourceId
	 */
	public int getSeparatorResourceId() {
		return _separatorResourceId;
	}

	/**
	 * ボタンを押しても反応しないようにする
	 * @param _isUnResponse the _isUnResponse to set
	 */
	public void setIsUnResponse(boolean _isUnResponse) {
		this._isUnResponse = _isUnResponse;
	}

	/**
	 * ボタンを押しても反応しないようにする
	 * @return the _isUnResponse
	 */
	public boolean isUnResponse() {
		return _isUnResponse;
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param attrs
	 */
	public BreadCrumbList(Context context, AttributeSet attrs) {
		super(context, attrs);

//		//xmlパラメータの解析
//        TypedArray a = context.obtainStyledAttributes(attrs,
//                R.styleable.BreadCrumbList, 0, 0);
//
//        //セパレータのDrawableを取得
//        
//        Drawable d = a.getDrawable(R.styleable.BreadCrumbList_separater_src);
//        setSeparatorDrawable(d);
//		Log.d("mpremocon", "onCreate d="+ d + ",a.length="+a.length());
		
		//テキスト色
		setTextColor(getResources().getColor(color.background_dark));

		//セパレータを指定する
		setSeparatorResourceId(R.drawable.breadcrums_separator);
		
		//ボタンの配置用の一番親のレイアウトを追加する
		addView(getButtonArea());

		//上側の隙間調節
//		getButtonArea().setPadding(getButtonArea().getPaddingLeft()
//				, getHorizontalScrollbarHeight()
//				, getButtonArea().getPaddingRight()
//				, getHorizontalScrollbarHeight()//getButtonArea().getPaddingBottom()
//				);

	}

	/**
	 * 上下の隙間をスクロールバーのサイズで調節するか
	 * @param top
	 * @param bottom
	 */
	public void adjustPadding(boolean top, boolean bottom){
		int top_padding = 0;
		int bottom_padding = 0;
		if(top){
			top_padding = getHorizontalScrollbarHeight();
		}
		if(bottom){
			bottom_padding = getHorizontalScrollbarHeight();
		}
		getButtonArea().setPadding(getButtonArea().getPaddingLeft()
				, top_padding
				, getButtonArea().getPaddingRight()
				, bottom_padding
				);
	}
	
	/**
	 * レイアウトを確定させる
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);


		//一番右へ移動
		fullScroll(FOCUS_RIGHT);
	}
	
	/**
	 * アイテムを追加する
	 * @param name
	 */
	public void push(String name){
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
	}

	/**
	 * アイテムを１つ外す
	 */
	public LinearLayout pop(){
		int del_index = getButtonList().size() - 1;
		LinearLayout ret = getButtonList().get(del_index);
		//リストから削除
		getButtonList().remove(del_index);
		//レイアウトから削除
		getButtonArea().removeView(ret);
		
		return ret;
	}

	/**
	 * 含まれるアイテムの数
	 */
	public int size(){
		return getButtonList().size();
	}

	/**
	 * クリックイベント
	 */
	@Override
	public void onClick(View v) {
		if(_onClickListener == null){
			//リスナーが登録されてないときは勝手に減らさない
		}else if(isUnResponse()){
			//ロック中は反応しない
		}else{
			//押したボタンをリストの中から探す
			for(int i=0; i<getButtonList().size(); i++){
				if(getButtonList().get(i).findViewById(1).equals(v)){
					//見つかったらそれより後ろのボタンを消す
					while((i+1) < getButtonList().size()){
						pop();
					}
					
					//イベントを呼ぶ
					_onClickListener.onClick(v, i);
					break;
				}
			}
		}
	}
	
	/**
	 * クリックリスナー
	 * @author Iori
	 *
	 */
	public interface OnClickListener{
		void onClick(View v, int position);
	}
}
