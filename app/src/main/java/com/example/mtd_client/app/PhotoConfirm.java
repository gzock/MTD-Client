package com.example.mtd_client.app;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;


public class PhotoConfirm extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ステータスバー非表示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // タイトルバー非表示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera_shot);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        switch(config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                // 横向きに固定
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;

            case Configuration.ORIENTATION_LANDSCAPE:

                setContentView(R.layout.activity_photo_confirm);

                String encPhotoData = null;
                /*
                Bundle extras;
                extras = getIntent().getExtras();
                //値が設定されていない場合にはextrasにはnullが設定されます。
                if (extras != null) {
                    //値が設定されている場合
                    encPhotoData = extras.getString("encPhotoData");
                }
                */
                PassToOtherActivity pass = (PassToOtherActivity)PhotoConfirm.this.getApplication();
                encPhotoData = (String)pass.getObj();

                byte[] decPhotoData = Base64.decode(encPhotoData, Base64.DEFAULT);
                Bitmap photoData = BitmapFactory.decodeByteArray(decPhotoData, 0, decPhotoData.length);
                ((ImageView) findViewById(R.id.photoConfirmView)).setImageBitmap(photoData);
                ((ImageView) findViewById(R.id.photoConfirmView)).setScaleType(ImageView.ScaleType.FIT_XY);

                pass.clearObj();
                break;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo_confirm, menu);
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
