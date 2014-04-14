package com.stealth.morphing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.dialog.DialogConstructor;
import com.stealth.dialog.DialogOptions;
import com.stealth.dialog.IDialogAdapter;
import com.stealth.utils.Utils;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Use the {@link MorphingFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class MorphingFragment extends Fragment implements View.OnClickListener {

	private static final int REQUEST_CHOOSER = 65456;

	private List<ApplicationInfo> mPackages;
	private ImageView mIcon;
	private EditText mName;
	private View.OnClickListener mAppPicked = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			ApplicationInfo ai = (ApplicationInfo) view.getTag();
			mIcon.setImageDrawable(mPackMan.getApplicationIcon(ai));
			mName.setText(mPackMan.getApplicationLabel(ai).toString());
			Utils.fadein(mIcon, 75);
			Utils.fadein(mName, 100);
			mAppDialog.dismiss();
		}

	};
	private IDialogAdapter<ApplicationInfo> mAppAdapter = new IDialogAdapter<ApplicationInfo>() {

		@Override
		public List<ApplicationInfo> getList() {
			return mPackages;
		}

		@Override
		public int getItemLayout() {
			return R.layout.dialog_button;
		}

		@Override
		public void setView(int index, View v) {
			ApplicationInfo ai = getList().get(index);
			((ImageView) v.findViewById(R.id.dialog_button_icon)).setImageDrawable(mPackMan.getApplicationIcon(ai));
			((TextView) v.findViewById(R.id.dialog_button_title)).setText(mPackMan.getApplicationLabel(ai));

			v.setTag(ai); // remember the application info in the view
			v.setOnClickListener(mAppPicked);
		}

	};
	private PackageManager mPackMan;
	private Dialog mAppDialog;

	public MorphingFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment MorphingFragment.
	 */
	public static MorphingFragment newInstance() {
		MorphingFragment fragment = new MorphingFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPackMan = getActivity().getPackageManager();
		if (getArguments() != null) {
			// we have no arguments
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View root = inflater.inflate(R.layout.fragment_morphing, container, false);
		if (root == null) {
			return null;
		}

		LinearLayout pickApp = (LinearLayout) root.findViewById(R.id.morph_pick_app);
		pickApp.setOnClickListener(this);

		LinearLayout pickIcon = (LinearLayout) root.findViewById(R.id.morph_pick_icon);
		pickIcon.setOnClickListener(this);

		LinearLayout share = (LinearLayout) root.findViewById(R.id.morph_share);
		share.setOnClickListener(this);

		mName = (EditText) root.findViewById(R.id.morph_edit_name);
		mIcon = (ImageView) root.findViewById(R.id.morph_edit_icon);

		return root;
	}

	/**
	 * Get the list of applications, sorted on their label.
	 *
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
		// do some checks, as without these we can't continue
		if (getActivity() == null) {
			return;
		}
		if (getActivity().getPackageManager() == null) {
			return;
		}

		// get the installed applications
		if (mPackages == null) { // only load it if we haven't yet
			mPackages = getInstalledApplication(mPackMan);
		}

		// setup the texts and dialog adapter
		DialogOptions<ApplicationInfo> options = new DialogOptions<ApplicationInfo>()
				.setTitle(R.string.morph_apppicker_title)
				.setDescription(R.string.morph_apppicker_description)
				.setPositiveButtonEnabled(false)
				.setNegative(R.string.cancel)
				.setDialogAdapter(mAppAdapter);

		// Build the dialog
		mAppDialog = DialogConstructor.show(getActivity(), options, null);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.morph_pick_app:
				showApplicationPicker();
				break;
			case R.id.morph_pick_icon:
				Intent getContentIntent = FileUtils.createGetContentIntent();
				Intent intent = Intent.createChooser(getContentIntent, Utils.str(R.string.morph_select_image));
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

					if (getActivity() == null) {
						return;
					}
					if (getActivity().getApplicationContext() == null) {
						return;
					}

					try {
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(
								getActivity().getApplicationContext().getContentResolver(),
								selectedImageUri);

						if (bitmap.getHeight() > 256 || bitmap.getWidth() > 256) {
							bitmap = Utils.crop(bitmap, 256, 256);
						}

						bitmap = Utils.correctOrientation(bitmap, selectedImageUri);

						mIcon.setImageBitmap(bitmap);
						Utils.fadein(mIcon, 75);

					}
					catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						Log.e(Utils.tag(this), "Well, crap...", e);
						e.printStackTrace();
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e(Utils.tag(this), "Well, crap...", e);
						e.printStackTrace();
					}
					catch (OutOfMemoryError e) {
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
