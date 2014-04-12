package com.stealth.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.visibility.VisibilityManager;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import content.ContentFragment;
import pin.PinManager;
import sharing.SharingUtils;
import spikes.morphing.AppMorph;

public class HomeActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, AppMorph.MorphProgressListener {

	private static final int ICON_CHOOSER = 2625;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment mNavDrawer;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	private int mActiveNavigationOption = 0;
	private ProgressDialog mProgress = null;
	private AppMorph mAppMorph;

	private boolean mRequestedActivity;

	/**
	 * Launch the HomeActivity by providing a pin
	 *
	 * @param context the context to use for the launch
	 * @param pin     the actual pin code that is used to launch us
	 * @return whether activity could launch
	 */
	public static boolean launch(Context context, String pin) {
		if (!PinManager.get().isPin(pin)) {
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

		mAppMorph = new AppMorph(this);
		mAppMorph.setMorphProgressListener(this);

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

			if(requestCode == ICON_CHOOSER && resultCode == RESULT_OK){
				new MorphTask("test", data.getData()).execute();
			}
	}

	/**
	 * Constructs the interface to show all main content to the user
	 */
	private void constructInterface() {
		setContentView(R.layout.activity_home);
		mNavDrawer = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavDrawer.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		mTitle = getTitle();

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, new ContentFragment())
				.commit();
	}

	@Override
	public void onBackPressed() {
		if (mNavDrawer.isDrawerOpen()) {
			mNavDrawer.closeDrawer();
		} else {
			super.onBackPressed();
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
		mActiveNavigationOption = position;
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mNavDrawer != null && !mNavDrawer.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.home, menu);

			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				Intent settingsIntent = new Intent(this, StealthSettingActivity.class);
				startActivity(settingsIntent);
				return true;
			case R.id.share_app:
				Intent getContentIntent = FileUtils.createGetContentIntent();
				Intent intent = Intent.createChooser(getContentIntent, "Select a file");
				setRequestedActivity(true);
				startActivityForResult(intent, ICON_CHOOSER);
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onProgress(AppMorph.ProgressStep progress) {
		Log.d("HomeActivity", "Progress: " + progress.toString());
	}

	@Override
	public void onMorphFailed(AppMorph.ProgressStep atPoint, String text) {
		Log.d("HomeActivity", "Failure at " + atPoint.toString());
	}

	private class MorphTask extends AsyncTask<Void, Void, Void> {

		private String mLabel;
		private Uri mIcon;

		public MorphTask(String label, Uri icon){
			mLabel = label;
			mIcon = icon;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			mAppMorph.morphApp(mLabel, mIcon);
			return null;
		}
	}
}
