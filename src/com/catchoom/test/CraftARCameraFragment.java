package com.catchoom.test;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.catchoom.test.R;
import com.craftar.CraftARError;
import com.craftar.CraftAROnDeviceIR;
import com.craftar.CraftARResult;
import com.craftar.CraftARSDK;
import com.craftar.CraftARSearchResponseHandler;

public class CraftARCameraFragment extends Fragment  implements CraftARSearchResponseHandler{

	private final static String TAG = "RecognitionFinderActivity";	

	ScreenSlideActivity mParentActivity;
	CraftAROnDeviceIR mOnDeviceIR;
	CraftARSDK mCraftARSDK;
	View mScanningLayout;
	long startFinderTimeMillis;
	boolean mIsCraftARInitialized = false;
	boolean mIsFragmentVisible = false;
	private final static long FINDER_SESSION_TIME_MILLIS= 10000;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_camera, container, false);
        mScanningLayout = rootView.findViewById(R.id.layout_scanning);
        mParentActivity = (ScreenSlideActivity) getActivity();
        
        //Obtain an instance of the CraftARSDK (which manages the camera interaction).
        //Note we already called CraftARSDK.init() in the Splash Screen, so we don't have to do it again
		mCraftARSDK = CraftARSDK.Instance(); 
		mCraftARSDK.startCapture(mParentActivity);
		
		//Get the instance to the OnDeviceIR singleton (it has already been initialized in the SplashScreenActivity, and the collectoins are already loaded).
		mOnDeviceIR = CraftAROnDeviceIR.Instance();	
		
		//Tell the SDK that the OnDeviceIR who manage the calls to singleShotSearch() and startFinding().
		//In this case, as we are using on-device-image-recognition, we will tell the SDK that the OnDeviceIR singleton will manage this calls.
		mCraftARSDK.setSearchController(mOnDeviceIR.getSearchController());
		
		//Tell the SDK that we want to receive the search responses in this class.
		mOnDeviceIR.setCraftARSearchResponseHandler(this);

		mIsCraftARInitialized = true;

		startFinding();

		return rootView;
    }
    
	@SuppressLint("NewApi")
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
			super.setUserVisibleHint(isVisibleToUser);
		}
		mIsFragmentVisible = isVisibleToUser;
		
		if(mIsCraftARInitialized){
			if(isVisibleToUser){
	        	startFinding();
			}else{
	        	stopFinding();
			}
		}
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
			//We have a timeout for the finder session (so the phone is not finding indefinitely, but stops after some time).
			long ellapsedTime = System.currentTimeMillis() - startFinderTimeMillis;
			if(ellapsedTime > FINDER_SESSION_TIME_MILLIS ){
				stopFinding();
				//No object were found during this session
				showNoObjectsDialog();
			}
		}
	}

	private void showNoObjectsDialog(){
		if(!mIsFragmentVisible){
			return;
		}
		boolean isActivityFinishing = getActivity().isFinishing();
		if(!isActivityFinishing){		
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
			dialogBuilder.setTitle("No objects found");
			dialogBuilder.setMessage("Point to an object of the "+SplashScreenActivity.COLLECTION_TOKEN+" collection");
			dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	if(mIsFragmentVisible){
		        		startFinding();
		        	}
		        }
		     });
		
			dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
			dialogBuilder.show();  
		}
	}
	
	private void showResultDialog(ArrayList<CraftARResult> results){
		//Show the results only if the fragment is visible and the activity is running
		if(!mIsFragmentVisible){
			return;
		}
		//Create a string with all the results
		String resultsText="";
		for(CraftARResult result:results){
			String itemName = result.getItem().getItemName();
			resultsText+= itemName + "\n";
		}
		resultsText = resultsText.substring(0,resultsText.length() - 1); //Eliminate the last \n
	
		//Show a dialog with the result 
		
		if((getActivity()!= null ) &&(! getActivity().isFinishing())){//Ensure that the activity is active.
			
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
			dialogBuilder.setTitle("Search results:");
			dialogBuilder.setMessage(resultsText);
			dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	if(mIsFragmentVisible){
		        		startFinding();
		        	}
		        }
		     });
		
			dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
			dialogBuilder.show();
		}
	}

	private void startFinding(){
		mScanningLayout.setVisibility(View.VISIBLE);
		mCraftARSDK.startFinder(); //Start finder in the CraftARSDK.
		startFinderTimeMillis= System.currentTimeMillis();
	}
	
	private void stopFinding(){
		mCraftARSDK.stopFinder(); //Stop the finder in the CraftARSDK.
		mScanningLayout.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void searchFailed(CraftARError error, int requestCode) {
		Log.e(TAG, "Search failed("+error.getErrorCode()+"):"+error.getErrorMessage());
	}	
	
	@Override
	public void onPause() {
	    super.onPause();
	    stopFinding();
	}
	
	@Override
	public void onStop() {
	    super.onPause();
	    stopFinding();
	}
	@Override
	public void onResume() {
	    super.onPause();
	    if(mIsFragmentVisible){
	    	startFinding();
	    }
	}
	
	public void onPreviewStarted(int width, int height){
		//We get the event from the CraftARActivity
	}
	
	public void onCameraOpenFailed(){
		//We get the event from the CraftARActivity
	}
 
}
