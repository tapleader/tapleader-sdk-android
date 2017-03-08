package ir.weclick;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by mehdi akbarian on 2017-03-05.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class WLifeCycleHandler implements Application.ActivityLifecycleCallbacks {
    private final static String TAG="WLifeCycleHandler";
    private final static String FILE_NAME="activityTrackingLog";
    private static WLifeCycleHandler mWLifeCycleHandler;
    private static WLifeCycleObject lastWLO;
    private BufferedWriter writer;
    private int counter=0;
    private final static int BUFFER_SIZE=5;

    private WLifeCycleHandler() {
    }
    /**
     * should be static for security reason!
     */
    static class WLifeCycleObject{
        private String name;
        private String startTime;
        private String endTime;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

    public static WLifeCycleHandler getInstance() {
        if (mWLifeCycleHandler == null)
            mWLifeCycleHandler = new WLifeCycleHandler();
        return mWLifeCycleHandler;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated" + activity.getComponentName().getClassName());


    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(TAG, "onActivityStarted" + activity.getComponentName().toString());
        if (lastWLO == null) {
            lastWLO=new WLifeCycleObject();
            lastWLO.setName(activity.getComponentName().getClassName());
            lastWLO.setStartTime(WUtils.getDateTime());
        } else {
            String current= WUtils.getDateTime();
            lastWLO.setEndTime(current);
            log(lastWLO);
            lastWLO=new WLifeCycleObject();
            lastWLO.setName(activity.getComponentName().getClassName());
            lastWLO.setStartTime(current);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, "onActivityResumed" + activity.getComponentName().toString());
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }


    private void log(WLifeCycleObject lastWLO) {
        final File log = new File(WPlugins.get().getCacheDir(), FILE_NAME);
        String data= WUtils.toJson(lastWLO);
        if (!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException e) {
                WLog.e(TAG, e.getMessage());
                return;
            }
        }
        try {
            writer = new BufferedWriter(new FileWriter(log, true));
            writer.write(data+"\r\n");
            writer.flush();
        } catch (IOException e) {
            WLog.e(TAG, e.getMessage());
        }
        if(counter++>=BUFFER_SIZE){
            try {
                flush();
            } catch (IOException e) {
                WLog.e(TAG,e.getMessage());
            }
        }
    }

    /**
     * read data from file with name {@link WLifeCycleObject#FILE_NAME} and sed to server
     * @throws IOException if file is not accessible!
     */
    private void flush() throws IOException {
        final File log = new File(WPlugins.get().getCacheDir(), FILE_NAME);
        if(!log.exists()) {
            WLog.e(TAG, Constants.Exception.ACTIVITY_LOG_NOT_FOUND);
            return;
        }else if(log.canRead()) {
            String body= WFileUtils.readFileToString(log,"UTF-8");
            String[] data=parsFile(body);
            JSONArray array=getJsonArray(data);
            String result=getBody(array);
            ServiceHandler.init().activityTracking(result, new HttpResponse() {
                @Override
                public void onServerResponse(JSONObject data) {
                    //TODO: should check what will happen in feature!
                    boolean isSuccess=true;
                    if(isSuccess){
                        try {
                            WFileUtils.forceDelete(log);
                        } catch (IOException e) {
                            WLog.e(TAG,e.getMessage());
                        }
                    }
                }

                @Override
                public void onServerError(String message, int code) {
                    WLog.e(TAG,message+ "error code: "+code);
                }
            });
        }else {
            //what can we do if we can't access to file?hum?
        }
    }


    private JSONArray getJsonArray(String[] data){
        JSONArray array=new JSONArray();
        for(int i=0;i<data.length;i++)
            array.put(data[i]);
        return array;
    }

    private String[] parsFile(String body){
        String[] lines=body.split("\\r?\\n");
        return lines;
    }

    private String getBody(JSONArray jsonArray){
        JSONObject body=new JSONObject();
        String applicationId=WPlugins.get().applicationId();
        String clientKey=WPlugins.get().clientKey();
        try {
            body.put("applicationId",applicationId);
            body.put("clientKey",clientKey);
            body.put("data",jsonArray);
        } catch (JSONException e) {
            WLog.e(TAG,e.getMessage());
        }
        return body.toString();
    }

}
