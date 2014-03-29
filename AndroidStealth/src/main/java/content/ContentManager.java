package content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.facebook.crypto.cipher.NativeGCMCipherException;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;

/**
 * ContentManager which copies the files to the local data directory Created by Alex on 13-3-14.
 */
public class ContentManager implements IContentManager {
	private ConcealCrypto crypto;
	private File mDataDir;
	private List<ContentChangedListener> mListeners = new ArrayList<ContentChangedListener>();

	public ContentManager(Context context) {
		mDataDir = context.getExternalFilesDir(null);
		crypto = new ConcealCrypto(context);
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

			Log.d("ContentManager.copyFile", "Copied the file");
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

	private boolean encryptItem(ContentItem contentItem, Context context) {

		try {
			Log.d(this.getClass().toString()+".encryptItem", "Encrypting file " + contentItem.getFile().getAbsolutePath());
			File encryptedFile = new File(mDataDir + "/" + contentItem.getFileName() + ".CRYPT");
			encryptedFile.createNewFile();

			Intent encryptIntent = new Intent(context, EncryptionService.class);
			encryptIntent.putExtra(EncryptionService.UNENCRYPTED_PATH_KEY, contentItem.getFile().getPath());
			encryptIntent.putExtra(EncryptionService.ENCRYPTED_PATH_KEY, encryptedFile.getPath());
			//TODO this is just for testing. Needs better safeguard against filechanges and stuff
			encryptIntent.putExtra(EncryptionService.ENTITY_KEY, encryptedFile.getName());
			encryptIntent.putExtra(EncryptionService.MODE_KEY, ConcealCrypto.CryptoMode.ENCRYPT);

			context.startService(encryptIntent);

			Log.d(this.getClass().toString()+".encryptItem", "Started service!");

			//			crypto.encrypt(encryptedFile, contentItem.getFile(), contentItem.getFileName());

//			return contentItem.getFile().delete();
			return true;
		}
		//		catch (KeyChainException e) {
		//			Log.e(this.getClass().toString() + ".encryptItem", "Error in encrypting data", e);
		//		}
		//		catch (CryptoInitializationException e) {
		//			Log.e(this.getClass().toString() + ".encryptItem", "Error in encrypting data", e);
		//		}
		catch (IOException e) {
			Log.e(this.getClass().toString() + ".encryptItem", "Error in encrypting data", e);
		}

		return false;
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
	public boolean addItem(File item) {
		try {
			copyFile(item, new File(mDataDir, item.getName()));
			notifyListeners();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public boolean removeItem(ContentItem item) {
		boolean removed = item.getFile().delete();
		if (removed) {
			notifyListeners();
		}
		return removed;
	}

	/**
	 * Encrypts all files in the {@param contentItemCollection}. Deletes the original file after encrypting them.
	 *
	 * @return true if ALL files are encrypted successfully, false otherwise.
	 */
	@Override
	public boolean encryptItems(Collection<ContentItem> contentItemCollection, Context context) {
		boolean success = true;

		for (ContentItem contentItem : contentItemCollection) {
			success = encryptItem(contentItem, context) && success;
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

		notifyListeners();

		return success;
	}

	/**
	 * Decrypts all files in the {@param contentItemCollection}. Deletes the encrypted files after decrypting them.
	 *
	 * @return true if ALL files are decrypted successfully, false otherwise.
	 */
	@Override
	public boolean decryptItems(Collection<ContentItem> contentItemCollection, Context context) {
		boolean success = true;

		for (ContentItem contentItem : contentItemCollection) {
			success = decryptItem(contentItem, context) && success;
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

		notifyListeners();

		return success;
	}

	public boolean decryptItem(ContentItem contentItem, Context context) {
		try {
			// Remove .CRYPT from filename
			String filename = mDataDir + "/" + contentItem.getFileName();
			filename = filename.substring(0, filename.length() - 6);

			// Create target file
			File decryptedFile = new File(filename);
			decryptedFile.createNewFile();

			Intent encryptIntent = new Intent(context, EncryptionService.class);
			encryptIntent.putExtra(EncryptionService.ENCRYPTED_PATH_KEY, contentItem.getFile().getPath());
			encryptIntent.putExtra(EncryptionService.UNENCRYPTED_PATH_KEY, decryptedFile.getPath());
			//TODO this is just for testing. Needs better safeguard against filechanges and stuff
			encryptIntent.putExtra(EncryptionService.ENTITY_KEY, contentItem.getFileName());
			encryptIntent.putExtra(EncryptionService.MODE_KEY, ConcealCrypto.CryptoMode.DECRYPT);

			context.startService(encryptIntent);
//			this.crypto.decrypt(contentItem.getFile(), decryptedFile, contentItem.getFileName());

//			return contentItem.getFile().delete();
			return true;
		}
//		catch (KeyChainException e) {
//			Log.e(this.getClass().toString() + ".decryptItem", "Error in decrypting data", e);
//		}
//		catch (CryptoInitializationException e) {
//			Log.e(this.getClass().toString() + ".decryptItem", "Error in decrypting data", e);
//		}
		catch (IOException e) {
			if (e instanceof NativeGCMCipherException) {
				Log.e(this.getClass().toString() + ".decryptItem", "Error in decrypting data", e);
				contentItem.getFile().delete();
			}
			else {
				Log.e(this.getClass().toString() + ".decryptItem", "Error in decrypting data", e);
			}
		}

		return false;
	}

	@Override
	public boolean removeItems(Collection<ContentItem> itemCollection) {
		boolean noFailure = true;
		boolean singleSuccess = false;
		for (ContentItem item : itemCollection) {
			boolean removed = item.getFile().delete();
			if (removed) {
				singleSuccess = true;
			}
			else {
				noFailure = false;
			}
		}

		//Empty list, we 'failed' anyway
		if (itemCollection.size() == 0) {
			noFailure = false;
		}

		if (singleSuccess) {
			notifyListeners();
		}

		return noFailure;
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
	public void removeAllContent() {
		for (File file : mDataDir.listFiles()) {
			file.delete();
		}
	}

	/**
	 * Notifies all listeners of a change in content
	 */
	private void notifyListeners() {
		for (ContentChangedListener listener : mListeners) {
			listener.contentChanged();
		}
	}
}
