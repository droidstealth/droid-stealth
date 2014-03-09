package sharing.APSharing;

import java.io.File;

/**
 * Container with information about the object being shared
 * Created by Alex on 3/3/14.
 */
public class Transferable {
    private File mTransferObject;
    private String mMimeType;
    private long mContentLength;

    public long getContentLength() {
        return mContentLength;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public File getTransferObject() {
        return mTransferObject;
    }

    public Transferable(File mTransferObject, String mMimeType, long mContentLength) {
        this.mTransferObject = mTransferObject;
        this.mMimeType = mMimeType;
        this.mContentLength = mContentLength;
    }
}
