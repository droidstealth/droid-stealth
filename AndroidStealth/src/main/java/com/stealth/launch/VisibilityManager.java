package com.stealth.launch;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.stealth.android.HomeActivity;
import com.stealth.android.StealthButton;

/**
 * This class manages the hidden/visible state of the application Created by OlivierHokke on 06-Apr-14.
 */
public class VisibilityManager {

	/**
	 * This method hides the application from the application drawer of the user. But only if the settings demand it, as
	 * the user can decide if the icon should be hidden in the application drawer. Also prevents app from being launched
	 * using intents
	 *
	 * @param context the context to use for this operation
	 */
	public static void hideApplication(Context context) {
		if (LaunchManager.isIconDisabled()) {
			hideClass(context, HomeActivity.class, PackageManager.DONT_KILL_APP);
		}
	}

	/**
	 * This method makes the application visible in the application drawer of the user. Also makes this application
	 * launchable with an intent.
	 *
	 * @param context the context to use for this operation
	 */
	public static void showApplication(Context context) {
		showClass(context, HomeActivity.class, PackageManager.DONT_KILL_APP);
	}

	/**
	 * Hides the widget
	 *
	 * @param context the context to use for this operation
	 */
	public static void hideWidget(Context context) {
		hideClass(context, StealthButton.class, PackageManager.DONT_KILL_APP);
	}

	/**
	 * Shows the widget
	 *
	 * @param context the context to use for this operation
	 */
	public static void showWidget(Context context) {
		showClass(context, StealthButton.class, PackageManager.DONT_KILL_APP);
	}

	/**
	 * This method hides the given class
	 *
	 * @param context the context to use for this operation
	 * @param toHide  the class to hide
	 */
	public static void hideClass(Context context, Class<?> toHide, int flag) {
		PackageManager pm = context.getPackageManager();
		ComponentName homeName = new ComponentName(context, toHide);
		if (pm != null
				&& pm.getComponentEnabledSetting(homeName) == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
			pm.setComponentEnabledSetting(homeName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, flag);
		}
	}


	/**
	 * This method makes the class visible
	 *
	 * @param context the context to use for this operation
	 * @param toShow  the class to show
	 */
	public static void showClass(Context context, Class<?> toShow, int flag) {
		PackageManager pm = context.getPackageManager();
		ComponentName homeName = new ComponentName(context, toShow);
		if (pm != null) {
			// make sure activity can be called
			pm.setComponentEnabledSetting(
					homeName,
					PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
					flag);
		}
	}
}
