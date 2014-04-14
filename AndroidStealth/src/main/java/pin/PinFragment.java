package pin;

import java.util.Random;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.stealth.android.R;
import com.stealth.utils.Utils;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PinFragment.OnPinResult} interface
 * to handle interaction events.
 * Use the {@link PinFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PinFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
	// the fragment initialization parameters
	public static final String ARG_DESCRIPTION = "pinDescription";
	public static final String ARG_TITLE = "pinTitle";
	public static final String ARG_PIN = "pinCode";

	private static final int RESET_STEP_YOUR = 0;
	private static final int RESET_STEP_FIRST = 1;
	private static final int RESET_STEP_SECOND = 2;
	private static final int RESET_STEP_DONE = 3;
	private static final int RESET_STEP_SPAM = 4;

	private int mDescriptionResource;
	private int mTitleResource;
	private String mCurrentPin;
	private Animation mSmallShake;
	private Animation mNormalShake;

	private TextView mTitle;
	private TextView mDescription;
	private TextView mPin;
	private TableLayout mKeyboard;

	private int mCurrentResetStep;
	private String mResettingPin;

	private OnPinResult mListener;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param pinTitle The title to show
	 * @param pinDescription The text resource to show.
	 * @param pin the pin
	 * @return A new instance of fragment PinFragment.
	 */
	public static PinFragment newInstance(int pinTitle, int pinDescription, String pin) {
		PinFragment fragment = new PinFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TITLE, pinTitle);
		args.putInt(ARG_DESCRIPTION, pinDescription);
		args.putString(ARG_PIN, pin);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Creates a new instance of the pin fragment but lets it use its default logics:
	 * pin resets, and if pin does not yet exist, pin creation
	 * @return A new instance of fragment PinFragment.
	 */
	public static PinFragment newInstance() {
		PinFragment fragment = new PinFragment();
		return fragment;
	}

	public PinFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mTitleResource = getArguments().getInt(ARG_TITLE);
			mDescriptionResource = getArguments().getInt(ARG_DESCRIPTION);
			mCurrentPin = getArguments().getString(ARG_PIN);
		} else {
			mDescriptionResource = R.string.pin_reset_your;
			mTitleResource = R.string.pin_reset_title;
			mCurrentPin = "";
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		// force portrait, as the pin layout otherwise fails. It should be completely visible
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public void onPause() {
		super.onPause();
		// let the sensor now define the orientation
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View root = inflater.inflate(R.layout.fragment_pin, container, false);

		if (root == null) {
			return root;
		}

		long animStep = 10;
		long animNow = 100;

		mTitle = (TextView)root.findViewById(R.id.pin_title);
		mTitle.setText(mTitleResource);
		mDescription = (TextView)root.findViewById(R.id.pin_description);
		mDescription.setText(mDescriptionResource);
		mPin = (TextView)root.findViewById(R.id.pin_entry);
		mPin.setText(mCurrentPin);
		mKeyboard = (TableLayout)root.findViewById(R.id.pin_keyboard);

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

		Utils.fadein(mTitle, animNow); animNow += animStep;
		Utils.fadein(mDescription, animNow); animNow += animStep;
		Utils.fadein(mPin, animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_1_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_2_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_3_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_4_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_5_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_6_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_7_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_8_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_9_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_asterisk_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_0_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_hashtag_container), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_cancel), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_accept), animNow); animNow += animStep;
		Utils.fadein(root.findViewById(R.id.pin_delete), animNow);

		mSmallShake = AnimationUtils.loadAnimation(root.getContext(), R.anim.shake_small);
		mNormalShake = AnimationUtils.loadAnimation(root.getContext(), R.anim.shake_normal);

		if (getActivity() instanceof OnPinResult) {
			mListener = (OnPinResult) getActivity();
		} else {
			mListener = getDefaultLogics();
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			root.setPadding(0, 0, 0, Utils.px(16));
			root.findViewById(R.id.pin_container).setLayoutParams(params);
		}



		setHasOptionsMenu(false);

		return root;
	}

	/**
	 * Builds the onPinResult listener that will help with the logics of the pin resets.
	 * @return the listener
	 */
	private OnPinResult getDefaultLogics() {

		mCurrentResetStep = 0;
		mResettingPin = "";
		showResetText();

		return new OnPinResult() {
			@Override
			public boolean onPinEntry(String pin) {
				return onResetPinEntry(pin);
			}

			@Override
			public void onPinCancel() {
				mCurrentResetStep = 0;
				mResettingPin = "";
				showResetText();
				pinClear();
			}
		};
	}

	/**
	 * This method does all the thinking and state management for the pin reset.
	 * It is called when a pin is entered.
	 * @param pin the pin that was entered
	 * @return whether pin was good
	 */
	private boolean onResetPinEntry(String pin) {

		pinClear();

		switch (mCurrentResetStep) {

			case RESET_STEP_YOUR:
				if (PinManager.get().isRealPin(pin)) {
					mCurrentResetStep++;
					showResetText();
					return true;
				} else {
					return false;
				}

			case RESET_STEP_FIRST:
				mResettingPin = pin;
				mCurrentResetStep++;
				showResetText();
				return true;

			case RESET_STEP_SECOND:
				if (pin.equals(mResettingPin)) {
					PinManager.get().setRealPin(pin); // remember the pin
					mCurrentResetStep++;
					showResetText();
					return true;
				} else {
					return false;
				}

			case RESET_STEP_DONE:
				if (PinManager.get().isRealPin(pin)) {
					mCurrentResetStep++;
					showResetText();
					return true;
				} else {
					return false;
				}

			case RESET_STEP_SPAM:
				if (PinManager.get().isRealPin(pin)) {
					showResetText();
					return true;
				} else {
					return false;
				}
		}
		return false;
	}

	/**
	 * Method that will show the correct text based on the current reset step.
	 */
	private void showResetText() {
		switch (mCurrentResetStep) {

			case RESET_STEP_YOUR:
				mDescription.setText(R.string.pin_reset_your);
				mTitle.setText(R.string.pin_reset_title);
				break;

			case RESET_STEP_FIRST:
				mDescription.setText(R.string.pin_reset_first);
				mTitle.setText(R.string.pin_resetting_title);
				break;

			case RESET_STEP_SECOND:
				mDescription.setText(R.string.pin_reset_second);
				mTitle.setText(R.string.pin_resetting_title);
				break;

			case RESET_STEP_DONE:
				mDescription.setText(R.string.pin_reset_done);
				mTitle.setText(R.string.pin_reset_success_title);
				break;

			case RESET_STEP_SPAM:
				String spam = Utils.str(R.string.pin_reset_spam);
				String[] spammers = spam.split("\\|");
				int i = new Random(System.nanoTime()).nextInt(spammers.length);
				mDescription.setText(spammers[i]);
				mTitle.setText(R.string.pin_practice_title);
				break;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	/**
	 * Inflates normal Menu.
	 *
	 * @param menu     The menu to which the items should be inflated
	 * @param inflater The inflater which is used to inflate the Menu
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
	}

	/**
	 * Shakes this fragment shortly to indicate a minor error.
	 */
	public void shakeSmall() {
		getView().startAnimation(mSmallShake);
	}
	/**
	 * Shakes this fragment to indicate an error.
	 */
	public void shakeNormal() {
		getView().startAnimation(mNormalShake);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public void updatePin() {
		((TextView)getView().findViewById(R.id.pin_entry)).setText(mCurrentPin);
	}

	/**
	 * Adds given char to the pin
	 * @param character
	 */
	public void pinAdd(String character) {
		if (mCurrentPin.length() < PinManager.PIN_MAX_SIZE) {
			mCurrentPin += character;
			updatePin();
		} else {
			shakeSmall();
		}
	}

	/**
	 * Remove a character from the pin
	 */
	public void pinPop() {
		if (mCurrentPin.length() == 0) return;
		mCurrentPin = mCurrentPin.substring(0, mCurrentPin.length() - 1);
		updatePin();
	}

	/**
	 * Clear pin entirely, start over
	 */
	public void pinClear() {
		mCurrentPin = "";
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

			case R.id.pin_accept:
				if(mListener != null)
					if (!PinManager.isPossiblePin(mCurrentPin)
							|| !mListener.onPinEntry(mCurrentPin)) {
						shakeNormal();
					}
				break;
			case R.id.pin_cancel:
				if(mListener != null)
					mListener.onPinCancel();
				break;
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
	 * Allows interaction with this fragment
	 */
	public interface OnPinResult {
		public boolean onPinEntry(String pin);
		public void onPinCancel();
	}

}
