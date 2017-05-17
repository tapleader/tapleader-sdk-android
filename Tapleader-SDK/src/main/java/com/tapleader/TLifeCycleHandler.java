package com.tapleader;

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

class TLifeCycleHandler implements Application.ActivityLifecycleCallbacks {
    private final static String FILE_NAME = "t_activity_tracking_log";
    private final static String PREFS_NAME = "BufferCounter";
    private final static String PARAMETER_NAME = "counter";
    private final static String TAG = "TLifeCycleHandler";
    private final static boolean SHOULD_NOTIFY_ALL= false;
    private static TLifeCycleHandler mTLifeCycleHandler;
    private static TModels.TLifeCycleObject lastTLO;
    private final static long MIN_STOPOVER=3*1000;
    private final static int BUFFER_SIZE = 10;
    private SharedPreferences prefs;
    private BufferedWriter writer;
    private  TSQLHelper helper;
    private int counter = 0;

    private TLifeCycleHandler(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.counter = getLastCounter();
        this.helper=new TSQLHelper(context);
    }

    public static TLifeCycleHandler getInstance(Context context) {
        if (mTLifeCycleHandler == null) {
            mTLifeCycleHandler = new TLifeCycleHandler(context);
        }
        return mTLifeCycleHandler;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        TLog.d(TAG, "onActivityCreated" + activity.getComponentName().getClassName());


    }

