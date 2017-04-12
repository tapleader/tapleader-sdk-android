# [Tapleader](http://tapleader.com) SDK 

[![](https://jitpack.io/v/tapleader/tapleader-sdk-android.svg)](https://jitpack.io/#tapleader/tapleader-sdk-android)



## Free and unlimited app analytics

[Tapleader Analytics](http://tapleader.com) provides free, unlimited reporting on up to some installs per day. Tapleader is a powerful in-app analytics platform that allows you to monitor the performance of your app install sources in real time, determining the performance of each marketing channel from a single interface. Never install another tracking SDK again.

The following tutorial will guide you to Install Tapleader SDK via *jar* file.

## Getting Started

* First of all download latest version of sdk *jar* file from [release](https://github.com/tapleader/tapleader-sdk-android/releases) tab:

```gradle
dependencies {
    compile files('libs/tapleader.jar')
    //...
}
```
* Move this file to */libs* folder of your project.
* Add this following lines to your **app level module** `buil.gradle` file dependencies:

* Or get Tapleader into your build:
Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
Then add the dependency to your app module build.gradle
```gradle
dependencies {
        compile 'com.github.tapleader:tapleader-sdk-android:V1.0'
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
            android:name="com.tapleader.CAMPAIGN_CODE"
            android:value="YOUR_CAMPAIGN_CODE" />
 ```
* Add following lines for permissions(before *Application* scope):

```xml
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

## Initializing the SDK


If your app covering device with API 23 and above you should check permissions. after permissons granted initialize Weclick SDK:

```java

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 99;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        //if permission doesn't granted you should request permission
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }
        //or permission granted and you should initialize Tapleader here
        else {
            Tapleader.initialize(getApplicationContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //after requesting permission next step is initializing SDK
                    Tapleader.initialize(getApplicationContext());
                }
                break;
            default:
                break;
        }
    }
}
```

But if your target API is below 23 just add this line in your starting method ( `onCreate`  ) of Activity:

```java
    Tapleader.initialize(getApplicationContext());

```
