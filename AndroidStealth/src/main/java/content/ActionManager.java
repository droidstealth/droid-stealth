package content;

import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.view.ActionMode;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.stealth.android.R;
import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedItem;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import encryption.EncryptionManager;
import encryption.IContentManager;

/**
 * Created by Alex on 13-4-2014.
 */
public class ActionManager implements IActionManager {

	private ActionMode mActionMode;
	private IContentManager mContentManager;
	private EncryptionManager mEncryptionManager;

	public ActionManager(IContentManager contentManager){
		mContentManager = contentManager;
	}

	@Override
	public void setEncryptionManager(EncryptionManager encryptionManager) {
		mEncryptionManager = encryptionManager;
	}

	@Override
	public void setActionMode(ActionMode actionMode) {
		mActionMode = actionMode;
	}

	@Override
	public void actionShred(ArrayList<IndexedItem> with, IOnResult<Boolean> listener) {
		mContentManager.removeItems(with, getWrapper(listener), true);
	}

	@Override
	public void actionOpen(Context context, IndexedItem with, IOnResult<Boolean> listener) {
		if(!(with instanceof IndexedFile)){
			return;
		}

		IndexedFile file = (IndexedFile)with;
		Uri uri = Uri.fromFile(file.getUnlockedFile());

		Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);

		MimeTypeMap myMime = MimeTypeMap.getSingleton();
		String mimeType = myMime.getMimeTypeFromExtension(file.getExtension().substring(1));
		newIntent.setDataAndType(uri,mimeType);
		newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);

		try {
			context.startActivity(newIntent);
		} catch (android.content.ActivityNotFoundException e) {
			Toast.makeText(context, "No handler for file " + file.getUnlockedFilename(), 4000).show();
		}

		if(listener != null)
			listener.onResult(true);
	}

	@Override
	public void actionLock(ArrayList<IndexedItem> with, final IOnResult<Boolean> listener) {
		if(mEncryptionManager == null)
			Utils.d("Called lock when encryption service not bound!");

		if (with == null) {
			Utils.d("We got an empty list to process. Can't deal with this.");
			return;
		}

		for (IndexedItem item : with) {
			if (item instanceof IndexedFile) {
				// clear the thumbnails because if we lock the file
				// it might have changed
				((IndexedFile) item).clearThumbnail();
			}
		}

		mEncryptionManager.encryptItems(with, getWrapper(listener));
	}

	@Override
	public void actionUnlock(ArrayList<IndexedItem> with, final IOnResult<Boolean> listener) {
		if(mEncryptionManager == null)
			Utils.d("Called unlock when encryption service not bound!");

		mEncryptionManager.decryptItems(with, getWrapper(listener));
	}

	@Override
	public void actionShare(Context context, ArrayList<IndexedItem> with, IOnResult<Boolean> listener) {
		if(with.size() == 1 && with.get(0) instanceof IndexedFile) {
			IndexedFile file = (IndexedFile)with.get(0);

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_file));

			MimeTypeMap map = MimeTypeMap.getSingleton();
			String mimeType = map.getMimeTypeFromExtension(file.getExtension().substring(1));
			intent.setType(mimeType);

			Uri uri = Uri.fromFile(file.getUnlockedFile());
			intent.putExtra(Intent.EXTRA_STREAM, uri);

			try {
				context.startActivity(intent);
			}catch (ActivityNotFoundException e){
				Utils.toast(R.string.share_not_found);
			}


		}
		else {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND_MULTIPLE);
			intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_files));


			ArrayList<Uri> files = new ArrayList<Uri>();

			ArrayList<String> mimes = new ArrayList<String>();
			MimeTypeMap myMime = MimeTypeMap.getSingleton();

			for (IndexedItem item : with) {
				if (item instanceof IndexedFile) {
					IndexedFile file = (IndexedFile) item;
					Uri uri = Uri.fromFile(file.getUnlockedFile());
					String mimeType = myMime.getMimeTypeFromExtension(file.getExtension().substring(1));
					if(!mimes.contains(mimeType))
						mimes.add(mimeType);
					files.add(uri);
				}
			}

			StringBuilder sb = new StringBuilder();
			for (String mime : mimes){
				sb.append(mime);
				sb.append(',');
			}

			sb.deleteCharAt(sb.length()-1);

			String mimeType = sb.toString();
			Utils.d("Mimetype: " + mimeType);
			intent.setType(mimeType);

			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);

			try {
				context.startActivity(intent);
			}catch (ActivityNotFoundException e){
				Utils.toast(R.string.multi_share_not_found);
			}

		}
		if(listener != null)
			listener.onResult(true);
	}

	@Override
	public void actionRestore(ArrayList<IndexedItem> with, final IOnResult<Boolean> listener) {

		mContentManager.removeItems(with, getWrapper(listener), false);
	}

	private void finishActionMode() {
		if (mActionMode != null) {
			Utils.runOnMain(new Runnable() {
				@Override
				public void run() {
					mActionMode.finish();
				}
			});
		}
	}

	/**
	 * returns a wrapper which disables the action mode when the result comes back, and calls the original wrapper after
	 * @param listener
	 * @return
	 */
	private IOnResult<Boolean> getWrapper(final IOnResult<Boolean> listener){
		return new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean result) {
				if(result) {
					finishActionMode();
				}

				if(listener != null)
					listener.onResult(result);
			}
		};
	}
}
