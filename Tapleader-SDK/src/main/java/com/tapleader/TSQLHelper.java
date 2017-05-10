package com.tapleader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by mehdi akbarian on 2017-04-23.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TSQLHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "tapleader_offline.db";
    static final String TAG = "TSQLHelper";
    static final int DATABASE_VERSION = 2;

    private static final String SQL_CREATE_OFFLINE_RECORD_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME + " (" +
                    TModels.TOfflineRecord.TOfflineRecordEntity._ID + " INTEGER PRIMARY KEY," +
                    TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_PATH + " TEXT," +
                    TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_DATE + " TEXT," +
                    TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_BODY + " TEXT)";

    private static final String SQL_DELETE_OFFLINE_RECORD_TABLE =
            "DROP TABLE IF EXISTS " + TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME;

    private static final  String SQL_CREATE_SETTINGS_TABLE=
            "CREATE TABLE IF NOT EXISTS " + TModels.TInstallObject.TInstallEntity.TABLE_NAME + " (" +
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
            "CREATE TABLE IF NOT EXISTS "+ TModels.TLifeCycleObject.TLifeCycleEntity.TABLE_NAME + " (" +
                    TModels.TLifeCycleObject.TLifeCycleEntity._ID + " INTEGER PRIMARY KEY," +
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_NAME + " TEXT," +
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DATE + " TEXT," +
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DURATION + " INT," +
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_COUNT + " INT)";
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
        db.execSQL(SQL_CREATE_SETTINGS_TABLE);
        db.execSQL(SQL_CREATE_OFFLINE_RECORD_TABLE);
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
        long newRowId=-1;
        try{
            db= this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_BODY, record.getBody());
            values.put(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_PATH, record.getPath());
            values.put(TModels.TOfflineRecord.TOfflineRecordEntity.COLUMN_NAME_DATE, record.getDate());
            newRowId = db.insert(TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME, null, values);
        }catch (SQLException e){
            TLog.e(TAG,e);
        }finally {
            db.close();
            return newRowId;
        }
    }

    boolean deleteOfflineRecord(long id){
        SQLiteDatabase db =null;
        int result=-1;
        try{
            db= this.getWritableDatabase();
            String selection = TModels.TOfflineRecord.TOfflineRecordEntity._ID + " = ?";
            String[] selectionArgs = { String.valueOf(id) };
            result=db.delete(TModels.TOfflineRecord.TOfflineRecordEntity.TABLE_NAME, selection, selectionArgs);
            db.close();
        }catch (SQLException e){
            TLog.e(TAG,e);
        }finally {

            return result > 0 ? true : false;
        }
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
            db.close();
        }catch (Exception e){
            TLog.e(TAG,e);
        }finally {
            if(cursor!=null)
                cursor.close();

            return list;
        }
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
            db.close();
        }catch (SQLException e){
            TLog.e(TAG,e);
        }finally {

            return count;
        }
    }

    long setSettings(TModels.TInstallObject installObject){
        SQLiteDatabase db =null;
        long newRowId=-1;
        try{
            db= this.getWritableDatabase();
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
            newRowId = db.insert(TModels.TInstallObject.TInstallEntity.TABLE_NAME, null, values);
            db.close();
        }catch (SQLException e){
            TLog.e(TAG,e);
        }finally {
            return newRowId;
        }
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
            db.close();
        }catch (SQLException e){
            TLog.e(TAG,e);
        }finally {

            return result;
        }
    }

    boolean isSettingExist(){
        long cnt=0l;
        SQLiteDatabase db=null;
        try {
            db = this.getReadableDatabase();
            cnt  = DatabaseUtils.queryNumEntries(db, TModels.TInstallObject.TInstallEntity.TABLE_NAME);
            db.close();
        }catch (Exception e){
            TLog.e(TAG,e);
        }finally {

            return cnt == 0l ? false:true;
        }
    }

    long addActivityLifecycleLog(TModels.TLifeCycleObject lco){
        SQLiteDatabase db = null;
        long newRowId=-1;
        Date startDate=TUtils.dateParser(lco.getEndTime());
        long duration = startDate.getTime()-TUtils.dateParser(lco.getStartTime()).getTime();
        String date = TUtils.getSimpleDate(startDate);
        try{
            db= this.getWritableDatabase();
            Cursor cursor=db.rawQuery("SELECT * FROM "+ TModels.TLifeCycleObject.TLifeCycleEntity.TABLE_NAME +
                    " WHERE "+ TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_NAME+" = \""+lco.getName()+
                    "\" AND "+ TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DATE+" = \""+date+"\"",null);
            //if exist just update and return
            while (cursor.moveToNext()){
                long id= cursor.getLong(cursor.getColumnIndex(TModels.TLifeCycleObject.TLifeCycleEntity._ID));
                int count=cursor.getInt(cursor.getColumnIndex(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_COUNT));
                return updateActivityLifecycleLog(db,count+1,id);
            }
            ContentValues values = new ContentValues();
            values.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_NAME,lco.getName());
            values.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DATE,date);
            values.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DURATION,duration);
            values.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_COUNT,1);
            newRowId = db.insert(TModels.TLifeCycleObject.TLifeCycleEntity.TABLE_NAME, null, values);
            db.close();
        }catch (SQLException e){
            TLog.e(TAG,e);
        }finally {
            return newRowId;
        }
    }

    private long updateActivityLifecycleLog(SQLiteDatabase db,int count,long id){
        try {
            ContentValues values = new ContentValues();
            values.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_COUNT, count);
            String selection = TModels.TLifeCycleObject.TLifeCycleEntity._ID + " = ?";
            String[] selectionArgs = { String.valueOf(id) };
            count = db.update(
                    TModels.TLifeCycleObject.TLifeCycleEntity.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
            db.close();
        }catch (Exception e){
            TLog.e(TAG,e);
        }finally {
            return id;
        }
    }

    int getActivityLifecycleCount() {
        int cnt=0;
        SQLiteDatabase db=null;
        try {
            db = this.getReadableDatabase();
            Cursor cursor= db.rawQuery("SELECT SUM("
                    + TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_COUNT
                    +") FROM " + TModels.TLifeCycleObject.TLifeCycleEntity.TABLE_NAME,null);
            cursor.moveToFirst();
            cnt=cursor.getInt(0);
            db.close();
        }catch (Exception e){
            TLog.e(TAG,e);
        }finally {
            return cnt;
        }
    }

    int truncateActivityLifeCycle(){
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;
        try {
            count = db.delete(TModels.TLifeCycleObject.TLifeCycleEntity.TABLE_NAME,null,null);
            db.close();
        }catch (Exception e){
            TLog.e(TAG,e);
        }finally {
            return count;
        }
    }

    JSONArray getActivityLifeCycle(){
        JSONArray array=new JSONArray();
        Cursor cursor=null;
        SQLiteDatabase db=null;
        try {
            db = this.getReadableDatabase();
            String[] projection = {
                    TModels.TLifeCycleObject.TLifeCycleEntity._ID,
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_NAME,
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DATE,
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DURATION,
                    TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_COUNT
            };

                cursor = db.query(
                        TModels.TLifeCycleObject.TLifeCycleEntity.TABLE_NAME, projection, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    JSONObject object=new JSONObject();
                    int name=cursor.getColumnIndexOrThrow(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_NAME);
                    int date=cursor.getColumnIndexOrThrow(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DATE);
                    int duration=cursor.getColumnIndexOrThrow(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DURATION);
                    int count=cursor.getColumnIndexOrThrow(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_COUNT);
                    object.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_NAME,cursor.getString(name));
                    object.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DATE,cursor.getString(date));
                    object.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_DURATION,cursor.getInt(duration));
                    object.put(TModels.TLifeCycleObject.TLifeCycleEntity.COLUMN_NAME_COUNT,cursor.getInt(count));
                    array.put(object);
                }
            db.close();
        }catch (SQLException e){
            TLog.e(TAG,e);
        }finally {

            return array;
        }
    }
}
