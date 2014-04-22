package content;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.support.v7.view.ActionMode;
import com.stealth.android.HomeActivity;
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
	public void actionOpen(HomeActivity activity, IndexedItem with, IOnResult<Boolean> listener);
	public void actionLock(ArrayList<IndexedItem> with, IOnResult<Boolean> listener);
	public void actionUnlock(ArrayList<IndexedItem> with, IOnResult<Boolean> listener);
	public void actionShare(HomeActivity activity, ArrayList<IndexedItem> with, IOnResult<Boolean> listener);
	public void actionRestore(ArrayList<IndexedItem> with, IOnResult<Boolean> listener, File exportDir);
}
