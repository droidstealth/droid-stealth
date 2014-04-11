package com.stealth.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewManager;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.stealth.android.R;
import com.stealth.utils.Utils;

/**
 * This class can show different dialogs.
 * Created by OlivierHokke on 11-Apr-14.
 */
public class DialogManager {
	public static void showConfirm(Activity activity, int titleResource, int descriptionResource,
			int negativeResource, int positiveResource, final IConfirmResponse response) {

		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_confirm);

		final EditText editText = (EditText)dialog.findViewById(R.id.dialog_input);
		((ViewManager)editText.getParent()).removeView(editText);

		final TextView title = (TextView)dialog.findViewById(R.id.dialog_title);
		title.setText(titleResource);

		final TextView description = (TextView)dialog.findViewById(R.id.dialog_description);
		description.setText(descriptionResource);

		final Button negative = (Button)dialog.findViewById(R.id.dialog_negative);
		negative.setText(negativeResource);

		final Button positive = (Button)dialog.findViewById(R.id.dialog_positive);
		positive.setText(positiveResource);

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				response.onCancel();
			}
		});
		negative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				response.onNegative();
				dialog.dismiss();
			}
		});
		positive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				response.onPositive();
				dialog.dismiss();
			}
		});

		dialog.show();
	}
}
