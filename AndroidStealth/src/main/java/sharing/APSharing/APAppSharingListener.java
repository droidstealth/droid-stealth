package sharing.APSharing;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;

/**
 * Created by Alex on 2/26/14.
 */
public class APAppSharingListener implements
        APAppSharingFragment.AppSharingListener,
        ServerStatusActivity.ServerShareDialogListener {
    private WifiAPManager mWifiAPManager;

    private Context mAppContext;
    private int mOriginalWifiState;

    public APAppSharingListener(Context context){
        mAppContext = context.getApplicationContext();
        mWifiAPManager = new WifiAPManager(mAppContext);

        mOriginalWifiState = mWifiAPManager.getWifiState();

        shareApp();
    }

    /**
     * Sets up and shows the necessary dialogs to guide the user through the sharing of
     * the app by creating a hotspot and using a uri to share the apk link.
     */
    public void shareApp(){
        //No need to do more, since the service is already running
        if(HttpServerService.isRunning())
            return;

        //Disable
        if(mWifiAPManager.getWifiApState() == WifiAPManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED){
            mWifiAPManager.setWifiApEnabled(null, false);
        }
    }

    @Override
    public void dialogCanceled() {
        //Do nothing for now
    }

    @Override
    public void dialogCreateNetworkClicked(String ssid, String password) {
        //Enable Wifi AP
        WifiConfiguration configuration = getConfigFromFields(ssid, password);
        mWifiAPManager.setWifiApEnabled(configuration, true);

        Intent serviceIntent = new Intent(mAppContext, HttpServerService.class);
        serviceIntent.putExtra(ServerStatusActivity.SSID_KEY, ssid);
        serviceIntent.putExtra(ServerStatusActivity.PASS_KEY, password);
        serviceIntent.putExtra(HttpServerService.NETWORK_STATUS, mOriginalWifiState);

        mAppContext.startService(serviceIntent);
    }

    private WifiConfiguration getConfigFromFields(String ssid, String password){
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = ssid;

        if(password != null && !password.isEmpty()){
            configuration.preSharedKey = password;
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        }
        else {
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        configuration.status = WifiConfiguration.Status.ENABLED;

        return configuration;
    }
}
