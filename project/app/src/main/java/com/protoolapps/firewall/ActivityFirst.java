package com.protoolapps.firewall;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import net.colindodd.toggleimagebutton.ToggleImageButton;

import bolts.AppLinks;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ActivityFirst extends AppCompatActivity {


    private ApplicationEx admobApp;
    private boolean isAdActive = false;
    private AlertDialog dialogVpn = null;
    private AlertDialog dialogDoze = null;
    private SharedPreferences prefs;
    private static final String TAG = "ProtWall.First";
    private Button btnAccess;
    private Button btnLogs;
    ToggleImageButton btnFwEnabled;
    Animation zoomInOut;
    TextView tVFwStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        //Handle facebook deeplinking
        FacebookSdk.sdkInitialize(getApplicationContext());
        Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
        if (targetUrl != null) {
            Log.i("Activity", "App Link Target URL: " + targetUrl.toString());
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        admobApp = (ApplicationEx)getApplication();
        admobApp.createInterstitial();
        admobApp.requestNewInterstitial();

        initBannerAds();

        initButtons();

        isAdActive = false;

        setDefaultUnusedPreferences();

        startRepeatingNotification();

    }


    private void initButtons() {

        boolean enabled = prefs.getBoolean("enabled", false);

        btnAccess = (Button) findViewById(R.id.btnAccesscontrol);
        btnLogs = (Button) findViewById(R.id.btnAccessLogs);
        btnFwEnabled = (ToggleImageButton) findViewById(R.id.tib_fwEnable);
        tVFwStatus = (TextView) findViewById(R.id.txt_firewall_status);

        // animation
        zoomInOut = AnimationUtils.loadAnimation(this, R.anim.scale_up_down);


        // Access control button
        if(enabled){
            activateAccessButton();
        }  else {
            deactivateAccessButton();
        }

        btnAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextScreen();
            }
        });

        btnLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent nextScreen = new Intent(ActivityFirst.this, ActivityLog.class);
                startActivity(nextScreen);
            }
        });


        // On/off switch
        btnFwEnabled.setChecked(enabled);
        btnFwEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Switch=" + isChecked);
                prefs.edit().putBoolean("enabled", isChecked).apply();

                if (isChecked) {
                    activateVpn();
                    activateAccessButton();
                } else {
                    ServiceSinkhole.stop("switch off", ActivityFirst.this);
                    deactivateAccessButton();
                }

            }
        });
        btnFwEnabled.startAnimation(zoomInOut);

    }

    private void activateAccessButton(){
        btnAccess.setVisibility(View.VISIBLE);
        btnLogs.setVisibility(View.VISIBLE);
        tVFwStatus.setText(getString(R.string.firewall_status) + " " + getString(R.string.on));
        //btnFwEnabled.clearAnimation();
    }

    private void deactivateAccessButton(){
        btnAccess.setVisibility(View.GONE);
        btnLogs.setVisibility(View.GONE);
        tVFwStatus.setText(getString(R.string.firewall_status) + " " + getString(R.string.off));
        //btnFwEnabled.startAnimation(zoomInOut);
    }


    private void nextScreen() {

        final Intent nextScreen = new Intent(ActivityFirst.this, ActivityMain.class);

        if(admobApp.isAdLoaded() && !isAdActive){
            admobApp.displayLoadedAd();
            admobApp.mInterstitialAd.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    admobApp.requestNewInterstitial();
                    startActivity(nextScreen);
                }
            });
        }else{
            startActivity(nextScreen);
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.i(TAG, "onActivityResult request=" + requestCode + " result=" + requestCode + " ok=" + (resultCode == RESULT_OK));
        Util.logExtras(data);

        if (requestCode == ActivityMain.REQUEST_VPN) {
            // Handle VPN approval
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();
            if (resultCode == RESULT_OK) {
                ServiceSinkhole.start("prepared", this);
                //Check battery otimizations on Android M and superior versions
                checkDoze();
            }


        }
        else {
            Log.w(TAG, "Unknown activity result request=" + requestCode);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void activateVpn() {
        try {
            final Intent prepare = VpnService.prepare(ActivityFirst.this);
            if (prepare == null) {
                Log.i(TAG, "Prepare done");
                onActivityResult(ActivityMain.REQUEST_VPN, RESULT_OK, null);
            } else {
                // Show dialog
                LayoutInflater inflater = LayoutInflater.from(ActivityFirst.this);
                View view = inflater.inflate(R.layout.vpn, null, false);
                dialogVpn = new AlertDialog.Builder(ActivityFirst.this)
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Log.i(TAG, "Start intent=" + prepare);
                                try {
                                    // com.android.vpndialogs.ConfirmDialog required
                                    startActivityForResult(prepare, ActivityMain.REQUEST_VPN);
                                } catch (Throwable ex) {
                                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                    onActivityResult(ActivityMain.REQUEST_VPN, RESULT_CANCELED, null);
                                    prefs.edit().putBoolean("enabled", false).apply();
                                }
                            }

                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                dialogVpn = null;
                            }
                        })
                        .create();
                dialogVpn.show();
            }
        } catch (Throwable ex) {
            // Prepare failed
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            prefs.edit().putBoolean("enabled", false).apply();
        }

    }


    private void checkDoze() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            final Intent doze = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            if (Util.batteryOptimizing(this) && getPackageManager().resolveActivity(doze, 0) != null) {
                    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    if (!prefs.getBoolean("nodoze", false)) {
                        LayoutInflater inflater = LayoutInflater.from(this);
                        View view = inflater.inflate(R.layout.doze, null, false);
                        final CheckBox cbDontAsk = (CheckBox) view.findViewById(R.id.cbDontAsk);
                        dialogDoze = new AlertDialog.Builder(this)
                                .setView(view)
                                .setCancelable(true)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        prefs.edit().putBoolean("nodoze", cbDontAsk.isChecked()).apply();
                                        startActivity(doze);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        prefs.edit().putBoolean("nodoze", cbDontAsk.isChecked()).apply();
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        dialogDoze = null;
                                        checkDataSaving();
                                    }
                                })
                                .create();
                        dialogDoze.show();
                    } else {
                        checkDataSaving();
                    }

                } else {
                checkDataSaving();
                }
            }

        else {
            showDelayDialog();
        }

    }

    private void checkDataSaving() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final Intent settings = new Intent(
                    Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            if (Util.dataSaving(this) && getPackageManager().resolveActivity(settings, 0) != null) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (!prefs.getBoolean("nodata", false)) {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.datasaving, null, false);
                    final CheckBox cbDontAsk = (CheckBox) view.findViewById(R.id.cbDontAsk);
                    dialogDoze = new AlertDialog.Builder(this)
                            .setView(view)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prefs.edit().putBoolean("nodata", cbDontAsk.isChecked()).apply();
                                    startActivity(settings);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prefs.edit().putBoolean("nodata", cbDontAsk.isChecked()).apply();
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    dialogDoze = null;
                                    showDelayDialog();
                                }
                            })
                            .create();
                    dialogDoze.show();
                }
                else {
                    showDelayDialog();
                }
            }
            else {
                showDelayDialog();
            }
        }
        else {
            showDelayDialog();
        }
    }

    private void showDelayDialog(){

        Resources res = getResources();

        final SweetAlertDialog pDialog = new SweetAlertDialog(ActivityFirst.this, SweetAlertDialog.PROGRESS_TYPE);

        Window window = pDialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(res.getString(R.string.activating_firewall));
        pDialog.setContentText(res.getString(R.string.please_wait));
        pDialog.setCancelable(false);

        pDialog.show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //pDialog.dismissWithAnimation();
//                pDialog.setTitleText("Sucess!")
//                        .setContentText("Your firewall is activated!")
//                        .setConfirmText("OK")
//                        .setConfirmClickListener(null)
//                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                activateAccessButton();

//                final Intent nextScreen = new Intent(ActivityFirst.this, ActivityCongrats.class);
//                startActivity(nextScreen);

                showCongratsDialog();

                pDialog.dismissWithAnimation();

            }

        }, 7000);


    }

    private void showCongratsDialog(){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("nocongrats", false)) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.congratsdialog, null, false);
            final CheckBox cbDontCongrats = (CheckBox) view.findViewById(R.id.cbDontCongrats);
            dialogDoze = new AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putBoolean("nocongrats", cbDontCongrats.isChecked()).apply();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            dialogDoze = null;
                        }
                    })
                    .create();
            dialogDoze.show();
        }

    }

    private void initBannerAds() {
        // Look up the AdView as a resource and load a request.
        NativeExpressAdView adView = (NativeExpressAdView)this.findViewById(R.id.main_adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("BABB35E2F44BD42D7B74D736D34239F2")
                .build();
        adView.loadAd(adRequest);

        //Mechanism to prevent interstitial displaying when banner is active
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                isAdActive = false;
            }
            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                isAdActive = true;
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                isAdActive = true;
            }

        });
    }

    private void setDefaultUnusedPreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("screen_wifi", false);
        editor.putBoolean("screen_other", false);
        editor.apply();
    }

    private void startRepeatingNotification() {
        Context ctx = ActivityFirst.this;

        Intent myIntent = new Intent(ctx, RepeatingNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, myIntent, 0);

        //Alarm to start a service every x hours
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long notifyTimeInterval = 1000 * 60 * 60 * 24; //1000*60*60*24 = 24 h

        if (AppPrefs.DEBUG_MODE) {
            notifyTimeInterval = 1000 * 60 * 1; // = 1 minute
        }

        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                notifyTimeInterval, pendingIntent);

        if (AppPrefs.DEBUG_MODE)
            Log.d("ALARM", "Setted repeating alarm ms: " + notifyTimeInterval);

    }


}
