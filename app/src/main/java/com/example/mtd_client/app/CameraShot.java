package com.example.mtd_client.app;

import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class CameraShot extends ActionBarActivity {

    // カメラインスタンス
    private Camera mCam = null;
    // カメラプレビュークラス
    private CameraPreview mCamPreview = null;
    // 画面タッチの2度押し禁止用フラグ
    private boolean mIsTake = false;
    private  String pictureTargetName = null;
    private static final String TAG = "CameraShot";
    private ServiceManager sm = new ServiceManager();
    private String targetId = null;
    private String targetName = null;
    private String projectName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_shot);

        sm.bindWsService(CameraShot.this);

        Bundle extras;
        extras = getIntent().getExtras();
        //値が設定されていない場合にはextrasにはnullが設定されます。
        if(extras != null) {
            //値が設定されている場合
            targetId = extras.getString("TargetID");
            targetName = extras.getString("TargetName");
            projectName = extras.getString("projectName");
         }

        // カメラインスタンスの取得
        try {
            mCam = Camera.open();
            Log.d(TAG, "Camera Open");
        } catch (Exception e) {
            // エラー
            Log.d(TAG, "Camera Open Error");
            this.finish();
        }

        // FrameLayout に CameraPreview クラスを設定
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        mCamPreview = new CameraPreview(this, mCam);
        preview.addView(mCamPreview);

        // mCamPreview に タッチイベントを設定
        mCamPreview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!mIsTake) {
                        // 撮影中の2度押し禁止用フラグ
                        mIsTake = true;
                        // 画像取得
                        mCam.takePicture(null, null, mPicJpgListener);

                    }
                }
                return true;
            }
        });

    }

    /**
     * JPEG データ生成完了時のコールバック
     */
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }

            // ターゲットID (24byte)を生成し、画像データの前にくっつける
            byte[] targetIdBytes   = targetId.getBytes();
            //byte[] targetNameBytes = targetName.getBytes();

            ByteBuffer byteBuf = ByteBuffer.allocate( targetIdBytes.length + data.length );
            byteBuf.put( targetIdBytes );
            //byteBuf.put( targetNameBytes );
            byteBuf.put( data );
            byte[] sendDataBytes = byteBuf.array();

            sm.send(sendDataBytes);
            /*
            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/test";
            // SD カードフォルダを取得
            File file = new File(saveDir);
            // フォルダ作成
            if (!file.exists()) {
                if (!file.mkdir()) {
                    Log.e("Debug", "Make Dir Error");
                }
            }
            // 画像保存パス
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            //String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";
            String imgPath = saveDir + "/" + pictureTargetName + ".jpg";
            // ファイル保存
            FileOutputStream fos;
            try {
                //第二引数をfalseにすると上書き許可
                fos = new FileOutputStream(imgPath, false);
                fos.write(data);
                fos.close();
                // アンドロイドのデータベースへ登録
                // (登録しないとギャラリーなどにすぐに反映されないため)
                //registAndroidDB(imgPath);
            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
            }
            */
            mIsTake = false;
        };
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera_shot, menu);
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

}