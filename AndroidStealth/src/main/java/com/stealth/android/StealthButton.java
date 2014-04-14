package com.stealth.android;

import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;

import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.stealth.utils.Utils;
import pin.PinActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StealthButton extends AppWidgetProvider {

	public static final String ACTION_BUTTON_PRESS = "buttonPress";
	private static final long TOUCH_INTERVAL = 500;
	private static final int CLICKS_TO_LAUNCH = 4;
	private Camera mCamera;
	private boolean mLightOn;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
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
			}
			else {
				mClicks = 0;
				obtainCamera();
				if (mCamera != null && mCamera.getParameters().getSupportedFlashModes().contains(FLASH_MODE_TORCH)) {
					mLightOn = FLASH_MODE_TORCH.equals(mCamera.getParameters().getFlashMode());
					toggleLight();
				}
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

	private void toggleLight() {
		if (mLightOn) {
			mLightOn = turnLightOff();
			if (mLightOn) {
				Toast.makeText(Utils.getContext(), "Failed to switch off flash", Toast.LENGTH_LONG).show();
			}
		}
		else {
			mLightOn = turnLightOn();
			if (!mLightOn) {
				Toast.makeText(Utils.getContext(), "Failed to switch on flash", Toast.LENGTH_LONG).show();
			}
		}
	}

	private boolean turnLightOn() {
		if (mCamera == null) {
			return false;
		}

		Camera.Parameters parameters = mCamera.getParameters();
		List<String> flashmodes = parameters.getSupportedFlashModes();
		if (flashmodes != null) {
			if (flashmodes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
				parameters.setFlashMode(FLASH_MODE_TORCH);
				mCamera.setParameters(parameters);
			}
			else {
				Toast.makeText(Utils.getContext(), "FLASH_MODE_TORCH not supported", Toast.LENGTH_LONG).show();
			}
		}
		return FLASH_MODE_TORCH.equals(mCamera.getParameters().getFlashMode());
	}

	private boolean turnLightOff() {
		if (mCamera == null) {
			return false;
		}

		Camera.Parameters parameters = mCamera.getParameters();
		List<String> flashModes = parameters.getSupportedFlashModes();
		String currentFlashmode = parameters.getFlashMode();

		if (!FLASH_MODE_OFF.equals(currentFlashmode)) {
			parameters.setFlashMode(FLASH_MODE_OFF);
			mCamera.setParameters(parameters);
		}

		return FLASH_MODE_OFF.equals(mCamera.getParameters().getFlashMode());
	}

	private void obtainCamera() {
		if (mCamera == null) {
			try {
				mCamera = Camera.open();
			}
			catch (RuntimeException e) {
				Toast.makeText(Utils.getContext(), "Camera.open() failed: " + e.toString(), Toast.LENGTH_LONG).show();
			}
		}
	}
}


