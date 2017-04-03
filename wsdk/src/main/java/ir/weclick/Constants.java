package ir.weclick;

import java.net.URL;

/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class Constants {

    static URL server;

    class Messages{
        static final String REQUEST_TIMEOUT="Request is timed out locally!";
    }

    class Code{
        static final int REQUEST_TIMEOUT=100;
    }

    class Exception{
        static final String APPLICATIONID_NOT_FOUND="ApplicationId not defined. " +
                "You must provide ApplicationId in AndroidManifest.xml.\n" +
                "<meta-data\n" +
                "    android:name=\"ir.weclick.APPLICATION_ID\"\n" +
                "    android:value=\"<Your Application Id>\" />";
        static final String CLIENTKEY_NOT_FOUND="ClientKey not defined. " +
                "You must provide ClientKey in AndroidManifest.xml.\n" +
                "<meta-data\n" +
                "    android:name=\"ir.weclick.CLIENT_KEY\"\n" +
                "    android:value=\"<Your Client Key>\" />";
        static final String NULL_CONTEXT="applicationContext is null. "
                + "You must call Parse.initialize(Context)"
                + " before using the Parse library.";
        public static final String PERMISSION_DENID ="To use this functionality, add this to your AndroidManifest.xml:\n"
                + "<uses-permission android:name=\"" +"%s"+ "\" />" ;
        public static final String ACTIVITY_LOG_NOT_FOUND="flush requested for Activity logging but log file not found!";
    }

    class Api{
        static final String NEW_INSTALL="install";
        static final String ACTIVITY_TRACKING="activityTracking";
        static final String CRASH_REPORT="crash";
    }



}
