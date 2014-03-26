package spikes.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.utils.EZ;

/**
 * This class provides easy control of the notifications for this app.
 * Created by Olivier Hokke on 3/26/14.
 */
public class FileStatusNotificationsManager {
    private static FileStatusNotificationsManager sInstance = null;
    public static FileStatusNotificationsManager get() {
        if (sInstance == null) {
            sInstance = new FileStatusNotificationsManager(EZ.getContext());
        }
        return sInstance;
    }

    private Context mContext;
    private FileStatusNotificationsManager(Context context) {
        mContext = context;
    }

    /**
     * Show the notification for indicating the user that files are currently being locked.
     */
    public void showFilesLocking() {
        showFilesLocking(0, 0);
    }

    /**
     * Show the notification for indicating the user that files are currently being locked.
     * Also, can show the progress of this using the parameters.
     * @param progressMax the max value for the progress bar
     * @param progressCurrent the current value for the progress bar
     */
    public void showFilesLocking(int progressMax, int progressCurrent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_stat_locking);
        mBuilder.setContentTitle(EZ.str(R.string.notification_locking_title));
        mBuilder.setContentText(EZ.str(R.string.notification_locking_sub));

        if (progressMax > 0) mBuilder.setProgress(progressMax, progressCurrent, false);

        build(mBuilder, 0);
    }

    /**
     * Show the notification for indicating the user that files are currently being locked.
     */
    public void showFilesUnlocking() {
        showFilesUnlocking(0, 0);
    }

    /**
     * Show the notification for indicating the user that files are currently being UNlocked.
     * Also, can show the progress of this using the parameters.
     * @param progressMax the max value for the progress bar
     * @param progressCurrent the current value for the progress bar
     */
    public void showFilesUnlocking(int progressMax, int progressCurrent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_stat_unlocking);
        mBuilder.setContentTitle(EZ.str(R.string.notification_unlocking_title));
        mBuilder.setContentText(EZ.str(R.string.notification_unlocking_sub));

        if (progressMax > 0) mBuilder.setProgress(progressMax, progressCurrent, false);

        mBuilder.setContentIntent(generatePendingIntent());
        build(mBuilder, 1);
    }

    /**
     * Show the notification for indicating the user that files are currently in the unlocked state.
     */
    public void showFilesUnlocked() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_stat_unlocked);
        mBuilder.setContentTitle(EZ.str(R.string.notification_unlocked_title));
        mBuilder.setContentText(EZ.str(R.string.notification_unlocked_sub));

        mBuilder.setContentIntent(generatePendingIntent());
        build(mBuilder, 2);
    }

    /**
     * Builds the notification and shows it to the user
     * @param notification the notification to build
     * @param id the id of the notification, allows changing this notification
     */
    private void build(NotificationCompat.Builder notification, int id) {
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(id, notification.build());
    }

    /**
     * Generates the intent. //TODO create the intent for locking files
     * @return
     */
    private PendingIntent generatePendingIntent() {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, HomeActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity. This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(HomeActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
