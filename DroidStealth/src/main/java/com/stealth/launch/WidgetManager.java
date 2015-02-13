package com.stealth.launch;

import android.content.Context;
import android.content.SharedPreferences;
import com.stealth.utils.Utils;

/**
 * The manager that will manage the widget launch settings.
 * Created by OlivierHokke on 14-Apr-14.
 */
public class WidgetManager {
	private static final String KEY_PREFS_WIDGET = "invisibleWidget";
	private static final String KEY_VISIBILITY = "widgetTemporarilyVisible";
	private static final String KEY_TAPS = "widgetTaps";
	private static final String KEY_IDS = "widgetIDS";

	private static boolean sInitialized;
	private static SharedPreferences sPrefs;

	private static boolean sWidgetTemporarilyVisible;
	private static int sTapCountToOpen;
	private static int[] sWidgetIDs;

	private static void initialize() {
		if (!sInitialized) {
			sPrefs = Utils.getContext().getSharedPreferences(KEY_PREFS_WIDGET, Context.MODE_PRIVATE);
			sWidgetTemporarilyVisible = sPrefs.getBoolean(KEY_VISIBILITY, false);
			sTapCountToOpen = sPrefs.getInt(KEY_TAPS, 4);
			sWidgetIDs = Utils.intArrayFromString(sPrefs.getString(KEY_IDS, ""));
			sInitialized = true;
		}
	}

	/**
	 * @return is the widget temporarily visible to the user? will be disabled after widget press
	 */
	public static boolean isWidgetTemporarilyVisible() {
		initialize();
		return sWidgetTemporarilyVisible;
	}

	public static void setWidgetTemporarilyVisible(boolean widgetTemporarilyVisible) {
		initialize();
		sWidgetTemporarilyVisible = widgetTemporarilyVisible;
		sPrefs.edit().putBoolean(KEY_VISIBILITY, sWidgetTemporarilyVisible).apply();
	}

	/**
	 * @return how many widget the user has (afawk)
	 */
	public static int[] getWidgetIDs() {
		return sWidgetIDs;
	}

	/**
	 * @param widgetIDs set the amount of widgets the user has
	 */
	public static void setWidgetIDs(int[] widgetIDs) {
		initialize();
		sWidgetIDs = widgetIDs;
		sPrefs.edit().putString(KEY_IDS, Utils.intArrayToString(sWidgetIDs)).apply();
	}

	/**
	 * @return get the amount of taps the user must do before the app opens
	 */
	public static int getTapCountToOpen() {
		initialize();
		return sTapCountToOpen;
	}

	public static void setTapCountToOpen(int tapCountToOpen) {
		initialize();
		sTapCountToOpen = tapCountToOpen;
		sPrefs.edit().putInt(KEY_TAPS, sTapCountToOpen).apply();
	}
}
