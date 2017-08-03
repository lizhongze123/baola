package com.XMBT.bluetooth.le.consts;


public class SampleGattAttributes {

    public static String SERVICE_UUID = "000018f0-0000-1000-8000-00805f9b34fb";

    public static String CHAR_WRITE = "00002af1-0000-1000-8000-00805f9b34fb";
    public static String CHAR_NOTIFY = "00002af0-0000-1000-8000-00805f9b34fb";

    /**
     * 防止死机命令，车灯状态
     */
    public static String WRITE_CRASH = "0433CC53AC";
    /**
     * 档位
     */
    public static String WRITE_SHIFT = "0433CC31CE";
    /**
     * 开启车灯
     */
    public static String WRITE_OPEN_LIGHT = "0455AA31CE";
    /**
     * 关闭车灯
     */
    public static String WRITE_CLOSE_LIGHT = "0455AA00FF";
    /**
     * 车灯状态 on
     */
    public static String LIGHT_ON = "04CC3331CE";
    /**
     * 车灯状态 off
     */
    public static String LIGHT_OFF = "04CC3300FF";
    /**
     * 自动开灯  mcu to app
     */
    public static String AUTO_LIGHT = "04EE1105FA";

    /**
     * 归家设置30
     */
    public static String SHIFT_30 = "0477881EE1";
    /**
     * 归家设置60
     */
    public static String SHIFT_60 = "0477883CC3";
    /**
     * 归家设置90
     */
    public static String SHIFT_90 = "0477885AA5";
    /**
     * 归家设置120
     */
    public static String SHIFT_120 = "0477887887";

    /**
     * 电池电压
     */
    public static String BATTERY_VOLTAGE = "04A758";
    /**
     * 电池温度
     */
    public static String BATTERY_TEMPERATURE = "06B847";
    /**
     * 使用天数
     */
    public static String DAYS_OF_USE = "06C936";
    /**
     * 工作电压
     */
    public static String WORK_VOLTAGE = "049966";
    /**
     * 工作档位
     */
    public static String WORK_SHIFT = "04BB44";
    /**
     * 故障信息
     */
    public static String TROUBLE_INFO = "04DD22";
    /**
     * 故障信息-工作电压过高
     */
    public static String TROUBLE_INFO_HIGH_VOLTAGE = "01FE";
    /**
     * 故障信息-工作电压过高
     */
    public static String TROUBLE_INFO_LOW_VOLTAGE = "02FD";
    /**
     * 故障信息-工作温度过高
     */
    public static String TROUBLE_INFO_HIGH_TEMPERATURE = "03FC";
    /**
     * 故障信息-点灯失败
     */
    public static String TROUBLE_INFO_FAILED = "04FB";
    /**
     * 手机未认证
     */
    public static String UNAUTHORIZED = "04EA15A35C";

    /**
     * 城市模式
     */
    public static String MODE_CITY = "04669900FF";
     /**
      * 高速模式
     */
    public static String MODE_HIGHWAY = "0466990FF0";
}
