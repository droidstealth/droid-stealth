package com.stealth.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.stealth.utils.Utils;

import pin.PinActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StealthButton extends AppWidgetProvider {

    public static final String ACTION_BUTTON_PRESS = "buttonPress";
    private static final long TOUCH_INTERVAL = 1500;
    private static final int CLICKS_TO_LAUNCH = 4;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
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

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stealth_button);
        Intent intent = new Intent(context, StealthButton.class);
        intent.setAction(ACTION_BUTTON_PRESS);
        views.setOnClickPendingIntent(R.id.toggleButton, PendingIntent.getBroadcast(context, 0, intent, 0));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static long mLastTime = 0;
    private static int mClicks = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction() != null && intent.getAction().equals(ACTION_BUTTON_PRESS)) {
            long now = System.currentTimeMillis();
            long diff = now - mLastTime;

            if (diff < TOUCH_INTERVAL) {
                mClicks++;
            } else {
                mClicks = 0;
            }

            mLastTime = now;

            if (CLICKS_TO_LAUNCH == mClicks) {
                Intent pinIntent = new Intent(context, PinActivity.class);
                pinIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(pinIntent);
                mClicks = 0;
            }
        }
    }
}


