package com.XMBT.bluetooth.le.ui.start;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.CacheConsts;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.ui.light.LightFunctionActivity;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.PhoneInfoUtils;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.ChargingProgess;
import com.XMBT.bluetooth.le.view.ListDialog;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.XMBT.bluetooth.le.view.dialog.InputDialog;
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
    private TextView tvFloodlight, tvWarninglight, tvUsb;
    private LinearLayout llFloodlight, llWarninglight, llUsb;
    private ImageView ivQuestion;

    private float temf, volf;
    private ChargingProgess chargingprigressView;

    private BleManager bleManager;

    private InputDialog inputDialog;
    private InputDialog changeDialog;

    private RelativeLayout rl;


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
        //第一次进来正常扫描，然后连接
        //第二次进来不用连接
        //正在连接着进来，先断开连接，再判断连接
        if (!bleManager.isSupportBle()) {
            showToast(getResources().getString(R.string.ble_not_supported));
        }

        if(BleManager.isConnSuccessful){

            if(!BleManager.CONNECT_TYPE.equals(GlobalConsts.POWER)){
                bleManager.disconnect();
                //如果有连接过，下一次自动连接
                String address = PreferenceUtils.readString(this, GlobalConsts.SP_BLUETOOTH_DEVICE, GlobalConsts.POWER, "");
                if(!TextUtils.isEmpty(address)){
                    bleManager.realConnect(GlobalConsts.POWER, address);
                }else{
                    bleManager.startScan(this, GlobalConsts.POWER);
                }
            }

        }else{
            //如果有连接过，下一次自动连接
            String address = PreferenceUtils.readString(this, GlobalConsts.SP_BLUETOOTH_DEVICE, GlobalConsts.POWER, "");
            if(!TextUtils.isEmpty(address)){
                bleManager.realConnect(GlobalConsts.POWER, address);
            }else{
                bleManager.startScan(this, GlobalConsts.POWER);
            }
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

        rl = (RelativeLayout) findViewById(R.id.rl);
        tvVoltage = (TextView) findViewById(R.id.tv_voltage);
        chargingprigressView = (ChargingProgess) findViewById(R.id.chargingprigressView);
        tvTemperature = (TextView) findViewById(R.id.tv_temperature);
        findViewById(R.id.iv_question).setOnClickListener(this);

        llFloodlight = (LinearLayout) findViewById(R.id.ll_floodlight);
        llWarninglight = (LinearLayout) findViewById(R.id.ll_warninglight);
        llUsb = (LinearLayout) findViewById(R.id.ll_usb);
        tvFloodlight = (TextView) findViewById(R.id.tv_floodlight);
        tvWarninglight = (TextView) findViewById(R.id.tv_warninglight);
        tvUsb = (TextView) findViewById(R.id.tv_usb);

        tvStatus = (TextView) findViewById(R.id.tv_status);

        xBanner = (XBanner) findViewById(R.id.xbanner);
        xBanner.setData(bannerUrls, null);
        xBanner.setmAdapter(this);

        llFloodlight.setOnClickListener(this);
        llWarninglight.setOnClickListener(this);
        llUsb.setOnClickListener(this);

        findViewById(R.id.ll_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BleManager.isConnSuccessful) {
                    showToast("请先连接设备");
                    return;
                }
                changeDialog.showDialog();
            }
        });

        inputDialog = new InputDialog(EmergencyActivity.this);
        inputDialog.setOnButtonListener(new InputDialog.OnButtonListener() {
            @Override
            public void onPositive(String str) {
                if(TextUtils.isEmpty(str) || str.length() != 4){
                    showToast("请输入正确的4位数字密码");
                }else{
                    writePwd(SampleGattAttributes.SEND_PWD, str);
                    inputDialog.dismiss();
                }
            }

            @Override
            public void onNavigate() {
                BleManager.getInstance(EmergencyActivity.this).disconnect();
            }
        });

        changeDialog = new InputDialog(EmergencyActivity.this);
        changeDialog.setCancelable(true);
        changeDialog.setCanceledOnTouchOutside(true);
        changeDialog.setOnButtonListener(new InputDialog.OnButtonListener() {
            @Override
            public void onPositive(String str) {
                if(TextUtils.isEmpty(str) || str.length() != 4){
                    showToast("请输入正确的4位数字密码");
                }else{
                    writePwd(SampleGattAttributes.CHANGE_PWD, str);
                    changeDialog.dismiss();
                }
            }

            @Override
            public void onNavigate() {

            }
        });

        connectChanged(BleManager.isConnSuccessful);
        initAll();
    }

    private void initAll(){
        tvVoltage.setText(CacheConsts.voltage);
        tvTemperature.setText(CacheConsts.temperature);
        tvStatus.setText(CacheConsts.tvStatus);
        tvStatus.setEnabled(CacheConsts.tvStatusBoolean);
        if(CacheConsts.tvStatusBoolean){
            rl.setBackground(getResources().getDrawable(R.drawable.label_shape_red));
        }else{
            rl.setBackground(getResources().getDrawable(R.drawable.label_shape_green));
        }
        chargingprigressView.setDCAnimation(CacheConsts.DCAnimation);
        tvFloodlight.setEnabled(CacheConsts.floodlight);
        tvWarninglight.setEnabled(CacheConsts.warninglight);
        tvUsb.setEnabled(CacheConsts.usb);
    }

    private void writePwd(String instructions, String value){
        showLoadingDialog(null);
        String str = Integer.toHexString(Integer.valueOf(value));
        while (str.length() < 4){
            str = "0" + str;
        }
        String pwdInstructions = instructions + str;
        LogUtils.d("发送的密码为--" + pwdInstructions);
        byte[] dataToWrite1 = HexUtil.hexStringToBytes(pwdInstructions);
        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite1);
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
        myIntentFilter.addAction(GlobalConsts.ACTION_DICOVERD);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, myIntentFilter);
