package content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.ipaulpro.afilechooser.utils.FileUtils;

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
		System.out.println("mDataDir: " + mDataDir.toString());
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

			System.out.println("Encrypted the file");
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

	@Override
	public boolean encryptItem(ContentItem contentItem) {
		try {
			File cacheDir = Environment.getDownloadCacheDirectory();
			File tempFile = File.createTempFile(contentItem.getFileName(), "tmp", cacheDir);
			// TODO Determine correct arguments for method below
			crypto.encrypt(tempFile, contentItem.getFile(), contentItem.getFileName());
			return true;
		}
		catch (KeyChainException e) {
			e.printStackTrace();
		}
		catch (CryptoInitializationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
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
	 * Encrypts all files in the {@param contentItemCollection}.
	 *
	 * @return true if ALL files are encrypted successfully, false otherwise.
	 */
	@Override
	public boolean encryptItems(Collection<ContentItem> contentItemCollection) {
		boolean success = true;

		for (ContentItem contentItem : contentItemCollection) {
			success = encryptItem(contentItem) && success;
		}

		if (success) {
            Log.i(this.getClass().toString(),"Encrypted items:");
		}
		else{
			Log.i(this.getClass().toString(), "Encrypted with errors:");
		}
		for(ContentItem contentItem : contentItemCollection){
			Log.i(this.getClass().toString(), "\t"+contentItem.getFileName());
		}
		return success;
	}

	/**
	 * Decrypts all files in the {@param contentItemCollection}.
	 *
	 * @return true if ALL files are decrypted successfully, false otherwise.
	 */
	@Override
	public boolean decryptItems(Collection<ContentItem> contentItemCollection) {
		boolean success = true;

		for (ContentItem contentItem : contentItemCollection) {
			success = decryptItem(contentItem) && success;
		}

		if (success) {
			System.out.println("Decrypted items:");
		}
		else{
			System.out.println("Decrypted with errors:");
		}
		for(ContentItem contentItem : contentItemCollection){
			System.out.println("\t"+contentItem.getFileName());
		}
		return success;
	}

	@Override
	public boolean decryptItem(ContentItem contentItem) {
		try {
            File cacheDir = Environment.getDownloadCacheDirectory();
            File tempFile = File.createTempFile(contentItem.getFileName(), "tmp", cacheDir);
			// TODO Determine correct arguments for method below
			this.crypto.decrypt(contentItem.getFile(), tempFile, contentItem.getFileName());
			return true;
		}
		catch (KeyChainException e) {
			e.printStackTrace();
		}
		catch (CryptoInitializationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
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
