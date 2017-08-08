package com.XMBT.bluetooth.le.ui.light;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.utils.Utils;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.bumptech.glide.Glide;
import com.stx.xhb.xbanner.XBanner;

import org.zackratos.ultimatebar.UltimateBar;

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
    private TimePicker tp0, tp1;

    public final static String EXTRA_DATA = "EXTRA_DATA";
    private String strTemp;

    private int iStalls = 15;
    private PopupWindow popupWindow;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_function);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initBle();
        initDatas();
        initPopWindow();
        initView();
        cbAuto.setChecked(flag);
        if(BleManager.isConnSuccessful){
            startTimer1();
            readDangwei();
        }
        registerBoradcastReceiver();
    }

    private void initBle() {
        bleManager = BleManager.getInstance(this);
        if (!bleManager.isSupportBle()) {
            showToast(getResources().getString(R.string.ble_not_supported));
        }
        bleManager.startScan(this,GlobalConsts.LIGHTING);
    }

    private void initDatas() {
        bannerUrls.add(GlobalConsts.BANNER_URL0);
        bannerUrls.add(GlobalConsts.BANNER_URL1);
        bannerUrls.add(GlobalConsts.BANNER_URL2);
        bannerUrls.add(GlobalConsts.BANNER_URL3);
        bannerUrls.add(GlobalConsts.BANNER_URL4);

        startTime = PreferenceUtils.readString(this, "light_info", "starttime");
        endTime = PreferenceUtils.readString(this, "light_info", "endtime");
        flag = PreferenceUtils.readBoolean(this, "light_info", "flag", flag);
        if (startTime == null && endTime == null) {
            startTime = "17:00";
            endTime = "23:00";
        }
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

    private void initPopWindow() {
        //加载弹出框的布局
        contentView = LayoutInflater.from(this).inflate(R.layout.pop_item, null);
        //设置弹出框的宽度和高度
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);// 取得焦点
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //点击外部消失
        popupWindow.setOutsideTouchable(true);
        //设置可以点击
        popupWindow.setTouchable(true);
        //进入退出的动画
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        cbAuto = (CheckBox) findViewById(R.id.cb_auto);
        //设置点击返回键dismiss
        cbAuto.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
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
                if(BleManager.isConnSuccessful){
                    BleManager.getInstance(LightFunctionActivity.this).disconnect();
                }else{
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
        tp0 = (TimePicker) contentView.findViewById(R.id.tp0);
        tp1 = (TimePicker) contentView.findViewById(R.id.tp1);
        tp0.setIs24HourView(true);
        tp1.setIs24HourView(true);
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
        cbAuto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //从底部显示
                popupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
                String[] starttimes = startTime.split(":");
                String[] endtimes = endTime.split(":");
                tp0.setCurrentHour(Integer.valueOf(starttimes[0]));
                tp0.setCurrentMinute(Integer.valueOf(starttimes[1]));
                tp1.setCurrentHour(Integer.valueOf(endtimes[0]));
                tp1.setCurrentMinute(Integer.valueOf(endtimes[1]));
                return true;
            }
        });
        tp0.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (minute < 10) {
                    startTime = hourOfDay + ":0" + minute;
                } else {
                    startTime = hourOfDay + ":" + minute;
                }
                if (hourOfDay < 10) {
                    startTime = "0" + hourOfDay + ":" + minute;
                } else {
                    startTime = hourOfDay + ":" + minute;
                }
            }
        });
        tp1.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (minute < 10) {
                    endTime = hourOfDay + ":0" + minute;
                } else {
                    endTime = hourOfDay + ":" + minute;
                }
                if (hourOfDay < 10) {
                    endTime = "0" + hourOfDay + ":" + minute;
                } else {
                    endTime = hourOfDay + ":" + minute;
                }
            }
        });
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
                if (cbAuto.isChecked()) {
                    flag = true;
                    PreferenceUtils.write(LightFunctionActivity.this, "light_info", "flag", true);
                } else {
                    PreferenceUtils.write(LightFunctionActivity.this, "light_info", "flag", false);
                }
                break;
            case R.id.confirmBtn:
                PreferenceUtils.write(LightFunctionActivity.this, "light_info", "starttime", startTime);
                PreferenceUtils.write(LightFunctionActivity.this, "light_info", "endtime", startTime);
                tvAutoInfo.setText(startTime + "开启 " + endTime + "关闭 长按设置时间");
                popupWindow.dismiss();
                break;
            case R.id.cb_manual:
                if (cbManual.isChecked()) {
                    newValue = SampleGattAttributes.WRITE_OPEN_LIGHT;
                } else {
                    newValue = SampleGattAttributes.WRITE_CLOSE_LIGHT;
                }
                dataToWrite = HexUtil.hexStringToBytes(newValue);
                bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                break;
            case R.id.cb_city:
                if (cbCity.isChecked()) {
                    cbHighway.setChecked(false);
                    seekBar.setProgress(0);
                    newValue = SampleGattAttributes.MODE_CITY;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                }
                break;
            case R.id.cb_highway:
                if (cbHighway.isChecked()) {
                    cbCity.setChecked(false);
                    seekBar.setProgress(15);
                    newValue = SampleGattAttributes.MODE_HIGHWAY;
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite);
                }
                break;
            case R.id.cb_30:
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
                break;
            case R.id.cb_60:
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
                break;
            case R.id.cb_90:
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
                break;
            case R.id.cb_120:
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
        registerReceiver(mBroadcastReceiver, myIntentFilter);
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
            }
        }
    };

    private void resetShift() {
        cb30.setChecked(false);
        cb60.setChecked(false);
        cb90.setChecked(false);
        cb120.setChecked(false);
        cbCity.setChecked(false);
        cbHighway.setChecked(false);
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
        PreferenceUtils.write(LightFunctionActivity.this, "light_info", "endtime", startTime);
//      editor.putInt("progress",seekBar.getProgress());
        handler1.removeCallbacksAndMessages(null);
        unregisterReceiver(mBroadcastReceiver);
    }

}
