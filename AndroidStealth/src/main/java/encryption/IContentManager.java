package encryption;

import java.io.File;
import java.util.Collection;

import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedFolder;
import com.stealth.files.IndexedItem;
import com.stealth.utils.IOnResult;

/**
 * Created by Alex on 3/6/14.
 */
public interface IContentManager {

	/**
	 * @return the current folder
	 */
	public IndexedFolder getCurrentFolder();

	/**
	 * Set the current folder.
	 *
	 * @param currentFolder the current folder
	 */
	public void setCurrentFolder(IndexedFolder currentFolder);

	/**
	 * @return an {@link com.stealth.files.IndexedFolder} that is the root that contains all content
	 */
	public IndexedFolder getRoot();

	/**
	 * @return an {@link java.util.Collection} of all the files currently stored in the application
	 */
	public Collection<IndexedFile> getFiles(IndexedFolder fromFolder);

	/**
	 * @return an {@link java.util.Collection} of all the folders currently stored in the application
	 */
	public Collection<IndexedFolder> getFolders(IndexedFolder fromFolder);

	/**
	 * Adds a new item to the storage
	 *
	 * @param toFolder the destination virtual folder
	 * @param item     the item to be added
	 * @param callback the callback that contains the resulting indexedFile
	 */
	public void addFile(IndexedFolder toFolder, File item, IOnResult<IndexedFile> callback);

	/**
	 * Removes an item from the storage
	 *  @param item     the item that should be removed
	 * @param callback the callback that notifies if the item was removed successfully
	 */
	public void removeItem(IndexedItem item, IOnResult<Boolean> callback);

	/**
	 * Removes a collection of items from storage
	 *  @param itemCollection The collection of items to be removed
	 * @param callback       the callback that notifies if the items were removed successfully
	 */
	public void removeItems(Collection<IndexedItem> itemCollection, IOnResult<Boolean> callback);

	/**
	 * Adds a listener to the list
	 *
	 * @param listener listener to be added
	 */
	public void addContentChangedListener(ContentChangedListener listener);

	/**
	 * Notifies the listeners
	 */
	public void notifyContentChangedListeners();

	/**
	 * Removes the listener from the list
	 *
	 * @param listener listener that should be removed
	 * @return whether the listener has been successfully removed
	 */
	public boolean removeContentChangedListener(ContentChangedListener listener);

	/**
	 * Removes everything
	 *
	 * @param callback the callback that notifies if everything was removed successfully
	 */
	public void removeAllContent(IOnResult<Boolean> callback);

	/**
	 * A listener interface that gets notified whenever the content in the storage is changed
	 */
	public interface ContentChangedListener {
		public void contentChanged();
	}
}
