package com.tapleader;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tapleader.tapleadersdk.BuildConfig;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TUtils {
    private static final Object lock = new Object();
    private static final String TAG = "TUtils";
    private static String versionName = null;
    private static int versionCode = -1;

    /**
     * this method need android.permission.READ_PHONE_STATE permission
     *
     * @return return details about phone to identify
     * user that if new or old one!
     */
    static TModels.TInstallObject getClientDetails(Context context) {
        TModels.TInstallObject wObject = new TModels.TInstallObject();
        JSONObject result = null;
        boolean infoValidation = false;
        try {
            TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            wObject.setAndroidId(android.provider.Settings.Secure.getString(context.getContentResolver(), "android_id"));
            wObject.setApplicationId(TPlugins.get().getApplicationId());
            wObject.setClientKey(TPlugins.get().getClientKey());
            wObject.setDeviceId(tManager.getDeviceId());
            wObject.setPackageName(context.getPackageName());
            wObject.setPhoneModel(Build.MODEL);
            wObject.setVersion(android.os.Build.VERSION.RELEASE);
            wObject.setAppVersion(getVersionName(context));
            wObject.setSdkVersion(BuildConfig.VERSION_CODE+"");
            wObject.setCallFromMain(callFromMainActivity(context));
            wObject.setCampaignId(TPlugins.get().getCampaignId());
            wObject.setCarrierName2("Unknown");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
                ArrayList<SubscriptionInfo> list = (ArrayList<SubscriptionInfo>) subscriptionManager.getActiveSubscriptionInfoList();
                infoValidation = true;
                if(list==null)
                    infoValidation = false;
                else if (list.size() == 0)
                    infoValidation = false;
                wObject.setSimSerialNumber(tManager.getSimSerialNumber());
                wObject.setCarrierName(list.get(0).getCarrierName().toString());
                if (list!=null && list.size() > 1) {
                    wObject.setCarrierName2(list.get(1).getCarrierName().toString());
                }
            }
            if (!infoValidation) {
                wObject.setSimSerialNumber(tManager.getSimSerialNumber());
                wObject.setCarrierName(tManager.getNetworkOperatorName());
            }
        } catch (Exception e) {
            TLog.e(TAG, e);
        }
        return wObject;
    }

    static void registerLifecycleHandler(Context context) {
        if (context instanceof Application)
            ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(TLifeCycleHandler.getInstance(context));
        else
            TLog.e(TAG, new Exception("can't start LifeCycleHandler"));
    }

    static String getDateTime() {
        return dateParser(new Date(System.currentTimeMillis()));
    }

    static Date dateParser(String s) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            Date date = format.parse(s);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String dateParser(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return format.format(date);
    }

    static String getSimpleDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    static String wSec(final String s) {
        final String MD5 = "MD5";
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean checkForClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static int getVersionCode(Context context) {
        synchronized (lock) {
            if (versionCode == -1) {
                try {
                    versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    TLog.e(TAG,e);
                }
            }
        }

        return versionCode;
    }

    static Context getContext() {
        return Tapleader.getApplicationContext();
    }

    static PackageManager getPackageManager(Context context) {
        return context.getPackageManager();
    }


    /**
     * Returns the version name for this app, as specified by the android:versionName attribute in the
     * <manifest> element of the manifest.
     */
    static String getVersionName(Context context) {
        synchronized (lock) {
            if (versionName == null) {
                try {
                    versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    TLog.e(TAG,e);
                }
            }
        }

        return versionName;
    }

    static String getMainActivityName(Context context){
        String activityName="Unknown";
        try {
            Intent t=context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            activityName=t.getComponent().getClassName();
        }catch (Exception e){
            TLog.e(TAG,e);
        }
        return activityName;
    }

    static boolean callFromMainActivity(Context context){
        String MainActivity=getMainActivityName(context);
        StackTraceElement[] elements=Thread.currentThread().getStackTrace();
        for(StackTraceElement element:elements){
            if(element.getClassName().equals(MainActivity))
                return true;
        }
        return false;
    }

    static boolean checkServiceStatus(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TService.class.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
    static void startService(Context context,Class service){
        Intent startServiceIntent = new Intent(context, service);
        context.startService(startServiceIntent);
    }

    static void saveInstallData(String installationId,Context context){
        Log.d(TAG,"saveInstallData");
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.Preferences.INSTALL_PARAMETER_NAME, false);
        editor.putString(Constants.Preferences.PACKAGE_VERSION_NAME, TUtils.getVersionName(context));
        editor.putInt(Constants.Preferences.PACKAGE_VERSION_CODE, TUtils.getVersionCode(context));
        editor.putString(Constants.Preferences.USER_INSTALLATION_ID, installationId);
        editor.commit();
    }

    static String getInstallationId(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.Preferences.USER_INSTALLATION_ID,"Unknown");
    }

    static long getLastPushActivityLogTime(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(Constants.Preferences.LAST_ACTIVITY_LOG_PUSH_TIME,0l);
    }

    static int getLunchCounter(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(Constants.Preferences.LAUNCH_COUNTER,0);
    }

    static void updateLunchCounter(Context context, int count){
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.Preferences.LAUNCH_COUNTER,count);
        editor.apply();
    }

    static long getLastLaunchTime(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(Constants.Preferences.LAST_LAUNCH_TIME,0l);
    }

    static void updateLunchTime(Context context, long time){
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Constants.Preferences.LAST_LAUNCH_TIME,time);
        editor.apply();
    }

    static void updateLastPushActivityLogTime(Context context, long time){
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Constants.Preferences.LAST_ACTIVITY_LOG_PUSH_TIME,time);
        editor.apply();
    }

    static void saveUpdateData(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.Preferences.PACKAGE_VERSION_NAME, TUtils.getVersionName(context));
        editor.putInt(Constants.Preferences.PACKAGE_VERSION_CODE, TUtils.getVersionCode(context));
        editor.apply();
    }

    static boolean shouldNotifyInstall(Context context){
        synchronized (lock){
            SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
            boolean result=prefs.getBoolean(Constants.Preferences.INSTALL_PARAMETER_NAME, true);
            Log.d(TAG,"shouldNotifyInstall = "+result);
            return result;
        }
    }

    static boolean shouldNotifyUpdatePackage(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.Preferences.PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.getString(Constants.Preferences.PACKAGE_VERSION_NAME, "Unknown").equals(TUtils.getVersionName(context))
                || prefs.getInt(Constants.Preferences.PACKAGE_VERSION_CODE, -1) != TUtils.getVersionCode(context);
    }

}
