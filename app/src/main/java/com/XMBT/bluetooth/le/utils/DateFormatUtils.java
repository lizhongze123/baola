package com.XMBT.bluetooth.le.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 格式化日期工具
 * Created by lzz on 2017/7/21.
 */

public class DateFormatUtils {

    public static final int MILLISECOND = 1;
    public static final int DATE = 2;

    public static final String FORMAT_ALL = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_YMDHM = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_YMD = "yyyy-MM-dd";
    public static final String FORMAT_HMS = "HH:mm:ss";
    public static final String FORMAT_HM = "HH:mm";


    public static long getTimeMills(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_YMDHM);
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * 当前时间的毫秒数
     *
     * @return
     */
    public static long getDate() {
        return System.currentTimeMillis();
    }

    /**
     * 当前时间
     *
     * @param format
     * @return
     */
    public static String getDate(String format) {
        DateFormat df = new SimpleDateFormat(format, Locale.CHINA);
        return df.format(System.currentTimeMillis());
    }

    /**
     * @param time   指定时间
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

    /**
     * 获取当前的前x天的时间，并格式化
     *
     * @param customDay x天
     * @return
     */
    public static String getBeforeDay(int customDay, int type) {

        String date = null;
        Calendar cl = Calendar.getInstance();
        int day = cl.get(Calendar.DAY_OF_MONTH) - customDay;
        cl.set(Calendar.DAY_OF_MONTH, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        date = sdf.format(cl.getTime());
        date = date + " 00:00";
        if (type == MILLISECOND) {
            try {
                return new SimpleDateFormat(FORMAT_YMDHM).parse(date).getTime() + "";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            return date;
        }
        return null;
    }

    public static String getBeforeHour(int customHour, int type) {
        Calendar cl = Calendar.getInstance();
        int hour;
        hour = cl.get(Calendar.HOUR_OF_DAY) - customHour;
        cl.set(Calendar.HOUR_OF_DAY, hour);
        if (type == MILLISECOND) {
            return cl.getTimeInMillis() + "";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_YMDHM);
            return sdf.format(cl.getTime());
        }
    }
}
