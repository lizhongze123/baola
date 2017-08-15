package com.XMBT.bluetooth.le.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * 轨迹点
 */

public class PointBean {

    /**
     * Point :
     */

    @SerializedName("Point")
    private String point;

    private double longitude;
    private double latitude;

    public double getLongitude(){
        if(TextUtils.isEmpty(point)){
            return 0;
        }
        String [] temp = null;
        temp = point.split(",");
        if(temp != null){
            setLongitude(temp[0]);
        }
        return longitude;
    }

    public void setLongitude(String lng){
        if(!TextUtils.isEmpty(lng)){
            longitude = Double.valueOf(lng);
        }
    }

    public double getLatitude(){
        if(TextUtils.isEmpty(point)){
            return 0;
        }
        String [] temp = null;
        temp = point.split(",");
        if(temp != null){
            setLatitude(temp[1]);
        }
        return latitude;
    }

    public void setLatitude(String lat){
        if(!TextUtils.isEmpty(lat)){
            latitude = Double.valueOf(lat);
        }
    }
}
