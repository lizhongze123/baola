package com.XMBT.bluetooth.le.utils;

import android.app.Activity;
import android.app.ProgressDialog;

import org.apache.commons.codec.binary.Base64;

/**
 * Created by Administrator on 2016/7/26 0026.
 */
public class ToolsUtil {
    public static ProgressDialog progressDialog;
    public static void showProgressDialog(Activity activity, String msg){
        progressDialog=new ProgressDialog(activity);

        progressDialog.setMessage(msg);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.cancel();
            progressDialog=null;
        }
    }

    /**
     * 解码
     * @param bytes
     * @return
     */
    public static byte[] decodeBase64(final byte[] bytes) {
        return Base64.decodeBase64(bytes);
    }

    /**
     * 二进制数据编码为BASE64字符串
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public static String encodeBase64(final byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }
}
