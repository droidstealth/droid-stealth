package spikes.stealthdialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stealth.android.HomeActivity;

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
