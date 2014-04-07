package com.stealth.files;

import android.content.Context;
import android.content.SharedPreferences;

import com.stealth.utils.Utils;

import java.io.File;

/**
 * This class manages all our folders of the app. It also allows us to change to a different one.
 * In turn, preferences from the new folder will be read and so the app could start from a
 * different folder, showing different content.
 * TODO: keys of folders might differ. Should we save them in the prefs folder.... ? could bring forth serious security issues
 * Created by OlivierHokke on 3/31/14.
 */
public class DirectoryManager
{
	private static final String SHARED_PREFS_KEY = "contentManager";
	private static final String FOLDER_MAIN_KEY = "mainFolder";
	public static final String FOLDER_THUMBS = "Thumbs";
	public static final String FOLDER_FILES = "Files";
	public static final String SUBFOLDER_LOCKED = "Locked";
	public static final String SUBFOLDER_UNLOCKED = "Unlocked";
	public static final String FOLDER_PREFS= "Preferences";

	private File mMainDir;
	private File mPrefsDir;
	private File mFilesDir;
	private File mLockedDir;
	private File mUnlockedDir;
	private File mThumbDir;

	private static DirectoryManager sInstance;

	/**
	 * Get the singleton instance of the directory manager. Make sure that a context has been set
	 * in the utils class.
	 * @return the directory manager
	 */
	public static DirectoryManager get() {
		if (sInstance == null) {
			sInstance = new DirectoryManager();
		}
		return sInstance;
	}

	private DirectoryManager()
	{
		Context context = Utils.getContext();
		SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
		String mainFolder = prefs.getString(FOLDER_MAIN_KEY, context.getExternalFilesDir(null).getPath());

		mMainDir = new File(mainFolder);
		mFilesDir = new File(mMainDir, FOLDER_FILES);
		mThumbDir = new File(mMainDir, FOLDER_THUMBS);
		mPrefsDir = new File(mMainDir, FOLDER_PREFS);
		mLockedDir = new File(mFilesDir, SUBFOLDER_LOCKED);
		mUnlockedDir = new File(mFilesDir, SUBFOLDER_UNLOCKED);

		mThumbDir.mkdir();
		mMainDir.mkdir();
		mFilesDir.mkdir();
		mPrefsDir.mkdir();
		mLockedDir.mkdir();
		mUnlockedDir.mkdir();
	}

	/**
	 * Get the File object of our current Main folder.
	 * @return the file object
	 */
	public static File main() {
		return get().mMainDir;
	}

	/**
	 * Get the File object of our current preferences folder.
	 * @return the file object
	 */
	public static File prefs() {
		return get().mPrefsDir;
	}

	/**
	 * Get the File object of our current files folder where we store
	 * all to-be-hidden files.
	 * @return the file object
	 */
	public static File files() {
		return get().mFilesDir;
	}

	/**
	 * Get the File object of our current locked files folder where we store
	 * all currently hidden files.
	 * @return the file object
	 */
	public static File locked() {
		return get().mLockedDir;
	}

	/**
	 * Get the File object of our current files folder where we store
	 * all unlocked files.
	 * @return the file object
	 */
	public static File unlocked() {
		return get().mUnlockedDir;
	}

	/**
	 * Get the File object of our current files folder where we store
	 * all to-be-hidden files.
	 * @return the file object
	 */
	public static File thumbs() {
		return get().mThumbDir;
	}

	/**
	 * Get the directory to put all files that are to be cached.
	 * @return the file object
	 */
	public static File cache() {
		return Utils.getContext().getCacheDir();
	}

	/**
	 * Sets the main folder for this application. Requires the application to restart
	 * because weird things are going to happen otherwise!!
	 * TODO: make sure nothing is happening in the app currently, otherwise wait untill app closes?
	 * @param context the context to get the preferences from
	 * @param folder the new folder
	 */
	public void setMainFolder(Context context, File folder) throws NoWritingPermissionsException, NotADirectoryException {
		if (folder.isDirectory())
		{
			if (!folder.canWrite()) {
				throw new NoWritingPermissionsException("We don't have writing permissions to save files in this folder");
			}
			SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
			SharedPreferences.Editor e = prefs.edit();
			e.putString(FOLDER_MAIN_KEY, folder.getPath());
			e.commit();

			mMainDir = folder;
		}
		else
		{
			throw new NotADirectoryException("Provided file was not a directory");
		}
	}

	public class NoWritingPermissionsException extends Exception {
		public NoWritingPermissionsException(String message) {
			super(message);
		}
	}
	public class NotADirectoryException extends Exception {
		public NotADirectoryException(String message) {
			super(message);
		}
	}
}
