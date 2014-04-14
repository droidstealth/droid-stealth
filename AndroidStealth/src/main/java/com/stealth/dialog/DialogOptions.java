package com.stealth.dialog;

/**
 * An object to give the dialogManager in order to setup the dialog Created by OlivierHokke on 11-Apr-14.
 */
public class DialogOptions<T> {
	private int mTitle;
	private int mDescription;
	private int mPositive;
	private int mNegative;
	private boolean mReverseColors = false;
	private boolean mPositiveButtonEnabled = true;
	private boolean mNegativeButtonEnabled = true;
	private IDialogAdapter<T> mDialogAdapter;

	/**
	 * @return the object that will provide the list of items to show and will fill the corresponding view items
	 */
	public IDialogAdapter<T> getDialogAdapter() {
		return mDialogAdapter;
	}

	/**
	 * @param dialogAdapter the object that will provide the list of items to show and will fill the corresponding view
	 *                      items
	 * @return itself, for call chaining
	 */
	public DialogOptions<T> setDialogAdapter(IDialogAdapter<T> dialogAdapter) {
		mDialogAdapter = dialogAdapter;
		return this;
	}

	public boolean isReverseColors() {
		return mReverseColors;
	}

	/**
	 * Should the button colors be reversed?
	 *
	 * @param reverseColors true for green on negative button, red on positive button
	 * @return itself, for call chaining
	 */
	public DialogOptions<T> setReverseColors(boolean reverseColors) {
		mReverseColors = reverseColors;
		return this;
	}

	public boolean isPositiveButtonEnabled() {
		return mPositiveButtonEnabled;
	}

	public DialogOptions<T> setPositiveButtonEnabled(boolean positiveButtonEnabled) {
		mPositiveButtonEnabled = positiveButtonEnabled;
		return this;
	}

	public boolean isNegativeButtonEnabled() {
		return mNegativeButtonEnabled;
	}

	public DialogOptions<T> setNegativeButtonEnabled(boolean negativeButtonEnabled) {
		mNegativeButtonEnabled = negativeButtonEnabled;
		return this;
	}

	public int getTitle() {
		return mTitle;
	}

	public DialogOptions<T> setTitle(int title) {
		mTitle = title;
		return this;
	}

	public int getDescription() {
		return mDescription;
	}

	public DialogOptions<T> setDescription(int descriptionResource) {
		mDescription = descriptionResource;
		return this;
	}

	public int getPositive() {
		return mPositive;
	}

	public DialogOptions<T> setPositive(int positiveResource) {
		mPositive = positiveResource;
		return this;
	}

	public int getNegative() {
		return mNegative;
	}

	public DialogOptions<T> setNegative(int negativeResource) {
		mNegative = negativeResource;
		return this;
	}
}
