package com.XMBT.bluetooth.le.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.XMBT.bluetooth.le.bean.RecordBean;

import java.util.ArrayList;
import java.util.List;


public class DBManger {

    private static DBManger singleton = null;
    private BaseDBHelper baseDBHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    public static DBManger getInstance(Context context) {
        if (singleton == null) {
            synchronized (DBManger.class) {
                if (singleton == null) {
                    singleton = new DBManger(context);
                }
            }
        }
        return singleton;
    }

    private DBManger(Context context) {
        baseDBHelper = new BaseDBHelper(context);
    }

    /***
     * 添加记录信息
     *
     * @param bean
     */
    public void addRecord(RecordBean bean) {
        db = baseDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("start", bean.startTime);
        values.put("stop", bean.stopTime);
        values.put("duration", bean.duration);
        values.put("date", bean.date);
        db.insert(BaseDBHelper.TABLE_DRIVING_RECORD, null, values);
    }

    /***
     * 查询所有记录信息
     *
     */
    public List<RecordBean> queryAllRecord() {
        List<RecordBean> dataList = new ArrayList<>();
        db = baseDBHelper.getWritableDatabase();
        Cursor cursor = db.query(BaseDBHelper.TABLE_DRIVING_RECORD, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            RecordBean bean = new RecordBean();
            bean.startTime = cursor.getString(cursor.getColumnIndex("start"));
            bean.stopTime = cursor.getString(cursor.getColumnIndex("stop"));
            bean.duration = cursor.getString(cursor.getColumnIndex("duration"));
            bean.date = cursor.getString(cursor.getColumnIndex("date"));
            dataList.add(bean);
        }
        cursor.close();
        return dataList;
    }

}
