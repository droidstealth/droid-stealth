package com.stealth.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

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
}
