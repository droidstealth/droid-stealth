package com.stealth.font;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.stealth.utils.Utils;

/**
 * Manages and sets the fonts
 * Created by OlivierHokke on 23-Apr-14.
 */
public class FontManager {

	public static final String TAG_BOLD_CONDENSED = "BoldCondensed";
	public static final String TAG_LIGHT_CONDENSED = "LightCondensed";
	public static final String TAG_REGULAR = "Regular";

	private static Typeface sBoldCondensed;
	private static Typeface sLightCondensed;
	private static Typeface sRegular;
	private static boolean sInitialized = false;
	private static void initialize() {
		if (!sInitialized){
			sBoldCondensed = Typeface.createFromAsset(Utils.getContext().getAssets(), "fonts/YanoneKaffeesatz-Bold.ttf");
			sLightCondensed = Typeface.createFromAsset(Utils.getContext().getAssets(), "fonts/YanoneKaffeesatz-Light.ttf");
			sRegular = Typeface.createFromAsset(Utils.getContext().getAssets(), "fonts/RobotoSlab-Light.ttf");
			sInitialized = true;
		}
	}

	/**
	 * Sets the typeface for the given textview to the bold condensed one
	 * @param v the textview to set
	 */
	public static void setBoldCondensed(TextView v) {
		initialize();
		v.setTypeface(sBoldCondensed);
	}

	/**
	 * Sets the typeface for the given textview to the light condensed one
	 * @param v the textview to set
	 */
	public static void setLightCondensed(TextView v) {
		initialize();
		v.setTypeface(sLightCondensed);
	}

	/**
	 * Sets the typeface for the given textview to the regular one
	 * @param v the textview to set
	 */
	public static void setRegular(TextView v) {
		initialize();
		v.setTypeface(sRegular);
	}

	/**
	 * Go through entire hierarchy and finds textviews to set the font of, but will
	 * only do so with the views that have special font tags
	 * @param v the view group to traverse
	 */
	public static void handleFontTags(View v) {
		initialize();
		if (v instanceof TextView) {
			Object tag = v.getTag();
			if (tag == null) {
				return;
			}
			else if (tag.equals(TAG_BOLD_CONDENSED)) {
				setBoldCondensed((TextView) v);
			}
			else if (tag.equals(TAG_LIGHT_CONDENSED)) {
				setLightCondensed((TextView) v);
			}
			else if (tag.equals(TAG_REGULAR)) {
				setRegular((TextView) v);
			}
		}
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup)v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				handleFontTags(vg.getChildAt(i));
			}
		}
	}
}
