package com.tapleader;

import org.json.JSONObject;

/**
 * Created by mehdi akbarian on 2017-04-19.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TOfflineResponse {
    private static final String TAG = "TOfflineResponse";
    private long id;
    private HttpResponse installResponse=new HttpResponse() {
        @Override
        public void onServerResponse(JSONObject data) {
            try {
                int status=data.getInt("Status");
                if (status == Constants.Code.REQUEST_SUCCESS) {
                    TUtils.saveInstallData(data.getString("InstallationId"));
                    OfflineStore.initialize(TUtils.getContext()).deleteInstallRecords();
                } else if(status == Constants.Code.NOT_COMPATIBLE_APPLICATION_ID_AND_CLIENT_KEY){
                    TLog.d(TAG, data.getString("Message"));
                    OfflineStore.initialize(TUtils.getContext()).deleteRequest(id);
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
                    OfflineStore.initialize(TUtils.getContext()).deleteRequest(id);
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

    private TOfflineResponse(long recordId){
        this.id=recordId;
    }

    static TOfflineResponse initialize(long recordId){
        return new TOfflineResponse(recordId);
    }

    HttpResponse getActivityTrackingResponse(){
        return activityTrackingResponse;
    }

    HttpResponse getInstallResponse(){
        return installResponse;
    }


}
