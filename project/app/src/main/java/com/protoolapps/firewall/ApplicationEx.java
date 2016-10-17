package com.protoolapps.firewall;

/*
    This file is part of ProtWall.

    ProtWall is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ProtWall is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ProtWall.  If not, see <http://www.gnu.org/licenses/>.


*/

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class ApplicationEx extends Application {
    private static final String TAG = "ProtWall.App";

    private Thread.UncaughtExceptionHandler mPrevHandler;
    public InterstitialAd mInterstitialAd;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Create version=" + Util.getSelfVersionName(this) + "/" + Util.getSelfVersionCode(this));

        mPrevHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                if (mPrevHandler != null)
                    mPrevHandler.uncaughtException(thread, ex);
            }
        });

        //Initialize facebook sdk to track installs
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    public void createInterstitial(){
        mInterstitialAd = new InterstitialAd(this);
        Resources res = getResources();
        String intersId = res.getString(R.string.admob_interstitial_id);
        mInterstitialAd.setAdUnitId(intersId);
    }

    public void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getResources().getString(R.string.admob_test_device_id))
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    public boolean isAdLoaded(){
        if (mInterstitialAd.isLoaded()) {
            return true;
        }
        return false;
    }

    public void displayLoadedAd(){
        mInterstitialAd.show();
    }
}
