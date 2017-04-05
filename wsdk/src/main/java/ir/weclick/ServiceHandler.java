package ir.weclick;

import java.net.URL;

/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class ServiceHandler implements NetworkObserver {
    private static boolean isConnected=false;
    private static ServiceHandler mServiceHandler;
    private static OfflineStore offlineStore;
    private ServiceHandler(){
        NetworkManager.init(this);
        offlineStore=OfflineStore.initialize(Weclick.getApplicationContext());
    }

    static ServiceHandler init(){
        if(mServiceHandler==null)
            mServiceHandler=new ServiceHandler();
        return mServiceHandler;
    }

    void installNotifier(HttpResponse httpResponse){
        String body=WUtils.getClientDetails();
        handleRequest(Constants.Api.NEW_INSTALL,body,httpResponse);
    }

    void activityTracking(String body ,HttpResponse httpResponse){
        handleRequest(Constants.Api.ACTIVITY_TRACKING,body,httpResponse);
    }

    void crashReport(String body ,HttpResponse httpResponse){
        //handleRequest(Constants.Api.CRASH_REPORT,body,httpResponse);
    }

    private static String urlGen(String api){
        URL server=Constants.server;
        return server.toString()+"/"+api;
    }

    private void handleRequest(String url,String body,HttpResponse httpResponse){
        if(isConnected) {
            HttpRequest httpRequest = new HttpRequest(urlGen(url), httpResponse);
            httpRequest.execute(body);
        }else {
            // TODO: 2017-03-01 offline handler should be implemented here
            offlineStore.store(url,body);
        }
    }

    @Override
    public void onChange(boolean isConnected) {
        this.isConnected=isConnected;
    }
}
