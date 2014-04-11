package com.stealth.dialog;

/**
 * Listener interface for the confirm dialog
 * Created by OlivierHokke on 11-Apr-14.
 */
public interface IConfirmResponse {
	void onPositive();
	void onNegative();
	void onCancel();
}