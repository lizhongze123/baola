package com.XMBT.bluetooth.le.ui.gbattery;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.XMBT.bluetooth.le.http.ApiResultCallback;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.view.dialog.WarningDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 车辆隐身设置
 */
public class StealthFragment extends BaseFragment {

    private View view;
    private Switch switchButton;
    private YunCheDeviceEntity device;
    private CheckBox cb;
    private String status;
    private boolean isFirstIn = true;

    public static StealthFragment newInstance(YunCheDeviceEntity device, String status) {
        StealthFragment itemFragement = new StealthFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DeviceFragment.DATA_DEVICE, device);
        bundle.putString(FortificationActivity.STATUS, status);
        itemFragement.setArguments(bundle);
        return itemFragement;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.fragment_stealth, null);
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
                if (!isFirstIn) {
                    if (isChecked) {
                        WarningDialog.normal(getContext(), "提示", "车辆隐身后，系统将不再记录所有行程及车身信息", "确定", "取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == DialogInterface.BUTTON_POSITIVE){
                                    setStealth(1);
                                    cb.setChecked(true);
                                }else{
                                    switchButton.setChecked(false);
                                }
                            }
                        }).show();
                    } else {
                        setStealth(0);
                        cb.setChecked(false);
                    }
                }
            }
        });
        if (status.equals(FortificationActivity.ON)) {
            cb.setChecked(true);
            switchButton.setChecked(true);
        }
        isFirstIn = false;
    }

    public void setStealth(int stealth) {
        showLoadingDialog(null);
        String mds = UserSp.getInstance(getContext()).getMds(GlobalConsts.userName);
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "gpsOnOff")
                .params("mds", mds)
                .params("macid", device.macid)
                .params("state", stealth)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            LogUtils.d(s);
                            JSONObject jsonObject = new JSONObject(s);
                            String success = jsonObject.getString("success");
                            if (success.equals("false")) {
                                String msg = jsonObject.getString("msg");
                                showToast(msg);
                            } else {
                                showToast("设置成功");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        dismissLoadingDialog();
                    }
                });

    }

}
