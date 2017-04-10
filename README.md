# [Tapleader](http://tapleader.com) SDK 

## Free and unlimited app analytics

[Tapleader Analytics](http://tapleader.com) provides free, unlimited reporting on up to some installs per day. Tapleader is a powerful in-app analytics platform that allows you to monitor the performance of your app install sources in real time, determining the performance of each marketing channel from a single interface. Never install another tracking SDK again.

The following tutorial will guide you to Install Tapleader SDK via *jar* file.

## Getting Started

* First of all download latest version of sdk *jar* file from [release](https://github.com/tapleader/tapleader-sdk-android/releases) tab.
* Move this file to */libs* folder of your project.
* Add this following lines to your **app level module** `buil.gradle` file dependencies:

```gradle
dependencies {
    compile files('libs/tapleader.jar')
    //...
}
```
* Now you should add your `Client Key` and `Application Id` to the `AndroidManifest.xml` file in *Application* scope:	
```xml
 <meta-data
            android:name="com.tapleader.APPLICATION_ID"
            android:value="YOUR_APPLICATION_IP"/>
  <meta-data
            android:name="com.tapleader.CLIENT_KEY"
            android:value="YOUR_CLIENT_KEY" />
	<meta-data
            android:name="com.tapleader.CAMPAIGN_ID"
            android:value="YOUR_CAMPAIGN_CODE" />
 ```
* Add following lines for permissions(before *Application* scope):

```xml
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.READ_PHONE_STATE" />
```
