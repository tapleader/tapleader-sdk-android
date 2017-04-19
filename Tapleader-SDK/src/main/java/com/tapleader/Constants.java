package com.tapleader;

import java.net.URL;

/**
 * Created by mehdi akbarian on 2017-02-27.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class Constants {

    static URL server;

    class Messages {
        static final String REQUEST_TIMEOUT = "Request is timed out locally!";
        static final String OFFLINE="no access to internet!request is recorded and will commit after connecting!";
    }

    class Code {
        static final int REQUEST_TIMEOUT = 100;
        static final int REQUEST_SUCCESS = 0;
        static final int REQUEST_ERROR = -100;
        static final int WRONG_CAMPAIGN_ID = -1;
        static final int NOT_COMPATIBLE_APPLICATION_ID_AND_CLIENT_KEY = -2;
        static final int OFFILNE=-99;
    }

    class Exception {
        public static final String PERMISSION_DENID = "To use this functionality, add this to your AndroidManifest.xml:\n"
                + "<uses-permission android:name=\"" + "%s" + "\" />";
        public static final String ACTIVITY_LOG_NOT_FOUND = "flush requested for Activity logging but log file not found!";
        public static final String REIITILIZE_NOT_ALLOWED = "class was initialized! you must reset if you want to reinitialize!";
        static final String APPLICATIONID_NOT_FOUND = "ApplicationId not defined. " +
                "You must provide ApplicationId in AndroidManifest.xml.\n" +
                "<meta-data\n" +
                "    android:name=\"com.tapleader.APPLICATION_ID\"\n" +
                "    android:value=\"<Your Application Id>\" />";
        static final String CLIENTKEY_NOT_FOUND = "ClientKey not defined. " +
                "You must provide ClientKey in AndroidManifest.xml.\n" +
                "<meta-data\n" +
                "    android:name=\"com.tapleader.CLIENT_KEY\"\n" +
                "    android:value=\"<Your Client Key>\" />";
        static final String CAMPAIGN_ID_NOT_FOUND = "CampaignId not defined. " +
                "You must provide CampaignId in AndroidManifest.xml.\n" +
                "<meta-data\n" +
                "    android:name=\"com.tapleader.CAMPAIGN_ID\"\n" +
                "    android:value=\"<Your CAMPAIGN ID>\" />";
        static final String NULL_CONTEXT = "applicationContext is null. "
                + "You must call Parse.initialize(Context)"
                + " before using the Parse library.";
    }

    class Api {
        static final String USER_ACCOUNT_DATA = "userAccountData";
        static final String ACTIVITY_TRACKING = "testpost";
        static final String PACKAGE_UPDATE = "update";
        static final String NEW_INSTALL = "install";
        static final String CRASH_REPORT = "crash";
    }

    class Permission {
        static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
        static final String GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";
    }

    class Preferences {
        static final String  PREFS_NAME = "App_info";
        static final String INSTALL_PARAMETER_NAME = "n_install";
        static final String PACKAGE_VERSION_NAME = "p_version_name";
        static final String PACKAGE_VERSION_CODE = "p_version_code";
        static final String USER_INSTALLATION_ID = "p_user_install_id";
    }


}
