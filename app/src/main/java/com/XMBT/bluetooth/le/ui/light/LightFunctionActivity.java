package com.XMBT.bluetooth.le.ui.light;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.ui.start.EmergencyActivity;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.utils.Utils;
import com.XMBT.bluetooth.le.view.ListDialog;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.XMBT.bluetooth.le.view.datePicker.DateTimePicker;
import com.XMBT.bluetooth.le.view.datePicker.DoubleTimePicker;
import com.bumptech.glide.Glide;
import com.stx.xhb.xbanner.XBanner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 汽车智能照明系统
 */
public class LightFunctionActivity extends BaseActivity implements XBanner.XBannerAdapter {

    private XBanner xBanner;
    private List<String> bannerUrls = new ArrayList<>();

    private CheckBox cbAuto, cbManual, cbCity, cbHighway;
    private CheckBox cb30, cb60, cb90, cb120;

    private SeekBar seekBar;

    public final static String EXTRA_DATA = "EXTRA_DATA";
    private String strTemp;

    private int iStalls = 15;

    private View contentView;
    private Calendar cal;
    private int hour;
    private int minute, day;
    private String startTime, endTime;
    private TextView tvAutoInfo;
    private boolean flag = false;

    private TitleBar titleBar;
    private final Handler handler1 = new Handler();

    private BleManager bleManager;

