package ir.weclick;

import android.app.Application;
import android.content.Context;
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

import ir.weclick.weclicksdk.BuildConfig;


/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class WUtils {
    private static final String TAG="WUtils";

    /**
     * this method need android.permission.READ_PHONE_STATE permission
     * @return return details about phone to identify
     * user that if new or old one!
     */
    static String getClientDetails(){
        WModels.WInstallObject wObject=new WModels.WInstallObject();
        JSONObject result=null;
        boolean infoValidation=false;
        try{
            TelephonyManager tManager = (TelephonyManager)Weclick.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            wObject.setAndroidId(android.provider.Settings.Secure.getString(Weclick.getApplicationContext().getContentResolver(), "android_id"));
            wObject.setApplicationId(WPlugins.get().getApplicationId());
            wObject.setClientKey(WPlugins.get().getClientKey());
            wObject.setDeviceId(tManager.getDeviceId());
            wObject.setPackageName(Weclick.getApplicationContext().getPackageName());
            wObject.setPhoneModel(Build.MODEL);
            wObject.setVersion(android.os.Build.VERSION.RELEASE);
            wObject.setAppVersion(BuildConfig.VERSION_NAME);
            wObject.setCarrierName2("Unknown");
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
                SubscriptionManager subscriptionManager=SubscriptionManager.from(Weclick.getApplicationContext());
                ArrayList<SubscriptionInfo> list= (ArrayList<SubscriptionInfo>) subscriptionManager.getActiveSubscriptionInfoList();
                infoValidation=true;
                if(list.size()==0)
                    infoValidation=false;
                wObject.setSimSerialNumber(tManager.getSimSerialNumber());
                wObject.setCarrierName(list.get(0).getCarrierName().toString());
                if(list.size()>1){
                    wObject.setCarrierName2(list.get(1).getCarrierName().toString());
                }
            }
            if(!infoValidation) {
                wObject.setSimSerialNumber(tManager.getSimSerialNumber());
                wObject.setCarrierName(tManager.getNetworkOperatorName());
            }

            result=wObject.getJson();
        }catch (Exception e){
            WLog.e(TAG,e.getMessage());
        }
        return result.toString();
    }

    static void registerLifecycleHandler(Context context){
        if(context instanceof Application)
            ((Application)context.getApplicationContext()).registerActivityLifecycleCallbacks(WLifeCycleHandler.getInstance());
        else
            WLog.e(TAG,"can't start LifeCycleHandler");
    }

    static String getDateTime(){
        return dateParser(new Date(System.currentTimeMillis()));
    }

    static Date dateParser(String s){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            Date date = format.parse(s);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String dateParser(Date date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return format.format(date);
    }

    static String getSimpleDate(Date date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    static boolean checkPermission(Context context,String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
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
}
