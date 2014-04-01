package com.stealth.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.stealth.utils.Utils;
import content.ContentFragment;
import pin.PinManager;
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

    private APSharing mSharing;

    /**
     * Launch the HomeActivity by providing a pin
     * @param context the context to use for the launch
     * @param pin the actual pin code that is used to launch us
     * @return whether activity could launch
     */
    public static boolean launch(Context context, String pin)
    {
        if (!PinManager.get().isPin(pin)) return false;
        try
        {
            PackageManager pm = context.getPackageManager();
            ComponentName homeName = new ComponentName(context, HomeActivity.class);

            if (pm != null)
            {
                // make sure activity can be called
                pm.setComponentEnabledSetting(
                        homeName,
                        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                        PackageManager.DONT_KILL_APP);
            }

            Intent stealthCall = new Intent(context, HomeActivity.class);
            stealthCall.addCategory(Intent.CATEGORY_LAUNCHER);
            stealthCall.putExtra(Intent.EXTRA_PHONE_NUMBER, pin.trim());
            stealthCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(stealthCall);

            Utils.toast(R.string.pin_description_unlocked);

            return true;
        }
        catch (Exception e)
        {
            Log.e("STEALTH", "Could not launch stealth app", e);
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Utils.setContext(this);

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

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		/*try {

		    String phoneNumber = getIntent().getStringExtra(PinManager.EXTRA_PIN);
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
		    Toast.makeText(getApplicationContext(), "App started without dialing phone number", Toast.LENGTH_SHORT).show();
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
