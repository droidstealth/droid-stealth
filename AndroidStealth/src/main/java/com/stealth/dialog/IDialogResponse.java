package com.stealth.dialog;

import java.util.ArrayList;

/**
 * Listener interface for the input dialog
 * Created by OlivierHokke on 11-Apr-14.
 */
public interface IDialogResponse {
	void onPositive(ArrayList<String> input);
	void onNegative();
	void onCancel();
}
