package com.stealth.dialog;

import java.util.ArrayList;

/**
 * An object to give the dialogManager in order to setup the dialog
 * Created by OlivierHokke on 11-Apr-14.
 */
public class DialogOptions {
	private int mTitle;
	private int mDescription;
	private int mPositive;
	private int mNegative;
	private ArrayList<DialogInput> mInputs;

	public ArrayList<DialogInput> getInputs() {
		return mInputs;
	}

	public DialogOptions setInputs(ArrayList<DialogInput> inputs) {
		mInputs = inputs;
		return this;
	}

	public DialogOptions addInput(DialogInput input) {
		if (mInputs == null) {
			mInputs = new ArrayList<DialogInput>();
		}
		mInputs.add(input);
		return this;
	}

	public int getTitle() {
		return mTitle;
	}

	public DialogOptions setTitle(int title) {
		mTitle = title;
		return this;
	}

	public int getDescription() {
		return mDescription;
	}

	public DialogOptions setDescription(int descriptionResource) {
		mDescription = descriptionResource;
		return this;
	}

	public int getPositive() {
		return mPositive;
	}

	public DialogOptions setPositive(int positiveResource) {
		mPositive = positiveResource;
		return this;
	}

	public int getNegative() {
		return mNegative;
	}

	public DialogOptions setNegative(int negativeResource) {
		mNegative = negativeResource;
		return this;
	}
}
