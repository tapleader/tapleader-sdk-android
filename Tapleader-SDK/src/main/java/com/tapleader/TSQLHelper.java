package com.tapleader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_OFFLINE_RECORD_TABLE);
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

    boolean deletOfflineRecord(long id){
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
            }
        }catch (Exception e){
            TLog.e(TAG,e);
        }finally {
            if(cursor!=null)
                cursor.close();
        }
        return list;
    }
}
