package sharing;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by Alex on 2/24/14.
 */
//TODO move to activity?
public class AppSharing {

    public boolean ShareAppViaBluetooth(Context context){
        //first we get the bluetooth intent. For now we'll zip
        //TODO: ask whether zip is needed?
        Intent btIntent = getBluetoothAppShareIntent(context, true);
        //we failed unfortunately
        if(btIntent == null)
            return false;

        //TODO make discoverable and share app

        return true;
    }

    /**
     * gets an intent to share the application over bluetooth
     * @param context context needed to retrieve the app and information about it
     * @param zip Whether the .apk is renamed to .zip. For stock android devices, should always
     *            be zipped. Some custom ROMs support bluetooth receiving of .apk files.
     * @return the intent to launch a bluetooth transmission
     */
    private Intent getBluetoothAppShareIntent(Context context, boolean zip){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null){
            //No Bluetooth support. Can't send anything
            return null;
        }

        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo;

        String packageName = context.getApplicationContext().getPackageName();

        try {
            appInfo = pm.getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA);
            Intent sendBt = new Intent(Intent.ACTION_SEND);
            // NOT THIS! sendBt.setType("application/vnd.android.package-archive");
            //zip because most android systems throw a fit if they receive apks over bluetooth
            sendBt.setType("application/zip");
            sendBt.putExtra(Intent.EXTRA_STREAM,
                    Uri.parse("file://" + appInfo.publicSourceDir));
            //calls bluetooth handling app
            sendBt.setClassName("com.android.bluetooth",
                    "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
            return  sendBt;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }

        //we failed
        return null;
    }
}
