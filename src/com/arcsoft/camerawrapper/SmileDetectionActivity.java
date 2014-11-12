package com.arcsoft.camerawrapper;

import java.io.IOException;

import com.example.testcamera.R;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SmileDetectionActivity extends Activity {


	private int mCameraId = CameraInfo.CAMERA_FACING_BACK;
	private Camera mCamera = null;
	private SurfaceView mSurfaceview = null;
	private SurfaceHolder mHolder = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smile_detection_activity);
		initUI();
	}

	@Override
	protected void onResume() {
		openCamera(mCameraId);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		stopPreview();
		super.onPause();
	}
	
	private void initUI() {


		mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
		mHolder = mSurfaceview.getHolder();
		mHolder.setKeepScreenOn(true);
		mHolder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				stopPreview();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
//				if (null == mCamera) {
//					mCamera = Camera.open(mCameraId);
//				}
//
//				initCameraParamter();
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				if (width < height) {
					return;
				}

				startPreview();
			}
		});
	
		
	}

   private boolean openCamera(int cameraId)
    {
    	mCamera = Camera.open(cameraId);
    	if(mCamera == null)
    		return false;
    	
    	initCameraParamter();
    	
    	return true;
    }
	private void initCameraParamter() {
		Parameters params = mCamera.getParameters();
		params.setPreviewSize(1920, 1080);
		mCamera.setParameters(params);
	}

	private void startPreview() {
		if(mCamera != null)
		{
			try {
				mCamera.setPreviewDisplay(mHolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mCamera.startPreview();
		}
	}

	private void stopPreview() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}


}
