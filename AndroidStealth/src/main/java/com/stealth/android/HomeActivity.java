package com.stealth.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import com.stealth.drawer.NavigationDrawerFragment;
import com.stealth.font.FontManager;
import com.stealth.morphing.MorphingFragment;
import com.stealth.settings.GeneralSettingsFragment;
import com.stealth.settings.LaunchSettingsFragment;
import com.stealth.launch.VisibilityManager;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import content.ContentFragment;
import pin.PinFragment;
import pin.PinManager;

public class HomeActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment mNavDrawer;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	private int mActiveNavigationOption = 0;
	private boolean mInterfaceConstructed = false;

	private boolean mRequestedActivity;

	/**
	 * Launch the HomeActivity by providing a pin
	 *
	 * @param context the context to use for the launch
	 * @param pin     the actual pin code that is used to launch us
	 * @return whether activity could launch
	 */
	public static boolean launch(Context context, String pin) {
		if (!PinManager.get().isPin(pin) && PinManager.get().hasPin()) {
			return false;
		}

		VisibilityManager.showApplication(context);

		Intent stealthCall = new Intent(context, HomeActivity.class);
		stealthCall.addCategory(Intent.CATEGORY_LAUNCHER);
		stealthCall.putExtra(PinManager.EXTRA_PIN, pin.trim());
		stealthCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(stealthCall);

		return true;
	}

	public void setRequestedActivity(boolean mRequestedActivity) {
		this.mRequestedActivity = mRequestedActivity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_loading);
		mInterfaceConstructed = false;

		FontManager.handleFontTags(findViewById(R.id.progress_layout));

		String pin = getIntent().getStringExtra(PinManager.EXTRA_PIN);
		BootManager.boot(this, pin, new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean succeeded) {

				if (succeeded) {
					Utils.toast(R.string.pin_description_unlocked); // welcome, Mr. Bond
					constructInterface(); // yay, we booted
				}
				else {
					finish(); // something went wrong. Incorrect pin maybe.
				}
			}
		});
	}

	/**
	 * Uses the value set through setRequestedActivity to determine if the app should close when it goes off screen. If
	 * a child fragment of the activity wants to open another app and keep running, like with startActivityForResult,
	 * they need to setRequestedActivity(true) on this activity beforehand. If no activity has been requested by the
	 * app, mRequestedActivity==False, finish up the app. If an activity has been requested don't finish up the app and
	 * reset the request flag.
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
	protected void onDestroy() {
		super.onDestroy();
		VisibilityManager.hideApplication(HomeActivity.this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Constructs the interface to show all main content to the user
	 */
	private void constructInterface() {
		setContentView(R.layout.activity_home);
		mNavDrawer = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavDrawer.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		mTitle = getTitle();
		mInterfaceConstructed = true;

		// start with pin activity if you have none
		if (!PinManager.get().hasPin()) {
			Utils.toast(R.string.pin_not_set_toast);
			mActiveNavigationOption = NavigationDrawerFragment.POSITION_PIN;
		}

		showCurrentFragment();
	}

	/**
	 * Open the requested fragment
	 */
	private void showCurrentFragment() {
		if (mInterfaceConstructed) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment toOpen = null;
			Utils.d("mActiveNavigationOption = " + mActiveNavigationOption);

			switch (mActiveNavigationOption) {
				case NavigationDrawerFragment.POSITION_HOME:
					toOpen = new ContentFragment();
					break;
				case NavigationDrawerFragment.POSITION_MORPH:
					toOpen = MorphingFragment.newInstance();
					break;
				case NavigationDrawerFragment.POSITION_LAUNCH:
					toOpen = LaunchSettingsFragment.newInstance();
					break;
				case NavigationDrawerFragment.POSITION_GENERAL:
					toOpen = GeneralSettingsFragment.newInstance("", "");
					break;
				case NavigationDrawerFragment.POSITION_PIN:
					toOpen = PinFragment.newInstance();
					break;
			}

			if (toOpen != null) {
				fragmentManager.beginTransaction()
						.replace(R.id.container, toOpen)
						.commit();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (mNavDrawer.isDrawerOpen()) {
			mNavDrawer.closeDrawer();
		} else {
			if (mActiveNavigationOption != 0) {
				mActiveNavigationOption = 0;
				showCurrentFragment();
			} else {
				super.onBackPressed();
			}
		}
	}

	/**
	 * This method is meant to fill the content fragment based on the navigation drawer's selected page
	 *
	 * @param position the item that is now active
	 */
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Utils.setContext(this); // onCreate is called later... so let's call this now :)
		if (position != mActiveNavigationOption) {
			mActiveNavigationOption = position;
			showCurrentFragment();
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
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
