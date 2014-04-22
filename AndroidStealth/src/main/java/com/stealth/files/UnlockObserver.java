package com.stealth.files;

import java.util.ArrayList;
import java.util.List;

import android.os.FileObserver;
import encryption.IContentManager;

/**
 * A class that can be used to monitor a file/folder and notify listeners when the folder changes
 * Created by Alex on 22-4-2014.
 */
public class UnlockObserver extends FileObserver {

	private List<IContentManager.ContentChangedListener> mListeners = new ArrayList<IContentManager.ContentChangedListener>();

	public UnlockObserver(String path)  {
		super(path);
	}

	/**
	 * Adds new listener that gets called when the file/folder changes
	 * @param listener
	 */
	public void addListener(IContentManager.ContentChangedListener listener){
		if(!mListeners.contains(listener)){
			mListeners.add(listener);
		}
	}

	/**
	 * Removes the listener from the list of listeners to call on change
	 * @param listener
	 * @return
	 */
	public boolean removeListener(IContentManager.ContentChangedListener listener){
		return mListeners.remove(listener);
	}

	@Override
	public void onEvent(int i, String s) {
		if(i == DELETE || i == MOVED_FROM || i == MODIFY) {
			for (IContentManager.ContentChangedListener listener : mListeners) {
				listener.contentChanged();
			}
		}
	}
}
