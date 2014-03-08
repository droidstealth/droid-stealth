package sharing.APSharing;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.stealth.android.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sharing.SharingUtils;

/**
 * Created by Alex on 2/26/14.
 */
public class HttpServerService extends Service {
    public interface StopListener {
        public void stop();
    }

    private static boolean isRunning = false;

    public static boolean isRunning(){
       return isRunning;
    }

    public static final String SSID_KEY = "SSID_VALUE";
    public static final String STOP_KEY = "STOP_KEY";

    private FileSharingHTTPD mServer;

    private WifiAPManager mManager;
    private volatile NotificationControl mNotificationControl;
    List<StopListener> mStopListeners;

    @Override
    public void onCreate() {
        super.onCreate();

        mManager = new WifiAPManager(this);
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        mStopListeners = new ArrayList<StopListener>();
        mNotificationControl = new NotificationControl(this, notificationManager, builder);

        mServer = new FileSharingHTTPD(this);
        mServer.setOnTransferListener(mNotificationControl);

        startSharingServer();
    }

    /**
     * Only used for setting the SSID at the start.
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        //If the file server isn't running yet, but the AP is active, start sharing!
        if(!isRunning){
            startSharingServer();
        }

        if(intent == null){
            return START_STICKY;
        }

        if(intent.getBooleanExtra(STOP_KEY, false)){
            Log.d("HTTPServerService", "Received stop message");

            for(StopListener listener : mStopListeners)
                listener.stop();
            stopSelf();
        }

        //set ssid in notification
        String ssid = intent.getStringExtra(SSID_KEY);
        if(ssid != null){
            mNotificationControl.setSSIDName(ssid);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("HttpServerService", "Shutting down service");
        super.onDestroy();

        stopForeground(true);
        mServer.stop();

        mNotificationControl.cancel();

        isRunning = false;
    }

    /**
     * We share a simple inner binder which can access the server's fields so we can add files to the server
     * @param intent ignored
     * @return a SharingBinder object
     */
    @Override
    public IBinder onBind(Intent intent) {
        return new SharingBinder();
    }

    /**
     * Start the server itself.
     */
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
        startForeground(NotificationControl.NOTIFY_ID, mNotificationControl.getNotification());
    }

    /**
     * A helper class for sharing the files
     */
    public class SharingBinder extends Binder {
        public void shareFile(String uri, String fileType, String filePath, boolean encoded, boolean showIntent){
            File transferObj = new File(filePath);
            //Make sure all values are valid
            if(transferObj.exists() && uri != null && fileType != null){
                Transferable transferable = new Transferable(transferObj, fileType, transferObj.length());

                mServer.addTransferable(uri, transferable);

                if(showIntent){
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, SharingUtils.getShareLink(uri));
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_uri) + " " + uri);
                    startActivity(sendIntent);
                }
            }
        }

        public Map<String, File> getSharedItems(){
            return mServer.getSharedItems();
        }

        public void addStopListener(StopListener listener){
            if(!mStopListeners.contains(listener))
                mStopListeners.add(listener);
        }
        public boolean removeStopListener(StopListener listener){
            return mStopListeners.remove(listener);
        }

        public void removeItem(String uri){
            mServer.removeTransferable(uri);
        }
    }
}
