package content;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;

/**
 * Created by Alex on 3/6/14.
 */
public class ContentFragment extends Fragment implements AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener {
	private static final int REQUEST_CHOOSER = 1234;
	private static final int CAMERA_REQUEST = 1888;

	private AbsListView mListView;
	private ActionMode mMode;
	private IContentManager mContentManager;
	private ContentAdapter mAdapter;
	private EncryptionService mEncryptionService;
	private boolean mIsBound;
	/**
	 * Remembers which item is currently being selected in single selecton mode
	 */
	private int mSingleSelected;

	public static ContentFragment newInstance(boolean loadEmpty) {
		ContentFragment contentFragment = new ContentFragment();

		Bundle bundle = new Bundle();
		bundle.putBoolean("LOAD_EMPTY", loadEmpty);
		contentFragment.setArguments(bundle);

		return contentFragment;
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			mEncryptionService = ((EncryptionService.ServiceBinder) iBinder).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mEncryptionService = null;
		}
	};

	void doBindService() {
		getActivity().getApplicationContext()
				.bindService(new Intent(getActivity(), EncryptionService.class), mConnection,
						Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			getActivity().getApplicationContext().unbindService(mConnection);
			mIsBound = false;
		}
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
	}

	/**
	 * Loads ContentAdapter and ContentManager
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContentManager = ContentManagerFactory.getInstance(getActivity());
		SharedPreferences preferences =
				PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

		mMode = null;
		mAdapter = new ContentAdapter(mContentManager);
		mContentManager.addContentChangedListener(mAdapter);

		setHasOptionsMenu(true);

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

		mListView = (AbsListView) content.findViewById(R.id.content_container);
		//		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setAdapter(mAdapter);

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

				if (resultCode == Activity.RESULT_OK) {

					final Uri uri = data.getData();

					// Get the File path from the Uri
					String path = FileUtils.getPath(getActivity(), uri);

					// Alternatively, use FileUtils.getFile(Context, Uri)
					if (path != null && FileUtils.isLocal(path)) {
						File selected = new File(path);
						mContentManager.addItem(selected, new IOnResult<Boolean>() {
							@Override
							public void onResult(Boolean result) {
								if (result) {
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
	 * Depending on the selection, this should enable/disable certain actions. For instance: One can only share files
	 * that are unlocked One can only lock files that are unlocked or being unlocked One can only unlock files that are
	 * locked or being locked
	 */
	public void handleActionButtons() {
		// TODO
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
		if (mMode != null) {
			if (mListView.getChoiceMode() == ListView.CHOICE_MODE_SINGLE) {
				if (mSingleSelected == position && mListView.isItemChecked(position)) {
					// the item was already previously set to true, but now we pressed it again, so
					// let's disable it. Selection mode will stop afterwards, because in theory
					// nothing is selected anymore.
					mListView.setItemChecked(position, false);
				}
			}
			else {
				mMode.setTitle(Utils.str(R.string.action_select_multi)
						.replace("{COUNT}", "" + mListView.getCheckedItemIds().length));
				setActionModeIcon(R.drawable.ic_select_multi);
			}
			mSingleSelected = position;
			disableIfNoneChecked();
		}
		else {
			// so we want to try to see how it feels if clicking on a file always starts the
			// selection UI
			mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			mMode = ((ActionBarActivity) getActivity())
					.startSupportActionMode(new ContentShareMultiModeListener());
			mListView.setItemChecked(position, true);
			mSingleSelected = position;

			mMode.setTitle(Utils.str(R.string.action_select_single));
			setActionModeIcon(R.drawable.ic_select_single);
		}
		handleActionButtons();
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

		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		if (mMode == null) {
			mMode = ((ActionBarActivity) getActivity())
					.startSupportActionMode(new ContentShareMultiModeListener());
			mListView.setItemChecked(position, true);
		}
		else {
			mMode.setTitle(Utils.str(R.string.action_select_multi)
					.replace("{COUNT}", "" + mListView.getCheckedItemIds().length));
			setActionModeIcon(R.drawable.ic_select_multi);

			mListView.setItemChecked(position, !mListView.isItemChecked(position));
			disableIfNoneChecked();
		}
		handleActionButtons();

		return true;
	}

	/**
	 * Disables the ActionMode if no more items are checked
	 */
	private void disableIfNoneChecked() {
		if (mListView.getCheckedItemIds().length == 0) {
			mMode.finish();
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
	 * Source: http://www.miximum.fr/porting-the-contextual-anction-mode-for-pre-honeycomb-android-apps.html Helper
	 * class which shows the CAB and
	 */
	private class ContentShareMultiModeListener implements ActionMode.Callback {

		/**
		 * Called when the ActionMode is created. Inflates the ActionMode Menu.
		 *
		 * @param actionMode The mode currently active
		 * @param menu       The menu to which the items should be inflated
		 * @return
		 */
		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.content_action, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
			return false;
		}

		/**
		 * Called when an ActionItem is clicked. Handles removal and sharing of ContentItem
		 *
		 * @param actionMode The mode currently active
		 * @param menuItem   The ActionItem clicked
		 * @return
		 */
		@Override
		public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
			long[] selected = mListView.getCheckedItemIds();
			if (selected.length > 0) {
				ArrayList<ContentItem> itemArrayList = new ArrayList<ContentItem>();
				for (long id : selected) {
					itemArrayList.add(mAdapter.getItem((int) id));
				}
				switch (menuItem.getItemId()) {
					case R.id.action_lock:
						if (!mIsBound) {
							Log.e(this.getClass().toString() + ".onActionItemClicked",
									"encryptionService was not bound");
						}
						mContentManager.encryptItems(itemArrayList, mEncryptionService);
						break;
					case R.id.action_unlock:
						if (!mIsBound) {
							Log.e(this.getClass().toString() + ".onActionItemClicked",
									"encryptionService was not bound");
						}
						mContentManager.decryptItems(itemArrayList, mEncryptionService);
						break;
					case R.id.action_share:
						//TODO share goes here
						// Below is a test to see if the binding with the service was ok
						mEncryptionService.startTestToast();
						break;
					case R.id.action_remove:
						//TODO unlock files if necessary, remove from list (don't delete file)
						break;
					case R.id.action_shred:
						mContentManager.removeItems(itemArrayList, new IOnResult<Boolean>() {
							@Override
							public void onResult(Boolean result) {
								if (result) {
									Utils.toast(R.string.content_success_shred);
								}
								else {
									Utils.toast(R.string.content_fail_shred);
								}
							}
						});

						break;
				}

			}
			actionMode.finish();
			return true;
		}

		/**
		 * Called when the ActionMode is finalized. Unchecks all items in view.
		 *
		 * @param actionMode
		 */
		@Override
		public void onDestroyActionMode(ActionMode actionMode) {
			// Destroying action mode, deselect all items
			for (int i = 0; i < mListView.getAdapter().getCount(); i++) {
				mListView.setItemChecked(i, false);
			}

			if (actionMode == mMode) {
				mMode = null;
			}
		}
	}
}
