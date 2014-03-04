package sharing.APSharing;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.stealth.android.R;

/**
 * Helper class for managing the sharing notification
 * Created by Alex on 3/3/14.
 */
public class NotificationControl implements FileSharingHTTPD.OnTransferListener {

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

    private long totalBytes = 0;
    private long bytesRead = 0;
    private int fileCount = 0;

    private Bitmap[] icons;

    private static final int notifyID = 481450917;

    public NotificationControl(Context context, NotificationManager mNotificationManager, NotificationCompat.Builder mBuilder) {
        this.mNotificationManager = mNotificationManager;
        this.mBuilder = mBuilder;

        mAppContext = context.getApplicationContext();

        loadIcons();
        setNotificationDefaults();
    }

    /**
     * Loads the bitmaps for our icon animation
     */
    private void loadIcons(){
        icons = new Bitmap[animIDs.length];
        for(int i = 0; i < animIDs.length; i++){
            icons[i] = BitmapFactory.decodeResource(mAppContext.getResources(), animIDs[i]);
        }
    }

    /**
     * sets the values for a default notification for our sharing service
     */
    private void setNotificationDefaults(){
        mBuilder.setContentTitle(mAppContext.getString(R.string.sharing_notification_title));
        mBuilder.setContentText(mAppContext.getString(R.string.cancel_notification_text));
        mBuilder.setLargeIcon(icons[0]);
        mBuilder.setContentInfo("");
        mBuilder.setOngoing(false);

    }

    @Override
    public void onTransferStarted(Transferable transferable) {
        totalBytes += transferable.getContentLength();
        fileCount++;

        if(!mIsRunning){
            start();
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

        mNotificationManager.notify(notifyID, mBuilder.build());
    }

    /**
     * Starts a new animation thread and changes the notification state.
     */
    private void start(){
        mIsRunning = true;
        Thread aniThread = new Thread(new Animator());
        aniThread.setPriority(Thread.MIN_PRIORITY);
        aniThread.setDaemon(true);
        aniThread.start();

        mBuilder.setContentText(mAppContext.getString(R.string.sharing_notification_text));
        mBuilder.setOngoing(true);
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
                    mNotificationManager.notify(notifyID, mBuilder.build());
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
