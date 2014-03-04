package sharing.APSharing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.stealth.android.R;

/**
 * Helper class for managing the sharing notification
 * Created by Alex on 3/3/14.
 */
public class NotificationControl implements FileSharingHTTPD.OnTransferListener {

    //list of animation IDs for when we're
    private static final int[] animIDs = new int[]{
            R.drawable.stat_sys_upload_anim0,
            R.drawable.stat_sys_upload_anim1,
            R.drawable.stat_sys_upload_anim2,
            R.drawable.stat_sys_upload_anim3,
            R.drawable.stat_sys_upload_anim4,
            R.drawable.stat_sys_upload_anim5,
    };

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private Context mAppContext;
    private boolean mIsRunning;

    // Values to keep track of the sharing progress and Notification representation
    private long totalBytes = 0;
    private long bytesRead = 0;
    private int fileCount = 0;

    private String mSsid;

    public static final int NOTIFY_ID = 481450917;

    /**
     *
     * @param context Used to retrieve resource strings
     * @param mNotificationManager updates the notification in Android
     * @param mBuilder alters the notification content
     */
    public NotificationControl(Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {
        this.mNotificationManager = mNotificationManager;
        this.mBuilder = mBuilder;

        mAppContext = context.getApplicationContext();

        //loadIcons();
        setNotificationDefaults();
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    /**
     * Sets the SSID and updates the notification
     * @param ssidName the name of the wifi AP
     */
    public void setSSIDName(String ssidName){
        mSsid = ssidName;
        setContentTitle();
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    /**
     * Sets the notification title based on whether the SSID has been set
     */
    private void setContentTitle(){
        StringBuilder sb = new StringBuilder();
        if(mSsid != null && !mSsid.isEmpty()){
            sb.append(mAppContext.getString(R.string.on_network_title));
            sb.append(" \"");
            sb.append(mSsid);
            sb.append('\"');
        }
        else {
            sb.append(mAppContext.getString(R.string.sharing_notification_title));
        }

        mBuilder.setContentTitle(sb.toString());
    }

    public void cancel(){
        mIsRunning = false;
        setNotificationDefaults();
    }

    /**
     * Helper function for {@link sharing.APSharing.HttpServerService#startForeground(int, android.app.Notification)}
     * @return
     */
    public Notification getNotification() {
        return mBuilder.build();
    }

    /**
     * Sets up an on click stop intent. User will be informed of this via text in the notification
     */
    private void setStopIntent(){
        int requestID = (int) System.currentTimeMillis();
        Intent stopIntent = new Intent(mAppContext, HttpServerService.class);
        stopIntent.putExtra(HttpServerService.STOP_KEY, true);
        PendingIntent intent = PendingIntent.getService(mAppContext, requestID, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(intent);
    }

    /**
     * sets the values for a default notification for our sharing service
     */
    private void setNotificationDefaults(){
        setContentTitle();
        mBuilder.setContentText(mAppContext.getString(R.string.cancel_notification_text));
        mBuilder.setSmallIcon(animIDs[0]);
        mBuilder.setContentInfo("");
        mBuilder.setOngoing(false);
        setStopIntent();
    }

    @Override
    public void onTransferStarted(Transferable transferable) {
        totalBytes += transferable.getContentLength();
        fileCount++;

        if(!mIsRunning){
            showUploadStart();
        }

        updateProgress();
    }

    @Override
    public void onBytesRead(long bytesRead, Transferable transferable) {
        this.bytesRead += bytesRead;

        updateProgress();
    }

    @Override
    public void onTransferFinished(Transferable transferable) {
        totalBytes -= transferable.getContentLength();
        bytesRead -= transferable.getContentLength();
        fileCount--;

        updateProgress();
    }

    //maximum for the progressbar. Gives accuracy. Keep below INT_MAX_VALUE
    private static final long total = 100l;

    /**
     * Updates the values of the notification
     */
    private void updateProgress(){
        int score = 0;
        if(totalBytes != 0){
            score = (int)(total * (bytesRead / totalBytes));
        }

        if(fileCount == 0){
            mIsRunning = false;
            setNotificationDefaults();
        }else{
            mBuilder.setNumber(fileCount);
        }

        mBuilder.setProgress(score, (int)total, false);

        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    /**
     * Starts a new animation thread and changes the notification state.
     */
    private void showUploadStart(){
        mIsRunning = true;
        Thread aniThread = new Thread(new Animator());
        aniThread.setPriority(Thread.MIN_PRIORITY);
        aniThread.setDaemon(true);
        aniThread.start();

        mBuilder.setContentText(mAppContext.getString(R.string.sharing_notification_text));
        mBuilder.setOngoing(true);
        //Disable stopping of service during upload?
        mBuilder.setContentIntent(null);
    }

    private static final long sleepTime = 200l;

    private int animIndex = 0;

    /**
     * Helper class that updates the notification's icon to animate it
     */
    private class Animator implements Runnable {
        @Override
        public void run() {
            while(mIsRunning){
                try {
                    Thread.sleep(sleepTime);

                    mBuilder.setSmallIcon(animIDs[animIndex++]);
                    mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
