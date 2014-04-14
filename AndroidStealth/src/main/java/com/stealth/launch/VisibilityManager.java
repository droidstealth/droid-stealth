package com.stealth.launch;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.stealth.android.HomeActivity;

/**
 * This class manages the hidden/visible state of the application
 * Created by OlivierHokke on 06-Apr-14.
 */
public class VisibilityManager {

	/**
	 * This method hides the application from the application drawer of the user.
	 * But only if the settings demand it, as the user can decide if the icon should be hidden
	 * in the application drawer.
	 * Also prevents app from being launched using intents
	 * @param context the context to use for this operation
	 */
	public static void hideApplication(Context context) {
		if (LaunchManager.isIconDisabled()) {
			PackageManager pm = context.getPackageManager();
			ComponentName homeName = new ComponentName(context, HomeActivity.class);
			if (pm != null
					&& pm.getComponentEnabledSetting(homeName) == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
				Log.w("Hiding: Disable", "Disabling app drawer icon.");
				pm.setComponentEnabledSetting(homeName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
			}
		}
	}

	/**
	 * This method makes the application visible in the application drawer of the user.
	 * Also makes this application launchable with an intent.
	 * @param context the context to use for this operation
	 */
	public static void showApplication(Context context) {
		PackageManager pm = context.getPackageManager();
		ComponentName homeName = new ComponentName(context, HomeActivity.class);
		if (pm != null) {
			// make sure activity can be called
			pm.setComponentEnabledSetting(
					homeName,
					PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
					PackageManager.DONT_KILL_APP);
		}
	}
}
