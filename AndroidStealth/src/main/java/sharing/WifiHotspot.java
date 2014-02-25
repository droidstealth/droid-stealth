package sharing;

/**
 * Source: http://stackoverflow.com/a/7049074
 */

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 2/24/14.
 */
public class WifiHotspot {

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

    private List<WifiAPStateListener> mListeners;
    private WifiManager mWifiManager;
    private WifiConfiguration mConfiguration;

    private WifiAPState mLastKnownState;

    public WifiHotspot(Context context) {
        mListeners = new ArrayList<WifiAPStateListener>();

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mLastKnownState = getWifiAPState();
    }

    public void setWifiAPConfiguration(WifiConfiguration configuration){
        mConfiguration = configuration;
    }

    public void addWifiAPStateListener(WifiAPStateListener listener){
        if(!mListeners.contains(listener))
            mListeners.add(listener);
    }

    public boolean removeWifiAPStateListener(WifiAPStateListener listener){
        return mListeners.remove(listener);
    }

    private WifiConfiguration getDefaultWifiAPConfiguration() {
        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = "AndroidStealth";
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        return netConfig;
    }

    public void createHotspot(){
        WifiAPTask task = new WifiAPTask();
        task.execute(true);
    }

    public void disableHotspot(){
        WifiAPTask task = new WifiAPTask();
        task.execute(false);
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

    class WifiAPTask extends AsyncTask<Boolean, Void, WifiAPState> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(mConfiguration == null)
                setWifiAPConfiguration(getDefaultWifiAPConfiguration());
        }

        @Override
        protected WifiAPState doInBackground(Boolean... params) {
            return setWifiApEnabled(params[0]);
        }

        @Override
        protected void onPostExecute(WifiAPState wifiAPState) {
            super.onPostExecute(wifiAPState);
            try {
            } catch (IllegalArgumentException e) {}
            for(WifiAPStateListener listener : mListeners)
                listener.wifiAPStateSet(wifiAPState);
        }
    }
}
