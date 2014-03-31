package content;

import android.content.Context;
import android.graphics.Bitmap;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.R;
import com.stealth.utils.Utils;
import com.stealth.utils.IOnResult;

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

    private static final String THUMBS_FOLDER = "_thumbs";

    private File mDataDir;
    private File mThumbDir;

    private List<ContentChangedListener> mListeners = new ArrayList<ContentChangedListener>();

    public ContentManager(Context context){
        mDataDir = context.getExternalFilesDir(null);
        mThumbDir = new File(mDataDir, THUMBS_FOLDER);
        mThumbDir.mkdir();
    }

    @Override
    public Collection<ContentItem> getStoredContent() {
        File[] files = mDataDir.listFiles();
        ArrayList<ContentItem> itemArrayList = new ArrayList<ContentItem>();

        for(File file : files){
            if (!file.getName().equals(THUMBS_FOLDER))
            itemArrayList.add(new ContentItem(file, file.getName()));
        }

        return itemArrayList;
    }

    /**
     * Gets the thumbnail of a file
     * @param item the file to find the thumbnail file of
     * @return the thumbnail file
     */
    public File getThumbnailFile(File item) {
        return new File(mThumbDir, item.getName() + ".jpg");
    }

    /**
     * Creates the thumbnail for an item and saves it in the thumbnail folder
     * @param item the file to generate the thumbnail of
     * @return the created thumbnail
     */
    public File createThumbnail(File item) {
        try {
            Bitmap thumb = FileUtils.getThumbnail(Utils.getContext(), item);
            if (thumb == null) return null;
            File thumbFile = getThumbnailFile(item);
            FileOutputStream out = new FileOutputStream(thumbFile);
            thumb.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            return thumbFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addItem(final File item, final IOnResult<Boolean> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // init
                File target = new File(mDataDir, item.getName());
                File thumb = null;
                String mimeType = FileUtils.getMimeType(item);
                boolean isMedia = FileUtils.isImageOrVideo(mimeType);

                try
                {
                    // copy to our folder
                    copyFile(item, target);

                    // create thumbnail
                    thumb = createThumbnail(target);
                    if (isMedia && thumb == null) {
                        Utils.toast(R.string.content_fail_thumb);
                    }

                    // delete original
                    boolean removed = Utils.delete(item);
                    if (!removed) {
                        Utils.toast(R.string.content_fail_original_delete);
                    }

                    // notify that we are done
                    notifyListeners();
                    if (callback != null)
                        callback.onResult(true);
                }
                catch (IOException e)
                {
                    e.printStackTrace();

                    // cleanup
                    if (target.exists() && !Utils.delete(target)) Utils.toast(R.string.content_fail_clean);
                    if (thumb != null && thumb.exists() && !Utils.delete(thumb)) Utils.toast(R.string.content_fail_clean);

                    // notify that we are done
                    if (callback != null)
                        callback.onResult(false);
                }
            }
        }).start();
    }

    /**
     * Removes a file completely right now in current thread,
     * including its thumbnail and encrypted version
     * @param contentItem the file to remove
     * @return whether it completely succeeded
     */
    public boolean removeItemNow(ContentItem contentItem) {
        File file = contentItem.getFile();
        File thumb = getThumbnailFile(file);
        boolean success = true;
        // TODO remove encrypted files
        // TODO make sure file is removed from any queue etc
        if (thumb.exists()) success &= Utils.delete(thumb);
        success &= Utils.delete(file);
        return success;
    }

    @Override
    public void removeItem(final ContentItem item, final IOnResult<Boolean> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean removed = removeItemNow(item);
                if(removed){
                    notifyListeners();
                }
                if (callback != null)
                    callback.onResult(removed);
            }
        }).start();
    }

    @Override
    public void removeItems(Collection<ContentItem> itemCollection, final IOnResult<Boolean> callback) {
        // make copy in case it changes while we are executing in another thread
        final ContentItem[] items = itemCollection.toArray((ContentItem[])java.lang.reflect.Array.newInstance(ContentItem.class, itemCollection.size()));
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                int failures = 0;
                boolean singleSuccess = false;
                for(ContentItem item : items){
                    if(removeItemNow(item))
                        singleSuccess |= true;
                    else failures++;
                }
                if (failures > 0) {
                    Utils.toast(Utils.str(R.string.content_fail_delete).replace("{COUNT}", "" + failures));
                }
                if(singleSuccess){
                    notifyListeners();
                }

                if (callback != null)
                    callback.onResult(failures == 0);
            }
        }).start();
    }

    @Override
    public void removeAllContent(final IOnResult<Boolean> callback) {
        removeItems(getStoredContent(), callback);
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


    /**
     * Notifies all listeners of a change in content. Tries to do it on the UI thread!
     */
    private void notifyListeners(){
        Utils.runOnMain(new Runnable() {
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
