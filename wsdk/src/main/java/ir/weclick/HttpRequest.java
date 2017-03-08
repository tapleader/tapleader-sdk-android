package ir.weclick;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

@TargetApi(Build.VERSION_CODES.CUPCAKE)
class HttpRequest extends AsyncTask<Object,Void,JSONObject> {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG="HttpRequest";
    private static OkHttpClient client = new OkHttpClient();
    private HttpResponse httpResponse;
    private boolean isCanceled=false;
    private String url;
    private Gson gson;

    public HttpRequest(String url ,HttpResponse httpResponse){
        this.url=url;
        this.httpResponse=httpResponse;
        gson=new Gson();
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
            str=post(url,body);
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


    private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = requestBuilder(url,body);
        Response response = client.newCall(request).execute();
        return response.body().string();

    }

    private Request requestBuilder(String url,RequestBody body){
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }


}
