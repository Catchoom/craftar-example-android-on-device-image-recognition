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
import android.view.View.OnClickListener;

import com.craftar.CraftARActivity;
import com.craftar.CraftARCamera;
import com.craftar.CraftARError;
import com.craftar.CraftAROnDeviceIR;
import com.craftar.CraftARResult;
import com.craftar.CraftARSDK;
import com.craftar.CraftARSearchResponseHandler;

public class RecognitionSingleShotActivity extends CraftARActivity implements CraftARSearchResponseHandler, OnClickListener {

	private final static String TAG = "RecognitionSingleShotActivity";

	private View mScanningLayout;
	private View mTapToScanLayout;
		
	CraftAROnDeviceIR mOnDeviceIR;
	CraftARSDK mCraftARSDK;
	CraftARCamera mCamera;
	private boolean mIsActivityRunning;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onPostCreate() {

		setContentView(R.layout.activity_recognition_only);
		
		 //Obtain an instance of the CraftARSDK (which manages the camera interaction).
        //Note we already called CraftARSDK.init() in the Splash Screen, so we don't have to do it again
		mCraftARSDK = CraftARSDK.Instance();
		mCraftARSDK.startCapture(this);
		
		//Get the instance to the OnDeviceIR singleton (it has already been initialized in the SplashScreenActivity, and the collections are already loaded).
		mOnDeviceIR = CraftAROnDeviceIR.Instance();	
		
		//Tell the SDK that the OnDeviceIR who manage the calls to singleShotSearch() and startFinding().
		//In this case, as we are using on-device-image-recognition, we will tell the SDK that the OnDeviceIR singleton will manage this calls.
		mCraftARSDK.setSearchController(mOnDeviceIR.getSearchController());
		
		//Tell the SDK that we want to receive the search responses in this class.
		mOnDeviceIR.setCraftARSearchResponseHandler(this);
		
		//Obtain the reference to the camera, to be able to restart the camera, trigger focus etc.
		//Note that if you use single-shot, you will always have to obtain the reference to the camera to restart it after you take the snapshot.
		mCamera = mCraftARSDK.getCamera();
		
		mScanningLayout = findViewById(R.id.layout_scanning);
		mTapToScanLayout = findViewById(R.id.tap_to_scan);
		mTapToScanLayout.setClickable(true);
		mTapToScanLayout.setOnClickListener(this);	
	}

	@Override
	public void onCameraOpenFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPreviewStarted(int width, int height) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onClick(View v) {
		if (v == mTapToScanLayout) {
			mTapToScanLayout.setVisibility(View.GONE);
			mScanningLayout.setVisibility(View.VISIBLE);
			mCraftARSDK.singleShotSearch();
		}
	}

	@Override
	public void searchResults(ArrayList<CraftARResult> result,
			long searchTimeMillis, int requestCode) {
		//Callback with the search results
		if(result.size()> 0){
			//We found something! Show the results
			showResultDialog(result);
		}else{
			//No objects found for this request
			showNoObjectsDialog();
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
	        	showTapToScan();
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
			//Get the name of the item matched: 
			//Note that you can retrieve here many other fields (URL, Custom field, bounding boxes, etc)
			String itemName = result.getItem().getItemName();
			resultsText+= itemName + "\n";
		}
		resultsText = resultsText.substring(0,resultsText.length() - 1); //Eliminate the last \n
	
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Search results:");
		dialogBuilder.setMessage(resultsText);
		dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	showTapToScan();
	        }
	     });
	
		dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dialogBuilder.show();
	}
	
	@Override
	public void searchFailed(CraftARError error, int requestCode) {
		mScanningLayout.setVisibility(View.GONE);
		mTapToScanLayout.setVisibility(View.VISIBLE);		
		Log.e(TAG, "Search failed("+error.getErrorCode()+"):"+error.getErrorMessage());
		//Some error occurred. We just show a toast with the error
		mCamera.restartCapture();		
	}
	
	private void showTapToScan(){
		mCamera.restartCapture();		
		mScanningLayout.setVisibility(View.GONE);
		mTapToScanLayout.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStop(){
		super.onStop();
		mIsActivityRunning = false;
		
	}
	@Override
	protected void onStart(){
		super.onStart();
		mIsActivityRunning = true;
		
	}

	

}
