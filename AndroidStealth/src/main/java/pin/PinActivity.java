package pin;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.utils.Utils;

public class PinActivity extends FragmentActivity implements PinFragment.OnPinResult {

	private PinFragment mPinFrag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pin);
		Utils.setContext(getApplicationContext());

		if (!PinManager.get().hasPin()) {
			// no pin yet set. Just launch
			HomeActivity.launch(getApplicationContext(), "");
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
	public boolean onPinEntry(String pin) {
		mPinFrag.clearPin();
		if (HomeActivity.launch(getApplicationContext(), pin)) {
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
