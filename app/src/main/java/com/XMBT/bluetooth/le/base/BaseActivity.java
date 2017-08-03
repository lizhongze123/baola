package com.XMBT.bluetooth.le.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.XMBT.bluetooth.le.utils.ToastUtils;

public class BaseActivity extends FragmentActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void showToast(String tips) {
        ToastUtils.toastInBottom(this, tips);
    }
}
