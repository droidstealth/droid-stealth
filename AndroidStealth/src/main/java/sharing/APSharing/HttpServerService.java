package sharing.APSharing;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.stealth.android.R;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Alex on 2/26/14.
 */
public class HttpServerService extends Service implements AppSharingHTTPD.OnAppTransferListener {

    private static boolean isRunning = false;

    public static boolean isRunning(){
       return isRunning;
    }

    public static final String ARGUMENT_KEY = "BUNDLE_ARGS_VALUE";
    public static final String NETWORK_STATUS = "NETWORK_STATUS";

    private static final int notifyID = 481450917;

    private AppSharingHTTPD mServer;

    private NotificationManager mNotificationManager;
    //Since it's updated on multiple threads (animation and HTTP server), make volatile
    private volatile NotificationCompat.Builder mBuilder;

    boolean mDownloadIsFinished = false;
    private Intent statusIntent;
    WifiAPManager mManager;
    private boolean apActive = false;
    private int mOriginalWifiStatus;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {

                if(mManager != null){
                    if(mManager.getWifiApState() == WifiAPManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED)
                        apActive = true;
                    else{
                        apActive = false;
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        isRunning = true;

        IntentFilter mFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
        registerReceiver(mReceiver, mFilter);

        mManager = new WifiAPManager(this);
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.upload_notification_title))
                .setContentText(getString(R.string.waiting_notification_text))
                .setSmallIcon(R.drawable.stat_sys_upload_anim0);

        statusIntent = new Intent(this, ServerStatusActivity.class);
        statusIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        mBuilder.setContentIntent(PendingIntent.getActivity(this, 0, statusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));

        mServer = new AppSharingHTTPD(this);
        mServer.setOnAppTransferListener(this);
        try {
            mServer.start();
        } catch (IOException e) {
            //Where did we go wrong?!
            Log.e("HttpServerService", "failed to start server!", e);
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Because the hotspot might not be running immediately, we sleep a bit
                while (!apActive){
                    try {
                        Thread.sleep(200l);
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                //Assume args have been set properly
                mOriginalWifiStatus = intent.getIntExtra(NETWORK_STATUS, 0);
                final String shareLink = getShareLink();

                String ssid = intent.getStringExtra(ServerStatusActivity.SSID_KEY);
                String pass = intent.getStringExtra(ServerStatusActivity.PASS_KEY);
                statusIntent.putExtra(ServerStatusActivity.SSID_KEY, ssid);
                statusIntent.putExtra(ServerStatusActivity.PASS_KEY, pass);
                statusIntent.putExtra(ServerStatusActivity.SHARE_LINK, shareLink);

                mBuilder.setContentIntent(PendingIntent.getActivity(HttpServerService.this, 0, statusIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
                mNotificationManager.notify(notifyID, mBuilder.build());

                //Start activity as dialog
                startActivity(statusIntent);

                startForeground(notifyID, mBuilder.build());
            }
        }).start();

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

        //if wifi was enabled before, turn it back on
        Log.d("HttpServerService", "Original wifi state: " + mOriginalWifiStatus);
        if(mOriginalWifiStatus == WifiManager.WIFI_STATE_ENABLED ||
                mOriginalWifiStatus == WifiManager.WIFI_STATE_ENABLING){
            Log.d("HttpServerService", "Restoring wifi");
            mManager.setWifiEnabled(true);
        }

        //No need to listen anymore
        unregisterReceiver(mReceiver);

        isRunning = false;
    }

    //No bindings! It doesn't want to be in a relation at the moment!
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getShareLink(){
        String ipAddress = getWifiApIpAddress();
        if(ipAddress != null){
            StringBuilder sb = new StringBuilder();
            sb.append("http://");
            sb.append(ipAddress);
            sb.append(":");
            sb.append(mServer.getListeningPort());
            sb.append("/");
            sb.append(AppSharingHTTPD.ApShareUri);

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
    }

    private int[] animIDs = new int[]{
            R.drawable.stat_sys_upload_anim0,
            R.drawable.stat_sys_upload_anim1,
            R.drawable.stat_sys_upload_anim2,
            R.drawable.stat_sys_upload_anim3,
            R.drawable.stat_sys_upload_anim4,
            R.drawable.stat_sys_upload_anim5,
    };

    private static final long sleepTime = 200l;

    private int animIndex = 0;

    @Override
    public void appTransferStarted() {
        //we don't want to get killed at this point!
        mBuilder.setContentText(getString(R.string.upload_notification_text));

        startForeground(notifyID, mBuilder.build());

        //Run new thread to update notification icon
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(!mDownloadIsFinished){
                            try {
                                Thread.sleep(sleepTime);

                                mBuilder.setSmallIcon(animIDs[animIndex++]);
                                mNotificationManager.notify(notifyID, mBuilder.build());
                            } catch (InterruptedException e) {
                                mDownloadIsFinished = true;
                            }
                        }
                    }
                }
        ).start();
    }

    //Update notification
    @Override
    public void onBytesRead(long totalBytesRead) {
        Log.d("HttpServerService","Bytes sent: " +  totalBytesRead);
        mBuilder.setProgress((int)mServer.getAppSize(), (int)totalBytesRead, false);
        mNotificationManager.notify(notifyID, mBuilder.build());
    }

    @Override
    public void appTransferFinished() {
        mDownloadIsFinished = true;
        //Set to finalize
        mBuilder.setContentTitle(getString(R.string.finalized_notification_text))
                .setSmallIcon(R.drawable.stat_sys_upload_anim0)
                .setProgress(0,0,false);
        //Allow notification to be removed
        stopForeground(false);
    }
}
