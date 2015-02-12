package com.stealth.pin;

import android.app.Activity;
import android.content.SharedPreferences;

import com.stealth.utils.Utils;

/**
 * This class manages the pin code for a user
 * Created by OlivierHokke on 3/31/14.
 */
public class PinManager {

	public static final String EXTRA_PIN = "pinCode"; // for intents
	private static final String PIN_PREFS = "pin";
	private static final String PIN_FAKE = "FAKE_PIN";
	private static final String PIN_REAL = "REAL_PIN";
	private static final String PIN_FAKE_DEFAULT = "";
	private static final String PIN_REAL_DEFAULT = "";
	public static final int PIN_MAX_SIZE = 20;
	public static final int PIN_MIN_SIZE = 3;
	private static PinManager sInstance;

	/**
	 * Checks if given pin could indeed be used as a pin.
	 * @param pin the possible pin to check
	 * @return if the pin is a possible pin
	 */
	public static boolean isPossiblePin(String pin) {
		return PIN_MIN_SIZE <= pin.length() && pin.length() <= PIN_MAX_SIZE;
	}

	/**
	 * Get the singleton instance of this class
	 * @return
	 */
	public static PinManager get() {
		if (sInstance == null) {
			sInstance = new PinManager();
		}
		return sInstance;
	}

	private String mFakePin = "";
	private String mRealPin = "";
	private SharedPreferences mSharedPrefs;

	/**
	 * Private constructor. Use get() to get instance.
	 */
	private PinManager() {
		// TODO use our own encrypted preferences object!!
		mSharedPrefs = Utils.getContext().getSharedPreferences(PIN_PREFS, Activity.MODE_PRIVATE);
		mRealPin = mSharedPrefs.getString(PIN_REAL, PIN_REAL_DEFAULT);
		mFakePin = mSharedPrefs.getString(PIN_FAKE, PIN_FAKE_DEFAULT);
	}

	/**
	 * Check if the user has set a pin at all
	 * @return if user has set a pin
	 */
	public boolean hasPin() {
		return !mRealPin.equals("");
	}

	/**
	 * Sets the new real pin code
	 * @param pin the new real pin code to save
	 */
	public void setRealPin(String pin) {
		mRealPin = pin;
		mSharedPrefs.edit().putString(PIN_REAL, pin).apply();
	}

	/**
	 * Sets the new fake pin code
	 * @param pin the new fake pin code to save
	 */
	public void setFakePin(String pin) {
		mFakePin = pin;
		mSharedPrefs.edit().putString(PIN_FAKE, pin).commit();
	}

	/**
	 * Check if given pin is the real pin
	 * @param pin the pin code to test
	 * @return whether pin is indeed the real pin code
	 */
	public boolean isRealPin(String pin) {
		if (pin == null) return false;
		return pin.equals(mRealPin);
	}

	/**
	 * Check if given pin is the fake pin
	 * @param pin the pin code to test
	 * @return whether pin is indeed the fake pin code
	 */
	public boolean isFakePin(String pin) {
		if (pin == null) return false;
		return pin.equals(mFakePin);
	}

	/**
	 * Check if given pin is the fake or the real pin
	 * @param pin the pin code to test
	 * @return whether pin is indeed the fake or the real pin code
	 */
	public boolean isPin(String pin) {
		return isRealPin(pin) || isFakePin(pin);
	}

}
