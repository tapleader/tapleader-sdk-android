package com.tapleader;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

public class Tapleader {
    private static final String TAPLEADER_APPLICATION_ID = "com.tapleader.APPLICATION_ID";
    private static final String TAPLEADER_CAMPAIGN_ID = "com.tapleader.CAMPAIGN_ID";
    private static final String TAPLEADER_CLIENT_KEY = "com.tapleader.CLIENT_KEY";
    private static final Object MUTEX = new Object();
    private static final String TAG = "Tapleader";
    private static ServiceHandler serviceHandler;
    private static TLock lock = new TLock();
    private static Account[] mAccounts;

    /**
     * read Application_ID and Client_ID from metadata in Manifest.xml file
     *
     * @param context
     */
    public static void initialize(Context context) {
        initialize(context, false);
    }

    /**
     * read Application_ID,Client_ID nd CAMPAIGN_ID from metadata in Manifest.xml file
     * get user Accounts data
     *
     * @param context
     */
    public static void initialize(Context context, boolean dangerousAccess) {
        Configuration.Builder builder = new Configuration.Builder(context, dangerousAccess);
        if (builder.applicationId == null) {
            throw new RuntimeException(Constants.Exception.APPLICATIONID_NOT_FOUND);
        }
        if (builder.clientKey == null) {
            throw new RuntimeException(Constants.Exception.CLIENTKEY_NOT_FOUND);
        }
        initialize(builder.build());
    }

    public static void initialize(Context context, String applicationId, String clientKey) {
        initialize(context, applicationId, clientKey, "");
    }

    public static void initialize(Context context, String applicationId, String clientKey, Boolean dangerousAccess) {
        initialize(context, applicationId, clientKey, "", dangerousAccess);
    }

    public static void initialize(Context context, String applicationId, String clientKey, String campaignId) {
        initialize(context, applicationId, clientKey, campaignId, false);
    }

    public static void initialize(Context context, String applicationId, String clientKey, String campaignId, boolean dangerousAccess) {
        initialize(new Configuration.Builder(context, dangerousAccess)
                .applicationId(applicationId)
                .clientKey(clientKey)
                .campaignId(campaignId)
                .build()
        );
    }

