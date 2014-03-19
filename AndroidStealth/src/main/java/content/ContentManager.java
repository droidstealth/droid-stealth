package content;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;

/**
 * ContentManager which copies the files to the local data directory Created by Alex on 13-3-14.
 */
public class ContentManager implements IContentManager {
	private File mDataDir;

	private List<ContentChangedListener> mListeners = new ArrayList<ContentChangedListener>();

	public ContentManager(Context context) {
		mDataDir = context.getExternalFilesDir(null);
	}

	/**
	 * Helper function to copy a file internally
	 *
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	private static void copyFileWithoutEncryption(File sourceFile, File destFile) throws IOException {
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

	private static void copyFileWithEncryption(File sourceFile, File destFile) throws IOException {
		if(!destFile.exists()){
			destFile.createNewFile();
		}

		FileChannel source = null;
		WritableByteChannel destination = null;

		try{
			source = new FileInputStream(sourceFile).getChannel();
			destination = Channels.newChannel(new CipherOutputStream(new FileOutputStream(destFile), Cipher.getInstance("CBC")));

			source.transferTo(0, source.size(), destination);
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
		finally{
			if(source != null){
				source.close();
			}
			if(destination != null){
				destination.close();
			}
		}
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
			copyFileWithoutEncryption(item, new File(mDataDir, item.getName()));
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
	 * @return true if ALL files are encrypted successfully, false otherwise.
	 */
	public boolean encryptItems(Collection<ContentItem> contentItemCollection){
		boolean success = true;

		for(ContentItem contentItem : contentItemCollection){
			success = contentItem.encrypt() && success;
		}

		return success;
	}

	public boolean encryptItem(ContentItem contentItem){
		return contentItem.encrypt();
	}

	/**
	 * Decrypts all files in the {@param contentItemCollection}.
	 * @return true if ALL files are decrypted successfully, false otherwise.
	 */
	public boolean decryptItems(Collection<ContentItem> contentItemCollection){
		boolean success = true;

		for(ContentItem contentItem : contentItemCollection){
			success = contentItem.decrypt() && success;
		}

		return success;
	}

	public boolean decryptItem(ContentItem contentItem){
		return contentItem.decrypt();
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
