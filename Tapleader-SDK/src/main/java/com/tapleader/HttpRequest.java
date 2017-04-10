package com.tapleader;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

@TargetApi(Build.VERSION_CODES.CUPCAKE)
class HttpRequest extends AsyncTask<Object, Void, JSONObject> {
    private static final String TAG = "HttpRequest";
    private boolean carshReportEnable = true;
    private boolean isCanceled = false;
    private HttpResponse httpResponse;
    private String url;

    public HttpRequest(String url, Boolean carshReportEnable, HttpResponse httpResponse) {
        this.carshReportEnable = carshReportEnable;
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
                if(code<0){
                    httpResponse.onServerError(jsonObject.getString("Message"),code);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected JSONObject doInBackground(Object... params) {
        String body = (String) params[0];
        JSONObject result = null;
        String str = "";
        try {
            str = sendPost(url, body);
            result = new JSONObject(str);
        } catch (Exception e) {
            if (carshReportEnable) {
                TLog.e(TAG, e.getMessage());
            }
            result = new JSONObject();
            try {
                result.put("Status", Constants.Code.REQUEST_ERROR);
                result.put("Message", str);
                result.put("InstallationId",-1);
            } catch (JSONException e1) {
                //never mind:D
            }
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
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", System.getProperty("http.agent"));
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setRequestProperty("Request-Date", TUtils.getDateTime());
        con.setRequestProperty("Request-Key", TUtils.wSec(con.getRequestProperties().get("Request-Date") + TPlugins.get().getApplicationId()));

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }


}
