package com.stealth.utils;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Read: easy :)
 * This class provides some quick utils to shorten our code and make it more readable.
 * Created by Olivier Hokke on 3/26/14.
 */
public class Utils {

    private static WeakReference<Context> sContext;

    /**
     * Set the context value of the main activity, so others can easily access it.
     * @param context
     */
    public static void setContext(Context context) {
        sContext = new WeakReference<Context>(context.getApplicationContext());
    }

    /**
     * Get the context so you can easily access for instance the resources of this app,
     * show notifications or access the device's sensors.
     * @return
     */
    public static Context getContext() {
        return sContext.get();
    }

    /**
     * Get a string from the resources of this app.
     * @param resource the resource, for instance R.string.hello_world
     * @return the string value of the string resource
     */
    public static String str(int resource) {
        return getContext().getResources().getString(resource);
    }

    /**
     * Show a toast message (automatically runs this on main thread)
     * @param message_resource the string resource to show in a toast
     */
    public static void toast(int message_resource) {
        toast(str(message_resource));
    }

    /**
     * Show a toast message (automatically runs this on main thread)
     * @param message the message to show in a toast
     */
    public static void toast(final String message) {
        runOnMain(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Give a runnable to run on the main thread, for instance to modify the UI code
     * from a different thread
     * @param run The runnable to run on the main thread
     */
    public static void runOnMain(Runnable run) {
        new Handler(getContext().getMainLooper()).post(run);
    }

//    public static boolean delete(File deletableFile) {
//        return 0 < Utils.getContext().getContentResolver().delete(Uri.parse(deletableFile.toString()), null, null);
//    }

    /**
     * Created this method because the normal file.delete() does not always work properly and this
     * call seems to have more rights. (executes on current thread)
     * @param f the file to be deleted
     */
    public static boolean delete(File f) {
        return deleteImage(f) || deleteVideo(f) || f.delete();
    }

    /**
     * Deletes a file if it is an image. Using this method also removes the image from the database
     * and doesn't just delete the file itself. (executes on current thread)
     * @param f the image to remove
     * @return returns true if it succeeded
     */
    public static boolean deleteImage(File f)
    {
        if (!FileUtils.isImage(FileUtils.getMimeType(f))) {
            return false;
        }

        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor c = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + " = ?",
                new String[] { f.getAbsolutePath() },
                null);

        boolean success = false;

        if(c != null) {
            if (c.moveToFirst()) {
                // We found the ID. Deleting the item via the content provider will also remove the file
                long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                success = 0 < contentResolver.delete(deleteUri, null, null);
            }
            c.close();
        }
        return success;
    }

    /**
     * Deletes a file if it is an video. Using this method also removes the video from the database
     * and doesn't just delete the file itself. (executes on current thread)
     * @param f the video to remove
     * @return returns true if it succeeded
     */
    public static boolean deleteVideo(File f)
    {
        if (!FileUtils.isVideo(FileUtils.getMimeType(f))) {
            return false;
        }

        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor c = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Video.Media._ID },
                MediaStore.Video.Media.DATA + " = ?",
                new String[] { f.getAbsolutePath() },
                null);

        boolean success = false;

        if(c != null) {
            if (c.moveToFirst()) {
                // We found the ID. Deleting the item via the content provider will also remove the file
                long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                success = 0 < contentResolver.delete(deleteUri, null, null);
            }
            c.close();
        }
        return success;
    }
}
