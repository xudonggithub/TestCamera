package com.arcsoft.camerawrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.testcamera.R;
import com.example.testcamera.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public abstract class CameraActivity extends Activity {
   
    private final String TAG = "CameraActivity";
    protected Camera mCamera = null;
    private int mCameraId = CameraInfo.CAMERA_FACING_FRONT;
    
    private Camera.AutoFocusCallback autoFocusCallback = null;
    private PictureCallback rawPicCallback = null;
    private PictureCallback jpegPicCallback = null;
    private PictureCallback postviewPicCallback = null;
    private ShutterCallback mShutterCallback = null;
    private PreviewCallback mPreviewCallback = null;
    private ToneGenerator	mTone = null;
    private FocusAdaptor 	mFocusAdaptor = null;
    
    private int             mPreviewWidth, mPreviewHeight;
    private boolean         mbDumping = false;
    
    //********** views **********************//
    private SurfaceView mSurfaceView = null;
    private SurfaceHolder holder = null;
    private FocusView     mFocusView = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "---onCreate");
        initAutoFocusCallback();
        initShutterCallback();
		initJpegPictureCallback();
        initRawPictureCallback();
        initPostPictureCallback();
        initPreviewCallback();
        initUI();
        openCamera(mCameraId);
    }
    
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "---onDestroy");
    	super.onDestroy();
    }
    
    @Override
    protected void onStart() {
    	Log.d(TAG, "---onStart");
    	super.onStart();
    }
    
    @Override
    protected void onPause() {
    	Log.d(TAG, "---onPause");
    	stopPreview();
    	releaseCamera();
    	super.onPause();
    }
    
    @Override
    protected void onRestart() {
    	Log.d(TAG, "---onRestart");
    	super.onRestart();
    }
    
    @Override
    protected void onResume() {
    	Log.d(TAG, "---onResume");
    	openCamera(mCameraId);
    	super.onResume();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "---onActivityResult,requestCode:"+requestCode+",resultCode:"+resultCode);
    	
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	DisplayMetrics dm = getResources().getDisplayMetrics();
    	Log.d(TAG, "onConfigurationChanged, w = "+dm.widthPixels+", h="+dm.heightPixels);
    	setCameraDisplayOrientation();
    	super.onConfigurationChanged(newConfig);
    }
    
    abstract protected int getLayoutID();
    abstract protected int getRelativeLayoutIDForFocusView();
    
    final private void initUI()
    {
    	RelativeLayout mainView = (RelativeLayout)getLayoutInflater().inflate(getLayoutID(), null);
    	setContentView(mainView);
    	RelativeLayout focusParant = (RelativeLayout)mainView.findViewById(getRelativeLayoutIDForFocusView());
		mFocusView = new FocusView(this);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		mFocusView.setVisibility(View.INVISIBLE);
		focusParant.addView(mFocusView, lp);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					mFocusAdaptor.touchToFocus((int)event.getX(), (int)event.getY());
				}
				return true;
			}
		});
		holder = mSurfaceView.getHolder();
		holder.addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Log.d(TAG, "surfaceDestroyed");
				stopPreview();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				Log.d(TAG, "surfaceCreated");

			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				Log.d(TAG, "surfaceChanged");
				startPreview();
			}
		});
		
		final Button dumpBtn = (Button)findViewById(R.id.dump_button);
		dumpBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mbDumping = !mbDumping;
				dumpBtn.setText(mbDumping ? R.string.dumping_button : R.string.dump_button);
			}
		});
		
		View switchCamera = findViewById(R.id.switch_button);
		switchCamera.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switchCamera();
				
			}
		});
	}
    
    private void initFocusAdaptor()
    {
    	if(mFocusAdaptor == null)
    		mFocusAdaptor = new FocusAdaptor();
    	DisplayMetrics dm = getResources().getDisplayMetrics();
    	
    	mFocusAdaptor.init(mCamera, new Rect(0, 0, dm.widthPixels, dm.heightPixels), 
    			new FocusAdaptor.FocusUIAdaptor() {
			
			@Override
			public void startFocus() {
				mFocusView.startFocus();
				
			}
			
			@Override
			public void setLocation(Rect location) {
				RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)mFocusView.getLayoutParams();
				param.leftMargin = location.left;
				param.topMargin = location.top;
				param.width = location.width();
				param.height = location.height();
				param.getRules()[RelativeLayout.CENTER_IN_PARENT] = 0;
				mFocusView.setLayoutParams(param);
				mFocusView.requestLayout();
				
			}
			
			@Override
			public void setInvisibilityDelayed(int delayedTime) {
				mFocusView.setInvisibilityDelayed(delayedTime);
			}
			
			@Override
			public void focusSucceed() {
				mFocusView.focusSucceed();
			}
			
			@Override
			public void focusFailed() {
				mFocusView.focusFailed();
			}
		});
    }
    
    private void initAutoFocusCallback()
    {
    	 autoFocusCallback = new Camera.AutoFocusCallback() {
 			
 			@Override
 			public void onAutoFocus(boolean success, Camera camera) {
 				Log.d(TAG, "AutoFocus Callback ,focus success:"+success);
 				if(success)
 				{
 					mCamera.takePicture(mShutterCallback, rawPicCallback, postviewPicCallback, jpegPicCallback);
 				}
 			}
 		};
    }
    
    
    private void initShutterCallback()
	{
		mShutterCallback = new Camera.ShutterCallback() {

			@Override
			public void onShutter() {
				if (mTone == null)

					mTone = new ToneGenerator(
							android.media.AudioManager.STREAM_MUSIC,
							ToneGenerator.MAX_VOLUME);
				mTone.startTone(ToneGenerator.TONE_PROP_BEEP2);
			}
		};
	}
    
    private void initJpegPictureCallback()
    {
    	jpegPicCallback = new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				String path = Environment.getExternalStorageDirectory()+"/testcamera"+File.separator
						+"pic_"+String.valueOf(System.currentTimeMillis()+".jpg");
				File file = new File(path);
				FileOutputStream fos = null;
				try{
					if(!file.getParentFile().exists())
						file.getParentFile().mkdirs();
					fos = new FileOutputStream(file);
					fos.write(data);
					fos.flush();
					fos.close();
					
					ExifInterface exif = new ExifInterface(path);
					String exif_orient=exif.getAttribute(ExifInterface.TAG_ORIENTATION);
					Log.d(TAG, "exif_orient:"+exif_orient);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				finally{
					if(fos != null)
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
				
				mCamera.startPreview();
				
			}
		};
    }
    
    private void initRawPictureCallback()
    {
        rawPicCallback = new PictureCallback() {
 			@Override
 			public void onPictureTaken(byte[] data, Camera camera) {
 				
 			}
 		};
 		
    }
    
    private void initPostPictureCallback()
    {
 		postviewPicCallback = new PictureCallback() {
 			@Override
 			public void onPictureTaken(byte[] data, Camera camera) {
 				
 			}
 		};
    }
    private String getTimeStamp()
    {
    	 java.text.DateFormat  sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    	 return sdf.format(System.currentTimeMillis());
    }
    private static final String IMG = "IMG_";
    private static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +File.separator + "PreviewDump"+File.separator;
    private void initPreviewCallback()
    {
    	mPreviewCallback = new PreviewCallback() {
			
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				if(mbDumping)
				{
					File file  = new File(SAVE_PATH.concat(IMG).concat(getTimeStamp()).concat("_").concat(String.valueOf(mPreviewWidth))
							.concat("x").concat(String.valueOf(mPreviewHeight).concat(".nv21")));
					FileOutputStream fos = null;
					try{
						if(!file.getParentFile().exists())
							file.getParentFile().mkdirs();
						fos = new FileOutputStream(file);
						fos.write(data);
						fos.close();
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				
			}
		};
    }
    private boolean openCamera(int cameraId)
    {
    	if(mCamera != null)
    		return true;
    	mCamera = Camera.open(cameraId);
    	if(mCamera == null)
    		return false;
    	setCemareParameter();
    	initFocusAdaptor();
    	return true;
    }
    
    public boolean switchCamera() {
		int cameraNum = Camera.getNumberOfCameras();
		if (cameraNum == 1)
		{
			return false;
		}
		stopPreview();
		releaseCamera();
		
		mCameraId = (mCameraId + 1) % cameraNum;
	
		boolean res = openCamera(mCameraId);
		startPreview();
		return res;
	}
    
    private void setCemareParameter()
    {
    	if(mCamera == null) return;
    	Camera.Parameters cameraParam = mCamera.getParameters();
    	Size previewSize = getMaxSupportedPreviewSize(cameraParam);
    	cameraParam.setPreviewSize(previewSize.width, previewSize.height);
    	mPreviewWidth = previewSize.width;
    	mPreviewHeight = previewSize.height;
    	cameraParam.setPreviewFormat(ImageFormat.NV21);
//    	cameraParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);//some front face camera will crash if do this
		mCamera.setParameters(cameraParam);
		
		setCameraDisplayOrientation();
    }
    
    
    private void setCameraDisplayOrientation() {
    	if(mCamera == null)
    		return;
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(mCameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.d(TAG, "setCameraDisplayOrientation, degrees="+degrees+", set orientaion:"+result);
        mCamera.setDisplayOrientation(result);
        Camera.Parameters parameters = mCamera.getParameters();
        /**
         * Sets the clockwise rotation angle in degrees relative to the orientation of the camera. 
         * This affects the pictures returned from JPEG PictureCallback.
         * The camera driver may set orientation in the EXIF header without rotating the picture. 
         * Or the driver may rotate the picture and the EXIF thumbnail. If the Jpeg picture is rotated, 
         * the orientation in the EXIF header will be missing or 1 (row #0 is top and column #0 is left side). 
         */
        parameters.setRotation(result);
        mCamera.setParameters(parameters);
    }
    
    private void releaseCamera()
    {
    	if(mCamera != null)
    	{
    		mCamera.release();
    		mCamera = null;
    	}
    	mFocusAdaptor.uninit();
    }
    private void startPreview()
    {
    	if(mCamera != null)
    	{
        	try {
    			mCamera.setPreviewDisplay(holder);//Pass a fully initialized SurfaceHolder to setPreviewDisplay(SurfaceHolder).
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		mCamera.startPreview();
    		mCamera.setPreviewCallback(mPreviewCallback);
    	}
    }
    private void stopPreview()
    {
    	if(mCamera != null)
    		mCamera.stopPreview();
    }
    
    private static Size getMaxSupportedPreviewSize(Camera.Parameters parameters) {
		List<Size> picSizeList = parameters.getSupportedPreviewSizes();
		int index = 0;
		int picSize = 0;

		for (int i = 0; i < picSizeList.size(); ++i) {
			int size = picSizeList.get(i).width * picSizeList.get(i).height;
			if (size > picSize) {
				index = i;
				picSize = size;
			}
		}
		return picSizeList.get(index);
	}
	
   
}
