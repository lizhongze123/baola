package com.XMBT.bluetooth.le.sp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 添加的设备sp
 */

public class UserSp {

    private SharedPreferences sp;

    private Context mContext;

    private String fileName = "userInfo";

    private static UserSp instance = null;

    public UserSp(Context context) {
        this.mContext = context;
        sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    public static synchronized UserSp getInstance(Context context) {
        if (instance == null) {
            instance = new UserSp(context);
        }
        return instance;
    }

    public String getProduct() {
        String productBase64 = sp.getString("product", "");
        return productBase64;
    }

    public void setProduct(String var) {
        sp.edit().putString("product", var).commit();
    }

    public String getMds() {
        String productBase64 = sp.getString("mds", "");
        return productBase64;
    }

    public void setMds(String var) {
        sp.edit().putString("mds", var).commit();
    }

    public String getId() {
        String productBase64 = sp.getString("id", "");
        return productBase64;
    }

    public void setId(String var) {
        sp.edit().putString("id", var).commit();
    }

}
