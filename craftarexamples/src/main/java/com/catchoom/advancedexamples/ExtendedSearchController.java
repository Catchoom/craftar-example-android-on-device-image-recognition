package com.catchoom.advancedexamples;

import java.util.ArrayList;

import android.util.Log;

import com.craftar.CraftARCloudRecognition;
import com.craftar.CraftARError;
import com.craftar.CraftAROnDeviceIR;
import com.craftar.CraftARQueryImage;
import com.craftar.CraftARResult;
import com.craftar.CraftARSearchResponseHandler;
import com.craftar.SearchController;

public class ExtendedSearchController implements SearchController{

	private final static String TAG = "ExtendedSearchController";
	CraftAROnDeviceIR mOnDeviceIR;
	CraftARCloudRecognition mCloudRecognition;
	CraftARSearchResponseHandler mResponseHandler;
	
	public ExtendedSearchController(CraftARSearchResponseHandler extendedSearchResponseHandler){
		mOnDeviceIR= CraftAROnDeviceIR.Instance();
		mCloudRecognition = CraftARCloudRecognition.Instance();
		mResponseHandler = extendedSearchResponseHandler;
		mOnDeviceIR.setCraftARSearchResponseHandler(new OnDeviceResponseHandler());
		mCloudRecognition.setCraftARSearchResponseHandler(new CloudResponseHandler());
	}

	CraftARQueryImage mLastQuery;
	boolean mIsFinding = false;
	
	@Override
	public void onPictureTaken(CraftARQueryImage image) {
		mLastQuery = image;
		mOnDeviceIR.search(image);
	}

	@Override
	public void onTakePictureFailed(CraftARError error) {
		mLastQuery = null;
		
		//Error taking the snapshot. 
		//Note that the requestCode is missing in this error, as we actually not performed any image recognition request.
		mResponseHandler.searchFailed(error, -1);
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
	

	class OnDeviceResponseHandler implements CraftARSearchResponseHandler{

		@Override
		public void searchResults(ArrayList<CraftARResult> results,	long searchTimeMillis, int requestCode) {
	
			//Callback with the search results
			if(results.size()> 0){
				//Send back the results to the responseHandler
				mResponseHandler.searchResults(results, searchTimeMillis, requestCode);
			}else{
				//Perform a search with the 
				if(mLastQuery != null){
					Log.d(TAG,"Nothing found locally, searching on the cloud...");
					mCloudRecognition.search(mLastQuery);
				}
			}			
		}

		@Override
		public void searchFailed(CraftARError error, int requestCode) {
			mResponseHandler.searchFailed(error, requestCode);				
		}
		
	}
	
	class CloudResponseHandler implements CraftARSearchResponseHandler{

		@Override
		public void searchResults(ArrayList<CraftARResult> results,
				long searchTimeMillis, int requestCode) {
			mResponseHandler.searchResults(results, searchTimeMillis, requestCode);
	
		}

		@Override
		public void searchFailed(CraftARError error, int requestCode) {
			mResponseHandler.searchFailed(error, requestCode);				
		}
		
	}
	
}
