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

    public String getPwd(String fileName) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String pwd = sp.getString("pwd", null);
        return pwd;
    }

    public void setPwd(String fileName, String var) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sp.edit().putString("pwd", var).commit();
    }

    public void setRefreshTime(String fileName, String var) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sp.edit().putString("refresh", var).commit();
    }

    public String getRefreshTime(String fileName) {
        sp = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String refresh = sp.getString("refresh", "10");
        return refresh;
    }

    public void saveUser(String account, String pwd){
        sp = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        sp.edit().putString("account", account).commit();
        sp.edit().putString("pwd", pwd).commit();
    }

    public String getAccount(){
        sp = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        String account = sp.getString("account", "");
        return account;
    }

    public String getPwd(){
        sp = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        String pwd = sp.getString("pwd", "");
        return pwd;
    }

}
