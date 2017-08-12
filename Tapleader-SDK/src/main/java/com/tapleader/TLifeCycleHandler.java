package com.tapleader;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.io.BufferedWriter;

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


    private TLifeCycleHandler(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
            TLog.e(TAG+" onActivityStopped.",e);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private long logToDb(TModels.TLifeCycleObject lastTLO){
        long endTime=TUtils.dateParser(lastTLO.getEndTime()).getTime();
        long startTime=TUtils.dateParser(lastTLO.getStartTime()).getTime();
        if(endTime-startTime>=MIN_STOPOVER || SHOULD_NOTIFY_ALL )
            return helper.addActivityLifecycleLog(lastTLO);
        return -1;
    }




}
