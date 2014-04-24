package com.stealth.font;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.stealth.utils.Utils;

/**
 * Manages and sets the fonts Created by OlivierHokke on 23-Apr-14.
 */
public class FontManager {

	public static final String TAG_BOLD_CONDENSED = "BoldCondensed";
	public static final String TAG_LIGHT_CONDENSED = "LightCondensed";
	public static final String TAG_REGULAR = "Regular";

	public static final String BOLD_CONDENSED_PATH = "fonts/YanoneKaffeesatz-Bold.ttf";
	public static final String LIGHT_CONDENSED_PATH = "fonts/YanoneKaffeesatz-Light.ttf";
	public static final String REGULAR_PATH = "fonts/RobotoSlab-Light.ttf";

	/**
	 * Sets the typeface for the given textview to the bold condensed one
	 *
	 * @param v the textview to set
	 */
	public static void setBoldCondensed(TextView v) {
		v.setTypeface(Typefaces.get(Utils.getContext(), BOLD_CONDENSED_PATH));
	}

	/**
	 * Sets the typeface for the given textview to the light condensed one
	 *
	 * @param v the textview to set
	 */
	public static void setLightCondensed(TextView v) {
		v.setTypeface(Typefaces.get(Utils.getContext(), LIGHT_CONDENSED_PATH));
	}

	/**
	 * Sets the typeface for the given textview to the regular one
	 *
	 * @param v the textview to set
	 */
	public static void setRegular(TextView v) {
		v.setTypeface(Typefaces.get(Utils.getContext(), REGULAR_PATH));
	}

	/**
	 * Go through entire hierarchy and finds textviews to set the font of, but will only do so with the views that have
	 * special font tags
	 *
	 * @param v the view group to traverse
	 */
	public static void handleFontTags(View v) {
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
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				handleFontTags(vg.getChildAt(i));
			}
		}
	}

	/**
	 * Helper class to retrieve fonts from
	 */
	private static class Typefaces {
		private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

		public static Typeface get(Context c, String assetPath) {
			synchronized (cache) {
				if (!cache.containsKey(assetPath)) {
					try {
						Typeface t = Typeface.createFromAsset(c.getAssets(),
								assetPath);
						cache.put(assetPath, t);
					} catch (Exception e) {
						Utils.d("Could not get typeface '" + assetPath
								+ "' because " + e.getMessage());
						return null;
					}
				}
				return cache.get(assetPath);
			}
		}
	}
}
