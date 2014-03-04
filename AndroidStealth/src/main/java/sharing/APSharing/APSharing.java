package sharing.APSharing;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sharing.SharingUtils;

/**
 * Interface with the file sharing server from the Application side
 * Created by Alex on 2/26/14.
 */
public class APSharing implements HttpServerService.StopListener {
    public static final String CUSTOM_AP = "CUSTOM_AP";
    public static final int SSIDLength = 8;
    public static final String PREF_PASS_KEY = "PREF_PASS_KEY";
    public static final String PREF_SSID_KEY = "PREF_SSID_KEY";

    private WifiAPManager mWifiAPManager;

    private Context mAppContext;
    private int mOriginalWifiState;

    private SharingConnection mConnection;
    private HttpServerService.SharingBinder mBinder;

    private boolean isSharing = false;
    private String mSsid = null;

    //Listens for a change in WIFI AP state. Needed to maintain a proper state of the sharing
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {

                if(mWifiAPManager != null){
                    WifiAPManager.WIFI_AP_STATE state = mWifiAPManager.getWifiApState();

                    Log.d("APSharing", state.toString());
                    if(state != WifiAPManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED){
                        //if we lose connection during a running session, stop the service
                        if(HttpServerService.isRunning()){
                            stop();
                        }
                    }else {
                        if(!HttpServerService.isRunning()){
                            Intent startServiceIntent = new Intent(mAppContext, HttpServerService.class);

                            startServiceIntent.putExtra(HttpServerService.SSID_KEY, mSsid);

                            mAppContext.startService(startServiceIntent);
                            mAppContext.bindService(startServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
                        }
                    }
                }
            }
        }
    };

    /**
     * Creates a new instance of the APSharing class
     * @param context used to retrieve wifi state and start and stop service
     */
    public APSharing(Context context){
        mAppContext = context.getApplicationContext();
        mWifiAPManager = new WifiAPManager(mAppContext);

        mConnection = new SharingConnection();
        mOriginalWifiState = mWifiAPManager.getWifiState();
    }

    /**
     * * Sets up and shows the necessary dialogs to guide the user through the sharing of
     * the app by creating a hotspot and using a uri to share the apk link.
     */
     private void startShare(){
        //No need to do more, since the service is already running, or is being started
        if(isSharing)
            return;

        isSharing = true;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        boolean customAP = preferences.getBoolean(CUSTOM_AP, false);

        String pass = null;

        if(customAP){
            mSsid = preferences.getString(PREF_SSID_KEY, null);
            pass = preferences.getString(PREF_PASS_KEY, null);
        }

        //Value couldn't be found or custom AP wasn't set. Generate simple wifi configuration
        if(mSsid == null){
            mSsid = SharingUtils.generateRandomSSID(SSIDLength);
            pass = null;
        }

        //Only now do we actually care about the AP state
        IntentFilter mFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
        mAppContext.registerReceiver(mReceiver, mFilter);

        WifiConfiguration configuration = SharingUtils.getConfigFromFields(mSsid, pass);
        if(configuration != null){
            mWifiAPManager.setWifiApEnabled(configuration, true);
            Log.d("APSharing", "Enabled AP!");
        }
    }


    /**
     * Stops and unbinds the service
     */
    @Override
    public void stop(){
        Intent stopServiceIntent = new Intent(mAppContext, HttpServerService.class);
        mAppContext.stopService(stopServiceIntent);
        //check if bound first
        if(mBinder != null){
            mAppContext.unbindService(mConnection);
            mBinder = null;
        }

        mWifiAPManager.setWifiApEnabled(null, false);

        mAppContext.unregisterReceiver(mReceiver);

        isSharing = false;
    }

    /**
     * Removes the file associated with the uri from the list of served files
     * @param uri the uri which was set as part of the sharing link
     */
    public void stopShare(final String uri){
        if(mBinder != null){
            mBinder.removeItem(uri);
        } else {
            mQueuedTasks.add(new Runnable() {
                @Override
                public void run() {
                    mBinder.removeItem(uri);
                }
            });
        }
    }

    /**
     * Sets the file up on the server
     * @param uri the uri which is set as part of the sharing link
     * @param fileType mime type of the file
     * @param filePath path to the file
     * @param encrypted whether the file is encrypted
     * @param showIntent whether an intent is shown for the user to share the link
     */
    public void share(final String uri, final String fileType, final String filePath, final boolean encrypted, final boolean showIntent){
        if(!isSharing){
            startShare();
            Log.d("APSharing","Start Share");
        }

        //run now
        if(mBinder != null){
            mBinder.shareFile(uri, fileType, filePath, encrypted, showIntent);
        }
        //Or add to queue for later execution
        else {
            mQueuedTasks.add(new Runnable() {
                @Override
                public void run() {
                    mBinder.shareFile(uri, fileType, filePath, encrypted, showIntent);
                }
            });
        }
    }

    /**
     * Sets the apk up on the service, and opens a sharing intent for the file
     */
    public void shareApk() {
        String path = SharingUtils.getApk(mAppContext).getPath();
        String uri = SharingUtils.getApkName(mAppContext) + ".apk";
        String mimeType = "application/android";
        boolean encrypted = false;
        boolean showIntent = true;

        share(uri, mimeType, path, encrypted, showIntent);
    }

    /**
     * Gets all the files that are currently shared on the server
     * @return mapping of URIs and their files. Returns an empty map if no connection has been made yet.
     */
    public Map<String, File> getSharedItems(){
        if(mBinder != null)
            return mBinder.getSharedItems();
        else
            return new HashMap<String, File>();
    }

    private List<Runnable> mQueuedTasks = new ArrayList<Runnable>();

    /**
     * Listener for the bind of the service
     */
    private class SharingConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBinder = (HttpServerService.SharingBinder)iBinder;
            mBinder.addStopListener(APSharing.this);

            for(Runnable task : mQueuedTasks){
                task.run();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
}
