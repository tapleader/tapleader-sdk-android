package ir.weclick;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;

import java.sql.Date;
import java.sql.Time;

/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class WUtils {
    private static Gson gson=new Gson();
    static class WInstallObject {
        private String ApplicationId;
        private String clientId;
        private String androidId;
        private String deviceId;
        private String phoneModel;
        private String version;
        private String carrierName;
        private String simSerialNumber;

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

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
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

        public String getPhoneModle() {
            return phoneModel;
        }

        public void setPhoneModle(String phoneModle) {
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
    }

    /**
     * this method need android.permission.READ_PHONE_STATE permission
     * @return return details about phone to identify
     * user that if new or old one!
     */
    static String getClientDetails(){
        WInstallObject wObject=new WInstallObject();
        try{
            TelephonyManager tManager = (TelephonyManager)Weclick.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            wObject.setAndroidId(android.provider.Settings.Secure.getString(Weclick.getApplicationContext().getContentResolver(), "android_id"));
            wObject.setDeviceId(tManager.getDeviceId());
            wObject.setPhoneModle(Build.MODEL);
            wObject.setVersion(android.os.Build.VERSION.RELEASE);
            wObject.setSimSerialNumber(tManager.getSimSerialNumber());
            wObject.setCarrierName(tManager.getNetworkOperatorName());
        }catch (Exception e){
            e.printStackTrace();
        }
        return toJson(wObject);
    }

    /**
     * conver object to string
     * @param model
     * @return
     */
    static String toJson(Object model){
        return gson.toJson(model);
    }

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
