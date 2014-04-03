package com.stealth.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.BuildConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.util.Random;

/**
 * Read: easy :)
 * This class provides some quick utils to shorten our code and make it more readable.
 * Created by Olivier Hokke on 3/26/14.
 */
public class Utils {

    private static final int MAX_RANDOM_STRING_LENGTH = 15;
    private static final String TAG = "TUDELFT";

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
     * Returns the log tag for class
     * @param object the class
     * @return logtag
     */
    public static String tag(Object object) {
        return TAG + object.getClass().getName();
    }

    /**
     * Returns the log tag
     * @return log tag
     */
    public static String tag() {
        return TAG;
    }

    /**
     * Get a string from the resources of this app.
     * @param resource the resource, for instance R.string.hello_world
     * @return the string value of the string resource
     */
    public static String str(int resource) {
        if (getContext() == null) return "";
        return getContext().getResources().getString(resource);
    }

    /**
     * Get a color from the resources of this app.
     * @param resource the resource, for instance R.string.hello_world
     * @return the string value of the string resource
     */
    public static int color(int resource) {
        if (getContext() == null) return Color.WHITE;
        return getContext().getResources().getColor(resource);
    }

    /**
     * Read a file as plain text
     * @param resource the file to read
     * @return the contents of the file
     */
    public static String read(File resource) throws IOException
    {
        StringBuilder text = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(resource));
        String line;

        while ((line = br.readLine()) != null)
        {
            text.append(line);
            text.append('\n');
        }

        return text.toString();
    }

    /**
     * Read a file as plain text
     * @param resource the file to read
     * @return the contents of the file
     */
    public static void write(File resource, String contents) throws IOException
    {
        FileWriter out = new FileWriter(resource);
        out.write(contents);
        out.close();
    }

    /**
     * Show a toast message (automatically runs this on main thread)
     * @param message_resource the string resource to show in a toast
     */
    public static void toast(int message_resource) {
        final String message = str(message_resource);
	    d("[TOAST] " + message, 1);
	    runOnMain(new Runnable() {
		    @Override
		    public void run() {
			    Toast.makeText(getContext(), message,
					    Toast.LENGTH_SHORT).show();
		    }
	    });
    }

	/**
	 * Print a debug message to logcat (and shows the source as well)
	 * @param message the message to log
	 */
	public static void d(final String message) {
		d(message, 1);
	}

    /**
     * Print a debug message to logcat (and shows the source as well)
     * @param message the message to log
     * @param padStack the amount of StackTraceElements earlier you want to show the stacktrace info
     */
    public static void d(final String message, int padStack) {
	    if (!BuildConfig.DEBUG) return;
	    StackTraceElement calledFrom = Thread.currentThread().getStackTrace()[3 + padStack];
	    Log.d(tag(), String.format("%1$-"+75+ "s", message) + " ~ [" + calledFrom.getClassName() + "." + calledFrom.getMethodName() + "@" + calledFrom.getLineNumber() + "]");
    }

    /**
     * Give a runnable to run on the main thread, for instance to modify the UI code
     * from a different thread
     * @param run The runnable to run on the main thread
     */
    public static void runOnMain(Runnable run) {
        if (getContext() == null) return;
        new Handler(getContext().getMainLooper()).post(run);
    }

    /**
     * Generates a random string of a random size
     * @return
     */
    public static String randomString() {
        // ensure different seed than next randomString call
        Random random = new Random(System.nanoTime() - 10);
        return randomString(random.nextInt(MAX_RANDOM_STRING_LENGTH));
    }

    /**
     * Generates a random string of a given size
     * @param length the size to give
     * @return
     */
    public static String randomString(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVW1234567890-_".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Helper function to copy a file internally
     * @param sourceFile the source file to copy
     * @param destFile the destination file to copy
     * @throws IOException
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    /**
     * Gets random file name for temporary file reading and writing
     * @param extension the extension to use for this file (include the '.')
     * @return the temporary file
     */
    public static File getRandomCacheFile(String extension)
    {
        return getRandomFile(getContext().getCacheDir(), extension);
    }

    /**
     * Gets random file name
     * @param baseDirectory the directory where to store the file
     * @param extension the extension to use for this file (include the '.')
     * @return the temporary file
     */
    public static File getRandomFile(File baseDirectory, String extension)
    {
        return new File(baseDirectory, randomString() + extension);
    }

    /**
     * Created this method because the normal file.delete() does not always work properly and this
     * call seems to have more rights. (executes on current thread)
     * @param f the file to be deleted
     * @return true if file is gone (also if it didn't exist in the first place)
     */
    public static boolean delete(File f) {
        return !f.exists() || deleteImage(f) || deleteVideo(f) || f.delete();
    }

    /**
     * Deletes a file if it is an image. Using this method also removes the image from the database
     * and doesn't just delete the file itself. (executes on current thread)
     * @param f the image to remove
     * @return returns true if it succeeded
     */
    public static boolean deleteImage(File f)
    {
        if (!FileUtils.isImage(FileUtils.getMimeType(f)) || getContext() == null) {
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
        if (!FileUtils.isVideo(FileUtils.getMimeType(f)) || getContext() == null) {
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
