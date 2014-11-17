package com.stealth.content;

import android.content.Context;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.R;
import com.stealth.files.FileIndex;
import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedFolder;
import com.stealth.files.IndexedItem;
import com.stealth.utils.Utils;
import com.stealth.utils.IOnResult;
import com.stealth.encryption.IContentManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ContentManager which copies the files to the local data directory Created by Alex on 13-3-14.
 */
public class ContentManager implements IContentManager {
	private List<ContentChangedListener> mListeners = new ArrayList<ContentChangedListener>();
	private FileIndex mIndex;
	private IndexedFolder mCurrentFolder;

	/**
	 * Create the content manager. Make sure that the file index is created!!
	 * @param context the context of this application
	 * @param index the file index that we will use to manage our content
	 */
	public ContentManager(Context context, FileIndex index) {
		mIndex = index;
		mCurrentFolder = index.getRoot();
	}

	@Override
	public void setCurrentFolder(IndexedFolder currentFolder) {
		mCurrentFolder = currentFolder;
	}

	@Override
	public IndexedFolder getCurrentFolder() {
		return mCurrentFolder;
	}

	@Override
	public Collection<IndexedFile> getFiles(IndexedFolder fromFolder) {
		return fromFolder.getFiles();
	}
	@Override
	public Collection<IndexedFolder> getFolders(IndexedFolder fromFolder) {
		return fromFolder.getFolders();
	}

	@Override
	public void addContentChangedListener(ContentChangedListener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	@Override
	public boolean removeContentChangedListener(ContentChangedListener listener) {
		return mListeners.remove(listener);
	}

	@Override
	public void addFile(final IndexedFolder toFolder, final File originalFile, final IOnResult<IndexedFile> callback)
	{
		if (!originalFile.isFile()) return;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// init
				final IndexedFile indexedFile = new IndexedFile(toFolder, originalFile);
				final File unlockedFile = indexedFile.getUnlockedFile();
				final File thumbFile = indexedFile.getThumbFile();
				final String mimeType = FileUtils.getMimeType(originalFile);
				final boolean isMedia = FileUtils.isImageOrVideo(mimeType);

				try
				{
					// copy to our folder
					Utils.copyFile(originalFile, unlockedFile);

					// create thumbnail
					ThumbnailManager.createThumbnail(indexedFile, new IOnResult<Boolean>() {
						@Override
						public void onResult(Boolean result) {
							if (isMedia && !thumbFile.exists()) Utils.toast(R.string.content_fail_thumb);

							// delete original
							boolean removed = Utils.delete(originalFile);
							if (!removed) Utils.toast(R.string.content_fail_original_delete);

							// add to the index
							mIndex.addFile(indexedFile);

							// notify that we are done
							notifyContentChangedListeners();
							if (callback != null) callback.onResult(indexedFile);
						}
					});
				}
				catch (Exception e)
				{
					// cleanup
					if (unlockedFile.exists() && !Utils.delete(unlockedFile))
						Utils.toast(R.string.content_fail_clean);
					if (thumbFile != null && thumbFile.exists() && !Utils.delete(thumbFile))
						Utils.toast(R.string.content_fail_clean);

					// notify that we are done but failed
					if (callback != null) callback.onResult(null);
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public IndexedFolder getRoot() {
		return mIndex.getRoot();
	}

	@Override
	public void removeAllContent(final IOnResult<Boolean> callback) {
		removeItem(mIndex.getRoot(), callback);
	}

	@Override
	public void removeItem(final IndexedItem item, final IOnResult<Boolean> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean removed = removeItemNow(item);
				if (removed) {
					notifyContentChangedListeners();
				}
				if (callback != null) {
					callback.onResult(removed);
				}
			}
		}).start();
	}

	@Override
	public void removeItems(Collection<IndexedItem> itemCollection, final IOnResult<Boolean> callback) {
		// make copy in case it changes while we are executing in another thread
		final IndexedItem[] items = itemCollection.toArray(
				(IndexedItem[]) java.lang.reflect.Array.newInstance(IndexedItem.class, itemCollection.size()));
		new Thread(new Runnable() {
			@Override
			public void run() {
				int failures = 0;
				boolean singleSuccess = false;
				for (IndexedItem item : items) {
					if (removeItemNow(item)) {
						singleSuccess |= true;
					}
					else {
						failures++;
					}
				}
				if (failures > 0) {
					Utils.d(Utils.str(R.string.content_fail_delete).replace("{COUNT}", "" + failures));
				}
				if (singleSuccess) {
					notifyContentChangedListeners();
				}

				if (callback != null) {
					callback.onResult(failures == 0);
			}
			}
		}).start();
	}

	/**
	 * Removes a file/folder completely in current thread,
	 * including its thumbnail and encrypted version
	 *
	 * @param item the item to remove
	 * @return whether it completely succeeded
	 */
	public boolean removeItemNow(IndexedItem item)
	{
		boolean success = true;
		if (item instanceof IndexedFolder)
		{
			IndexedFolder folder = (IndexedFolder) item;
			for (IndexedFolder f : folder.getFolders()) success &= removeItemNow(f);
			for (IndexedFile f : folder.getFiles()) success &= removeItemNow(f);
			mIndex.removeFolder(folder);
		}
		else
		{
			IndexedFile file = (IndexedFile) item;
			success &= Utils.delete(file.getLockedFile());
			success &= Utils.delete(file.getUnlockedFile());
			success &= Utils.delete(file.getThumbFile());
			mIndex.removeFile(file);
		}
		return success;
	}

	/**
	 * Notifies all listeners of a change in content. Tries to do it on the UI thread!
	 */
	public void notifyContentChangedListeners() {
		Utils.runOnMain(new Runnable() {
			@Override
			public void run() {
				notifyListenersNow();
			}
		});
	}

	/**
	 * Notifies all listeners of a change in content as we speak.
	 */
	private void notifyListenersNow() {
		for (ContentChangedListener listener : mListeners) {
			if (listener != null) {
				listener.contentChanged();
			}
		}
	}
}
