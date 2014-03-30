package content;

import static content.ConcealCrypto.CryptoMode;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.facebook.crypto.cipher.NativeGCMCipherException;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;

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
	private IBinder mBinder;

	ConcealCrypto mEncrypter;


	ExecutorService cryptoExecutor;

	@Override
	public void onCreate() {
		super.onCreate();

		//use a scheduled thread pool for the running of our crypto system
		cryptoExecutor = Executors.newScheduledThreadPool(PoolSize);
		mEncrypter = new ConcealCrypto(this);
		mBinder = new ServiceBinder();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(this.getClass().toString() + ".onStartCommand", "Received service start command!");
		File encryptedFile = new File(intent.getStringExtra(ENCRYPTED_PATH_KEY));
		File unencryptedFile = new File(intent.getStringExtra(UNENCRYPTED_PATH_KEY));
		String entityName = intent.getStringExtra(ENTITY_KEY);

		Bundle bundle = intent.getExtras();
		CryptoMode mode = (CryptoMode) (bundle != null ? bundle.get(MODE_KEY) : null);

		CryptoTask cryptoTask = new CryptoTask(mEncrypter, encryptedFile, unencryptedFile, entityName, mode);

		cryptoExecutor.submit(cryptoTask);

		Log.d(this.getClass().toString() + ".onStartCommand", "submitted task!");
		//Don't redeliver or restart. The threading handles everything properly.
		return START_NOT_STICKY;
	}

	public Future addCryptoTask(File encrypted, File unencrypted, String entityName, CryptoMode mode){
		CryptoTask task = new CryptoTask(mEncrypter, encrypted, unencrypted, entityName, mode);

		Log.d(this.getClass().toString()+".addCryptoTask", "Submitting new task..");
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

		public CryptoTask(ConcealCrypto encrypter, File encryptedFile, File unencryptedFile, String entityName,
		                  CryptoMode mode) {
			this.encrypter = encrypter;
			this.encryptedFile = encryptedFile;
			this.unencryptedFile = unencryptedFile;
			this.entityName = entityName;
			this.cryptoMode = mode;
		}

		@Override
		public void run() {
			try {

				Log.d(this.getClass().toString() + ".run", "Starting en/decryption task.");

				switch (cryptoMode) {
					case ENCRYPT:
						encrypter.encrypt(encryptedFile, unencryptedFile, entityName);
						unencryptedFile.delete();
						break;
					case DECRYPT:
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
		}
	}

	public class ServiceBinder extends Binder{
		public EncryptionService getService(){
			return EncryptionService.this;
		}
	}
}
