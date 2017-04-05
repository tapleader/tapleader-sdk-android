package ir.weclick;

import android.content.Context;

import java.io.File;

/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class WPlugins {

  private static final String INSTALLATION_ID_LOCATION = "installationId";

  private static final Object LOCK = new Object();
  private static WPlugins instance;

  // TODO(grantland): Move towards a Config/Builder parameter pattern to allow other configurations
  // such as path (disabled for Android), etc.
  static void initialize(String applicationId, String clientKey,String deviceId) {
    WPlugins.set(new WPlugins(applicationId, clientKey,deviceId));
  }

  static void set(WPlugins plugins) {
    synchronized (LOCK) {
      if (instance == null) {
        instance = plugins;
      }
    }
  }

  static WPlugins get() {
    synchronized (LOCK) {
      return instance;
    }
  }

  static void reset() {
    synchronized (LOCK) {
      instance = null;
    }
  }

  final Object lock = new Object();
  private final String applicationId;
  private final String clientKey;
  private final String deviceId;

  protected File weclickDir;
  protected File cacheDir;
  protected File filesDir;

  private WPlugins(String applicationId, String clientKey,String deviceId) {
    this.applicationId = applicationId;
    this.clientKey = clientKey;
    this.deviceId=deviceId;
  }

  String getApplicationId() {
    return applicationId;
  }

  String getClientKey() {
    return clientKey;
  }

  String getDeviceId(){
    return deviceId;
  }



  @Deprecated
  File getweclickDir() {
    throw new IllegalStateException("Stub");
  }

  File getCacheDir() {
    throw new IllegalStateException("Stub");
  }

  File getFilesDir() {
    throw new IllegalStateException("Stub");
  }

  static class Android extends WPlugins {
    static void initialize(Context context, String applicationId, String clientKey,String deviceId) {
      WPlugins.set(new Android(context, applicationId, clientKey,deviceId));
    }

    static Android get() {
      return (Android) WPlugins.get();
    }

    private final Context applicationContext;

    private Android(Context context, String applicationId, String clientKey,String deviceId) {
      super(applicationId, clientKey,deviceId);
      applicationContext = context.getApplicationContext();
    }

    Context applicationContext() {
      return applicationContext;
    }

    @Override @SuppressWarnings("deprecation")
    File getweclickDir() {
      synchronized (lock) {
        if (weclickDir == null) {
          weclickDir = applicationContext.getDir("weclick", Context.MODE_PRIVATE);
        }
        return createFileDir(weclickDir);
      }
    }

    @Override
    File getCacheDir() {
      synchronized (lock) {
        if (cacheDir == null) {
          cacheDir = new File(applicationContext.getCacheDir(), "ir.weclick");
        }
        return createFileDir(cacheDir);
      }
    }

    @Override
    File getFilesDir() {
      synchronized (lock) {
        if (filesDir == null) {
          filesDir = new File(applicationContext.getFilesDir(), "ir.weclick");
        }
        return createFileDir(filesDir);
      }
    }
  }

  private static File createFileDir(File file) {
    if (!file.exists()) {
      if (!file.mkdirs()) {
        return file;
      }
    }
    return file;
  }
}
