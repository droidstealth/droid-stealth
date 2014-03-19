package content;

import java.io.File;

/**
 * Data object which contains the file and other information to be displayed Created by Alex on 3/6/14.
 */
public class ContentItem {
	private File mFile;
	private String mFileName;
	private boolean encrypted = false;

	public ContentItem(File mFile, String mFileName) {
		this.mFile = mFile;
		this.mFileName = mFileName;
	}

	public File getFile() {
		return mFile;
	}

	public String getFileName() {
		return mFileName;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	/**
	 * Not yet implemented!
	 * Tries to encrypt the ContentItem.
	 *
	 * @return Returns true upon successful encryption, false otherwise.
	 */
	public boolean encrypt() {

		try{
			// TODO encrypt file
			encrypted = true;
		}
		catch (Exception e){
			e.printStackTrace();
			encrypted = false;
		}
		finally {
			return encrypted;
		}
	}

	/**
	 * Not yet implemented!
	 * Tries to decrypt the ContentItem
	 *
	 * @return Returns true upon successful decryption, false otherwise.
	 */
	public boolean decrypt(){
		try{
			// TODO decrypt file
			encrypted = false;
		}
		catch (Exception e){
			encrypted = true;
		}
		finally{
			return !encrypted;
		}
	}
}
