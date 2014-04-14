package content;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
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
	private ArrayList<CheckableLinearLayout> mViews;
	private IndexedFolder mLastFolder;

	/**
	 * Creates a new ContentAdapter
	 *
	 * @param manager the content manager used to retrieve the actual content
	 */
	public ContentAdapter(IContentManager manager) {
		mViews = new ArrayList<CheckableLinearLayout>();
		mContentManager = manager;
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

	public int getItemId(IndexedItem item) {
		return mContentItems.indexOf(item);
	}

	public ArrayList<CheckableLinearLayout> getViews() {
		return mViews;
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

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		IndexedItem item = getItem(i);

		if (view == null) {
			view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_content, null);
		}

		// if it is a checkable layout, remember it.
		if (view instanceof CheckableLinearLayout) {
			((CheckableLinearLayout) view).setItemID(i);
			// remember the views so we can check for whether they are checked
			if (!mViews.contains(view)) {
				mViews.add((CheckableLinearLayout) view);
			}
		}

		// style the view accordingly
		if (item instanceof IndexedFolder) {
			styleFolderView((IndexedFolder) item, view);
		}
		else {
			styleFileView((IndexedFile) item, view);
		}

		return view;
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

		IOnResult<Boolean> notifyChanges = new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean result) {
				if (result) {
					Utils.d("We have a bitmap to show!");
					notifyDataSetChanged();
				}
				else {
					Utils.d("We failed to get the bitmap :(");
				}
			}
		};

		if (file.getThumbFile().exists()) {
			boolean modified = isUnlocked && file.isModified();
			if (file.getThumbnail() == null || modified) {
				if (modified) {
					Utils.d("A file has been modified! Getting new thumbnail.");
					file.resetModificationChecker();
					ThumbnailManager.createThumbnail(file, notifyChanges);
				}
				else {
					ThumbnailManager.retrieveThumbnail(file, notifyChanges);
				}
			}
			else {
				thumbImage.setImageBitmap(file.getThumbnail());
				thumbImage.invalidate();
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
	}
}

