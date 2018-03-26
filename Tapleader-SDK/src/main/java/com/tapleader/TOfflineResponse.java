package com.tapleader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
                    TLog.d(TAG, "\n==================WARNING=====================\n"
                            +data.getString("Message")
                    + "\n================================================\n");
                    OfflineStore.initialize(context).deleteRequest(id);
                }else {
                    TLog.e(TAG+"InstallResponse#status_check",new Exception("status:"+status));
                }
            } catch (Exception e) {
                TLog.e(TAG+"#InstallResponse",e);
            }
        }

        @Override
        public void onServerError(String message, int code) {
            TLog.e(TAG+"#InstallResponse",new Exception("on server error: "+message+" code"+code));
        }
    };

    private HttpResponse activityTrackingResponse=new HttpResponse() {
        @Override
        public void onServerResponse(JSONObject data) {
            try {
                int status=data.getInt("Status");
                if (status == Constants.Code.REQUEST_SUCCESS) {
                    OfflineStore.initialize(context).deleteRequest(id);
                    TUtils.updateLastPushActivityLogTime(context,System.currentTimeMillis());

                } else {
                    TLog.e(TAG+"#activityTrackingResponse#status_check"
                            ,new Exception("status:"+status));
                }
            } catch (Exception e) {
                TLog.e(TAG+"#activityTrackingResponse",e);
            }
        }

        @Override
        public void onServerError(String message, int code) {
            TLog.e(TAG+"#activityTrackingResponse"
                    ,new Exception("on server error: "+message+" code"+code));
        }
    };

    private TOfflineResponse(long recordId,Context context){
        this.context=context;
        this.id=recordId;
    }

    static TOfflineResponse initialize(long recordId,Context context){
        return new TOfflineResponse(recordId,context);
    }

    private HttpResponse retentionResponse =new HttpResponse() {
        @Override
        public void onServerResponse(JSONObject data) {
            int status= 0;
            try {
                status = data.getInt("Status");
                if (status == Constants.Code.REQUEST_SUCCESS) {
                    Log.d(TAG,"default response done! for record with id= "+id);
                    OfflineStore.initialize(context).deleteRequest(id);
                }else {
                    TLog.e(TAG+"#retentionResponse#status_check"
                            ,new Exception(data.getString("Message")+"\n for record with id= "+id));
                }
            } catch (JSONException e) {
                TLog.e(TAG+"#retentionResponse",e);
            }

        }

        @Override
        public void onServerError(String message, int code) {
            TLog.e(TAG+"#retentionResponse"
                    ,new Exception("on server error: "+message+" code"+code));
        }
    };

    private HttpResponse moreInfoResponse=new HttpResponse() {
        @Override
        public void onServerResponse(JSONObject data) {
            int status= 0;
            try {
                status = data.getInt("Status");
                if (status == Constants.Code.REQUEST_SUCCESS) {
                    TUtils.updateMoreInfo(context,false);
                }else {
                    TLog.e(TAG+"#moreInfoResponse#status_check"
                            ,new Exception("status:"+status));
                }
            } catch (JSONException e) {
                TLog.e(TAG+"#moreInfoResponse",e);
            }
        }

        @Override
        public void onServerError(String message, int code) {
            TLog.e(TAG+"#moreInfoResponse"
                    ,new Exception("on server error: "+message+" code"+code));
        }
    };


    private HttpResponse eventResponse=new HttpResponse() {
        @Override
        public void onServerResponse(JSONObject data) {
            try {
                int status=data.getInt("Status");
                if(status==Constants.Code.REQUEST_SUCCESS){
                    new TSQLHelper(context).deleteEvents();
                }else {
                    TLog.e(TAG+"#eventResponse#status_check"
                            ,new Exception("status:"+status));
                }
            } catch (JSONException e) {
                TLog.e(TAG+"#eventResponse",e);
            }
        }

        @Override
        public void onServerError(String message, int code) {
            TLog.e(TAG+"#eventResponse",new Exception("event push failed : "+message));
        }
    };

    HttpResponse getMoreInfoResponse() {
        return moreInfoResponse;
    }

    HttpResponse getRetentionResponse(){
        return retentionResponse;
    }

    HttpResponse getActivityTrackingResponse(){
        return activityTrackingResponse;
    }

    HttpResponse getInstallResponse(){
        return installResponse;
    }

    HttpResponse getEventResponse() {
        return eventResponse;
    }
}
