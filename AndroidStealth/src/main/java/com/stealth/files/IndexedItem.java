package com.stealth.files;

import com.stealth.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

/**
 * The interface that contains the most important functionality for an indexed item
 * Created by OlivierHokke on 4/1/14.
 */
public abstract class IndexedItem {
    public static final int FOLDER_UID = 0;
    private String mUID;
    private JSONArray mRaw;
    private int mJsonID;

    /**
     * @return the remembered position in the JSON Array
     */
    public int getJsonID() {
        return mJsonID;
    }

    /**
     * @param jsonID the remembered position in the JSON Array
     */
    public void setJsonID(int jsonID) {
        this.mJsonID = jsonID;
    }

    public JSONArray makeRaw() {
        JSONArray raw = new JSONArray();
        raw.put(mUID);
        return raw;
    }

    IndexedItem(JSONArray raw) {
        mUID = raw.optString(FOLDER_UID);
        mRaw = raw;
    }

    IndexedItem() { }

    void conclude() {
        this.mUID = Utils.randomString(5);
        // TODO check if indeed unique UID
        this.mRaw = makeRaw();
    }

    public String getUID() {
        return mUID;
    }

    public JSONArray getRaw() {
        return mRaw;
    }

    /**
     * When reading out the JSON file first the objects are created
     * then afterwards the links are made.
     */
    public abstract void resolveLink();

    /**
     * Undo the linking done in the resolveLink method
     */
    public abstract void unresolveLink();
}
