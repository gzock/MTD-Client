package com.example.mtd_client.app;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Gzock on 2014/05/05.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCam;
    public CameraPreview(Context context, Camera cam) {
        super(context);
        mCam = cam;
        // サーフェスホルダーの取得とコールバック通知先の設定
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    /**
     * SurfaceView 生成
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Camera.Parameters parameters = mCam.getParameters();
            Log.d("CameraPreview", "PictureSize -> " + mCam.getParameters().getPictureSize().width + "x" + mCam.getParameters().getPictureSize().height);
            Log.d("CameraPreview", "PreviewSize -> " + mCam.getParameters().getPreviewSize().width + "x" + mCam.getParameters().getPreviewSize().height);

            //TODO: 解像度選択画面は作らないとね
            /*
            parameters.setPictureSize(1920,1080);
            parameters.setPreviewSize(1280,720);
            mCam.setParameters(parameters);
            */
            // カメラインスタンスに、画像表示先を設定
            mCam.setPreviewDisplay(holder);
            // プレビュー開始
            mCam.startPreview();
        } catch (IOException e) {
            Log.d("CameraPreview", e.toString());
        }
    }


    /**
     * SurfaceView 破棄
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
    }


    /**
     * SurfaceHolder が変化したときのイベント
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 画面回転に対応する場合は、ここでプレビューを停止し、
        // 回転による処理を実施、再度プレビューを開始する。
    }



}
