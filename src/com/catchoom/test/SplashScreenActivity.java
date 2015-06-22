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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.catchoom.test.R;
import com.craftar.CraftARCollection;
import com.craftar.CraftARCollectionManager;
import com.craftar.CraftARCollectionManager.AddCollectionListener;
import com.craftar.CraftARError;
import com.craftar.CraftAROnDeviceIR;
import com.craftar.ImageRecognition.SetCollectionListener;

public class SplashScreenActivity extends Activity implements SetCollectionListener,
AddCollectionListener{

	private final static String TAG = "SplashScreenActivity";	

	//Collection token of the collection you want to load.
	//Note that you can load several collections at once, but every search 
	//request is performed only on ONE collection (the one that you have set through CraftAROnDeviceIR.setCollection()).
	public final static String COLLECTION_TOKEN="catchoomcooldemo";

	CraftAROnDeviceIR mCraftAROnDeviceIR;
	CraftARCollectionManager mCollectionManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash_screen);
			
		//Initialize the Collection Manager
		mCollectionManager = CraftARCollectionManager.Instance(getApplicationContext()); 
		
		//Initialize the Offline IR Module
		mCraftAROnDeviceIR = CraftAROnDeviceIR.Instance(getApplicationContext());
			
				
		//Obtain the collection with your token.
		//This will lookup for the collection in the internal storage, and return the collection if it's available.
		CraftARCollection col =  mCollectionManager.get(COLLECTION_TOKEN); 
		if(col == null){
			//Collection is not available. Add it from assets using the collection bundle.
			mCollectionManager.addCollection((AddCollectionListener)this,"catchoomcooldemoBundle.zip");
		}else{
			//Collection is already available in the device.
			loadCollection(col);
		}
    }

	@Override
	public void collectionReady() {
		//Collection is ready for recognition.
		Intent launchersActivity = new Intent( SplashScreenActivity.this, LaunchersActivity.class);
		startActivity(launchersActivity);
		finish();
	}

	@Override
	public void setCollectionFailed(CraftARError error) {
		//Error loading the collection into memory. No recognition can be performed unless a collection has been set.
		Log.e(TAG,"SetCollectionFailed ("+error.getErrorCode()+"):"+error.getErrorMessage());
		Toast.makeText(getApplicationContext(), "Error loading", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void setCollectionProgress(double progress) {
		//The images from the collection are loading into memory. You will have to load the collections into memory every time you open the app. 
		Log.d(TAG,"SetCollectionProgress:"+progress);
	}


	@Override
	public void collectionAdded(CraftARCollection collection) {
		//Collection bundle has been added. Set this collection as current collection.
		loadCollection(collection);
	}
	
	@Override
	public void addCollectionFailed(CraftARError error) {
		//Error adding the bundle to the device internal storage. 
		Log.e(TAG,"AddCollectionFailed("+error.getErrorCode()+"):"+error.getErrorMessage());
		Toast.makeText(getApplicationContext(), "Error adding collection", Toast.LENGTH_SHORT).show();
		switch(error.getErrorCode()){
		case COLLECTION_BUNDLE_SDK_VERSION_IS_OLD:
			//You are trying to add a bundle which version is newer than the SDK version. 
			//You should either update the SDK, or download and add a bundle compatible with this SDK version.
			break;
		case COLLECTION_BUNDLE_VERSION_IS_OLD:
			//You are trying to add a bundle which is outdated, since the SDK version is newer than the bundleSDK 
			//You should download a bundle compatible with the newer SDK version.
			break;
		default:
			break;
		}
	}

	@Override
	public void addCollectionProgress(float progress) {
		//Progress adding the collection to internal storage (de-compressing bundle and storing into the device storage).
		//Note that this might only happen once per app installation, or when the bundle is updated.
		Log.d(TAG,"AddCollectionProgress:"+progress);
	}
	
	private void loadCollection(CraftARCollection collection){
		mCraftAROnDeviceIR.setCollection((SetCollectionListener)this, collection);
	}

}
