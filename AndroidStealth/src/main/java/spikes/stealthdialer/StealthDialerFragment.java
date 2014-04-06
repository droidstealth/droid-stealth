package spikes.stealthdialer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stealth.android.HomeActivity;
import com.stealth.android.R;

/**
 * Takes a phone number it was given and displays this whenever loaded.
 *
 * @author Joris Z. van den Oever
 * Created on 2/26/14.
 */
public class StealthDialerFragment extends Fragment {
    public static final String CALL_KEY = "CALL NUMBER KEY";
    public static final String TAG = "STEALTHDIALER";
    TextView callData;
    String phone_number_dialed;

    /**
     * For updating the last called phone number if the app was still running in the background.
     * @param phone_number_dialed The phone number that needs to be displayed by the fragment.
     *                            The empty string counts as no number.
     */
    public void setPhone_number_dialed(String phone_number_dialed) {
        this.phone_number_dialed = phone_number_dialed;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.call_display_layout, container, false);
        callData = (TextView)rootView.findViewById(R.id.callData);
        return rootView;
    }

    /*
     * When the fragment is displayed again the textbox with the phonenumber is changed to the most
     * recently used phone number that triggered the StealthDialReceiver.
     * In addition to what Fragment.onStart() does.
     */
    @Override
	public void onResume() {
        if(phone_number_dialed == null || phone_number_dialed.equals(""))
            callData.setText("No call was made");
        else
            callData.setText(phone_number_dialed + " was the call that sent you here.");
        
        super.onStart();
    }

}
