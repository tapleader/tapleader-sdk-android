package com.tapleader;

import java.net.URL;

/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class ServiceHandler implements NetworkObserver {
    private static final String TAG = "ServiceHandler";
    private static boolean isConnected = false;
    private static ServiceHandler mServiceHandler;
    private static OfflineStore offlineStore;

    private ServiceHandler() {
        NetworkManager.init(this);
        offlineStore = OfflineStore.initialize(Tapleader.getApplicationContext());
    }

    static ServiceHandler init() {
        if (mServiceHandler == null)
            mServiceHandler = new ServiceHandler();
        return mServiceHandler;
    }

    private static String urlGen(String api) {
        URL server = Constants.server;
        return server.toString() + api;
    }

    void installNotifier(String body, HttpResponse httpResponse) {
        handleRequest(Constants.Api.NEW_INSTALL, body,true, httpResponse);
    }

    void activityTracking(String body, HttpResponse httpResponse) {
        //handleRequest(Constants.Api.ACTIVITY_TRACKING,body,httpResponse);
    }

    void crashReport(String body, HttpResponse httpResponse) {
        handleRequest(Constants.Api.CRASH_REPORT, body,false, httpResponse);
    }

    void packageUpdate(String body, HttpResponse httpResponse) {
        // handleRequest(Constants.Api.PACKAGE_UPDATE,body,httpResponse);
    }

    void userAccountData(String body, HttpResponse httpResponse) {
        //handleRequest(Constants.Api.USER_ACCOUNT_DATA,body,httpResponse);
    }

    private void handleRequest(String url, String body,Boolean crashReporter, HttpResponse httpResponse) {
        if (isConnected) {
            HttpRequest httpRequest = new HttpRequest(urlGen(url),crashReporter, httpResponse);
            httpRequest.execute(body);
        } else {
            // TODO: 2017-03-01 offline handler should be implemented here
            offlineStore.store(url, body);
            httpResponse.onServerError(Constants.Messages.OFFLINE,Constants.Code.OFFILNE);
        }
    }

    @Override
    public void onChange(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
