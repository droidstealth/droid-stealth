package com.stealth.settings;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import com.stealth.android.R;
import com.stealth.android.StealthButton;
import com.stealth.launch.DialerManager;
import com.stealth.launch.LaunchManager;
import com.stealth.launch.WidgetManager;
import com.stealth.utils.Utils;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Use the {@link LaunchSettingsFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class LaunchSettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

	/**
	 * Use this factory method to create a new instance of this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment LaunchSettingsFragment.
	 */
	public static LaunchSettingsFragment newInstance() {
		LaunchSettingsFragment fragment = new LaunchSettingsFragment();
		return fragment;
	}

	private CheckBox mDialer;
	private EditText mLaunchCode;
	private CheckBox mWidget;
	private CheckBox mWidgetVisible;
	private CheckBox mHideIcon;

	public LaunchSettingsFragment() {
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
		View root = inflater.inflate(R.layout.fragment_launch_settings, container, false);

		mLaunchCode = (EditText) root.findViewById(R.id.launch_dialer_code);
		mDialer = (CheckBox) root.findViewById(R.id.launch_dialer_use);
		mWidget = (CheckBox) root.findViewById(R.id.launch_widget_use);
		mWidgetVisible = (CheckBox) root.findViewById(R.id.launch_widget_visible);
		mHideIcon = (CheckBox) root.findViewById(R.id.launch_icon_disable);

		mDialer.setOnCheckedChangeListener(this);
		mWidget.setOnCheckedChangeListener(this);
		mWidgetVisible.setOnCheckedChangeListener(this);
		mHideIcon.setOnCheckedChangeListener(this);

		mDialer.setChecked(LaunchManager.isDialerEnabled());
		mWidget.setChecked(LaunchManager.isWidgetEnabled());
		mWidgetVisible.setChecked(WidgetManager.isWidgetTemporarilyVisible());
		mHideIcon.setChecked(LaunchManager.isIconDisabled());

		mLaunchCode.setText(DialerManager.getLaunchCode());
		mLaunchCode.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			public void afterTextChanged(Editable editable) {
				if (mLaunchCode.getText() == null) {
					return;
				}

				String code = mLaunchCode.getText().toString();
				String newCode = DialerManager.setLaunchCode(code);

				// if the code changed because it was not entirely valid, set the new code
				if (!newCode.equals(code)) {
					mLaunchCode.setText(DialerManager.setLaunchCode(newCode));
				}
			}
		});

		return root;
	}

	@Override
	public void onCheckedChanged(CompoundButton box, boolean b) {
		switch (box.getId()) {
			case R.id.launch_dialer_use:
				LaunchManager.setDialerEnabled(b);
				break;
			case R.id.launch_widget_use:
				LaunchManager.setWidgetEnabled(b);
				break;
			case R.id.launch_widget_visible:
				WidgetManager.setWidgetTemporarilyVisible(b);
				StealthButton.updateMe(getActivity().getApplicationContext());
				break;
			case R.id.launch_icon_disable:
				boolean result = LaunchManager.setIconDisabled(b);
				if (result != b) {
					mHideIcon.setChecked(result);
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
