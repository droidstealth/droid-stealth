package content;

import static content.ConcealCrypto.CryptoMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.facebook.crypto.cipher.NativeGCMCipherException;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.R;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;

/**
 * ContentManager which copies the files to the local data directory Created by Alex on 13-3-14.
 */
public class ContentManager implements IContentManager {
	private ConcealCrypto crypto;
	private File mDataDir;
	private File mThumbDir;
	private List<ContentChangedListener> mListeners = new ArrayList<ContentChangedListener>();

	public ContentManager(Context context) {
		mDataDir = context.getExternalFilesDir(null);
		crypto = new ConcealCrypto(context);
		mThumbDir = new File(mDataDir, "_thumbs");
		mThumbDir.mkdir();
	}

	@Override
	public Collection<ContentItem> getStoredContent() {
		File[] files = mDataDir.listFiles();
		ArrayList<ContentItem> itemArrayList = new ArrayList<ContentItem>();

		for (File file : files) {
			itemArrayList.add(new ContentItem(file, file.getName()));
		}

		return itemArrayList;
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

	/**
	 * Gets the thumbnail of a file
	 *
	 * @param item the file to find the thumbnail file of
	 * @return the thumbnail file
	 */
	public File getThumbnailFile(File item) {
		return new File(mThumbDir, item.getName() + ".jpg");
	}

	/**
	 * Creates the thumbnail for an item and saves it in the thumbnail folder
	 *
	 * @param item the file to generate the thumbnail of
	 * @return the created thumbnail
	 */
	public File createThumbnail(File item) {
		try {
			Bitmap thumb = FileUtils.getThumbnail(Utils.getContext(), item);
			if (thumb == null) {
				return null;
			}
			File thumbFile = getThumbnailFile(item);
			FileOutputStream out = new FileOutputStream(thumbFile);
			thumb.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.close();
			return thumbFile;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void addItem(final File item, final IOnResult<Boolean> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// init
				File target = new File(mDataDir, item.getName());
				File thumb = null;
				String mimeType = FileUtils.getMimeType(item);
				boolean isMedia = FileUtils.isImageOrVideo(mimeType);

				try {
					// copy to our folder
					copyFile(item, target);

					// create thumbnail
					thumb = createThumbnail(target);
					if (isMedia && thumb == null) {
						Utils.toast(R.string.content_fail_thumb);
					}

					// delete original
					boolean removed = Utils.delete(item);
					if (!removed) {
						Utils.toast(R.string.content_fail_original_delete);
					}

					// notify that we are done
					notifyListeners();
					if (callback != null) {
						callback.onResult(true);
					}
				}
				catch (IOException e) {
					e.printStackTrace();

					// cleanup
					if (target.exists() && !Utils.delete(target)) {
						Utils.toast(R.string.content_fail_clean);
					}
					if (thumb != null && thumb.exists() && !Utils.delete(thumb)) {
						Utils.toast(R.string.content_fail_clean);
					}

					// notify that we are done
					if (callback != null) {
						callback.onResult(false);
					}
				}
			}
		}).start();
	}


	@Override
	public void removeAllContent(final IOnResult<Boolean> callback) {
		removeItems(getStoredContent(), callback);
	}

