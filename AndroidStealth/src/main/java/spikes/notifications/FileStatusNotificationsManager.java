package spikes.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.stealth.android.R;
import com.stealth.utils.Utils;

import encryption.EncryptionService;

/**
 * This class provides easy control of the notifications for this app.
 * Created by Olivier Hokke on 3/26/14.
 */
public class FileStatusNotificationsManager {

    public static final String ACTION_LOCK_ALL = "lockAllItems";

    private static FileStatusNotificationsManager sInstance = null;
    public static FileStatusNotificationsManager get() {
        if (sInstance == null) {
            sInstance = new FileStatusNotificationsManager(Utils.getContext());
        }
        return sInstance;
    }

    private Context mContext;
    private FileStatusNotificationsManager(Context context) {
        mContext = context;
    }

    private static final int NOTIFICATION_ID_LOCKING = 1;
    private static final int NOTIFICATION_ID_UNLOCKING = 2;
    private static final int NOTIFICATION_ID_UNLOCKED = 3;

    private boolean mShowingLocking = false;
    private boolean mShowingUnlocking = false;
    private boolean mShowingUnlocked = false;

    /**
     * Hides the notification for indicating the user that files are currently being locked.
     */
    public void hideFilesLocking() {
        if (!mShowingLocking) return;
        cancel(NOTIFICATION_ID_LOCKING);
        mShowingLocking = false;
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
        if (mShowingLocking) return;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_stat_locking);
        mBuilder.setContentTitle(Utils.str(R.string.notification_locking_title));
        mBuilder.setContentText(Utils.str(R.string.notification_locking_sub));

        if (progressMax > 0) mBuilder.setProgress(progressMax, progressCurrent, false);

        build(mBuilder, NOTIFICATION_ID_LOCKING);
        mShowingLocking = true;
    }

    /**
     * Hides the notification for indicating the user that files are currently being locked.
     */
    public void hideFilesUnlocking() {
        if (!mShowingUnlocking) return;
        cancel(NOTIFICATION_ID_UNLOCKING);
        mShowingUnlocking = false;
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
        if (mShowingUnlocking) return;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_stat_unlocking);
        mBuilder.setContentTitle(Utils.str(R.string.notification_unlocking_title));
        mBuilder.setContentText(Utils.str(R.string.notification_unlocking_sub));

        if (progressMax > 0) mBuilder.setProgress(progressMax, progressCurrent, false);

        mBuilder.setContentIntent(generatePendingIntent());
        build(mBuilder, NOTIFICATION_ID_UNLOCKING);
        mShowingUnlocking = true;
    }

    /**
     * Hide the notification for indicating the user that files are currently in the unlocked state.
     */
    public void hideFilesUnlocked() {
        if (!mShowingUnlocked) return;
        cancel(NOTIFICATION_ID_UNLOCKED);
        mShowingUnlocked = false;
    }

    /**
     * Show the notification for indicating the user that files are currently in the unlocked state.
     */
    public void showFilesUnlocked() {
        if (mShowingUnlocked) return;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_stat_unlocked);
        mBuilder.setContentTitle(Utils.str(R.string.notification_unlocked_title));
        mBuilder.setContentText(Utils.str(R.string.notification_unlocked_sub));
        mBuilder.setOngoing(true);

        mBuilder.setContentIntent(generatePendingIntent());
        build(mBuilder, NOTIFICATION_ID_UNLOCKED);
        mShowingUnlocked = true;
    }

    /**
     * Builds the notification and shows it to the user
     * @param notification the notification to build
     * @param id the id of the notification, allows changing this notification
     */
    private void build(NotificationCompat.Builder notification, int id) {
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, notification.build());
    }

    /**
     * Cancels the notification
     * @param id the id of the notification, allows changing this notification
     */
    private void cancel(int id) {
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    /**
     * Generates the intent.
     * @return
     */
    private PendingIntent generatePendingIntent()
    {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, EncryptionService.class);
        resultIntent.setAction(ACTION_LOCK_ALL);
        return PendingIntent.getService(Utils.getContext(), 0, resultIntent, 0);
    }
}
