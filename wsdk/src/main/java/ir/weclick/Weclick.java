package ir.weclick;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

public class Weclick{
    private static final Object MUTEX = new Object();
    private static OfflineStore offlineStore;
    private static ServiceHandler serviceHandler;
    private static final String WECLICK_APPLICATION_ID = "ir.weclick.APPLICATION_ID";
    private static final String WECLICK_CLIENT_KEY = "ir.weclick.CLIENT_KEY";

    public final static class Configuration {

        final Context context;
        final String applicationId;
        final String clientKey;
        final String server;
        final boolean localDataStoreEnabled;

        public static final class Builder {
            private Context context;
            private String applicationId;
            private String clientKey;
            private String server = "http://e.weclick.ir/api/developer";
            private boolean localDataStoreEnabled;

            public Builder(Context context) {
                this.context = context;

                if (context != null) {
                    localDataStoreEnabled=!NetworkManager.checkInternetAccess(context);
                    Context applicationContext = context.getApplicationContext();
                    Bundle metaData = ManifestInfo.getApplicationMetadata(applicationContext);
                    if (metaData != null) {
                        applicationId = metaData.getString(WECLICK_APPLICATION_ID);
                        clientKey = metaData.getString(WECLICK_CLIENT_KEY);
                    }
                }
            }

            public Builder applicationId(String applicationId) {
                this.applicationId = applicationId;
                return this;
            }

            /**
             * Set the client key to be used by Weclick.
             *
             * This method is only required if you intend to use a different {@code getClientKey} than
             * is defined by {@code ir.weclick.CLIENT_KEY} in your {@code AndroidManifest.xml}.
             *
             * @param clientKey The client key to set.
             * @return The same builder, for easy chaining.
             */
            public Builder clientKey(String clientKey) {
                this.clientKey = clientKey;
                return this;
            }


            public Configuration build() {
                return new Configuration(this);
            }

        }

        private Configuration(Builder builder) {
            this.context = builder.context;
            this.applicationId = builder.applicationId;
            this.clientKey = builder.clientKey;
            this.server = builder.server;
            this.localDataStoreEnabled = builder.localDataStoreEnabled;
        }
    }

    /**
     * read Application_ID and Client_ID from metadata in Manifest.xml file
     * @param context
     */
    public static void initialize(Context context) {
        Configuration.Builder builder = new Configuration.Builder(context);
        if (builder.applicationId == null) {
            throw new RuntimeException(Constants.Exception.APPLICATIONID_NOT_FOUND);
        } if (builder.clientKey == null) {
            throw new RuntimeException(Constants.Exception.CLIENTKEY_NOT_FOUND);
        }
        initialize(builder.build());
    }

    public static void initialize(Context context, String applicationId, String clientKey) {
        initialize(new Configuration.Builder(context)
                .applicationId(applicationId)
                .clientKey(clientKey)
                .build()
        );
    }


