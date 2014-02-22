package spikes.filepicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.R;

import org.w3c.dom.Text;

import java.io.File;

/**
 * Created by Alex on 2/21/14.
 */
public class FilePickerFragment extends Fragment implements View.OnClickListener {
    private static final int REQUEST_CHOOSER = 1234;



    TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.file_picker_layout, container, false);
        textView = (TextView)rootView.findViewById(R.id.file_name);
        rootView.findViewById(R.id.select_file).setOnClickListener(this);
        return rootView;
    }

    /**
     * gets the response from the file picker intent
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CHOOSER:

                if (resultCode == Activity.RESULT_OK) {

                    final Uri uri = data.getData();

                    // Get the File path from the Uri
                    String path = FileUtils.getPath(getActivity(), uri);

                    // Alternatively, use FileUtils.getFile(Context, Uri)
                    if (path != null && FileUtils.isLocal(path)) {
                        File selected = new File(path);
                        File encrypted = new File(selected.getParentFile(), "encryptedtest");

                        Intent encryptIntent = new Intent(getActivity(), EncryptionService.class);
                        encryptIntent.putExtra(EncryptionService.UNENCRYPTED_PATH_KEY, selected.getPath());
                        encryptIntent.putExtra(EncryptionService.ENCRYPTED_PATH_KEY, encrypted.getPath());
                        //TODO this is just for testing. Needs better safeguard against filechanges and stuff
                        encryptIntent.putExtra(EncryptionService.ENTITY_KEY, encrypted.getName());
                        //just encrypting for now
                        encryptIntent.putExtra(EncryptionService.ENCRYPT_KEY, true);

                        getActivity().startService(encryptIntent);
                        Log.d("com.stealth.android", "Started service!");
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        Intent getContentIntent = FileUtils.createGetContentIntent();

        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
        startActivityForResult(intent, REQUEST_CHOOSER);
    }
}
