/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.catchoom.test;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.catchoom.test.R;
import com.craftar.CraftARActivity;


/**
 * Demonstrates a "screen-slide" animation using a {@link ViewPager}. Because {@link ViewPager}
 * automatically plays such an animation when calling {@link ViewPager#setCurrentItem(int)}, there
 * isn't any animation-specific code in this sample.
 */
public class ScreenSlideActivity extends CraftARActivity{
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    public static final String TAG =  "CraftarFragmentSlider";
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    
    
    private CraftARCameraFragment cameraFragment;
    private EmptyFragment emptyFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
	public void onPostCreate() {
        setContentView(R.layout.activity_screen_slide);
		cameraFragment= new CraftARCameraFragment();
		emptyFragment= new EmptyFragment();
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }


    /**
     * A simple pager adapter that represents 5 {@link CraftARCameraFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
            case 0:
            	return cameraFragment;
            case 1:
            	return emptyFragment;
            default:
            	return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


	@Override
	public void onCameraOpenFailed() {
		cameraFragment.onCameraOpenFailed();
	}

	@Override
	public void onPreviewStarted(int width, int height) {
		cameraFragment.onPreviewStarted(width,height);
	}
  
}
