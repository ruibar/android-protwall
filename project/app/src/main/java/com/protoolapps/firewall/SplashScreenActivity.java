package com.protoolapps.firewall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;


public class SplashScreenActivity extends AppCompatActivity {

    private ApplicationEx applicationInst;
    private int CHECK_ADLOADED_INTERVAL = 500;
    private int MAX_SPLASH_INTERVAL = 8000;
    private Handler mHandler;
    private boolean isAdDisplayed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        applicationInst = (ApplicationEx) getApplication();
        applicationInst.createInterstitial();
        applicationInst.requestNewInterstitial();
        isAdDisplayed = false;

        delayedGoToNextScreen();

        mHandler = new Handler();
        startRepeatingTask();
    }


    private void delayedGoToNextScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isAdDisplayed){
                     nextScreen();
                }
            }
        }, MAX_SPLASH_INTERVAL);
    }

    private void nextScreen() {
        stopRepeatingTask();
        Intent intent = new Intent(SplashScreenActivity.this,
                ActivityFirst.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    Runnable mAdStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                checkAdLoaded();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mAdStatusChecker, CHECK_ADLOADED_INTERVAL);
            }
        }
    };

    private void startRepeatingTask() {
        mAdStatusChecker.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mAdStatusChecker);
    }

    private void checkAdLoaded(){
        if(applicationInst.isAdLoaded()){
            applicationInst.displayLoadedAd();
            isAdDisplayed = true;
            applicationInst.mInterstitialAd.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    applicationInst.requestNewInterstitial();
                    nextScreen();
                }
            });
        }
    }


}