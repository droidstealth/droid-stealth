package com.stealth.launch;

import android.content.Context;
import android.content.SharedPreferences;
import com.stealth.utils.Utils;

/**
 * The manager that will manage the dialer launch settings.
 * Created by OlivierHokke on 14-Apr-14.
 */
public class DialerManager {
	private static final String KEY_PREFS_DIALER = "dialer";
	private static final String KEY_LAUNCH_CODE = "dialerLaunchCode";

	private static boolean sInitialized;
	private static SharedPreferences sPrefs;

	private static String sLaunchCode;

	private static void initialize() {
		if (!sInitialized) {
			sPrefs = Utils.getContext().getSharedPreferences(KEY_PREFS_DIALER, Context.MODE_PRIVATE);
			sLaunchCode = sPrefs.getString(KEY_LAUNCH_CODE, "##");
			sInitialized = true;
		}
	}

	/**
	 * @return get the code that will launch the application from the dialer
	 */
	public static String getLaunchCode() {
		initialize();
		return sLaunchCode;
	}

	public static void setLaunchCode(String launchCode) {
		initialize();
		sLaunchCode = launchCode;
		sPrefs.edit().putString(KEY_LAUNCH_CODE, sLaunchCode).apply();
	}
}
