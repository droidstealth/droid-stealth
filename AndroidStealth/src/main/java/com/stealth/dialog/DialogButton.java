package com.stealth.dialog;

import android.graphics.drawable.Drawable;

/**
 * This class represents a button that can be added to the dialog in the elements list.
 * Created by OlivierHokke on 13-Apr-14.
 */
public class DialogButton implements IDialogElement {
	private Drawable mIcon;
	private String mTitle;

	public DialogButton(Drawable icon, String title) {
		mIcon = icon;
		mTitle = title;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public DialogButton setIcon(Drawable icon) {
		mIcon = icon;
		return this;
	}

	public String getTitle() {
		return mTitle;
	}

	public DialogButton setTitle(String title) {
		mTitle = title;
		return this;
	}
}
