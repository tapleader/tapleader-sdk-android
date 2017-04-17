package com.tapleader;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

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
    static String getClientDetails() {
        TModels.TInstallObject wObject = new TModels.TInstallObject();
        JSONObject result = null;
        boolean infoValidation = false;
        try {
            TelephonyManager tManager = (TelephonyManager) Tapleader.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            wObject.setAndroidId(android.provider.Settings.Secure.getString(Tapleader.getApplicationContext().getContentResolver(), "android_id"));
            wObject.setApplicationId(TPlugins.get().getApplicationId());
            wObject.setClientKey(TPlugins.get().getClientKey());
            wObject.setDeviceId(tManager.getDeviceId());
            wObject.setPackageName(Tapleader.getApplicationContext().getPackageName());
            wObject.setPhoneModel(Build.MODEL);
            wObject.setVersion(android.os.Build.VERSION.RELEASE);
            wObject.setAppVersion(getVersionName());
            wObject.setCampaignId(TPlugins.get().getCampaignId());
            wObject.setCarrierName2("Unknown");
            wObject.setCallFromMain(callFromMainActivity());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subscriptionManager = SubscriptionManager.from(Tapleader.getApplicationContext());
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

            result = wObject.getJson();
        } catch (Exception e) {
            TLog.e(TAG, e.getMessage());
        }
        return result.toString();
    }

    static void registerLifecycleHandler(Context context) {
        if (context instanceof Application)
            ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(TLifeCycleHandler.getInstance());
        else
            TLog.e(TAG, "can't start LifeCycleHandler");
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

    static int getVersionCode() {
        synchronized (lock) {
            if (versionCode == -1) {
                try {
                    versionCode = getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    TLog.e(TAG, "Couldn't find info about own package", e);
                }
            }
        }

        return versionCode;
    }

    static Context getContext() {
        return Tapleader.getApplicationContext();
    }

    static PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }


    /**
     * Returns the version name for this app, as specified by the android:versionName attribute in the
     * <manifest> element of the manifest.
     */
    static String getVersionName() {
        synchronized (lock) {
            if (versionName == null) {
                try {
                    versionName = getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    TLog.e(TAG, "Couldn't find info about own package", e);
                }
            }
        }

        return versionName;
    }

    static String getMainActivityName(){
        Intent t=getPackageManager().getLaunchIntentForPackage(getContext().getPackageName());
        return t.getComponent().getClassName();
    }

    static boolean callFromMainActivity(){
        String MainActivity=getMainActivityName();
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

}
