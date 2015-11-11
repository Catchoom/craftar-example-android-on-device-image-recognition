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
public class ExtendedRecognitionActivity extends CraftARActivity implements OnClickListener{

	private final static String TAG = "ExtendedRecognitionActivity";

	private final static String MY_ONDEVICE_COLLECTION_TOKEN = SplashScreenActivity.COLLECTION_TOKEN;
	private final static String MY_CLOUD_COLLECTION_TOKEN = "cloudrecognition";
	
	private View mScanningLayout;
	private View mTapToScanLayout;
		
	CraftAROnDeviceIR mOnDeviceIR; //The On-Device image recognition module
	CraftARCloudRecognition mCloudIR; //The Cloud Image recognition module
	CraftAROnDeviceCollectionManager mOnDeviceCollectionManager; //The On-Device collection manager, used to manage the on-device collections
	
	MyCloudSetCollectionListener mCloudSetCollectionListener; //The listener for the setCollection call of the CraftARCloudRecognition module
	MyOnDeviceSetCollectionListener mOnDeviceSetCollectionListener; //The listener for the setCollection call of the CraftAROnDeviceIR module
	MyOnDeviceAddCollectionListener mOnDeviceAddCollectionListener; //The listener for the addCollection call of the CraftAROnDeviceIR module
	
	//Obtains the pictures and frames of the camera, and the finder events. 
	//It will be used to forward the searches to the corresponding module.
	MySearchController mSearchController; 

	CloudResponseHandler mCloudResponseHandler; // Listener for the search() requests of the CraftARCloudRecognition module
	OnDeviceResponseHandler mOnDeviceResponseHandler; // Listener for the search() requests of the CraftAROnDeviceIR module
	
	CraftARSDK mCraftARSDK; //The CraftARSDK object.
	CraftARCamera mCamera; //Provides high-level access to some features of the device camera.
	
	private boolean mIsActivityRunning;
	
	boolean mOnDeviceReady = false; //Flag to control if the call to setCollection in the CraftAROnDeviceIR module has been completed successfully.
	boolean mCloudReady = false; //Flag to control if the call to setCollection in the CraftARCloudRecognition module has been completed successfully.
	
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
		
		onDeviceSetup(); //Setup the CraftAROnDeviceIR module
		cloudSetup(); //Setup the CraftARCloudRecognition module
		
		mSearchController = new MySearchController();
		
