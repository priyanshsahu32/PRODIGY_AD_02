package com.pcsahu.todoapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {
    ImageView logoImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_splash );
        logoImageView   = findViewById( R.id.splash_logo );

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getWindow().setFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        }

        Thread thread = new Thread(){
            public void run(){
                try {


                    ScaleAnimation animation = new ScaleAnimation(
                            1f, 1.5f, // Start and end scale X
                            1f, 1.5f, // Start and end scale Y
                            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point X (center)
                            Animation.RELATIVE_TO_SELF, 0.5f  // Pivot point Y (center)
                    );

                    animation.setRepeatCount(Animation.INFINITE); // Infinite animation
                    animation.setRepeatMode(Animation.REVERSE); // Reverse the animation to decrease size

                    animation.setDuration(1000); // Duration in milliseconds
                    logoImageView.startAnimation(animation);
                    sleep(2500);

                    startActivity( new Intent(SplashActivity.this,loginActivity.class) );
                    finish();
                }catch (Exception e){
                    Toast.makeText( SplashActivity.this, "ERROR", Toast.LENGTH_SHORT ).show();
                }
            }
        };

        thread.start();
    }
}