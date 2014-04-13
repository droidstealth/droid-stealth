package com.stealth.morphing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.dialog.DialogButton;
import com.stealth.dialog.DialogConstructor;
import com.stealth.dialog.DialogOptions;
import com.stealth.dialog.IDialogResponse;
import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedFolder;
import com.stealth.files.IndexedItem;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link MorphingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MorphingFragment extends Fragment implements View.OnClickListener {

	private static final int REQUEST_CHOOSER = 65456;

	private List<ApplicationInfo> mPackages;
	private ImageView mIcon;
	private EditText mName;
	private LinearLayout mPickApp;
	private LinearLayout mPickIcon;
	private LinearLayout mShare;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MorphingFragment.
     */
    public static MorphingFragment newInstance() {
        MorphingFragment fragment = new MorphingFragment();
        return fragment;
    }

    public MorphingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
	        // we have no arguments
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
	    View root = inflater.inflate(R.layout.fragment_morphing, container, false);
	    if (root == null) return null;

	    mPickApp = (LinearLayout) root.findViewById(R.id.morph_pick_app);
	    mPickApp.setOnClickListener(this);

	    mPickIcon = (LinearLayout) root.findViewById(R.id.morph_pick_icon);
	    mPickIcon.setOnClickListener(this);

	    mShare = (LinearLayout) root.findViewById(R.id.morph_share);
	    mShare.setOnClickListener(this);

	    mName = (EditText)root.findViewById(R.id.morph_edit_name);
	    mIcon = (ImageView)root.findViewById(R.id.morph_edit_icon);

        return root;
    }

	/**
	 * Get the list of applications, sorted on their label.
	 * @param packageManager the package manager to get the info from
	 * @return the sorted application list
	 */
	public List<ApplicationInfo> getInstalledApplication(PackageManager packageManager) {
		List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);
		Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(packageManager));
		return apps;
	}

	/**
	 * Shows the application picker in order to obtain the icon and name of another application
	 */
	private void showApplicationPicker() {
		DialogOptions options = new DialogOptions()
				.setTitle(R.string.morph_apppicker_title)
				.setDescription(R.string.morph_apppicker_description)
				.setPositiveButtonEnabled(false)
				.setNegative(R.string.cancel);

		if (getActivity() == null) return;
		if (getActivity().getPackageManager() == null) return;

		final PackageManager packageManager = getActivity().getPackageManager();
		if (mPackages == null) { // only load it if we haven't yet
			mPackages = getInstalledApplication(packageManager);
		}

		for (ApplicationInfo ai : mPackages) {
			options.addInput(new DialogButton(
					packageManager.getApplicationIcon(ai),
					packageManager.getApplicationLabel(ai).toString()));
		}

		DialogConstructor.show(getActivity(), options, new IDialogResponse() {
			@Override
			public void onPositive(ArrayList<String> input) { }
			@Override
			public void onNegative() { }
			@Override
			public void onCancel() { }
			@Override
			public boolean onButton(int i) {
				ApplicationInfo ai = mPackages.get(i);
				mIcon.setImageDrawable(packageManager.getApplicationIcon(ai));
				mName.setText(packageManager.getApplicationLabel(ai).toString());
				Utils.fadein(mIcon, 75);
				Utils.fadein(mName, 100);
				return true;
			}
		});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.morph_pick_app:
				showApplicationPicker();
				break;
			case R.id.morph_pick_icon:
				Intent getContentIntent = FileUtils.createGetContentIntent();
				Intent intent = Intent.createChooser(getContentIntent, "Select a file");
				((HomeActivity) getActivity()).setRequestedActivity(true);
				startActivityForResult(intent, REQUEST_CHOOSER);
				break;
			case R.id.morph_share:
				// do morph and share
				break;
		}
	}
	/**
	 * Listens for the return of the get content intent. Adds the items if successful
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CHOOSER:
				if (resultCode == Activity.RESULT_OK) {

					Uri selectedImageUri = Uri.parse(data.getDataString());

					if (getActivity() == null) return;
					if (getActivity().getApplicationContext() == null) return;

					try {
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(
								getActivity().getApplicationContext().getContentResolver(),
								selectedImageUri);

						if (bitmap.getHeight() > 256 || bitmap.getWidth() > 256) {
							bitmap = Utils.cropSquare(bitmap);
							bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
						}

						mIcon.setImageBitmap(bitmap);
						Utils.fadein(mIcon, 75);

					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						Log.e(Utils.tag(this), "Well, crap...", e);
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e(Utils.tag(this), "Well, crap...", e);
						e.printStackTrace();
					}
				}
				break;
		}
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
