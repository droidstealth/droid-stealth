package com.stealth.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.stealth.android.R;
import com.stealth.utils.Utils;

/**
 * This class can show different dialogs.
 * Created by OlivierHokke on 11-Apr-14.
 */
public class DialogConstructor {

	public static void show(Activity activity, final DialogOptions options, final IDialogResponse response) {

		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_base);

		final ArrayList<EditText> editTexts = new ArrayList<EditText>();
		final ArrayList<IDialogElement> elementList = options.getElements();
		final LinearLayout scrollWrap = (LinearLayout) dialog.findViewById(R.id.dialog_scroll_wrap);
		final ScrollView scroll = (ScrollView) dialog.findViewById(R.id.dialog_scroll);
		final LinearLayout inputsContainer = (LinearLayout) dialog.findViewById(R.id.dialog_inputs);

		LayoutInflater inflater = activity.getLayoutInflater();

		if (elementList == null) {
			Utils.remove(scrollWrap);
		} else {
			for (IDialogElement i : elementList) {
				if (i instanceof DialogInput) {

					//construct input field
					DialogInput di = (DialogInput) i;
					EditText et = (EditText) inflater.inflate(R.layout.dialog_input, inputsContainer);
					if (et == null) continue;

					et.setInputType(di.getInputType());
					et.setHint(di.getInputHint());
					et.setText(di.getValue());
					inputsContainer.addView(et);

				} else if (i instanceof DialogButton) {

					//construct button field
					final DialogButton db = (DialogButton) i;
					LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.dialog_button, null);
					if (ll == null) continue;

					((ImageView)ll.findViewById(R.id.dialog_button_icon)).setImageDrawable(db.getIcon());
					((TextView)ll.findViewById(R.id.dialog_button_title)).setText(db.getTitle());

					ll.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							boolean result = response.onButton(elementList.indexOf(db));
							if (result) {
								dialog.dismiss();
							}
						}
					});

					inputsContainer.addView(ll);

				}
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

		Button negative = (Button)dialog.findViewById(R.id.dialog_negative);
		if (!options.isNegativeButtonEnabled()) {
			Utils.remove(negative);
		} else {
			negative.setText(options.getNegative());
			if (options.isReverseColors()) {
				negative.setTextColor(Utils.color(R.color.positive));
			}
			negative.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					response.onNegative();
					dialog.dismiss();
				}
			});
		}

		Button positive = (Button)dialog.findViewById(R.id.dialog_positive);
		if (!options.isPositiveButtonEnabled()) {
			Utils.remove(positive);
		} else {
			positive.setText(options.getPositive());
			if (options.isReverseColors()) {
				positive.setTextColor(Utils.color(R.color.negative));
			}
			positive.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (elementList == null) {
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
		}

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				response.onCancel();
			}
		});
		dialog.show();
	}
}
