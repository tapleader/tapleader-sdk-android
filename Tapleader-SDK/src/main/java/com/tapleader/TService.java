package com.tapleader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class TService extends Service implements NetworkObserver {
    private static AtomicBoolean isConnectedToNet=new AtomicBoolean(false);
    private static ServiceHandler mServiceHandler;
    private static OfflineStore mOfflineStore;
    private final static String TAG="TService";
    private final static boolean FORCE_RESTART = true;
    private TBinder binder;
    @Override
    public void onCreate() {
        super.onCreate();
        this.binder = new TBinder();
        TBroadcastManager.registerNetworkObserver(this);
        mServiceHandler=ServiceHandler.init(getApplicationContext());
        mOfflineStore=OfflineStore.initialize(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
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
        isConnectedToNet.set(isConnected);
        if(isConnected){
            binder.handleRequests(mOfflineStore.getAllRequests());
        }
    }


    public class TBinder extends Binder {
        /**
         * use {@link TBinder#handleRequests(ArrayList)}
         * @param requests
         */
        @Deprecated
        public void handleRequests(JSONArray requests) {
            if(requests==null)
                return;
            for(int i=0;i<requests.length();i++){
                String path = null;
                String body= null;
                try {
                    JSONObject result=requests.getJSONObject(i);
                    path = result.getString("path");
                    body=result.getString("body");
                } catch (JSONException e) {
                    TLog.e(TAG, e);
                    continue;
                }
                if(path==null)
                    continue;
                if (path.equals(Constants.Api.NEW_INSTALL)) {
                    if (TUtils.shouldNotifyInstall()) {
                        mServiceHandler.installNotifier(TUtils.getClientDetails(),TOfflineResponse.installResponse);
                    }
                }else if(path.equals(Constants.Api.ACTIVITY_TRACKING)) {
                    mServiceHandler.activityTracking(body,TOfflineResponse.activityTrackingResponse);

                }
            }
        }

        /**
         * push offline Request to server
         * @param records
         * @since version 1.1.4
         */
        public void handleRequests(ArrayList<TModels.TOfflineRecord> records){
            if(records==null)
                return;
            else if(records.size()==0)
                return;
            else {
                for(TModels.TOfflineRecord record:records){
                    if(record!=null){
                        switch (record.getPath()){
                            case Constants.Api.NEW_INSTALL:
                                mServiceHandler.installNotifier(record.getBody(),TOfflineResponse.installResponse);
                                break;
                            case Constants.Api.ACTIVITY_TRACKING:
                                mServiceHandler.activityTracking(record.getBody(),TOfflineResponse.activityTrackingResponse);
                                break;
                        }
                    }
                }
            }
        }
    }
}
