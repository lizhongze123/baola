package com.XMBT.bluetooth.le.ui.pbattery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.ListDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 汽车智能动力电池
 */
public class BatteryActivity extends BaseActivity {

    private VoltageFragment voltageFragment;
    private StartTestFragment startTestFragment;
    private ChargeFragment chargeFragment;
    private DrivingFragment drivingFragment;

    private RadioButton[] btnAry = new RadioButton[4];
    private Fragment[] fragmentAry = null;
    private int currentIndex;
    private int selectedIndex;
    private MyButtonListener myButtonListener;

    private BleManager bleManager;
    private final Handler handler1 = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batery);
        StatusBarHelper.setStatusBarColor(this, R.color.black);
        registerBoradcastReceiver();
        initViews();
        addListener();
        initBle();
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

            if(!BleManager.CONNECT_TYPE.equals(GlobalConsts.BATTERY)){
                bleManager.disconnect();
                //如果有连接过，下一次自动连接
                String address = PreferenceUtils.readString(this, GlobalConsts.SP_BLUETOOTH_DEVICE, GlobalConsts.BATTERY, "");
                if(!TextUtils.isEmpty(address)){
                    bleManager.realConnect(GlobalConsts.BATTERY, address);
                }else{
                    bleManager.startScan(this, GlobalConsts.BATTERY);
                }
            }

        }else{
            //如果有连接过，下一次自动连接
            String address = PreferenceUtils.readString(this, GlobalConsts.SP_BLUETOOTH_DEVICE, GlobalConsts.BATTERY, "");
            if(!TextUtils.isEmpty(address)){
                bleManager.realConnect(GlobalConsts.BATTERY, address);
            }else{
                bleManager.startScan(this, GlobalConsts.BATTERY);
            }
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
                LogUtils.d("发送防止蓝牙死机命令");
                String newValue1 = SampleGattAttributes.WRITE_CRASH;
                byte[] dataToWrite1 = HexUtil.hexStringToBytes(newValue1);
                bleManager.WriteCharX(bleManager.gattCharacteristic_write, dataToWrite1);
                handler1.postDelayed(this, 2000);
            }
        }, 2000);
    }

    private void initViews() {
        btnAry[0] = (RadioButton) findViewById(R.id.radio1);
        btnAry[1] = (RadioButton) findViewById(R.id.radio2);
        btnAry[2] = (RadioButton) findViewById(R.id.radio3);
        btnAry[3] = (RadioButton) findViewById(R.id.radio4);
        voltageFragment = VoltageFragment.newInstance(false);
        startTestFragment = StartTestFragment.newInstance(false);
        chargeFragment = ChargeFragment.newInstance(false);
        drivingFragment = DrivingFragment.newInstance(false);
        fragmentAry = new Fragment[]{voltageFragment, startTestFragment, chargeFragment, drivingFragment};
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, voltageFragment);
        fragmentTransaction.show(voltageFragment);
        fragmentTransaction.commit();
    }

    private void addListener() {
        myButtonListener = new MyButtonListener();
        for (int i = 0; i < btnAry.length; i++) {
            btnAry[i].setOnClickListener(myButtonListener);
        }
    }

    /**
     * 注册广播接收器
     */
    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(GlobalConsts.ACTION_CONNECT_CHANGE);
        myIntentFilter.addAction(GlobalConsts.ACTION_SCAN_BLE_OVER);
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GlobalConsts.ACTION_CONNECT_CHANGE)) {
                int status = intent.getIntExtra("CONNECT_STATUC", 0);
                if (status == 0) {
                    //断开连接
                    BleManager.isConnSuccessful = false;
                    handler1.removeCallbacksAndMessages(null);
                } else {
                    //已连接
                    BleManager.isConnSuccessful = true;
                    startTimer1();

                }
            } else if (action.equals(GlobalConsts.ACTION_SCAN_BLE_OVER)) {
                int status = intent.getIntExtra(BleManager.SCAN_BLE_STATUS, 0);
                if (status == 0) {
//                    showToastCenter("未能检测到该设备，请稍后重试");
                }
            }
        }
    };

    class MyButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.radio1:
                    selectedIndex = 0;
                    break;
                case R.id.radio2:
                    selectedIndex = 1;
                    break;
                case R.id.radio3:
                    selectedIndex = 2;
                    break;
                case R.id.radio4:
                    selectedIndex = 3;
                    break;
            }
            if (selectedIndex != currentIndex) {
                FragmentTransaction transation = getSupportFragmentManager().beginTransaction();
                transation.hide(fragmentAry[currentIndex]);
                if (!fragmentAry[selectedIndex].isAdded()) {
                    transation.add(R.id.fragment_container, fragmentAry[selectedIndex]);
                }
                transation.show(fragmentAry[selectedIndex]);
                transation.commit();
                btnAry[selectedIndex].setSelected(true);
                btnAry[currentIndex].setSelected(false);
                currentIndex = selectedIndex;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }



}
