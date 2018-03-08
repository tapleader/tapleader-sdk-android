package com.tapleader;

import android.provider.BaseColumns;
import android.util.Log;

import com.tapleader.tapleadersdk.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
        private String sdkVersion;
        private String campaignId;
        private boolean callFromMain;


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

        public String getSdkVersion() {
            return sdkVersion;
        }

        public void setSdkVersion(String sdkVersion) {
            this.sdkVersion = sdkVersion;
        }

        public String getCampaignId() {
            return campaignId;
        }

        public void setCampaignId(String campaignId) {
            this.campaignId = campaignId;
        }

        public boolean isCallFromMain() {
            return callFromMain;
        }

        public void setCallFromMain(boolean callFromMain) {
            this.callFromMain = callFromMain;
        }

        public JSONObject getJson(){
            JSONObject object = new JSONObject();
            try {
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
                object.put("sdkVersion", BuildConfig.VERSION_CODE);
                object.put("campaignCode", getCampaignId());
                object.put("callFromMain",isCallFromMain());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return object;
        }

        public static class TInstallEntity implements BaseColumns{
            public static final String TABLE_NAME = "settings";
            public static final String COLUMN_NAME_ANDROID_ID="androidId";
            public static final String COLUMN_NAME_APP_ID="applicationId";
            public static final String COLUMN_NAME_CLIENT_KEY="clientKey";
            public static final String COLUMN_NAME_DEVICE_ID="deviceId";
            public static final String COLUMN_NAME_PCKG_NAME="packageName";
            public static final String COLUMN_NAME_PHONE_NAME="phoneModel";
            public static final String COLUMN_NAME_ANDROID_VERSION="version";
            public static final String COLUMN_NAME_APP_VERSION="appVersion";
            public static final String COLUMN_NAME_SIM_SERIAL="simSerialNumber";
            public static final String COLUMN_NAME_CARRIER_ONE="carrierName1";
            public static final String COLUMN_NAME_CARRIER_TWO="carrierName2";
            public static final String COLUMN_NAME_CALL_FROM_MAIN="callFromMain";
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

    static class TCrashReport{
        private String tag;
        private String message;
        private String deviceId;
        private String version;
        private String packageName;
        private String date;
        private String appVersion;
        private String sdkVersion;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getSdkVersion() {
            return sdkVersion;
        }

        public void setSdkVersion(String sdkVersion) {
            this.sdkVersion = sdkVersion;
        }

        public JSONObject getJson()  {
            JSONObject object = new JSONObject();
            try {
                object.put("tag",getTag());
                object.put("message", getMessage());
                object.put("deviceId", getDeviceId());
                object.put("version", getVersion());
                object.put("packageName", getPackageName());
                object.put("appVersion",getAppVersion());
                object.put("sdkVersion",getSdkVersion());
                object.put("date", TUtils.getDateTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }
    }

    static class TLifeCycleObject {
        private String name;
        private String startTime;
        private String endTime;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public JSONObject getJson() throws JSONException {
            JSONObject object = new JSONObject();
            object.put("name", getName());
            object.put("startTime", getStartTime());
            object.put("endTime", getEndTime());
            return object;
        }

        static class  TLifeCycleEntity implements BaseColumns{
            public static final String TABLE_NAME = "life_cycle";
            public static final String COLUMN_NAME_NAME="name";
            public static final String COLUMN_NAME_DATE="date";
            public static final String COLUMN_NAME_DURATION="duration";
            public static final String COLUMN_NAME_COUNT="count";

        }
    }

    static class TOfflineRecord{
        private final static String TAG="TOfflineRecord";
        private long id;
        private String body;
        private String path;
        private String date;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        private String getJson() {
            JSONObject object = new JSONObject();
            try {
                object.put("path", path);
                object.put("body", body);
                object.put("date", TUtils.getDateTime());
            } catch (JSONException e) {
                //
            }
            return object.toString();
        }

        @Override
        public String toString() {
            return "path: "+getPath()
                    +"body: "+getBody()
                    +"date: "+getDate();
        }

        /**
         * By implementing the BaseColumns interface,
         * your inner class can inherit a primary key field called _ID that some
         * Android classes such as cursor adaptors will expect it to have. It's not required,
         * but this can help your database work harmoniously with the Android framework.
         */
        static class TOfflineRecordEntity implements BaseColumns{
            public static final String TABLE_NAME = "offline_rec";
            public static final String COLUMN_NAME_PATH="path";
            public static final String COLUMN_NAME_BODY="body";
            public static final String COLUMN_NAME_DATE="date";

        }
    }

    static class RetentionObject{
        private String clientKey;
        private String packageName;
        private int launchCounter;
        private String deviceId;
        private String installationId;

        public String getInstallationId() {
            return installationId;
        }

        public void setInstallationId(String installationId) {
            this.installationId = installationId;
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

        public int getLaunchCounter() {
            return launchCounter;
        }

        public void setLaunchCounter(int launchCounter) {
            this.launchCounter = launchCounter;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }


        public JSONObject getJson(){
            JSONObject object = new JSONObject();
            try {
                object.put("clientKey", getClientKey());
                object.put("deviceId", getDeviceId());
                object.put("launchesCount", getLaunchCounter());
                object.put("packageName", getPackageName());
                object.put("InstallationId",getInstallationId());
            }catch (Exception e){
                e.printStackTrace();
            }
            return object;
        }
    }

    static class TEventObject {
        private String eventName;
        private double value;
        private HashMap<String, Double> details;


        public TEventObject(String name, double value) {
            this.eventName=name;
            this.value=value;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public HashMap<String, Double> getDetails() {
            return details;
        }


        public String getDetailsString(){
            String str=null;
            JSONArray array=new JSONArray();
            for(Map.Entry<String,Double> entry: details.entrySet()){
                JSONObject object=new JSONObject();
                try {
                    object.put(entry.getKey(),entry.getValue());
                } catch (JSONException e) {
                    TLog.e("TEventObject#getDetailsString",e);
                }
                array.put(object);
            }
            str=array.toString();
            return str;
        }

        public void setDetails(HashMap<String, Double> details) {
            this.details = details;
        }

        static TEventObject getModel(String name, double value, String detailsJsonArray){
            TEventObject object=new TEventObject(name,value);

            try {
                JSONArray details=new JSONArray(detailsJsonArray);
                HashMap<String,Double> map=new HashMap<>();
                for(int i=0;i<details.length();i++){
                    String nameKey=null;
                    String valueKey=null;
                    Iterator<String> keys= details.getJSONObject(i).keys();
                    try {
                        if(keys.hasNext())
                            nameKey = keys.next();
                        if(keys.hasNext())
                            valueKey = keys.next();
                    }catch (Exception e){
                        TLog.e("TEventObject#getModel#getKeys",e);
                    }
                    if(nameKey!=null)
                        map.put(nameKey,details.getJSONObject(i).getDouble(nameKey));
                }
                object.setDetails(map);
                return object;
            } catch (JSONException e) {
                TLog.e("TEventObject#getModel",e);
            }

            return null;
        }

        public JSONObject getJsonObject(){
            JSONObject object=new JSONObject();
            try {
                object.put("EventName", getEventName());
                object.put("Value", getValue());
                JSONArray array = new JSONArray();
                if(details!=null && details.size()>0) {
                    for (Map.Entry<String, Double> d : details.entrySet()) {
                        JSONObject detail = new JSONObject();
                        detail.put("Key", d.getKey());
                        detail.put("Value", d.getValue());
                        array.put(detail);
                    }
                    object.put("Details", array);
                }
                return object;
            }catch (Exception e){
                TLog.e("TEventObject#getJsonObject",e);
            }
            return null;
        }
        @Override
        public String toString() {
            return super.toString();
        }

        static class  TEventity implements BaseColumns{
            public static final String TABLE_NAME = "event";
            public static final String COLUMN_NAME_EVENT_NAME="name";
            public static final String COLUMN_NAME_EVENT_VALUE="value";
            public static final String COLUMN_NAME_DETAILS="details";

        }
    }
}
