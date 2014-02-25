package sharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.stealth.android.R;

/**
 * Created by Alex on 2/25/14.
 */
public class APAppSharingFragment extends DialogFragment {

    public interface AppSharingListener{
        public void dialogCanceled();
        public void networkCreated();
        public void networkCreationFailed();
    }

    private WifiAPManager mWifiAPManager;

    private AppSharingListener mListener;

    public void setAppSharingListener(AppSharingListener listener){
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mWifiAPManager = new WifiAPManager(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.ap_sharing_dialog, null));

        builder.setPositiveButton(R.string.start_ap_app_sharing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mWifiAPManager.setWifiApEnabled(getConfigFromFields(), true);

                dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel_ap_app_sharing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        return builder.create();
    }

    //TODO fields!
    private WifiConfiguration getConfigFromFields(){
        return null;
    }
}

