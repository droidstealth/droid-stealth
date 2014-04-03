package spikes.stealthdialer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;

import com.stealth.android.HomeActivity;
import com.stealth.utils.Utils;

import pin.PinManager;

/**
 * Creates BroadcastReceiver that listens for Intent.ACTION_NEW_OUTGOING_CALL
 * When it receives such an intent it checks the number dialed to see if it starts with #555
 * If this is the case it intercepts the call and cuts it off. Before starting a specified activity.
 *
 * This needs to have priority 1 if it needs to intercept the actual call from being made.
 *
 * @author Joris Z. van den Oever
 * Created on 2/26/14.
 */
public class StealthDialReceiver extends BroadcastReceiver {
    public StealthDialReceiver() {
    }


    /**
     * On receiving an Intent to make a call. Check that number to see if it is a number that
     * activates the receiver. (Starts with #555) If it does then sends a startup intent to the
     * app launch activity.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.setContext(context);
        String pin = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if (HomeActivity.launch(context, pin)) {
	        setResultData(null);
        }
    }
}
