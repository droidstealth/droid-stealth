package com.stealth.morphing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.android.HomeActivity;
import com.stealth.android.R;
import com.stealth.dialog.DialogConstructor;
import com.stealth.dialog.DialogOptions;
import com.stealth.dialog.IDialogAdapter;
import com.stealth.font.FontManager;
import com.stealth.utils.Utils;

import sharing.SharingUtils;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Use the {@link MorphingFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class MorphingFragment extends Fragment implements View.OnClickListener, AppMorph.MorphProgressListener {

	private static final int REQUEST_CHOOSER = 65456;

	private List<ApplicationInfo> mPackages;
	private ImageView mIcon;
	private EditText mName;
	private AppMorph mAppMorph;
	private ProgressDialog mMorphProgressDialog;
    private NfcAdapter mNfcAdapter;

	private File mCurrentApp;
	private String mCurrentLabel;
	private Uri mCurrentIconPath;

	private View.OnClickListener mAppPicked = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			ApplicationInfo ai = (ApplicationInfo) view.getTag();

			int iconSize = Utils.px(48);

			Bitmap bitmap = convertToBitmap(mPackMan.getApplicationIcon(ai), iconSize, iconSize);

			File cache = Utils.getRandomCacheFile(".png");
			try {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cache));
			}
			catch (FileNotFoundException e) {
				Utils.toast(R.string.icon_retrieve_failed);
			}

			mCurrentIconPath = Uri.fromFile(cache);
			mCurrentLabel = mPackMan.getApplicationLabel(ai).toString();

			mIcon.setImageDrawable(mPackMan.getApplicationIcon(ai));
			mName.setText(mCurrentLabel);
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPackMan = getActivity().getPackageManager();

		mAppMorph = new AppMorph(getActivity());
		mAppMorph.setMorphProgressListener(this);
		mAppMorph.setIconResName("ic_drawer_home");

		mCurrentApp = new File(getActivity().getPackageResourcePath());

        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)
                && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
            mNfcAdapter.setBeamPushUrisCallback(new FileUriCallback(), getActivity());
        }


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

		View pickApp = root.findViewById(R.id.morph_pick_app);
		pickApp.setOnClickListener(this);

		View pickIcon = root.findViewById(R.id.morph_pick_icon);
		pickIcon.setOnClickListener(this);

		View share = root.findViewById(R.id.morph_share);
		share.setOnClickListener(this);

		View reset = root.findViewById(R.id.morph_reset);
		reset.setOnClickListener(this);

		View morph = root.findViewById(R.id.morph_execute);
		morph.setOnClickListener(this);

		mName = (EditText) root.findViewById(R.id.morph_edit_name);
		mIcon = (ImageView) root.findViewById(R.id.morph_edit_icon);

		FontManager.handleFontTags(root);

		return root;
	}

	private Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
		Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mutableBitmap);
		drawable.setBounds(0, 0, widthPixels, heightPixels);
		drawable.draw(canvas);

		return mutableBitmap;
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
	 * Selects an action based on the view that was clicked
	 *
	 * @param view
	 */
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.morph_pick_app:
				showApplicationPicker();
				break;
			case R.id.morph_pick_icon:
				pickIcon();
				break;
			case R.id.morph_share:
				shareApp();
				break;
			case R.id.morph_execute:
				morphApp();
				break;
		}
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

	/**
	 * Lets the user pick a new icon from their own folder
	 */
	private void pickIcon() {
		Intent getContentIntent = FileUtils.createGetContentIntent();
		getContentIntent.setType("image/*");
		Intent intent = Intent.createChooser(getContentIntent, Utils.str(R.string.morph_select_image));
		((HomeActivity) getActivity()).setRequestedActivity(true);
		startActivityForResult(intent, REQUEST_CHOOSER);
	}

	/**
	 * Share currently selected application through an intent
	 */
	private void shareApp() {
		if (mCurrentApp != null) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("application/zip");
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mCurrentApp));

			startActivity(intent);
		}
	}

	/**
	 * Sets the currently selected app to share to the
	 */
	private void morphApp() {

		mMorphProgressDialog = ProgressDialog.show(getActivity(), Utils.str(R.string.morphing_dialog_title),
				Utils.str(R.string.morphing_started), false, false);

		new MorphTask(mCurrentLabel, mCurrentIconPath).execute();
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

					mCurrentIconPath = data.getData();

					if (getActivity() == null) {
						return;
					}
					if (getActivity().getApplicationContext() == null) {
						return;
					}

					setImageOnView(mCurrentIconPath, mIcon);
				}
				break;
		}
	}

	private void setImageOnView(Uri imageURI, ImageView view) {
		Bitmap bitmap = loadCroppedBitmapFromUri(imageURI, 256);

		if (bitmap == null) {
			//TODO notify user of failure!
			return;
		}

		bitmap = Utils.correctOrientation(bitmap, mCurrentIconPath);

		view.setImageBitmap(bitmap);
		Utils.fadein(view, 75);
	}

	/**
	 * Loads a Bitmap from the given URI and scales and crops it to the given size
	 *
	 * @param uri  The URI to load the Bitmap from
	 * @param size The size the final Bitmap should be
	 * @return
	 */
	private Bitmap loadCroppedBitmapFromUri(Uri uri, int size) {
		File bitmapFile = FileUtils.getFile(getActivity(), uri);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(bitmapFile.getPath(), options);

		options.inSampleSize = calculateSampleSize(options, size, size);
		options.inJustDecodeBounds = false;

		Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);

		if (bitmap == null) {
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
	 *
	 * @param options   The options from which width and height of the original image are extracted
	 * @param reqHeight height in pixel the resulting image should be
	 * @param reqWidth  width in pixels the resulting image should be
	 * @return the sample size required to get an image at least the size of reqHeight and reqWidth
	 */
	private static int calculateSampleSize(BitmapFactory.Options options, int reqHeight, int reqWidth) {
		float cropRatio = (float) reqHeight / (float) reqWidth;
		float baseRatio = (float) options.outHeight / (float) options.outWidth;
		//Default to 1 to prevent sampling errors
		float scale = 1f;

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

	@Override
	public void onProgress(AppMorph.ProgressStep progress) {
		if (mMorphProgressDialog != null) {
			String message = null;

			switch (progress) {
				case Extracting:
					message = Utils.str(R.string.morphing_extracting);
					break;
				case SettingLabel:
					message = Utils.str(R.string.morphing_labeling);
					break;
				case SettingIcons:
					message = Utils.str(R.string.morphing_icons);
					break;
				case Repackaging:
					message = Utils.str(R.string.morphing_repackaging);
					break;
				case Signing:
					message = Utils.str(R.string.morphing_signing);
					break;
			}

			if (message != null) {
				final String finalMessage = message;
				Utils.runOnMain(new Runnable() {
					@Override
					public void run() {
						if (mMorphProgressDialog != null) {
							mMorphProgressDialog.setMessage(finalMessage);
						}
					}
				});

			}
		}
	}

	@Override
	public void onMorphFailed(AppMorph.ProgressStep atPoint, Exception failure) {
		mMorphProgressDialog.dismiss();
		mMorphProgressDialog = null;
		Utils.toast(R.string.morph_failed);
		Utils.d("Failed: " + atPoint.toString() + " because: " + failure.getLocalizedMessage());
        failure.printStackTrace();
	}

	@Override
	public void onFinished(final File newApk) {
		Utils.runOnMain(new Runnable() {
			@Override
			public void run() {
				if (mMorphProgressDialog != null) {
					mMorphProgressDialog.dismiss();
					mMorphProgressDialog = null;
				}

				if (newApk != null) {
					mCurrentApp = newApk;

					View currentView = getView();
					if (currentView != null) {
						TextView label = (TextView) currentView.findViewById(R.id.morph_current_name);
						label.setText(mCurrentLabel);
						Utils.fadein(label, 100);

						ImageView icon = (ImageView) currentView.findViewById(R.id.morph_current_icon);
						setImageOnView(mCurrentIconPath, icon);
					}

					mCurrentLabel = null;
					mCurrentIconPath = null;
				}
				else {
					//TODO indicate failure
				}
			}
		});
	}

	private class MorphTask extends AsyncTask<Void, Void, File> {

		private String mLabel;
		private Uri mIcon;

		public MorphTask(String label, Uri icon) {
			mLabel = label;
			mIcon = icon;
		}

		@Override
		protected File doInBackground(Void... voids) {
			mAppMorph.morphApp(mLabel, mIcon);
			return null;
		}
	}


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private class FileUriCallback implements NfcAdapter.CreateBeamUrisCallback {

        @Override
        public Uri[] createBeamUris(NfcEvent nfcEvent) {
            return new Uri[] { Uri.fromFile(mCurrentApp) };
        }

        private Uri getApkUri() {
            return Uri.fromFile(SharingUtils.getApk(getActivity()));
        }
    }
}
