package com.tapleader;

import android.content.Context;

/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class ServiceHandler implements NetworkObserver {
    private static final String TAG = "ServiceHandler";
    private static boolean isConnected = false;
    private static ServiceHandler mServiceHandler;
    private static OfflineStore offlineStore;
    private static Context context;

    private ServiceHandler(Context context) {
        this.isConnected=false;
        this.context=context;
        TBroadcastManager.registerNetworkObserver(this);
        offlineStore = OfflineStore.initialize(context);
    }

    static ServiceHandler init(Context context) {
        if (mServiceHandler == null)
            mServiceHandler = new ServiceHandler(context);
        TLog.d(TAG,"init");
        return mServiceHandler;
    }

    private static String urlGen(String api) {
        return Constants.server + api;
    }

    void installNotifier(String body, HttpResponse httpResponse) {
        handleRequest(Constants.Api.NEW_INSTALL, body,true,true, httpResponse);
    }

    void activityTracking(String body, HttpResponse httpResponse) {
        handleRequest(Constants.Api.ACTIVITY_TRACKING,body,true,true,httpResponse);
    }

    void crashReport(String body, HttpResponse httpResponse) {
        handleRequest(Constants.Api.CRASH_REPORT, body,false,false, httpResponse);
    }

    void packageUpdate(String body, HttpResponse httpResponse) {
        // handleRequest(Constants.Api.PACKAGE_UPDATE,body,httpResponse);
    }

    void userAccountData(String body, HttpResponse httpResponse) {
        //handleRequest(Constants.Api.USER_ACCOUNT_DATA,body,httpResponse);
    }

    void pingPong(HttpResponse httpResponse){
        HttpRequest httpRequest = new HttpRequest(urlGen(Constants.Api.PING_PONG),false, httpResponse);
        httpRequest.execute();
    }

    private void handleRequest(String url, String body,Boolean crashReporter,boolean supportOffilne, HttpResponse httpResponse) {
        if (isConnected) {
            HttpRequest httpRequest = new HttpRequest(urlGen(url),crashReporter, httpResponse);
            httpRequest.execute(body);
        } else if(supportOffilne) {
            // TODO: 2017-03-01 offline handler should be implemented here
            //offlineStore.store(url, body);
            TModels.TOfflineRecord record=new TModels.TOfflineRecord();
            record.setDate(TUtils.getDateTime());
            record.setPath(url);
            record.setBody(body);
            long id=offlineStore.store(record);
            httpResponse.onServerError(Constants.Messages.OFFLINE,Constants.Code.OFFILNE);
        }
    }

    @Override
    public void onChange(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        TBroadcastManager.destroyNetworkObserver(this);
    }
}
