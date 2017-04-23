package com.tapleader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mehdi akbarian on 2017-04-19.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TOfflineResponse {
    private static final String TAG = "TOfflineResponse";

    static HttpResponse installResponse=new HttpResponse() {
        @Override
        public void onServerResponse(JSONObject data) {
            try {
                if (data.getInt("Status") == Constants.Code.REQUEST_SUCCESS) {
                    TUtils.saveUpdateData();
                    //TODO: remove install record from db
                } else
                    TLog.d(TAG, data.getString("Message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServerError(String message, int code) {

        }
    };
    static HttpResponse activityTrackingResponse=new HttpResponse() {
        @Override
        public void onServerResponse(JSONObject data) {

        }

        @Override
        public void onServerError(String message, int code) {

        }
    };
}