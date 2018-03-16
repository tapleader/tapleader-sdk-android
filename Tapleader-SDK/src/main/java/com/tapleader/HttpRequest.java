package com.tapleader;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import com.flurry.android.FlurryAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

@TargetApi(Build.VERSION_CODES.CUPCAKE)
class HttpRequest extends AsyncTask<Object, Void, JSONObject> {
    private static final String TAG = "HttpRequest";
    private static final int CONNECT_TIMEOUT=10000;
    private static final int READ_TIMEOUT=20000;
    private boolean crashReportEnable = true;
    private boolean isCanceled = false;
    private HttpResponse httpResponse;
    private int retryCounter=0;
    private String url;

    public HttpRequest(String url, Boolean crashReportEnable, HttpResponse httpResponse) {
        this.crashReportEnable = crashReportEnable;
        this.httpResponse = httpResponse;
        this.url = url;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if (!isCanceled) {
            httpResponse.onServerResponse(jsonObject);
            try {
                int code=jsonObject.getInt("Status");
                if(code<0)
                    httpResponse.onServerError(jsonObject.getString("Message"),code);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected JSONObject doInBackground(Object... params) {
        String body = "";
        if(params!=null && params.length>0 && params[0]!=null)
            body = (String) params[0];
        else if(params!=null && params.length>0)
            return getFailResult("body is :"+ params[0]!=null ? (String) params[0] : "NULL");
        else
            return getFailResult("params is null in HttpRequest");
        JSONObject result = null;
        String str = "";
        try {
            str = sendPost(url, body);
            if(str!=null && !str.isEmpty())
                result = new JSONObject(str);
            else
                return getFailResult("no response from server");
        } catch (Exception e) {
            if (crashReportEnable) {
                TLog.e(TAG+" doInBackground.", e);
            }
            result = getFailResult(e.getMessage());
        }
        return result;
    }


    protected void onCanceled() {
        isCanceled = true;
        httpResponse.onServerError(Constants.Messages.REQUEST_TIMEOUT, Constants.Code.REQUEST_TIMEOUT);
        httpResponse.onServerResponse(null);
    }

    public void setTimeout(final long millsec) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onCanceled();
            }
        }, millsec);
    }

    private String sendPost(String url, String body) throws IOException {
        URL obj = new URL(url);

        sendAnalytics(url,body,"send post method (FIRST TRY)", Calendar.getInstance().toString());

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setConnectTimeout(CONNECT_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", System.getProperty("http.agent"));
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setRequestProperty("Request-Date", TUtils.getDateTime());
        if(TPlugins.get()!=null)
            con.setRequestProperty("Request-Key", TUtils.wSec(con.getRequestProperties().get("Request-Date") + TPlugins.get().getApplicationId()));
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        if(responseCode==HttpURLConnection.HTTP_OK
                || responseCode==HttpURLConnection.HTTP_ACCEPTED) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }else if((responseCode== HttpURLConnection.HTTP_CLIENT_TIMEOUT
        || responseCode==HttpURLConnection.HTTP_GATEWAY_TIMEOUT
        || responseCode==HttpURLConnection.HTTP_FORBIDDEN) && retryCounter<3){
            retryCounter++;
            sendAnalytics(url,body,"send post method (RETRY)",Calendar.getInstance().toString());
            return sendPost(url,body);
        }else {
            return "";
        }
    }

    private void sendAnalytics(String url, String body, String s, String s1) {



        Map<String, String> articleParams = new HashMap<String, String>();

//param keys and values have to be of String type
        articleParams.put("url", url);
        articleParams.put("body", body);
        articleParams.put("details", s);
        articleParams.put("time", s1);

//up to 10 params can be logged with each event
        FlurryAgent.logEvent("HttpRequest.java", articleParams);
    }


    private JSONObject getFailResult(String message){
        JSONObject result = new JSONObject();
        try {
            result.put("Status", Constants.Code.REQUEST_ERROR);
            result.put("Message", message);
            result.put("accept",false);
            result.put("InstallationId",-1);
        } catch (JSONException e1) {
            //
        }
        return result;
    }


}
