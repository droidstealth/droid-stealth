package content;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.stealth.android.R;

import java.util.ArrayList;

/**
 * Created by Alex on 3/6/14.
 */
public class ContentFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private AbsListView mListView;
    private ActionMode mMode;
    private IContentManager mContentManager;
    private ContentAdapter mAdapter;

    /**
     * Loads ContentAdapter
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentManager = new DummyManager();

        mMode = null;
        mAdapter = new ContentAdapter(mContentManager);
        mContentManager.addContentChangedListener(mAdapter);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.content, menu);
    }

    /**
     * Creates a new content view and sets its listeners
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_content, container,false);

        mListView = (AbsListView) content.findViewById(R.id.content_container);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mListView.setAdapter(mAdapter);

        return content;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.content_add:
                //TODO filepicker goes here
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks whether an item is selected and enables or disables the ActionMode based on that
     * @param adapterView
     * @param view
     * @param position
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(mMode != null){
            disableIfNone();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

        if (mMode == null) {
            mListView.setItemChecked(position, true);
            mMode = ((ActionBarActivity)getActivity()).startSupportActionMode(new ContentShareMultiModeListener());
        }
        else {
            disableIfNone();
        }

        return true;
    }

    private void disableIfNone(){
        if(mListView.getCheckedItemIds().length == 0){
            mMode.finish();
        }
    }

    /**
     * Source: http://www.miximum.fr/porting-the-contextual-anction-mode-for-pre-honeycomb-android-apps.html
     * Helper class which shows the CAB and
     */
    private class ContentShareMultiModeListener implements ActionMode.Callback {

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

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            long[] selected = mListView.getCheckedItemIds();
            if (selected.length > 0) {
                switch (menuItem.getItemId()){
                    case R.id.action_share:
                        //TODO share goes here
                        break;
                    case R.id.action_remove:
                        ArrayList<ContentItem> itemArrayList = new ArrayList<ContentItem>();
                        for (long id: selected) {
                            itemArrayList.add(mAdapter.getItem((int)id));
                        }
                        mContentManager.removeItems(itemArrayList);

                        break;
                }

            }
            actionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            // Destroying action mode, deselect all items
            for (int i = 0; i < mListView.getAdapter().getCount(); i++)
                mListView.setItemChecked(i, false);

            if (actionMode == mMode) {
                mMode = null;
            }
        }
    }
}
