package com.tapleader;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by mehdi akbarian on 2017-03-01.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class OfflineStore{

    private static final String FILE_NAME = "t_offlineStore";
    private static final String TAG = "OfflineStore";
    private static OfflineStore mOfflineStore = null;
    private static Context context;

    private OfflineStore(Context context) {
        this.context = context;
    }

    public static OfflineStore initialize(Context context) {
        if (mOfflineStore == null)
            mOfflineStore = new OfflineStore(context);
        TLog.d(TAG,"initialize");
        return mOfflineStore;
    }

    /**
     * use {@link OfflineStore#store(TModels.TOfflineRecord)}
     * @param url
     * @param body
     */
    @Deprecated()
    public void store(String url, String body) {
        File localData = new File(TPlugins.get().getCacheDir(), FILE_NAME);
        String data = getData(url, body);
        if (!localData.exists()) {
            try {
                localData.createNewFile();
            } catch (IOException e) {
                TLog.e(TAG, e);
                return;
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(localData, true));
            writer.write(data + "\r\n");
            writer.flush();
        } catch (IOException e) {
            TLog.e(TAG, e);
        }
    }

    /**
     *
     * @param record
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     * @since version 1.1.4
     */
    long store(TModels.TOfflineRecord record){
        TSQLHelper helper=new TSQLHelper(context);
        long id=-1l;
        if(record!=null) {
            switch (record.getPath()){
                case Constants.Endpoint.NEW_INSTALL:
                    TModels.TOfflineRecord old=isInstallRecordExist();
                    if(old!=null){
                        id=old.getId();
                        helper.updateOfflineRecordId(record,id);
                    }else {
                        id = helper.insertNewOfflineRecord(record);
                    }
                    break;
                case Constants.Endpoint.ACTIVITY_TRACKING:
                    id = helper.insertNewOfflineRecord(record);
                    break;
            }
        }
        return id;
    }

    TModels.TOfflineRecord isInstallRecordExist(){
        ArrayList<TModels.TOfflineRecord> list=getAllRequests();
        for(TModels.TOfflineRecord record:list){
            if(record.getPath().equals(Constants.Endpoint.NEW_INSTALL)){
                return record;
            }
        }
        return null;
    }

    /**
     * get list of requests that saved in file!
     * use {@link OfflineStore#getAllRequests()}
     * @return
     */
    @Deprecated
    JSONArray getRequests() {
        File localData = new File(TPlugins.get().getCacheDir(), FILE_NAME);
        if (!localData.exists())
            return null;
        JSONArray requests;
        try {
            String data = TFileUtils.readFileToString(localData, "UTF-8");
            String[] lines=TFileUtils.splitFileLines(data);
            requests=getJsonArray(lines);
        } catch (IOException e) {
            TLog.e(TAG, e);
            return null;
        }
        return requests;
    }

    /**
     * get list of {@link com.tapleader.TModels.TOfflineRecord}
     * @return list of requests in database
     * @since version 1.1.4
     */
    ArrayList<TModels.TOfflineRecord> getAllRequests(){
        ArrayList<TModels.TOfflineRecord> list=new ArrayList<>();
        TSQLHelper helper=new TSQLHelper(context);
        list.addAll(helper.getOfflineRecords(Constants.Endpoint.NEW_INSTALL));
        list.addAll(helper.getOfflineRecords(Constants.Endpoint.ACTIVITY_TRACKING));
        helper.close();
        return list;
    }

    /**
     * delete file of offline requests!
     * use {@link OfflineStore#deleteRequest(long)}
     * @return true if delete file successfully!
     */
    @Deprecated
    boolean deleteRequests(){
        File localData = new File(TPlugins.get().getCacheDir(), FILE_NAME);
        if (!localData.exists())
            return false;
        return TFileUtils.deleteQuietly(localData);
    }

    /**
     * delete each request by id
     * @param id
     * @return true if delete successfully!
     * @since version 1.1.4
     */
    boolean deleteRequest(long id){
        TSQLHelper helper=new TSQLHelper(context);
        return helper.deleteOfflineRecord(id);
    }

    boolean deleteInstallRecords(){
        TSQLHelper helper=new TSQLHelper(context);
        ArrayList<TModels.TOfflineRecord> list=new ArrayList<>();
        list.addAll(helper.getOfflineRecords(Constants.Endpoint.NEW_INSTALL));
        helper.close();
        if(list.size()==0)
            return false;
        for(TModels.TOfflineRecord record:list){
            deleteRequest(record.getId());
        }
        return true;
    }

    @Deprecated
    private JSONArray getJsonArray(String[] data) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < data.length; i++)
            array.put(data[i]);
        return array;
    }

    @Deprecated
    private String getData(String url, String body) {
        JSONObject object = new JSONObject();
        try {
            object.put("path", url);
            object.put("body", body);
            object.put("date", TUtils.getDateTime());
        } catch (JSONException e) {
            TLog.e(TAG, e);
        }
        return object.toString();
    }

    @Deprecated
    private JSONObject parsData(String data) {
        JSONObject object = null;
        try {
            object = new JSONObject(data);
        } catch (JSONException e) {
            TLog.e(TAG, e);
        }
        return object;
    }
}
