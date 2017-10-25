package com.XMBT.bluetooth.le.consts;

/**
 * 页面缓存
 */

public class CacheConsts {
    public static String voltage = "电池电压：0V";
    public static String temperature = "电池温度：0 ℃";
    public static int DCAnimation = 0;
    public static String tvStatus = "设备未连接";
    public static boolean tvStatusBoolean;
    public static boolean floodlight, warninglight, usb;


    public void reset(){
//        voltage = "电池电压：0V";
//        temperature = "电池温度：0 ℃";
//        DCAnimation = 0;
//        tvStatus = "设备未连接";
//        tvStatusBoolean = false;
        floodlight = false;
        warninglight = false;
        usb = false;
    }
}
