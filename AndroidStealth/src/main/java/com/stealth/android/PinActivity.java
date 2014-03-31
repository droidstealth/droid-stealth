package com.stealth.android;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.Toast;

public class PinActivity extends FragmentActivity implements PinFragment.OnPinResult {

    private PinFragment mPinFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

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
        Context context = getApplicationContext();
        mPinFrag.pinClear();
        if (pin.startsWith("#")) {
            // TODO make it work normally... instead of just opening the home activity
            Toast.makeText(context, "YAY!!!", Toast.LENGTH_SHORT).show();

            try {
                String uri = "tel:" + pin.trim() ;
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e("STEALTH", "SOM TIN WONG", e);
            }

        } else {
            Toast.makeText(context, "NAAY..", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPinCancel() {
        finish();
    }
}
