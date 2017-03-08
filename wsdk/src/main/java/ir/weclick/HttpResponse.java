package ir.weclick;

import org.json.JSONObject;

/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

interface HttpResponse {

    void onServerResponse(JSONObject data);
    void onServerError(String message,int code);
}
