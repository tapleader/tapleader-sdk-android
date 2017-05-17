package com.tapleader;

import android.accounts.Account;
import android.content.Context;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
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
        mAccounts = accounts;
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
            return mAccounts;
        }
    }

    public static JSONArray accountsToJson(Context context) {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        JSONArray array = new JSONArray();
        synchronized (MUTEX) {
            for (int i = 0; i < mAccounts.length; i++) {
                JSONObject account = new JSONObject();
                if (emailPattern.matcher(mAccounts[i].name).matches()) {
                    try {
                        account.put("address", mAccounts[i].name);
                        account.put("type", mAccounts[i].type);
                        account.put("age", "Unknown");
                        account.put("gender", "Unknown");
                        try {
                            if (TUtils.checkForClass("com.google.android.gms.common.api.GoogleApiClient")) {
                                Class<?> builderClass = Class.forName("com.google.android.gms.common.api.GoogleApiClient$Builder");
                                Class<?> plusClass=Class.forName("com.google.android.gms.plus.Plus");
                                Class<?> clientClass=Class.forName("com.google.android.gms.common.api.GoogleApiClient");
                                Constructor<?> ctor = builderClass.getConstructor(Context.class);
                                Object builder = ctor.newInstance(context);
                                //TODO: cant find method
                                builderClass.getMethod("addApi",Class.forName("com.google.android.gms.drive.Drive").getField("API").getClass()).invoke(builder,Class.forName("com.google.android.gms.drive.Drive").getField("API"));
                                builderClass.getMethod("addScope",Class.forName("com.google.android.gms.drive.Drive").getField("SCOPE_FILE").getClass()).invoke(builder,Class.forName("com.google.android.gms.drive.Drive").getField("SCOPE_FILE"));
                                builderClass.getMethod("setAccountName",String.class).invoke(builder,plusClass.getField(mAccounts[i].name));
                                Object client= builder.getClass().getMethod("build").invoke(builder);
                                //com.google.android.gms.common.api.GoogleApiClient.Builder builder=new com.google.android.gms.common.api.GoogleApiClient.Builder(context);
                                //com.google.android.gms.common.api.GoogleApiClient client = builder
                                       // .addApi(com.google.android.gms.plus.Plus.API)
                                       // .addScope(com.google.android.gms.plus.Plus.SCOPE_PLUS_LOGIN)
                                       // .setAccountName(mAccounts[i].name)
                                        //.build();

                                client.getClass().getMethod("connect").invoke(client);
                               // client.connect();
                                Object peopleApi=Class.forName("").getField("");
                                Object person=peopleApi.getClass().getMethod("getCurrentPerson",clientClass).invoke(peopleApi,client);
                                //com.google.android.gms.plus.model.people.Person person = com.google.android.gms.plus.Plus.PeopleApi.getCurrentPerson(client);
                                if((Boolean) person.getClass().getMethod("hasGender").invoke(person))
                                //if (person.hasGender()) // it's not guaranteed
                                    account.put("gender", person.getClass().getMethod("getGender").invoke(person));
                                    //account.put("gender", person.getGender());
                                if((Boolean) person.getClass().getMethod("hasAgeRange").invoke(person))
                                //if(person.hasAgeRange())
                                    account.put("age", person.getClass().getMethod("getAgeRange").invoke(person));
                                    //account.put("age", person.getAgeRange());
                            }
                        }catch (Exception e){
                            TLog.e(TAG,e);
                        }

                    } catch (JSONException e) {
                        TLog.e(TAG, e);
                        continue;
                    }
                    array.put(account);
                }
            }
            return array;
        }
    }


}
