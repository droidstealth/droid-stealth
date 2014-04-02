package content;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stealth.android.R;
import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedFolder;
import com.stealth.files.IndexedItem;
import com.stealth.utils.Utils;

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

    /**
     * Creates a new ContentAdapter
     * @param manager the content manager used to retrieve the actual content
     */
    public ContentAdapter(IContentManager manager){
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_content, null);
        }
        IndexedItem item = getItem(i);
        if (item instanceof IndexedFolder)
        {
            IndexedFolder folder = (IndexedFolder) item;
            ((TextView)view.findViewById(R.id.file_text)).setText(folder.getName());
        }
        else
        {
            IndexedFile file = (IndexedFile) item;
            File thumb = file.getThumbFile();
            if (thumb.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(thumb.getPath());
                if (bm != null) {
                    ((ImageView) view.findViewById(R.id.file_preview)).setImageBitmap(bm);
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

            ((TextView)view.findViewById(R.id.file_text)).setText(file.getName());
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
