package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.XMBT.bluetooth.le.R;

/**
 * Created by haowenlee on 2017/6/4.
 */
public class FortificationFragment extends Fragment {

    private View view;

    private Switch switchButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.fortification_fragment, null);
        initViews();
        return view;
    }

    private void initViews() {
        switchButton = (Switch) view.findViewById(R.id.switchButton);
    }
}
