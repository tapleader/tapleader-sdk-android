package com.tapleader;

import android.accounts.Account;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * Created by mehdi akbarian on 2017-04-09.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TgAccountUtils {

    private static final String TAG = "TgAccountUtils";
    private static final Object MUTEX = new Object();
    private static TgAccountUtils mTgAccountUtils;
    private static Account[] mAccounts;

    private TgAccountUtils(Account[] accounts) {
        this.mAccounts = accounts;
    }

    public static void initialize(Account[] accounts) {
        synchronized (MUTEX) {
            if (mTgAccountUtils == null) {
                mTgAccountUtils = new TgAccountUtils(accounts);
            }
        }
    }

    public static void reset() {
        synchronized (MUTEX) {
            mTgAccountUtils = null;
        }
    }

    public static Account[] getAccounts() {
        synchronized (MUTEX) {
            return mTgAccountUtils.mAccounts;
        }
    }

    public static JSONArray accountsToJson() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        JSONArray array = new JSONArray();
        synchronized (MUTEX) {
            for (int i = 0; i < mAccounts.length; i++) {
                JSONObject account = new JSONObject();
                if (emailPattern.matcher(mAccounts[i].name).matches()) {
                    try {
                        account.put("address", mAccounts[i].name);
                        account.put("type", mAccounts[i].type);
                        if (TUtils.checkForClass("com.google.android.gms.common.api.GoogleApiClient")) {
                            //TODO:get gender and age from google play services
                        } else {
                            account.put("age", "Unknown");
                            account.put("gender", "Unknown");
                        }
                    } catch (JSONException e) {
                        TLog.e(TAG, e.getMessage());
                        continue;
                    }
                    array.put(account);
                }
            }
            return array;
        }
    }


}
