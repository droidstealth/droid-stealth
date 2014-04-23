package com.stealth.dialog;

/**
 * Listener interface for the input dialog Created by OlivierHokke on 11-Apr-14.
 */
public interface IDialogResponse {
	void onPositive();
	void onNegative();
	void onCancel();
}
