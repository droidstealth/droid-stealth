package com.stealth.dialog;

/**
 * Allows one to add input fields to a dialog Created by OlivierHokke on 11-Apr-14.
 */
public class DialogInput {
	private int mInputHint;
	private String mValue;
	private int mInputType;
	private int mMaxLength;

	public int getInputHint() {
		return mInputHint;
	}

	/**
	 * Set the string resource that will hint the user of what to fill in when the input field is empty.
	 *
	 * @param inputHint string resource
	 * @return itself for call chaining
	 */
	public DialogInput setInputHint(int inputHint) {
		mInputHint = inputHint;
		return this;
	}

	public String getValue() {
		return mValue;
	}

	/**
	 * Set the start value of the input field
	 *
	 * @param value string resource
	 * @return itself for call chaining
	 */
	public DialogInput setValue(String value) {
		mValue = value;
		return this;
	}

	public int getInputType() {
		return mInputType;
	}

	/**
	 * Set the input type so android knows what can be filled in this input field. For instance just numbers?
	 *
	 * @param inputType InputType to let the keyboard change its layout accordingly
	 * @return itself for call chaining
	 */
	public DialogInput setInputType(int inputType) {
		mInputType = inputType;
		return this;
	}

	public int getMaxLength() {
		return mMaxLength;
	}

	/**
	 * Set the max length of the input field
	 *
	 * @param maxLength max character count in input field
	 * @return itself for call chaining
	 */
	public DialogInput setMaxLength(int maxLength) {
		mMaxLength = maxLength;
		return this;
	}
}