package com.XMBT.bluetooth.le.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * 轨迹点
 */

public class PointBean {

    /**
     *  "Point": "113.2367822,23.1342726,1503651089000,6.41,0,,0;113.2404333,23.1430215,1503651256000,59.5,39,,0;113.1843512,23.0985459,1503708335000,0.44,0,,0;"
     */

    @SerializedName("Point")
    private String point;

    private double longitude;
    private double latitude;

//    public double getLongitude(){
//        if(TextUtils.isEmpty(point)){
//            return 0;
//        }
//        String [] temp = null;
//        temp = point.split(",");
//        if(temp != null){
//            setLongitude(temp[0]);
//        }
//        return longitude;
//    }
//
//    public void setLongitude(String lng){
//        if(!TextUtils.isEmpty(lng)){
//            longitude = Double.valueOf(lng);
//        }
//    }
//
//    public double getLatitude(){
//        if(TextUtils.isEmpty(point)){
//            return 0;
//        }
//        String [] temp = null;
//        temp = point.split(",");
//        if(temp != null){
//            setLatitude(temp[1]);
//        }
//        return latitude;
//    }
//
//    public void setLatitude(String lat){
//        if(!TextUtils.isEmpty(lat)){
//            latitude = Double.valueOf(lat);
//        }
//    }


    public void setPoint(String str){
        if(!TextUtils.isEmpty(str)){
            String [] temp = null;
            temp = str.split(",");
            if(temp != null){
                longitude = Double.valueOf(temp[0]);
                latitude = Double.valueOf(temp[1]);
            }
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
