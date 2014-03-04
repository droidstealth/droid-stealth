package spikes.stealthdialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stealth.android.HomeActivity;

/**
 * Creates BroadcastReceiver that listens for Intent.ACTION_NEW_OUTGOING_CALL
 * When it receives such an intent it checks the number dialed to see if it starts with #555
 * If this is the case it intercepts the call and cuts it off. Before starting a specified activity.
 *
 * @author Joris Z. van den Oever
 * Created on 2/26/14.
 */
public class StealthDialReceiver extends BroadcastReceiver {
    public StealthDialReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if(phoneNumber.startsWith("#555")){
            Intent stealthCall = new Intent(context, HomeActivity.class);
            stealthCall.setAction("stealth.call");
            stealthCall.addCategory(Intent.CATEGORY_LAUNCHER);
            stealthCall.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
            stealthCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(stealthCall);

            setResultData(null);
        }
    }
}
