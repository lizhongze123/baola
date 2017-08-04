package com.XMBT.bluetooth.le.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.XMBT.bluetooth.le.utils.LogUtils;

public class BaseDBHelper extends SQLiteOpenHelper {

    /**
     * 数据库名字
     */
    private static final String DB_NAME = "pourio.db";
    private static final int VERSION = 1;
    /**
     * @Fields TABLE_DRIVING_RECORD : 行驶记录表
     */
    public static final String TABLE_DRIVING_RECORD = "record";

    public BaseDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RECORD = "create table " + TABLE_DRIVING_RECORD + "(" +
                "_id integer primary key autoincrement, " +
                "date text, " +
                "start text, " +
                "stop text, " +
                "duration text" +
                ")";
        db.execSQL(CREATE_RECORD);
        LogUtils.i("数据库创建成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("drop table if exists " + TABLE_DRIVING_RECORD);
            onCreate(db);
        }
    }

}
