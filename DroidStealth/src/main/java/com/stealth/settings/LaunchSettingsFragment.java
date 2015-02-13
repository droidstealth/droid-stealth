package com.stealth.settings;

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
import android.widget.ImageView;

import com.stealth.android.R;
import com.stealth.android.StealthButton;
import com.stealth.dialog.DialogConstructor;
import com.stealth.dialog.DialogOptions;
import com.stealth.dialog.IDialogResponse;
import com.stealth.font.FontManager;
import com.stealth.launch.DialerManager;
import com.stealth.launch.LaunchManager;
import com.stealth.launch.WidgetManager;
import com.stealth.utils.Utils;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Use the {@link LaunchSettingsFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class LaunchSettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

	private CheckBox mDialer;
	private EditText mLaunchCode;
	private CheckBox mWidget;
	private ImageView mWidgetHelp;
	private CheckBox mWidgetVisible;
	private CheckBox mHideIcon;
	private ImageView mHideIconHelp;
	private boolean mIgnoreToasts;
	private ImageView mWidgetVisibleHelp;
	private ImageView mDialerHelp;

	public LaunchSettingsFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment LaunchSettingsFragment.
	 */
	public static LaunchSettingsFragment newInstance() {
		LaunchSettingsFragment fragment = new LaunchSettingsFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View root = inflater.inflate(R.layout.fragment_launch_settings, container, false);

		mLaunchCode = (EditText) root.findViewById(R.id.launch_dialer_code);
		mDialer = (CheckBox) root.findViewById(R.id.launch_dialer_use);
		mDialerHelp = (ImageView) root.findViewById(R.id.launch_dialer_use_help);
		mWidget = (CheckBox) root.findViewById(R.id.launch_widget_use);
		mWidgetHelp = (ImageView) root.findViewById(R.id.launch_widget_use_help);
		mWidgetVisible = (CheckBox) root.findViewById(R.id.launch_widget_visible);
		mWidgetVisibleHelp = (ImageView) root.findViewById(R.id.launch_widget_visible_help);
		mHideIcon = (CheckBox) root.findViewById(R.id.launch_icon_disable);
		mHideIconHelp = (ImageView) root.findViewById(R.id.launch_icon_disable_help);

		mHideIconHelp.setOnClickListener(this);
		mWidgetHelp.setOnClickListener(this);
		mWidgetVisibleHelp.setOnClickListener(this);
		mDialerHelp.setOnClickListener(this);

		mDialer.setOnCheckedChangeListener(this);
		mWidget.setOnCheckedChangeListener(this);
		mWidgetVisible.setOnCheckedChangeListener(this);
		mHideIcon.setOnCheckedChangeListener(this);

		mIgnoreToasts = true;

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

				if (!mIgnoreToasts) {
					Utils.toast(R.string.launch_toast_launch_save);
				}
			}
		});

		mIgnoreToasts = false;

		FontManager.handleFontTags(root);

		return root;
	}

	@Override
	public void onCheckedChanged(CompoundButton box, boolean b) {
		switch (box.getId()) {
			case R.id.launch_dialer_use:
				LaunchManager.setDialerEnabled(b);
				if (!mIgnoreToasts) {
					if (b) {
						Utils.toast(R.string.launch_toast_dialer_on);
					}
					else {
						Utils.toast(R.string.launch_toast_dialer_off);
					}
				}
				break;
			case R.id.launch_widget_use:
				LaunchManager.setWidgetEnabled(b);
				if (!mIgnoreToasts) {
					if (b) {
						Utils.toast(R.string.launch_toast_widget_on);
					}
					else {
						Utils.toast(R.string.launch_toast_widget_off);
					}
				}
				break;
			case R.id.launch_widget_visible:
				WidgetManager.setWidgetTemporarilyVisible(b);
				StealthButton.updateMe(getActivity().getApplicationContext());
				if (!mIgnoreToasts) {
					if (b) {
						Utils.toast(R.string.launch_toast_widget_visible_on);
					}
					else {
						Utils.toast(R.string.launch_toast_widget_visible_off);
					}
				}
				break;
			case R.id.launch_icon_disable:
				boolean result = LaunchManager.setIconDisabled(b);
				if (!mIgnoreToasts) {
					if (result != b) {
						mHideIcon.setChecked(result);
						Utils.toast(R.string.launch_toast_icon_fail);
					}
					else {
						if (b) {
							Utils.toast(R.string.launch_toast_icon_on);
						}
						else {
							Utils.toast(R.string.launch_toast_icon_off);
						}
					}
				}
				break;

		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.launch_widget_use_help:
				createHelpDialog(R.string.launch_widget_use, R.string.launch_widget_description);
				break;
			case R.id.launch_dialer_use_help:
				createHelpDialog(R.string.launch_dialer_use, R.string.launch_dialer_description);
				break;
			case R.id.launch_icon_disable_help:
				createHelpDialog(R.string.launch_icon_disable, R.string.launch_icon_description);
				break;
			case R.id.launch_widget_visible_help:
				createHelpDialog(R.string.launch_widget_visible, R.string.launch_widget_visible_description);
				break;
		}
	}

	private void createHelpDialog(int titleResourceID, int bodyResourceID) {
		DialogOptions dialogOptions = new DialogOptions()
				.setTitle(titleResourceID)
				.setNegativeButtonEnabled(false)
				.setPositiveButtonEnabled(false)
				.setDescription(bodyResourceID);

		DialogConstructor.show(getActivity(), dialogOptions, new IDialogResponse() {
			@Override
			public void onPositive() {
				// Do nothing
			}

			@Override
			public void onNegative() {
				// Do nothing
			}

			@Override
			public void onCancel() {
				// Do nothing
			}
		});
	}
}