		//Set the SearchController in the SDK. By doing this, the SDK will forward the pictures, the frames, and the finder events to our SearchController.
		mCraftARSDK.setSearchController(mSearchController); 
		mCamera = mCraftARSDK.getCamera(); //Obtain the camera object from the SDK.
	}

	private void onDeviceSetup(){

		mOnDeviceReady = false;
		mOnDeviceIR = CraftAROnDeviceIR.Instance();	//Get the instance to the CraftAROnDeviceIR module		

		mOnDeviceResponseHandler = new OnDeviceResponseHandler(); //Create our SetCollectionListener for the CraftAROnDeviceIR module.
		mOnDeviceSetCollectionListener = new MyOnDeviceSetCollectionListener();  //Create our SetCollectionListener for the CraftAROnDeviceIR module.
		mOnDeviceAddCollectionListener = new MyOnDeviceAddCollectionListener();  //Create an AddCollectionListener for the CraftAROnDeviceIR module.
		
		mOnDeviceIR.setCraftARSearchResponseHandler(mOnDeviceResponseHandler ); //Tell the SDK that we want to receive the on-device search responses in the OnDeviceResponseHandler

		mOnDeviceCollectionManager = CraftAROnDeviceCollectionManager.Instance(); //Obtain the on-device collection manager.
		CraftAROnDeviceCollection collection = mOnDeviceCollectionManager.get(MY_ONDEVICE_COLLECTION_TOKEN); // Try to obtain the local collection for our token
		
		//Check if the collection could be obtained
		if(collection != null){ 
			//The collection was available in the device, use this collection in the CraftAROnDeviceIR module.
			mOnDeviceIR.setCollection(collection, mOnDeviceSetCollectionListener);
		}else{
			//The collection was not available in the device, add this collection.
			mOnDeviceCollectionManager.addCollectionWithToken(MY_ONDEVICE_COLLECTION_TOKEN, mOnDeviceAddCollectionListener);
		}
		
	}
	
	private void cloudSetup(){

		mCloudReady = false; 
		mCloudIR = CraftARCloudRecognition.Instance(); //Get the instance to the CraftARCloudRecognition module		
		mCloudResponseHandler = new CloudResponseHandler(); //Create our CraftARSearchResponseHandler for the CraftARCloudRecognition module.
		mCloudSetCollectionListener = new MyCloudSetCollectionListener(); //Create our SetCollectionListener for the CraftARCloudRecognition module.

		mCloudIR.setCraftARSearchResponseHandler(mCloudResponseHandler); //Tell the SDK that we want to receive the search responses from the Cloud in the CloudResponseHandler
		//Use the collection specified by the TOKEN in the CraftARCloudRecognition module. Receive the callbacks from the setCollection call in our CloudSetCollectionListener 
		mCloudIR.setCollection(MY_CLOUD_COLLECTION_TOKEN, mCloudSetCollectionListener); 
	}
	@Override
	public void onCameraOpenFailed() {
		//Error opening the camera
		Toast.makeText(getApplicationContext(), "Camera error", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPreviewStarted(int width, int height) {
		Log.d(TAG, "Camera preview started successfully");
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.tap_to_scan:
				mTapToScanLayout.setVisibility(View.GONE);
				mScanningLayout.setVisibility(View.VISIBLE);
				mCraftARSDK.singleShotSearch(); //Request a picture to the SDK. Since we are implementing our custom SearchController, this will produce a callback to the method
				// onPictureTaken(CraftARQueryImage queryImage) of our SearchController.
				break;
		}
	}
	
	private void showNoObjectsDialog(){
		if(!mIsActivityRunning){
			return;
		}
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("No objects found");
		dialogBuilder.setMessage("Point to an object of the collection " +MY_ONDEVICE_COLLECTION_TOKEN + " or " + MY_CLOUD_COLLECTION_TOKEN);
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
	
	private void onSearchFailed(CraftARError error){
		mScanningLayout.setVisibility(View.GONE);
		mTapToScanLayout.setVisibility(View.VISIBLE);		
		Log.e(TAG, "Search failed("+error.getErrorCode()+"):"+error.getErrorMessage());
		//Some error occurred. We just show a toast with the error
		mCamera.restartCapture();	
	}

	class OnDeviceResponseHandler implements CraftARSearchResponseHandler{

		@Override
		public void searchResults(ArrayList<CraftARResult> results,
				long searchTimeMillis, int requestCode) {
			//Callback with the search results
			if(results.size()> 0){
				//We found something! Show the results
				showResultDialog(results);
			}else{
				if(mCloudReady){
					Toast.makeText(getApplicationContext(), "Nothing found locally,  searching on cloud...", Toast.LENGTH_SHORT).show();
					//No objects found locally for this request, search it on the cloud!
					CraftARQueryImage lastQueryImage = mSearchController.getLastQueryImage();
					if(lastQueryImage != null){
						mCloudIR.search(lastQueryImage);
					}
				}else{
					Toast.makeText(getApplicationContext(), "Cloud not ready", Toast.LENGTH_SHORT).show();
					showNoObjectsDialog();
				}
			}			
		}

		@Override
		public void searchFailed(CraftARError error, int requestCode) {
			onSearchFailed(error);				
		}
		
	}
	
	class CloudResponseHandler implements CraftARSearchResponseHandler{

		@Override
		public void searchResults(ArrayList<CraftARResult> results,
				long searchTimeMillis, int requestCode) {
			if(results.size()> 0){
				//We found something! Show the results
				showResultDialog(results);
			}else{
				//No objects found for this request
				showNoObjectsDialog();
			}	
		}

		@Override
		public void searchFailed(CraftARError error, int requestCode) {
			onSearchFailed(error);
		}
		
	}
	
	class MySearchController implements SearchController{

		CraftARQueryImage mLastQuery;
		boolean mIsFinding = false;
		public CraftARQueryImage getLastQueryImage(){
			return mLastQuery;
		}
		
		@Override
		public void onPictureTaken(CraftARQueryImage image) {
			mLastQuery = image;
			if(mOnDeviceReady){
				mOnDeviceIR.search(image);
			}
		}

		@Override
		public void onTakePictureFailed(CraftARError error) {
			mOnDeviceResponseHandler.searchFailed(error, 0);
		}

		@Override
		public void onPreviewFrame(CraftARQueryImage image) {
			if(mIsFinding){
				//TODO: If you want to use Finder mode, implement your logic here!
				//Example: Search this frame only when the previous request was finished.
				if(mOnDeviceIR.getPendingSearchRequestsCount() == 0 ){
					mOnDeviceIR.search(image);
				}
			}
		}

		@Override
		public void onFinderActivated() {
			mIsFinding = true;
		}

		@Override
		public void onFinderDeactivated() {
			mIsFinding = false;
		}
		
	}
	
	class MyOnDeviceSetCollectionListener implements SetOnDeviceCollectionListener{

		@Override
		public void collectionReady() {
			Log.d(TAG, "OnDevice collection is ready!");
			mOnDeviceReady = true;
			mScanningLayout.setVisibility(View.GONE);
			mTapToScanLayout.setVisibility(View.VISIBLE);
		}

		@Override
		public void setCollectionFailed(CraftARError error) {
			Toast.makeText(getApplicationContext(), "Error setting on-device collection:"+error.getErrorMessage(), Toast.LENGTH_SHORT).show();			
		}

		@Override
		public void setCollectionProgress(double progress) {
			Log.d(TAG,"Progress loading on-device collection:"+progress);
		}
		
	}
	
	class MyCloudSetCollectionListener implements SetCollectionListener{

		@Override
		public void collectionReady() {
			Log.d(TAG, "Cloud collection is ready!");
			mCloudReady = true;
		}

		@Override
		public void setCollectionFailed(CraftARError error) {
			Toast.makeText(getApplicationContext(), "Error setting cloud collection:"+error.getErrorMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	/** Listener used when adding an on-device collection in the device. */
	class MyOnDeviceAddCollectionListener implements AddCollectionListener{

		@Override
		public void collectionAdded(CraftAROnDeviceCollection collection) {
			mOnDeviceIR.setCollection(collection, mOnDeviceSetCollectionListener);	
		}

		@Override
		public void addCollectionFailed(CraftARError error) {
			Toast.makeText(getApplicationContext(), "Error adding on-device collection:"+error.getErrorMessage(), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void addCollectionProgress(float progress) {
			Log.d(TAG,"Progress adding on-device collection:"+progress);
			
		}
		
	}
	

}
