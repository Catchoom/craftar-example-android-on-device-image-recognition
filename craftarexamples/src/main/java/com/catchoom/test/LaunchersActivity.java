// com.craftar.craftatexamplesir is free software. You may use it under the MIT license, which is copied
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.catchoom.advancedexamples.ExtendedRecognitionActivity;

public class LaunchersActivity extends Activity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_launchers);

		// Setup example links
		findViewById(R.id.play_finder).setOnClickListener(this);
		findViewById(R.id.play_recognition_only).setOnClickListener(this);
		findViewById(R.id.play_fragment_finder).setOnClickListener(this);
		findViewById(R.id.play_extended_recognition).setOnClickListener(this);

		// Setup bottom Links
		findViewById(R.id.imageButton_logo).setOnClickListener(this);
		findViewById(R.id.button_signUp).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
				
		// Clicked on play links
		Intent intent = null;
		switch(v.getId()){
		case R.id.play_finder:
			intent = new Intent(this, RecognitionFinderActivity.class);
			break;
		case R.id.play_recognition_only:
			intent = new Intent(this, RecognitionSingleShotActivity.class);
			break;
		case R.id.play_fragment_finder:
			intent = new Intent(this, ScreenSlideActivity.class);
			break;
		case R.id.play_extended_recognition:
			intent = new Intent(this, ExtendedRecognitionActivity.class);
			break;
		case R.id.imageButton_logo:
			intent = new Intent(this, WebActivity.class);
			intent.putExtra(WebActivity.WEB_ACTIVITY_URL, "http://catchoom.com/product/?utm_source=CraftARExamplesApp&amp;utm_medium=Android&amp;utm_campaign=HelpWithAPI");
			break;
		case R.id.button_signUp:
			intent = new Intent(this, WebActivity.class);
			intent.putExtra(WebActivity.WEB_ACTIVITY_URL, "https://my.craftar.net/try-free?utm_source=CraftARExamplesApp&amp;utm_medium=Android&amp;utm_campaign=HelpWithAPI");
			break;
		}
		
		if (intent != null) {
			startActivity(intent);
			return;
		}
	}

}
