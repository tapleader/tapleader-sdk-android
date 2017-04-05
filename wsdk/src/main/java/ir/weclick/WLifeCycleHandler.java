package ir.weclick;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mehdi akbarian on 2017-03-05.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class WLifeCycleHandler implements Application.ActivityLifecycleCallbacks {
    private final static String TAG="WLifeCycleHandler";
    private final static String FILE_NAME="activityTrackingLog";
    private final static String PREFS_NAME = "BufferCounter";
    private final static String PARAMETER_NAME="counter";
    private static WLifeCycleHandler mWLifeCycleHandler;
    private static WLifeCycleObject lastWLO;
    private BufferedWriter writer;
    private SharedPreferences prefs;
    private int counter=0;
    private final static int BUFFER_SIZE=10;

    private WLifeCycleHandler() {
        this.prefs = Weclick.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.counter=getLastCounter();
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

        public JSONObject getJson() throws JSONException {
            JSONObject object=new JSONObject();
            object.put("name",getName());
            object.put("startTime",getStartTime());
            object.put("endTime",getEndTime());
            return object;
        }
    }

    public static WLifeCycleHandler getInstance() {
        if (mWLifeCycleHandler == null) {
            mWLifeCycleHandler = new WLifeCycleHandler();
        }
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
        String data= null;
        try {
            data = lastWLO.getJson().toString();
        } catch (JSONException e) {
            WLog.e(TAG,e.getMessage());
            return;
        }
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
            } catch (Exception e) {
                WLog.e(TAG,e.getMessage());
            }
        }else {
            updateLastCounter(counter);
        }
    }

    /**
     * read data from file with name {@link WLifeCycleObject#FILE_NAME} and sed to server
     * @throws IOException if file is not accessible!
     */
    private void flush() throws IOException, JSONException {
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
                            counter=0;
                            updateLastCounter(counter);
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

    private String getBody(JSONArray jsonArray) throws JSONException {
        class Value{
            int count=0;
            long duration=0l;
            String date;

            public Value(int count, long duration,String date) {
                this.count = count;
                this.duration = duration;
                this.date=date;
            }
        }
        class Key{
            String name;
            String date;

            public Key(String name, String date) {
                this.name = name;
                this.date = date;
            }

            @Override
            public boolean equals(Object obj) {
                if(obj instanceof Key){
                    if(((Key) obj).name.equals(this.name) && ((Key) obj).date.equals(this.date)){
                        return true;
                    }
                }
                return false;
            }


            @Override
            public int hashCode() {
                String code=name+date;
                return code.hashCode();
            }
        }
        HashMap<Key,Value> counterMap=new HashMap<Key,Value>();
        for(int i=0;i<jsonArray.length();i++){
            JSONObject object= new JSONObject(jsonArray.get(i).toString());
            String name=object.getString("name");
            String end=object.getString("endTime");
            String start=object.getString("startTime");
            Date endDate=WUtils.dateParser(end);
            Date startDate=WUtils.dateParser(start);
            String date=WUtils.getSimpleDate(endDate);
            long result=endDate.getTime()-startDate.getTime();
            Key key=new Key(name,date);
            if(counterMap.containsKey(key)) {
                Value temp=counterMap.get(key);
                temp.count+=1;
                temp.duration+=result;
                counterMap.put(key,temp);
            }else {
                counterMap.put(key,new Value(1,result,date));
            }
        }
        JSONArray array=new JSONArray();
        String tempDate="";
        JSONArray tempArray=new JSONArray();
        for(Map.Entry<Key,Value> e:counterMap.entrySet()){
            JSONObject temp=new JSONObject();
            temp.put("name",e.getKey().name);
            temp.put("count",e.getValue().count);
            temp.put("duration",e.getValue().duration);
            if(!tempDate.equals(e.getValue().date)) {
                if(tempDate.length()!=0){
                    JSONObject object=new JSONObject();
                    object.put(tempDate,tempArray);
                    array.put(object);
                    tempArray=new JSONArray();
                    tempDate=e.getValue().date;
                }else {
                    tempDate=e.getValue().date;
                }
            }
            if(tempDate.equals(e.getValue().date)){
                tempArray.put(temp);
            }
        }
        JSONObject object=new JSONObject();
        object.put(tempDate,tempArray);
        array.put(object);
        JSONObject body=new JSONObject();
        String applicationId=WPlugins.get().getApplicationId();
        String clientKey=WPlugins.get().getClientKey();
        String deviceId=WPlugins.get().getDeviceId();
        try {
            body.put("getApplicationId",applicationId);
            body.put("getClientKey",clientKey);
            body.put("deviceId",deviceId);
            body.put("data",array);
        } catch (JSONException e) {
            WLog.e(TAG,e.getMessage());
        }
        return body.toString();
    }

    private int getLastCounter(){
        int id = prefs.getInt(PARAMETER_NAME, 0);
        return id;
    }

    private void updateLastCounter(int id){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PARAMETER_NAME, id);
        editor.commit();
    }

}
