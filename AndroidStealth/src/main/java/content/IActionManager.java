package content;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.support.v7.view.ActionMode;
import com.stealth.files.IndexedItem;
import com.stealth.utils.IOnResult;
import encryption.EncryptionManager;

/**
 * Created by Alex on 13-4-2014.
 */
public interface IActionManager {
	public void setEncryptionManager(EncryptionManager encryptionManager);
	public void setActionMode(ActionMode actionMode);
	public void actionShred(ArrayList<IndexedItem> with, IOnResult<Boolean> listener);
	public void actionOpen(IndexedItem with, IOnResult<Boolean> listener, Context context);
	public void actionLock(ArrayList<IndexedItem> with, IOnResult<Boolean> listener);
	public void actionUnlock(ArrayList<IndexedItem> with, IOnResult<Boolean> listener);
	public void actionShare(ArrayList<IndexedItem> with, IOnResult<Boolean> listener);
	public void actionRestore(ArrayList<IndexedItem> with, IOnResult<Boolean> listener);
}
