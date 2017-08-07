package com.XMBT.bluetooth.le.ui.device.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.ui.MainActivity;

/**
 * 汽车智能动力电池
 */
public class BateryActivity extends BaseActivity {

    private VoltageFragment2 voltageFragment;
    private StartTestFragment2 startTestFragment;
    private ChargeFragment chargeFragment;
    private DrivingFragment drivingFragment;

    private RadioButton[] btnAry = new RadioButton[4];
    private Fragment[] fragmentAry = null;
    private int currentIndex;
    private int selectedIndex;
    private MyButtonListener myButtonListener;

    private boolean isConnSuccessful;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batery);
        isConnSuccessful = getIntent().getBooleanExtra(MainActivity.CONNECTED_STATUS, false);
        initView();
        addListener();
        registerBoradcastReceiver();
    }

    private void initView() {
        btnAry[0] = (RadioButton) findViewById(R.id.radio1);
        btnAry[1] = (RadioButton) findViewById(R.id.radio2);
        btnAry[2] = (RadioButton) findViewById(R.id.radio3);
        btnAry[3] = (RadioButton) findViewById(R.id.radio4);
        voltageFragment = VoltageFragment2.newInstance(isConnSuccessful);
        startTestFragment = StartTestFragment2.newInstance(isConnSuccessful);
        chargeFragment = ChargeFragment.newInstance(isConnSuccessful);
        drivingFragment = DrivingFragment.newInstance(isConnSuccessful);
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
                    isConnSuccessful = false;
                } else {
                    //已连接
                    isConnSuccessful = true;
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
        MainActivity.disconnect();
        unregisterReceiver(mBroadcastReceiver);
    }
}
