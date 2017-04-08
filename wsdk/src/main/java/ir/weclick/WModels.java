package ir.weclick;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mehdi akbarian on 2017-04-08.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class WModels {
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
    static class WUpdatePackageObject{
        private String applicationId;
        private String clientKey;
        private String packageName;
        private String appVersionName;
        private int appVersionCode;

        public WUpdatePackageObject(String appVersionName, int appVersionCode) {
            this.appVersionName = appVersionName;
            this.appVersionCode = appVersionCode;
            this.packageName=Weclick.getApplicationContext().getPackageName();
            this.applicationId=WPlugins.get().getApplicationId();
            this.clientKey=WPlugins.get().getClientKey();
        }

        public int getAppVersionCode() {
            return appVersionCode;
        }

        public void setAppVersionCode(int appVersionCode) {
            this.appVersionCode = appVersionCode;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }

        public String getClientKey() {
            return clientKey;
        }

        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getAppVersionName() {
            return appVersionName;
        }

        public void setAppVersionName(String appVersion) {
            this.appVersionName = appVersion;
        }

        public JSONObject getJson() throws JSONException {
            JSONObject  object = new JSONObject();
            object.put("getApplicationId",getApplicationId());
            object.put("getClientKey",getClientKey());
            object.put("packageName",getPackageName());
            object.put("appVersionName",getAppVersionName());
            object.put("appVersionCode",getAppVersionCode());

            return object;
        }
    }
}
