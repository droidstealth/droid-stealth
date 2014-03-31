package com.stealth.android;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PinFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PinFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PinFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String mPin = "";

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PinFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PinFragment newInstance(String param1, String param2) {
        PinFragment fragment = new PinFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public PinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_pin, container, false);

        root.findViewById(R.id.pin_0_container).setOnClickListener(this);
        root.findViewById(R.id.pin_1_container).setOnClickListener(this);
        root.findViewById(R.id.pin_2_container).setOnClickListener(this);
        root.findViewById(R.id.pin_3_container).setOnClickListener(this);
        root.findViewById(R.id.pin_4_container).setOnClickListener(this);
        root.findViewById(R.id.pin_5_container).setOnClickListener(this);
        root.findViewById(R.id.pin_6_container).setOnClickListener(this);
        root.findViewById(R.id.pin_7_container).setOnClickListener(this);
        root.findViewById(R.id.pin_8_container).setOnClickListener(this);
        root.findViewById(R.id.pin_9_container).setOnClickListener(this);
        root.findViewById(R.id.pin_0_container).setOnClickListener(this);
        root.findViewById(R.id.pin_asterisk_container).setOnClickListener(this);
        root.findViewById(R.id.pin_hashtag_container).setOnClickListener(this);
        root.findViewById(R.id.pin_cancel).setOnClickListener(this);
        root.findViewById(R.id.pin_accept).setOnClickListener(this);
        root.findViewById(R.id.pin_delete).setOnClickListener(this);
        root.findViewById(R.id.pin_delete).setOnLongClickListener(this);

        ((TextView)root.findViewById(R.id.pin_entry)).setText(mPin);

        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updatePin() {
        ((TextView)getView().findViewById(R.id.pin_entry)).setText(mPin);
    }

    /**
     * Adds given char to the pin
     * @param character
     */
    public void pinAdd(String character) {
        mPin += character;
        updatePin();
    }

    /**
     * Remove a character from the pin
     */
    public void pinPop() {
        mPin = mPin.substring(0, mPin.length() - 1);
        updatePin();
    }

    /**
     * Clear pin entirely, start over
     */
    public void pinClear() {
        mPin = "";
        updatePin();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.pin_0_container: pinAdd("0"); break;
            case R.id.pin_1_container: pinAdd("1"); break;
            case R.id.pin_2_container: pinAdd("2"); break;
            case R.id.pin_3_container: pinAdd("3"); break;
            case R.id.pin_4_container: pinAdd("4"); break;
            case R.id.pin_5_container: pinAdd("5"); break;
            case R.id.pin_6_container: pinAdd("6"); break;
            case R.id.pin_7_container: pinAdd("7"); break;
            case R.id.pin_8_container: pinAdd("8"); break;
            case R.id.pin_9_container: pinAdd("9"); break;
            case R.id.pin_asterisk_container: pinAdd("*"); break;
            case R.id.pin_hashtag_container: pinAdd("#"); break;
            case R.id.pin_delete: pinPop(); break;
            case R.id.pin_accept: /* TODO */ break;
            case R.id.pin_cancel: getActivity().finish(); break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch(view.getId()) {
            case R.id.pin_delete: pinClear(); return true;
        }
        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
