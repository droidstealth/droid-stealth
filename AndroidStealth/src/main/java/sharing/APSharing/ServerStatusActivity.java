package sharing.APSharing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.stealth.android.R;

/**
 * Dialog with general information about the server, and a share link
 * Created by Alex on 2/26/14.
 */
public class ServerStatusActivity extends ActionBarActivity implements View.OnClickListener {
    public static final String SSID_KEY = "SSID_VALUE";
    public static final String PASS_KEY = "PASS_VALUE";
    public static final String SHARE_LINK = "SHARE_LINK";

    private String mShareLink;

    public interface ServerShareDialogListener {
        public void dialogCanceled();
    }

    private ServerShareDialogListener mListener;

    private AlertDialog mDialog;

    public void setServerShareDialogListener(ServerShareDialogListener listener){
        mListener = listener;
    }

    //We check for api versions ourselves
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = getLayoutInflater().inflate(R.layout.share_status_dialog, null);

        String ssid = getIntent().getStringExtra(SSID_KEY);
        String pass = getIntent().getStringExtra(PASS_KEY);
        mShareLink = getIntent().getStringExtra(SHARE_LINK);

        TextView ssidField = (TextView)contentView.findViewById(R.id.SSID_field);
        TextView passField = (TextView)contentView.findViewById(R.id.password_field);
        TextView shareLink = (TextView)contentView.findViewById(R.id.text_link);

        ssidField.setText(ssid);
        passField.setText(pass);
        shareLink.setText(mShareLink);

        ImageView share = (ImageView)contentView.findViewById(R.id.share_link);
        share.setOnClickListener(this);

        Button close = (Button)contentView.findViewById(android.R.id.button1);
        Button stop = (Button)contentView.findViewById(android.R.id.button2);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Log.d("ServerStatusActivity", "Closing");
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent stopServiceIntent = new Intent(ServerStatusActivity.this, HttpServerService.class);
                stopService(stopServiceIntent);
                finish();
                Log.d("ServerStatusActivity", "Closing");
            }
        });

        setContentView(contentView);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
            if(nfc != null){
                nfc.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback()
                {
                    /*
                     * (non-Javadoc)
                     * @see android.nfc.NfcAdapter.CreateNdefMessageCallback#createNdefMessage(android.nfc.NfcEvent)
                     */
                    @Override
                    public NdefMessage createNdefMessage(NfcEvent event)
                    {
                        NdefRecord uriRecord = NdefRecord.createUri(mShareLink);
                        return new NdefMessage(new NdefRecord[] { uriRecord });
                    }

                }, this, this);
            }
        }
    }

    @Override
    public void onClick(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mShareLink);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}
