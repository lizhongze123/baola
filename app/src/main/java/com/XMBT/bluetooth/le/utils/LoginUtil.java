package com.XMBT.bluetooth.le.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.XMBT.bluetooth.le.ui.misc.LoginActivity;

import java.lang.ref.WeakReference;

/**
 * 登录判断类
 */

public class LoginUtil {

    public static final String mINVOKER = "INTERCEPTOR_INVOKER";


    public  static void checkLogin(Context context, final LoginForCallBack callBack) {
        // 弱引用，防止内存泄露，
        WeakReference<Context> reference= new WeakReference<Context>(context);
        if (TextUtils.isEmpty(Configure.USERID)) { // 判断是否登录，否返回true
            Configure.CALLBACK = new ICallBack() {

                @Override
                public void postExec() {
                    // 登录回调后执行登录回调前需要做的操作
                    if (!TextUtils.isEmpty(Configure.USERID)) {
                        // 这里需要再次判断是否登录，防止用户取消登录，取消则不执行登录成功需要执行的回调操作
                        callBack.callBack();
                        //防止调用界面的回调方法中有传进上下文的引用导致内存泄漏
                        Configure.CALLBACK = null;
                    }
                }
            };
            Context mContext = reference.get();
            if (mContext != null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                reference = null;
            }
        } else {
            // 登录状态直接执行登录回调前需要做的操作
            callBack.callBack();
        }
    }


    public void clear() {
        try {
            finalize();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 声明一个登录成功回调的接口
    public interface ICallBack {
        // 在登录操作及信息获取完成后调用这个方法来执行登录回调需要做的操作
        void postExec();
    }

    public interface LoginForCallBack {
        void callBack();
    }

}
