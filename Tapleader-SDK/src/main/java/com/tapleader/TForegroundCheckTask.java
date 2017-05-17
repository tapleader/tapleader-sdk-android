package com.tapleader;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by mehdi akbarian on 2017-05-10.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

public class TForegroundCheckTask extends AsyncTask<Context,Void,Boolean> {
    @Override
    protected Boolean doInBackground(Context... params) {
        Context context=params[0];
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
