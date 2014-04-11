package com.stealth.dialog;

/**
 * Allows one to add input fields to a dialog
 * Created by OlivierHokke on 11-Apr-14.
 */
public class DialogInput {
	private int mInputHint;
	private String mValue;
	private int mInputType;
	private int mMaxLength;

	public int getInputHint() {
		return mInputHint;
	}

	public DialogInput setInputHint(int inputHint) {
		mInputHint = inputHint;
		return this;
	}

	public String getValue() {
		return mValue;
	}

	public DialogInput setValue(String value) {
		mValue = value;
		return this;
	}

	public int getInputType() {
		return mInputType;
	}

	public DialogInput setInputType(int inputType) {
		mInputType = inputType;
		return this;
	}

	public int getMaxLength() {
		return mMaxLength;
	}

	public DialogInput setMaxLength(int maxLength) {
		mMaxLength = maxLength;
		return this;
	}
}