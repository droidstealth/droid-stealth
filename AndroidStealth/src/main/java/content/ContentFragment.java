package content;

import java.io.File;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.dialog.DialogConstructor;
import com.stealth.dialog.DialogOptions;
import com.stealth.dialog.IDialogResponse;
import com.stealth.files.FileIndex;
import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedFolder;
import com.stealth.files.IndexedItem;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import encryption.EncryptionManager;
import encryption.EncryptionService;
import encryption.IContentManager;
import sharing.SharingUtils;

/**
 * Please only instantiate me if you have created the file index successfully Created by Alex on 3/6/14.
 */
public class ContentFragment extends Fragment implements AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener, EncryptionService.IUpdateListener {
	private static final int REQUEST_CHOOSER = 1234;
	private static final int CAMERA_REQUEST = 1888;
	private GridView mGridView;
	private android.support.v7.view.ActionMode mMode;
	private ContentShareMultiModeListener mMultiModeListener;
	private IContentManager mContentManager;
	private ContentAdapter mAdapter;
	private EncryptionManager mEncryptionManager;
	private EncryptionService mEncryptionService;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			mEncryptionService = ((EncryptionService.ServiceBinder) iBinder).getService();
			mEncryptionManager = EncryptionManager.create(mEncryptionService);
			Utils.d("Encryption manager is connected!");
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mEncryptionManager = null;
			mEncryptionService = null;
			Utils.d("Encryption manager is disconnected..?");
		}
	};
	private NfcAdapter mNfcAdapter;
	private boolean mIsBound;
	/**
	 * Remembers which item is currently being selected in single selection mode
	 */
	private int mSingleSelected;
	private IOnResult<Boolean> mUpdateList = new IOnResult<Boolean>() {
		@Override
		public void onResult(Boolean result) {
			Utils.runOnMain(new Runnable() {
				@Override
				public void run() {
					Utils.d("updating list");
					mContentManager.notifyContentChangedListeners();
					handleActionButtons();
				}
			});
		}
	};

	public static ContentFragment newInstance(boolean loadEmpty) {
		ContentFragment contentFragment = new ContentFragment();

		Bundle bundle = new Bundle();
		bundle.putBoolean("LOAD_EMPTY", loadEmpty);
		contentFragment.setArguments(bundle);

		return contentFragment;
	}

	void doBindService() {
		Utils.d("Trying to bind service");
		EncryptionService.addUpdateListener(ContentFragment.this);
		getActivity().getApplicationContext()
				.bindService(new Intent(getActivity(), EncryptionService.class), mConnection,
						Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			Utils.d("Trying to unbind service");

			getActivity().getApplicationContext().unbindService(mConnection);
			EncryptionService.removeUpdateListener(this);
			mIsBound = false;
		}
	}

	/**
	 * Loads ContentAdapter and ContentManager
	 *
	 * @param savedInstanceState
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContentManager = ContentManagerFactory.getInstance(
				getActivity(),
				FileIndex.get());

		Utils.d("Created content fragment");

		mMode = null;
		mAdapter = new ContentAdapter(mContentManager);
		mContentManager.addContentChangedListener(mAdapter);

		if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)
				&& (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)) {
			mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
			mNfcAdapter.setBeamPushUrisCallback(new FileUriCallback(), getActivity());
		}

		setHasOptionsMenu(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		doUnbindService();
	}

	@Override
	public void onResume() {
		super.onResume();
		doBindService();
		mUpdateList.onResult(true);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onEncryptionServiceUpdate() {
		if (getActivity() != null) {
			mUpdateList.onResult(true);
		} else {
			Utils.d("Calling the service IUpdateListener but its activity does not exist anymore. " +
					"We should have stopped listening.. why didn't we?");
		}
	}


	/**
	 * Inflates normal Menu.
	 *
	 * @param menu     The menu to which the items should be inflated
	 * @param inflater The inflater which is used to inflate the Menu
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.content, menu);
	}

	/**
	 * Creates a new content view and sets its listeners
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View content = inflater.inflate(R.layout.fragment_content, container, false);

		mGridView = (GridView) content.findViewById(R.id.content_container);
		//		mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);
		mGridView.setAdapter(mAdapter);

		return content;
	}

	/**
	 * Called when a MenuItem is clicked. Handles adding of items
	 *
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.content_add:
				Intent getContentIntent = FileUtils.createGetContentIntent();
				Intent intent = Intent.createChooser(getContentIntent, "Select a file");
				((HomeActivity) getActivity()).setRequestedActivity(true);
				startActivityForResult(intent, REQUEST_CHOOSER);
				return true;
			case R.id.content_make:
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				//cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFolder));
				((HomeActivity) getActivity()).setRequestedActivity(true);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Listens for the return of the get content intent. Adds the items if successful
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CAMERA_REQUEST:
			case REQUEST_CHOOSER:

				if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST) {

					if (data == null) {
						Utils.d("Oops... Result was OK, but intent was null. That's just great.");
						return;
					}

					final Uri uri = data.getData();

					if (uri == null) {
						Utils.d("Oops... Result was OK, but uri was null. That's just great.");
						return;
					}

					// Get the File path from the Uri
					String path = FileUtils.getPath(Utils.getContext(), uri);

					// Alternatively, use FileUtils.getFile(Context, Uri)
					if (path != null && FileUtils.isLocal(path)) {
						File file = new File(path);
						IndexedFolder dir = mContentManager.getCurrentFolder();
						mContentManager.addFile(dir, file, new IOnResult<IndexedFile>() {
							@Override
							public void onResult(IndexedFile result) {
								if (result != null) {

									ArrayList<IndexedItem> itemList = new ArrayList<IndexedItem>();
									itemList.add(result);
									actionLock(itemList, null); // lock right now

									Utils.toast(R.string.content_success_add);
								}
								else {
									Utils.toast(R.string.content_fail_add);
								}
							}
						});
					}
				}
				break;
		}
	}

	/**
	 * @return all currently selected indexed items
	 */
	public ArrayList<IndexedItem> getSelectedItems() {
		long[] selected = mGridView.getCheckedItemIds();
		ArrayList<IndexedItem> selectedItems = new ArrayList<IndexedItem>();
		for (long id : selected) {
			selectedItems.add(mAdapter.getItem((int) id));
		}
		return selectedItems;
	}

	/**
	 * @return the correct content action mode based on current selection
	 */
	private ContentActionMode getContentActionMode() {
		// TODO also support folder. Currently only looking at files, until #79 is made
		ArrayList<IndexedItem> selectedItems = getSelectedItems();

		boolean locked = false;
		boolean unlocked = false;

		for (IndexedItem item : selectedItems) {
			if (item instanceof IndexedFile) {
				IndexedFile file = (IndexedFile) item;
				locked |= file.isLocked();
				unlocked |= file.isUnlocked();
			}
		}

		if (selectedItems.size() > 1) {
			if (locked && unlocked) {
				return ContentActionMode.MULTI_MIXED;
			}
			else if (locked) {
				return ContentActionMode.MULTI_LOCKED;
			}
			else if (unlocked) {
				return ContentActionMode.MULTI_UNLOCKED;
			}
			else {
				return ContentActionMode.PROCESSING;
			}
		}
		else {
			if (locked) {
				return ContentActionMode.SINGLE_LOCKED;
			}
			else if (unlocked) {
				return ContentActionMode.SINGLE_UNLOCKED;
			}
			else {
				return ContentActionMode.PROCESSING;
			}
		}
	}

	/**
	 * Depending on the selection, this should enable/disable certain actions. For instance: One can only share files
	 * that are unlocked One can only lock files that are unlocked or being unlocked One can only unlock files that are
	 * locked or being locked
	 */
	public void handleActionButtons() {
		if (mMultiModeListener != null) {
			mMultiModeListener.inflate(getContentActionMode());
		}
	}

	/**
	 * Handles the selection UI.
	 */
	public void handleSelection() {
		for (CheckableLinearLayout view : mAdapter.getViews()) {
			if (view != null) {
				int id = view.getItemID();
				// keep this debug line commented, just in case we need to do these checks again
				// Utils.debug("do you even goat bro? ItemChecked? " + mGridView.isItemChecked(id) + " Activated? " +
				// view.isActivated() + "; Checked? " + view.isChecked() + "; Enabled? " + view.isEnabled() + ";
				// InLayout? " + view.isInLayout() + "; Selected? " + view.isSelected() + "; Shown? " + view.isShown
				// ());
				if (mGridView.isItemChecked(id)) {
					view.findViewById(R.id.file_select).setBackgroundResource(R.drawable.frame_selected);
				}
				else {
					view.findViewById(R.id.file_select).setBackgroundResource(0);
				}
			}
		}
	}

	/**
	 * Because a Checkable is used, it needs to be unchecked when the view is not in ActionMode. If the view is in
	 * ActionMode, check whether any items are still checked after the click.
	 *
	 * @param adapterView
	 * @param view
	 * @param position
	 * @param l
	 */
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
		if (isSelecting()) {
			if (isSingleSelecting()) {
				if (mSingleSelected == position && mGridView.isItemChecked(position)) {
					// the item was already previously set to true, but now we pressed it again, so
					// let's disable it. Selection mode will stop afterwards, because in theory
					// nothing is selected anymore.
					mGridView.setItemChecked(position, false);
					showSingleSelectionFeedback();
				}
			}
			else {
				showMultiSelectionFeedback();
			}
			mSingleSelected = position;
			disableIfNoneChecked();
		}
		else {
			startSingleSelection(position);
		}
		handleActionButtons();
		handleSelection();
	}

	/**
	 * Enables ActionMode if it's not active. Otherwise make sure the ActionMode can still be active.
	 *
	 * @param adapterView
	 * @param view
	 * @param position
	 * @param l
	 * @return
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

		if (isMultiSelecting()) {
			mGridView.setItemChecked(position, !mGridView.isItemChecked(position));
			showMultiSelectionFeedback();
			disableIfNoneChecked();
		}
		else if (isSingleSelecting()) {
			startMultiSelection(position);
			mGridView.setItemChecked(mSingleSelected, true);
		}
		else {
			startMultiSelection(position);
		}

		handleActionButtons();
		handleSelection();

		return true;
	}

	/**
	 * Starts the single selection mode with given file
	 *
	 * @param withItemId the item to select
	 */
	public void startSingleSelection(int withItemId) {
		mGridView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mMultiModeListener = new ContentShareMultiModeListener();
		mMode = ((ActionBarActivity) getActivity()).startSupportActionMode(mMultiModeListener);
		mGridView.setItemChecked(withItemId, true);
		mSingleSelected = withItemId;

		showSingleSelectionFeedback();
	}

	/**
	 * Starts the multi selection mode with given file
	 *
	 * @param withItemId the item to select
	 */
	public void startMultiSelection(int withItemId) {
		mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mMultiModeListener = new ContentShareMultiModeListener();
		mMode = ((ActionBarActivity) getActivity()).startSupportActionMode(mMultiModeListener);
		mGridView.setItemChecked(withItemId, true);

		showMultiSelectionFeedback();
	}

	/**
	 * Check if we are currently in multi selection mode
	 */
	public boolean isMultiSelecting() {
		return isSelecting() && mGridView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE;
	}

	/**
	 * Check if we are currently in single selection mode
	 */
	public boolean isSingleSelecting() {
		return isSelecting() && mGridView.getChoiceMode() == ListView.CHOICE_MODE_SINGLE;
	}

	/**
	 * Check if we are currently in a selection mode
	 */
	public boolean isSelecting() {
		return mMode != null;
	}

	/**
	 * Shows the feedback of the single selection mode
	 */
	private void showSingleSelectionFeedback() {
		mMode.setTitle(Utils.str(R.string.action_select_single));
		setActionModeIcon(R.drawable.ic_select_single);
	}

	/**
	 * Shows the feedback of the multi selection mode
	 */
	private void showMultiSelectionFeedback() {
		mMode.setTitle(Utils.str(R.string.action_select_multi)
				.replace("{COUNT}", "" + mGridView.getCheckedItemIds().length));
		setActionModeIcon(R.drawable.ic_select_multi);
	}

	/**
	 * Disables the ActionMode if no more items are checked
	 */
	private void disableIfNoneChecked() {
		if (mGridView.getCheckedItemIds().length == 0 && mMode != null) {
			mMultiModeListener = null;
			finishActionMode(mMode);
		}
	}

	/**
	 * Sets the resource of the action mode in a hacky way
	 *
	 * @param resource
	 */
	private void setActionModeIcon(int resource) {
		try {
			int doneButtonId = Resources.getSystem().getIdentifier("action_mode_close_button", "id", "android");
			LinearLayout layout = (LinearLayout) getActivity().findViewById(doneButtonId);
			((ImageView) layout.getChildAt(0)).setImageResource(resource);
		}
		catch (Exception e) {
			// could not set image
		}
	}

	/**
	 * Locks all items
	 *
	 * @param with the items to perform this action on
	 * @param actionMode the mode to finish if desired
	 */
	public void actionLock(ArrayList<IndexedItem> with, final android.support.v7.view.ActionMode actionMode) {
		if (!mIsBound) {
			Utils.d("EncryptionService was not bound");
		}

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

		mEncryptionManager.encryptItems(with, new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean result) {
				if (result) {
					finishActionMode(actionMode);
				}
			}
		});
	}

	/**
	 * Unlocks all items
	 *
	 * @param with the items to perform this action on
	 * @param actionMode the mode to finish if desired
	 */
	public void actionUnlock(ArrayList<IndexedItem> with, final android.support.v7.view.ActionMode actionMode) {
		if (!mIsBound) {
			Log.e(this.getClass().toString() + ".onActionItemClicked",
					"encryptionService was not bound");
		}

		mEncryptionManager.decryptItems(with, new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean result) {
				if (result) {
					finishActionMode(actionMode);
				}
			}
		});
	}

	/**
	 * Shreds all items
	 *
	 * @param with the items to perform this action on
	 * @param actionMode the mode to finish if desired
	 */
	public void actionShred(final ArrayList<IndexedItem> with, final android.support.v7.view.ActionMode actionMode) {

		final IOnResult<Boolean> shredListener = new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean result) {
				if (result) {
					Utils.toast(R.string.content_success_shred);
					finishActionMode(actionMode);
				}
				else {
					Utils.toast(R.string.content_fail_shred);
				}
			}
		};

		DialogOptions options = new DialogOptions()
				.setTitle(R.string.dialog_shred_title)
				.setDescription(R.string.dialog_shred_description)
				.setNegative(R.string.cancel)
				.setPositive(R.string.yes)
				.setReverseColors(true);

		DialogConstructor.show(
				getActivity(),
				options,
				new IDialogResponse() {
					@Override
					public void onPositive(ArrayList<String> input) {
						mContentManager.removeItems(with, shredListener);
						disableIfNoneChecked();
					}

					@Override
					public void onNegative() {
						// do nothing
					}

					@Override
					public void onCancel() {
						// do nothing
					}
				}
		);
	}

	public enum ContentActionMode {
		SINGLE_LOCKED, SINGLE_UNLOCKED, MULTI_LOCKED, MULTI_UNLOCKED, MULTI_MIXED, PROCESSING
	}

	/**
	 * Finishes the action mode on the UI thread
	 * @param actionMode the action mode the finish
	 */
	private void finishActionMode(final android.support.v7.view.ActionMode actionMode) {
		if (actionMode == null) return;
		Utils.runOnMain(new Runnable() {
			@Override
			public void run() {
				actionMode.finish();
			}
		});
	}

	/**
	 * Source: http://www.miximum.fr/porting-the-contextual-anction-mode-for-pre-honeycomb-android-apps.html Helper
	 * class which shows the CAB and
	 */
	private class ContentShareMultiModeListener implements android.support.v7.view.ActionMode.Callback {

		private ContentActionMode mContentMode;
		private Menu mMenu;

		/**
		 * Called when the ActionMode is created. Inflates the ActionMode Menu.
		 *
		 * @param actionMode The mode currently active
		 * @param menu       The menu to which the items should be inflated
		 * @return
		 */
		@Override
		public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
			mMenu = menu;
			inflate(mContentMode);
			return true;
		}

		/**
		 * Inflates the given content action mode to fit the selected context
		 *
		 * @param mode the mode to inflate
		 */
		public void inflate(ContentActionMode mode) {
			if (mode == mContentMode) {
				return; // already inflated
			}

			Utils.d("Inflating " + mode);

			mContentMode = mode;
			mMenu.clear();
			MenuInflater inflater = getActivity().getMenuInflater();

			switch (mode) {
				case MULTI_LOCKED:
					inflater.inflate(R.menu.content_action_multi_locked, mMenu);
					break;
				case MULTI_UNLOCKED:
					inflater.inflate(R.menu.content_action_multi_unlocked, mMenu);
					break;
				case MULTI_MIXED:
					inflater.inflate(R.menu.content_action_multi_mixed, mMenu);
					break;
				case PROCESSING:
					inflater.inflate(R.menu.content_action_processing, mMenu);
					break;
				case SINGLE_LOCKED:
					inflater.inflate(R.menu.content_action_single_locked, mMenu);
					break;
				case SINGLE_UNLOCKED:
					inflater.inflate(R.menu.content_action_single_unlocked, mMenu);
					break;
			}
		}

		@Override
		public boolean onPrepareActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
			return false;
		}

		/**
		 * Called when an ActionItem is clicked. Handles removal and sharing of files
		 *
		 * @param actionMode The mode currently active
		 * @param menuItem   The ActionItem clicked
		 * @return
		 */
		@Override
		public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem menuItem) {

			ArrayList<IndexedItem> selectedItems = getSelectedItems();

			if (selectedItems.size() == 0) {
				finishActionMode(actionMode);
				return false;
			}

			switch (menuItem.getItemId()) {
				case R.id.action_lock:
					actionLock(selectedItems, actionMode);
					break;
				case R.id.action_unlock:
					actionUnlock(selectedItems, actionMode);
					break;
				case R.id.action_share:
					//TODO share goes here
					break;
				case R.id.action_restore:
					//TODO unlock files if necessary, remove from list and restore to choosen/original location (don't
					// delete file)
					break;
				case R.id.action_open:
					//TODO open selected file
					// delete file)
					break;
				case R.id.action_shred:
					actionShred(selectedItems, actionMode);
					break;
			}

			return true;
		}

		/**
		 * Called when the ActionMode is finalized. Unchecks all items in view.
		 *
		 * @param actionMode
		 */
		@Override
		public void onDestroyActionMode(android.support.v7.view.ActionMode actionMode) {
			// Destroying action mode, deselect all items
			for (int i = 0; i < mGridView.getAdapter().getCount(); i++) {
				mGridView.setItemChecked(i, false);
			}
			handleSelection();

			if (actionMode == mMode) {
				mMode = null;
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private class FileUriCallback implements NfcAdapter.CreateBeamUrisCallback {

		@Override
		public Uri[] createBeamUris(NfcEvent nfcEvent) {
			return new Uri[] { getApkUri() };
		}

		private Uri getApkUri() {
			return Uri.fromFile(SharingUtils.getApk(getActivity()));
		}
	}
}
