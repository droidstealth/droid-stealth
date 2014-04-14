package com.stealth.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.stealth.launch.LaunchManager;
import com.stealth.launch.WidgetManager;
import com.stealth.utils.Utils;

import pin.PinActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StealthButton extends AppWidgetProvider {

	public static final String ACTION_BUTTON_PRESS = "buttonPress";
	private static final long TOUCH_INTERVAL = 1500;

	/**
	 * Update all the instances of this widget
	 * @param context using what context
	 */
	public static void updateMe(Context context) {
		Utils.d("Requesting widget update");

		Intent intent = new Intent(context, StealthButton.class);
		intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");

		int [] ids = getWidgetIDs(context);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		context.sendBroadcast(intent);
	}

	/**
	 * Gets the widget ids. Also keeps track of changes in widget count.
	 * If it increases, then they will show themselves.
	 * @param context using what context
	 * @return the widget ids
	 */
	private static int[] getWidgetIDs(Context context) {
		ComponentName name = new ComponentName(context, StealthButton.class);
		int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
		return ids;
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Utils.setContext(context);
		// just update all
		appWidgetIds = getWidgetIDs(context);
		final int N = appWidgetIds.length;
		for (int i=0; i<N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
		}
		WidgetManager.setWidgetIDs(appWidgetIds);
		Utils.d("widget ids: " + Utils.intArrayToString(appWidgetIds));
	}


	@Override
	public void onEnabled(Context context) {
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context) {
		// Enter relevant functionality for when the last widget is disabled
	}

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId) {
		Utils.setContext(context);

		Utils.d("Updating widget " + appWidgetId
				+ " and is widget visible? " + WidgetManager.isWidgetTemporarilyVisible());

		boolean found = false;
		for (int i : WidgetManager.getWidgetIDs()) {
			if (i == appWidgetId) {
				found = true;
				break;
			}
		}

		if (!found) {
			// NEW WIDGET!!
			WidgetManager.setWidgetTemporarilyVisible(true);
			updateMe(context);
		}

		// Construct the RemoteViews object
		RemoteViews views;
		if (WidgetManager.isWidgetTemporarilyVisible()) {
			views = new RemoteViews(context.getPackageName(), R.layout.stealth_button_visible);
		} else {
			views = new RemoteViews(context.getPackageName(), R.layout.stealth_button);
		}
		Intent intent = new Intent(context, StealthButton.class);
		intent.setAction(ACTION_BUTTON_PRESS);
		views.setOnClickPendingIntent(R.id.stealth_image, PendingIntent.getBroadcast(context, 0, intent, 0));

		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	private static long mLastTime = 0;
	private static int mClicks = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Utils.setContext(context);

		if (!LaunchManager.isWidgetEnabled()) return;

		if (intent.getAction() != null && intent.getAction().equals(ACTION_BUTTON_PRESS)) {

			if (WidgetManager.isWidgetTemporarilyVisible()) {
				// hide the widget again after tap
				WidgetManager.setWidgetTemporarilyVisible(false);
				updateMe(context);
			}

			long now = System.currentTimeMillis();
			long diff = now - mLastTime;

			if (diff < TOUCH_INTERVAL) {
				mClicks++;
			} else {
				mClicks = 0;
			}

			mLastTime = now;

			if (WidgetManager.getTapCountToOpen() == mClicks) {
				PinActivity.launch(context);
				mClicks = 0;
			}
		}
	}
}


