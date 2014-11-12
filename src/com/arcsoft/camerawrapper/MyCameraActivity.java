package com.arcsoft.camerawrapper;

import com.example.testcamera.R;

import android.os.Bundle;
import android.view.KeyEvent;

public class MyCameraActivity extends CameraActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
