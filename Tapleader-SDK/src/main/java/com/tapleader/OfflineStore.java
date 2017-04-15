package com.tapleader;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class OfflineStore implements NetworkObserver {

    private static final String FILE_NAME = "t_offlineStore";
    private static final String TAG = "OfflineStore";
    private static OfflineStore mOfflineStore = null;
    private static Context context;

    private OfflineStore() {
    }

    private OfflineStore(Context context) {
        this.context = context;
        NetworkManager.init(this);
        Log.d(TAG, "constructed");
    }

    public static OfflineStore initialize(Context context) {
        if (mOfflineStore == null)
            mOfflineStore = new OfflineStore(context);
        return mOfflineStore;
    }

    public void store(String url, String body) {
        File localData = new File(TPlugins.get().getCacheDir(), FILE_NAME);
        String data = getData(url, body);
        if (!localData.exists()) {
            try {
                localData.createNewFile();
            } catch (IOException e) {
                TLog.e(TAG, e.getMessage());
                return;
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(localData, true));
            writer.write(data + "\r\n");
            writer.flush();
        } catch (IOException e) {
            TLog.e(TAG, e.getMessage());
        }


    }

    public void push() {
        File localData = new File(TPlugins.get().getCacheDir(), FILE_NAME);
        if (!localData.exists())
            return;
        String data = "";
        JSONObject result = null;
        try {
            data = TFileUtils.readFileToString(localData, "UTF-8");
            result = parsData(data);
        } catch (IOException e) {
            TLog.e(TAG, e.getMessage());
            return;
        }
        String path = null;
        try {
            path = result.getString("path");
        } catch (JSONException e) {
            TLog.e(TAG, e.getMessage());
            return;
        }
        if (path != null && path.equals(Constants.Api.NEW_INSTALL)) {
            final String INSTALL_PARAMETER_NAME = "n_install";
            final String PACKAGE_VERSION_NAME = "p_version_name";
            final String PACKAGE_VERSION_CODE = "p_version_code";
            final String USER_INSTALLATION_ID = "p_user_install_id";
            final String PREFS_NAME = "App_info";
            final SharedPreferences prefs = Tapleader.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            if (prefs.getBoolean(INSTALL_PARAMETER_NAME, true)) {
                ServiceHandler.init().installNotifier(TUtils.getClientDetails(), new HttpResponse() {
                    @Override
                    public void onServerResponse(JSONObject data) {
                        TLog.d(TAG, data.toString());
                        try {
                            if (data.getInt("Status") == Constants.Code.REQUEST_SUCCESS) {
                                File localData = new File(TPlugins.get().getCacheDir(), FILE_NAME);
                                //TODO:it just for new install!
                                TFileUtils.forceDelete(localData);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean(INSTALL_PARAMETER_NAME, false);
                                editor.putString(PACKAGE_VERSION_NAME, TUtils.getVersionName());
                                editor.putInt(PACKAGE_VERSION_CODE, TUtils.getVersionCode());
                                editor.putString(USER_INSTALLATION_ID, data.getString("InstallationId"));
                                editor.apply();
                            }
                        } catch (Exception e) {
                            TLog.e(TAG, e.getMessage());
                        }
                    }

                    @Override
                    public void onServerError(String message, int code) {

                    }
                });
            }else {
                //TODO:it just for new install!
                try {
                    TFileUtils.forceDelete(localData);
                } catch (IOException e) {
                    TLog.e(TAG,e.getMessage());
                }
            }
        }
    }

    private String getData(String url, String body) {
        JSONObject object = new JSONObject();
        try {
            object.put("path", url);
            object.put("body", body);
            object.put("date", TUtils.getDateTime());
        } catch (JSONException e) {
            TLog.e(TAG, e.getMessage());
        }
        return object.toString();
    }

    private JSONObject parsData(String data) {
        JSONObject object = null;
        try {
            object = new JSONObject(data);
        } catch (JSONException e) {
            TLog.e(TAG, e.getMessage());
        }
        return object;
    }

    @Override
    public void onChange(boolean isConnected) {
        if (isConnected) {
            push();
        }
    }
}
