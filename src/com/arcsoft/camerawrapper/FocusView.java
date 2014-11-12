package com.arcsoft.camerawrapper;

import com.example.testcamera.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

 public class FocusView extends ImageView
{
	private Context mContext = null; 
	private Animation mAnimtion = null;
	public FocusView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public FocusView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FocusView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setScaleType(ScaleType.FIT_XY);
		setImageResource(R.drawable.focus_normal);
		
		mAnimtion = AnimationUtils.loadAnimation(mContext, R.anim.focus);
	}
	public void startFocus()
	{
		clearAnimation();
		setImageResource(R.drawable.focus_normal);
		startAnimation(mAnimtion);
		setVisibility(View.VISIBLE);
	}

	public void focusFailed()
	{
		clearAnimation();
		setImageResource(R.drawable.focus_failed);
		invalidate();
	}
	public void focusSucceed()
	{
		clearAnimation();
		setImageResource(R.drawable.focus_success);
		invalidate();
	}
	public void setInvisibilityDelayed(int delayedTime)
	 {
			postDelayed(new Runnable() {
	
				@Override
				public void run() {
					setVisibility(View.INVISIBLE);
				}
			}, delayedTime);
		}
}