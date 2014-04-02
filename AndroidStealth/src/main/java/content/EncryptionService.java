package content;

import static content.ConcealCrypto.CryptoMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
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
public class EncryptionService extends Service {

	private static final int PoolSize = 10;

	public static final String ENCRYPTED_PATH_KEY = "ENCRYPTED_PATH";
	public static final String UNENCRYPTED_PATH_KEY = "UNENCRYPTED_PATH";
	public static final String ENTITY_KEY = "ENTITY";
	//whether the service should encrypt or decrypt
	public static final String MODE_KEY = "MODE";


    private HashMap<String, CryptoTask> mToEncrypt = new HashMap<String, CryptoTask>();
    private HashMap<String, CryptoTask> mToDecrypt = new HashMap<String, CryptoTask>();

	private IBinder mBinder;

	ConcealCrypto mEncrypter;


	ExecutorService cryptoExecutor;

	@Override
	public void onCreate() {
		super.onCreate();
        Utils.setContext(getApplicationContext());

		//use a scheduled thread pool for the running of our crypto system
		cryptoExecutor = Executors.newScheduledThreadPool(PoolSize);
		mEncrypter = new ConcealCrypto(this);
		mBinder = new ServiceBinder();

        handleUpdate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null && intent.getAction().equals(FileStatusNotificationsManager.ACTION_LOCK_ALL)) {
            EncryptionManager.create(this).encryptItems(FileIndex.get().getUnlockedFiles(), null);
            return super.onStartCommand(intent,flags,startId);
        } else {
		    throw new IllegalStateException("EncryptionService should not be started through Intent anymore");
        }

		//		Log.d(this.getClass().toString() + ".onStartCommand", "Received service start command!"); File encryptedFile = new File(intent.getStringExtra(ENCRYPTED_PATH_KEY));
		//		File unencryptedFile = new File(intent.getStringExtra(UNENCRYPTED_PATH_KEY));
		//		String entityName = intent.getStringExtra(ENTITY_KEY);
		//
		//		Bundle bundle = intent.getExtras();
		//		CryptoMode mode = (CryptoMode) (bundle != null ? bundle.get(MODE_KEY) : null);
		//
		//		CryptoTask cryptoTask = new CryptoTask(mEncrypter, encryptedFile, unencryptedFile, entityName, mode, null);
		//
		//		cryptoExecutor.submit(cryptoTask);
		//
		//		Log.d(this.getClass().toString() + ".onStartCommand", "submitted task!");
		//		//Don't redeliver or restart. The threading handles everything properly.
		//		return START_NOT_STICKY;
	}

    public interface UpdateListener {
        public abstract void onEncryptionServiceUpdate();
    }
    private ArrayList<UpdateListener> mListeners = new ArrayList<UpdateListener>();

    /**
     * Add a listener in order to listen to changes in the queues
     * @param listener the listener.
     */
    public void addUpdateListener(UpdateListener listener) {
        mListeners.add(listener);
    }

    /**
     * Handles changes in the queue
     */
    private void handleUpdate() {
        for (UpdateListener listener : mListeners)
            if (listener != null) listener.onEncryptionServiceUpdate();
        handleNotifications();
    }

    /**
     * Updates all notifications asynchronously
     */
    public void handleNotifications() {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (mToDecrypt.size() > 0) FileStatusNotificationsManager.get().showFilesUnlocking();
                else FileStatusNotificationsManager.get().hideFilesUnlocking();
                if (mToEncrypt.size() > 0) FileStatusNotificationsManager.get().showFilesLocking();
                else FileStatusNotificationsManager.get().hideFilesLocking();
                FileIndex.create(false, new IOnResult<FileIndex>() {
                    @Override
                    public void onResult(FileIndex result) {
                        if (result.hasUnlockedFiles()) {
                            FileStatusNotificationsManager.get().showFilesUnlocked();
                        } else {
                            FileStatusNotificationsManager.get().hideFilesUnlocked();
                        }
                    }
                });
            }
        }).start();
    }

	public Future addCryptoTask(File encrypted, File unencrypted, final String entityName, final CryptoMode mode, final IOnResult<Boolean> callback) {
		CryptoTask task = new CryptoTask(mEncrypter, encrypted, unencrypted, entityName, mode, new IOnResult<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                if (mode == CryptoMode.DECRYPT) mToDecrypt.remove(entityName);
                if (mode == CryptoMode.ENCRYPT) mToEncrypt.remove(entityName);
                if (callback != null) callback.onResult(result);
                handleUpdate();
            }
        });

        if (mode == CryptoMode.DECRYPT) mToDecrypt.put(entityName, task);
        if (mode == CryptoMode.ENCRYPT) mToEncrypt.put(entityName, task);
        handleUpdate();

		Log.d(this.getClass().toString() + ".addCryptoTask", "Submitting new task..");
		return cryptoExecutor.submit(task);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		cryptoExecutor.shutdown();
	}

	public void startTestToast() {
		Toast.makeText(getApplicationContext(), "This is a test", Toast.LENGTH_LONG).show();
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

				Log.d(this.getClass().toString() + ".run", "Starting en/decryption task.");

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
				Log.e("EncryptionService", "KeychainException!", e);
			}
			catch (CryptoInitializationException e) {
				Log.e("EncryptionService", "CryptoInitializationException!", e);
			}
			catch (IOException e) {
				Log.e("EncryptionService", "IOException", e);
				// This is the error we cannot explain yet, but we want the app to behave as expected; delete the source file.
				if (e instanceof NativeGCMCipherException) {
					encryptedFile.delete();
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
