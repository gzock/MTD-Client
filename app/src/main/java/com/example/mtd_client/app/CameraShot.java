package com.example.mtd_client.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.koushikdutta.async.http.socketio.SocketIOClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;


public class CameraShot extends ActionBarActivity {

    // カメラインスタンス
    private Camera mCam = null;
    // カメラプレビュークラス
    private CameraPreview mCamPreview = null;
    // 画面タッチの2度押し禁止用フラグ
    private boolean mIsTake = false;
    private  String pictureTargetName = null;
    private static final String TAG = "CameraShot";
    public static final int CAMERA_SHOT = 0;
    public static final int NON_CAMERA_SHOT = 1;
    private ServiceManager sm = new ServiceManager();
    private String targetId = null;
    private String targetName = null;

    private boolean shotFlag = false;
    private SocketIOService socketio            = null;
    private SocketIOClient client = null;
    private              ServiceReceiver   receiver          =  new ServiceReceiver();
    private byte[] photoDataHolder = null;

    private Dialog dialog = null;
    private final int CAMERA_PARAM_SETTINGS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // ステータスバー非表示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // タイトルバー非表示
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_camera_shot);




        Bundle extras;
        extras = getIntent().getExtras();
        //値が設定されていない場合にはextrasにはnullが設定されます。
        if(extras != null) {
            //値が設定されている場合
            targetId = extras.getString("TargetID");
            targetName = extras.getString("TargetName");
         }



        //TODO: こっち側でreciverでupdate受け取って、json受け渡してfinish -> Mainでjsonを使ってlistview更新とかどう？
        //TODO: どうせ今までのほうだと更新無理だったんだし

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        switch(config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                // 横向きに固定
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;

            case Configuration.ORIENTATION_LANDSCAPE:

                //sm.bindWsService(CameraShot.this);
                Intent intent = new Intent(CameraShot.this, SocketIOService.class);
                IntentFilter filter = new IntentFilter(SocketIOService.ACTION);
                CameraShot.this.registerReceiver(receiver, filter);
                Boolean bool = CameraShot.this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                // カメラインスタンスの取得
                try {
                    mCam = Camera.open();
                    Log.d(TAG, "Camera Open");

                    /*
                    Camera.Parameters parameters = mCam.getParameters();
                    Log.d(TAG, parameters.getPictureSize().width + "x" + parameters.getPictureSize().height);
                    Log.d(TAG, parameters.getPreviewSize().width + "x" + parameters.getPreviewSize().height);

                    float defaultCameraRatio = (float) parameters.getPictureSize().width / (float) parameters.getPictureSize().height;
                    int PHOTO_HEIGHT_THRESHOLD = 1080;

                    List<Camera.Size> sizes = mCam.getParameters().getSupportedPictureSizes();
                    for(Camera.Size s : sizes) {
                        Log.d(TAG, s.width + "x" + s.height);
                    }
                    //parameters.setPreviewSize( 1920, 1080 );
                    //parameters.setPictureSize( 1920, 1080 );

                    for (Camera.Size s : sizes) {
                        float ratio = (float) s.width / (float) s.height;
                        if (ratio == defaultCameraRatio && s.height <= PHOTO_HEIGHT_THRESHOLD) {
                            parameters.setPictureSize( s.width, s.height );
                            parameters.setPreviewSize( s.width, s.height );
                            break;
                        }
                    }
                    //parameters.setPictureSize( sizes.get(4).width, sizes.get(4).height ); //プレビューサイズの2倍の大きさで画像を保存する
                    //parameters.setPictureSize(1920,1080);
                    //parameters.setPreviewSize(1280,720);
                    //mCam.setParameters(parameters);
                    */


                } catch (Exception e) {
                    // エラー
                    Log.d(TAG, "Camera Open Error");
                    this.finish();
                }
                // FrameLayout に CameraPreview クラスを設定
                FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
                mCamPreview = new CameraPreview(this, mCam);
                preview.addView(mCamPreview);
                break;
        }




        final ImageButton settingButton = (ImageButton)findViewById(R.id.settingButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialog( CAMERA_PARAM_SETTINGS );

            }
        });

        ImageButton reShotButton = (ImageButton)findViewById(R.id.reShotButton);
        reShotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(shotFlag) {
                    mCam.startPreview();
                    shotFlag = false;
                    photoDataHolder = null;
                } else {
                    Toast.makeText(CameraShot.this, "既に撮影可能状態です", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageButton submitButton = (ImageButton)findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsTake) {
                    if(shotFlag) {
                        Toast.makeText(CameraShot.this, "この写真を採用します", Toast.LENGTH_LONG).show();
                        // 撮影中の2度押し禁止用フラグ
                        mIsTake = true;
                        // 画像取得
                        //mCam.takePicture(null, null, mPicJpgListener);
                        submitPhoto( photoDataHolder );
                        shotFlag = false;
                    } else {
                        Toast.makeText(CameraShot.this, "未撮影状態です", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });



        if(mCamPreview != null) {
            // mCamPreview に タッチイベントを設定
            // マニュアルフォーカス
            // ちゃんとソース理解しよう
            // 感謝! -> http://stackoverflow.com/questions/17968132/set-the-camera-focus-area-in-android
            // http://techsketcher.blogspot.jp/2012/12/android40.html
            mCamPreview.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (mCam != null) {

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                Log.d(TAG, "Manual Focus Start");
                                float x = event.getX();
                                float y = event.getY();
                                float touchMajor = event.getTouchMajor();
                                float touchMinor = event.getTouchMinor();

                                Rect touchRect = new Rect((int) (x - touchMajor / 2), (int) (y - touchMinor / 2), (int) (x + touchMajor / 2), (int) (y + touchMinor / 2));
                                submitFocusAreaRect(touchRect);
                            } else {
                                Toast.makeText(CameraShot.this, "この端末はタッチフォーカスに対応していません", Toast.LENGTH_SHORT).show();
                                mCam.autoFocus(mAutoFocusListener);
                            }
                       }
                    }
                    return true;
                }
            });
        }




    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            socketio = ((SocketIOService.SocketIOBinder)service).getService();
            client = socketio.getSocketIOClinet();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            socketio = null;
        }
    };


    private void submitFocusAreaRect(final Rect touchRect)
    {
        Camera.Parameters cameraParameters = mCam.getParameters();

        if (cameraParameters.getMaxNumFocusAreas() == 0)
        {
            return;
        }

        // Convert from View's width and height to +/- 1000

        Rect focusArea = new Rect();

        focusArea.set(touchRect.left * 2000 / mCamPreview.getWidth() - 1000,
                touchRect.top * 2000 / mCamPreview.getHeight() - 1000,
                touchRect.right * 2000 / mCamPreview.getWidth() - 1000,
                touchRect.bottom * 2000 / mCamPreview.getHeight() - 1000);

        // Submit focus area to camera

        ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
        focusAreas.add(new Camera.Area(focusArea, 1000));

        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        cameraParameters.setFocusAreas(focusAreas);
        mCam.setParameters(cameraParameters);

        // Start the autofocus operation

        mCam.autoFocus(mAutoFocusListener);
    }

    /**
     * オートフォーカス完了のコールバック
     */
    private Camera.AutoFocusCallback mAutoFocusListener = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            if(success) {
                Log.d(TAG, "Manual Focus Success !!");
                mCam.takePicture(null, null, mPicJpgListener);
            }else {
                Log.d(TAG, "Manual Focus Failed...");
            }
        }
    };

    /**
     * JPEG データ生成完了時のコールバック
     */
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "Picture CallBack...");
            if (data == null) {
                return;
            } else {
                photoDataHolder = data;
                shotFlag = true;
            }

        };
    };

    private void submitPhoto(byte[] photoData) {
        Log.d(TAG, "*** Submit Photo ***");

        String checkSum = null;

        try {
            String KEY = "hogehoge";
            String ALGORISM = "hmacSHA256";
            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(), ALGORISM);

            Mac mac = Mac.getInstance(ALGORISM);
            mac.init(secretKeySpec);
            byte[] result = mac.doFinal( photoData ); // "hoge"が認証メッセージ
            checkSum = new String(Hex.encodeHex(result));
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        String base64Enc = Base64.encodeToString( photoData, Base64.DEFAULT);

        SendJobData sendJobData = new SendJobData();
        sendJobData.setTargetID( targetId );
        sendJobData.setData( base64Enc );
        sendJobData.setCheckSum( checkSum );
        sendJobData.setTargetName( targetName );

        socketio.addSendJob( sendJobData );
            /*
            JSONArray jArray = new JSONArray();
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("id", targetId);
                jObj.put("data", base64Enc);
                jObj.put("check_sum", checkSum);
                jArray.put(jObj);
                client.emit("addPhoto", jArray);

            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
            */
            /*
            // ターゲットID (24byte)を生成し、画像データの前にくっつける
            byte[] targetIdBytes   = targetId.getBytes();
            //byte[] targetNameBytes = targetName.getBytes();

            ByteBuffer byteBuf = ByteBuffer.allocate( targetIdBytes.length + data.length );
            byteBuf.put( targetIdBytes );
            //byteBuf.put( targetNameBytes );
            byteBuf.put( data );
            byte[] sendDataBytes = byteBuf.array();

            sm.send(sendDataBytes);
            sendDataBytes = null;
            byteBuf.clear();
            data = null;
            sm.unBindWsService();
            */
        mIsTake = false;
        CameraShot.this.setResult(CAMERA_SHOT);
        //mCam.release();
        CameraShot.this.unregisterReceiver( receiver );
        CameraShot.this.unbindService( serviceConnection );
        CameraShot.this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){

            // 戻るボタン押されたということは、画像の採用が行われていない
            CameraShot.this.unregisterReceiver( receiver );
            CameraShot.this.unbindService( serviceConnection );

            CameraShot.this.setResult(NON_CAMERA_SHOT);
            CameraShot.this.finish();

        }
        return false;
    }

    // Receiverクラス
    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {

        }

    }


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

    @Override
    protected void onDestroy() {

        //CameraShot.this.unregisterReceiver( receiver );
        //CameraShot.this.unbindService( serviceConnection );
        super.onDestroy();
    }

    // onStopでリリースとかしないとダメらしい
    // http://www.atmarkit.co.jp/ait/articles/1005/27/news097.html
    @Override
    protected void onStop() {
        if(mCam != null) {
            mCam.stopPreview();
            mCam.release();
        }
        super.onStop();
  }
    @Override
    public void onUserLeaveHint(){
        //ホームボタンが押された時や、他のアプリが起動した時に呼ばれる
        //戻るボタンが押された場合には呼ばれない
        Toast.makeText(getApplicationContext(), TAG + " Good bye!" , Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        dialog = super.onCreateDialog(id);
        client = socketio.getSocketIOClinet();


        //idは何個かダイアログがある場合に使う
        // ここはswitch使わないほうがいい
        if( id == CAMERA_PARAM_SETTINGS) {
            final List<Camera.Size> sizes = mCam.getParameters().getSupportedPictureSizes();
            ArrayList<String> strs = new ArrayList<String>();

            for(Camera.Size s : sizes) {
                Log.d(TAG, s.width + "x" + s.height);
                strs.add( s.width + "x" + s.height);
            }
            final CharSequence[] chars = strs.toArray(new CharSequence[strs.size()]);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( CameraShot.this );
            dialogBuilder.setTitle("撮影解像度選択");

            dialogBuilder.setSingleChoiceItems(
                    chars,
                    0, // Initial
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "変更前 PictureSize -> " + mCam.getParameters().getPictureSize().width + "x" + mCam.getParameters().getPictureSize().height);
                            Camera.Parameters parameters = mCam.getParameters();
                            parameters.setPictureSize( sizes.get(which).width, sizes.get(which).height );
                            mCam.setParameters(parameters);
                            Log.d(TAG, "変更後 PictureSize -> " + mCam.getParameters().getPictureSize().width + "x" + mCam.getParameters().getPictureSize().height);
                            dialog.dismiss();
                        }
                    }
            );
            dialogBuilder.setPositiveButton("OK", null);
            dialog = dialogBuilder.create();
        }
        return dialog;
    }

}
