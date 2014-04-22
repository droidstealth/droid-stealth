package com.stealth.files;

import android.graphics.Bitmap;
import android.util.Log;
import com.stealth.utils.Utils;
import content.ThumbnailManager;
import encryption.EncryptionService;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

/**
 * This class represents a file in our internal indexing system.
 * Created by OlivierHokke on 4/1/14.
 */
public class IndexedFile extends IndexedItem {
	public static final String LOCKED_EXTENSION = ".crypto";
	public static final String THUMB_EXTENSION = ".crypto";
	public static final String MODIFICATION_CHECKER_EXTENSION = ".check";

	public static final int FILE_FOLDER = 1;
	public static final int FILE_NAME = 2;
	public static final int FILE_EXTENSION = 3;
	public static final int FILE_ORIGINAL = 4;

	private String mTempLinkUID;
	private Bitmap mThumb;
	private boolean mRetrievingThumb;

	private IndexedFolder mFolder;
	private String mName;
	private String mExtension;
	private String mOriginal;

	public JSONArray makeRaw() {
		JSONArray raw = super.makeRaw();
		raw.put(mFolder.getUID());
		raw.put(mName);
		raw.put(mExtension);
		raw.put(mOriginal);
		return raw;
	}

	IndexedFile(JSONArray raw) {
		super(raw);
		try {
			mTempLinkUID = raw.getString(FILE_FOLDER);
			mName = raw.getString(FILE_NAME);
			mExtension = raw.getString(FILE_EXTENSION);
			mOriginal = raw.getString(FILE_ORIGINAL);
		} catch (JSONException e) {}
	}

	/**
	 * Create a new file object
	 * @param mFolder the virtual folder this file is in.
	 * @param mName the name of the file virtually
	 * @param mExtension the extension of the file
	 * @param mOriginal the path to the original file
	 */
	public IndexedFile(IndexedFolder mFolder, String mName, String mExtension, String mOriginal) {
		super();
		this.mName = mName;
		this.mExtension = mExtension;
		this.mOriginal = mOriginal;
		setFolder(mFolder);
		conclude();
	}

	/**
	 * Create a new file object
	 * @param mFolder the virtual folder this file is in.
	 * @param originalFile the original file to base this one on
	 */
	public IndexedFile(IndexedFolder mFolder, File originalFile) {
		super();
		String filename = originalFile.getName();
		this.mName = filename.substring(0, filename.lastIndexOf("."));
		this.mExtension = filename.substring(filename.lastIndexOf("."));
		this.mOriginal = originalFile.getAbsolutePath();
		setFolder(mFolder);
		conclude();
	}

	public IndexedFolder getFolder() {
		return mFolder;
	}

	public String getName() {
		return mName;
	}

	@Override
	public void resolveLink() {
		if (mTempLinkUID != null) {
			setFolder(FileIndex.get().getFolder(mTempLinkUID));
			mTempLinkUID = null;
		}
	}

	@Override
	public void unresolveLink() {
		if (mFolder != null) {
			mFolder.removeFile(this);
		}
	}

	/**
	 * @return the filename as it should be in its unlocked state.
	 */
	public String getUnlockedFilename() {
		return getUID() + mExtension;
	}

	/**
	 * @return the filename as it should be in its locked state.
	 */
	public String getLockedFilename() {
		return getUID() + LOCKED_EXTENSION;
	}

	/**
	 * @return the filename of the modification checker when file is unlocked. Used to check for modifications.
	 */
	private String getModificationCheckerFilename() {
		return getUID() + MODIFICATION_CHECKER_EXTENSION;
	}

	/**
	 * @return the filename as it should be in its locked state.
	 */
	public String getThumbFilename() {
		return getUID() + THUMB_EXTENSION;
	}

	/**
	 * @return the file as it should be in its unlocked state.
	 */
	public File getUnlockedFile() {
		return new File(DirectoryManager.unlocked(), getUnlockedFilename());
	}

	/**
	 * @return the filename as it should be in its locked state.
	 */
	public File getLockedFile() {
		return new File(DirectoryManager.locked(), getLockedFilename());
	}

	/**
	 * @return the filename as it should be in its locked state.
	 */
	public File getThumbFile() {
		return new File(DirectoryManager.thumbs(), getThumbFilename());
	}

	/**
	 * @return the filename as it used to be in its original state.
	 */
	public File getOriginalFile() {
		return new File(mOriginal);
	}

	/**
	 * @return the file as it should be in its unlocked state.
	 */
	private File getModificationCheckerFile() {
		return new File(DirectoryManager.unlocked(), getModificationCheckerFilename());
	}

	/**
	 * @return true if file is currently locked
	 */
	public boolean isLocked() {
		return !isProcessing() && !getUnlockedFile().exists() && getLockedFile().exists();
	}

	/**
	 * @return true if file is currently unlocked
	 */
	public boolean isUnlocked() {
		return !isProcessing() && getUnlockedFile().exists() && !getLockedFile().exists();
	}

	/**
	 * @return true if file is currently being processed
	 */
	public boolean isProcessing() {
		return ThumbnailManager.isCreating(this) || EncryptionService.inQueue(this);
	}

	/**
	 * Set the parent virtual folder of this file
	 * Only the FileIndex can do this.
	 * @param mFolder the new folder
	 */
	void setFolder(IndexedFolder mFolder) {
		this.mFolder = mFolder;
		if (mFolder != null) {
			mFolder.addFile(this);
		}
	}

	/**
	 * Set the virtual name of this file
	 * Only the FileIndex can do this.
	 * @param mName the new name
	 */
	void setName(String mName) {
		this.mName = mName;
	}

	/**
	 * Set the extension of this file
	 * Only the FileIndex can do this.
	 * @param mExtension the new file extension
	 */
	void setExtension(String mExtension) {
		this.mExtension = mExtension;
	}

	/**
	 * @return get the remembered thumbnail
	 */
	public Bitmap getThumbnail() {
		return mThumb;
	}

	/**
	 * @param mThumb the thumbnail to remember for this file
	 */
	public void setThumbnail(Bitmap mThumb) {
		this.mThumb = mThumb;
	}

	/**
	 * Clears the thumbnail and sets it to be garbage collected
	 */
	public void clearThumbnail() {
		mThumb = null;
	}

	/**
	 * Creates the file that will allow the check for modifications
	 */
	public void createModificationChecker() {
		File f = getModificationCheckerFile();
		if (f.exists()) f.delete();
		try {
			f.createNewFile();
		} catch (Exception e) {
			Log.d(Utils.tag(), "Couldn't create modification checker.", e);
		}
	}

	/**
	 * Removes the file that will allow the check for modifications
	 */
	public void removeModificationChecker() {
		File f = getModificationCheckerFile();
		if (f.exists()) f.delete();
	}

	/**
	 * Resets the file that will allow the check for modifications to the current
	 * date and time. This will let isModified() return false until a new modification is done.
	 */
	public void resetModificationChecker() {
		createModificationChecker();
	}

	/**
	 * Checks if the unlocked file was modified. If so, returns true.
	 * @return true if unlocked file was modified.
	 */
	public boolean isModified() {
		return isUnlocked() && getModificationCheckerFile().exists()
				&& getUnlockedFile().lastModified() > getModificationCheckerFile().lastModified();
	}
}
