package sharing.APSharing;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.stealth.android.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by Alex on 2/26/14.
 */
public class HttpServerService extends Service {

    private static boolean isRunning = false;

    public static boolean isRunning(){
       return isRunning;
    }

    public static final String SSID_KEY = "SSID_VALUE";
    public static final String SHARE_FILE_PATH_KEY = "SHARE_FILE_PATH_KEY";
    public static final String SHARE_URI_KEY = "SHARE_URI_KEY";
    public static final String SHARE_MIME_TYPE_KEY = "SHARE_MIME_TYPE_KEY";

    private static final int notifyID = 481450917;

    private FileSharingHTTPD mServer;

    private NotificationManager mNotificationManager;
    //Since it's updated on multiple threads (animation and HTTP server), make volatile
    private volatile NotificationCompat.Builder mBuilder;

    boolean mDownloadIsFinished = false;
    private WifiAPManager mManager;
    private NotificationControl mNotificationControl;

    //Listens for a change in WIFI AP state. Needed because the server needs the AP's host address
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {

                if(mManager != null){
                    if(mManager.getWifiApState() == WifiAPManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED){
                        startSharingServer();
                    }
                    else{
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mManager = new WifiAPManager(this);
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        mNotificationControl = new NotificationControl(this, mNotificationManager, mBuilder);

        mServer = new FileSharingHTTPD(this);
        mServer.setOnTransferListener(mNotificationControl);

        IntentFilter mFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        //If the file server isn't running yet, but the AP is active, start sharing!
        if(!isRunning && mManager.getWifiApState() == WifiAPManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED){
            startSharingServer();
        }

        if(intent == null)
            return START_STICKY;

        String shareFile = intent.getStringExtra(SHARE_FILE_PATH_KEY);
        String uri = intent.getStringExtra(SHARE_URI_KEY);
        String mimeType = intent.getStringExtra(SHARE_MIME_TYPE_KEY);
        File transferObj = new File(shareFile);
        //Make sure all values are valid
        if(transferObj.exists() && uri != null && mimeType != null){
            Transferable transferable = new Transferable(transferObj, mimeType, transferObj.length());

            mServer.addTransferable(uri, transferable);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("HttpServerService", "Shutting down service");
        super.onDestroy();

        stopForeground(true);
        mServer.stop();
        mNotificationManager.cancel(notifyID);

        //Disable AP
        mManager = new WifiAPManager(this);
        mManager.setWifiApEnabled(null, false);

        //No need to listen anymore
        unregisterReceiver(mReceiver);

        isRunning = false;
    }

    //No bindings! It doesn't want to be in a relation at the moment!
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void constructNotification(){
        mBuilder.setContentTitle(getString(R.string.sharing_notification_title))
                .setContentText(getString(R.string.cancel_notification_text));
    }

    private void startSharingServer(){

        try {
            mServer.start();
        } catch (IOException e) {
            //Where did we go wrong?!
            Log.e("HttpServerService", "failed to start server!", e);
            stopSelf();
            return;
        }

        isRunning = true;

        /* Since we have a HTTP server running, we really don't want to get killed.
         * User specifies through custom notification the service should be stopped.
         */
        startForeground(notifyID, mBuilder.build());
    }

    /*
    private String getShareLink(){
        String ipAddress = getWifiApIpAddress();
        if(ipAddress != null){
            StringBuilder sb = new StringBuilder();
            sb.append("http://");
            sb.append(ipAddress);
            sb.append(":");
            sb.append(mServer.getListeningPort());
            sb.append("/");
            sb.append(FileSharingHTTPD.ApShareUri);

            String shareLink = sb.toString();
            Log.d("HttpServerService", "Share Link " + shareLink);

            return shareLink;
        }
        return null;
    }

    private String getApkName(){
        final PackageManager pm = getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            ai = null;
            Log.e("HttpServerService", "failed to get package name");
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : null);
        return applicationName;
    }


    public String getWifiApIpAddress() {
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
    }*/
}