    synchronized static void initialize(Configuration configuration) {
        TLog.setLogLevel(Log.VERBOSE);
        if (lock.isLocked())
            return;
        lock.lock();
        if (!TUtils.callFromApplication(configuration.context)) {
            lock.unlock();
            throw new RuntimeException("Tapleader should be initialized in Application class!");
        }
        String deviceId = "PERMISSION_NOT_GRANTED";
        if (ManifestInfo.hasGrantedPermissions(configuration.context, Constants.Permission.READ_PHONE_STATE))
            deviceId = ((TelephonyManager) configuration.context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        TPlugins.initialize(configuration.context, configuration.applicationId, configuration.clientKey, deviceId, configuration.campaignId);
        TUtils.registerLifecycleHandler(configuration.context);
        initializeTBroadcastReceiver(configuration.context);
        serviceHandler = ServiceHandler.init(configuration.context);
        TKeyValueCache.initialize(configuration.context);
        checkDbData(configuration.context, TUtils.getClientDetails(configuration.context));
        checkCacheApplicationId();
        checkForNewInstallOrUpdate(configuration.dangerousAccess);
        if (!TUtils.checkServiceStatus(configuration.context)) {
            TUtils.startService(configuration.context, TService.class);
        }
    }

    private static void checkDbData(Context context, TModels.TInstallObject installObject) {
        TSQLHelper helper = new TSQLHelper(context);
        if (!helper.isSettingExist()) {
            helper.setSettings(installObject);
            Log.d(TAG, "db settings set!");
            return;
        }
        String appID = helper.getSetting(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_APP_ID);
        String androidV = helper.getSetting(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_ANDROID_VERSION);
        if (appID != null && androidV != null && !appID.equals(installObject.getApplicationId()) || !androidV.equals(installObject.getVersion())) {
            helper.setSettings(installObject);
            Log.d(TAG, "db settings update!");
        }

    }

    /**
     * Get lists all accounts of any type registered on the device and
     * send it to your Tapleader panel!
     * if you want to get user accounts data you must add {@link android.Manifest.permission#GET_ACCOUNTS}
     * and for API 23 and above must grant permission at RunTime!
     * check docs for more information!
     *
     * @throws java.lang.SecurityException: caller does not have permission to access {@link android.Manifest.permission#GET_ACCOUNTS}
     */
    public static void requestForUserAccountData() throws SecurityException {
        if (ManifestInfo.hasGrantedPermissions(getApplicationContext(), Constants.Permission.GET_ACCOUNTS)) {
            //noinspection MissingPermission
            mAccounts = AccountManager.get(getApplicationContext()).getAccounts();
            if (mAccounts.length > 0) {
                TgAccountUtils.initialize(mAccounts);
                String body = TgAccountUtils.accountsToJson(getApplicationContext()).toString();
                serviceHandler.userAccountData(body, new HttpResponse() {
                    @Override
                    public void onServerResponse(JSONObject data) {

                    }

                    @Override
                    public void onServerError(String message, int code) {

                    }
                });
            }
        } else {
            throw new SecurityException("caller does not have permission to access " + Manifest.permission.GET_ACCOUNTS);
        }
    }

    private static void checkForNewInstallOrUpdate(final boolean dangerousAccess) {
        TUtils.updateLunchCounter(getApplicationContext(), TUtils.getLunchCounter(getApplicationContext()) + 1);
        if (TUtils.shouldNotifyInstall(getApplicationContext())) {
            serviceHandler.installNotifier(TUtils.getClientDetails(getApplicationContext()).getJson().toString(), new HttpResponse() {
                @Override
                public void onServerResponse(JSONObject data) {
                    try {
                        if (data.getInt("Status") == Constants.Code.REQUEST_SUCCESS) {
                            TUtils.saveInstallData(data.getString("InstallationId"), getApplicationContext());
                            if (dangerousAccess) {
                                requestForUserAccountData();
                            }
                        } else
                            TLog.d(TAG, data.getString("Message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                        TLog.d(TAG, "initialize done and unlocked!");
                    }
                }

                @Override
                public void onServerError(String message, int code) {
                    lock.unlock();
                }
            });

        } else if (TUtils.shouldNotifyMoreInfo(getApplicationContext())) {
            if (TUtils.checkForPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                    && TUtils.shouldNotifyMoreInfo(getApplicationContext()) && !TUtils.getInstallationId(getApplicationContext()).equals("Unknown")) {
                serviceHandler.sendMoreInfo(TUtils.getClientDetails(getApplicationContext()).getJson().toString(),
                        TOfflineResponse.initialize(-1,getApplicationContext()).getMoreInfoResponse() );
            }
        } else if (TUtils.shouldNotifyUpdatePackage(getApplicationContext())) {
            final String PACKAGE_NAME = getApplicationContext().getPackageName();
            final String APPLICATION_ID = TPlugins.get().getApplicationId();
            final String CLIENT_KEY = TPlugins.get().getClientKey();
            String body = "";
            try {
                body = new TModels.TUpdatePackageObject(TUtils.getVersionName(getApplicationContext()), TUtils.getVersionCode(getApplicationContext()), PACKAGE_NAME, APPLICATION_ID, CLIENT_KEY).getJson().toString();
            } catch (JSONException e) {
                TLog.e(TAG, e);
            }
            serviceHandler.packageUpdate(body, new HttpResponse() {
                @Override
                public void onServerResponse(JSONObject data) {
                    try {
                        if (data.getInt("Status") == Constants.Code.REQUEST_SUCCESS) {
                            TUtils.saveUpdateData(getApplicationContext());
                        } else
                            TLog.d(TAG, data.getString("Message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                        TLog.d(TAG, "initialize done and unlocked!");
                    }
                }

                @Override
                public void onServerError(String message, int code) {

                }
            });
        } else if ((System.currentTimeMillis() - TUtils.getLastLaunchTime(getApplicationContext())) >= 1000 * 60 * 5) {
            Log.d(TAG, "notify retention!");
            serviceHandler.retention(getRetentionData(), new HttpResponse() {
                @Override
                public void onServerResponse(JSONObject data) {
                    try {
                        if (data.getInt("Status") == Constants.Code.REQUEST_SUCCESS) {
                            Log.d(TAG, "retention notified done!");
                        } else
                            TLog.d(TAG, data.getString("Message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }

                @Override
                public void onServerError(String message, int code) {

                }
            });
        } else {
            lock.unlock();
            TLog.d(TAG, "initialize done and unlocked!");
        }
    }

    static String getRetentionData() {
        int counter = TUtils.getLunchCounter(getApplicationContext());
        TModels.RetentionObject retentionObject = new TModels.RetentionObject();
        retentionObject.setClientKey(TPlugins.get().getClientKey());
        retentionObject.setDeviceId(TPlugins.get().getDeviceId());
        retentionObject.setLaunchCounter(counter);
        retentionObject.setPackageName(getApplicationContext().getPackageName());
        retentionObject.setInstallationId(TUtils.getInstallationId(getApplicationContext()));
        return retentionObject.getJson().toString();
    }

    static Context getApplicationContext() {
        return TPlugins.get().applicationContext();
    }

    static void checkContext() {
        if (TPlugins.get().applicationContext() == null) {
            throw new RuntimeException(Constants.Exception.NULL_CONTEXT);
        }
    }

    static void initializeTBroadcastReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Constants.Action.ACTION_RESTART_SERVICE);
        filter.addAction(Constants.Action.ACTION_ALARM_MANAGER);
        context.registerReceiver(new TBroadcastManager(context), filter);
    }

    static void checkCacheApplicationId() {
        synchronized (MUTEX) {
            try {
                String applicationId = TPlugins.get().getApplicationId();
                if (applicationId != null) {
                    File dir = Tapleader.getTapleaderCacheDir();
                    // Make sure the current version of the cache is for this application id.
                    File applicationIdFile = new File(dir, "getApplicationId");
                    if (applicationIdFile.exists()) {
                        boolean matches = false;
                        try {
                            RandomAccessFile f = new RandomAccessFile(applicationIdFile, "r");
                            byte[] bytes = new byte[(int) f.length()];
                            f.readFully(bytes);
                            f.close();
                            String diskApplicationId = new String(bytes, "UTF-8");
                            matches = diskApplicationId.equals(applicationId);
                        } catch (Exception e) {
                            TLog.e(TAG, e);
                        }

                        if (!matches) {
                            try {
                                TFileUtils.deleteDirectory(dir);
                            } catch (IOException e) {
                                TLog.e(TAG, e);
                            }
                        }
                    }

                    // Create the version file if needed.
                    applicationIdFile = new File(dir, "getApplicationId");
                    try {
                        FileOutputStream out = new FileOutputStream(applicationIdFile);
                        out.write(applicationId.getBytes("UTF-8"));
                        out.close();
                    } catch (Exception e) {
                        TLog.e(TAG, e);
                    }
                }
            } catch (Exception e) {
                TLog.e(TAG, e);
            }

        }
    }

    static File getTapleaderCacheDir() {
        TPlugins tPlugins = TPlugins.get();
        if (tPlugins == null)
            return null;
        return tPlugins.getCacheDir();
    }

    static File getTapleaderCacheDir(String subDir) {
        synchronized (MUTEX) {
            File dir = new File(getTapleaderCacheDir(), subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }
    }

    static File getTapleaderFilesDir() {
        TPlugins tPlugins = TPlugins.get();
        if (tPlugins == null)
            return null;
        return tPlugins.getFilesDir();
    }

    static File getTapleaderFilesDir(String subDir) {
        synchronized (MUTEX) {
            File fDir = getTapleaderFilesDir();
            if (fDir != null) {
                File dir = new File(fDir, subDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                return dir;
            }
            return null;
        }
    }

    /**
     * Java's object cloning mechanism can allow an attacker to manufacture new instances of classes you define
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Sorry but we can't let you to clone Tapleader!");
    }

    /**
     * we are greater than everything dude!
     *
     * @param obj
     * @return
     */
    @Override
    public final boolean equals(Object obj) {
        return false;
    }

    /**
     * Serialization is dangerous because it allows adversaries to get their hands on the internal state
     * of class.
     * An adversary can serialize class into a byte array that can be read and
     * This allows the adversary to inspect the full internal state of your object
     * including any fields we marked private
     * and including the internal state of any objects.
     *
     * @param out
     * @throws java.io.IOException
     */
    private final void writeObject(ObjectOutputStream out)
            throws java.io.IOException {
        throw new java.io.IOException("Sorry but Tapleader cannot be serialized!");
    }

    /**
     * An adversary can create a sequence of bytes that happens to deserialize to an instance of your class.
     * This is dangerous, since you do not have control over what state the deserialize object is in.
     *
     * @param in
     * @throws java.io.IOException
     */
    private final void readObject(ObjectInputStream in)
            throws java.io.IOException {
        throw new java.io.IOException("Class cannot be deserialize");
    }

    public final static class Configuration {

        final Context context;
        final String applicationId;
        final String clientKey;
        final String campaignId;
        final String server;
        final boolean localDataStoreEnabled;
        final boolean dangerousAccess;

        private Configuration(Builder builder) {
            this.context = builder.context;
            this.applicationId = builder.applicationId;
            this.clientKey = builder.clientKey;
            this.server = builder.server;
            this.localDataStoreEnabled = builder.localDataStoreEnabled;
            this.dangerousAccess = builder.dangerousAccess;
            this.campaignId = builder.campaignId;
        }

        public static final class Builder {
            private Context context;
            private String applicationId;
            private String clientKey;
            private String campaignId;
            private String server = "http://e.tapleader.com/api/sdk/";
            private boolean localDataStoreEnabled;
            private boolean dangerousAccess;

            public Builder(Context context, boolean dangerousAccess) {
                this.context = context;
                this.dangerousAccess = dangerousAccess;
                if (context != null) {
                    //localDataStoreEnabled = !TBroadcastManager.checkInternetAccess(context);
                    Context applicationContext = context.getApplicationContext();
                    Bundle metaData = ManifestInfo.getApplicationMetadata(applicationContext);
                    if (metaData != null) {
                        applicationId = String.valueOf(metaData.getInt(TAPLEADER_APPLICATION_ID));
                        clientKey = metaData.getString(TAPLEADER_CLIENT_KEY);
                        campaignId = metaData.getString(TAPLEADER_CAMPAIGN_ID);
                        /**
                         * {@link Bundle#getString(String)} returns null if all characters in value field of metadata are numerical!
                         */
                        if (campaignId == null) {
                            campaignId = String.valueOf(metaData.getInt(TAPLEADER_CAMPAIGN_ID));
                        }

                    }
                }
            }

            public Builder applicationId(String applicationId) {
                this.applicationId = applicationId;
                return this;
            }

            /**
             * Set the client key to be used by Tapleader.
             * <p>
             * This method is only required if you intend to use a different {@code getClientKey} than
             * is defined by {@link com.tapleader.Tapleader#TAPLEADER_CLIENT_KEY} in your {@code AndroidManifest.xml}.
             *
             * @param clientKey The client key to set.
             * @return The same builder, for easy chaining.
             */
            public Builder clientKey(String clientKey) {
                this.clientKey = clientKey;
                return this;
            }

            public Builder campaignId(String campaignId) {
                this.campaignId = campaignId;
                return this;
            }

            public Configuration build() {
                return new Configuration(this);
            }

        }
    }
}
