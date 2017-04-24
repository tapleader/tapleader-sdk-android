package com.tapleader;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

public class TBroadcastManager extends BroadcastReceiver {
    private static List<NetworkObserver> networkObservers;
    private static final String TAG = "TBroadcastManager";
    private static final boolean SHOULD_PINGPONG=true;
    private static final boolean SHOULD_NOTIFY_INSTANTLY=false;
    private static AtomicBoolean isConnected=new AtomicBoolean(false);
    private static NetworkInfo activeNetInfo;
    private static NetworkInfo activeNetwork;
    private static Object MUTEX=new Object();
    private static final long MAX_LAT=120000;
    private static final long MIN_LAT=60000;
    private static Context context;

    public TBroadcastManager(){

    }
    public TBroadcastManager(Context context) {
        this.context=context;
        doPingPong();
    }

    private static boolean checkInstantly(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager==null)
            return false;
        activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetInfo==null)
            return false;
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    static void registerNetworkObserver(NetworkObserver networkObserver) {
        if (networkObservers == null) {
            networkObservers = new ArrayList<>();
        }
        Log.d(TAG,"registerNetworkObserver");
        networkObservers.add(networkObserver);
        if(SHOULD_NOTIFY_INSTANTLY)
            networkObserver.onChange(checkInstantly(TUtils.getContext()));
    }

    static void destroyNetworkObserver(NetworkObserver networkObserver) {
        if (networkObservers != null) {
            if (networkObservers.contains(networkObserver)) {
                networkObservers.remove(networkObserver);
            }
        }
    }

    static boolean checkInternetAccess(Context context) {
        return checkInstantly(context);
    }

    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case ConnectivityManager.CONNECTIVITY_ACTION:
                pushUpdateMessage(checkInstantly(context),SHOULD_PINGPONG);
                break;
            case Constants.Action.ACTION_RESTART_SERVICE:
                TUtils.startService(TUtils.getContext(), TService.class);
                break;
        }

    }

    private void pushUpdateMessage(boolean state,boolean pingPong) {
        synchronized (MUTEX) {
            if(!pingPong) {
                broadcastAll(state);
                isConnected.set(state);
            }else if (state == true) {
                doPingPong();
            }else {
                broadcastAll(state);
                isConnected.set(false);
            }
        }

    }

    private void doPingPong() {
        Log.d(TAG,"do pingpong");
        if(context==null){
            Log.d(TAG,"Context is null");
            return;
        }
        ServiceHandler.init(context).pingPong(new HttpResponse() {
            @Override
            public void onServerResponse(JSONObject data) {
                try {
                    if (data.getInt("Status") == Constants.Code.REQUEST_SUCCESS) {
                        broadcastAll(true);
                        isConnected.set(true);
                    }else {
                        Log.d(TAG,"response state is not success!");
                        scheduleJob();
                    }
                } catch (JSONException e) {
                    scheduleJob();
                }
            }

            @Override
            public void onServerError(String message, int code) {
                Log.d(TAG,"TBroadcastManager connection is true but cant access ping pong! message: "+message+" code: "+code);
                scheduleJob();
            }
        });
    }

    private void broadcastAll(boolean b) {
        if (networkObservers != null) {
            for (NetworkObserver n : networkObservers) {
                if (n != null)
                    n.onChange(b);
            }
        }
    }



    void scheduleJob(){
        //TODO: remove false!
        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            class TJS extends JobService{

                @Override
                public boolean onStartJob(JobParameters params) {
                    pushUpdateMessage(checkInstantly(TUtils.getContext()),SHOULD_PINGPONG);
                    return false;
                }

                @Override
                public boolean onStopJob(JobParameters params) {
                    return false;
                }
            }
            ComponentName serviceComponent = new ComponentName(TUtils.getContext(), TJS.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            builder.setMinimumLatency(MIN_LAT);
            builder.setOverrideDeadline(MAX_LAT);
            builder.setRequiresCharging(false);
            JobScheduler jobScheduler = (JobScheduler) TUtils.getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if(isConnected.get())
                        cancel();
                    pushUpdateMessage(checkInstantly(TUtils.getContext()),SHOULD_PINGPONG);
                    cancel();
                }
            },MIN_LAT,MAX_LAT);
        }
    }
}
