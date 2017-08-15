package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseFragment;

public class InvisibleFragment extends BaseFragment {

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.invisible_fragment, null);
        return view;
    }
}
