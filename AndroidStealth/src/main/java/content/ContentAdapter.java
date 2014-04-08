package content;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.stealth.android.R;
import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedFolder;
import com.stealth.files.IndexedItem;
import com.stealth.utils.Utils;
import encryption.IContentManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple class to display previews of the files. For now it just instantiates ImageView with an icon
 * Created by Alex on 3/6/14.
 */
public class ContentAdapter extends BaseAdapter implements IContentManager.ContentChangedListener {
	private IContentManager mContentManager;
	private List<IndexedItem> mContentItems;
	private ArrayList<CheckableLinearLayout> mViews;

	/**
	 * Creates a new ContentAdapter
	 * @param manager the content manager used to retrieve the actual content
	 */
	public ContentAdapter(IContentManager manager){
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
		//mViews = new SparseArray<View>();
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		IndexedItem item = getItem(i);

		if(view == null){
			view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_content, null);
		}

		if (view instanceof CheckableLinearLayout) {
			((CheckableLinearLayout) view).setItemID(i);
			// remember the views so we can check for whether they are checked
			if (!mViews.contains(view)) {
				mViews.add((CheckableLinearLayout) view);
			}
		}

		if (item instanceof IndexedFolder)
		{
			IndexedFolder folder = (IndexedFolder) item;
			//((TextView)view.findViewById(R.id.file_text)).setText(folder.getName());
		}
		else
		{
			IndexedFile file = (IndexedFile) item;
			File thumb = file.getThumbFile();
			if (thumb.exists()) {
				Bitmap bm = BitmapFactory.decodeFile(thumb.getPath());
				if (bm != null) {
					((ImageView) view.findViewById(R.id.file_thumb)).setImageBitmap(bm);
				}
			}

			if (file.getUnlockedFile().exists()) {

				((ImageView) view.findViewById(R.id.file_status)).setImageResource(R.drawable.ic_status_unlocked);
				view.findViewById(R.id.file_status).setBackgroundColor(Utils.color(R.color.unlocked));
				view.findViewById(R.id.content_item_status_line).setBackgroundColor(Utils.color(R.color.unlocked));
			} else {
				((ImageView) view.findViewById(R.id.file_status)).setImageResource(R.drawable.ic_status_locked);
				view.findViewById(R.id.file_status).setBackgroundColor(Utils.color(R.color.locked));
				view.findViewById(R.id.content_item_status_line).setBackgroundColor(Utils.color(R.color.locked));
			}

			//((TextView)view.findViewById(R.id.file_text)).setText(file.getName());
		}

		return view;
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
	private void setContent(){
		IndexedFolder current = mContentManager.getCurrentFolder();
		mContentItems = new ArrayList<IndexedItem>(mContentManager.getFolders(current));
		mContentItems.addAll(new ArrayList<IndexedItem>(mContentManager.getFiles(current)));
	}
}

