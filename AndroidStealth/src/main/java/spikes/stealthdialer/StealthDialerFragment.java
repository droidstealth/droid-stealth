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
    public static final String CALL_KEY = "CALL NUMBER KEY";
    TextView callData;
    String call;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.call_display_layout, container, false);

        callData = (TextView)rootView.findViewById(R.id.callData);
        call = getArguments().getString(CALL_KEY);
        if(call == null || call.equals(""))
            callData.setText("No call was made");
        else
            callData.setText(call + " was the call that sent you here.");

        return rootView;
    }
}
