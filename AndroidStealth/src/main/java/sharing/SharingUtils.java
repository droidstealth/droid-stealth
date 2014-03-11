package sharing;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import sharing.APSharing.FileSharingHTTPD;
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

    /**
     * Checks validity of the ssid according to wifi specifications
     * @param ssid string to be validated
     * @return whether it is a valid Wifi SSID
     */
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

    /**
     * Checks validity of the password according to WPA specifications
     * @param password string to be validated
     * @return whether it is a valid WPA password
     */
    public static boolean validWPAPass(String password){
        return password.length() > 8 && password.length() < 64;
    }

    /**
     * Helper function to retrieve a share link given a certain uri
     * @param uri an identifier of the file that's going to be shared. Pick something recognizable.
     * @return a string representation of an HTTP link
     */
    public static String getShareLink(String uri){
        String ipAddress = getWifiIpAddress();
        if(ipAddress != null){
            StringBuilder sb = new StringBuilder();
            sb.append("http://");
            sb.append(ipAddress);
            sb.append(":");
            sb.append(FileSharingHTTPD.Port);
            sb.append("/");
            sb.append(uri);

            return sb.toString();
        }
        return null;
    }

    /**
     * Helper function to retrieve the name of the application
     * @param context
     * @return
     */
    public static String getApkName(Context context){
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            ai = null;
            Log.e("HttpServerService", "failed to get package name");
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : null);
        return applicationName;
    }

    /**
     * Helper function to retrieve a {@link java.io.File} referencing the Application's package.
     * @param context needed to retrieve the package location.
     * @return A reference to the apk. Can be null if the apk couldn't be found for some reason.
     */
    public static File getApk(Context context){
        String path = context.getPackageResourcePath();
        File apk  = new File(path);
        if(apk.exists())
            return apk;
        else
            return null;
    }

    /**
     * Generates a new alpha-numerical SSID
     * @param length length of the SSID
     * @return the new SSID
     */
    public static String generateRandomSSID(int length){
        RandomString randomString = new RandomString(length);
        return randomString.nextString();
    }

    /**
     * Constructs a valid {@link android.net.wifi.WifiConfiguration} from the SSID and password.
     * @param ssid a valid Wifi SSID
     * @param password a valid WPA password, or null if no password is used
     * @return A new {@link android.net.wifi.WifiConfiguration}, or null if the ssid or password were invalid
     */
    public static WifiConfiguration getConfigFromFields(String ssid, String password){
        WifiConfiguration configuration = new WifiConfiguration();

        if(!validSSID(ssid))
            return null;

        configuration.SSID = ssid;

        if(password != null && !password.isEmpty()){
            if(!validWPAPass(password))
                return null;
            configuration.preSharedKey = password;
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        }
        else {
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        configuration.status = WifiConfiguration.Status.ENABLED;

        return configuration;
    }



    /**
     * Gets the current IP address of the wifi connection. In the case of a wifi AP, it returns its
     * own host address, needed to share links with
     * @return a String representation of the Wifi IP address.
     */
    public static String getWifiIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        Log.d("HttpServerService", "Checking address: " + inetAddress.getHostAddress());
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
                            Log.d("HttpServerService", "Found IP: " + inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("HttpServerService", "failed to get IP");
        }
        Log.d("HttpServerService", "IP not found!");
        return null;
    }

    /**
     * simple helper class for random ID generation
     */
    private static class RandomString {
        private static final char[] symbols = new char[36];

        static {
            for (int idx = 0; idx < 10; ++idx)
                symbols[idx] = (char) ('0' + idx);
            for (int idx = 10; idx < 36; ++idx)
                symbols[idx] = (char) ('a' + idx - 10);
        }

        private final Random random = new Random();

        private final char[] buf;

        public RandomString(int length) {
            if (length < 1)
                throw new IllegalArgumentException("length < 1: " + length);
            buf = new char[length];
        }

        public String nextString() {
            for (int idx = 0; idx < buf.length; ++idx)
                buf[idx] = symbols[random.nextInt(symbols.length)];
            return new String(buf);
        }
    }
}
