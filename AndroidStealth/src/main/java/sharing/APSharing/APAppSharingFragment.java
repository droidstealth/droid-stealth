package sharing.APSharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.stealth.android.R;

import sharing.SharingUtils;

/**
 * Dialog to get the fields for a new AP connection
 * Created by Alex on 2/25/14.
 */
public class APAppSharingFragment extends DialogFragment {

    public interface AppSharingListener{
        public void dialogCanceled();
        public void dialogCreateNetworkClicked(String ssid, String password);
    }

    private AppSharingListener mListener;

    TextView mPasswordField;
    TextView mSSIDField;

    public APAppSharingFragment(){
    }

    public void setAppSharingListener(AppSharingListener listener){
        mListener = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setTitle(R.string.ap_dialog_title);

        mBuilder.setView(createContentView());

        mBuilder.setPositiveButton(R.string.start_ap_app_sharing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { if (mListener != null) {
                dismiss();

                if(mListener != null)
                    mListener.dialogCreateNetworkClicked(mSSIDField.getText().toString(), mPasswordField.getText().toString());
                }
            }
        });
        mBuilder.setNegativeButton(R.string.cancel_ap_app_sharing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();

                if(mListener != null)
                    mListener.dialogCanceled();
            }
        });

        final AlertDialog mDialog = mBuilder.create();

        APConfigurationValidator configurationValidator = new APConfigurationValidator(mDialog);
        mSSIDField.addTextChangedListener(configurationValidator);
        mPasswordField.addTextChangedListener(configurationValidator);

        //Disable the create button since short passes and SSIDs aren't allowed
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        return mDialog;
    }

    /**
     * Creates the content view and hooks up any events that are needed
     * @return
     */
    private View createContentView(){
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View contentView = inflater.inflate(R.layout.ap_setup_dialog, null);

        mSSIDField = (TextView)contentView.findViewById(R.id.SSID_field);
        mPasswordField = (TextView)contentView.findViewById(R.id.password_field);

        ((CheckBox)contentView.findViewById(R.id.show_password_check)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked)
                    mPasswordField.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                else
                    mPasswordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        return contentView;
    }

    private class APConfigurationValidator implements TextWatcher{

        private final AlertDialog mDialog;

        public APConfigurationValidator(AlertDialog dialog){
            mDialog = dialog;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String pass = mPasswordField.getText().toString();
            boolean validPass = pass.length() == 0? true: SharingUtils.validWPAPass(pass);
            boolean validSSID = SharingUtils.validSSID(mSSIDField.getText().toString());
            if(!validPass || !validSSID)
               mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            else
               mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }


}

