package com.XMBT.bluetooth.le.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.XMBT.bluetooth.le.utils.ToastUtils;
import com.XMBT.bluetooth.le.view.loadingdialog.LoadingDialog;

import java.util.ArrayList;

public class BaseActivity extends FragmentActivity {

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
    }

    public void showLoadingDialog(String tips){
        loadingDialog = new LoadingDialog(this);
        loadingDialog.setLoadingText(tips);
        loadingDialog.show();
    }

    public void dismissLoadingDialog(){
        if(loadingDialog != null){
           loadingDialog.dismiss();
        }
    }

    public void showToast(String tips) {
        ToastUtils.toastInBottom(this, tips);
    }

    public void showToastCenter(String tips) {
        ToastUtils.toastInCenter(this, tips);
    }
}
