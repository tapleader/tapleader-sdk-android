package com.tapleader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class TService extends Service implements NetworkObserver {
    private static AtomicBoolean isConnectedToNet=new AtomicBoolean(false);
    private static ServiceHandler mServiceHandler;
    private static OfflineStore mOfflineStore;
    private final static String TAG="TService";
    private final static boolean FORCE_RESTART = true;
    private TBinder binder;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        mServiceHandler=ServiceHandler.init(this);
        //if(getApplicationContext()!=null)
        mOfflineStore=OfflineStore.initialize(this);
        this.binder = new TBinder();
        TBroadcastManager.registerNetworkObserver(this);
        Log.d(TAG,"mServiceHandler: "+(mServiceHandler==null));
        Log.d(TAG,"mOfflineStore: "+(mOfflineStore==null));
        Tapleader.initializeTBroadcastReceiver(this);
        Log.d(TAG,"initializeTBroadcastReceiver");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG,"onTaskRemoved");
        restart();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        TBroadcastManager.destroyNetworkObserver(this);
        restart();

    }

    private void restart(){
        if (FORCE_RESTART) {
            Log.d(TAG,"restart");
            Intent intent = new Intent(Constants.Action.ACTION_RESTART_SERVICE);
            sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onChange(boolean isConnected) {
        Log.d(TAG,"onChange");
        isConnectedToNet.set(isConnected);
        if(isConnected && mOfflineStore!=null){
            binder.handleRequests(mOfflineStore.getAllRequests());
        }
    }


    public class TBinder extends Binder {

        /**
         * push offline Request to server
         * @param records
         * @since version 1.1.4
         */
        public void handleRequests(ArrayList<TModels.TOfflineRecord> records){
            Log.d(TAG,"handleRequests");
            if(records==null)
                return;
            else if(records.size()==0)
                Log.d(TAG,"record list size= 0");
            else {
                for(TModels.TOfflineRecord record:records){
                    if(record!=null){
                        TLog.d(TAG,record.toString());
                        switch (record.getPath()){
                            case Constants.Api.NEW_INSTALL:
                                if(TUtils.shouldNotifyInstall())
                                    mServiceHandler.installNotifier(record.getBody(),TOfflineResponse.initialize(record.getId()).getInstallResponse());
                                break;
                            case Constants.Api.ACTIVITY_TRACKING:
                                mServiceHandler.activityTracking(record.getBody(),TOfflineResponse.initialize(record.getId()).getActivityTrackingResponse());
                                break;
                        }
                    }
                }
            }
        }
    }
}
