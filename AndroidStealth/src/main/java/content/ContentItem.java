package content;

import java.io.File;

/**
 * Created by Alex on 3/6/14.
 */
public class ContentItem {
    private File mFile;

    public ContentItem(File file){
        mFile = file;
    }

    public File getFile() { return mFile; }
}
