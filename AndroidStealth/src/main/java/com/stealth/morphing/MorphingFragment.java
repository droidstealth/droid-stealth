package com.stealth.morphing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.stealth.android.R;
import com.stealth.dialog.DialogButton;
import com.stealth.dialog.DialogConstructor;
import com.stealth.dialog.DialogOptions;
import com.stealth.dialog.IDialogResponse;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link MorphingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MorphingFragment extends Fragment implements View.OnClickListener {

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
			mPackages = getActivity().getPackageManager().getInstalledApplications(0);
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
				break;
			case R.id.morph_share:
				// do morph and share
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
