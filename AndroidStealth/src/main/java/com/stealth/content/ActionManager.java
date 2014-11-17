package com.stealth.content;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.view.ActionMode;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedFolder;
import com.stealth.files.IndexedItem;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import com.stealth.encryption.EncryptionManager;
import com.stealth.encryption.IContentManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by Alex on 13-4-2014.
 */
public class ActionManager {

	private ActionMode mActionMode;
	private IContentManager mContentManager;
	private EncryptionManager mEncryptionManager;

	public ActionManager(IContentManager contentManager) {
		mContentManager = contentManager;
	}

	/**
	 * Sets the EncryptionManager used to control item encryption
	 *
	 * @param encryptionManager
	 */
	public void setEncryptionManager(EncryptionManager encryptionManager) {
		mEncryptionManager = encryptionManager;
	}

	/**
	 * Passes the actionMode to this object to finalize when actions are completed
	 *
	 * @param actionMode
	 */
	public void setActionMode(ActionMode actionMode) {
		mActionMode = actionMode;
	}

	/**
	 * Removes all given items from the application index
	 *
	 * @param with     The items to remove
	 * @param listener a listener which is called with a result when the task has been completed
	 */
	public void actionShred(ArrayList<IndexedItem> with, IOnResult<Boolean> listener) {
		mContentManager.removeItems(with, getWrapper(listener, 0));
	}

	/**
	 * Opens the selected file through an intent based on the file's mimetype
	 *
	 * @param activity activity used to launch the intent
	 * @param with     the item to open
	 * @param listener a listener which is called with a result when the task has been completed
	 */
	public void actionOpen(HomeActivity activity, IndexedItem with, IOnResult<Boolean> listener) {
		if (!(with instanceof IndexedFile)) {
			return;
		}

		IndexedFile file = (IndexedFile) with;
		Uri uri = Uri.fromFile(file.getUnlockedFile());

		Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);

