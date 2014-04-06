package content;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {

	public CheckableLinearLayout(Context context) {
		super(context);
	}

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private static final int[] CheckedStateSet = {android.R.attr.state_checked};

	private boolean mChecked = false;
	private int mItemID = 0;

	public int getItemID() {
		return mItemID;
	}

	public void setItemID(int mItemID) {
		this.mItemID = mItemID;
	}

	public boolean isChecked() {
		return mChecked;
	}

	public void setChecked(boolean b) {
		if (b != mChecked) {
			mChecked = b;
			refreshDrawableState();
		}
	}

	public void toggle() {
		setChecked(!mChecked);
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CheckedStateSet);
		}
		return drawableState;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		invalidate();
	}

}