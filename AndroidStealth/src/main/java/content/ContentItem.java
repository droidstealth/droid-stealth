package content;

import java.io.File;

/**
 * Data object which contains the file and other information to be displayed
 * Created by Alex on 3/6/14.
 */
public class ContentItem {
    private File mFile;
    private boolean mEncrypted;
    private String mFileName;

    public ContentItem(File mFile, boolean mEncrypted, String mFileName) {
        this.mFile = mFile;
        this.mEncrypted = mEncrypted;
        this.mFileName = mFileName;
    }

    public File getFile() { return mFile; }

    public boolean isEncrypted() { return mEncrypted; }

    public String getFileName() { return mFileName; }
}