	@Override
	public void removeItem(final ContentItem item, final IOnResult<Boolean> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean removed = removeItemNow(item);
				if (removed) {
					notifyListeners();
				}
				if (callback != null) {
					callback.onResult(removed);
				}
			}
		}).start();
	}

	@Override
	public void removeItems(Collection<ContentItem> itemCollection, final IOnResult<Boolean> callback) {
		// make copy in case it changes while we are executing in another thread
		final ContentItem[] items = itemCollection.toArray(
				(ContentItem[]) java.lang.reflect.Array.newInstance(ContentItem.class, itemCollection.size()));
		new Thread(new Runnable() {
			@Override
			public void run() {
				int failures = 0;
				boolean singleSuccess = false;
				for (ContentItem item : items) {
					if (removeItemNow(item)) {
						singleSuccess |= true;
					}
					else {
						failures++;
					}
				}
				if (failures > 0) {
					Utils.toast(Utils.str(R.string.content_fail_delete).replace("{COUNT}", "" + failures));
				}
				if (singleSuccess) {
					notifyListeners();
				}

				if (callback != null) {
					callback.onResult(failures == 0);
				}
			}
		}).start();
	}

	/**
	 * Removes a file completely right now in current thread, including its thumbnail and encrypted version
	 *
	 * @param contentItem the file to remove
	 * @return whether it completely succeeded
	 */
	public boolean removeItemNow(ContentItem contentItem) {
		File file = contentItem.getFile();
		File thumb = getThumbnailFile(file);
		boolean success = true;
		// TODO remove encrypted files
		// TODO make sure file is removed from any queue etc
		if (thumb.exists()) {
			success &= Utils.delete(thumb);
		}
		success &= Utils.delete(file);
		return success;
	}

	/**
	 * Helper function to copy a file internally
	 *
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	private static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	/**
	 * Encrypts all files in the {@param contentItemCollection}. Deletes the original file after encrypting them.
	 *
	 * @return true if ALL files are encrypted successfully, false otherwise.
	 */
	@Override
	public boolean encryptItems(Collection<ContentItem> contentItemCollection, EncryptionService service) {
		boolean success = true;

		for (ContentItem contentItem : contentItemCollection) {
			success = encryptItem(contentItem, service) && success;
		}

		if (success) {
			Log.i(this.getClass().toString(), "Encrypted items:");
		}
		else {
			Log.e(this.getClass().toString(), "Encrypted with errors:");
		}
		for (ContentItem contentItem : contentItemCollection) {
			Log.e(this.getClass().toString(), contentItem.getFile().getAbsolutePath());
		}

		return success;
	}

	private boolean encryptItem(ContentItem contentItem, EncryptionService service) {

		try {
			Log.d(this.getClass().toString() + ".encryptItem",
					"Encrypting file " + contentItem.getFile().getAbsolutePath());
			File encryptedFile = new File(mDataDir + "/" + contentItem.getFileName() + ".CRYPT");
			encryptedFile.createNewFile();

			Future taskFuture = service.addCryptoTask(encryptedFile, contentItem.getFile(), encryptedFile.getName(),
					CryptoMode.ENCRYPT);

			taskFuture.get(1, TimeUnit.MINUTES);

			notifyListeners();

			return true;
		}
		catch (IOException e) {
			Log.e(this.getClass().toString() + ".encryptItem", "Error in encrypting data", e);
		}
		catch (InterruptedException e) {
			Log.e(this.getClass().toString() + ".encryptItem", "Interrupted while encrypting", e);
		}
		catch (ExecutionException e) {
			Log.e(this.getClass().toString() + ".encryptItem", "Exception while executing encryption", e);
		}
		catch (TimeoutException e) {
			Log.e(this.getClass().toString() + ".encryptItem", "Timed out while waiting for encryption", e);
		}

		return false;
	}

	/**
	 * Decrypts all files in the {@param contentItemCollection}. Deletes the encrypted files after decrypting them.
	 *
	 * @return true if ALL files are decrypted successfully, false otherwise.
	 */
	@Override
	public boolean decryptItems(Collection<ContentItem> contentItemCollection, EncryptionService service) {
		boolean success = true;

		for (ContentItem contentItem : contentItemCollection) {
			success = decryptItem(contentItem, service) && success;
		}

		if (success) {
			Log.i(this.getClass().toString() + ".decryptItems", "Decrypted items:");
		}
		else {
			Log.w(this.getClass().toString() + ".decryptItems", "Decrypted with errors:");
		}
		for (ContentItem contentItem : contentItemCollection) {
			System.out.println("\t" + contentItem.getFileName());
		}

		return success;
	}

	public boolean decryptItem(ContentItem contentItem, EncryptionService service) {
		try {
			// Remove .CRYPT from filename
			String filename = mDataDir + "/" + contentItem.getFileName();
			filename = filename.substring(0, filename.length() - 6);

			// Create target file
			File decryptedFile = new File(filename);
			decryptedFile.createNewFile();

			Future taskFuture = service.addCryptoTask(contentItem.getFile(), decryptedFile, decryptedFile.getName(),
					CryptoMode.DECRYPT);

			taskFuture.get(1, TimeUnit.MINUTES);

			notifyListeners();

			return true;
		}
		catch (IOException e) {
			if (e instanceof NativeGCMCipherException) {
				Log.e(this.getClass().toString() + ".decryptItem", "Error in decrypting data", e);
				contentItem.getFile().delete();
			}
			else {
				Log.e(this.getClass().toString() + ".decryptItem", "Error in decrypting data", e);
			}
		}
		catch (InterruptedException e) {
			Log.e(this.getClass().toString() + ".decryptItem", "Interrupted while decrypting", e);
		}
		catch (ExecutionException e) {
			Log.e(this.getClass().toString() + ".decryptItem", "Exception while executing decryption", e);
		}
		catch (TimeoutException e) {
			Log.e(this.getClass().toString() + ".decryptItem", "Timed out while waiting for decryption", e);
		}

		return false;
	}

	/**
	 * Notifies all listeners of a change in content. Tries to do it on the UI thread!
	 */
	private void notifyListeners() {
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
			listener.contentChanged();
		}
	}
}
