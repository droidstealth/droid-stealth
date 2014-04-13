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

	/**
	 * Called when a button was pressed in the elements list.
	 * @param i the button that was pressed. Is index of element list.
	 * @return should the dialog be closed?
	 */
	boolean onButton(int i);
}
