package com.XMBT.bluetooth.le.utils;

import com.orhanobut.logger.Logger;


/**Logcat统一管理类*/
public class LogUtils {

    private LogUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化

    private static final String TAG = "lzz";

    public static void i(String msg) {
        if (isDebug){
            Logger.i(msg);
        }
    }

    public static void d(String msg) {
        if (isDebug){
            Logger.d(msg);
        }
    }

    public static void e(String msg) {
        if (isDebug){
            Logger.e(msg);
        }
    }

    public static void v(String msg) {
        if (isDebug){
            Logger.v(msg);
        }
    }

    public static void w(String msg) {
        if (isDebug){
            Logger.w(msg);
        }
    }

}
