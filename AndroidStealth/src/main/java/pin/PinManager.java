package pin;

import android.app.Activity;
import android.content.SharedPreferences;

import com.stealth.utils.Utils;

/**
 * This class manages the pin code for a user
 * Created by OlivierHokke on 3/31/14.
 */
public class PinManager {

    public static final String EXTRA_PIN = "pinCode"; // for intents
    private static final String PIN_PREFS = PinManager.class.getSimpleName();
    private static final String PIN_FAKE = "FAKE_PIN";
    private static final String PIN_REAL = "REAL_PIN";
    private static final String PIN_FAKE_DEFAULT = "#666";
    private static final String PIN_REAL_DEFAULT = "#555";
    private static PinManager sInstance;

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

    private String mFakePin = "#666";
    private String mRealPin = "#555";
    private SharedPreferences mSharedPrefs;

    /**
     * Private constructor. Use get() to get instance.
     */
    private PinManager() {
        mSharedPrefs = Utils.getContext().getSharedPreferences(PIN_PREFS, Activity.MODE_PRIVATE);
        mFakePin = mSharedPrefs.getString(PIN_FAKE, PIN_FAKE_DEFAULT);
        mRealPin = mSharedPrefs.getString(PIN_REAL, PIN_REAL_DEFAULT);
    }

    /**
     * Sets the new real pin code
     * @param pin the new real pin code to save
     */
    public void setRealPin(String pin) {
        mRealPin = pin;
        mSharedPrefs.edit().putString(PIN_REAL, mRealPin).commit();
    }

    /**
     * Sets the new fake pin code
     * @param pin the new fake pin code to save
     */
    public void setFakePin(String pin) {
        mFakePin = pin;
        mSharedPrefs.edit().putString(PIN_FAKE, mRealPin).commit();
    }

    /**
     * Check if given pin is the real pin
     * @param pin the pin code to test
     * @return whether pin is indeed the real pin code
     */
    public boolean isRealPin(String pin) {
        return pin.startsWith(mFakePin);
    }

    /**
     * Check if given pin is the fake pin
     * @param pin the pin code to test
     * @return whether pin is indeed the fake pin code
     */
    public boolean isFakePin(String pin) {
        return pin.startsWith(mRealPin);
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
