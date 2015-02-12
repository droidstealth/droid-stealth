package com.stealth.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.stealth.android.R;
import com.stealth.font.FontManager;
import com.stealth.utils.Utils;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link GeneralSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GeneralSettingsFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GeneralSettingsFragment.
     */
    public static GeneralSettingsFragment newInstance() {
        GeneralSettingsFragment fragment = new GeneralSettingsFragment();
        return fragment;
    }
    public GeneralSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
	        // we have no arguments
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_general_settings, container, false);

	    CheckBox box;

	    box = (CheckBox) v.findViewById(R.id.general_thumbnails);
	    box.setChecked(GeneralSettingsManager.isThumbnailsShown());
	    box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
			    GeneralSettingsManager.setShowThumbnails(b);
			    Utils.toast(R.string.general_saved);
		    }
	    });

	    box = (CheckBox) v.findViewById(R.id.general_doubletap_lock);
	    box.setChecked(GeneralSettingsManager.isDoubleTapLock());
	    box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
			    GeneralSettingsManager.setDoubleTapLock(b);
			    Utils.toast(R.string.general_saved);
		    }
	    });

	    box = (CheckBox) v.findViewById(R.id.general_doubletap_unlock);
	    box.setChecked(GeneralSettingsManager.isDoubleTapUnlock());
	    box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
			    GeneralSettingsManager.setDoubleTapUnlock(b);
			    Utils.toast(R.string.general_saved);
		    }
	    });

	    FontManager.handleFontTags(v);

	    return v;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
