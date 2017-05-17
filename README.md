# Installing [Tapleader](http://tapleader.com) Android SDK 

[![](https://jitpack.io/v/tapleader/tapleader-sdk-android.svg)](https://jitpack.io/#tapleader/tapleader-sdk-android) [![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=15)

The following tutorial will guide you to Install Tapleader SDK via *jar* file and gradle.

Before install, [create a tapleader account](http://tapleader.com/account/register).

## Installing

### Method 1: Install *jar* file

* Download latest version of sdk *jar* file from [release page](https://github.com/tapleader/tapleader-sdk-android/releases):
* Move this file to */libs* folder of your project.
* Add this following lines to your **app level module** `buil.gradle` file dependencies:
```gradle
dependencies {
    compile files('libs/tapleader.jar')
    //...
}
```
### Method 2: Install with `build.gradle`:

* Add it in your root `build.gradle` at the end of repositories:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
* Add the dependency to your app module `build.gradle`
```gradle
dependencies {
        compile 'com.github.tapleader:tapleader-sdk-android:v1.2.0'
	//...
}
```

## Configuration

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
 Campaign_Code is optional. Usually you shouldn't provide campaign code in manifest file. Just in case you want to [Tracking Without Campaign Link](https://tapleader.com/docs/no-link-tracking)
* Add following lines for permissions(before *Application* scope):

```xml
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

## Initializing the SDK


If your app covering device with API 23 and above you should check permissions. after permissions granted initialize Tapleader SDK:

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
