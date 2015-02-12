package com.stealth.files;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import com.stealth.preferences.EncryptedPreferences;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides all the functionality to manage all files in the app. It virtually has folders and files with
 * nice names, but in real the files are all stored in the same physical folder with not-so-nice names (UUID's) Created
 * by OlivierHokke on 4/1/14.
 */
public class FileIndex {
	private static final String PREFERENCES_KEY_FAKE = "fileIndexFake";
	private static final String PREFERENCES_KEY_REAL = "fileIndexReal";
	private static final String JSON_FILES = "files";
	private static final String JSON_FOLDERS = "folders";

	public static FileIndex sInstance;
	private EncryptedPreferences mPreferences;
	private JSONObject mIndex;
	private HashMap<String, IndexedFile> mFiles;
	private HashMap<String, IndexedFolder> mFolders;
	private IndexedFolder mRoot;
	private JSONArrayUnsorted mFilesArray;
	private JSONArrayUnsorted mFoldersArray;
	/**
	 * All listeners
	 */
	private ArrayList<OnFileIndexChangedListener> mListeners = new ArrayList<OnFileIndexChangedListener>();

	/**
	 * Privately create the file index instance and retrieve the index from the preferences
	 *
	 * @param fake     whether the fake index should be used instead of the real one
	 * @param callback the callback that wants to be notified when the fileIndex is ready
	 */
	private FileIndex(boolean fake, final IOnResult<FileIndex> callback) {
		IOnResult<EncryptedPreferences> onResult = new IOnResult<EncryptedPreferences>() {
			@Override
			public void onResult(EncryptedPreferences result) {
				Utils.d("Created the EncryptedPreferences");
				mPreferences = result;
				mIndex = result.getJson();

				try {
					if (mIndex.has(JSON_FILES) && mIndex.has(JSON_FOLDERS)) {
						Utils.d("Index exists.. let's read and parse");
						mFilesArray = new JSONArrayUnsorted(mIndex.getJSONArray(JSON_FILES));
						mFoldersArray = new JSONArrayUnsorted(mIndex.getJSONArray(JSON_FOLDERS));
						parseJSON();
					}
					else // we are starting from scratch apparently
					{
						Utils.d("Index does not exist.. let's create, parse and make the root");
						mFilesArray = new JSONArrayUnsorted(new JSONArray());
						mFoldersArray = new JSONArrayUnsorted(new JSONArray());
						mIndex.put(JSON_FILES, mFilesArray.getManagedArray());
						mIndex.put(JSON_FOLDERS, mFoldersArray.getManagedArray());
						parseJSON();
						mRoot = new IndexedFolder(null, "root");
						addFolder(mRoot);
					}
				}
				catch (JSONException e) {
					Log.e(Utils.tag(this), "Oh hey, not workin something", e);
				}

				callback.onResult(FileIndex.this);
			}
		};

		EncryptedPreferences.get(fake ? PREFERENCES_KEY_FAKE : PREFERENCES_KEY_REAL, onResult);
	}

	/**
	 * Create the FileIndex singleton instance
	 *
	 * @param fake     whether the fake index should be used instead of the real one
	 * @param callback the callback that wants to be notified when the fileIndex is ready
	 */
	public static void create(boolean fake, IOnResult<FileIndex> callback) {
		if (sInstance == null) {
			sInstance = new FileIndex(fake, callback);
		}
		else {
			callback.onResult(sInstance);
		}
	}

	/**
	 * @return the file index singleton instance
	 */
	public static FileIndex get() {
		return sInstance;
	}

	public void parseJSON() {
		mFolders = new HashMap<String, IndexedFolder>();
		mFiles = new HashMap<String, IndexedFile>();

		// read all folders
		for (int i = 0; i < mFoldersArray.length(); i++) {
			try {
				IndexedFolder f = new IndexedFolder(mFoldersArray.getJSONArray(i));
				f.setJsonID(i);
				mFolders.put(f.getUID(), f);
				if (!f.hasParent()) {
					mRoot = f;
				}
			}
			catch (JSONException e) {
			}
		}

		// read all files
		for (int i = 0; i < mFilesArray.length(); i++) {
			try {
				IndexedFile f = new IndexedFile(mFilesArray.getJSONArray(i));
				f.setJsonID(i);
				mFiles.put(f.getUID(), f);
			}
			catch (JSONException e) {
			}
		}

		// resolve all folder links
		for (Map.Entry<String, IndexedFolder> entry : mFolders.entrySet()) {
			entry.getValue().resolveLink();
		}

		// resolve all file links
		for (Map.Entry<String, IndexedFile> entry : mFiles.entrySet()) {
			entry.getValue().resolveLink();
		}
	}

	/**
	 * @return the root folder where all our content is
	 */
	public IndexedFolder getRoot() {
		return mRoot;
	}

	/**
	 * Get a folder by its UID
	 *
	 * @param uid the unique ID of the folder
	 * @return the folder if it was found. Otherwise null
	 */
	public IndexedFolder getFolder(String uid) {
		return mFolders.get(uid);
	}

	/**
	 * Get a folder by its jsonArray
	 *
	 * @param jsonArray the jsonArray of the folder
	 * @return the folder if it was found. Otherwise null
	 */
	public IndexedFolder getFolder(JSONArray jsonArray) {
		try {
			return getFolder(jsonArray.getString(IndexedItem.FOLDER_UID));
		}
		catch (JSONException e) {
			Log.e(Utils.tag(this), "hm?", e);
			return null;
		}
	}

	/**
	 * Get a file by its UID
	 *
	 * @param uid the unique ID of the file
	 * @return the file if it was found. Otherwise null
	 */
	public IndexedFile getFile(String uid) {
		return mFiles.get(uid);
	}

	/**
	 * Get a file by its jsonArray
	 *
	 * @param jsonArray the jsonArray of the file
	 * @return the file if it was found. Otherwise null
	 */
	public IndexedFile getFile(JSONArray jsonArray) {
		try {
			return getFile(jsonArray.getString(IndexedItem.FOLDER_UID));
		}
		catch (JSONException e) {
			Log.e(Utils.tag(this), "hm?", e);
			return null;
		}
	}

	/**
	 * Saves changes by committing the preferences file and notifying our listeners.
	 */
	public void saveChanges() {
		mPreferences.commit();
		onFileIndexChanged();
	}

	/**
	 * Adds a file to the index
	 *
	 * @param file the file to add
	 */
	public void addFile(IndexedFile file) {
		file.setJsonID(mFilesArray.length());
		mFilesArray.put(file.getRaw());
		mFiles.put(file.getUID(), file);
		saveChanges();
	}

	/**
	 * Adds a virtual folder to the index
	 *
	 * @param folder the virtual folder to add
	 */
	public void addFolder(IndexedFolder folder) {
		folder.setJsonID(mFoldersArray.length());
		mFoldersArray.put(folder.getRaw());
		mFolders.put(folder.getUID(), folder);
		saveChanges();
	}

	/**
	 * Removes the given folder from the index and recursively removes its contents
	 *
	 * @param folder the folder to remove from the index
	 */
	public void removeFolder(IndexedFolder folder) {
		// remove recursively
		for (IndexedFolder f : folder.getFolders()) {
			removeFolder(f);
		}
		for (IndexedFile f : folder.getFiles()) {
			removeFile(f);
		}

		if (folder != mRoot) // we can never remove the root!
		{
			int i = folder.getJsonID();
			JSONArray movedItem = (JSONArray) mFoldersArray.remove(i).getMovedElement();
			if (movedItem != null) {
				getFolder(movedItem).setJsonID(i); // remember its new position
			}

			mFolders.remove(folder.getUID());
			folder.unresolveLink();
		}
		saveChanges();
	}

	/**
	 * Removes the given file from the index
	 *
	 * @param file the file to remove from the index
	 */
	public void removeFile(IndexedFile file) {
		int i = file.getJsonID();
		Object movedItem = mFilesArray.remove(i).getMovedElement();
		if (movedItem != null && movedItem instanceof JSONArray) {
			getFile((JSONArray)movedItem).setJsonID(i); // remember its new position
		}

		mFiles.remove(file.getUID());
		file.unresolveLink();

		saveChanges();
	}

	/**
	 * Register a new listener
	 *
	 * @param listener the listener
	 */
	public void registerListener(OnFileIndexChangedListener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	/**
	 * Unregister a registered listener
	 *
	 * @param listener the listener
	 */
	public void unregisterListener(OnFileIndexChangedListener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	/**
	 * Notify all listeners that the file index has changed.
	 */
	private void onFileIndexChanged() {
		Utils.runOnMain(new Runnable() {
			@Override
			public void run() {
				for (OnFileIndexChangedListener listener : mListeners) {
					if (listener != null) {
						listener.onFileIndexChanged();
					}
				}
			}
		});
	}

	/**
	 * Searches the complete file index for unlocked files.
	 *
	 * @return true if a file is currently unlocked
	 */
	public ArrayList<IndexedItem> getUnlockedFiles() {
		ArrayList<IndexedItem> unlocked = new ArrayList<IndexedItem>();
		for (Map.Entry<String, IndexedFolder> folderMap : mFolders.entrySet()) {
			for (IndexedFile file : folderMap.getValue().getFiles()) {
				if (file.getUnlockedFile().exists()) {
					unlocked.add(file);
				}
			}
		}
		return unlocked;
	}

	/**
	 * Searches the complete file index for unlocked files.
	 *
	 * @return true if a file is currently unlocked
	 */
	public boolean hasUnlockedFiles() {
		for (Map.Entry<String, IndexedFolder> folderMap : mFolders.entrySet()) {
			if (folderMap.getValue() == null) continue;
			if (folderMap.getValue().getFiles() == null) continue;
			for (IndexedFile file : folderMap.getValue().getFiles()) {
				if (file == null) continue;
				if (file.getUnlockedFile().exists()) {
					return true;
				}
			}
		}
		return false;
	}

	public interface OnFileIndexChangedListener {
		void onFileIndexChanged();
	}
}
