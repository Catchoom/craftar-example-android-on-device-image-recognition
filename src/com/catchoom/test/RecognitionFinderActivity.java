// com.craftar.craftarexamplesir is free software. You may use it under the MIT license, which is copied
// below and available at http://opensource.org/licenses/MIT
//
// Copyright (c) 2014 Catchoom Technologies S.L.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of
// this software and associated documentation files (the "Software"), to deal in
// the Software without restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
// Software, and to permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
// PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
// FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

package com.catchoom.test;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.catchoom.test.R;
import com.craftar.CraftARActivity;
import com.craftar.CraftARError;
import com.craftar.CraftAROnDeviceIR;
import com.craftar.CraftARResult;
import com.craftar.CraftARSDK;
import com.craftar.CraftARSearchResponseHandler;

public class RecognitionFinderActivity extends CraftARActivity implements CraftARSearchResponseHandler{

	private final static String TAG = "RecognitionFinderActivity";	

	CraftAROnDeviceIR mOnDeviceIR;
	CraftARSDK mCraftARSDK;
	View mScanningLayout;
	long startFinderTimeMillis;
	private final static long FINDER_SESSION_TIME_MILLIS= 10000;
	boolean mIsActivityRunning = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onPostCreate(){

		setContentView(R.layout.activity_recognition_finder);
			 
		//Obtain an instance of the CraftARSDK (which manages the camera interaction).
        //Note we already called CraftARSDK.init() in the Splash Screen, so we don't have to do it again
		mCraftARSDK = CraftARSDK.Instance();
		mCraftARSDK.startCapture(this);
		
		//Get the instance to the OnDeviceIR singleton (it has already been initialized in the SplashScreenActivity, and the collectoins are already loaded).
		mOnDeviceIR = CraftAROnDeviceIR.Instance();	
		
		//Tell the SDK that the OnDeviceIR who manage the calls to singleShotSearch() and startFinding().
		//In this case, as we are using on-device-image-recognition, we will tell the SDK that the OnDeviceIR singleton will manage this calls.
		mCraftARSDK.setSearchController(mOnDeviceIR.getSearchController());
		
		//Tell the SDK that we want to receive the search responses in this class.
		mOnDeviceIR.setCraftARSearchResponseHandler(this);
		
		mScanningLayout = findViewById(R.id.layout_scanning);
	
		startFinding();
	}
	
	@Override
	public void onCameraOpenFailed() {
		Toast.makeText(getApplicationContext(), "Camera error", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPreviewStarted(int width, int height) {
		Log.d(TAG, "Preview started with width:"+width+", height:"+height);
	}

	@Override
	public void searchResults(ArrayList<CraftARResult> results,
			long searchTimeMillis, int requestCode) {
		//Callback with the search results
		
		if(results.size() > 0){
			//We found something! Show the results
			stopFinding();
			showResultDialog(results);	
		}else{
			long ellapsedTime = System.currentTimeMillis() - startFinderTimeMillis;
			if(ellapsedTime > FINDER_SESSION_TIME_MILLIS ){
				stopFinding();
				//No object were found during this session
				showNoObjectsDialog();
			}
		}
	}

	private void showNoObjectsDialog(){
		if(!mIsActivityRunning){
			return;
		}
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("No objects found");
		dialogBuilder.setMessage("Point to an object of the "+SplashScreenActivity.COLLECTION_TOKEN+" collection");
		dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	startFinding();
	        }
	     });
	
		dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dialogBuilder.show();  
	}
	
	private void showResultDialog(ArrayList<CraftARResult> results){
		if(!mIsActivityRunning){
			return;
		}
		
		String resultsText="";
		for(CraftARResult result:results){
			String itemName = result.getItem().getItemName();
			resultsText+= itemName + "\n";
		}
		resultsText = resultsText.substring(0,resultsText.length() - 1); //Eliminate the last \n
	
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Search results:");
		dialogBuilder.setMessage(resultsText);
		dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	startFinding();
	        }
	     });
	
		dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dialogBuilder.show();
	}
	

	private void startFinding(){
		mScanningLayout.setVisibility(View.VISIBLE);
		mCraftARSDK.startFinder();
		startFinderTimeMillis= System.currentTimeMillis();
	}
	
	private void stopFinding(){
		mCraftARSDK.stopFinder();
		mScanningLayout.setVisibility(View.INVISIBLE);
	}
	
	
	@Override
	public void searchFailed(CraftARError error, int requestCode) {
		Log.e(TAG, "Search failed("+error.getErrorCode()+"):"+error.getErrorMessage());
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		mIsActivityRunning = false;
	}
	
	@Override
	public void onPause(){
		super.onPause();
		stopFinding();
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		mIsActivityRunning = true;
	}

	
}
