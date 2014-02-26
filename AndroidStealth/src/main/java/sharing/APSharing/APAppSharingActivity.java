package sharing.APSharing;

import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Alex on 2/26/14.
 */
public class APAppSharingActivity extends FragmentActivity implements
        APAppSharingFragment.AppSharingListener,
        ServerStatusActivity.ServerShareDialogListener {
    private WifiAPManager mWifiAPManager;

    private WifiAPManager.WIFI_STATE mOriginalWifiState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiAPManager = new WifiAPManager(this);

        mOriginalWifiState = mWifiAPManager.getWifiState();

        shareApp();
    }

    @Override
    public void onBackPressed() {
        //Do nothing. User must cancel manually by pressing cancel!
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

        APAppSharingFragment fragment = new APAppSharingFragment();
        fragment.setAppSharingListener(this);
        fragment.show(getSupportFragmentManager(), null);
    }

    @Override
    public void dialogCanceled() {
        finish();
    }

    @Override
    public void dialogCreateNetworkClicked(String ssid, String password) {
        //Enable Wifi AP
        WifiConfiguration configuration = getConfigFromFields(ssid, password);
        mWifiAPManager.setWifiApEnabled(configuration, true);

        Bundle args = new Bundle();
        args.putString(ServerStatusActivity.SSID_KEY, ssid);
        args.putString(ServerStatusActivity.PASS_KEY, password);

        Intent serviceIntent = new Intent(this, HttpServerService.class);
        serviceIntent.putExtra(HttpServerService.ARGUMENT_KEY, args);

        startService(serviceIntent);
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
