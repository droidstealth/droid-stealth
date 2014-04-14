package com.stealth.dialog;

import java.util.List;

import android.view.View;

/**
 * Created by Wolfox on 13-Apr-14.
 */
public interface IDialogAdapter<T> {
	/**
	 * @return the list of elements to show in the dialog
	 */
	List<T> getList();

	/**
	 * @return the resource id to inflate for each list item
	 */
	int getItemLayout();

	/**
	 * Fill up the given view with the given element of the list
	 *
	 * @param index the index of the element to use
	 * @param v     the view
	 */
	void setView(int index, View v);
}
