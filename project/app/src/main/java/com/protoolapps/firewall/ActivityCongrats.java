package com.protoolapps.firewall;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;

public class ActivityCongrats extends AppCompatActivity {


    private ApplicationEx admobApp;
    private boolean isAdActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congrats);

        admobApp = (ApplicationEx)getApplication();
        admobApp.requestNewInterstitial();

        initButtons();

        isAdActive = false;

    }


    private void initButtons() {

        Button btnProceed = (Button) findViewById(R.id.btnProceedControl);

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextScreen();
            }
        });

        TextView tVCongrats = (TextView) findViewById(R.id.txt_congrats);
        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        tVCongrats.startAnimation(zoomIn);
    }


    private void nextScreen() {

        final Intent nextScreen = new Intent(ActivityCongrats.this, ActivityMain.class);

        if(admobApp.isAdLoaded() && !isAdActive){
            admobApp.displayLoadedAd();
            isAdActive = true;
            admobApp.mInterstitialAd.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    admobApp.requestNewInterstitial();
                    startActivity(nextScreen);
                    isAdActive = false;
                }
            });
        }else{
            startActivity(nextScreen);
        }

        finish();
    }


}
