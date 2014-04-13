package com.stealth.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.stealth.android.R;
import com.stealth.utils.Utils;

/**
 * This class can show different dialogs. Created by OlivierHokke on 11-Apr-14.
 */
public class DialogConstructor {
	public static Dialog show(Activity activity, final DialogOptions options, final IDialogResponse response) {

		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_base);

		final ArrayList<EditText> editTexts = new ArrayList<EditText>();
		final IDialogAdapter<?> adapter = options.getDialogAdapter();
		final ListView list = (ListView) dialog.findViewById(R.id.dialog_list);

		if (adapter == null) {
			Utils.remove(list);
		}
		else {
			list.setAdapter(new DialogInternalAdapter(adapter));
		}

		// give scrollable view a max height
		ViewGroup.LayoutParams scrollWrapParams = list.getLayoutParams();
		list.measure(list.getWidth(), list.getHeight());
		Utils.d("Ok " + list.getWidth() + " ; " + list.getHeight());
		Utils.d("Ok " + list.getMeasuredWidth() + " ; " + list.getMeasuredHeight());

		//		if (scroll.getMeasuredHeight() < scrollWrapParams.height) {
		//			scrollWrapParams.height = scroll.getMeasuredHeight();
		//			scrollWrap.setLayoutParams(scrollWrapParams);
		//		}

		final TextView title = (TextView) dialog.findViewById(R.id.dialog_title);
		title.setText(options.getTitle());

		final TextView description = (TextView) dialog.findViewById(R.id.dialog_description);
		description.setText(options.getDescription());

		Button negative = (Button) dialog.findViewById(R.id.dialog_negative);
		if (!options.isNegativeButtonEnabled()) {
			Utils.remove(negative);
		}
		else {
			negative.setText(options.getNegative());
			if (options.isReverseColors()) {
				negative.setTextColor(Utils.color(R.color.positive));
			}
			negative.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (response != null) {
						response.onNegative();
					}
					dialog.dismiss();
				}
			});
		}

		Button positive = (Button) dialog.findViewById(R.id.dialog_positive);
		if (!options.isPositiveButtonEnabled()) {
			Utils.remove(positive);
		}
		else {
			positive.setText(options.getPositive());
			if (options.isReverseColors()) {
				positive.setTextColor(Utils.color(R.color.negative));
			}
			positive.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (response != null) {
						response.onPositive();
					}
					dialog.dismiss();
				}
			});
		}

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				if (response != null) {
					response.onCancel();
				}
			}
		});
		dialog.show();
		return dialog;
	}


	public static class DialogInternalAdapter extends BaseAdapter {

		private IDialogAdapter<?> mDialogAdapter;

		public DialogInternalAdapter(IDialogAdapter<?> dialogAdapter) {
			mDialogAdapter = dialogAdapter;
		}

		@Override
		public int getCount() {
			return mDialogAdapter.getList().size();
		}

		@Override
		public Object getItem(int i) {
			return mDialogAdapter.getList().get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isEmpty() {
			return mDialogAdapter.getList().isEmpty();
		}

		@Override
		public View getView(int i, View v, ViewGroup parent) {
			if (v == null) {
				LayoutInflater vi;
				vi = LayoutInflater.from(parent.getContext());
				v = vi.inflate(mDialogAdapter.getItemLayout(), null);
			}
			mDialogAdapter.setView(i, v);
			return v;
		}
	}
}
