package com.tapleader;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class ManifestInfo {
    private static final String TAG = "ManifestInfo";

    private static final Object lock = new Object();
    static int versionCode = -1;
    static String versionName = null;
    private static long lastModified = -1;
    private static int iconId = 0;
    private static String displayName = null;


    /**
     * Returns the last time this application's APK was modified on disk. This is a proxy for both
     * version changes and if the APK has been restored from backup onto a different device.
     */
    public static long getLastModified() {
        synchronized (lock) {
            if (lastModified == -1) {
                File apkPath = new File(getContext().getApplicationInfo().sourceDir);
                lastModified = apkPath.lastModified();
            }
        }

        return lastModified;
    }

    /**
     * Returns the version code for this app, as specified by the android:versionCode attribute in the
     * <manifest> element of the manifest.
     */
    public static int getVersionCode() {
        synchronized (lock) {
            if (versionCode == -1) {
                try {
                    versionCode = getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode;
                } catch (NameNotFoundException e) {
                    TLog.e(TAG, "Couldn't find info about own package", e);
                }
            }
        }

        return versionCode;
    }

    /**
     * Returns the version name for this app, as specified by the android:versionName attribute in the
     * <manifest> element of the manifest.
     */
    public static String getVersionName() {
        synchronized (lock) {
            if (versionName == null) {
                try {
                    versionName = getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
                } catch (NameNotFoundException e) {
                    TLog.e(TAG, "Couldn't find info about own package", e);
                }
            }
        }

        return versionName;
    }

    /**
     * Returns the display name of the app used by the app launcher, as specified by the android:label
     * attribute in the <application> element of the manifest.
     */
    public static String getDisplayName(Context context) {
        synchronized (lock) {
            if (displayName == null) {
                ApplicationInfo appInfo = context.getApplicationInfo();
                displayName = context.getPackageManager().getApplicationLabel(appInfo).toString();
            }
        }

        return displayName;
    }

    /**
     * Returns the default icon id used by this application, as specified by the android:icon
     * attribute in the <application> element of the manifest.
     */
    public static int getIconId() {
        synchronized (lock) {
            if (iconId == 0) {
                iconId = getContext().getApplicationInfo().icon;
            }
        }

        return iconId;
    }

    /**
     * Returns whether the given action has an associated receiver defined in the manifest.
     */
    static boolean hasIntentReceiver(String action) {
        return !getIntentReceivers(action).isEmpty();
    }

    /**
     * Returns a list of ResolveInfo objects corresponding to the BroadcastReceivers with Intent Filters
     * specifying the given action within the app's package.
     */
    static List<ResolveInfo> getIntentReceivers(String... actions) {
        Context context = getContext();
        String packageName = context.getPackageName();
        List<ResolveInfo> list = new ArrayList<>();

        for (String action : actions) {
            list.addAll(
                    context.getPackageManager().queryBroadcastReceivers(
                            new Intent(action),
                            PackageManager.GET_INTENT_FILTERS));
        }

        for (int i = list.size() - 1; i >= 0; --i) {
            String receiverPackageName = list.get(i).activityInfo.packageName;
            if (!receiverPackageName.equals(packageName)) {
                list.remove(i);
            }
        }
        return list;
    }

    private static Context getContext() {
        return Tapleader.getApplicationContext();
    }

    private static PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }

    private static ApplicationInfo getApplicationInfo(Context context, int flags) {
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), flags);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * @return A {@link Bundle} if meta-data is specified in AndroidManifest, otherwise null.
     */
    public static Bundle getApplicationMetadata(Context context) {
        ApplicationInfo info = getApplicationInfo(context, PackageManager.GET_META_DATA);

        if (info != null) {
            return info.metaData;
        }
        return null;
    }

    private static PackageInfo getPackageInfo(String name) {
        PackageInfo info = null;

        try {
            info = getPackageManager().getPackageInfo(name, 0);
        } catch (NameNotFoundException e) {
            // do nothing
        }

        return info;
    }

    private static ServiceInfo getServiceInfo(Class<? extends Service> clazz) {
        ServiceInfo info = null;

        try {
            info = getPackageManager().getServiceInfo(new ComponentName(getContext(), clazz), 0);
        } catch (NameNotFoundException e) {
            // do nothing
        }

        return info;
    }

    private static ActivityInfo getReceiverInfo(Class<? extends BroadcastReceiver> clazz) {
        ActivityInfo info = null;

        try {
            info = getPackageManager().getReceiverInfo(new ComponentName(getContext(), clazz), 0);
        } catch (NameNotFoundException e) {
            // do nothing
        }

        return info;
    }

    /**
     * Returns {@code true} if this package has requested all of the listed permissions.
     * <p/>
     * <strong>Note:</strong> This package might have requested all the permissions, but may not
     * be granted all of them.
     */
    static boolean hasRequestedPermissions(Context context, String... permissions) {
        String packageName = context.getPackageName();
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS);
            if (pi.requestedPermissions == null) {
                return false;
            }
            return Arrays.asList(pi.requestedPermissions).containsAll(Arrays.asList(permissions));
        } catch (NameNotFoundException e) {
            TLog.e(TAG, "Couldn't find info about own package", e);
            return false;
        }
    }

    /**
     * Returns {@code true} if this package has been granted all of the listed permissions.
     * <p/>
     * <strong>Note:</strong> This package might have requested all the permissions, but may not
     * be granted all of them.
     */
    static boolean hasGrantedPermissions(Context context, String... permissions) {
        String packageName = context.getPackageName();
        PackageManager packageManager = context.getPackageManager();
        for (String permission : permissions) {
            if (packageManager.checkPermission(permission, packageName) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private static boolean checkResolveInfo(Class<? extends BroadcastReceiver> clazz, List<ResolveInfo> infoList) {
        for (ResolveInfo info : infoList) {
            if (info.activityInfo != null && clazz.getCanonicalName().equals(info.activityInfo.name)) {
                return true;
            }
        }

        return false;
    }

    private static boolean checkReceiver(Class<? extends BroadcastReceiver> clazz, String permission, Intent[] intents) {
        ActivityInfo receiver = getReceiverInfo(clazz);

        if (receiver == null) {
            return false;
        }

        if (permission != null && !permission.equals(receiver.permission)) {
            return false;
        }

        for (Intent intent : intents) {
            List<ResolveInfo> receivers = getPackageManager().queryBroadcastReceivers(intent, 0);
            if (receivers.isEmpty()) {
                return false;
            }

            if (!checkResolveInfo(clazz, receivers)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isGooglePlayServicesAvailable() {
        return Build.VERSION.SDK_INT >= 8 && getPackageInfo("com.google.android.gsf") != null;
    }

    private static ManifestCheckResult ppnsSupportLevel() {
        Context context = getContext();
    /*
     * For backwards compatibility, the only required declaration for PPNS is the declaration of
     * PushService as a <service>. That's the only declaration we checked before adding GCM support.
     */
   /* if (getServiceInfo(PushService.class) == null) {
      return ManifestCheckResult.MISSING_REQUIRED_DECLARATIONS;
    }*/

        String[] optionalPermissions = new String[]{
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.VIBRATE",
                "android.permission.WAKE_LOCK",
                "android.permission.READ_PHONE_STATE",
                "android.permission.RECEIVE_BOOT_COMPLETED"
        };

        if (!hasGrantedPermissions(context, optionalPermissions)) {
            return ManifestCheckResult.MISSING_OPTIONAL_DECLARATIONS;
        }

        String packageName = context.getPackageName();
        Intent[] intents = new Intent[]{
                new Intent("android.intent.action.BOOT_COMPLETED").setPackage(packageName),
                new Intent("android.intent.action.USER_PRESENT").setPackage(packageName)
        };

        return ManifestCheckResult.HAS_ALL_DECLARATIONS;
    }

    private static String getPpnsManifestMessage() {
        return "make sure that these permissions are declared as children of the root " +
                "<manifest> element:\n" +
                "\n" +
                "<uses-permission android:name=\"android.permission.INTERNET\" />\n" +
                "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />\n" +
                "<uses-permission android:name=\"android.permission.RECEIVE_BOOT_COMPLETED\" />\n" +
                "<uses-permission android:name=\"android.permission.READ_PHONE_STATE\" />\n" +
                "\n" +
                "Also, please make sure that these services and broadcast receivers are declared as " +
                "children of the <application> element:\n" +
                "\n";
    }

    enum ManifestCheckResult {
        /*
         * Manifest has all required and optional declarations necessary to support this push service.
         */
        HAS_ALL_DECLARATIONS,

        /*
         * Manifest has all required declarations to support this push service, but is missing some
         * optional declarations.
         */
        MISSING_OPTIONAL_DECLARATIONS,

        /*
         * Manifest doesn't have enough required declarations to support this push service.
         */
        MISSING_REQUIRED_DECLARATIONS
    }
}
