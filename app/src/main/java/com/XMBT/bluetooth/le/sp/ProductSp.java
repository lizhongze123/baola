package com.XMBT.bluetooth.le.sp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 添加的设备sp
 */

public class ProductSp {

    private SharedPreferences sp;

    private Context mContext;

    private String fileName = "base64";

    private static ProductSp instance = null;

    public ProductSp(Context context){
        this.mContext = context;
        sp = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
    }

    public static synchronized ProductSp getInstance(Context context){
        if(instance == null) {
            instance = new ProductSp(context);
        }
        return instance;
    }

    public String getProduct(String key){
        String productBase64 = sp.getString(key, "");
        return productBase64;
    }

    public void setProduct(String key, String var){
        sp.edit().putString(key,var).commit();
    }

}
