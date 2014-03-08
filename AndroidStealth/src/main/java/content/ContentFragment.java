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
     * Loads ContentAdapter and ContentManager
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentManager = ContentManagerFactory.getInstance();

        mMode = null;
        mAdapter = new ContentAdapter(mContentManager);
        mContentManager.addContentChangedListener(mAdapter);

        setHasOptionsMenu(true);
    }

    /**
     * Inflates normal Menu.
     * @param menu The menu to which the items should be inflated
     * @param inflater The inflater which is used to inflate the Menu
     */
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

    /**
     * Called when a MenuItem is clicked. Handles adding of items //TODO
     * @param item
     * @return
     */
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
     * Because a Checkable is used, it needs to be unchecked when the view is not in ActionMode.
     * If the view is in ActionMode, check whether any items are still checked after the click.
     * @param adapterView
     * @param view
     * @param position
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(mMode != null){
            disableIfNoneChecked();
        }
        else {
            mListView.setItemChecked(position, false);
        }
    }

    /**
     * Enables ActionMode if it's not active. Otherwise make sure the ActionMode can still be active.
     * @param adapterView
     * @param view
     * @param position
     * @param l
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

        if (mMode == null) {
            mListView.setItemChecked(position, true);
            mMode = ((ActionBarActivity)getActivity())
                    .startSupportActionMode(new ContentShareMultiModeListener());
        }
        else {
            disableIfNoneChecked();
        }

        return true;
    }

    /**
     * Disables the ActionMode if no more items are checked
     */
    private void disableIfNoneChecked(){
        if(mListView.getCheckedItemIds().length == 0){
            mMode.finish();
        }
    }

    /**
     * Source: http://www.miximum.fr/porting-the-contextual-anction-mode-for-pre-honeycomb-android-apps.html
     * Helper class which shows the CAB and
     */
    private class ContentShareMultiModeListener implements ActionMode.Callback {

        /**
         * Called when the ActionMode is created. Inflates the ActionMode Menu.
         * @param actionMode The mode currently active
         * @param menu The menu to which the items should be inflated
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
         * @param actionMode The mode currently active
         * @param menuItem The ActionItem clicked
         * @return
         */
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

        /**
         * Called when the ActionMode is finalized. Unchecks all items in view.
         * @param actionMode
         */
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
