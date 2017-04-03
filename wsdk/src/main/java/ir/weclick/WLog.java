/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package ir.weclick;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class WLog {
  public static final int LOG_LEVEL_NONE = Integer.MAX_VALUE;
  private static final String TAG ="WLog" ;

  private static int logLevel = Integer.MAX_VALUE;

  /**
   * Sets the level of logging to display, where each level includes all those below it. The default
   * level is {@link #LOG_LEVEL_NONE}. Please ensure this is set to {@link Log#ERROR}
   * or {@link #LOG_LEVEL_NONE} before deploying your app to ensure no sensitive information is
   * logged. The levels are:
   * <ul>
   * <li>{@link Log#VERBOSE}</li>
   * <li>{@link Log#DEBUG}</li>
   * <li>{@link Log#INFO}</li>
   * <li>{@link Log#WARN}</li>
   * <li>{@link Log#ERROR}</li>
   * <li>{@link #LOG_LEVEL_NONE}</li>
   * </ul>
   *
   * @param logLevel
   *          The level of logcat logging that Parse should do.
   */
  public static void setLogLevel(int logLevel) {
    WLog.logLevel = logLevel;
  }

  /**
   * Returns the level of logging that will be displayed.
   */
  public static int getLogLevel() {
    return logLevel;
  }

  private static void log(int messageLogLevel, String tag, String message, Throwable tr) {
    if (messageLogLevel >= logLevel) {
      if (tr == null) {
        Log.println(logLevel, tag, message);
      } else {
        Log.println(logLevel, tag, message + '\n' + Log.getStackTraceString(tr));
      }
    }
  }

  static void v(String tag, String message, Throwable tr) {
    log(Log.VERBOSE, tag, message, tr);
  }

  static void v(String tag, String message) {
    v(tag, message, null);
  }

  static void d(String tag, String message, Throwable tr) {
    log(Log.DEBUG, tag, message, tr);
  }

  static void d(String tag, String message) {
    d(tag, message, null);
  }

  static void i(String tag, String message, Throwable tr) {
    log(Log.INFO, tag, message, tr);
  }

  static void i(String tag, String message) {
    i(tag, message, null);
  }

  static void w(String tag, String message, Throwable tr) {
    log(Log.WARN, tag, message, tr);
  }

  static void w(String tag, String message) {
    w(tag, message, null);
  }

  static void e(String tag, String message, Throwable tr) {
    log(Log.ERROR, tag, message, tr);
  }

  static void e(String tag, String message) {
    e(tag, message, null);
    JSONObject object=new JSONObject();

    try {
      object.put("tag",tag);
      object.put("message",message);
      object.put("date",WUtils.getDateTime());
    } catch (JSONException e) {
      //why do we fall?
    }
    ServiceHandler.init().crashReport(object.toString(), new HttpResponse() {
      @Override
      public void onServerResponse(JSONObject data) {
        if(data!=null)
          WLog.d(TAG,data.toString());
      }

      @Override
      public void onServerError(String message, int code) {
        if(message!=null)
          WLog.d(TAG,message+" code: "+code);
      }
    });
  }
}
