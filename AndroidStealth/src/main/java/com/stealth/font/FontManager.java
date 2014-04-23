package com.stealth.font;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.stealth.android.R;
import com.stealth.utils.Utils;

/**
 * Manages and sets the fonts
 * Created by OlivierHokke on 23-Apr-14.
 */
public class FontManager {

	public static final String TAG_BOLD_CONDENSED = "BoldCondensed";
	public static final String TAG_LIGHT_CONDENSED = "LightCondensed";

	private static Typeface sBoldCondensed;
	private static Typeface sLightCondensed;
	private static boolean sInitialized = false;
	private static void initialize() {
		if (!sInitialized){
			sBoldCondensed = Typeface.createFromAsset(Utils.getContext().getAssets(), "fonts/YanoneKaffeesatz-Bold.ttf");
			sLightCondensed = Typeface.createFromAsset(Utils.getContext().getAssets(), "fonts/YanoneKaffeesatz-Light.ttf");
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
	 * Go through entire hierarchy and finds textviews to set the font of, but will
	 * only do so with the views that have special font tags
	 * @param v the view group to traverse
	 */
	public static void handleFontTags(ViewGroup v) {
		initialize();
		for (int i = 0; i < v.getChildCount(); i++) {
			View c = v.getChildAt(i);
			if (c instanceof TextView) {
				if (c.getTag() == null) {
					continue;
				}
				else if (c.getTag().equals(TAG_BOLD_CONDENSED)) {
					setBoldCondensed((TextView) c);
				}
				else if (c.getTag().equals(TAG_LIGHT_CONDENSED)) {
					setLightCondensed((TextView) c);
				}
			}
			else if (c instanceof ViewGroup) {
				handleFontTags((ViewGroup) c);
			}
		}
	}
}
