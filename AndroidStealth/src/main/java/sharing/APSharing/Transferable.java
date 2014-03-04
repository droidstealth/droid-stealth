package sharing.APSharing;

import java.io.File;

/**
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
