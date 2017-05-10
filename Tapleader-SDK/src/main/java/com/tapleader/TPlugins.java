package com.tapleader;

import android.content.Context;

import java.io.File;

/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TPlugins {

    private static final String INSTALLATION_ID_LOCATION = "installationId";

    private static final Object LOCK = new Object();
    private static TPlugins instance;
    final Object lock = new Object();
    private final String applicationId;
    private final String clientKey;
    private final String deviceId;
    private final String campaignId;
    protected File tapleaderDir;
    protected File cacheDir;
    protected File filesDir;
    private Context context;
    private TPlugins(Context context,String applicationId, String clientKey, String deviceId, String campaignId) {
        this.context=context;
        this.applicationId = applicationId;
        this.clientKey = clientKey;
        this.deviceId = deviceId;
        this.campaignId = campaignId;
    }

    // TODO(grantland): Move towards a Config/Builder parameter pattern to allow other configurations
    // such as path (disabled for Android), etc.
    static void initialize(Context context,String applicationId, String clientKey, String deviceId, String campaignId) {
        TPlugins.set(new TPlugins(context,applicationId, clientKey, deviceId, campaignId));
    }

    static void set(TPlugins plugins) {
        synchronized (LOCK) {
            if (instance == null) {
                instance = plugins;
            }
        }
    }

    static TPlugins get() {
        synchronized (LOCK) {
            return instance;
        }
    }


    Context applicationContext(){
        return context;
    }

    static void refresh(Context context){
        synchronized (LOCK){
            if(instance==null){
                TSQLHelper helper=new TSQLHelper(context);
                String appId=helper.getSetting(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_APP_ID);
                String clnKey=helper.getSetting(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CLIENT_KEY);
                String devId=helper.getSetting(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_DEVICE_ID);
                String cmpId=ManifestInfo.getApplicationMetadata(context).getString("com.tapleader.CAMPAIGN_ID");
                initialize(context,appId,clnKey,devId,cmpId!=null ? cmpId : "Unknown");
            }
        }
    }
    static void reset() {
        synchronized (LOCK) {
            instance = null;
        }
    }

    private static File createFileDir(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return file;
            }
        }
        return file;
    }

    String getApplicationId() {
        return applicationId;
    }

    String getClientKey() {
        return clientKey;
    }

    String getDeviceId() {
        return deviceId;
    }

    String getCampaignId() {
        return campaignId;
    }

    @SuppressWarnings("deprecation")
    File getTapleaderDir() {
        synchronized (lock) {
            if (tapleaderDir == null) {
                tapleaderDir = context.getDir("tapleader", Context.MODE_PRIVATE);
            }
            return createFileDir(tapleaderDir);
        }
    }

    File getCacheDir() {
        synchronized (lock) {
            if (cacheDir == null) {
                cacheDir = new File(context.getCacheDir(), "com.tapleader");
            }
            return createFileDir(cacheDir);
        }
    }

    File getFilesDir() {
        synchronized (lock) {
            if (filesDir == null) {
                filesDir = new File(context.getFilesDir(), "com.tapleader");
            }
            return createFileDir(filesDir);
        }
    }
}
