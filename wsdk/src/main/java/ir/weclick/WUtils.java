package ir.weclick;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;



import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Time;

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
        private String appVersion;

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
            object.put("applicationId",getApplicationId());
            object.put("clientKey",getClientKey());
            object.put("deviceId",getDeviceId());
            object.put("packageName",getPackageName());
            object.put("phoneModel",getPhoneModel());
            object.put("version",getVersion());
            object.put("simSerialNumber",getSimSerialNumber());
            object.put("carrierName",getCarrierName());
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
        try{
            TelephonyManager tManager = (TelephonyManager)Weclick.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            wObject.setAndroidId(android.provider.Settings.Secure.getString(Weclick.getApplicationContext().getContentResolver(), "android_id"));
            wObject.setApplicationId(WPlugins.get().applicationId());
            wObject.setClientKey(WPlugins.get().clientKey());
            wObject.setDeviceId(tManager.getDeviceId());
            wObject.setPackageName(Weclick.getApplicationContext().getPackageName());
            wObject.setPhoneModel(Build.MODEL);
            wObject.setVersion(android.os.Build.VERSION.RELEASE);
            wObject.setSimSerialNumber(tManager.getSimSerialNumber());
            wObject.setCarrierName(tManager.getNetworkOperatorName());
            wObject.setAppVersion(BuildConfig.VERSION_NAME);
            result=wObject.getJson();
        }catch (Exception e){
            WLog.e(TAG,e.getMessage());
        }
        return result.toString();
    }
/*

    */
/**
     * conver object to string
     * @return
     *//*

    static String toJson(Object model){
        JSONObject object=new JSONObject(model);
        return object.toString();
    }
*/

    static void registerLifecycleHandler(Context context){
        if(context instanceof Application)
            ((Application)context.getApplicationContext()).registerActivityLifecycleCallbacks(WLifeCycleHandler.getInstance());
        else
            WLog.e("WUtils","cant start Activity Cycle Handler");
    }

    static String getDateTime(){
        String date = new Date(System.currentTimeMillis()).toString();
        String time = new Time(System.currentTimeMillis()).toString();
        return date+" "+time;
    }

}
