package com.XMBT.bluetooth.le.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import com.XMBT.bluetooth.le.R;

import org.zackratos.ultimatebar.UltimateBar;

/**
 * Created by lzz on 2017/8/8.
 */

public class StatusBarHelper {

    public static void setStatusBarColor(Context context, @ColorRes int id){
        UltimateBar ultimateBar = new UltimateBar((Activity) context);
        ultimateBar.setColorBar(ContextCompat.getColor(context, id));
    }

    public static void setTransparentBar(Context context, int color){
        UltimateBar ultimateBar = new UltimateBar((Activity) context);
        ultimateBar.setTransparentBar(color, 50);
    }

    public static void setImmersionBar(Context context){
        UltimateBar ultimateBar = new UltimateBar((Activity) context);
        ultimateBar.setImmersionBar();
    }

}
