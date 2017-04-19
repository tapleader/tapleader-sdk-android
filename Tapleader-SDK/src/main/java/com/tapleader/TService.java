package com.tapleader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

class TService extends Service {
    private final static String TAG="TService";
    private TBinder binder;

    @Override
    public void onCreate() {
        super.onCreate();
        this.binder = new TBinder();
        TLog.d(TAG,"service created!");
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    public class TBinder extends Binder {

        //Implement your methods here
    }
}
