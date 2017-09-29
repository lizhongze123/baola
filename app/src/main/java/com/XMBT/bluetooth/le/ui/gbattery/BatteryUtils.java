package com.XMBT.bluetooth.le.ui.gbattery;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by lzz on 2017/9/12.
 */

public class BatteryUtils {


    public static boolean isInitTTS;


    //----------------电子围栏-------------------

    public static double fenceLat;
    public static double fenceLng;
    public static double radius ;
    public static String fenceStatus = "0"; 
    public static boolean isFirstFenceAlarm = true;
    public static long fenceTime;

    //----------------位移报警-------------------
    public static String displaceStatus = "0";
    public static boolean isFirstDisplaceStatusAlarm = true;
    public static long time;
    /**
     * 保存第一次采集到的位置
     * 最后面的位置和他对比
     */
    public static LatLng start;
    /**
     * 保存最新的一个位置，下一个和他对比
     */
    public static LatLng last;


    public static void reset(){
        fenceLng = 0;
        fenceLat = 0;
        radius = 0;
        fenceStatus = "0";
        isFirstFenceAlarm = true;
        fenceTime = 0;

        displaceStatus = "0";
        isFirstDisplaceStatusAlarm = true;
        time = 0;
        start = null;
        last = null;
    }
}
