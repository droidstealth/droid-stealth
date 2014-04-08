package content;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;

import com.stealth.android.HomeActivity;
import com.stealth.android.R;

/**
 * Created by Joris Z. van den Oever on 3/20/14.
 *
 * Uses HomeActivity to access ContentFragment inside it.
 * This is the recommended method for testing.
 */
public class ContentFragmentTest extends ActivityInstrumentationTestCase2<HomeActivity> {
    private String mEntryPhoneNumber;
    private ContentFragment mContent;

    public ContentFragmentTest() {
        super(HomeActivity.class);
        mEntryPhoneNumber = "#555";
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        Intent newCall = new Intent();
        newCall.addCategory(Intent.CATEGORY_LAUNCHER);
        newCall.putExtra(Intent.EXTRA_PHONE_NUMBER, mEntryPhoneNumber);
        setActivityIntent(newCall);
        HomeActivity home = getActivity();
        mContent = (ContentFragment) home.getSupportFragmentManager().findFragmentById(R.id.container);
    }

    public void testPreconditions() {
        View contentContainer = mContent.getView();
        assertNotNull(contentContainer);

        AbsListView listView = (AbsListView) contentContainer.findViewById(R.id.content_container);
        assertNotNull(listView);
    }

    public void testOnOptionsItemSelected() {
        IntentFilter captureIntent = new IntentFilter(Intent.ACTION_GET_CONTENT);
        //have minimal code happening in the callback. Hence the failure result.
        Instrumentation.ActivityResult failureResult = new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null);
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(captureIntent, null, true);
        MenuItem content_add = (MenuItem) mContent.getView().findViewById(R.id.content_add);
        assertNotNull(content_add);

        mContent.onOptionsItemSelected(content_add);
        assertNotNull(am.waitForActivityWithTimeout(1000)); //Think it's milliseconds.
        //Since we mocked the result we can do the low timeout.
        assertEquals(1, am.getHits());
    }
}
