package content;

import com.stealth.utils.IOnResult;

import java.io.File;
import java.util.Collection;

import android.content.Context;

/**
 * Created by Alex on 3/6/14.
 */
public interface IContentManager {

	/**
     * A listener interface that gets notified whenever the content in the storage is changed
     */
    public interface ContentChangedListener {
        public void contentChanged();
    }

    /**
     * @return an {@link java.util.Collection} of all the items currently stored in the application
     */
    public Collection<ContentItem> getStoredContent();

    /**
     * Adds a new item to the storage
     * @param item the item to be added
     * @param callback the callback that notifies if the item was added successfully
     */
    public void addItem(File item, IOnResult<Boolean> callback);

    /**
     * Removes an item from the storage
     * @param item the item that should be removed
     * @param callback the callback that notifies if the item was removed successfully
     */
    public void removeItem(ContentItem item, IOnResult<Boolean> callback);

    /**
     * Removes a collection of items from storage
     * @param itemCollection The collection of items to be removed
     * @param callback the callback that notifies if the items were removed successfully
     */
    public void removeItems(Collection<ContentItem> itemCollection, IOnResult<Boolean> callback);

	public boolean decryptItems(Collection<ContentItem> itemCollection, EncryptionService service);
	public boolean encryptItems(Collection<ContentItem> itemCollection, EncryptionService service);

    /**
     * Adds a listener to the list
     * @param listener listener to be added
     */
    public void addContentChangedListener(ContentChangedListener listener);

    /**
     * Removes the listener from the list
     * @param listener listener that should be removed
     * @return whether the listener has been successfully removed
     */
    public boolean removeContentChangedListener(ContentChangedListener listener);

    /**
     * Removes everything
     * @param callback the callback that notifies if everything was removed successfully
     */
	public void removeAllContent(IOnResult<Boolean> callback);
}
