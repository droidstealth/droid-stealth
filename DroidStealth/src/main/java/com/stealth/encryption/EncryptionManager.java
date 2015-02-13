package com.stealth.encryption;

import android.util.Log;

import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedFolder;
import com.stealth.files.IndexedItem;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;

import java.io.File;
import java.util.Collection;

/**
 * This class provides methods for locking and unlocking indexed files and folders.
 * Created by Wolfox on 4/2/14.
 */
public class EncryptionManager {
	private static EncryptionManager sInstance;

	/**
	 * Creates the encryption manager if it doesn't yet exist.
	 * @param service the encryption service to encrypt with
	 * @return the one and only encryption manager
	 */
	public static EncryptionManager create(EncryptionService service) {
		if (sInstance == null) {
			sInstance = new EncryptionManager(service);
		}
		return sInstance;
	}

	/**
	 * Get the singleton isntance of this manager.
	 * @return the one and only encryption manager
	 */
	public static EncryptionManager get() {
		return sInstance;
	}

	private EncryptionService mService;

	/**
	 * Create the manager
	 * @param service the encryption service to encrypt with
	 */
	private EncryptionManager(EncryptionService service) {
		mService = service;
	}


	/**
	 * Encrypts all files in the {@param items}. Deletes the original file after encrypting them.
	 *
	 * @return true if ALL files are encrypted successfully, false otherwise.
	 */
	public boolean encryptItems(Collection<IndexedItem> items, IOnResult<Boolean> callback)
	{
		return cryptoItems(items, ConcealCrypto.CryptoMode.ENCRYPT, callback);
	}

	/**
	 * Decrypts all files in the {@param items}.
	 *
	 * @return true if ALL files are decrypted successfully, false otherwise.
	 */
	public boolean decryptItems(Collection<IndexedItem> items, IOnResult<Boolean> callback)
	{
		return cryptoItems(items, ConcealCrypto.CryptoMode.DECRYPT, callback);
	}

	/**
	 * Encrypts/decrypts all files in the {@param items}.
	 *
	 * @return true if ALL files are decrypted successfully, false otherwise.
	 */
	public boolean cryptoItems(Collection<IndexedItem> items, ConcealCrypto.CryptoMode mode, IOnResult<Boolean> callback)
	{
		boolean success = true;
		for (IndexedItem item : items) success &= cryptoItem(item, mode, callback);
		if (success) Log.i(Utils.tag(), "Crypto mode '" + mode.key + "' items success!");
		else Log.e(Utils.tag(), "Crypto mode '" + mode.key + "' with errors:");
		return success;
	}

	/**
	 * Perform the encryption, by sending out a task to the service.
	 * @param item the item to encrypt/decrypt
	 * @param mode what to do: encryption/decryption?
	 * @param callback the method that will be called once this one is ready
	 * @return whether we succeeded (we will..)
	 */
	public boolean cryptoItem(IndexedItem item, ConcealCrypto.CryptoMode mode, IOnResult<Boolean> callback)
	{
		if (item instanceof IndexedFolder)
		{
			boolean success = true;
			IndexedFolder folder = (IndexedFolder) item;
			for (IndexedFolder f : folder.getFolders()) success &= cryptoItem(f, mode, callback);
			for (IndexedFile f : folder.getFiles()) success &= cryptoItem(f, mode, callback);
			return success;
		}
		else
		{
			IndexedFile file = (IndexedFile) item;
			Log.d(Utils.tag(), "'" + mode.key + "' file " + file.getUnlockedFile().getAbsolutePath());
			File locked = file.getLockedFile();
			File unlocked = file.getUnlockedFile();

			// check if we actually need to do anything?
			if (mode == ConcealCrypto.CryptoMode.DECRYPT && unlocked.exists()) return true;

			mService.addCryptoTask(file, mode, callback);
			return true;
		}
	}
}
