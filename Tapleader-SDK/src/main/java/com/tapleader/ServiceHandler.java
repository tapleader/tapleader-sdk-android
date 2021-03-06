package com.tapleader;

import android.content.Context;
import android.util.Log;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
        isConnected=false;
        ServiceHandler.context =context;
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
        handleRequest(Constants.Endpoint.NEW_INSTALL, body,true,true, httpResponse);
    }

    void activityTracking(String body, HttpResponse httpResponse) {
        handleRequest(Constants.Endpoint.ACTIVITY_TRACKING,body,true,true,httpResponse);
    }

    void crashReport(String body, HttpResponse httpResponse) {
        handleRequest(Constants.Endpoint.CRASH_REPORT, body,false,false, httpResponse);
    }

    void packageUpdate(String body, HttpResponse httpResponse) {
        // handleRequest(Constants.Endpoint.PACKAGE_UPDATE,body,httpResponse);
    }

    void userAccountData(String body, HttpResponse httpResponse) {
        //handleRequest(Constants.Endpoint.USER_ACCOUNT_DATA,body,httpResponse);
    }

    void invokeEvent(String body,HttpResponse httpResponse){
        HttpRequest httpRequest = new HttpRequest(urlGen(Constants.Endpoint.EVENT),false, httpResponse);
        httpRequest.execute(body);
        if(Tapleader.DEBUG_MODE)
            sendAnalytics(urlGen(Constants.Endpoint.EVENT),body,"invoke event method", Calendar.getInstance().toString());

    }

    void pingPong(HttpResponse httpResponse){
        HttpRequest httpRequest = new HttpRequest(urlGen(Constants.Endpoint.PING_PONG),false, httpResponse);
        httpRequest.execute("ping-pong");
    }

    void sendMoreInfo(String body, HttpResponse httpResponse){
        String installId=TUtils.getInstallationId(context);
        handleRequest(Constants.Endpoint.NEW_INSTALL+"/"+installId, body,true,true, httpResponse);
    }

    void retention(String body,HttpResponse httpResponse){
       handleRequest(Constants.Endpoint.SECOND_LAUNCH,body,true,true,httpResponse);
    }
    private void handleRequest(String url, String body,boolean crashReporter,boolean supportOffline, HttpResponse httpResponse) {
        if (isConnected) {
            HttpRequest httpRequest = new HttpRequest(urlGen(url),crashReporter, httpResponse);
            httpRequest.execute(body);
        } else if(supportOffline) {
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
        ServiceHandler.isConnected = isConnected;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        TBroadcastManager.destroyNetworkObserver(this);
    }

    private void sendAnalytics(String url, String body, String s, String s1) {
        Map<String, String> articleParams = new HashMap<String, String>();

        articleParams.put("url", url);
        articleParams.put("body", body);
        articleParams.put("details", s);
        articleParams.put("time", s1);

        StackTraceElement[] elements=Thread.currentThread().getStackTrace();
        String report="";

        for(StackTraceElement stackTraceElement:elements){
            report=report.concat(stackTraceElement.getFileName()
            +" => "+stackTraceElement.getClassName()
            +" => "+stackTraceElement.getMethodName()
            +" => "+stackTraceElement.getLineNumber()
            +"\n===============================\n");
        }

        articleParams.put("stacktrace",report);

        Log.d("ServiceHandler",report);

        //FlurryAgent.logEvent("ServiceHandler.java", articleParams);
    }
}
