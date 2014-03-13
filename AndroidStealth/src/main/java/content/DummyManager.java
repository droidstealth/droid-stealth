package content;

import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Alex on 3/6/14.
 */
public class DummyManager implements IContentManager {
    List<ContentChangedListener> mListeners = new ArrayList<ContentChangedListener>();

    HashSet<ContentItem> mStorage = new HashSet<ContentItem>();

    public DummyManager(){
	    generateDummyContent();
    }

    @Override
    public Collection<ContentItem> getStoredContent() {
        return new ArrayList<ContentItem>(mStorage);
    }

    @Override
    public void addItem(File item) {
        mStorage.add(new ContentItem(item, item.getName()));

        notifyListeners();
    }

    @Override
    public boolean removeItem(ContentItem item) {
        boolean removed = mStorage.remove(item);

        if(removed)
            notifyListeners();

        return removed;
    }

    @Override
    public boolean removeItems(Collection<ContentItem> itemCollection) {
        boolean noFailure = true;
        boolean singleSuccess = false;
        for(ContentItem item : itemCollection){
            boolean removed = mStorage.remove(item);
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

    /**
     * Notifies all listeners of a change in content
     */
    private void notifyListeners(){
        for(ContentChangedListener listener : mListeners)
            listener.contentChanged();
    }

    /**
     * Generates dummy content to test with
     */
    private void generateDummyContent(){
        for(int i = 0; i < 30; i++)
            mStorage.add(new ContentItem(new File("/"), "Test " + i));
    }

	@Override
	public void removeAllContent(){
		for(ContentItem contentItem : mStorage){
			mStorage.remove(contentItem);
		}
	}
}
