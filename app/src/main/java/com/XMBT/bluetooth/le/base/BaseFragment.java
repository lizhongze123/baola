package com.XMBT.bluetooth.le.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.XMBT.bluetooth.le.utils.ToastUtils;
import com.XMBT.bluetooth.le.view.loadingdialog.LoadingDialog;

public class BaseFragment extends Fragment {

    LoadingDialog loadingDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showToast(String tips) {
        ToastUtils.toastInBottom(getActivity(), tips);
    }

    public void showToastCenter(String tips) {
        ToastUtils.toastInCenter(getActivity(), tips);
    }

    public void showLoadingDialog(String tips){
        loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.setLoadingText(tips);
        loadingDialog.show();
    }

    public void dismissLoadingDialog(){
        if(loadingDialog != null){
            loadingDialog.dismiss();
        }
    }

}