		MimeTypeMap myMime = MimeTypeMap.getSingleton();
		String mimeType = myMime.getMimeTypeFromExtension(file.getExtension().substring(1));
		newIntent.setDataAndType(uri, mimeType);
		newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);

		try {
			activity.setRequestedActivity(true);
			activity.startActivity(newIntent);
		}
		catch (android.content.ActivityNotFoundException e) {
			Toast.makeText(activity, "No handler for file " + file.getUnlockedFilename(), 4000).show();
			activity.setRequestedActivity(false);
		}

		finishActionMode();

		if (listener != null) {
			listener.onResult(true);
		}
	}

	/**
	 * Locks the given files
	 *
	 * @param with     Files to lock
	 * @param listener a listener which is called with a result when the task has been completed
	 */
	public void actionLock(ArrayList<IndexedItem> with, final IOnResult<Boolean> listener) {
		if (mEncryptionManager == null) {
			Utils.d("Called lock when encryption service not bound!");
			return;
		}

		for (IndexedItem item : with) {
			if (item instanceof IndexedFile) {
				// clear the thumbnails because if we lock the file
				// it might have changed
				((IndexedFile) item).clearThumbnail();
			}
		}

		mEncryptionManager.encryptItems(with, getWrapper(listener, 0));
	}

	/**
	 * Unlocks the given files
	 *
	 * @param with     Files to unlock
	 * @param listener a listener which is called with a result when the task has been completed
	 */
	public void actionUnlock(ArrayList<IndexedItem> with, final IOnResult<Boolean> listener) {
		if (mEncryptionManager == null) {
			Utils.d("Called unlock when encryption service not bound!");
			return;
		}

		mEncryptionManager.decryptItems(with, getWrapper(listener, 0));
	}

	/**
	 * Shares the content with an intent
	 *
	 * @param activity The context needed to launch the intent
	 * @param with     The item(s) to share
	 * @param listener a listener which is called with a result when the task has been completed
	 */
	public void actionShare(HomeActivity activity, ArrayList<IndexedItem> with, IOnResult<Boolean> listener) {
		Intent shareIntent;

		if (with.size() == 1 && with.get(0) instanceof IndexedFile) {
			shareIntent = getShareIntentSingle((IndexedFile) with.get(0));
		}
		else {
			shareIntent = getShareIntentMultiple(with);
		}

		activity.setRequestedActivity(true);

		boolean activityFound = true;
		try {
			activity.startActivity(shareIntent);
		}
		catch (ActivityNotFoundException e) {
			Utils.toast(R.string.share_not_found);
			activityFound = false;
		}

		if (listener != null) {
			listener.onResult(activityFound);
		}
	}

	/**
	 * Creates an intent to share multiple files with
	 *
	 * @param with The files to share
	 * @return share intent
	 */
	private Intent getShareIntentMultiple(ArrayList<IndexedItem> with) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND_MULTIPLE);

		ArrayList<Uri> files = new ArrayList<Uri>();

		ArrayList<String> mimes = new ArrayList<String>();
		MimeTypeMap myMime = MimeTypeMap.getSingleton();

		//TODO fix for folders
		for (IndexedItem item : with) {
			if (item instanceof IndexedFile) {
				IndexedFile file = (IndexedFile) item;
				Uri uri = Uri.fromFile(file.getUnlockedFile());
				String mimeType = myMime.getMimeTypeFromExtension(file.getExtension().substring(1));
				if (!mimes.contains(mimeType)) {
					mimes.add(mimeType);
				}
				files.add(uri);
			}
		}

		StringBuilder sb = new StringBuilder();
		for (String mime : mimes) {
			sb.append(mime);
			sb.append(',');
		}

		sb.deleteCharAt(sb.length() - 1);

		String mimeType = sb.toString();
		Utils.d("Mimetype: " + mimeType);
		intent.setType(mimeType);

		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);

		return intent;
	}

	/**
	 * Gets the intent to share a single IndexedFile with
	 *
	 * @param with the file to share
	 * @return share intent
	 */
	private Intent getShareIntentSingle(IndexedFile with) {

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);

		MimeTypeMap map = MimeTypeMap.getSingleton();
		String mimeType = map.getMimeTypeFromExtension(with.getExtension().substring(1));
		intent.setType(mimeType);

		Uri uri = Uri.fromFile(with.getUnlockedFile());
		intent.putExtra(Intent.EXTRA_STREAM, uri);

		return intent;
	}

	/**
	 * Restores all items to their original location, and removes all successfully restored items from the file index
	 *
	 * @param with      the files to restore and remove from the index
	 * @param listener  called with a false when no files have been restored and removed, and with the removal result
	 *                  otherwise
	 * @param exportDir folder to export the files to
	 */
	public void actionRestore(final ArrayList<IndexedItem> with, final IOnResult<Boolean> listener,
			final File exportDir) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<IndexedItem> restoredItems = new ArrayList<IndexedItem>();
				for (IndexedItem item : with) {
					if (restoreItem(item, exportDir)) {
						restoredItems.add(item);
					}
				}

				if (restoredItems.size() == 0) {
					Utils.toast(R.string.action_no_restore);
					if (listener != null) {
						listener.onResult(false);
					}
				}

				boolean allFilesRestored = restoredItems.size() == with.size();

				mContentManager.removeItems(restoredItems, getWrapper(listener,
						allFilesRestored ? R.string.action_restored_items : R.string.action_partial_restore));
			}
		}).start();
	}

	/**
	 * Restores a single item to its original position
	 *
	 * @param item
	 * @return whether the item has been successfully restored
	 */
	private boolean restoreItem(IndexedItem item, File exportDir) {
		boolean success = true;
		if (item instanceof IndexedFolder) {
			IndexedFolder folder = (IndexedFolder) item;
			for (IndexedFolder f : folder.getFolders()) {
				success &= restoreItem(f, exportDir);
			}
			for (IndexedFile f : folder.getFiles()) {
				success &= restoreItem(f, exportDir);
			}

			return success;
		}
		else if (item instanceof IndexedFile) {
			IndexedFile file = (IndexedFile) item;
			File original = new File(exportDir, file.getName() + file.getExtension());
			File unlocked = file.getUnlockedFile();

			if (!unlocked.exists()) {
				return false;
			}
			else {
				original.delete();
				file.removeModificationChecker();

				try {
					original.createNewFile();

					copyFile(unlocked, original);
				}
				catch (IOException e) {
					return false;
				}

				Utils.d("Restored file to location " + original.getPath());

				return true;
			}
		}

		return false;
	}

	/**
	 * Copy the source content to the destination content
	 *
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	private void copyFile(File src, File dst) throws IOException {
		FileInputStream inStream = new FileInputStream(src);
		FileOutputStream outStream = new FileOutputStream(dst);
		FileChannel inChannel = inStream.getChannel();
		FileChannel outChannel = outStream.getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		}
		finally {
			inStream.close();
			outStream.close();
		}
	}

	/**
	 * Closes the action mode if it's active
	 */
	private void finishActionMode() {
		if (mActionMode != null) {
			Utils.runOnMain(new Runnable() {
				@Override
				public void run() {
					if (mActionMode != null) {
						mActionMode.finish();
					}
				}
			});
		}
	}

	/**
	 * Returns a wrapper which disables the action mode when the result comes back, and calls the original wrapper
	 * after
	 *
	 * @param listener
	 * @param toastResID
	 * @return
	 */
	private IOnResult<Boolean> getWrapper(final IOnResult<Boolean> listener, final int toastResID) {
		return new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean result) {
				if (result && toastResID != 0) {
					Utils.toast(toastResID);
				}

				if (listener != null) {
					listener.onResult(result);
				}
			}
		};
	}
}
