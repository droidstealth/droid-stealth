package sharing.APSharing;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.stealth.android.R;

import java.io.IOException;

/**
 * Created by Alex on 2/26/14.
 */
public class HttpServerService extends Service implements AppSharingHTTPD.OnAppTransferListener {

    private static boolean isRunning = false;

    public static boolean isRunning(){
       return isRunning;
    }

    public static final String ARGUMENT_KEY = "BUNDLE_ARGS_VALUE";

    private static final int notifyID = 481450917;

    private AppSharingHTTPD mServer;

    private NotificationManager mNotificationManager;
    //Since it's updated on multiple threads (animation and HTTP server), make volatile
    private volatile NotificationCompat.Builder mBuilder;

    boolean mDownloadIsFinished = false;

    private Bundle mArgs;

    @Override
    public void onCreate() {
        super.onCreate();

        isRunning = true;

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.upload_notification_title))
                .setContentText(getString(R.string.upload_notification_text))
                .setSmallIcon(R.drawable.stat_sys_upload_anim0);

        mServer = new AppSharingHTTPD(this);
        mServer.setOnAppTransferListener(this);
        try {
            mServer.start();
        } catch (IOException e) {
            //Where did we go wrong?!
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mArgs != null)
            return START_NOT_STICKY;

        //Assume args have been set properly
        mArgs = intent.getBundleExtra(ARGUMENT_KEY);

        //TODO start special activity

        startForeground(notifyID, mBuilder.build());
        return START_STICKY;
    }

    //TODO call activity that shows ap status and share link
    private void setPendingIntent(){
        //TODO pass share link, ssid, pass


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mNotificationManager.cancel(notifyID);

        isRunning = false;
    }

    //No bindings! It doesn't want to be in a relation at the moment!
    @Override
    public IBinder onBind(Intent intent) {
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
