package com.stealth.android;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.stealth.utils.Utils;
import content.ContentFragment;
import sharing.APSharing.APSharing;
import sharing.SharingUtils;

public class HomeActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	private APSharing mSharing;

	public void setRequestedActivity(boolean mRequestedActivity) {
		this.mRequestedActivity = mRequestedActivity;
	}

	private boolean mRequestedActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Utils.setContext(this);

		mSharing = new APSharing(this);

		mNavigationDrawerFragment = (NavigationDrawerFragment)
				getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

				PackageManager pm = getPackageManager();
				ComponentName homeName = new ComponentName(this, HomeActivity.class);
				if (pm.getComponentEnabledSetting(homeName) == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
					Log.w("Hiding: Disable", "Disabling app drawer icon.");
					pm.setComponentEnabledSetting(homeName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
							PackageManager.DONT_KILL_APP);
				}
	}

	/**
	 * Uses the value set through setRequestedActivity to determine if the app should close when it goes off screen.
	 * If a child fragment of the activity wants to open another app and keep running, like with startActivityForResult,
	 * they need to setRequestedActivity(true) on this activity beforehand.
	 *
	 * If no activity has been requested by the app, mRequestedActivity==False, finish up the app.
	 * If an activity has been requested don't finish up the app and reset the request flag.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		if (!mRequestedActivity) {
			this.finish();
		}
		else {
			mRequestedActivity = false;
		}
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		/*try {
		    String phoneNumber = getIntent().getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		    if (phoneNumber.startsWith("#555")) {
                // TODO some actions
		    }
		    else if (phoneNumber.startsWith("#666")) {
			    // TODO wipe data here if activity mode is 'panic'
                // TODO some other actions
		    }
	    }
	    catch (NullPointerException e) {
		    e.printStackTrace();
		    Toast.makeText(getApplicationContext(), "App started without dialing phone number",
		    Toast.LENGTH_SHORT).show();
	    }*/
		fragmentManager.beginTransaction()
				.replace(R.id.container, new ContentFragment())
				.commit();
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
	 *
	 * @param menu that contains the 'Share Application' menu entry.
	 */
	private void checkHotspotAvailability(Menu menu) {
		MenuItem appSharingItem = menu.findItem(R.id.app_sharing);

		if (!SharingUtils.hasAPWifiSupport(this)) {
			appSharingItem.setEnabled(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
