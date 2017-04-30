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
        mServiceHandler=ServiceHandler.init(this);
        mOfflineStore=OfflineStore.initialize(this);
        TPlugins.refresh(this);
        this.binder = new TBinder();
        TBroadcastManager.registerNetworkObserver(this);
        Tapleader.initializeTBroadcastReceiver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        TBroadcastManager.destroyNetworkObserver(this);
        restart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                AtomicBoolean installNotifyTry=new AtomicBoolean(false);
                for(TModels.TOfflineRecord record:records){
                    if(record!=null){
                        Log.d(TAG,record.toString());
                        switch (record.getPath()){
                            case Constants.Api.NEW_INSTALL:
                                if(TUtils.shouldNotifyInstall(TService.this) && !installNotifyTry.get()) {
                                    installNotifyTry.set(true);
                                    mServiceHandler.installNotifier(record.getBody(), TOfflineResponse.initialize(record.getId(), TService.this).getInstallResponse());
                                }
                                break;
                            case Constants.Api.ACTIVITY_TRACKING:
                                mServiceHandler.activityTracking(record.getBody(),TOfflineResponse.initialize(record.getId(),TService.this).getActivityTrackingResponse());
                                break;
                        }
                    }
                }
            }
        }
    }
}
