package com.XMBT.bluetooth.le.ui.gbattery;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.XMBT.bluetooth.le.view.dialog.WarningDialog;
import com.baidu.tts.client.SpeechSynthesizer;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 设防控制
 */
public class FortificationActivity extends BaseActivity {

    private RadioButton[] btnAry = new RadioButton[2];
    private MyButtonListener myButtonListener;
    private YunCheDeviceEntity device;
    private String defenceStatus = "";
    private String gpsOnOff = "";
    public static final String ON = "1";
    public static final String OFF = "0";
    public static final String SAFFON = "SAFEON";
    public static final String SAFFOFF = "SAFEOFF";

    private LinearLayout llStealth, llFortification;
    private Switch swStealth, swFortification;
    private CheckBox cbStealth, cbFortification;
    private boolean isFirstIn = true;

    private SpeechSynthesizer mSpeechSynthesizer;



    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (defenceStatus.equals(ON)) {
                cbFortification.setChecked(true);
                swFortification.setChecked(true);
            }else{
                cbFortification.setChecked(false);
                swFortification.setChecked(false);
            }
            if (gpsOnOff.equals(ON)) {
                cbStealth.setChecked(true);
                swStealth.setChecked(true);
            }
            isFirstIn = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortification);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        device = (YunCheDeviceEntity) getIntent().getSerializableExtra(DeviceFragment.DATA_DEVICE);
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        initViews();
        addListener();
        getStatus();
    }


    private void initViews() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnAry[0] = (RadioButton) findViewById(R.id.radio1);
        btnAry[1] = (RadioButton) findViewById(R.id.radio2);
        llFortification = (LinearLayout) findViewById(R.id.ll_fortification);
        llStealth = (LinearLayout) findViewById(R.id.ll_stealth);
        swStealth = (Switch) findViewById(R.id.sw_stealth);
        swFortification = (Switch) findViewById(R.id.sw_fortification);
        cbFortification = (CheckBox) findViewById(R.id.cb_fortification);
        cbStealth = (CheckBox) findViewById(R.id.cb_stealth);

        swFortification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                LogUtils.d("===========set==========");

                if (!isFirstIn) {
                    if (isChecked) {
                        if (device.equipmentStatus == 0) {
//                            if (isInitTTS) {
                                mSpeechSynthesizer.speak("设备已离线");
                                showToast("设备已离线");
//                            }
                            swFortification.setChecked(false);
                        } else if (device.equipmentStatus == 1) {
//                            if (isInitTTS) {
                                mSpeechSynthesizer.speak("设备未启用");
                                showToast("设备未启用");
//                            }
                            swFortification.setChecked(false);
                        } else if (device.equipmentStatus == 2) {
                            setFortification(SAFFON);
                            cbFortification.setChecked(true);
                        }
                    } else {
                        setFortification(SAFFOFF);
                        cbFortification.setChecked(false);
                    }
                }
            }
        });

        swStealth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!isFirstIn) {
                    if (isChecked) {
                        WarningDialog.normal(FortificationActivity.this, "提示", "车辆隐身后，系统将不再记录所有行程及车身信息", "确定", "取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    setStealth(ON);
                                    cbStealth.setChecked(true);
                                } else {
                                    swStealth.setChecked(false);
                                }
                            }
                        }).show();
                    } else {
                        setStealth(OFF);
                        cbStealth.setChecked(false);
                    }
                }
            }
        });

    }

    private void addListener() {
        myButtonListener = new MyButtonListener();
        for (int i = 0; i < btnAry.length; i++) {
            btnAry[i].setOnClickListener(myButtonListener);
        }
    }



    class MyButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.radio1:
                    llFortification.setVisibility(View.VISIBLE);
                    llStealth.setVisibility(View.GONE);
                    break;
                case R.id.radio2:
                    llStealth.setVisibility(View.VISIBLE);
                    llFortification.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public void getStatus() {
        showLoadingDialog(null);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "getUserStatus")
                .params("mds", mds)
                .params("macid", device.macid)
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
                                JSONObject rowsObj = jsonObject.getJSONObject("rows");
                                defenceStatus = rowsObj.getString("defenceStatus");
                                gpsOnOff = rowsObj.getString("gpsOnOff");
                                mhandler.sendEmptyMessage(0);
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

    public void setFortification(final String status) {
        showLoadingDialog(null);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "SendCommands")
                .params("mds", mds)
                .params("macid", device.macid)
                .params("cmd", status)
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
                                if(status.equals(SAFFON)){
                                    showToast("设置成功");
                                    BatteryUtils.displaceStatus = ON;
                                    defenceStatus = ON;
                                    mhandler.sendEmptyMessage(0);
                                }
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

    /** 设置*/
    public void setStealth(String stealth) {
        showLoadingDialog(null);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
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


    @Override
    protected void onDestroy() {
        this.mSpeechSynthesizer.release();
        super.onDestroy();
    }

}
