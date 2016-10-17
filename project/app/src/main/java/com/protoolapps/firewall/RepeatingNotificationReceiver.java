package com.protoolapps.firewall;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;



/* Start service on alarm manager event */
public class RepeatingNotificationReceiver extends BroadcastReceiver {

	private int NOTIF_ID = 138;

	@Override
	public void onReceive(Context context, Intent intent) {

        float nextTimeToShowInSeconds = (float) (3 * 24 * 60 * 60);//3 days
        long when = System.currentTimeMillis();

        if(AppPrefs.DEBUG_MODE)
            nextTimeToShowInSeconds = 55f;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firewallEnabled = prefs.getBoolean("enabled", false);

        //Conditions to show scan notification
        boolean isTimeElapsedToShow = AppPrefs.isTimeToShowNotification(
                nextTimeToShowInSeconds, context);

        //Show after 3 days and if firewall is disabled
        if (isTimeElapsedToShow && !firewallEnabled) {
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            Intent notificationIntent = new Intent(context, ActivityFirst.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Resources res = context.getResources();
            NotificationCompat.Builder mNotifyBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(
                    context).setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(res.getString(R.string.notifscan_title))
                    .setContentText(res.getString(R.string.notifscan_message))
                    .setSound(alarmSound)
                    .setAutoCancel(true).setWhen(when)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(NOTIF_ID, mNotifyBuilder.build());
            NOTIF_ID++;
        }


	} 
}