//        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private void connectChanged(boolean isConnected) {
        if (isConnected) {
            titleBar.setTvRight("已连接");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.dark_blue));
        } else {
            titleBar.setTvRight("未连接");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.white));
            tvStatus.setText("设备未连接");
            tvStatus.setEnabled(false);
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
                    LogUtils.d("通知指令为：" + strTemp);
                }
                //过滤掉00:00:00:00:00
                if (!strTemp.equals("0000000000")) {
                    //密码相关指令
                    if(strTemp.equals(SampleGattAttributes.PWD_WRONG)){
                        LogUtils.e("----收到密码错误指令----");
                        dismissLoadingDialog();
                        if(!inputDialog.isShowing()){
                            inputDialog.showDialog();
                        }
                    }else if(strTemp.equals(SampleGattAttributes.PWD_RIGHT)){
                        dismissLoadingDialog();
                        LogUtils.e("----收到密码正确指令----");
                    }else if(strTemp.equals(SampleGattAttributes.CHANGE_PWD_WRONG)){
                        dismissLoadingDialog();
                        showToast("修改密码失败");
                    }else if(strTemp.equals(SampleGattAttributes.CHANGE_PWD_RIGHT)){
                        showToast("修改密码成功");
                        dismissLoadingDialog();
                        //修改成功后发送密码至服务器
                    }else if(strTemp.equals(SampleGattAttributes.FLOODLIGHT_STATUS_OPEN)){
                        tvFloodlight.setEnabled(true);
                        CacheConsts.floodlight = true;
                        //白灯状态开
                    }else if(strTemp.equals(SampleGattAttributes.FLOODLIGHT_STATUS_CLOSE)){
                        tvFloodlight.setEnabled(false);
                        CacheConsts.floodlight = false;
                        //白灯状态关
                    }else if(strTemp.equals(SampleGattAttributes.WARNINGLIGHT_STATUS_OPEN)){
                        tvWarninglight.setEnabled(true);
                        CacheConsts.warninglight = true;
                        //红蓝状态开
                    }else if(strTemp.equals(SampleGattAttributes.WARNINGLIGHT_STATUS_CLOSE)){
                        tvWarninglight.setEnabled(false);
                        CacheConsts.warninglight = false;
                        //红蓝状态关
                    }else if(strTemp.equals(SampleGattAttributes.USB_STATUS_OPEN)){
                        tvUsb.setEnabled(true);
                        CacheConsts.usb = true;
                        //usb状态开
                    }else if(strTemp.equals(SampleGattAttributes.USB_STATUS_CLOSE)){
                        tvUsb.setEnabled(false);
                        CacheConsts.usb = false;
                        //usb状态关
                    }else if(strTemp.equals(SampleGattAttributes.NORMAL_POWER)){
                        CacheConsts.tvStatus = "电源良好 允许启动汽车";
                        tvStatus.setText(CacheConsts.tvStatus);
                        CacheConsts.voltageBoolean = false;
                        CacheConsts.tvStatusBoolean = false;
                    }else if(strTemp.equals(SampleGattAttributes.LOW_POWER)){
                        CacheConsts.tvStatus = "电量不足 禁止启动汽车";
                        tvStatus.setText(CacheConsts.tvStatus);
                        CacheConsts.tvStatusBoolean = true;
                        CacheConsts.voltageBoolean = true;
                    }else if(strTemp.equals(SampleGattAttributes.MCU_TO_APP)){
                        //收到该命令后，回应命令
                        String newValue1 = SampleGattAttributes.APP_TO_MCU;
                        byte[] dataToWrite1 = HexUtil.hexStringToBytes(newValue1);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite1);
                    }else if (strTemp.length() == 10) {
                        String substr = strTemp.substring(0, 6);
                        String substr2 = strTemp.substring(6, 10);

                        //温度优先级最高
                        if (substr.equals(SampleGattAttributes.REAL_TEMPERATURE)) {
                            String tempstr = substr2.substring(0, 2);
                            temf = Integer.parseInt(tempstr, 16);
                            CacheConsts.temperature = "电池温度：" + temf + "℃";
                            if (temf >= 45) {
                                tvTemperature.setText(CacheConsts.temperature);
                                CacheConsts.tvStatus = "温度过高 立即停止使用";
                                CacheConsts.tvStatusBoolean = true;
                                tvStatus.setText(CacheConsts.tvStatus);
                                tvStatus.setEnabled(CacheConsts.tvStatusBoolean);
                                rl.setBackground(getResources().getDrawable(R.drawable.label_shape_red));
                            } else {

                                if(!CacheConsts.voltageBoolean){
                                    tvTemperature.setText(CacheConsts.temperature);
                                    CacheConsts.tvStatusBoolean = false;
                                    tvStatus.setEnabled(CacheConsts.tvStatusBoolean);
                                    rl.setBackground(getResources().getDrawable(R.drawable.label_shape_green));
                                }else{
                                    tvTemperature.setText(CacheConsts.temperature);
                                    CacheConsts.tvStatusBoolean = true;
                                    tvStatus.setEnabled(CacheConsts.tvStatusBoolean);
                                    rl.setBackground(getResources().getDrawable(R.drawable.label_shape_red));
                                }

                            }
                        }else if (substr.equals(SampleGattAttributes.REAL_VOLTAGE)) {
                            String voltageStr = substr2.substring(0, 2);
                            int vol10 = Integer.parseInt(voltageStr, 16);
                            volf = vol10 / 10f;
                            CacheConsts.voltage = "电池电压：" + volf + "V";
                            tvVoltage.setText(CacheConsts.voltage);
                        }else if (substr.equals(SampleGattAttributes.BATTERY_INDICATOR)) {
                            if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_FIVE)) {
                                LogUtils.d("电池电量---18");
                                chargingprigressView.setDCAnimation(18);
                                CacheConsts.DCAnimation = 18;
                            } else if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_FOUR)) {
                                LogUtils.d("电池电量---14");
                                chargingprigressView.setDCAnimation(14);
                                CacheConsts.DCAnimation = 14;
                            } else if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_THREE)) {
                                LogUtils.d("电池电量---10");
                                chargingprigressView.setDCAnimation(10);
                                CacheConsts.DCAnimation = 10;
                            } else if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_TWO)) {
                                LogUtils.d("电池电量---6");
                                chargingprigressView.setDCAnimation(6);
                                CacheConsts.DCAnimation = 6;
                            } else if (substr2.equals(SampleGattAttributes.BATTERY_INDICATOR_ONE)) {
                                LogUtils.d("电池电量---2");
                                chargingprigressView.setDCAnimation(2);
                                CacheConsts.DCAnimation = 2;
                            }
                        }
                    }


                }
            }else if(action.equals(GlobalConsts.ACTION_DICOVERD)){
                LogUtils.d("准备发送mac地址");
                sendMac();
            } else if (action.equals(GlobalConsts.ACTION_CONNECT_CHANGE)) {
                int status = intent.getIntExtra(BluetoothLeClass.CONNECT_STATUS, BluetoothLeClass.STATE_DISCONNECTED);
                if (status == BluetoothLeClass.STATE_DISCONNECTED) {
                    connectChanged(false);
                } else {
                    connectChanged(true);
                }
            } else if (action.equals(GlobalConsts.ACTION_SCAN_BLE_OVER)) {
//                ArrayList<iBeaconClass.iBeacon> mLeDevices;
//                mLeDevices = (ArrayList<iBeaconClass.iBeacon>) intent.getSerializableExtra(BleManager.SCAN_BLE_DATA);
//                if(mLeDevices.size() > 0){
//                    showPopupWindow(EmergencyActivity.this, titleBar, mLeDevices);
//                }
            } else if (action.equals(GlobalConsts.ACTION_SCAN_NEW_DEVICE)) {
                ArrayList<iBeaconClass.iBeacon> mLeDevices;
                mLeDevices = (ArrayList<iBeaconClass.iBeacon>) intent.getSerializableExtra(BleManager.SCAN_BLE_DATA);
                showPopupWindow(EmergencyActivity.this, titleBar, mLeDevices);
            }
        }
    };

    private final Handler mHandler = new Handler();
    private int macCount;
    /**
     * 连接成功后发送mac地址最后两个byte,间隔300ms发送三次
     */
    private void sendMac() {

//        mHandler.postDelayed(this, 300);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                macCount = 0;
                String macAddress = PhoneInfoUtils.getMacAddress();
                macAddress = macAddress.substring(12,macAddress.length());
                macAddress = macAddress.replaceAll(":", "");
                String newValue1 = SampleGattAttributes.SEND_MAC + macAddress;
                byte[] dataToWrite1 = HexUtil.hexStringToBytes(newValue1);
                bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite1);
                macCount++;
                LogUtils.d("第" + macCount + "次发送mac地址--" + newValue1);
            }
        },1000);



