package spikes.filepicker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Alex on 2/22/14.
 */
public class EncryptionService extends Service {

    private static final int PoolSize = 10;

    public static final String ENCRYPTED_PATH_KEY = "ENCRYPTED_PATH";
    public static final String UNENCRYPTED_PATH_KEY = "UNENCRYPTED_PATH";
    public static final String ENTITY_KEY = "ENTITY";
    //whether the service should encrypt or decrypt
    public static final String ENCRYPT_KEY = "ENCRYPT";

    ConcealCrypto mEncrypter;


    ExecutorService cryptoExecutor;

    @Override
    public void onCreate() {
        super.onCreate();

        //use a scheduled thread pool for the running of our crypto system
        cryptoExecutor = Executors.newScheduledThreadPool(PoolSize);
        mEncrypter = new ConcealCrypto(this);
    }

    //no binding necessary here
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("com.stealth.android", "Received service start command!");
        File encryptedFile = new File(intent.getStringExtra(ENCRYPTED_PATH_KEY));
        File unencryptedFile = new File(intent.getStringExtra(UNENCRYPTED_PATH_KEY));
        String entityName = intent.getStringExtra(ENTITY_KEY);
        boolean encrypt = intent.getBooleanExtra(ENCRYPT_KEY, true);

        CryptoTask cryptoTask = new CryptoTask(mEncrypter, encryptedFile, unencryptedFile, entityName, encrypt);

        cryptoExecutor.submit(cryptoTask);

        Log.d("com.stealth.android", "submitted task!");
        //Don't redeliver or restart. The threading handles everything properly.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cryptoExecutor.shutdown();
    }

    private class CryptoTask implements Runnable {
        private final ConcealCrypto encrypter;
        private final File encryptedFile;
        private final File unencryptedFile;
        private final String entityName;
        private final boolean encrypt;
        public CryptoTask(ConcealCrypto encrypter, File encryptedFile, File unencryptedFile, String entityName, boolean encrypt){
            this.encrypter = encrypter;
            this.encryptedFile = encryptedFile;
            this.unencryptedFile = unencryptedFile;
            this.entityName = entityName;
            this.encrypt = encrypt;
        }

        @Override
        public void run() {
            try
            {
                if(encrypt)
                    encrypter.encrypt(encryptedFile, unencryptedFile, entityName);
                else
                    encrypter.decrypt(encryptedFile, unencryptedFile, entityName);

                Log.d("com.stealth.android", "Finished task!");
            } catch (KeyChainException e) {
                Log.e("EncryptionService", "KeychainException!", e);
                //TODO log?
            } catch (CryptoInitializationException e) {
                Log.e("EncryptionService", "CryptoInitializationException!",e );
                //TODO log?
            } catch (IOException e) {
                Log.e("EncryptionService", "IOException", e);
                //TODO log?
            }
        }
    }
}
