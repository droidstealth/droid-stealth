package com.stealth.morphing;

import java.io.File;
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
import android.widget.TextView;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.dialog.DialogConstructor;
import com.stealth.dialog.DialogOptions;
import com.stealth.dialog.IDialogAdapter;
import com.stealth.font.FontManager;
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

		FontManager.handleFontTags((ViewGroup) root);

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
				getContentIntent.setType("image/*");
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

					Uri selectedImageUri = data.getData();

					if (getActivity() == null) {
						return;
					}
					if (getActivity().getApplicationContext() == null) {
						return;
					}

					Bitmap bitmap = loadCroppedBitmapFromUri(selectedImageUri, 256);

					if(bitmap == null) {
						//TODO notify user of failure!
						return;
					}

					bitmap = Utils.correctOrientation(bitmap, selectedImageUri);

					mIcon.setImageBitmap(bitmap);
					Utils.fadein(mIcon, 75);
				}
				break;
		}
	}

	/**
	 * Loads a Bitmap from the given URI and scales and crops it to the given size
	 * @param uri The URI to load the Bitmap from
	 * @param size The size the final Bitmap should be
	 * @return
	 */
	private Bitmap loadCroppedBitmapFromUri(Uri uri, int size){
		File bitmapFile = FileUtils.getFile(getActivity(), uri);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(bitmapFile.getPath(), options);

		options.inSampleSize = calculateSampleSize(options, size, size);
		options.inJustDecodeBounds = false;

		Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);

		if(bitmap == null){
			Utils.d("Bitmap loading failed!");
			return null;
		}

		if (bitmap.getHeight() > size || bitmap.getWidth() > size) {
			bitmap = Utils.crop(bitmap, size, size);
		}

		return bitmap;
	}

	/**
	 * Helper function to determine the minimum required sample size for a bitmap based on the passed options
	 * @param options The options from which width and height of the original image are extracted
	 * @param reqHeight height in pixel the resulting image should be
	 * @param reqWidth width in pixels the resulting image should be
	 * @return the sample size required to get an image at least the size of reqHeight and reqWidth
	 */
	private static int calculateSampleSize(BitmapFactory.Options options, int reqHeight, int reqWidth){
		float cropRatio = (float) reqHeight / (float) reqWidth;
		float baseRatio = (float) options.outHeight / (float) options.outWidth;
		float scale, x = 0f, y = 0f;

		if (baseRatio > cropRatio) {
			scale = (float) reqWidth / (float) options.outWidth;
		}
		else {
			scale = (float) reqHeight / (float) options.outHeight;
		}

		return (int) Math.ceil(scale);
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
