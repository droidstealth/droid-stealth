package com.stealth.drawer;

/**
 * The items that the navigation drawer will contain Created by OlivierHokke on 12-Apr-14.
 */
public class NavigationDrawerItem {
	private int mName;
	private int mIcon;
	private int mColor;

	public NavigationDrawerItem(int name, int icon, int color) {
		mName = name;
		mIcon = icon;
		mColor = color;
	}

	public int getName() {
		return mName;
	}

	public void setName(int name) {
		mName = name;
	}

	public int getIcon() {
		return mIcon;
	}

	public void setIcon(int icon) {
		mIcon = icon;
	}

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		mColor = color;
	}
}