    public static void initialize(Configuration configuration) {
        //TODO: remove this line for release
        WLog.setLogLevel(Log.VERBOSE);
        String deviceId="PERMISSION_NOT_GRANTED";
        if(WUtils.checkPermission(configuration.context,Constants.Permission.READ_PHONE_STATE))
            deviceId = ((TelephonyManager)configuration.context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        WPlugins.Android.initialize(configuration.context, configuration.applicationId, configuration.clientKey,deviceId);
        WUtils.registerLifecycleHandler(configuration.context);
        initializeNetwoekManager(configuration.context);
        serviceHandler=ServiceHandler.init();

        try {
            Constants.server = new URL(configuration.server);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }

        offlineStore = OfflineStore.initialize(configuration.context);
        WKeyValueCache.initialize(configuration.context);

        // Make sure the data on disk for Parse is for the current
        // application.
        checkCacheApplicationId();

    }

    static Context getApplicationContext() {
        checkContext();
        return WPlugins.Android.get().applicationContext();
    }

    static void checkContext() {
        if (WPlugins.Android.get().applicationContext() == null) {
            throw new RuntimeException(Constants.Exception.NULL_CONTEXT);
        }
    }

    static boolean hasPermission(String permission) {
        return (getApplicationContext().checkCallingOrSelfPermission(permission) ==
                PackageManager.PERMISSION_GRANTED);
    }

    static void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            throw new IllegalStateException(String.format(Constants.Exception.PERMISSION_DENID,permission));
        }
    }

    static void initializeNetwoekManager(Context context){
        String action="android.net.conn.CONNECTIVITY_CHANGE";
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        context.registerReceiver(new NetworkManager(),filter);
    }

    static void checkCacheApplicationId() {
        synchronized (MUTEX) {
            String applicationId = WPlugins.get().getApplicationId();
            if (applicationId != null) {
                File dir = Weclick.getWeclickCacheDir();
                // Make sure the current version of the cache is for this application id.
                File applicationIdFile = new File(dir, "getApplicationId");
                if (applicationIdFile.exists()) {
                    // Read the file
                    boolean matches = false;
                    try {
                        RandomAccessFile f = new RandomAccessFile(applicationIdFile, "r");
                        byte[] bytes = new byte[(int) f.length()];
                        f.readFully(bytes);
                        f.close();
                        String diskApplicationId = new String(bytes, "UTF-8");
                        matches = diskApplicationId.equals(applicationId);
                    } catch (FileNotFoundException e) {
                        // Well, it existed a minute ago. Let's assume it doesn't match.
                    } catch (IOException e) {
                        // Hmm, the getApplicationId file was malformed or something. Assume it
                        // doesn't match.
                    }

                    // The application id has changed, so everything on disk is invalid.
                    if (!matches) {
                        try {
                            WFileUtils.deleteDirectory(dir);
                        } catch (IOException e) {
                            // We're unable to delete the directy...
                        }
                    }
                }else {
                    serviceHandler.installNotifier(new HttpResponse() {
                        @Override
                        public void onServerResponse(JSONObject data) {

                            WLog.d("Weclick",data.toString());
                        }

                        @Override
                        public void onServerError(String message, int code) {

                        }
                    });
                }

                // Create the version file if needed.
                applicationIdFile = new File(dir, "getApplicationId");
                try {
                    FileOutputStream out = new FileOutputStream(applicationIdFile);
                    out.write(applicationId.getBytes("UTF-8"));
                    out.close();
                } catch (FileNotFoundException e) {
                    // Nothing we can really do about it.
                } catch (UnsupportedEncodingException e) {
                    // Nothing we can really do about it. This would mean Java doesn't
                    // understand UTF-8, which is unlikely.
                } catch (IOException e) {
                    // Nothing we can really do about it.
                }
            }
        }
    }

    static File getWeclickCacheDir() {
        return WPlugins.get().getCacheDir();
    }

    static File getWeclickCacheDir(String subDir) {
        synchronized (MUTEX) {
            File dir = new File(getWeclickCacheDir(), subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }
    }

    static File getWeclickFilesDir() {
        return WPlugins.get().getFilesDir();
    }

    static File getWeclickFilesDir(String subDir) {
        synchronized (MUTEX) {
            File dir = new File(getWeclickFilesDir(), subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }
    }

    /**
     * Java's object cloning mechanism can allow an attacker to manufacture new instances of classes you define
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Sorry but we can't let you to clone Weclick!");
    }


    /**
     * we are greater than everything dude!
     * @param obj
     * @return
     */
    @Override
    public final boolean equals(Object obj) {
        return false;
    }

    /**
     * Serialization is dangerous because it allows adversaries to get their hands on the internal state
     * of class.
     * An adversary can serialize class into a byte array that can be read and
     * This allows the adversary to inspect the full internal state of your object
     * including any fields we marked private
     * and including the internal state of any objects.
     * @param out
     * @throws java.io.IOException
     */
    private final void writeObject(ObjectOutputStream out)
            throws java.io.IOException {
        throw new java.io.IOException("Sorry but Weclick cannot be serialized!");
    }

    /**
     * An adversary can create a sequence of bytes that happens to deserialize to an instance of your class.
     * This is dangerous, since you do not have control over what state the deserialized object is in.
     * @param in
     * @throws java.io.IOException
     */
    private final void readObject(ObjectInputStream in)
            throws java.io.IOException {
        throw new java.io.IOException("Class cannot be deserialized");
    }
}
