package com.arcsoft.camerawrapper;

import java.io.File;
import java.io.IOException;

import com.example.testcamera.R;

import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;

public class MyCameraActivity extends CameraActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ExifInterface exif;
		try {
			exif = new ExifInterface(Environment.getExternalStorageDirectory()+File.separator+"IMG_20150127171126_1.jpg");
			String width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
			String height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
			System.out.println("_GetFileInfoByName3 get W&H from exif, w="+width+",h="+height); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	  @Override
	    protected void onDestroy() {
	    	System.out.println("Activity2:onDestroy");
	    	super.onDestroy();
	    }
	    
	    @Override
	    protected void onStart() {
	    	System.out.println("Activity2:onStart");
	    	super.onStart();
	    }
	    
	    @Override
	    protected void onPause() {
	    	System.out.println("Activity2:onPause");
	    	super.onPause();
	    }
	    
	    @Override
	    protected void onRestart() {
	    	System.out.println("Activity2:onRestart");
	    	super.onRestart();
	    }
	    
	    @Override
	    protected void onResume() {
	    	System.out.println("Activity2:onResume");
	    	super.onResume();
	    }
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			System.out.println("-->Activity2:keyback,finish");
			setResult(RESULT_OK);
			finish();
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected int getLayoutID() {
		return R.layout.activity_camera;
	}

	@Override
	protected int getRelativeLayoutIDForFocusView() {
		return R.id.main_layout_id;
	}
}
