package content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.stealth.android.R;
import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedFolder;
import com.stealth.files.IndexedItem;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import encryption.IContentManager;

/**
 * Simple class to display previews of the files. For now it just instantiates ImageView with an icon Created by Alex on
 * 3/6/14.
 */
public class ContentAdapter extends BaseAdapter implements IContentManager.ContentChangedListener {

	private IContentManager mContentManager;
	private List<IndexedItem> mContentItems;
	private HashMap<View, Integer> mViewToPositions;
	private HashMap<IndexedItem, View> mItemToView;
	private GridView mGridView;
	private IndexedFolder mLastFolder;
	private HashSet<IndexedItem> mAnimCheck;
	private long mLastAnimTime = 0;
	private IAdapterChangedListener mListener;

	public IAdapterChangedListener getAdapterChangedListener() {
		return mListener;
	}

	public void setAdapterChangedListener(IAdapterChangedListener listener) {
		mListener = listener;
	}

	/**
	 * Creates a new ContentAdapter
	 *
	 * @param manager the content manager used to retrieve the actual content
	 */
	public ContentAdapter(IContentManager manager, GridView gridView) {
		mViewToPositions = new HashMap<View, Integer>();
		mItemToView = new HashMap<IndexedItem, View>();
		mAnimCheck = new HashSet<IndexedItem>();
		mContentManager = manager;
		mGridView = gridView;
		setContent();
	}

	@Override
	public int getCount() {
		return mContentItems.size();
	}

	@Override
	public IndexedItem getItem(int i) {
		return mContentItems.get(i);
	}

	public Set<View> getViews() {
		return mViewToPositions.keySet();
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return mContentItems.isEmpty();
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	/**
	 * Handles the selection for all current views
	 */
	public void handleSelections() {
		for (View view : getViews()) {
			handleSelection(view);
		}
	}

	/**
	 * Handles the selection UI of given view
	 *
	 * @param view the view to handle its selection of
	 */
	public void handleSelection(View view) {
		if (view == null) {
			return;
		}
		if (mGridView.isItemChecked(mViewToPositions.get(view))) {
			view.findViewById(R.id.file_select).setBackgroundResource(R.drawable.frame_selected);
		}
		else {
			view.findViewById(R.id.file_select).setBackgroundResource(0);
		}
	}

	/**
	 * Handle the fade-in animation based on the time of the last animation. This ensures animations timeslots of 20,
	 * but immediate animation when last filled slot already passed
	 *
	 * @param v the view to animate
	 */
	private void fadeIn(View v) {
		long t = mLastAnimTime + 20;
		long now = System.currentTimeMillis();
		if (t < now) {
			t = now;
		}
		mLastAnimTime = t;
		Utils.fadein(v, t - now);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = LayoutInflater.from(Utils.getContext()).inflate(R.layout.item_content, null);
			if (view == null) {
				return null;
			}
		}

		IndexedItem item = getItem(i);
		if (!mAnimCheck.contains(item)) {
			mAnimCheck.add(item);
			Utils.fadein(view, i * 20);
			fadeIn(view);
		}

		mViewToPositions.put(view, i);
		mItemToView.put(item, view);

		// style the view accordingly
		if (item instanceof IndexedFolder) {
			styleFolderView((IndexedFolder) item, view);
		}
		else {
			styleFileView((IndexedFile) item, view);
		}

		handleSelection(view);

		return view;
	}

	/**
	 * Sets the thumbnail of given file, but only if there is a view for it at the moment
	 *
	 * @param file the file to display the thumbnail of
	 */
	private void displayThumbnail(IndexedFile file) {
		// Get the view associated with this file. However, note that the view might be assigned to
		// some other item already... so let's also check if the information is still up to date
		View v = mItemToView.get(file);
		int i = mViewToPositions.get(v);
		if (mContentItems.get(i) != file) {
			return; // information was out of date
		}

		if (file.getThumbnail() != null) {
			ImageView thumbImage = (ImageView) v.findViewById(R.id.file_preview);
			thumbImage.setImageBitmap(file.getThumbnail());
			thumbImage.invalidate();
		}
		else {
			Utils.d("We failed to get the bitmap :(");
		}
	}

	private void styleFolderView(IndexedFolder folder, View view) {
		// TODO #79
	}

	private void styleFileView(final IndexedFile file, final View view) {

		ImageView thumbImage = (ImageView) view.findViewById(R.id.file_preview);
		ImageView statusImage = (ImageView) view.findViewById(R.id.file_status);
		ImageView statusImageBG = (ImageView) view.findViewById(R.id.file_status_background);
		View statusBar = view.findViewById(R.id.content_item_status_line);

		boolean isUnlocked = file.isUnlocked();

		thumbImage.setImageBitmap(null);
		thumbImage.invalidate();

		if (file.getThumbFile().exists()) {

			IOnResult<Boolean> displayThumb = new IOnResult<Boolean>() {
				@Override
				public void onResult(Boolean result) {
					displayThumbnail(file);
				}
			};

			boolean modified = isUnlocked && file.isModified();
			if (file.getThumbnail() == null || modified) {
				if (modified) {
					Utils.d("A file has been modified! Getting new thumbnail.");
					file.resetModificationChecker();
					ThumbnailManager.createThumbnail(file, displayThumb);
				}
				else {
					ThumbnailManager.retrieveThumbnail(file, displayThumb);
				}
			}
			else {
				displayThumb.onResult(true);
			}
		}

		if (isUnlocked) {
			statusImage.clearAnimation();
			statusImage.setImageResource(R.drawable.ic_status_unlocked);
			statusImageBG.setBackgroundColor(Utils.color(R.color.unlocked));
			view.findViewById(R.id.content_item_status_line).setBackgroundColor(Utils.color(R.color.unlocked));
		}
		else if (file.isLocked()) {
			statusImage.clearAnimation();
			statusImage.setImageResource(R.drawable.ic_status_locked);
			statusImageBG.setBackgroundColor(Utils.color(R.color.locked));
			statusBar.setBackgroundColor(Utils.color(R.color.locked));
		}
		else {
			statusImage.setImageResource(R.drawable.ic_status_processing);
			statusImageBG.setBackgroundColor(Utils.color(R.color.processing));
			statusBar.setBackgroundColor(Utils.color(R.color.processing));
			if (view.getContext() != null) {
				statusImage.setAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate));
			}
		}
	}

	/**
	 * retrieves content and notifies listeners of change in data
	 */
	@Override
	public void contentChanged() {
		Utils.d("contentChanged");
		setContent();
		notifyDataSetChanged();
	}

	/**
	 * Retrieves the content from the manager
	 */
	private void setContent() {
		IndexedFolder current = mContentManager.getCurrentFolder();

		if (mLastFolder != current && mContentItems != null) {
			for (IndexedItem item : mContentItems) {
				if (item instanceof IndexedFile) {
					// save some memory by clearing the thumbnails
					((IndexedFile) item).clearThumbnail();
				}
			}
		}

		mLastFolder = current;

		mContentItems = new ArrayList<IndexedItem>(mContentManager.getFolders(current));
		mContentItems.addAll(new ArrayList<IndexedItem>(mContentManager.getFiles(current)));

		if (mListener != null) {
			mListener.onAdapterChanged();
		}
	}

	public interface IAdapterChangedListener {
		void onAdapterChanged();
	}
}

