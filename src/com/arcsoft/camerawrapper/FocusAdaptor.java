package com.arcsoft.camerawrapper;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

public class FocusAdaptor {
	private final static int    FOCUS_RECT_RADIUS = 100;//TODO
	private final static int    MSG_CANCEL_AUTO_FOCUS = 0x800;
	private FocusState state = new FocusState();
	private Rect   previewRect = new Rect();
	private Camera camera = null;
	private float  scaleX = 0;
	private float  scaleY = 0;
	private float  offsetX = 0;
	private float  offsetY = 0;
	private Handler mHandler = null;
	private FocusUIAdaptor uiAdaptor = null;
	private Camera.AutoFocusCallback autoFocusCallback = null;
	
	public FocusAdaptor()//, int displayW, int displayH)
	{
		initHandler();
		initAutoFocusCallback();
	}
	
	public boolean init(Camera camera, Rect cameraPreviewRect, FocusUIAdaptor uiAdapter)
	{
		this.camera = camera;
		this.uiAdaptor = uiAdapter;
		state.setFocusedIdle();
		previewRect.left = cameraPreviewRect.left;
		previewRect.top = cameraPreviewRect.top;
		previewRect.right = cameraPreviewRect.right;
		previewRect.bottom = cameraPreviewRect.bottom;

		offsetX = -previewRect.left;
		offsetY = -previewRect.top;
		
		int w = previewRect.right - previewRect.left;
		int h = previewRect.bottom - previewRect.top;
		if(camera == null || uiAdaptor == null ||
				w == 0 || h == 0)
			return false;
		scaleX = (float)2000 / w;
		scaleY = (float)2000 / h;
		return true;
	}
	
	public void uninit()
	{
		mHandler.removeMessages(MSG_CANCEL_AUTO_FOCUS);
		this.camera = null;
		this.uiAdaptor = null;
		previewRect.setEmpty();
		offsetX = 0;
		offsetY = 0;
		scaleX = 0;
		scaleY = 0;
	}
	
	private void initAutoFocusCallback()
	{
		autoFocusCallback = new AutoFocusCallbackWrap();
	}
	
	private void initHandler()
	{
		mHandler = new Handler()
		{
			@Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	                case MSG_CANCEL_AUTO_FOCUS: {
	                    cancelAutoFocus();
	                    uiAdaptor.setInvisibilityDelayed(0);
	                    state.setFocusedIdle();
	                    break;
	                }
	            }
	        }
		};
	}
	private void cancelAutoFocus()
	{
		camera.cancelAutoFocus();
	}
	
	public void touchToFocus(int touchedX, int touchedY)
	{
		if(!state.isFocusIdle())
			return;
		Camera.Parameters param = camera.getParameters();
		if(param.getMaxNumFocusAreas() <= 0)
			return;
		Rect rect = getFocusUIArea(touchedX, touchedY);
		uiAdaptor.setLocation(rect);
		setCameraFocusArea(rect, param);
		camera.setParameters(param);
		state.setFocusStarted();
		uiAdaptor.startFocus();
		camera.autoFocus(autoFocusCallback);
	}
	
	private Rect getFocusUIArea(int centerX, int centerY)
	{
		Rect rect = new Rect(centerX - FOCUS_RECT_RADIUS, centerY - FOCUS_RECT_RADIUS,
				centerX + FOCUS_RECT_RADIUS, centerY + FOCUS_RECT_RADIUS);
		return rect;
	}
	
	private void setCameraFocusArea(Rect locRect, Camera.Parameters param)
	{
		List<Camera.Area> focusAreas = param.getFocusAreas();
		if(focusAreas == null || focusAreas.size() == 0)
		{
			focusAreas = new ArrayList<Camera.Area>();
			Rect rect = new Rect();
			convert2FocusRect(locRect, rect);
			focusAreas.add(new Camera.Area(rect, 1000));
		}
		else
		{
			convert2FocusRect(locRect, focusAreas.get(0).rect);
		}
		param.setFocusAreas(focusAreas);
	}
	
	public void prepareCapture(Camera.AutoFocusCallback fc)
	{
		if(state.isFocussucceed())
		{
			uiAdaptor.setInvisibilityDelayed(0);
			fc.onAutoFocus(true, camera);
		}
		else
		{
			Camera.Parameters param = camera.getParameters();
			int centerX = (previewRect.right - previewRect.left) >> 1;
			int centerY = (previewRect.bottom  - previewRect.top) >> 1;
			Rect rect = getFocusUIArea(centerX, centerY);
			uiAdaptor.setLocation(rect);
			if(param.getMaxNumFocusAreas() > 0)
			{
				setCameraFocusArea(rect, param);
				camera.setParameters(param);
			}
			state.setFocusStarted();
			uiAdaptor.startFocus();
			camera.autoFocus(new AutoFocusCallbackWrap(fc, 800));
		}
		
	}
	
	
	private void convert2FocusRect(Rect orgRect, Rect resultRect)
	{
		int left = (int)((orgRect.left + offsetX) * scaleX - 1000);
		int top = (int)((orgRect.top + offsetY) * scaleY - 1000);
		int right = (int)((orgRect.right + offsetX) * scaleX - 1000);
		int bottom = (int)((orgRect.bottom + offsetY) * scaleY - 1000);
		resultRect.left = left;
		resultRect.top = top;
		resultRect.right = right;
		resultRect.bottom = bottom;
	}
	
	
	private class FocusState
	{
		int state = 0;
		public FocusState()
		{
			state = 0;
		}
		public boolean isFocusIdle()
		{
			return state == 0;
		}
		public boolean isFocusing()
		{
			return state == 1;
		}
		public boolean isFocussucceed()
		{
			return state == 2;
		}
		public void setFocusStarted()
		{
			state = 1;
		}
		public void setFocusEnd(boolean success)
		{
			if(success)
				state = 2;
			else
				state = 3;
		}
		public void setFocusedIdle()
		{
			state = 0;
		}
	}
	
	private class AutoFocusCallbackWrap implements Camera.AutoFocusCallback
	{
		private  Camera.AutoFocusCallback mCallback = null;
		private  int cancelAutoFocusSucceedDelayedTime = 3000;
		public AutoFocusCallbackWrap()
		{
			
		}
		//if cancelAutoFocusSucceedDelayedTime < 0, using default value 3000;
		public AutoFocusCallbackWrap(Camera.AutoFocusCallback callback, int cancelAutoFocusSucceedDelayedTime)
		{
			mCallback = callback;
			if(cancelAutoFocusSucceedDelayedTime >= 0)
				this.cancelAutoFocusSucceedDelayedTime = cancelAutoFocusSucceedDelayedTime;
		}

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			state.setFocusEnd(success);
			if(success)
			{
				uiAdaptor.focusSucceed();
				mHandler.sendEmptyMessageDelayed(MSG_CANCEL_AUTO_FOCUS, cancelAutoFocusSucceedDelayedTime);
			}
			else
			{
				uiAdaptor.focusFailed();
				mHandler.sendEmptyMessageDelayed(MSG_CANCEL_AUTO_FOCUS, 800);
			}
			if(mCallback != null)
				mCallback.onAutoFocus(success, camera);
		}
		
	}
	
	interface FocusUIAdaptor
	{
		public void setLocation(Rect location);
		public void startFocus();
		public void focusFailed();
		public void focusSucceed();
		public void setInvisibilityDelayed(int delayedTime);
	}

}
