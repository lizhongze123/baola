package com.XMBT.bluetooth.le.consts;


import com.XMBT.bluetooth.le.utils.Configure;

public class GlobalConsts {


    public static String userName ;

    public static final String URL = "http://app.ycqpmall.com/";
    public static final String GET_DATE = URL + "GetDateServices.asmx/GetDate";
    public static final String LOGIN = URL + "GetDateServices.asmx/loginSystem";

    public static final String BBS_URL = "http://wx.vemax.cn/bbs2";

    public static final String BANNER_URL0 = "http://www.ycqpmall.com/gg/new1.jpg";
    public static final String BANNER_URL1 = "http://www.ycqpmall.com/gg/new2.jpg";
    public static final String BANNER_URL2 = "http://www.ycqpmall.com/gg/new3.jpg";
    public static final String BANNER_URL3 = "http://www.ycqpmall.com/gg/new4.jpg";
    public static final String BANNER_URL4 = "http://www.ycqpmall.com/gg/new5.jpg";

    /**
     * 设备
     */
    public static final String LIGHTING = "Pourio HID"; //照明系统
    public static final String POWER = "XM Power"; //启动电源
    public static final String BATTERY = "XM Battery";//动力电池
    public static final String GPS_BATTERY = "GPS"; //gps智能电池


    public static final String LIGHTING_CN = "汽车智能照明系统";
    public static final String POWER_CN = "汽车智能启动电源";
    public static final String BATTERY_CN = "汽车智能动力电池";
    public static final String GPS_BATTERY_CN = "汽车GPS智能电池";

    /**
     * IntentFilter
     */
    public static final String FILTER_ADD_DEVICE = "add_device";
    public static final String FILTER_ACTION_CONNECT = "action_connect";


    public static final String ACTION_CONNECT_CHANGE = "XM_CONNECT";
    public static final String ACTION_NOTIFI = "XM_NOTIFI";
    public static final String ACTION_NAME_RSSI = "XM_RSSI";

    public static final String ACTION_SCAN_BLE_OVER = "action_scan_ble_over";

    public static final String ACTION_SCAN_NEW_DEVICE = "action_scan_new_device";


    /**
     * 连接成功的蓝牙地址的sp文件名
     */
    public static final String SP_BLUETOOTH_DEVICE = "baoliao";

    /**
     * 产品sp file名
     */
    public static String getProductSpName(){
        if(Configure.USERID != null){
            return "product" + Configure.USERID;
        } else{
            return "";
        }

    }

    public final static String EXTRA_DATA = "EXTRA_DATA";


}
