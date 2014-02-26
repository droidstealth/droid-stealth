package sharing.APSharing;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
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

    public interface ServerShareDialogListener {
        public void dialogCanceled();
    }

    private ServerShareDialogListener mListener;

    private AlertDialog mDialog;

    public void setServerShareDialogListener(ServerShareDialogListener listener){
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = getLayoutInflater().inflate(R.layout.share_app_dialog, null);

        Bundle args = getIntent().getBundleExtra(HttpServerService.ARGUMENT_KEY);
        String ssid = args.getString(SSID_KEY);
        String pass = args.getString(PASS_KEY);

        TextView ssidField = (TextView)contentView.findViewById(R.id.SSID_field);
        TextView passField = (TextView)contentView.findViewById(R.id.password_field);

        ssidField.setText(ssid);
        passField.setText(pass);

        ImageView share = (ImageView)contentView.findViewById(R.id.share_link);
        share.setOnClickListener(this);

        //TODO button to kill service

        setContentView(R.layout.share_app_dialog);
    }

    @Override
    public void onClick(View view) {
        //TODO share intent here
    }
}
