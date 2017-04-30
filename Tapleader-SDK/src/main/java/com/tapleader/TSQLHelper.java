package com.tapleader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by mehdi akbarian on 2017-04-23.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TSQLHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "tapleader_offline.db";
    static final String TAG = "TSQLHelper";
    static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_OFFLINE_RECORD_TABLE =
            "CREATE TABLE " + TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME + " (" +
                    TModels.TOfflineRecord.TOfflineRecordEntity._ID + " INTEGER PRIMARY KEY," +
                    TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_PATH + " TEXT," +
                    TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_DATE + " TEXT," +
                    TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_BODY + " TEXT)";

    private static final String SQL_DELETE_OFFLINE_RECORD_TABLE =
            "DROP TABLE IF EXISTS " + TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME;

    private static final  String SQL_CREATE_SETTINGS_TABLE=
            "CREATE TABLE " + TModels.TInstallObject.TInstallEntity.TABLE_NAME + " (" +
                    TModels.TInstallObject.TInstallEntity._ID + " INTEGER PRIMARY KEY," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_ANDROID_ID + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_ANDROID_VERSION + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_APP_ID + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_APP_VERSION + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CALL_FROM_MAIN + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CLIENT_KEY + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CARRIER_ONE + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CARRIER_TWO + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_SIM_SERIAL + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_PHONE_NAME + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_PCKG_NAME + " TEXT," +
                    TModels.TInstallObject.TInstallEntity.COLUMN_NAME_DEVICE_ID + " TEXT)";
    private static final String SQL_DELETE_SETTINGS_TABLE =
            "DROP TABLE IF EXISTS " + TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME;

    private static final String SQL_CREATE_LIFECYCLE_TABLE=
            "CREATE TABLE "+ TModels.TLifeCycleObject.TLifeCycleEntity.TABLE_NAME + " (" +
                    TModels.TLifeCycleObject.TLifeCycleEntity._ID + " INTEGER PRIMARY KEY," +
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_NAME + " TEXT," +
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_START + " TEXT," +
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_END + " TEXT)";
    private static final String SQL_DELETE_LIFECYCLE_TABLE =
            "DROP TABLE IF EXISTS " + TModels.TLifeCycleObject.TLifeCycleEntity.TABLE_NAME;

    TSQLHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    TSQLHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    TSQLHelper(Context context, SQLiteDatabase.CursorFactory factory,DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_OFFLINE_RECORD_TABLE);
        db.execSQL(SQL_CREATE_SETTINGS_TABLE);
        db.execSQL(SQL_CREATE_LIFECYCLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_OFFLINE_RECORD_TABLE);
        db.execSQL(SQL_DELETE_SETTINGS_TABLE);
        db.execSQL(SQL_DELETE_LIFECYCLE_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    long insertNewOfflineRecord(TModels.TOfflineRecord record){
        SQLiteDatabase db =null;
        try{
            db= this.getWritableDatabase();
        }catch (SQLException e){
            TLog.e(TAG,e);
            return -1l;
        }
        ContentValues values = new ContentValues();
        values.put(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_BODY, record.getBody());
        values.put(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_PATH, record.getPath());
        values.put(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_DATE, record.getDate());
        long newRowId = db.insert(TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    boolean deleteOfflineRecord(long id){
        SQLiteDatabase db =null;
        try{
            db= this.getWritableDatabase();
        }catch (SQLException e){
            TLog.e(TAG,e);
            return false;
        }
        String selection = TModels.TOfflineRecord.TOfflineRecordEntity._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        int result=db.delete(TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME, selection, selectionArgs);
        db.close();
        return  result > 0 ? true : false;
    }

    ArrayList<TModels.TOfflineRecord> getOfflineRecords(String path) {
        ArrayList<TModels.TOfflineRecord> list = new ArrayList<>();
        Cursor cursor=null;
        SQLiteDatabase db=null;
        try {
            db = this.getReadableDatabase();
        }catch (SQLException e){
            TLog.e(TAG,e);
            return list;
        }

        String[] projection = {
                TModels.TOfflineRecord.TOfflineRecordEntity._ID,
                TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_PATH,
                TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_BODY,
                TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_DATE
        };

        String selection = TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_PATH + " = ?";
        String[] selectionArgs = {path};
        try {
            cursor = db.query(
                    TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                TModels.TOfflineRecord record=new TModels.TOfflineRecord();
                int idIndex=cursor.getColumnIndexOrThrow(TModels.TOfflineRecord.TOfflineRecordEntity._ID);
                int pathIndex=cursor.getColumnIndexOrThrow(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_PATH);
                int bodyIndex=cursor.getColumnIndexOrThrow(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_BODY);
                int dateIndex=cursor.getColumnIndexOrThrow(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_DATE);
                record.setBody(cursor.getString(bodyIndex));
                record.setPath(cursor.getString(pathIndex));
                record.setDate(cursor.getString(dateIndex));
                record.setId(cursor.getLong(idIndex));
                list.add(record);
            }
        }catch (Exception e){
            TLog.e(TAG,e);
        }finally {
            if(cursor!=null)
                cursor.close();
        }
        return list;
    }

    int updateOfflineRecordId(TModels.TOfflineRecord record,long id){
        Cursor cursor=null;
        SQLiteDatabase db=null;
        int count=0;
        try {
            db = this.getReadableDatabase();
            ContentValues values = new ContentValues();
            values.put(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_BODY, record.getBody());
            String selection = TModels.TOfflineRecord.TOfflineRecordEntity._ID + " = ?";
            String[] selectionArgs = { String.valueOf(id) };
            count = db.update(
                    TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
        }catch (SQLException e){
            TLog.e(TAG,e);
        }
        Log.d(TAG," update row count = "+count);
        return count;
    }

    long setSettings(TModels.TInstallObject installObject){
        SQLiteDatabase db =null;
        try{
            db= this.getWritableDatabase();
        }catch (SQLException e){
            TLog.e(TAG,e);
            return -1l;
        }
        ContentValues values = new ContentValues();
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CLIENT_KEY,installObject.getClientKey());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_ANDROID_ID,installObject.getAndroidId());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_ANDROID_VERSION,installObject.getVersion());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_APP_ID,installObject.getApplicationId());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_APP_VERSION,installObject.getAppVersion());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CALL_FROM_MAIN,installObject.isCallFromMain());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CARRIER_ONE,installObject.getCarrierName());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_CARRIER_TWO,installObject.getCarrierName2());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_DEVICE_ID,installObject.getDeviceId());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_PCKG_NAME,installObject.getPackageName());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_PHONE_NAME,installObject.getPhoneModel());
        values.put(TModels.TInstallObject.TInstallEntity.COLUMN_NAME_SIM_SERIAL,installObject.getSimSerialNumber());
        long newRowId = db.insert(TModels.TInstallObject.TInstallEntity.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    String getSetting(String colName){
        String result="";
        Cursor cursor=null;
        SQLiteDatabase db=null;
        try {
            db = this.getReadableDatabase();
            cursor=db.rawQuery("SELECT * from "+ TModels.TInstallObject.TInstallEntity.TABLE_NAME,null);
            while (cursor.moveToNext()){
                result=cursor.getString(cursor.getColumnIndex(colName));
            }
        }catch (SQLException e){
            TLog.e(TAG,e);
            return result;
        }
        return result;
    }

    boolean isSettingExist(){
        long cnt=0l;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            cnt  = DatabaseUtils.queryNumEntries(db, TModels.TInstallObject.TInstallEntity.TABLE_NAME);
            db.close();
        }catch (Exception e){
            TLog.e(TAG,e);
        }
        return cnt == 0l ? false:true;
    }

    long addActivityLifecycLog(TModels.TLifeCycleObject lifeCycleObject){
        SQLiteDatabase db =null;
        try{
            db= this.getWritableDatabase();
        }catch (SQLException e){
            TLog.e(TAG,e);
            return -1l;
        }
        ContentValues values = new ContentValues();
        values.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_NAME,lifeCycleObject.getName());
        values.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_START,lifeCycleObject.getStartTime());
        values.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_END,lifeCycleObject.getEndTime());
        long newRowId = db.insert(TModels.TInstallObject.TInstallEntity.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }
}
