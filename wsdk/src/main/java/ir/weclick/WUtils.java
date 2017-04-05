package ir.weclick;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import org.json.JSONException;
import org.json.JSONObject;

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
    static class WInstallObject {
        private String ApplicationId;
        private String clientKey;
        private String packageName;
        private String androidId;
        private String deviceId;
        private String phoneModel;
        private String version;
        private String carrierName;
        private String simSerialNumber;
        private String carrierName2;
        private String appVersion;

        public String getCarrierName2() {
            return carrierName2;
        }

        public void setCarrierName2(String carrierName2) {
            this.carrierName2 = carrierName2;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getCarrierName() {
            return carrierName;
        }

        public void setCarrierName(String carrierName) {
            this.carrierName = carrierName;
        }

        public String getApplicationId() {
            return ApplicationId;
        }

        public void setApplicationId(String applicationId) {
            ApplicationId = applicationId;
        }

        public String getClientKey() {
            return clientKey;
        }

        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }

        public String getAndroidId() {
            return androidId;
        }

        public void setAndroidId(String androidId) {
            this.androidId = androidId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getPhoneModel() {
            return phoneModel;
        }

        public void setPhoneModel(String phoneModle) {
            this.phoneModel = phoneModle;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSimSerialNumber() {
            return simSerialNumber;
        }

        public void setSimSerialNumber(String simSerialNumber) {
            this.simSerialNumber = simSerialNumber;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public JSONObject getJson() throws JSONException {
            JSONObject  object = new JSONObject();
            object.put("androidId",getAndroidId());
            object.put("getApplicationId",getApplicationId());
            object.put("getClientKey",getClientKey());
            object.put("deviceId",getDeviceId());
            object.put("packageName",getPackageName());
            object.put("phoneModel",getPhoneModel());
            object.put("version",getVersion());
            object.put("simSerialNumber",getSimSerialNumber());
            object.put("carrierName",getCarrierName());
            object.put("carrierName2",getCarrierName2());
            object.put("appVersion",getAppVersion());

            return object;
        }
    }

    /**
     * this method need android.permission.READ_PHONE_STATE permission
     * @return return details about phone to identify
     * user that if new or old one!
     */
    static String getClientDetails(){
        WInstallObject wObject=new WInstallObject();
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
}
