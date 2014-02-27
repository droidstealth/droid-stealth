package spikes.stealthdialer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stealth.android.R;

/**
 * Provides an example implementation of the intercepting calls and keeping the call history clean.
 * Created by Zim on 2/26/14.
 */
public class StealthDialerFragment extends Fragment {
    TextView callData;
    String call;

    public StealthDialerFragment(String number){
        super();
        call = number;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.call_display_layout, container, false);
        callData = (TextView)rootView.findViewById(R.id.callData);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w("StealthFragment", "stealth on start is here.");
        Log.w("StealthFragment", "Call is: " + call);
        if(call == null || call.equals(""))
            callData.setText("No call was made");
        else
            callData.setText(call + " was the call that sent you here.");
    }
}
