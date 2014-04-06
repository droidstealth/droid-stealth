package com.stealth.files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This JSONArray object is created to allow the removal of elements in android API < 19. However,
 * this class breaks the order of the objects. It keeps track of its own length value (which is
 * stored as the first element of this array), and when removing elements, we simply replace
 * them with the last element in the list (essentially swapping some index with the last). Finally
 * we set the last element to null. Therefore, it is possible that the toString() outputs several
 * null elements at the end of the list.
 *
 * This class is therefore quite quick when dealing with long arrays, but gets bloated if many
 * elements are removed and the order does not get preserved.
 *
 * TODO: preserve order using a mapping in a list perhaps
 * TODO: remove ",null,null,null" at end of string
 *
 * Created by OlivierHokke on 4/2/14.
 */
public class JSONArrayUnsorted {

	/**
	 * This class gives a user full feedback on the result of an element remove.
	 */
	public class JSONArrayRemoveResult {
		private Object mRemoved;
		private Object mMoved;
		private int mMovedFromIndex;

		/**
		 * Create the feedback object
		 * @param mRemoved the removed object at the index given during the remove() call
		 * @param mMoved the object that moved from the end of the array to the given index
		 * @param mMovedFromIndex the index that the object moved from to replace the now removed JSON element
		 */
		public JSONArrayRemoveResult(Object mRemoved, Object mMoved, int mMovedFromIndex) {
			this.mRemoved = mRemoved;
			this.mMoved = mMoved;
			this.mMovedFromIndex = mMovedFromIndex;
		}

		/**
		 * @return the removed object at the index given during the remove() call
		 */
		public Object getRemovedElement() {
			return mRemoved;
		}

		/**
		 * @return the object that moved from the end of the array to the given index
		 */
		public Object getMovedElement() {
			return mMoved;
		}

		/**
		 * @return the index that the object moved from to replace the now removed JSON element
		 */
		public int getMovedFromIndex() {
			return mMovedFromIndex;
		}
	}

	private JSONArray mManaged;

	/**
	 * Give the JSON array that is to be managed
	 * @param toManage to manage JSON array
	 */
	public JSONArrayUnsorted(JSONArray toManage) {
		mManaged = toManage;
	}

	/**
	 * Retreive the JSON Array that is managed by this class.
	 * DO NOT MODIFY THIS CLASS!!!!!!!!!! Only this class should
	 * @return the managed JSON Array
	 */
	public JSONArray getManagedArray() {
		return mManaged;
	}

