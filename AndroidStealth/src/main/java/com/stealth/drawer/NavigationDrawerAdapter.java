package com.stealth.drawer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.stealth.android.R;
import com.stealth.utils.Utils;

/**
 * The content adapter for the listview in the navigation drawer
 * Created by OlivierHokke on 12-Apr-14.
 */
public class NavigationDrawerAdapter extends ArrayAdapter<NavigationDrawerItem> {

	public NavigationDrawerAdapter(Context context) {
		super(context, R.layout.item_navigation_drawer);
	}

	public NavigationDrawerAdapter(Context context, List<NavigationDrawerItem> items) {
		super(context, R.layout.item_navigation_drawer, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;

		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.item_navigation_drawer, null);
		}

		NavigationDrawerItem p = getItem(position);

		if (p != null && v != null) {
			FrameLayout col = (FrameLayout) v.findViewById(R.id.drawer_item_color);
			ImageView ico = (ImageView) v.findViewById(R.id.drawer_item_icon);
			TextView name = (TextView) v.findViewById(R.id.drawer_item_name);

			col.setBackgroundColor(Utils.color(p.getColor()));
			ico.setImageResource(p.getIcon());
			name.setText(p.getName());
		}

		return v;

	}
}