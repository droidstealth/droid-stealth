package com.stealth.android;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;

import content.ContentFragment;

/**
 * Created by Joris Z. van den Oever on 3/13/14.
 */
public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    public HomeActivityTest() {
        super(HomeActivity.class);
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test to make sure the home is empty unless called with the right intent.
     */
    public void testPreconditions() {
        HomeActivity home = getActivity();
        Fragment container = home.getSupportFragmentManager().findFragmentById(R.id.container);
        assertEquals(null, container);
    }

    public void testWrongPhoneNumberIntent(){
        //TODO: Update this to go from the config? Or test setup? Example right now anyway.
        String mBadNumber = "#12345";
        String mEntryNumber = "#555";
        String mDeleteNumber = "#666";

        Intent newCall = new Intent();
        newCall.addCategory(Intent.CATEGORY_LAUNCHER);
        newCall.putExtra(Intent.EXTRA_PHONE_NUMBER, mBadNumber);
        setActivityIntent(newCall);
        HomeActivity home = getActivity();

        Fragment container = home.getSupportFragmentManager().findFragmentById(R.id.container);
        assertEquals(null, container);
        home.finish();      //Tell the activity to wrap up.
        setActivity(null);  //Set the testCase activity to null so it gets restarted instead of opening the old one.
                            // Needed to make the next intent get processed correctly.

        newCall.removeExtra(Intent.EXTRA_PHONE_NUMBER);
        newCall.putExtra(Intent.EXTRA_PHONE_NUMBER, mEntryNumber);
        setActivityIntent(newCall);
        home = getActivity();

        //TODO: Later test for stuff getting shown or deleted but not implemented yet.
        container = home.getSupportFragmentManager().findFragmentById(R.id.container);
        assertEquals(ContentFragment.class, container.getClass());
        home.finish();
        setActivity(null);

        newCall.removeExtra(Intent.EXTRA_PHONE_NUMBER);
        newCall.putExtra(Intent.EXTRA_PHONE_NUMBER, mDeleteNumber);
        setActivityIntent(newCall);
        home = getActivity();

        container = home.getSupportFragmentManager().findFragmentById(R.id.container);
        assertEquals(ContentFragment.class, container.getClass());
    }
}