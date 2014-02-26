package sharing;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Environment;

import sharing.APSharing.WifiAPManager;

/**
 * Created by Alex on 2/25/14.
 */
public class SharingUtils {
    public static boolean hasBluetoothSupport(){
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean hasAPWifiSupport(Context context){
        WifiAPManager manager = new WifiAPManager(context);
        return manager.getWifiApState() != WifiAPManager.WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
    }

    public static boolean hasRemovableExternalStorage(){
        return Environment.isExternalStorageRemovable();
    }

    private static char[] invalidChars = new char[]{
        '?', '\"' , '$', '[', '\\' , ']', '+'
    };

    private static char[] invalidFirstChars = new char[]{
        '!', '#', ';'
    };

    public static boolean validSSID(String ssid){
        //Technically allowed, but we like to keep it simple
        if(ssid == null)
            return false;

        //empty strings 'not allowed' either!
        if(ssid.isEmpty())
            return false;

        //These chars have been specified not to be allowed at all
        for (char invalid : invalidChars){
            if(ssid.indexOf(invalid) != -1)
                return false;
        }

        //The SSID can't start with these chars
        for (char invalidFirst : invalidFirstChars){
            if(ssid.indexOf(invalidFirst) == 0)
                return false;
        }

        //everything else is allowed
        return true;
    }

    public static boolean validWPAPass(String password)
    {
        return password.length() > 8 && password.length() < 64;
    }
}
