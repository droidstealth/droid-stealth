package content;

import static content.ConcealCrypto.CryptoMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.facebook.crypto.cipher.NativeGCMCipherException;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.stealth.files.FileIndex;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import spikes.notifications.FileStatusNotificationsManager;

/**
 * Created by Alex on 2/22/14.
 */
public class EncryptionService extends Service implements FileIndex.OnFileIndexChangedListener {

	private static final int POOL_SIZE = 10;

	private HashMap<String, CryptoTask> mToEncrypt = new HashMap<String, CryptoTask>();
	private HashMap<String, CryptoTask> mToDecrypt = new HashMap<String, CryptoTask>();
	private ArrayList<UpdateListener> mListeners = new ArrayList<UpdateListener>();
	private IBinder mBinder;
	private ConcealCrypto mEncrypter;
	private ExecutorService mCryptoExecutor;

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.setContext(getApplicationContext());
		FileIndex.create(false, new IOnResult<FileIndex>() {
			@Override
			public void onResult(FileIndex result) {
				result.registerListener(EncryptionService.this);
			}
		});

		//use a scheduled thread pool for the running of our crypto system
		mCryptoExecutor = Executors.newScheduledThreadPool(POOL_SIZE);
		mEncrypter = new ConcealCrypto(this);
		mBinder = new ServiceBinder();

		handleUpdate(false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCryptoExecutor.shutdown();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction() != null && intent.getAction().equals(FileStatusNotificationsManager.ACTION_LOCK_ALL)) {
			EncryptionManager.create(this).encryptItems(FileIndex.get().getUnlockedFiles(), null);
			return super.onStartCommand(intent, flags, startId);
		}
		else {
			throw new IllegalStateException("EncryptionService should not be started through Intent anymore");
		}
	}

	@Override
	public void onFileIndexChanged() {
		handleUpdate(false);
	}

	public interface UpdateListener {
		public abstract void onEncryptionServiceUpdate();
	}

	/**
	 * Add a listener in order to listen to changes in the queues
	 *
	 * @param listener the listener.
	 */
	public void addUpdateListener(UpdateListener listener) {
		if (mListeners.contains(listener)) {
			return;
		}
		mListeners.add(listener);
	}

	/**
	 * Handles changes in the queue
	 */
	private void handleUpdate(boolean notifyListenersOnUpdate) {
		if (notifyListenersOnUpdate) {
			boolean somethingWasNull = false;
			for (UpdateListener listener : mListeners) {
				if (listener != null) {
					listener.onEncryptionServiceUpdate();
				} else {
					somethingWasNull = true;
				}
			}
			if (somethingWasNull) {
				// keep the list clean :)
				mListeners.removeAll(Collections.singleton(null));
			}
		}
		handleNotifications();
	}

	/**
	 * Updates all notifications asynchronously
	 */
	public void handleNotifications() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (mToDecrypt.size() > 0) {
					FileStatusNotificationsManager.get().showFilesUnlocking();
				}
				else {
					FileStatusNotificationsManager.get().hideFilesUnlocking();
				}
				if (mToEncrypt.size() > 0) {
					FileStatusNotificationsManager.get().showFilesLocking();
				}
				else {
					FileStatusNotificationsManager.get().hideFilesLocking();
				}
				FileIndex.create(false, new IOnResult<FileIndex>() {
					@Override
					public void onResult(FileIndex result) {
						if (result.hasUnlockedFiles()) {
							FileStatusNotificationsManager.get().showFilesUnlocked();
						}
						else {
							FileStatusNotificationsManager.get().hideFilesUnlocked();
						}
					}
				});
			}
		}).start();
	}

	public Future addCryptoTask(File encrypted, File unencrypted, final String entityName, final CryptoMode mode,
			final IOnResult<Boolean> callback) {
		CryptoTask task =
				new CryptoTask(mEncrypter, encrypted, unencrypted, entityName, mode, new IOnResult<Boolean>() {
					@Override
					public void onResult(Boolean result) {
						if (mode == CryptoMode.DECRYPT) {
							mToDecrypt.remove(entityName);
						}
						if (mode == CryptoMode.ENCRYPT) {
							mToEncrypt.remove(entityName);
						}
						if (callback != null) {
							callback.onResult(result);
						}
						handleUpdate(true);
					}
				});

		if (mode == CryptoMode.DECRYPT) {
			mToDecrypt.put(entityName, task);
		}
		if (mode == CryptoMode.ENCRYPT) {
			mToEncrypt.put(entityName, task);
		}
		handleUpdate(true);

		Log.d(Utils.tag(this), "Submitting new task..");
		return mCryptoExecutor.submit(task);
	}

	private class CryptoTask implements Runnable {
		private final ConcealCrypto encrypter;
		private final File encryptedFile;
		private final File unencryptedFile;
		private final String entityName;
		private final CryptoMode cryptoMode;
		private IOnResult<Boolean> callback;

		public CryptoTask(ConcealCrypto encrypter, File encryptedFile, File unencryptedFile, String entityName,
				CryptoMode mode, IOnResult<Boolean> callback) {
			this.encrypter = encrypter;
			this.encryptedFile = encryptedFile;
			this.unencryptedFile = unencryptedFile;
			this.entityName = entityName;
			this.cryptoMode = mode;
			this.callback = callback;
		}

		@Override
		public void run() {
			try {
				Log.d(Utils.tag(), "Starting en/decryption task.");
				switch (cryptoMode) {
					case ENCRYPT:
						encryptedFile.createNewFile();
						encrypter.encrypt(encryptedFile, unencryptedFile, entityName);
						unencryptedFile.delete();
						break;
					case DECRYPT:
						unencryptedFile.createNewFile();
						encrypter.decrypt(encryptedFile, unencryptedFile, entityName);
						encryptedFile.delete();
						break;
				}

				Log.d(this.getClass().toString() + ".run", "Finished task!");
			}
			catch (KeyChainException e) {
				Log.e(Utils.tag(), "KeychainException!", e);
			}
			catch (CryptoInitializationException e) {
				Log.e(Utils.tag(), "CryptoInitializationException!", e);
			}
			catch (IOException e) {
				Log.e(Utils.tag(), "IOException", e);
				// This is the error we cannot explain yet but we want
				// the app to behave as expected; delete the source file.
				// TODO find reason error and fix
				if (e instanceof NativeGCMCipherException) {
					if (cryptoMode == CryptoMode.ENCRYPT) {
						unencryptedFile.delete();
					} else if (cryptoMode == CryptoMode.DECRYPT) {
						encryptedFile.delete();
					}
				}
			}

			callback.onResult(true);
		}
	}

	public class ServiceBinder extends Binder {
		public EncryptionService getService() {
			return EncryptionService.this;
		}
	}
}
