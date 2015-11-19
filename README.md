## CraftAR - Android On-Device Image Recognition SDK examples

### Introduction

The CraftAR Service for [Augmented Reality and Image Recognition](http://catchoom.com/product/craftar/augmented-reality-and-image-recognition/) is a  service that allows you to build a wide range of __Image Recognition__ and __Augmented Reality__ applications
and services.

With CraftAR, you can create amazing apps that provide digital content
for real-life objects like printed media, packaging among others. You
can use our online web panel or APIs, to upload images to be recognized and set
content to display upon recognition in your CraftAR-powered app.

The [Android On-Device Image Recognition SDK](http://catchoom.com/documentation/on-device-image-recognition-sdk/android-on-device-image-recognition-sdk/) focuses on opening the camera in the mobile device and performing
Image Recognition requests on the device. The display of the result of the request
of each is up to you.

This document describes mainly the Examples of different uses of the Service and the On Device Image Recognition SDK.
General use of the SDK can be found in the Documentation webpage for the [Android On-Device Image Recognition SDK](http://catchoom.com/documentation/on-device-image-recognition-sdk/android-on-device-image-recognition-sdk/). Complete SDK documentation of the classes can be found within the distribution of the SDK itself.

### How to use the examples

This repository comes with an Eclipse project of an Android app with several
examples that show how to use the SDK. The project can easily be imported to Android Studio.

Eclipse:
 1. Import this project in Eclipse.
 2. Download and uncompress the [CraftAR On-Device Image Recognition SDK for Android](http://catchoom.com/product/craftar/augmented-reality-and-image-recognition-sdk/#download-mobile-sdk).
 3. Copy the libs/ and assets/ folders from the SDK in your application root directory.

Android Studio:
 1. Import this project in Android Studio (Import Non-Android Studio project)
 2. Download and uncompress the [CraftAR On-Device Image Recognition SDK for Android](http://catchoom.com/product/craftar/augmented-reality-and-image-recognition-sdk/#download-mobile-sdk).
 3. In Android studio, select the view 'Project Files' in the project explorer.
 4. Create a folder 'libs' in the root of the project. Copy all the .jars included in the libs folder of our SDK there.
 5. Create a folder jniLibs in the src/main folder. Copy the folders armeabi-v7a and x86 there.
 6. Copy the assets/ folder in your application root directory.
 7. If you have import errors with the CraftAR classes, right click on each of the jars in libs/ folder and click on 'Add as library'
 8. Note that the project importer might have not imported some files from the source project (for example, the example reference images). Locate them and copy them in the project. 

* Note: If your app uses android-support libraries, remove the android-support-v13.jar from the libs folder, and use your own one instead.

### Reference images

The project includes a folder 'ReferenceImages' with sample images using the Open Demos collection with the 'imagerecognition' token.

Samples images for on-device:

![Business card](https://github.com/Catchoom/craftar-example-android-on-device-image-recognition/blob/master/Reference%20images/biz_card.jpe)

![Cart](https://github.com/Catchoom/craftar-example-android-on-device-image-recognition/blob/master/Reference%20images/Image%20Recognition.jpg)

Examples for the extended search:

![Kid](https://github.com/Catchoom/craftar-example-android-on-device-image-recognition/blob/master/Reference%20images/kid_with_mobile.jpe)

![CraftAR Logo](https://github.com/Catchoom/craftar-example-android-on-device-image-recognition/blob/master/Reference%20images/craftar_logo.jpe)


