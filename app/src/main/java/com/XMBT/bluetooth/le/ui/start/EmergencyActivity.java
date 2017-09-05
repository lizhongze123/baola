package com.XMBT.bluetooth.le.ui.start;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.ui.light.LightFunctionActivity;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.ChargingProgess;
import com.XMBT.bluetooth.le.view.ListDialog;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.bumptech.glide.Glide;
import com.stx.xhb.xbanner.XBanner;

import java.util.ArrayList;
import java.util.List;

/**
 * 汽车智能启动电源
 */
public class EmergencyActivity extends BaseActivity implements XBanner.XBannerAdapter, View.OnClickListener {

    private XBanner xBanner;
    private List<String> bannerUrls = new ArrayList<>();

    private TitleBar titleBar;

    public final static String EXTRA_DATA = "EXTRA_DATA";

    private String strTemp;

    private TextView tvVoltage, tvStatus, tvTemperature;
    private CheckBox cbFloodlight, tvWarninglight, cbUsb, tvFloodlight, cbWarninglight, tvUsb;

    private float temf, volf;
    private ChargingProgess chargingprigressView;

    private BleManager bleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initBle();
        initDatas();
        initView();
        registerBoradcastReceiver();
    }

    private void initBle() {
        bleManager = BleManager.getInstance(this);
        if (!bleManager.isSupportBle()) {
            showToast(getResources().getString(R.string.ble_not_supported));
        }

        //如果有连接过，下一次自动连接
        String address = PreferenceUtils.readString(this, GlobalConsts.SP_BLUETOOTH_DEVICE, GlobalConsts.POWER, "");
        if(!TextUtils.isEmpty(address)){
            bleManager.realConnect(GlobalConsts.POWER, address);
        }else{
            bleManager.startScan(this, GlobalConsts.POWER);
        }
    }

    private void initDatas() {
        bannerUrls.add(GlobalConsts.BANNER_URL0);
        bannerUrls.add(GlobalConsts.BANNER_URL1);
        bannerUrls.add(GlobalConsts.BANNER_URL2);
        bannerUrls.add(GlobalConsts.BANNER_URL3);
        bannerUrls.add(GlobalConsts.BANNER_URL4);
    }

    private void initTitle() {
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.setRightOnClicker(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BleManager.isConnSuccessful) {
                    BleManager.getInstance(EmergencyActivity.this).disconnect();
                } else {
                    BleManager.getInstance(EmergencyActivity.this).startScan(EmergencyActivity.this, GlobalConsts.POWER);
                }
            }
        });
    }

    private void initView() {
        initTitle();

        tvVoltage = (TextView) findViewById(R.id.tv_voltage);
        chargingprigressView = (ChargingProgess) findViewById(R.id.chargingprigressView);
        tvTemperature = (TextView) findViewById(R.id.tv_temperature);
        cbFloodlight = (CheckBox) findViewById(R.id.cb_floodlight);
        cbWarninglight = (CheckBox) findViewById(R.id.cb_warninglight);
        cbUsb = (CheckBox) findViewById(R.id.cb_usb);
        tvFloodlight = (CheckBox) findViewById(R.id.tv_floodlight);
        tvWarninglight = (CheckBox) findViewById(R.id.tv_warninglight);
        tvUsb = (CheckBox) findViewById(R.id.tv_usb);
        tvStatus = (TextView) findViewById(R.id.tv_status);

        xBanner = (XBanner) findViewById(R.id.xbanner);
        xBanner.setData(bannerUrls, null);
        xBanner.setmAdapter(this);

        cbFloodlight.setOnClickListener(this);
        cbWarninglight.setOnClickListener(this);
        cbUsb.setOnClickListener(this);

        connectChanged(BleManager.isConnSuccessful);
    }

    @Override
    public void loadBanner(XBanner banner, View view, int position) {
        Glide.with(this).load(bannerUrls.get(position)).into((ImageView) view);
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(GlobalConsts.ACTION_NAME_RSSI);
        myIntentFilter.addAction(GlobalConsts.ACTION_CONNECT_CHANGE);
        myIntentFilter.addAction(GlobalConsts.ACTION_NOTIFI);
        myIntentFilter.addAction(GlobalConsts.ACTION_SCAN_BLE_OVER);
        myIntentFilter.addAction(GlobalConsts.ACTION_SCAN_NEW_DEVICE);

        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private void connectChanged(boolean isConnected) {
        if (isConnected) {
            titleBar.setTvRight("已连接");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.dark_blue));
        } else {
            titleBar.setTvRight("未连接");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.white));
            chargingprigressView.setProgress(0);
            tvVoltage.setText("电池电压:" + 0 + "V");
            tvTemperature.setText("电池温度:" + 0 + "℃");
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String strXqdDd = "";
            String strXqdDdy = "";
            if (action.equals(GlobalConsts.ACTION_NOTIFI)) {
                strTemp = intent.getStringExtra(EXTRA_DATA);
                //去除：
                if (!TextUtils.isEmpty(strTemp)) {
                    strTemp = strTemp.replaceAll(":", "");
                }
                if (!strTemp.equals("0000000000")) {  //过滤掉00:00:00:00:00

                    if (strTemp.length() == 10) {
                        String substr = strTemp.substring(0, 6);
                        String substr2 = strTemp.substring(6, 10);

                        if (substr.equals(SampleGattAttributes.REAL_VOLTAGE)) {
                            String voltageStr = substr2.substring(0, 2);
                            int vol10 = Integer.parseInt(voltageStr, 16);
                            volf = vol10 / 10f;
                            if (volf < 10.5) {
                                tvVoltage.setText("电池电压:" + volf + "V");
                                tvVoltage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            } else {
                                tvVoltage.setText("电池电压:" + volf + "V");
                            }
                        }
                        //电量>=3格时，电源良好
                        //电量<=2格时，电量不足
                        //温度大于45°，禁止启动汽车
                        if (substr.equals(SampleGattAttributes.BATTERY_INDICATOR)) {
                            if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_FIVE)) {
                                LogUtils.d("电池电量---18");
                                chargingprigressView.setDCAnimation(18);
                                tvStatus.setText("电源良好，允许启动汽车");
                            } else if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_FOUR)) {
                                LogUtils.d("电池电量---14");
                                chargingprigressView.setDCAnimation(14);
                                tvStatus.setText("电源良好，允许启动汽车");
                            } else if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_THREE)) {
                                LogUtils.d("电池电量---10");
                                chargingprigressView.setDCAnimation(10);
                                tvStatus.setText("电量不足，禁止启动汽车");
                            } else if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_TWO)) {
                                LogUtils.d("电池电量---6");
                                chargingprigressView.setDCAnimation(6);
                                tvStatus.setText("电量不足，禁止启动汽车");
                            } else if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_ONE)) {
                                LogUtils.d("电池电量---2");
                                chargingprigressView.setDCAnimation(2);
                                tvStatus.setText("电量不足，禁止启动汽车");
                            }
                        }
                        if (substr.equals(SampleGattAttributes.REAL_TEMPERATURE)) {
                            String tempstr = substr2.substring(0, 2);
                            temf = Integer.parseInt(tempstr, 16);
                            if (temf >= 45) {
                                tvTemperature.setText("点击温度:" + temf + "℃");
                                tvStatus.setText("温度过高 禁止启动");
                            } else {
                                tvTemperature.setText("点击温度:" + temf + "℃");
                            }
                        }

                        if (substr.equals(SampleGattAttributes.USED_DAYS)) {
//                            String tem=tempetureTv.getText().toString();
//                            tempetureTv.setText(tem+"\n使用天数："+substr2);
                        }

                        if (substr.equals("04:99:66")) {// || substr.equals("04:BB:44") ){

                        }

                        if (substr.equals("04:BB:44")) {
                            strXqdDdy = "氙气灯电压->" + substr2;
                        }

                        if (substr.equals("04:DD:22")) {

                            if (substr2.equals("01:FE")) {
                                //氙气大灯故障:工作电压过高!
                            }

                            if (substr2.equals("02:FD")) {
                                //氙气大灯故障:工作电压过低
                            }

                            if (substr2.equals("03:FC")) {
                                //氙气大灯故障:工作温度过高
                            }

                            if (substr2.equals("04:FB")) {
                                //氙气大灯故障:点灯失败!"+"\n"+"氙气大灯已经关闭!
                            }
                        }

                        if (substr.equals("04:CC:33")) {

                            if (substr2.equals("31:CE")) {
                                //"车灯状态: 开"
                            }

                            if (substr2.equals("00:FF")) {
                                //车灯状态: 关"
                            }
                        }
                    }


                }
            } else if (action.equals(GlobalConsts.ACTION_CONNECT_CHANGE)) {
                int status = intent.getIntExtra(BluetoothLeClass.CONNECT_STATUS, BluetoothLeClass.STATE_DISCONNECTED);
                if (status == BluetoothLeClass.STATE_DISCONNECTED) {
                    connectChanged(false);
                } else {
                    connectChanged(true);
                }
            } else if (action.equals(GlobalConsts.ACTION_SCAN_BLE_OVER)) {
                int status = intent.getIntExtra(BleManager.SCAN_BLE_STATUS, 0);
                if (status == 0) {
                    showToastCenter("未能检测到该设备，请稍后重试");
                }
            } else if (action.equals(GlobalConsts.ACTION_SCAN_NEW_DEVICE)) {
                ArrayList<iBeaconClass.iBeacon> mLeDevices;
                mLeDevices = (ArrayList<iBeaconClass.iBeacon>) intent.getSerializableExtra(BleManager.SCAN_BLE_STATUS);
                showPopupWindow(EmergencyActivity.this, titleBar, mLeDevices);

            }
        }
    };


    private ListDialog dialog;

    public void showPopupWindow(Context context, View view, ArrayList<iBeaconClass.iBeacon> mLeDevices) {
        if (dialog == null) {
            dialog = new ListDialog(context, new ListDialog.ItemClickCallback() {
                @Override
                public void callback(iBeaconClass.iBeacon bean, int position) {
                    //点击设备连接
                    BleManager.getInstance(EmergencyActivity.this).realConnect(bean.name, bean.bluetoothAddress);
                }
            });
        }
        dialog.changeData(mLeDevices);
        dialog.show(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleManager.disconnect();
        unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    public void onClick(View v) {
        String newValue;
        byte[] dataToWrite;

        if (!BleManager.isConnSuccessful) {
            showToast("设备未连接");
            return;
        }

        switch (v.getId()) {
            case R.id.cb_floodlight:
//              点击之后的状态

                if (cbFloodlight.isChecked()) {

                    newValue = SampleGattAttributes.FLOODLIGHT_OPEN;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);

                    cbFloodlight.setChecked(true);
                    tvFloodlight.setChecked(true);
                    tvFloodlight.setText("照明灯(开启)");
                    cbWarninglight.setChecked(false);
                    tvWarninglight.setChecked(false);
                    tvWarninglight.setText("警示灯(关闭)");
                    cbUsb.setChecked(true);
                    tvUsb.setChecked(true);
                    tvUsb.setText("USB输出(开启)");
                } else {
                    newValue = SampleGattAttributes.FLOODLIGHT_CLOSE;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);

                    cbFloodlight.setChecked(false);
                    tvFloodlight.setChecked(false);
                    tvFloodlight.setText("照明灯(关闭)");
                    cbUsb.setChecked(false);
                    tvUsb.setChecked(false);
                    tvUsb.setText("USB输出(关闭)");
                }
                break;
            case R.id.cb_warninglight:
                if (cbWarninglight.isChecked()) {

                    newValue = SampleGattAttributes.WARNINGLIGHT_FAST;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);

                    cbFloodlight.setChecked(false);
                    tvFloodlight.setChecked(false);
                    tvFloodlight.setText("照明灯(关闭)");
                    cbWarninglight.setChecked(true);
                    tvWarninglight.setChecked(true);
                    tvWarninglight.setText("警示灯(开启)");
                    cbUsb.setChecked(true);
                    tvUsb.setChecked(true);
                    tvUsb.setText("USB输出(开启)");
                    LogUtils.d(tvFloodlight.isChecked()+"-----");
                } else {
                    newValue = SampleGattAttributes.WARNINGLIGHT_CLOSE;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);


                    cbWarninglight.setChecked(false);
                    tvWarninglight.setChecked(false);
                    tvWarninglight.setText("警示灯(关闭)");
                    cbUsb.setChecked(false);
                    tvUsb.setChecked(false);
                    tvUsb.setText("USB输出(关闭)");
                }
                break;
            case R.id.cb_usb:
                if (cbUsb.isChecked()) {

                    newValue = SampleGattAttributes.USB_OPEN;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);

                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
                    cbUsb.setChecked(true);
                    tvUsb.setChecked(true);
                    tvUsb.setText("USB输出(开启)");
                } else {
                    newValue = SampleGattAttributes.USB_CLOSE;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
                    cbUsb.setChecked(false);
                    tvUsb.setChecked(false);
                    tvUsb.setText("USB输出(关闭)");
                    cbWarninglight.setChecked(false);
                    tvWarninglight.setChecked(false);
                    tvWarninglight.setText("警示灯(关闭)");
                    cbFloodlight.setChecked(false);
                    tvFloodlight.setChecked(false);
                    tvFloodlight.setText("照明灯(关闭)");
                }
                break;
        }


    }
}
