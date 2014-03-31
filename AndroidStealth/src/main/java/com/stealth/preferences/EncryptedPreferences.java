package com.stealth.preferences;

import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class provides the possibility to save settings to the preferences folder in our main
 * directory and saves it encrypted so that no one can read the settings. Only we can.
 * Advantage is that if the encryption key is backed (exported/imported) up or doesn't change
 * between main folders, one can swap between them!
 *
 * Created by OlivierHokke on 3/31/14.
 */
public class EncryptedPreferences {
    private static HashMap<String, EncryptedPreferences> sInstances
            = new HashMap<String, EncryptedPreferences>();

    /**
     * Get the encryption preferences object that you want. You might have to wait a bit because
     * it is possible that it is still reading the file
     * @param name the name of the preferences file
     * @param callback the callback that will notify you that the instance is ready for you to use
     */
    public static void get(final String name, IOnResult<EncryptedPreferences> callback)
    {
        Runnable handleQueue = new Runnable() {
            @Override
            public void run() {
                sInstances.get(name).handleQueue();
            }
        };

        if (!sInstances.containsKey(name))
        {
            sInstances.put(name, new EncryptedPreferences(name, handleQueue));
        }
        sInstances.get(name).addToQueue(callback);
        handleQueue.run();
    }

    /**
     * The queue
     */
    private LinkedList<IOnResult<EncryptedPreferences>> mQueue = new LinkedList<IOnResult<EncryptedPreferences>>();

    /**
     * Add a callback to the queue of a preference file
     * @param waiting the callback to put on hold
     */
    private void addToQueue(IOnResult<EncryptedPreferences> waiting) {
        mQueue.addFirst(waiting);
    }

    /**
     * Handles the queue if we are ready with reading the preference file
     */
    private void handleQueue() {
        // only handle if we are ready to
        if (!mReady) return;

        Utils.runOnMain(new Runnable() {
            @Override
            public void run() {
                // let's handle the queue because we are ready to handle them
                while (!mQueue.isEmpty()) {
                    mQueue.getLast().onResult(EncryptedPreferences.this);
                }
            }
        });
    }

    /**
     * The name of this preferences file
     */
    private String mName;
    /**
     * Is the preferences file read/created?
     */
    private boolean mReady;
    /**
     * Are we currently writing?
     */
    private boolean mWriting;
    /**
     * When we were already writing but another write request was made, then
     * do another write
     */
    private boolean mWriteAgain;
    /**
     * The file that contains the encrypted version of these preferences
     */
    private File mEncryptedFile;
    /**
     * The JSON object that contains all live values
     */
    private JSONObject mJson;

    /**
     * Creates a new EncryptedPreferences object and reads/creates the appropriate
     * preferences file.
     * @param name the name of the preferences file
     * @param onReady what to do when ready
     */
    private EncryptedPreferences(String name, final Runnable onReady) {
        sInstances.put(mName, this);
        mReady = false;
        mName = name;
        mJson = new JSONObject();
        mEncryptedFile = new File(Directories.prefs(), name + ".crypto");

        if (mEncryptedFile.exists()) // one can only read it if it exists
        {
            readPreferences(new IOnResult<Boolean>() {
                @Override
                public void onResult(Boolean result) {
                    if (result)
                    {
                        mReady = true;
                        onReady.run();
                    }
                }
            });
        }
        else // it doesn't exist, create it
        {
            writePreferences();
            mReady = true;
            onReady.run();
        }
    }

    /**
     * Get the JSON Object that contains the settings
     * @return the json object with all the settings
     */
    public JSONObject getJson() {
        return mJson;
    }

    /**
     * Reads the preferences file as it is encrypted in the preferences folder
     * @param callback the callback to notify the result
     */
    private void readPreferences(final IOnResult<Boolean> callback )
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    File cache = Utils.getRandomCacheFile(".prefs");

                    // TODO decrypt mEncryptedFile to mDecryptedFile

                    //Read text from file
                    //mJson = new JSONObject(Utils.read(cache)); // correct way
                    mJson = new JSONObject(Utils.read(mEncryptedFile)); //TEMPORARY

                    // we succeeded
                    if (callback != null)
                        callback.onResult(true);
                }
                catch (JSONException e)
                {
                    Utils.toast("Can't read preferences file.. " + e.getMessage());
                    if (callback != null)
                        callback.onResult(false);
                }
                catch (IOException e)
                {
                    Utils.toast("Can't read preferences file.. " + e.getMessage());
                    if (callback != null)
                        callback.onResult(false);
                }
            }
        });
    }

    /**
     * Writes the preferences file from the current live JSON object to the preferences folder
     * and encrypts it
     */
    private void writePreferences()
    {
        if (mWriting) mWriteAgain = true;
        mWriting = true;

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    File cache = Utils.getRandomCacheFile(".prefs");
                    Utils.write(cache, mJson.toString());

                    //mEncryptedFile;
                    // TODO encrypt mDecryptedFile to mEncryptedFile
                    // TODO then remove the mDecryptedFile
                    Utils.copyFile(cache, mEncryptedFile);

//                    // delete old
//                    if (!mEncryptedFile.delete())
//                        throw new IOException("Could not delete encrypted preference file");
//                    // put new one
//                    if (!cache.renameTo(mEncryptedFile))
//                        throw new IOException("Could not place new encrypted preference file");
                }
                catch (IOException e)
                {
                    Utils.toast("Can't write preferences file.. " + e.getMessage());
                }

                mWriting = false;
                if (mWriteAgain)
                {
                    writePreferences();
                    mWriteAgain = false;
                }
            }
        });
    }
}
