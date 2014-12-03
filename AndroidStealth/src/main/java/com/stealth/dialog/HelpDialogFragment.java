package com.stealth.dialog;

import java.lang.String;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import com.stealth.android.R;

public class HelpDialogFragment extends DialogFragment {

	public static final String TITLE = "dialog.title";
	public static final java.lang.String MESSAGE = "dialog.message";

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int title = getArguments().getInt(TITLE);
		int message = getArguments().getInt(MESSAGE);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(title);
		builder.setMessage(message);

		return builder.create();
	}
}
