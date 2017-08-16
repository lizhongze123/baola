package com.XMBT.bluetooth.le.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

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

    public void showLoadingDialog(final String tips){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog = new LoadingDialog(getActivity());
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(loadingDialog != null){
                    loadingDialog.dismiss();
                }
            }
        });

    }

}
