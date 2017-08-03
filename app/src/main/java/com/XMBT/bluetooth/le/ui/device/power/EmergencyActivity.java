package com.XMBT.bluetooth.le.ui.device.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.ui.device.IndexFragment;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.view.ChargingProgess;
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

    private boolean isConnSuccessful;

    private TitleBar titleBar;

    public final static String EXTRA_DATA= "EXTRA_DATA";

    private String strTemp;

    private TextView tvVoltage, tvStatus, tvTemperature;
    private CheckBox cbFloodlight, tvWarninglight, cbUsb, tvFloodlight, cbWarninglight, tvUsb;

    private float temf, volf;
    private ChargingProgess chargingprigressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        initDatas();
        initView();
        registerBoradcastReceiver();
    }

    private void initDatas() {
        isConnSuccessful = getIntent().getBooleanExtra(IndexFragment.CONNECTED_STATUS, false);
        bannerUrls.add(GlobalConsts.BANNER_URL0);
        bannerUrls.add(GlobalConsts.BANNER_URL1);
        bannerUrls.add(GlobalConsts.BANNER_URL2);
        bannerUrls.add(GlobalConsts.BANNER_URL3);
        bannerUrls.add(GlobalConsts.BANNER_URL4);
    }

    private void initTitle() {
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setAction(new TitleBar.TitleBarOnClickListener() {
            @Override
            public void performAction(View view) {
                onBackPressed();
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

        connectChanged(isConnSuccessful);
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

    private void connectChanged(boolean isConnected){
        if(isConnected){
            titleBar.setTvRight("已连接");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.dark_blue));
        }else{
            titleBar.setTvRight("未连接");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.white));
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String strXqdDd = "";
            String strXqdDdy = "";
            if (action.equals(GlobalConsts.ACTION_NOTIFI)){
                strTemp = intent.getStringExtra(EXTRA_DATA);

                if (!strTemp.equals("00:00:00:00:00")){  //过滤掉00:00:00:00:00

                    if(strTemp.equals("EE")){

                    } else{
                        String substr = strTemp.substring(0,8);
                        String substr2 = strTemp.substring(9,14);

                        if ( substr.equals("04:A7:58") ){
                            String voltagestr=substr2.substring(0,2);
                            int vol10=Integer.parseInt(voltagestr,16);
                            volf = vol10/10f;
                            if(volf < 10.5){
                                tvVoltage.setText(volf+"V  \n电池电压");
                                tvVoltage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            }else {
                                tvVoltage.setText(volf+"V  \n电池电压");
                            }
                        }
                        if(substr.equals("04:DA:25")){
                            if(substr2.equals("05:FA")){
                                chargingprigressView.setDCAnimation(18);
                                tvStatus.setText("电源良好，允许启动汽车");
                            }else if(substr2.equals("04:FB")){
                                chargingprigressView.setDCAnimation(14);
                                tvStatus.setText("电源良好，允许启动汽车");
                            }else if(substr2.equals("03:FC")){
                                chargingprigressView.setDCAnimation(10);
                                tvStatus.setText("电量不足，禁止启动汽车");
                            }else if(substr2.equals("02:FD")){
                                chargingprigressView.setDCAnimation(6);
                                tvStatus.setText("电量不足，禁止启动汽车");
                            }else if(substr2.equals("01:FE")){
                                chargingprigressView.setDCAnimation(2);
                                tvStatus.setText("电量不足，禁止启动汽车");
                            }
                        }
                        if ( substr.equals("06:B8:47") ){
                            String tempstr=substr2.substring(0,2);
                            temf = Integer.parseInt(tempstr,16);
                            if(temf >= 45){
                                tvTemperature.setText("点击温度:"+temf+"℃");
                                tvStatus.setText("温度过高 禁止启动");
                            }else {
                                tvTemperature.setText("点击温度:"+temf+"℃");
                            }
                        }

                        if ( substr.equals("06:C9:36") ){
//                            String tem=tempetureTv.getText().toString();
//                            tempetureTv.setText(tem+"\n使用天数："+substr2);
                        }

                        if (substr.equals("04:99:66")) {// || substr.equals("04:BB:44") ){

                        }

                        if (substr.equals("04:BB:44")){
                            strXqdDdy = "氙气灯电压->"+ substr2;
                        }

                        if ( substr.equals("04:DD:22")){

                            if( substr2.equals("01:FE")){
                                //氙气大灯故障:工作电压过高!
                            }

                            if ( substr2.equals("02:FD")){
                               //氙气大灯故障:工作电压过低
                            }

                            if ( substr2.equals("03:FC")){
                                //氙气大灯故障:工作温度过高
                            }

                            if ( substr2.equals("04:FB")){
                                //氙气大灯故障:点灯失败!"+"\n"+"氙气大灯已经关闭!
                            }
                        }

                        if ( substr.equals("04:CC:33")){

                            if ( substr2.equals("31:CE" )){
                                //"车灯状态: 开"
                            }

                            if ( substr2.equals("00:FF")){
                                //车灯状态: 关"
                            }
                        }

                    }
                }
            }

            if(action.equals(GlobalConsts.ACTION_CONNECT_CHANGE)){
                int status = intent.getIntExtra(BluetoothLeClass.CONNECT_STATUS, BluetoothLeClass.STATE_DISCONNECTED);
                if(status == BluetoothLeClass.STATE_DISCONNECTED)	{
                    connectChanged(false);
                } else{
                    connectChanged(true);
                }
            }
        }
    };


    @Override
    public void onClick(View v) {
        String newValue;
        byte[] dataToWrite;

        switch (v.getId()){
            case R.id.cb_floodlight:
                if (cbFloodlight.isChecked()){
                    if(volf<10.5 || temf>55){
                        showToast("温度过高，禁止启动");
                        return;
                    }
                    newValue = "0452AD31CE";
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    IndexFragment.WriteCharX(IndexFragment.gattCharacteristic_write, dataToWrite);
                    tvFloodlight.setChecked(true);
                    tvFloodlight.setText("照明灯(开启)");
                }else {
                    newValue = "0452AD00FF";
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_write, dataToWrite);
                    tvFloodlight.setChecked(false);
                    tvFloodlight.setText("照明灯(关闭)");
                }
                break;
            case R.id.cb_warninglight:
                if(cbWarninglight.isChecked()){
                    if(volf<10.5 || temf>55){
                        showToast("温度过高，禁止启动");
                        return;
                    }
                    newValue = "04639C8679";
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_write,dataToWrite );
                    tvWarninglight.setChecked(true);
                    tvWarninglight.setText("警示灯(开启)");
                }else {
                    newValue = "04639C00FF";
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_write,dataToWrite );
                    tvWarninglight.setChecked(false);
                    tvWarninglight.setText("警示灯(关闭)");
                }
                break;
            case R.id.cb_usb:
                if(cbUsb.isChecked()){
                    if(volf < 10.5 || temf > 55){
                        showToast("温度过高，禁止启动");
                        return;
                    }
                    newValue = "04748B9768";
                    dataToWrite = HexUtil.hexStringToBytes(newValue);

                    IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_write,dataToWrite );
                    tvUsb.setChecked(true);
                    tvUsb.setText("USB输出(开启)");
                }else {
                    newValue = "04748B00FF";
                    dataToWrite = HexUtil.hexStringToBytes(newValue);
                    IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_write,dataToWrite );
                    tvUsb.setChecked(false);
                    tvUsb.setText("USB输出(关闭)");
                }
                break;
        }
    }
}
