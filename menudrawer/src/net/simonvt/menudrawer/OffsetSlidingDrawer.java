package net.simonvt.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Copyright Infakt Sp. z o.o.
 * Created by Rafal Ciurkot (rafal.ciurkot@infakt.pl) on 11.12.14
 *
 * @author rciurkot
 */
public class OffsetSlidingDrawer extends SlidingDrawer {
	private int INITIAL_OFFSET_PX = 55;
	private OnOffsetChangedListener onOffsetChangedListener;

	OffsetSlidingDrawer(Activity activity, int dragMode) {
		super(activity, dragMode);
	}

	public OffsetSlidingDrawer(Context context) {
		super(context);
	}

	public OffsetSlidingDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public OffsetSlidingDrawer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setInitialOffsetPx(int initialOffsetPx) {
		this.INITIAL_OFFSET_PX = initialOffsetPx;
		setTouchBezelSize(initialOffsetPx);
	}

	@Override
	protected void drawOverlay(Canvas canvas) {
		//do nothing
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
			throw new IllegalStateException("Must measure with an exact size");
		}

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);

		if (mOffsetPixels == -1) openMenu(false);

		int menuWidthMeasureSpec;
		int menuHeightMeasureSpec;
		switch (getPosition()) {
			case TOP:
			case BOTTOM:
				menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
				menuHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, mMenuSize);
				break;

			default:
				// LEFT/RIGHT
				menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mMenuSize);
				menuHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
		}
		mMenuContainer.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

		final int contentWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width - INITIAL_OFFSET_PX);
		final int contentHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
		mContentContainer.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);

		setMeasuredDimension(width, height);

		updateTouchAreaSize();
	}

	@Override
	public void closeMenu(boolean animate) {
		animateOffsetTo(INITIAL_OFFSET_PX, 0, animate);
	}

	@Override
	protected void onMoveEvent(float dx, float dy) {
		switch (getPosition()) {
			case LEFT:
				float offsetPixels = Math.min(Math.max(mOffsetPixels + dx, INITIAL_OFFSET_PX), mMenuSize);
				setOffsetPixels(offsetPixels);
				break;

			default:
				super.onMoveEvent(dx, dy);
				break;
		}
	}

	@Override
	protected void onUpEvent(int x, int y) {
		final int offsetPixels = (int) mOffsetPixels;

		switch (getPosition()) {
			case LEFT: {
				if (mIsDragging) {
					mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
					final int initialVelocity = (int) getXVelocity(mVelocityTracker);
					mLastMotionX = x;
					animateOffsetTo(initialVelocity > 0 ? mMenuSize : INITIAL_OFFSET_PX, initialVelocity, true);

					// Close the menu when content is clicked while the menu is visible.
				} else if (mMenuVisible && x > offsetPixels) {
					closeMenu();
				}
				break;
			}
			default:
				super.onUpEvent(x, y);
				break;
		}
	}

	@Override
	protected void offsetMenu(int offsetPixels) {
		switch (getPosition()) {
			case LEFT: {
				//do nothing
				break;
			}
			default:
				super.offsetMenu(offsetPixels);
				break;
		}
	}

	@Override
	protected void setOffsetPixels(float offsetPixels) {
		super.setOffsetPixels(offsetPixels);
		mMenuVisible = offsetPixels > INITIAL_OFFSET_PX;
		if (onOffsetChangedListener != null) {
			float ratio = (offsetPixels - INITIAL_OFFSET_PX) / (1.0f * mMenuSize - INITIAL_OFFSET_PX);
			onOffsetChangedListener.onOffsetChanged(ratio, mMenuSize, INITIAL_OFFSET_PX);
		}
	}

	public void setOnOffsetChangedListener(OnOffsetChangedListener onOffsetChangedListener) {
		this.onOffsetChangedListener = onOffsetChangedListener;
	}

	public interface OnOffsetChangedListener {
		void onOffsetChanged(float ratio, int maxWidth, int initialOffset);
	}
}