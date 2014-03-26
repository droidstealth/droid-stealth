package com.stealth.utils;

import android.content.Context;

/**
 * Read: easy :)
 * This class provides some quick utils to shorten our code and make it more readable.
 * Created by Olivier Hokke on 3/26/14.
 */
public class EZ {

    private static Context sContext;

    /**
     * Set the context value of the main activity, so others can easily access it.
     * @param context
     */
    public static void setContext(Context context) {
        sContext = context;
    }

    /**
     * Get the context so you can easily access for instance the resources of this app,
     * show notifications or access the device's sensors.
     * @return
     */
    public static Context getContext() {
        return sContext;
    }

    /**
     * Get a string from the resources of this app.
     * @param resource the resource, for instance R.string.hello_world
     * @return the string value of the string resource
     */
    public static String str(int resource) {
        return sContext.getResources().getString(resource);
    }
}
