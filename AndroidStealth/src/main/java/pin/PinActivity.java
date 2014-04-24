package pin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.launch.VisibilityManager;
import com.stealth.utils.Utils;

public class PinActivity extends FragmentActivity implements PinFragment.OnPinResult {

	private PinFragment mPinFrag;
	private boolean mLaunchingMainApplication = false;

	/**
	 * Launches the pin dialog
	 *
	 * @param context the context to use for the launch
	 */
	public static void launch(Context context) {
		// application may be hidden, so show for now
		VisibilityManager.showApplication(context);

		Intent pinIntent = new Intent(context, PinActivity.class);
		pinIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(pinIntent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pin);
		Utils.setContext(getApplicationContext());

		if (!PinManager.get().hasPin()) {
			// no pin yet set. Just launch
			HomeActivity.launch(getApplicationContext(), "");
			mLaunchingMainApplication = true;
			finish();
			return;
		}

		// Check that the activity is using the layout version with
		// the fragment_container FrameLayout
		if (findViewById(R.id.container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create a new Fragment to be placed in the activity layout
			mPinFrag = PinFragment.newInstance(R.string.pin_title, R.string.pin_description_unlock, "");

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, mPinFrag).commit();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!mLaunchingMainApplication) {
			VisibilityManager.hideApplication(this);
		}
	}

	@Override
	public boolean onPinEntry(String pin) {
		mPinFrag.clearPin();
		if (HomeActivity.launch(getApplicationContext(), pin)) {
			mLaunchingMainApplication = true;
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onPinCancel() {
		finish();
	}
}
