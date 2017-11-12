package com.XMBT.bluetooth.le.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.BatteryCache;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.LogUtils;
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

    public void showLoadingDialog(final String tips){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog = new LoadingDialog(BaseActivity.this);
                if(TextUtils.isEmpty(tips)){
                    loadingDialog.setLoadingText("加载中，请稍候");
                }else{
                    loadingDialog.setLoadingText(tips);
                }
                loadingDialog.show();
            }
        });
    }

    public void dismissLoadingDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(loadingDialog != null){
                    loadingDialog.dismiss();
                }
            }
        });
    }

    public void showToast(String tips) {
        ToastUtils.toastInBottom(this, tips);
    }

    public void showToastCenter(String tips) {
        ToastUtils.toastInCenter(this, tips);
    }

}
