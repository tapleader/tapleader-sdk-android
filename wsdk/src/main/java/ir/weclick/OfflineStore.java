package ir.weclick;

import android.content.Context;
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

    private static final String FILE_NAME="offlineStore";
    private static final String TAG="OfflineStore";
    private static OfflineStore mOfflineStore=null;
    private static Context context;
    private OfflineStore(){}
    private OfflineStore(Context context) {
        this.context=context;
        NetworkManager.init(this);
        Log.d(TAG,"constructed");
    }

    public static OfflineStore initialize(Context context){
        if(mOfflineStore==null)
            mOfflineStore=new OfflineStore(context);
        return mOfflineStore;
    }

    public void store(String url ,String body){
        File localData=new File(WPlugins.get().getCacheDir(),FILE_NAME);
        String data=getData(url,body);
        if(!localData.exists()){
            try {
                localData.createNewFile();
            } catch (IOException e) {
                WLog.e(TAG, e.getMessage());
                return;
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(localData, true));
            writer.write(data+"\r\n");
            writer.flush();
        } catch (IOException e) {
            WLog.e(TAG, e.getMessage());
        }


    }

    public void push(){
        File localData=new File(WPlugins.get().getCacheDir(),FILE_NAME);
        String data="";
        JSONObject result=null;
        try {
            data=WFileUtils.readFileToString(localData,"UTF-8");
            result=parsData(data);
        } catch (IOException e) {
            WLog.e(TAG,e.getMessage());
            return;
        }
        String path=null;
        try {
            path = result.getString("path");
        } catch (JSONException e) {
            WLog.e(TAG,e.getMessage());
            return;
        }
        if(path!=null && path.equals(Constants.Api.NEW_INSTALL)) {
            ServiceHandler.init().installNotifier(new HttpResponse() {
                @Override
                public void onServerResponse(JSONObject data) {
                    WLog.d(TAG, data.toString());
                    File localData = new File(WPlugins.get().getCacheDir(), FILE_NAME);
                    try {
                        WFileUtils.forceDelete(localData);
                    } catch (IOException e) {
                        WLog.e(TAG, e.getMessage());
                    }
                }

                @Override
                public void onServerError(String message, int code) {

                }
            });
        }
    }

    private String getData(String url,String body){
        JSONObject object=new JSONObject();
        try {
            object.put("path",url);
            object.put("body",body);
            object.put("date",WUtils.getDateTime());
        } catch (JSONException e) {
            WLog.e(TAG,e.getMessage());
        }
        return object.toString();
    }

    private JSONObject parsData(String data){
        JSONObject object=null;
        try {
            object=new JSONObject(data);
        } catch (JSONException e) {
            WLog.e(TAG,e.getMessage());
        }
        return object;
    }

    @Override
    public void onChange(boolean isConnected) {
        if(isConnected){
            push();
        }
    }
}
