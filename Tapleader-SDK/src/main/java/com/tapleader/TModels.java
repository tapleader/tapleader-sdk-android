package com.tapleader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mehdi akbarian on 2017-04-08.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TModels {
    static class TInstallObject {
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
        private String campaignId;

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

        public String getCampaignId() {
            return campaignId;
        }

        public void setCampaignId(String campaignId) {
            this.campaignId = campaignId;
        }

        public JSONObject getJson() throws JSONException {
            JSONObject object = new JSONObject();
            object.put("androidId", getAndroidId());
            object.put("applicationId", getApplicationId());
            object.put("clientKey", getClientKey());
            object.put("deviceId", getDeviceId());
            object.put("packageName", getPackageName());
            object.put("phoneModel", getPhoneModel());
            object.put("version", getVersion());
            object.put("simSerialNumber", getSimSerialNumber());
            object.put("carrierName", getCarrierName());
            object.put("carrierName2", getCarrierName2());
            object.put("appVersion", getAppVersion());
            object.put("campaignId", getCampaignId());
            return object;
        }
    }

    static class TUpdatePackageObject {
        private String applicationId;
        private String clientKey;
        private String packageName;
        private String appVersionName;
        private int appVersionCode;

        public TUpdatePackageObject(String appVersionName, int appVersionCode, String packageName, String applicationId, String clientKey) {
            this.appVersionName = appVersionName;
            this.appVersionCode = appVersionCode;
            this.packageName = packageName;
            this.applicationId = applicationId;
            this.clientKey = clientKey;
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
            JSONObject object = new JSONObject();
            object.put("getApplicationId", getApplicationId());
            object.put("getClientKey", getClientKey());
            object.put("packageName", getPackageName());
            object.put("appVersionName", getAppVersionName());
            object.put("appVersionCode", getAppVersionCode());

            return object;
        }
    }

    static class TAccountsData {

        private JSONArray accounts;
        private String applicationId;
        private String clientKey;

        public TAccountsData(JSONArray accounts, String applicationId, String clientKey) {
            this.applicationId = applicationId;
            this.clientKey = clientKey;
            this.accounts = accounts;
        }

        public JSONArray getAccounts() {
            return accounts;
        }

        public void setAccounts(JSONArray accounts) {
            this.accounts = accounts;
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

        public JSONObject getJson() throws JSONException {
            JSONObject object = new JSONObject();
            object.put("getApplicationId", getApplicationId());
            object.put("getClientKey", getClientKey());
            object.put("accounts", accounts);
            return object;
        }

    }

    static class TResponse{
        private int status;
        private String message;
        private int installationId;

        public TResponse(int status, String message,int installationId) {
            this.status = status;
            this.message = message;
            this.installationId=installationId;
        }

        public TResponse(JSONObject object){
            try {
                this.status=object.getInt("Status");
                this.message=object.getString("Message");
                if(object.length()>2)
                    this.installationId=object.getInt("InstallationId");
            }catch (Exception e){

            }
        }
    }
}