	public int length() {
		try {
			return mManaged.getInt(0);
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Removes the element at the given position, and returns the element
	 * that was moved from the back to the new available empty spot due to the
	 * deletion
	 * @param index the index at which to remove an element
	 * @return the result of the remove action
	 */
	public JSONArrayRemoveResult remove(int index) {
		if (length() <= 0) return null;
		try {
			index++; // object really is 1 further away
			int end = length(); // normally -1, but they are 1 element further away
			Object removed = mManaged.get(index);
			Object last = mManaged.get(end);
			mManaged.put(index, last); // swap
			mManaged.put(end, JSONObject.NULL); // set last element to null
			decrease(); // we now removed an element so shrink the array
			return new JSONArrayRemoveResult(removed, last, end); // return the result of the remove
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		}
	}

	private void possiblyPaddedLength(int index) {
		try {
			mManaged.put(0, Math.max(length(), index + 1));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void increment() {
		try {
			mManaged.put(0, length() + 1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void decrease() {
		try {
			mManaged.put(0, length() - 1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public JSONArrayUnsorted put(boolean value) {
		increment();
		try {
			// the last few elements are possibly null, we should use our own last element
			mManaged.put(length(), value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JSONArrayUnsorted put(double value) throws JSONException {
		increment();
		// the last few elements are possibly null, we should use our own last element
		mManaged.put(length(), value);
		return this;
	}

	public JSONArrayUnsorted put(int value) {
		increment();
		try {
			// the last few elements are possibly null, we should use our own last element
			mManaged.put(length(), value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JSONArrayUnsorted put(long value) {
		increment();
		try {
			// the last few elements are possibly null, we should use our own last element
			mManaged.put(length(), value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JSONArrayUnsorted put(Object value) {
		increment();
		try {
			// the last few elements are possibly null, we should use our own last element
			mManaged.put(length(), value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}

	public JSONArrayUnsorted put(int index, boolean value) throws JSONException {
		mManaged.put(index + 1, value);
		possiblyPaddedLength(index);
		return this;
	}

	public JSONArrayUnsorted put(int index, double value) throws JSONException {
		mManaged.put(index + 1, value);
		possiblyPaddedLength(index);
		return this;
	}

	public JSONArrayUnsorted put(int index, int value) throws JSONException {
		mManaged.put(index + 1, value);
		possiblyPaddedLength(index);
		return this;
	}

	public JSONArrayUnsorted put(int index, long value) throws JSONException {
		mManaged.put(index + 1, value);
		possiblyPaddedLength(index);
		return this;
	}

	public JSONArrayUnsorted put(int index, Object value) throws JSONException {
		mManaged.put(index + 1, value);
		possiblyPaddedLength(index);
		return this;
	}

	public boolean isNull(int index) {
		return mManaged.isNull(index + 1);
	}

	public Object get(int index) throws JSONException {
		return mManaged.get(index + 1);
	}

	public Object opt(int index) {
		return mManaged.opt(index + 1);
	}

	public boolean getBoolean(int index) throws JSONException {
		return mManaged.getBoolean(index + 1);
	}

	public boolean optBoolean(int index) {
		return mManaged.optBoolean(index + 1);
	}

	public boolean optBoolean(int index, boolean fallback) {
		return mManaged.optBoolean(index + 1, fallback);
	}

	public double getDouble(int index) throws JSONException {
		return mManaged.getDouble(index + 1);
	}

	public double optDouble(int index) {
		return mManaged.optDouble(index + 1);
	}

	public double optDouble(int index, double fallback) {
		return mManaged.optDouble(index + 1, fallback);
	}

	public int getInt(int index) throws JSONException {
		return mManaged.getInt(index + 1);
	}

	public int optInt(int index) {
		return mManaged.optInt(index + 1);
	}

	public int optInt(int index, int fallback) {
		return mManaged.optInt(index + 1, fallback);
	}

	public long getLong(int index) throws JSONException {
		return mManaged.getLong(index + 1);
	}

	public long optLong(int index) {
		return mManaged.optLong(index + 1);
	}

	public long optLong(int index, long fallback) {
		return mManaged.optLong(index + 1, fallback);
	}

	public String getString(int index) throws JSONException {
		return mManaged.getString(index + 1);
	}

	public String optString(int index) {
		return mManaged.optString(index + 1);
	}

	public String optString(int index, String fallback) {
		return mManaged.optString(index + 1, fallback);
	}

	public JSONArray getJSONArray(int index) throws JSONException {
		return mManaged.getJSONArray(index + 1);
	}

	public JSONArray optJSONArray(int index) {
		return mManaged.optJSONArray(index + 1);
	}

	public JSONObject getJSONObject(int index) throws JSONException {
		return mManaged.getJSONObject(index + 1);
	}

	public JSONObject optJSONObject(int index) {
		return mManaged.optJSONObject(index + 1);
	}

	public JSONObject toJSONObject(JSONArray names) throws JSONException {
		return mManaged.toJSONObject(names);
	}

	public String join(String separator) throws JSONException {
		return mManaged.join(separator);
	}

	public String toString() {
		return mManaged.toString();
	}

	public String toString(int indentSpaces) throws JSONException {
		return mManaged.toString(indentSpaces);
	}

	public boolean equals(Object o) {
		return mManaged.equals(o);
	}

	public int hashCode() {
		return mManaged.hashCode();
	}
}
