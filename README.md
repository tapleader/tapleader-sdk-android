# Installing [Tapleader](http://tapleader.com) Android SDK 

[![](https://jitpack.io/v/tapleader/tapleader-sdk-android.svg)](https://jitpack.io/#tapleader/tapleader-sdk-android) [![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=15)

The following tutorial will guide you to Install Tapleader SDK via gradle.

Before install, [create a tapleader account](http://tapleader.com/account/register).

## Installing

### Install with `build.gradle`:

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
        compile 'com.github.tapleader:tapleader-sdk-android:v1.3.3'
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
            android:name="com.tapleader.CAMPAIGN_ID"
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

Initial Tapleader in your Application class:

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Tapleader.initialize(this);
    }
}
```
Don't forget to register your App class in AndroidManifest.xml :

```xml
<application
        android:name=".App"
        <!-- other attributes -->
        >
```


Tapleader need Some Safe permission to track user information, If your app covering device with API 23 and above you should grant permissions:

```java

public class ManiActivity extends AppCompatActivity {

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
    }
}
```

