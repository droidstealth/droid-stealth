package com.stealth.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;
import content.ContentFragment;

import sharing.APSharing.APSharing;
import sharing.SharingUtils;

public class HomeActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private APSharing mSharing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mSharing = new APSharing(this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
	    try {
		    String phoneNumber = getIntent().getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		    if (phoneNumber.startsWith("#555")) {
			    fragmentManager.beginTransaction()
			            .replace(R.id.container, new ContentFragment(false))
			            .commit();
		    }
		    else if (phoneNumber.startsWith("#666")) {
			    // TODO wipe data here if activity mode is 'panic'
			    fragmentManager.beginTransaction()
			                   .replace(R.id.container, new ContentFragment(true))
			                   .commit();
		    }
	    }
	    catch (NullPointerException e) {
		    e.printStackTrace();
		    Toast.makeText(getApplicationContext(), "App started without dialing phone number", Toast.LENGTH_SHORT).show();
	    }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);

            checkHotspotAvailability(menu);

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * Check if the device has AP Wifi support. If not, disable the 'Share Application' menu entry.
     * @param menu that contains the 'Share Application' menu entry.
     */
    private void checkHotspotAvailability(Menu menu) {
        MenuItem appSharingItem = menu.findItem(R.id.app_sharing);

        if(!SharingUtils.hasAPWifiSupport(this)){
            appSharingItem.setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.app_sharing:
                mSharing.shareApk();
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, StealthSettingActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
