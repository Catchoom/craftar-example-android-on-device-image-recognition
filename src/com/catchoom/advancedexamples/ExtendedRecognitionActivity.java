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

package com.catchoom.advancedexamples;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.catchoom.test.R;
import com.catchoom.test.SplashScreenActivity;
import com.craftar.CraftARActivity;
import com.craftar.CraftARCamera;
import com.craftar.CraftARCloudRecognition;
import com.craftar.CraftARError;
import com.craftar.CraftAROnDeviceCollection;
import com.craftar.CraftAROnDeviceCollectionManager;
import com.craftar.CraftAROnDeviceCollectionManager.AddCollectionListener;
import com.craftar.CraftAROnDeviceIR;
import com.craftar.CraftARQueryImage;
import com.craftar.CraftARResult;
import com.craftar.CraftARSDK;
import com.craftar.CraftARSearchResponseHandler;
import com.craftar.ImageRecognition.SetCollectionListener;
import com.craftar.ImageRecognition.SetOnDeviceCollectionListener;
import com.craftar.SearchController;

/**
 * This example shows how to perform extended image recognition using the single-shot mode, 
 * using on-device image recognition + cloud image recognition.
 * 
 * The example will load an on-device collection and search always first in the on-device collection. 
 * If nothing is found in the on-device collection, and there's connectivity, it will search it in
 * the cloud collection, which is supposed to have a different content than the on-device collection, so
 * we expect to find a match there.
 * 
 * Extended image recognition is useful if you want to pre-fetch some images into the application (in an on-device collection),
 * because they're more likely to be scanned, so you skip searching into the cloud for all those requests. Note that the size of
 * on-device collection affects the size of the app, but the size of the cloud collection don't.
 * 
 * 
 * How to use:
 * 
 * You can find the Reference images in the Reference Images folder of this project:
 * 
 * 		The on-device collection contains the images biz_card and shopping_kart. 
 * 		The cloud collection in addition contains the images kid_with_mobile and craftar_logo.
 * 
 * So, if you point to the image biz_card, it will be recognized using the on-device module. If you point to another image, a search
 * in the cloud will be performed. In the case you were pointing to the kid_with_mobile or to the craftar_logo images, the search in the cloud
 * will find the match.
 * **/
public class ExtendedRecognitionActivity extends CraftARActivity implements OnClickListener, CraftARSearchResponseHandler{

	private final static String TAG = "ExtendedRecognitionActivity";

	private final static String MY_ONDEVICE_COLLECTION_TOKEN = SplashScreenActivity.COLLECTION_TOKEN;
	private final static String MY_CLOUD_COLLECTION_TOKEN = "cloudrecognition";
	
	private View mScanningLayout;
	private View mTapToScanLayout;
		
	CraftARSDK mCraftARSDK; //The CraftARSDK object.
	CraftARCamera mCamera; //Provides high-level access to some features of the device camera.
	
	private boolean mIsActivityRunning;
		
	@Override
	public void onPostCreate() {

		setContentView(R.layout.activity_recognition_only);
		mScanningLayout = findViewById(R.id.layout_scanning);
		mTapToScanLayout = findViewById(R.id.tap_to_scan);
		mTapToScanLayout.setOnClickListener(this);
		mTapToScanLayout.setVisibility(View.GONE);

		 //Obtain an instance of the CraftARSDK (which manages the camera interaction).
		mCraftARSDK = CraftARSDK.Instance();
		mCraftARSDK.init(getApplicationContext()); //Initialize always the SDK before doing any other operation. If the SDK has already been initialized, this is a no-op.
		mCraftARSDK.startCapture((CraftARActivity)this); //Starts the camera capture.
		
		CraftARCloudRecognition cloudIR = CraftARCloudRecognition.Instance(); //Get the instance to the CraftARCloudRecognition module		
		//Use the collection specified by the TOKEN in the CraftARCloudRecognition module. Receive the callbacks from the setCollection call in our CloudSetCollectionListener 
		cloudIR.setCollection(MY_CLOUD_COLLECTION_TOKEN, new SetCollectionListener() {
			@Override
			public void collectionReady() {
				Log.d(TAG, "Cloud collection is ready!");
				mScanningLayout.setVisibility(View.GONE);
				mTapToScanLayout.setVisibility(View.VISIBLE);
			}

			@Override
			public void setCollectionFailed(CraftARError error) {
				Toast.makeText(getApplicationContext(), "Error setting cloud collection:"+error.getErrorMessage(), Toast.LENGTH_SHORT).show();
			}
		}); 		
		
		ExtendedSearchController extendedSearchController = new ExtendedSearchController((CraftARSearchResponseHandler)this);
		
		//Set the SearchController in the SDK. By doing this, the SDK will forward the pictures, the frames, and the finder events to our SearchController.
		mCraftARSDK.setSearchController(extendedSearchController); 
		mCamera = mCraftARSDK.getCamera(); //Obtain the camera object from the SDK.
	}
	
	@Override
	public void onCameraOpenFailed() {
		Toast.makeText(getApplicationContext(), "Camera error", Toast.LENGTH_SHORT).show();		
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
