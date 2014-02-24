package sharing;

/**
 * Source: http://stackoverflow.com/a/7049074
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.widget.TextView;

import com.stealth.android.R;

import java.lang.reflect.Method;

/**
 * Created by Alex on 2/24/14.
 */
class SetWifiAPTask extends AsyncTask<Void, Void, SetWifiAPTask.WifiAPState> {

    public enum WifiAPState{
        WIFI_AP_STATE_UNKNOWN,
        WIFI_AP_STATE_DISABLING,
        WIFI_AP_STATE_DISABLED,
        WIFI_AP_STATE_ENABLING,
        WIFI_AP_STATE_ENABLED,
        WIFI_AP_STATE_FAILED
    }

    public interface WifiAPStateListener {
        public void wifiAPStateSet(WifiAPState state);
    }

    private boolean mMode;
    private ProgressDialog mDialog;
    private TextView mProgressDialogTitle;

    private WifiAPStateListener mListener;
    private WifiManager mWifiManager;
    private WifiConfiguration mConfiguration;

    public SetWifiAPTask(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        buildProgressDialog(context);
    }

    private void buildProgressDialog(Context context){
        mDialog = new ProgressDialog(context);
        mDialog.setIndeterminate(true);

        Resources resources = context.getResources();
        String title = resources.getString(R.string.wifi_AP_progress_title);
        String message = resources.getString(R.string.wifi_AP_progress_message);

        //Needed to set the wifi SSID in the title when the APConfiguration gets set.
        mProgressDialogTitle = ((TextView) mDialog.findViewById(resources.getIdentifier(
                "alertTitle", "id", "android")));

        mDialog.setTitle(title);
        mDialog.setMessage(message);
    }

    public void setWifiAPConfiguration(WifiConfiguration configuration){
        mConfiguration = configuration;

        //Sets the new title with the SSID included
        CharSequence original = mProgressDialogTitle.getText();
        String labeledTitle = original + " \"" + mConfiguration.SSID +"\"";
        mProgressDialogTitle.setText(labeledTitle);
    }

    public void setWifiAPStateListener(WifiAPStateListener listener){
        mListener = listener;
    }

    private WifiConfiguration getDefaultWifiAPConfiguration() {
        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = "AndroidStealth";
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        return netConfig;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if(mConfiguration == null)
            setWifiAPConfiguration(getDefaultWifiAPConfiguration());

        mDialog.show();
    }

    @Override
    protected WifiAPState doInBackground(Void... params) {
        setWifiApEnabled(mMode);
        return null;
    }

    @Override
    protected void onPostExecute(WifiAPState wifiAPState) {
        super.onPostExecute(wifiAPState);
        try {
            mDialog.dismiss();
        } catch (IllegalArgumentException e) {}
        if(mListener != null){
                mListener.wifiAPStateSet(wifiAPState);
        }
    }

    /**
     * Enable/disable wifi
     * @param enabled
     * @return WifiAP state
     */
    private WifiAPState setWifiApEnabled(boolean enabled) {
        //First, disable normal wifi. Sleep to give it time to shutdown (trivial approach)
        if (enabled && mWifiManager.getConnectionInfo() !=null) {
            mWifiManager.setWifiEnabled(false);
            try {Thread.sleep(1500);} catch (Exception e) {}
        }

        try {
            mWifiManager.setWifiEnabled(false);
            Method method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            method1.invoke(mWifiManager, mConfiguration, enabled); // true
        } catch (Exception e) {
        }

        if (!enabled) {
            int loopMax = 10;
            while (loopMax>0 && (getWifiAPState()==WifiAPState.WIFI_AP_STATE_DISABLING
                    || getWifiAPState()==WifiAPState.WIFI_AP_STATE_ENABLED
                    || getWifiAPState()==WifiAPState.WIFI_AP_STATE_FAILED)) {
                try {Thread.sleep(500);loopMax--;} catch (Exception e) {}
            }
            //Turn wifi back on for user
            mWifiManager.setWifiEnabled(true);
        } else if (enabled) {
            int loopMax = 10;
            while (loopMax>0 && (getWifiAPState()==WifiAPState.WIFI_AP_STATE_ENABLING
                    || getWifiAPState()==WifiAPState.WIFI_AP_STATE_DISABLED
                    || getWifiAPState()==WifiAPState.WIFI_AP_STATE_FAILED)) {
                try {Thread.sleep(500);loopMax--;} catch (Exception e) {}
            }
        }

        return getWifiAPState();
    }

    public WifiAPState getWifiAPState() {
        WifiAPState state = WifiAPState.WIFI_AP_STATE_UNKNOWN;
        try {
            Method method2 = mWifiManager.getClass().getMethod("getWifiApState");
            state = WifiAPState.values()[(Integer)method2.invoke(mWifiManager)+1];
        } catch (Exception e) {}
        return state;
    }

}
