package content;

import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Alex on 2/22/14.
 */
public interface ICrypto {
    /**
     * Encrypts a file input stream to the given file
     * @param encrypted file to be written to. Cannot be a directory
     * @param unencrypted the original file to be encrypted
     * @exception java.io.IOException thrown when the operation fails, either because the encrypted
     * file already exists, or something failed during encryption
     */
    public void encrypt(File encrypted, File unencrypted, String entityName) throws IOException, CryptoInitializationException, KeyChainException;

    public void decrypt(File encrypted, File unencrypted, String entityName) throws IOException, CryptoInitializationException, KeyChainException;
}
