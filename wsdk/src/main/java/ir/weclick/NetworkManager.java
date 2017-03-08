package ir.weclick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class NetworkManager  extends BroadcastReceiver {
    private static List<NetworkObserver> networkObservers;
    private static NetworkInfo activeNetInfo;
    private static NetworkInfo mobNetInfo;
    private static NetworkInfo activeNetwork;

    public void onReceive( Context context, Intent intent ) {
        pushUpdateMessage(checkInstantly(context));
    }

    private static boolean checkInstantly(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetInfo = connectivityManager.getActiveNetworkInfo();
        mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        WifiManager manager= (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo info=manager.getConnectionInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    static void init(NetworkObserver networkObserver){
        if(networkObservers==null){
            networkObservers=new ArrayList<>();
        }
        networkObservers.add(networkObserver);
        networkObserver.onChange(checkInstantly(Weclick.getApplicationContext()));
    }

    static void destroy(NetworkObserver networkObserver){
        if(networkObservers!=null){
            if(networkObservers.contains(networkObserver)){
                networkObservers.remove(networkObserver);
            }
        }
    }

    private void pushUpdateMessage(boolean state){
        if(networkObservers!=null){
            for (NetworkObserver n:networkObservers){
                if(n!=null)
                    n.onChange(state);
            }
        }
    }

    static boolean checkInternetAccess(Context context){
        return checkInstantly(context);
    }
}