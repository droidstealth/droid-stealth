package com.stealth.launch;

import android.content.Context;
import android.content.SharedPreferences;
import com.stealth.utils.Utils;

/**
 * Manages the launch settings for this application
 * Created by OlivierHokke on 14-Apr-14.
 */
public class LaunchManager {
	private static final String KEY_PREFS_LAUNCH = "launch";
	private static final String KEY_DIALER = "dialerEnabled";
	private static final String KEY_WIDGET = "widgetEnabled";
	private static final String KEY_ICON = "iconEnabled";

	private static boolean sInitialized;
	private static SharedPreferences sPrefs;

	private static boolean sIconDisabled;
	private static boolean sWidgetEnabled;
	private static boolean sDialerEnabled;

	private static void initialize() {
		if (!sInitialized) {
			sPrefs = Utils.getContext().getSharedPreferences(KEY_PREFS_LAUNCH, Context.MODE_PRIVATE);
			sIconDisabled = sPrefs.getBoolean(KEY_ICON, false);
			sWidgetEnabled = sPrefs.getBoolean(KEY_WIDGET, false);
			sDialerEnabled = sPrefs.getBoolean(KEY_DIALER, false);
			sInitialized = true;
		}
	}

	/**
	 * @return Is the application icon visible in the app drawer of the device?
	 */
	public static boolean isIconDisabled() {
		initialize();
		return sIconDisabled;
	}

	public static void setIconDisabled(boolean iconDisabled) {
		initialize();
		sIconDisabled = iconDisabled;
		sPrefs.edit().putBoolean(KEY_ICON, sIconDisabled).apply();
	}

	/**
	 * @return can the user make use of the widget to launch the application?
	 */
	public static boolean isWidgetEnabled() {
		initialize();
		return sWidgetEnabled;
	}

	public static void setWidgetEnabled(boolean widgetEnabled) {
		initialize();
		sWidgetEnabled = widgetEnabled;
		sPrefs.edit().putBoolean(KEY_WIDGET, sWidgetEnabled).apply();
	}

	/**
	 * @return can the user make use of the dialer to launch the application?
	 */
	public static boolean isDialerEnabled() {
		initialize();
		return sDialerEnabled;
	}

	public static void setDialerEnabled(boolean dialerEnabled) {
		initialize();
		sDialerEnabled = dialerEnabled;
		sPrefs.edit().putBoolean(KEY_DIALER, sDialerEnabled).apply();
	}
}
