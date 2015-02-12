package com.stealth.settings;

import android.content.Context;
import android.content.SharedPreferences;
import com.stealth.utils.Utils;

/**
 * Manages the general settings
 * Created by OlivierHokke on 25-Apr-14.
 */
public class GeneralSettingsManager {
	private static final String KEY_PREFS = "general";
	private static final String KEY_THUMBS = "showThumbs";
	private static final String KEY_DOUBLETAP_UNLOCK = "doubleTapUnlock";
	private static final String KEY_DOUBLETAP_LOCK = "doubleTapLock";

	private static boolean sInitialized;
	private static SharedPreferences sPrefs;

	private static boolean sShowThumbnails;
	private static boolean sDoubleTapUnlock;
	private static boolean sDoubleTapLock;

	private static void initialize() {
		if (!sInitialized) {
			sPrefs = Utils.getContext().getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE);
			sShowThumbnails = sPrefs.getBoolean(KEY_THUMBS, true);
			sDoubleTapUnlock = sPrefs.getBoolean(KEY_DOUBLETAP_UNLOCK, true);
			sDoubleTapLock = sPrefs.getBoolean(KEY_DOUBLETAP_LOCK, true);
			sInitialized = true;
		}
	}

	public static boolean isThumbnailsShown() {
		initialize();
		return sShowThumbnails;
	}

	public static void setShowThumbnails(boolean showThumbnails) {
		initialize();
		sShowThumbnails = showThumbnails;
		sPrefs.edit().putBoolean(KEY_THUMBS, sShowThumbnails).apply();
	}

	public static boolean isDoubleTapUnlock() {
		initialize();
		return sDoubleTapUnlock;
	}

	public static void setDoubleTapUnlock(boolean doubleTapUnlock) {
		initialize();
		sDoubleTapUnlock = doubleTapUnlock;
		sPrefs.edit().putBoolean(KEY_DOUBLETAP_UNLOCK, sDoubleTapUnlock).apply();
	}

	public static boolean isDoubleTapLock() {
		initialize();
		return sDoubleTapLock;
	}

	public static void setDoubleTapLock(boolean doubleTapLock) {
		initialize();
		sDoubleTapLock = doubleTapLock;
		sPrefs.edit().putBoolean(KEY_DOUBLETAP_LOCK, sDoubleTapLock).apply();
	}
}

