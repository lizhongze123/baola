package com.XMBT.bluetooth.le.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 格式化日期工具
 * Created by lzz on 2017/7/21.
 */

public class DateFormatUtils {

    public static  final String FORMAT_ALL = "yyyy-MM-dd HH:mm:ss";
    public static  final String FORMAT_YMDHM = "yyyy-MM-dd HH:mm";
    public static  final String FORMAT_YMD = "yyyy-MM-dd";
    public static  final String FORMAT_HMS = "HH:mm:ss";
    public static  final String FORMAT_HM = "HH:mm";


    /**
     * 当前时间
     * @param format
     * @return
     */
    public static String getDate(String format) {
        DateFormat df = new SimpleDateFormat(format, Locale.CHINA);
        return df.format(System.currentTimeMillis());
    }

    /**
     *
     * @param time  指定时间
     * @param format
     * @return
     */
    public static String getDate(long time, String format) {
        if (time == 0) {
            return "";
        }
        DateFormat df = new SimpleDateFormat(format, Locale.CHINA);
        return df.format(time);
    }

}