//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(macCount != 3){
//                    //54:14:73:A0:47:B4
//                    String macAddress = PhoneInfoUtils.getMacAddress();
//                    macAddress = macAddress.substring(12,macAddress.length());
//                    macAddress = macAddress.replaceAll(":", "");
//                    String newValue1 = SampleGattAttributes.SEND_MAC + macAddress;
//                    byte[] dataToWrite1 = HexUtil.hexStringToBytes(newValue1);
//                    bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite1);
//                    macCount++;
//                    LogUtils.d("第" + macCount + "次发送mac地址--" + newValue1);
//                    mHandler.postDelayed(this, 300);
//                }
//
//            }
//        }, 300);
    }

    private ListDialog dialog;

    public void showPopupWindow(Context context, View view, ArrayList<iBeaconClass.iBeacon> mLeDevices) {
        if (dialog == null) {
            dialog = new ListDialog(context, new ListDialog.ItemClickCallback() {
                @Override
                public void callback(iBeaconClass.iBeacon bean, int position) {
                    BleManager.getInstance(EmergencyActivity.this).stopScan();
                    //点击设备连接
                    BleManager.getInstance(EmergencyActivity.this).realConnect(bean.name, bean.bluetoothAddress);
                }
            });
        }
        if (!dialog.isShowing()){
            dialog.show(view);
        }
        dialog.changeData(mLeDevices);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
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

            case R.id.iv_question:
                showToast("即将推出，敬请期待。");
                break;
            case R.id.ll_floodlight:

                if(!tvFloodlight.isEnabled()){
                    LogUtils.e("开");
                    newValue = SampleGattAttributes.FLOODLIGHT_OPEN;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
                }else{
                    LogUtils.e("关");
                    newValue = SampleGattAttributes.FLOODLIGHT_CLOSE;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
                }
                break;
            case R.id.ll_warninglight:
                if(!tvWarninglight.isEnabled()){
                    newValue = SampleGattAttributes.WARNINGLIGHT_FAST;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
                }else{
                    newValue = SampleGattAttributes.WARNINGLIGHT_CLOSE;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
                }
                break;
            case R.id.ll_usb:
                if(!tvUsb.isEnabled()){
                    newValue = SampleGattAttributes.USB_OPEN;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
                }else{
                    newValue = SampleGattAttributes.USB_CLOSE;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
                }

                break;
                default:
        }


    }
}
