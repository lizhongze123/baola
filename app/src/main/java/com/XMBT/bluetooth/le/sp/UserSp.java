package com.XMBT.bluetooth.le.sp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 添加的设备sp
 */

public class UserSp {

    private SharedPreferences sp;

    private Context mContext;

    private static UserSp instance = null;

    public UserSp(Context context) {
        this.mContext = context;
    }

    public static synchronized UserSp getInstance(Context context) {
        if (instance == null) {
            instance = new UserSp(context);
        }
        return instance;
    }

    public String getProduct(String fileName) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String productBase64 = sp.getString("product", null);
        return productBase64;
    }

    public void setProduct(String fileName, String var) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sp.edit().putString("product", var).commit();
    }

    public String getMds(String fileName) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String productBase64 = sp.getString("mds", null);
        return productBase64;
    }

    public void setMds(String fileName, String var) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sp.edit().putString("mds", var).commit();
    }

    public String getId(String fileName) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String id = sp.getString("id", null);
        return id;
    }

    public void setId(String fileName, String var) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sp.edit().putString("id", var).commit();
    }

    public void setRefreshTime(String fileName, String var) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sp.edit().putString("refresh", var).commit();
    }

    public String getRefreshTime(String fileName) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String refresh = sp.getString("refresh", "5");
        return refresh;
    }

}
