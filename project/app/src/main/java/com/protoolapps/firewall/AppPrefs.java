package com.protoolapps.firewall;

import android.content.Context;
import android.content.SharedPreferences;


public class AppPrefs {
	
	private static final String AV_PREFERENCES = "protwall.preferences";

	private static final String SHARED_PREFS_NOTIFICATON_TIME = "SHARED_PREFS_NOTIFICATION_TIME";

	public static final boolean DEBUG_MODE = false;


	private static void saveNotificationShowTime(Context ctx) {
		SharedPreferences.Editor spe = ctx.getSharedPreferences(AV_PREFERENCES,
				Context.MODE_PRIVATE).edit();

		spe.putLong(SHARED_PREFS_NOTIFICATON_TIME, System.currentTimeMillis());
		spe.apply();
	}


	public static boolean isTimeToShowNotification(float capInSeconds, Context ctx) {

		long lastTime;
		SharedPreferences sp = ctx.getSharedPreferences(AV_PREFERENCES,
				Context.MODE_PRIVATE);
		lastTime = sp.getLong(SHARED_PREFS_NOTIFICATON_TIME, 0);
		//Check if it is the first time the user executes the app
		if (lastTime == 0) {
			//Saves the first time
			saveNotificationShowTime(ctx);
			return false;
		}
		float timeDiffInSeconds = (float)(System.currentTimeMillis() - lastTime) / 1000;

		if (timeDiffInSeconds > capInSeconds) {
			saveNotificationShowTime(ctx);
			return true;
		}
		return false;
	}


}
