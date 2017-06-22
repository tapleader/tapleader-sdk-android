package com.tapleader;

import android.app.AlarmManager;
import android.app.Service;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class TService extends Service implements NetworkObserver {

    private static AtomicBoolean isConnectedToNet=new AtomicBoolean(false);
    private static ServiceHandler mServiceHandler;
    private static OfflineStore mOfflineStore;
    private final static String TAG="TService";
    private final static boolean FORCE_RESTART = true;
    private static long INTERVAL=AlarmManager.INTERVAL_HOUR;
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

    @Override
    public void onTrimMemory(int level) {
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                //Release any UI objects that currently hold memory.
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                INTERVAL=2*AlarmManager.INTERVAL_HOUR;
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                //Release as much memory as the process can.
                break;
            default:
                //Release any non-critical data structures.
                break;
        }

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
                            case Constants.Endpoint.NEW_INSTALL:
                                if(TUtils.shouldNotifyInstall(TService.this) && !installNotifyTry.get()) {
                                    installNotifyTry.set(true);
                                    mServiceHandler.installNotifier(record.getBody(), TOfflineResponse.initialize(record.getId(), TService.this).getInstallResponse());
                                }
                                break;
                            case Constants.Endpoint.ACTIVITY_TRACKING:
                                mServiceHandler.activityTracking(record.getBody(),TOfflineResponse.initialize(record.getId(),TService.this).getActivityTrackingResponse());
                                break;
                            case Constants.Endpoint.SECOND_LAUNCH:
                                mServiceHandler.retention(record.getBody(),TOfflineResponse.initialize(record.getId(),TService.this).getRetentionResponse());
                                break;
                            default:
                                if(record.getPath().equals(Constants.Endpoint.NEW_INSTALL.concat("/"+TUtils.getInstallationId(TService.this)))){
                                    mServiceHandler.sendMoreInfo(record.getBody(),TOfflineResponse.initialize(record.getId(),TService.this).getMoreInfoResponse());
                                }
                        }
                    }
                }
            }
        }

        public void handleAlarmMangerRequest(){
            final int BUFFER_SIZE = 10;
            if(isConnectedToNet.get()){
                final TSQLHelper helper=new TSQLHelper(TService.this);
                int recordCount = helper.getActivityLifecycleCount();
                long currentTime=System.currentTimeMillis();
                long lastTime=TUtils.getLastPushActivityLogTime(TService.this);
                if(recordCount>BUFFER_SIZE
                        || (recordCount>0 && currentTime-lastTime>INTERVAL)){
                    final JSONObject body=new JSONObject();
                    try {
                        body.put("clientKey",helper.getSetting(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CLIENT_KEY));
                        body.put("packageName",helper.getSetting(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_PCKG_NAME));
                        body.put("InstallationId",TUtils.getInstallationId(TService.this));
                        body.put("data",helper.getActivityLifeCycle());
                        helper.truncateActivityLifeCycle();
                    } catch (JSONException e) {
                            TLog.e(TAG,e,TService.this);
                    }
                    mServiceHandler.activityTracking(body.toString(), new HttpResponse() {
                        @Override
                        public void onServerResponse(JSONObject data) {
                            try {
                                int status = data.getInt("Status");
                                if (status == Constants.Code.REQUEST_SUCCESS) {
                                    TUtils.updateLastPushActivityLogTime(TService.this,System.currentTimeMillis());
                                }
                            } catch (JSONException e) {
                                TLog.e(TAG,e,TService.this);
                            }
                        }

                        @Override
                        public void onServerError(String message, int code) {
                            TModels.TOfflineRecord record=new TModels.TOfflineRecord();
                            record.setBody(body.toString());
                            record.setPath(Constants.Endpoint.ACTIVITY_TRACKING);
                            record.setDate(TUtils.getDateTime());
                            helper.insertNewOfflineRecord(record);
                        }
                    });
                }
            }
        }
    }
}
