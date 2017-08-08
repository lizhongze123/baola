package com.XMBT.bluetooth.le.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.XMBT.bluetooth.le.utils.ToastUtils;

public class BaseFragment extends Fragment {

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

}
