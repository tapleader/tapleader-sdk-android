package com.tapleader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Keep;
import android.support.annotation.RestrictTo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */
@Keep
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class TBroadcastManager extends BroadcastReceiver {
    private static HashSet<NetworkObserver> networkObservers;
    private static final String TAG = "TBroadcastManager";
    private static final boolean SHOULD_PING_PONG = true;
    private static final boolean SHOULD_NOTIFY_INSTANTLY = false;
    private static AtomicBoolean isConnectedToServer = new AtomicBoolean(false);
    private static AtomicBoolean isConnectedTONetwork = new AtomicBoolean(false);
    private static AtomicBoolean isTryingToPingPong = new AtomicBoolean(false);
    private static AtomicBoolean isAlarmManagerSet = new AtomicBoolean(false);
    private static AtomicLong[] lastTriggerTime;
    private static NetworkInfo activeNetInfo;
    private static NetworkInfo activeNetwork;
    private static Object MUTEX = new Object();
    private static final long MAX_LAT = 120000;
    private static final long MIN_LAT = 60000;
    private static final long INTERVAL= 2 * 60 * 1000;
    private static AtomicBoolean isRegistered;
    private static Context context;
    private AlarmManager alarmMgr;

    public TBroadcastManager() {
        TBroadcastManager.isRegistered=new AtomicBoolean(false);
    }

    public TBroadcastManager(Context context) {
        TBroadcastManager.isRegistered=new AtomicBoolean(false);
        initTBM(context);
    }

    private void initTBM(Context context) {
        TBroadcastManager.context = context;
        TBroadcastManager.lastTriggerTime=startTriggerTimer(4);
        doPingPong();
        setAlarm(context);
    }

    private AtomicLong[] startTriggerTimer(int size) {
        AtomicLong[] temp=new AtomicLong[size];
        for(int i=0;i<size;i++){
            temp[i]=new AtomicLong(0);
        }
        return temp;
    }

    private static boolean checkInstantly(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
            return false;
        activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo == null)
            return false;
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    static void registerNetworkObserver(NetworkObserver networkObserver) {
        if (networkObservers == null) {
            networkObservers = new HashSet<>();
        }
        networkObservers.add(networkObserver);
        if (SHOULD_NOTIFY_INSTANTLY)
            networkObserver.onChange(checkInstantly(context));
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
        TBroadcastManager.context = context;
        switch (intent.getAction()) {
            case ConnectivityManager.CONNECTIVITY_ACTION:
                if(checkTriggerTime(lastTriggerTime[0],5))
                    pushUpdateMessage(checkInstantly(context), SHOULD_PING_PONG);
                break;
            case Constants.Action.ACTION_RESTART_SERVICE:
                if(checkTriggerTime(lastTriggerTime[1],5))
                    TUtils.startService(context, TService.class);
                break;
            case Constants.Action.ACTION_ALARM_MANAGER:
                if(checkTriggerTime(lastTriggerTime[2],INTERVAL/2)) {
                    Log.d(TAG, "ALARM : " + Constants.Action.ACTION_ALARM_MANAGER);
                    notifyTService(context);
                }
                break;
        }

    }

    private boolean checkTriggerTime(AtomicLong atomicLong,long interval) {
        synchronized (atomicLong) {
            if (SystemClock.elapsedRealtime() >= atomicLong.get() + interval) {
                atomicLong.set(SystemClock.elapsedRealtime());
                return true;
            }
            Log.d("TBroadcast checkTrigger","not allowed to trigger");
            return false;
        }
    }

    /**
     * notify {@link TService} to push user Activity data and more info
     * @param context
     */
    private void notifyTService(final Context context) {
        try {
            ServiceConnection mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    TService.TBinder binder = (TService.TBinder) service;
                    binder.handleAlarmMangerRequest();
                    context.unbindService(this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG, "Alarm Manager Done!");
                }
            };
            context.bindService(new Intent(context, TService.class), mConnection, Context.BIND_AUTO_CREATE);
        }catch (Exception e){
            TLog.e("TBroadcastManager#notifyService",e);
        }
    }

    private void pushUpdateMessage(boolean state, boolean pingPong) {
        synchronized (MUTEX) {
            isConnectedTONetwork.set(state);
            if (!pingPong) {
                broadcastAll(state);
                isConnectedToServer.set(state);
            } else if (state == true) {
                doPingPong();
            } else {
                broadcastAll(state);
                isConnectedToServer.set(false);
            }
        }

    }

    private void doPingPong() {
        if (context == null)
            return;
        else if (isTryingToPingPong.get())
            return;
        if(!checkTriggerTime(lastTriggerTime[3],30000))
            return;
        resetPingPong();
        ServiceHandler.init(context).pingPong(new HttpResponse() {
            @Override
            public void onServerResponse(JSONObject data) {
                isTryingToPingPong.set(false);
                try {
                    if (data.getInt("Status") == Constants.Code.REQUEST_SUCCESS) {
                        broadcastAll(true);
                        isConnectedToServer.set(true);
                    } else {
                        scheduleJob();
                    }
                } catch (JSONException e) {
                    scheduleJob();
                }
            }

            @Override
            public void onServerError(String message, int code) {
                TLog.d(TAG, "TBroadcastManager connection is true but cant access ping pong! message: " + message + " code: " + code);
                scheduleJob();
            }
        });
    }

    private void resetPingPong() {
        isTryingToPingPong.set(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (isTryingToPingPong!=null && isTryingToPingPong.get()) {
                    isTryingToPingPong.set(false);
                }
                cancel();
            }
        }, 45 * 1000, MAX_LAT);
    }

    private void broadcastAll(boolean b) {
        if (networkObservers != null) {
            ArrayList<NetworkObserver> otherObserver=new ArrayList<>();
            for (NetworkObserver n : networkObservers) {
                if (n != null) {
                    if(n instanceof ServiceHandler)
                        n.onChange(b);
                    else
                        otherObserver.add(n);
                }
            }
            for (NetworkObserver n : otherObserver){
                if(n!=null){
                    n.onChange(b);
                }
            }
        }
    }


    void scheduleJob() {
        if (!isConnectedTONetwork.get())
            return;
        Log.d(TAG, "scheduleJob");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (isConnectedToServer.get())
                    cancel();
                pushUpdateMessage(checkInstantly(context), SHOULD_PING_PONG);
                cancel();
            }
        }, MIN_LAT, MAX_LAT);
    }


    public void setAlarm(Context context) {
        if(isAlarmManagerSet.get())
            return;
        isAlarmManagerSet.set(true);
        Log.d(TAG,"alarmIntent Set!");
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(Constants.Action.ACTION_ALARM_MANAGER);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+INTERVAL,INTERVAL, alarmIntent);
        ComponentName receiver = new ComponentName(context, TBroadcastManager.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public AtomicBoolean getIsRegistered() {
        return isRegistered;
    }
}
