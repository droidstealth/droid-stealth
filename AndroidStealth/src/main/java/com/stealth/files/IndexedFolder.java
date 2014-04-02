package com.stealth.files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This class represents a folder in our internal indexing system.
 * Created by OlivierHokke on 4/1/14.
 */
public class IndexedFolder extends IndexedItem {
    public static final int FOLDER_PARENT = 1;
    public static final int FOLDER_NAME = 2;

    private String mTempLinkUID;

    private IndexedFolder mParent;
    private String mName;

    private ArrayList<IndexedFolder> mFolders = new ArrayList<IndexedFolder>();
    private ArrayList<IndexedFile> mFiles = new ArrayList<IndexedFile>();

    public JSONArray makeRaw() {
        JSONArray raw = super.makeRaw();
        if (mParent != null) raw.put(mParent.getUID());
        else raw.put(JSONObject.NULL);
        raw.put(mName);
        return raw;
    }

    IndexedFolder(JSONArray raw) {
        super(raw);
        mTempLinkUID = raw.optString(FOLDER_PARENT);
        mName = raw.optString(FOLDER_NAME);
    }

    /**
     * Create a new virtual folder
     * @param mParent the virtual folder this folder is in
     * @param mName the name of the folder virtually
     */
    public IndexedFolder(IndexedFolder mParent, String mName) {
        super();
        this.mParent = mParent;
        this.mName = mName;
        conclude();
    }

    public void addFolder(IndexedFolder folder) {
        mFolders.add(folder);
    }

    public void removeFolder(IndexedFolder folder) {
        mFolders.remove(folder);
    }

    public ArrayList<IndexedFolder> getFolders() {
        return mFolders;
    }

    public void addFile(IndexedFile file) {
        mFiles.add(file);
    }

    public void removeFile(IndexedFile file) {
        mFiles.remove(file);
    }

    public ArrayList<IndexedFile> getFiles() {
        return mFiles;
    }

    @Override
    public void resolveLink() {
        if (mTempLinkUID != null) {
            setParent(FileIndex.get().getFolder(mTempLinkUID));
            mTempLinkUID = null;
        }
    }

    @Override
    public void unresolveLink() {
        if (mParent != null) {
            mParent.removeFolder(this);
        }
    }

    public boolean hasParent() {
        return mTempLinkUID == null && mParent == null;
    }

    public IndexedFolder getParent() {
        return mParent;
    }

    public String getName() {
        return mName;
    }

    /**
     * Set the name of this folder.
     * Only the FileIndex can do this.
     * @param mName the new name
     */
    void setName(String mName) {
        this.mName = mName;
    }

    /**
     * Set the parent of this folder.
     * Only the FileIndex can do this.
     * @param mParent the new parent
     */
    void setParent(IndexedFolder mParent) {
        this.mParent = mParent;
        if (mParent != null) {
            mParent.addFolder(this);
        }
    }
}
