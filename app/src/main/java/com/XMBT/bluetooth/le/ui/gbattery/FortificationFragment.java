package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseFragment;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

/**
 *设防控制
 */
public class FortificationFragment extends BaseFragment {

    private View view;
    private Switch switchButton;
    private YunCheDeviceEntity device;
    private CheckBox cb;
    private String status;
    private boolean isFirstIn = true;

    public static FortificationFragment newInstance(YunCheDeviceEntity device, String status) {
        FortificationFragment itemFragement = new FortificationFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DeviceFragment.DATA_DEVICE, device);
        bundle.putString(FortificationActivity.STATUS, status);
        itemFragement.setArguments(bundle);
        return itemFragement;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.fragment_fortification, null);
        Bundle arguments = getArguments();
        if (arguments != null) {
            device = (YunCheDeviceEntity) arguments.getSerializable(DeviceFragment.DATA_DEVICE);
            status = arguments.getString(FortificationActivity.STATUS);
        }
        initViews();
        return view;
    }

    private void initViews() {
        cb = (CheckBox) view.findViewById(R.id.cb);
        switchButton = (Switch) view.findViewById(R.id.switchButton);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if(!isFirstIn){
                    if (isChecked) {
                        cb.setChecked(true);
                    } else {
                        cb.setChecked(false);
                    }
                }
            }
        });
        if(status == FortificationActivity.ON){
            cb.setChecked(true);
            switchButton.setChecked(true);
            isFirstIn = false;
        }
    }

}
