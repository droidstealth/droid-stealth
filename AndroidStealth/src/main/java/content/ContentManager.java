package content;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.R;
import com.stealth.utils.EZ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ContentManager which copies the files to the local data directory
 * Created by Alex on 13-3-14.
 */
public class ContentManager implements IContentManager {
    private File mDataDir;
    private File mThumbDir;

    private List<ContentChangedListener> mListeners = new ArrayList<ContentChangedListener>();

    public ContentManager(Context context){
        mDataDir = context.getExternalFilesDir(null);
        mThumbDir = new File(mDataDir, "_thumbs");
        mThumbDir.mkdir();
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

    /**
     * Creates the thumbnail for an item and saves it in the thumbnail folder
     * @param item the file to generate the thumbnail of
     * @return the created thumbnail
     */
    public File createThumbnail(File item) {
        try {
            Bitmap thumb = FileUtils.getThumbnail(EZ.getContext(), item);
            if (thumb == null) return null;
            File thumbFile = new File(mThumbDir, item.getName());
            FileOutputStream out = new FileOutputStream(thumbFile);
            thumb.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            return thumbFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean addItem(final File item) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File target = new File(mDataDir, item.getName());
                File thumb = null;
                try {
                    // create thumbnail
                    thumb = createThumbnail(item);
                    if (thumb == null) EZ.toast(R.string.content_fail_thumb);
                    // copy to our folder
                    copyFile(item, target);
                    // delete original
                    boolean removed = item.delete();
                    if (!removed) EZ.toast(R.string.content_fail_original_delete);
                    // notify that we are done
                    notifyListeners();
                } catch (IOException e) {
                    e.printStackTrace();
                    // cleanup
                    if (target.exists() && !target.delete()) EZ.toast(R.string.content_fail_clean);
                    if (thumb != null && thumb.exists() && !thumb.delete()) EZ.toast(R.string.content_fail_clean);
                }
            }
        }).start();
        return true;
    }

    @Override
    public boolean removeItem(ContentItem item) {
        boolean removed = item.getFile().delete();
        if(removed){
            notifyListeners();
        }
        return removed;
    }

    @Override
    public boolean removeItems(Collection<ContentItem> itemCollection) {
        boolean noFailure = true;
        boolean singleSuccess = false;
        for(ContentItem item : itemCollection){
            boolean removed = item.getFile().delete();
            if(removed){
                singleSuccess = true;
            }
            else{
                noFailure = false;
            }
        }

        //Empty list, we 'failed' anyway
        if(itemCollection.size() == 0){
            noFailure = false;
        }

        if(singleSuccess){
            notifyListeners();
        }

        return noFailure;
    }

    @Override
    public void addContentChangedListener(ContentChangedListener listener) {
        if(!mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }

    @Override
    public boolean removeContentChangedListener(ContentChangedListener listener) {
        return mListeners.remove(listener);
    }

    @Override
    public void removeAllContent() {
        for(File file: mDataDir.listFiles()){
            file.delete();
        }
    }

    /**
     * Notifies all listeners of a change in content. Tries to do it on the UI thread!
     */
    private void notifyListeners(){
        EZ.runOnMain(new Runnable() {
            @Override
            public void run() {
                notifyListenersNow();
            }
        });
    }

    /**
     * Notifies all listeners of a change in content as we speak.
     */
    private void notifyListenersNow() {
        for (ContentChangedListener listener : mListeners){
            listener.contentChanged();
        }
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
