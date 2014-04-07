package pin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

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
			mPinFrag = new PinFragment();
			Bundle b = new Bundle();
			b.putInt(PinFragment.ARG_DESCRIPTION_RESOURCE, R.string.pin_description_unlock);
			b.putString(PinFragment.ARG_PIN, "");
			mPinFrag.setArguments(b);

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, mPinFrag).commit();
		}
	}

	@Override
	public void onPinEntry(String pin) {
		mPinFrag.pinClear();
		if (HomeActivity.launch(getApplicationContext(), pin)) {
			finish();
		} else {
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			mPinFrag.getView().startAnimation(shake);
		}
	}

	@Override
	public void onPinCancel() {
		finish();
	}
}