    @Override
    public void onActivityStarted(Activity activity) {
        TLog.d(TAG, "onActivityStarted" + activity.getComponentName().toString());
        if (lastTLO == null) {
            lastTLO = new TModels.TLifeCycleObject();
            lastTLO.setName(activity.getComponentName().getClassName().replace('.','_'));
            lastTLO.setStartTime(TUtils.getDateTime());
        } else {
            String current = TUtils.getDateTime();
            lastTLO.setEndTime(current);
            logToDb(lastTLO);
            lastTLO = new TModels.TLifeCycleObject();
            lastTLO.setName(activity.getComponentName().getClassName().replace('.','_'));
            lastTLO.setStartTime(current);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        try {
            boolean foreground = new TForegroundCheckTask().execute(TUtils.getContext()).get();
            if(!foreground) {
                TUtils.updateLunchTime(TUtils.getContext(),System.currentTimeMillis());
            }
        } catch (Exception e) {
            TLog.e(TAG,e);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
    @Deprecated
    private void log(TModels.TLifeCycleObject lastTLO) {
        final File log = new File(TPlugins.get().getCacheDir(), FILE_NAME);
        String data = null;
        try {
            data = lastTLO.getJson().toString();
        } catch (JSONException e) {
            TLog.e(TAG, e);
            return;
        }
        if (!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException e) {
                TLog.e(TAG, e);
                return;
            }
        }
        try {
            writer = new BufferedWriter(new FileWriter(log, true));
            writer.write(data + "\r\n");
            writer.flush();
        } catch (IOException e) {
            TLog.e(TAG, e);
        }
        if (counter++ >= BUFFER_SIZE) {
            try {
                flush();
            } catch (Exception e) {
                TLog.e(TAG, e);
            }
        } else {
            updateLastCounter(counter);
        }
    }

    private long logToDb(TModels.TLifeCycleObject lastTLO){
        long endTime=TUtils.dateParser(lastTLO.getEndTime()).getTime();
        long startTime=TUtils.dateParser(lastTLO.getStartTime()).getTime();
        if(endTime-startTime>=MIN_STOPOVER || SHOULD_NOTIFY_ALL )
            return helper.addActivityLifecycleLog(lastTLO);
        return -1;
    }
    /**
     * read data from file with name {@link TLifeCycleHandler#FILE_NAME} and sed to server
     *
     * @throws IOException if file is not accessible!
     */
    @Deprecated
    private void flush() throws IOException, JSONException {
        if(TUtils.getContext()==null) {
            TLog.d(TAG,"context is null");
            return;
        }
        final File log = new File(TPlugins.get().getCacheDir(), FILE_NAME);
        if (!log.exists()) {
            TLog.e(TAG, new Exception(Constants.Exception.ACTIVITY_LOG_NOT_FOUND));
            return;
        } else if (log.canRead()) {
            String body = TFileUtils.readFileToString(log, "UTF-8");
            String[] data = TFileUtils.splitFileLines(body);
            JSONArray array = getJsonArray(data);
            String result = getBody(array);
            ServiceHandler.init(TUtils.getContext()).activityTracking(result, new HttpResponse() {
                @Override
                public void onServerResponse(JSONObject data) {
                    boolean isSuccess = true;
                    try {
                        if (data.getInt("Status") == Constants.Code.REQUEST_SUCCESS) {
                            try {
                                Log.d(TAG,data.toString());
                                TFileUtils.forceDelete(log);
                                counter = 0;
                                updateLastCounter(counter);
                            } catch (IOException e) {
                                TLog.e(TAG, e);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onServerError(String message, int code) {
                }
            });
        }
    }

    @Deprecated
    private JSONArray getJsonArray(String[] data) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < data.length; i++)
            array.put(data[i]);
        return array;
    }

    @Deprecated
    private String[] parsFile(String body) {
        String[] lines = body.split("\\r?\\n");
        return lines;
    }

    /**
     * When I wrote this, only God and I understood what I was doing. Now, God only knows
     *
     * @param jsonArray
     * @return
     * @throws JSONException
     */
    @Deprecated
    private String getBody(JSONArray jsonArray) throws JSONException {
        class Value {
            int count = 0;
            long duration = 0l;

            public Value(int count, long duration) {
                this.count = count;
                this.duration = duration;
            }
        }
        class Key {
            String name;
            String date;

            public Key(String name, String date) {
                this.name = name;
                this.date = date;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Key)
                    if (((Key) obj).name.equals(this.name) && ((Key) obj).date.equals(this.date))
                        return true;
                return false;
            }

            @Override
            public int hashCode() {
                String code = name + date;
                return code.hashCode();
            }
        }
        HashMap<Key, Value> logMap = new HashMap<Key, Value>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = new JSONObject(jsonArray.get(i).toString());
            String name = object.getString("name");
            String end = object.getString("endTime");
            String start = object.getString("startTime");
            Date endDate = TUtils.dateParser(end);
            Date startDate = TUtils.dateParser(start);
            String date = TUtils.getSimpleDate(endDate);
            long result = endDate.getTime() - startDate.getTime();
            Key key = new Key(name, date);
            if (logMap.containsKey(key)) {
                Value temp = logMap.get(key);
                temp.count += 1;
                temp.duration += result;
                logMap.put(key, temp);
            } else {
                logMap.put(key, new Value(1, result));
            }
        }
        JSONArray dateArray = new JSONArray();
        String currentDate = "";
        JSONArray innerLogArray = new JSONArray();
        for (Map.Entry<Key, Value> e : logMap.entrySet()) {
            JSONObject temp = new JSONObject();
            temp.put("name", e.getKey().name);
            temp.put("count", e.getValue().count);
            temp.put("duration", e.getValue().duration);
            if (!currentDate.equals(e.getKey().date)) {
                if (!currentDate.isEmpty()) {
                    JSONObject logObject = new JSONObject();
                    logObject.put(currentDate, innerLogArray);
                    dateArray.put(logObject);
                    innerLogArray = new JSONArray();
                    currentDate = e.getKey().date;
                } else {
                    currentDate = e.getKey().date;
                }
            }
            if (currentDate.equals(e.getKey().date)) {
                innerLogArray.put(temp);
            }
        }
        JSONObject object = new JSONObject();
        object.put(currentDate, innerLogArray);
        dateArray.put(object);
        JSONObject body = new JSONObject();
        String applicationId = TPlugins.get().getApplicationId();
        String clientKey = TPlugins.get().getClientKey();
        String deviceId = TPlugins.get().getDeviceId();
        try {
            body.put("clientKey", clientKey);
            body.put("deviceId", deviceId);
            body.put("packageName",TUtils.getContext().getPackageName());
            body.put("data", dateArray);
        } catch (JSONException e) {
            TLog.e(TAG, e);
        }
        return body.toString();
    }

    @Deprecated
    private int getLastCounter() {
        int id = prefs.getInt(PARAMETER_NAME, 0);
        return id;
    }

    @Deprecated
    private void updateLastCounter(int id) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PARAMETER_NAME, id);
        editor.commit();
    }



}
