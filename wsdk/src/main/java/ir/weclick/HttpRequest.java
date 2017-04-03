package ir.weclick;

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
class HttpRequest extends AsyncTask<Object,Void,JSONObject> {
   // private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG="HttpRequest";
   // private static OkHttpClient client = new OkHttpClient();
    private HttpResponse httpResponse;
    private boolean isCanceled=false;
    private String url;
   // private Gson gson;

    public HttpRequest(String url ,HttpResponse httpResponse){
        this.url=url;
        this.httpResponse=httpResponse;
        //gson=new Gson();
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if(!isCanceled){
            httpResponse.onServerResponse(jsonObject);
        }
    }

    @Override
    protected JSONObject doInBackground(Object... params) {
        String body=(String)params[0];
        JSONObject result=null;
        String str="";
        try {
            str=sendPost(url,body);
            result=new JSONObject(str);
        } catch (IOException e) {
            WLog.e(TAG,e.getMessage());
        } catch (JSONException e) {
            WLog.e(TAG,e.getMessage());
            result=new JSONObject();
            try {
                result.put("Message",str);
            } catch (JSONException e1) {
                WLog.e(TAG,e1.getMessage());
            }
        }
        return result;
    }


    protected void onCanceled() {
        isCanceled=true;
        httpResponse.onServerError(Constants.Messages.REQUEST_TIMEOUT,Constants.Code.REQUEST_TIMEOUT);
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


    /*private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = requestBuilder(url,body);
        Response response = client.newCall(request).execute();
        return response.body().string();

    }*/

    /*private Request requestBuilder(String url,RequestBody body){
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }*/


    // HTTP POST request
    private String sendPost(String url ,String body) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con .setRequestMethod("POST");
        con.setRequestProperty("User-Agent", System.getProperty("http.agent"));
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

       // String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

        // Send post request
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