    private DoubleTimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_function);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initBle();
        initDatas();
        initTimePickView();
        initView();
        cbAuto.setChecked(flag);
        if (BleManager.isConnSuccessful) {
            startTimer1();
            readDangwei();
        }
        registerBoradcastReceiver();
    }

    Calendar calendar;

    private void initTimePickView() {
        calendar = Calendar.getInstance();
        timePicker = new DoubleTimePicker(this, DoubleTimePicker.HOUR_OF_DAY);
        timePicker.setOnDateTimePickListener(new DoubleTimePicker.OnTwoTimePickListener() {
            @Override
            public void onPicked(String hour, String minute, String secHour, String secMinute) {
                startTime = hour + ":" + minute;
                endTime = secHour + ":" + secMinute;
                PreferenceUtils.write(LightFunctionActivity.this, "light_info", "starttime", startTime);
                PreferenceUtils.write(LightFunctionActivity.this, "light_info", "endtime", endTime);
                tvAutoInfo.setText(startTime + "开启 " + endTime + "关闭 长按设置时间");
            }
        });
    }

    private void showTimeRangePickView() {
        startTime = PreferenceUtils.readString(this, "light_info", "starttime");
        endTime = PreferenceUtils.readString(this, "light_info", "endtime");

        if (startTime == null && endTime == null) {
            startTime = "17:00";
            endTime = "23:00";
        }
        String[] starttimes = startTime.split(":");
        String[] endtimes = endTime.split(":");
        if (timePicker != null && !timePicker.isShowing()) {
            timePicker.setSelectedItem(Integer.valueOf(starttimes[0]), Integer.valueOf(starttimes[1]), Integer.valueOf(endtimes[0]), Integer.valueOf(endtimes[1]));
            timePicker.show();
        }
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

            if(!BleManager.CONNECT_TYPE.equals(GlobalConsts.LIGHTING)){
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

        flag = PreferenceUtils.readBoolean(this, "light_info", "flag", flag);

    }

    /**
     * 连接成功后不断发送命令
     * 防止蓝牙司机
     */
    private void startTimer1() {
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("lzz", "发送防止蓝牙死机命令");
                String newValue1 = SampleGattAttributes.WRITE_CRASH;
                byte[] dataToWrite1 = HexUtil.hexStringToBytes(newValue1);
                bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite1);
                handler1.postDelayed(this, 1000);
            }
        }, 1000);
    }

    /**
     * 档位
     */
    private void readDangwei() {
        String newValue = SampleGattAttributes.WRITE_SHIFT;
        byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);
        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
    }

    private void startTimer() {
        cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        int nowtime = hour * 60 + minute;
        String[] starttimestr = startTime.split(":");
        int starthour = Integer.valueOf(starttimestr[0]);
        int startminute = Integer.valueOf(starttimestr[1]);
        String[] endtimestr = endTime.split(":");
        int endhour = Integer.valueOf(endtimestr[0]);
        int endminute = Integer.valueOf(endtimestr[1]);
        int starttime = starthour * 60 + startminute;
        int endtime = endhour * 60 + endminute;
        if (cbAuto.isChecked()) {
            if (nowtime >= starttime && nowtime <= endtime) {
                //开灯指令
                String newValue = SampleGattAttributes.WRITE_OPEN_LIGHT;
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);
                bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
            }
        }
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
                    BleManager.getInstance(LightFunctionActivity.this).disconnect();
                } else {
                    BleManager.getInstance(LightFunctionActivity.this).startScan(LightFunctionActivity.this, GlobalConsts.LIGHTING);
                }
            }
        });
    }

    private void initView() {
        initTitle();
        xBanner = (XBanner) findViewById(R.id.xbanner);
        cbManual = (CheckBox) findViewById(R.id.cb_manual);
        cbCity = (CheckBox) findViewById(R.id.cb_city);
        cbHighway = (CheckBox) findViewById(R.id.cb_highway);
        cb30 = (CheckBox) findViewById(R.id.cb_30);
        cb60 = (CheckBox) findViewById(R.id.cb_60);
        cb90 = (CheckBox) findViewById(R.id.cb_90);
        cb120 = (CheckBox) findViewById(R.id.cb_120);
        tvAutoInfo = (TextView) findViewById(R.id.tv_autoInfo);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        xBanner.setData(bannerUrls, null);
        xBanner.setmAdapter(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String newValue = "046699";
                String strtemp = "";
                byte[] PwmValue = new byte[2];
                int i = seekBar.getProgress();
                PwmValue[0] = (byte) (((i & 0xFF) * iStalls / 15) & 0xff);
                PwmValue[1] = (byte) ~((byte) (((i & 0xFF) * iStalls / 15) & 0xff));
                strtemp = Utils.bytesToHexString(PwmValue);
                newValue += strtemp;
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);
                bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cbAuto = (CheckBox) findViewById(R.id.cb_auto);
        cbAuto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showTimeRangePickView();
                return true;
            }
        });
        startTime = PreferenceUtils.readString(this, "light_info", "starttime");
        endTime = PreferenceUtils.readString(this, "light_info", "endtime");

        if (startTime == null && endTime == null) {
            startTime = "17:00";
            endTime = "23:00";
        }
        tvAutoInfo.setText(startTime + "开启 " + endTime + "关闭 长按设置时间");
        connectChanged(BleManager.isConnSuccessful);
    }

    private void connectChanged(boolean isConnected) {
        if (isConnected) {
            titleBar.setTvRight("已连接");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.dark_blue));
        } else {
            titleBar.setTvRight("未连接");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.white));
        }
    }

    public void doClick(View view) {
        String newValue;
        byte[] dataToWrite;
        switch (view.getId()) {
            case R.id.cb_auto:
//                cal=Calendar.getInstance();
//                hour=cal.get(Calendar.HOUR_OF_DAY);
//                minute=cal.get(Calendar.MINUTE);
//                String[] starttimestr=starttime.split(":");
//                int starthour=Integer.valueOf(starttimestr[0]);
//                int startminute=Integer.valueOf(starttimestr[1]);
//                String[] endtimestr=endtime.split(":");
//                int endhour=Integer.valueOf(endtimestr[0]);
//                int endminute=Integer.valueOf(endtimestr[1]);
//                if(autoCb.isChecked()) {
//                    if ((hour >= starthour && minute >= startminute) && (hour <= endhour && minute <= endminute)) {
//                        String newValue = "0455AA31CE";
//                        byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//                        IndexFragment.WriteCharX(IndexFragment.gattCharacteristic_char1, dataToWrite);
//                    }
//                }else {
//                    String newValue =  "0455AA00FF";
//                    byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//                    IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_char1,dataToWrite );
//                }
                if (BleManager.isConnSuccessful) {
                    if (cbAuto.isChecked()) {
                        flag = true;
                        PreferenceUtils.write(LightFunctionActivity.this, "light_info", "flag", true);
                    } else {
                        PreferenceUtils.write(LightFunctionActivity.this, "light_info", "flag", false);
                    }
                } else {
                    showToast("请先连接设备");
                    resetShift();
                }
                break;
            case R.id.cb_manual:
                if (BleManager.isConnSuccessful) {
                    if (cbManual.isChecked()) {
                        newValue = SampleGattAttributes.WRITE_OPEN_LIGHT;
                    } else {
                        newValue = SampleGattAttributes.WRITE_CLOSE_LIGHT;
                    }
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                } else {
                    showToast("请先连接设备");
                    resetShift();
                }
                break;
            case R.id.cb_city:
                if (BleManager.isConnSuccessful) {
                    if (cbCity.isChecked()) {
                        cbHighway.setChecked(false);
                        seekBar.setProgress(0);
                        newValue = SampleGattAttributes.MODE_CITY;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    }
                } else {
                    showToast("请先连接设备");
                    resetShift();
                }
                break;
            case R.id.cb_highway:
                if (BleManager.isConnSuccessful) {
                    if (cbHighway.isChecked()) {
                        cbCity.setChecked(false);
                        seekBar.setProgress(15);
                        newValue = SampleGattAttributes.MODE_HIGHWAY;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    }
                } else {
                    showToast("请先连接设备");
                    resetShift();
                }
                break;
            case R.id.cb_30:
                if (BleManager.isConnSuccessful) {
                    if (cb30.isChecked()) {
                        cb60.setChecked(false);
                        cb90.setChecked(false);
                        cb120.setChecked(false);
                        newValue = SampleGattAttributes.SHIFT_30;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    } else {
                        newValue = SampleGattAttributes.WRITE_CLOSE_LIGHT;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    }
                } else {
                    showToast("请先连接设备");
                    resetShift();
                }
                break;
            case R.id.cb_60:
                if (BleManager.isConnSuccessful) {
                    if (cb60.isChecked()) {
                        cb30.setChecked(false);
                        cb90.setChecked(false);
                        cb120.setChecked(false);
                        newValue = SampleGattAttributes.SHIFT_60;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    } else {
                        newValue = SampleGattAttributes.WRITE_CLOSE_LIGHT;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    }
                } else {
                    showToast("请先连接设备");
                    resetShift();
                }
                break;
            case R.id.cb_90:
                if (BleManager.isConnSuccessful) {
                    if (cb90.isChecked()) {
                        cb30.setChecked(false);
                        cb60.setChecked(false);
                        cb120.setChecked(false);
                        newValue = SampleGattAttributes.SHIFT_90;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    } else {
                        newValue = SampleGattAttributes.WRITE_CLOSE_LIGHT;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    }
                } else {
                    showToast("请先连接设备");
                    resetShift();
                }
                break;
            case R.id.cb_120:
                if (BleManager.isConnSuccessful) {
                    if (cb120.isChecked()) {
                        cb30.setChecked(false);
                        cb60.setChecked(false);
                        cb90.setChecked(false);
                        newValue = SampleGattAttributes.SHIFT_120;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    } else {
                        newValue = SampleGattAttributes.WRITE_CLOSE_LIGHT;
                        dataToWrite = HexUtil.hexStringToBytes(newValue);
                        bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                    }
                } else {
                    showToast("请先连接设备");
                    resetShift();
                }
                break;
        }
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
//        registerReceiver(mBroadcastReceiver, myIntentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, myIntentFilter);

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

                if (strTemp.equals(SampleGattAttributes.AUTO_LIGHT)) {
                    //收到自动开灯的信号后判断时间区间
                    startTimer();
                }

                if (!strTemp.equals("0000000000")) {  //过滤掉00:00:00:00:00

                    if (strTemp.length() == 10) {
                        String substr = strTemp.substring(0, 6);
                        String substr2 = strTemp.substring(6, 10);

                        if (substr.equals(SampleGattAttributes.BATTERY_VOLTAGE)) {
                            //电池电压
                        }

                        if (substr.equals(SampleGattAttributes.BATTERY_TEMPERATURE)) {
                            //电池温度
                        }

                        if (substr.equals(SampleGattAttributes.DAYS_OF_USE)) {
                            //使用天数
                        }

                        if (substr.equals(SampleGattAttributes.WORK_VOLTAGE)) {
                            //工作电压
                        }

                        if (substr.equals(SampleGattAttributes.WORK_SHIFT)) {
                            //工作档位
                            strXqdDdy = "氙气灯电压->" + substr2;
                            String sub = substr2.substring(0, 2);
                            seekBar.setProgress(Integer.parseInt(sub, 16));
                        }

                        if (substr.equals(SampleGattAttributes.TROUBLE_INFO)) {
                            //故障信息
                            if (substr2.equals(SampleGattAttributes.TROUBLE_INFO_HIGH_VOLTAGE)) {
                                //氙气大灯故障:工作电压过高
                            }

                            if (substr2.equals(SampleGattAttributes.TROUBLE_INFO_LOW_VOLTAGE)) {
                                //氙气大灯故障:工作电压过低
                            }

                            if (substr2.equals(SampleGattAttributes.TROUBLE_INFO_HIGH_TEMPERATURE)) {
                                //氙气大灯故障:工作温度过高
                            }

                            if (substr2.equals(SampleGattAttributes.TROUBLE_INFO_FAILED)) {
                                //氙气大灯故障:点灯失败!"+"\n"+"氙气大灯已经关闭
                            }
                        }

                        if (substr.equals(SampleGattAttributes.UNAUTHORIZED)) {
                            new AlertDialog.Builder(LightFunctionActivity.this)
                                    .setMessage("此手机未认证，请打开车内大灯开关完成认证")
                                    .setPositiveButton("确定", null)
                                    .show();
                        }

                        if (strTemp.equals(SampleGattAttributes.LIGHT_ON)) {
                            cbManual.setChecked(true);
                        } else if (strTemp.equals(SampleGattAttributes.LIGHT_OFF)) {
                            cbManual.setChecked(false);
                            resetShift();
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
                showPopupWindow(LightFunctionActivity.this, titleBar, mLeDevices);

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
                    BleManager.getInstance(LightFunctionActivity.this).realConnect(bean.name, bean.bluetoothAddress);
                }
            });
        }
        dialog.changeData(mLeDevices);
        dialog.show(view);
    }

    private void resetShift() {
        cb30.setChecked(false);
        cb60.setChecked(false);
        cb90.setChecked(false);
        cb120.setChecked(false);
        cbCity.setChecked(false);
        cbHighway.setChecked(false);
        cbAuto.setChecked(false);
        cbManual.setChecked(false);
        seekBar.setProgress(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceUtils.write(LightFunctionActivity.this, "light_info", "starttime", startTime);
        PreferenceUtils.write(LightFunctionActivity.this, "light_info", "endtime", endTime);
//      editor.putInt("progress",seekBar.getProgress());
        handler1.removeCallbacksAndMessages(null);
        bleManager.disconnect();
//        unregisterReceiver(mBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

    }

}
