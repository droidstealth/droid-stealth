package content;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * ContentManager which copies the files to the local data directory
 * Created by Alex on 13-3-14.
 */
public class ContentManager implements IContentManager {
    private File mDataDir;

    private List<ContentChangedListener> mListeners = new ArrayList<ContentChangedListener>();

    public ContentManager(Context context){
        mDataDir = context.getExternalFilesDir(null);
    }

    @Override
    public Collection<ContentItem> getStoredContent() {
        File[] files = mDataDir.listFiles();
        ArrayList<ContentItem> itemArrayList = new ArrayList<ContentItem>();

        for(File file : files){
            itemArrayList.add(new ContentItem(file, file.getName()));
        }

        return itemArrayList;
    }

    @Override
    public void addItem(File item) {
        try {
            copyFile(item, new File(mDataDir, item.getName()));
            notifyListeners();
        } catch (IOException e) {
        }
    }

    @Override
    public boolean removeItem(ContentItem item) {
        boolean removed = item.getFile().delete();
        if(removed)
            notifyListeners();
        return removed;
    }

    @Override
    public boolean removeItems(Collection<ContentItem> itemCollection) {
        boolean noFailure = true;
        boolean singleSuccess = false;
        for(ContentItem item : itemCollection){
            boolean removed = item.getFile().delete();
            if(removed)
                singleSuccess = true;
            else
                noFailure = false;
        }

        //Empty list, we 'failed' anyway
        if(itemCollection.size() == 0)
            noFailure = false;

        if(singleSuccess)
            notifyListeners();

        return noFailure;
    }

    @Override
    public void addContentChangedListener(ContentChangedListener listener) {
        if(!mListeners.contains(listener))
            mListeners.add(listener);
    }

    @Override
    public boolean removeContentChangedListener(ContentChangedListener listener) {
        return mListeners.remove(listener);
    }

    @Override
    public void removeAllContent() {
        for(File file: mDataDir.listFiles())
            file.delete();
    }

    /**
     * Notifies all listeners of a change in content
     */
    private void notifyListeners(){
        for(ContentChangedListener listener : mListeners)
            listener.contentChanged();
    }

    /**
     * Helper function to copy a file internally
     * @param sourceFile
     * @param destFile
     * @throws IOException
     */
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }
}
