package com.tapleader;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by mehdi akbarian on 2017-04-19.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TOfflineResponse {
    private static final String TAG = "TOfflineResponse";
    private long id;
    private Context context;
    private HttpResponse installResponse=new HttpResponse() {
        @Override
        public void onServerResponse(JSONObject data) {
            try {
                int status=data.getInt("Status");
                if (status == Constants.Code.REQUEST_SUCCESS) {
                    TUtils.saveInstallData(data.getString("InstallationId"),context);
                    OfflineStore.initialize(context).deleteInstallRecords();
                } else if(status == Constants.Code.NOT_COMPATIBLE_APPLICATION_ID_AND_CLIENT_KEY){
                    TLog.d(TAG, data.getString("Message"));
                    OfflineStore.initialize(context).deleteRequest(id);
                }else {
                    //do nothing
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServerError(String message, int code) {

        }
    };

    private HttpResponse activityTrackingResponse=new HttpResponse() {
        @Override
        public void onServerResponse(JSONObject data) {
            try {
                int status=data.getInt("Status");
                if (status == Constants.Code.REQUEST_SUCCESS) {
                    OfflineStore.initialize(context).deleteRequest(id);
                    File log = new File(TPlugins.get().getCacheDir(), "t_activity_tracking_log");
                    if (!log.exists()) {
                        TLog.e(TAG, new Exception(Constants.Exception.ACTIVITY_LOG_NOT_FOUND));
                    }else
                        TFileUtils.forceDelete(log);
                } else {
                    //do nothing
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServerError(String message, int code) {

        }
    };

    private TOfflineResponse(long recordId,Context context){
        this.context=context;
        this.id=recordId;
    }

    static TOfflineResponse initialize(long recordId,Context context){
        return new TOfflineResponse(recordId,context);
    }

    HttpResponse getActivityTrackingResponse(){
        return activityTrackingResponse;
    }

    HttpResponse getInstallResponse(){
        return installResponse;
    }


}
