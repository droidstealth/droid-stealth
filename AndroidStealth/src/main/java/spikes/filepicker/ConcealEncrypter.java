package spikes.filepicker;

import android.content.Context;

import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Alex on 2/22/14.
 */
public class ConcealEncrypter implements IEncrypter {

    Crypto crypto;

    public ConcealEncrypter(Context context){
        crypto = new Crypto(
                new SharedPrefsBackedKeyChain(context),
                new SystemNativeCryptoLibrary());
    }

    @Override
    public void encrypt(File encrypted, File unencrypted, String entityName) throws IOException, CryptoInitializationException, KeyChainException {
        doFileChecks(unencrypted, encrypted);

        FileInputStream from = new FileInputStream(unencrypted); // Stream to read from source
        OutputStream to = crypto.getCipherOutputStream(new FileOutputStream(encrypted),
                new Entity(entityName)); // Stream to write to destination

        copyStreams(from, to);
    }

    @Override
    public void decrypt(File encrypted, File unencrypted, String entityName) throws IOException, CryptoInitializationException, KeyChainException {
        doFileChecks(encrypted, unencrypted);

        InputStream from = crypto.getCipherInputStream(new FileInputStream(encrypted),
                new Entity(entityName));

        FileOutputStream to = new FileOutputStream(unencrypted);

        copyStreams(from, to);
    }

    //copies the contents of the InputStream to the OutputStream
    private void copyStreams(InputStream from, OutputStream to) throws IOException{
        try {
            byte[] buffer = new byte[4096]; // To hold file contents
            int bytes_read; // How many bytes in buffer

            // Read a chunk of bytes into the buffer, then write them out,
            // looping until we reach the end of the file (when read() returns
            // -1). Note the combination of assignment and comparison in this
            // while loop. This is a common I/O programming idiom.
            while ((bytes_read = from.read(buffer)) != -1)
                // Read until EOF
                to.write(buffer, 0, bytes_read); // write
        }
        // Catch block simply gets thrown
        // Always close the streams, even if exceptions were thrown
        finally {
            if (from != null)
                try {
                    from.close();
                } catch (IOException e) {
                }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) {
                }
        }
    }

    private void doFileChecks(File from, File to) throws IOException{
        //we can't work with a non-existing file
        if(!from.exists())
            throw new IOException("File to be encrypted does not exist!");

        if(!from.canRead())
            throw new IOException("Can't read the unencrypted file!");

        /* We don't want to create corrupted data by writing to an existing file. Assume the programmer
         * dun goofed and toss the existing file out.
         */
        if(to.exists()){
            if(!to.canWrite())
                throw new IOException("File already exists but we can't overwrite it!");
            to.delete();
        }
    }
}
