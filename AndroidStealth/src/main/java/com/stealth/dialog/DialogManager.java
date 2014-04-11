package com.stealth.dialog;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.stealth.android.R;
import com.stealth.utils.Utils;

/**
 * This class can show different dialogs.
 * Created by OlivierHokke on 11-Apr-14.
 */
public class DialogManager {

	public static void show(Activity activity, final DialogOptions options, final IDialogResponse response) {

		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_base);

		final ArrayList<EditText> editTexts = new ArrayList<EditText>();
		final ArrayList<DialogInput> inputList = options.getInputs();
		final LinearLayout scrollWrap = (LinearLayout) dialog.findViewById(R.id.dialog_scroll_wrap);
		final ScrollView scroll = (ScrollView) dialog.findViewById(R.id.dialog_scroll);
		final LinearLayout inputsContainer = (LinearLayout) dialog.findViewById(R.id.dialog_inputs);

		if (inputList == null) {
			Utils.remove(scrollWrap);
		} else {
			for (DialogInput i : inputList) {
				EditText et = new EditText(activity);
				et.setTextColor(Utils.color(R.color.white));
				et.setInputType(i.getInputType());
				et.setHint(i.getInputHint());
				et.setText(i.getValue());
				et.setBackgroundResource(R.drawable.frame_input_states);
				et.setPadding(Utils.px(8),Utils.px(8),Utils.px(8), Utils.px(4));
				inputsContainer.addView(et);
			}
		}

		// give scrollable view a max height
		ViewGroup.LayoutParams scrollWrapParams = scrollWrap.getLayoutParams();
		scroll.measure(scroll.getWidth(), scroll.getHeight());
		if (scroll.getMeasuredHeight() < scrollWrapParams.height) {
			scrollWrapParams.height = scroll.getMeasuredHeight();
			scrollWrap.setLayoutParams(scrollWrapParams);
		}

		final TextView title = (TextView)dialog.findViewById(R.id.dialog_title);
		title.setText(options.getTitle());

		final TextView description = (TextView)dialog.findViewById(R.id.dialog_description);
		description.setText(options.getDescription());

		final Button negative = (Button)dialog.findViewById(R.id.dialog_negative);
		negative.setText(options.getNegative());

		final Button positive = (Button)dialog.findViewById(R.id.dialog_positive);
		positive.setText(options.getPositive());

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
				if (inputList == null) {
					response.onPositive(null);
				} else {
					ArrayList<String> result = new ArrayList<String>();
					for (EditText et : editTexts) {
						result.add(et.getText().toString());
					}
					response.onPositive(result);
				}
				dialog.dismiss();
			}
		});

		dialog.show();
	}
}
