package content;

import java.io.File;

/**
 * Data object which contains the file and other information to be displayed
 * Created by Alex on 3/6/14.
 */
public class ContentItem {
    private File mFile;
    private String mFileName;

    public ContentItem(File mFile, String mFileName) {
        this.mFile = mFile;
        this.mFileName = mFileName;
    }

    public File getFile() { return mFile; }

    public String getFileName() { return mFileName; }
}
