package com.stealth.content;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.files.IndexedFile;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;

/**
 * This class performs the necessary steps to create/read and de/encrypt thumbnails for images
 * Created by OlivierHokke on 06-Apr-14.
 */
public class ThumbnailManager {

	private static final int POOL_SIZE = 1;
	private static HashSet<IndexedFile> sRetrievingThumbs = new HashSet<IndexedFile>();
	private static HashSet<IndexedFile> sCreatingThumbs = new HashSet<IndexedFile>();
	private static ExecutorService sExecutorService;

	/**
	 * Get the executor service to run
	 * @return
	 */
	private static ExecutorService getExecutorService() {
		if (sExecutorService == null || sExecutorService.isShutdown() || sExecutorService.isTerminated()) {
			sExecutorService = Executors.newFixedThreadPool(POOL_SIZE);
		}
		return sExecutorService;
	}

	/**
	 * Is the manager currently creating a thumbnail for the given file?
	 * @param item the file to check
	 * @return if thumbnail is being created
	 */
	public static boolean isCreating(IndexedFile item) {
		return sCreatingThumbs.contains(item);
	}

	/**
	 * Creates the thumbnail for an item, encrypts it and saves it in the thumbnail folder
	 * @param item the file to generate the thumbnail of
	 */
	public static void createThumbnail(final IndexedFile item, final IOnResult<Boolean> callback) {

		if (sCreatingThumbs.contains(item)) {
			// we are already in the process of retrieving the thumbnail.
			callback.onResult(false);
			return;
		}

		final IOnResult<Boolean> onResult = new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean result) {
				sCreatingThumbs.remove(item);
				callback.onResult(result);
			}
		};

		sCreatingThumbs.add(item);

		getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				boolean result = false;

				if (!item.isThumbnailable()) {
					onResult.onResult(false);
					return;
				}

				try {

					// generate thumbnail
					Bitmap thumb = FileUtils.getThumbnail(Utils.getContext(), item.getUnlockedFile());

					if (thumb == null) {
						onResult.onResult(false);
						return;
					}

					// save thumbnail to cache
					File cache = Utils.getRandomCacheFile(".jpg");
					FileOutputStream out = new FileOutputStream(cache);
					thumb.compress(Bitmap.CompressFormat.JPEG, 90, out);
					out.close();

					// encrypt
					File thumbFile = item.getThumbFile();
					Utils.getMainCrypto().encrypt(thumbFile, cache, item.getName());
					Utils.delete(cache);

					item.setThumbnail(thumb);

					result = true;

				} catch (Exception e) {

					Utils.d("Failed to create thumbnail of item '" + item.getName() + "'. " + e.getMessage());
					e.printStackTrace();

				}

				onResult.onResult(result);
			}
		});
	}

	/**
	 * Gets the encrypted thumbnail of an item, decrypts it and sets it in the item, but
	 * only if thumbnail was not already set.
	 * @param item the file to generate the thumbnail of
	 * @param callback the callback to notify whether it succeeded
	 */
	public static void retrieveThumbnail(final IndexedFile item, final IOnResult<Boolean> callback) {

		if (item.getThumbnail() != null) {
			// bitmap is remembered, so nothing to retrieve
			callback.onResult(true);
			return;
		}

		if (sRetrievingThumbs.contains(item)) {
			// we are already in the process of retrieving the thumbnail.
			callback.onResult(false);
			return;
		}

		final File thumbFile = item.getThumbFile();
		if (!thumbFile.exists()) {
			// there is no thumbnail to retrieve
			callback.onResult(false);
			return;
		}

		sRetrievingThumbs.add(item);

		getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				boolean result = false;

				// just a precaution: wait until creation of thumbnail is done
				while (sCreatingThumbs.contains(item)) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// this is fine, just keep waiting until thumbnail is created
					}
				}

				try {

					// decrypt
					File thumbFile = item.getThumbFile();
					File cache = Utils.getRandomCacheFile();
					Utils.getMainCrypto().decrypt(thumbFile, cache, item.getName());

					// read the bitmap
					Bitmap bm = BitmapFactory.decodeFile(cache.getPath());
					Utils.delete(cache);

					item.setThumbnail(bm);
					result = true;

				} catch (Exception e) {

					Utils.d("Failed to read thumbnail of item '" + item.getName() + "'. " + e.getMessage());
					e.printStackTrace();

				}

				sRetrievingThumbs.remove(item); // remember that we are done retrieving
				Utils.runCallbackOnMain(callback, result);
			}
		});
	}
}
