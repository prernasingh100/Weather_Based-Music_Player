package com.example.user.sdcard1;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_SCREEN_TIMEOUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        final ViewGroup Layout = (ViewGroup) findViewById(R.id.layout);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        Layout.setLayoutParams(param);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent h = new Intent(SplashScreen.this,MainActivity.class);
                startActivity(h);
                finish();
            }
        }, SPLASH_SCREEN_TIMEOUT);
    }
}
