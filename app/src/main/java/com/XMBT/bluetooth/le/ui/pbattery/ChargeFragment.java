package com.XMBT.bluetooth.le.ui.pbattery;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseFragment;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.utils.DensityUtils;
import com.XMBT.bluetooth.le.view.DashboardView;
import com.XMBT.bluetooth.le.view.ListDialog;
import com.XMBT.bluetooth.le.view.TitleBar;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 汽车智能动力电池--充电测试Fragment
 */
public class ChargeFragment extends BaseFragment implements View.OnClickListener {

    public final static String EXTRA_DATA = "EXTRA_DATA";

    private ProgressBar pb1, pb2;
    private ImageView ivTriangle1, ivTriangle2;
    private TextView tvVoltage1, tvVoltage2;
    private TextView tvInfo1, tvInfo2;
    private View view;
    private Button btnStart, btnRestart;
    private RelativeLayout firstLayout, secondLayout, thirdLayout;
    private TitleBar titleBar;
    private TextView tvLeftStandard1, tvLeftStandard2;
    private DashboardView dashboardView;

    /**
     * 标准电压
     */
    private int standardVoltage = 1350;
    /**
     * 最大电压
     */
    private int maxVoltage = 1800;
    /**
     * 收到的数据
     */
    private String strTemp;

    private List<Integer> lowVoltageList = new ArrayList<>();
    private List<Integer> highvVltageList = new ArrayList<>();

    private TimeCount timeCount;
    private float startDegree = 0.0f;
    private float endDegree = 45.0f;
    Handler handler = new Handler();

    private boolean isHigh = false;

    public static ChargeFragment newInstance(Boolean isConnSuccessful) {
        ChargeFragment itemFragement = new ChargeFragment();
        return itemFragement;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.charge_fragment, null);
        initViews();
        return view;
    }

    private void initTitle() {
        titleBar = (TitleBar) view.findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        titleBar.setRightOnClicker(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BleManager.isConnSuccessful){
                    BleManager.getInstance(getContext()).disconnect();
                }else{
                    BleManager.getInstance(getContext()).startScan(getContext(), GlobalConsts.BATTERY);
                }
            }
        });
    }

    private void initViews() {
        initTitle();
        pb1 = (ProgressBar) view.findViewById(R.id.pb1);
        pb2 = (ProgressBar) view.findViewById(R.id.pb2);
        ivTriangle1 = (ImageView) view.findViewById(R.id.iv_triangle1);
        ivTriangle2 = (ImageView) view.findViewById(R.id.iv_triangle2);
        tvVoltage1 = (TextView) view.findViewById(R.id.tv_voltage1);
        tvVoltage2 = (TextView) view.findViewById(R.id.tv_voltage2);
        tvInfo1 = (TextView) view.findViewById(R.id.tv_info1);
        tvInfo2 = (TextView) view.findViewById(R.id.tv_info2);
        btnStart = (Button) view.findViewById(R.id.btn_start);
        firstLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout1);
        secondLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout2);
        thirdLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout3);
        btnRestart = (Button) view.findViewById(R.id.btn_restart);
        btnStart.setOnClickListener(this);
        btnRestart.setOnClickListener(this);
        tvLeftStandard1 = (TextView) view.findViewById(R.id.tv_leftStandard1);
        tvLeftStandard2 = (TextView) view.findViewById(R.id.tv_leftStandard2);
        dashboardView = (DashboardView) view.findViewById(R.id.dashboardView);

        //画出标准区间
        int margin = standardVoltage * 300 / maxVoltage;  //求出标准所占的margin
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) tvLeftStandard1.getLayoutParams();
        rl.setMarginStart(DensityUtils.dip2px(getActivity(), margin));
        tvLeftStandard1.setLayoutParams(rl);
        tvLeftStandard2.setLayoutParams(rl);

        timeCount = new TimeCount(5000, 1000);
        connectChanged(BleManager.isConnSuccessful);
        registerBoradcastReceiver();
    }

    private void connectChanged(boolean isConnected) {
        if (isAdded()) {
            if (isConnected) {
                titleBar.setTvRight("已连接");
                titleBar.setTvRightTextColor(getResources().getColor(R.color.dark_blue));
            } else {
                titleBar.setTvRight("未连接");
                titleBar.setTvRightTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GlobalConsts.ACTION_NOTIFI)) {
                strTemp = intent.getStringExtra(EXTRA_DATA);

                if (!strTemp.equals("00:00:00:00:00")) {  //过滤掉00:00:00:00:00

                    if (strTemp.length() == 14) {
                        if (strTemp.substring(0, 2).equals("08")) {
                            String substr = strTemp.substring(3, 5) + strTemp.substring(6, 8);
                            int voltage10 = Integer.parseInt(substr, 16);
                            if (isHigh) {
                                highvVltageList.add(voltage10);
                            } else {
                                lowVoltageList.add(voltage10);
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
            }else if(action.equals(GlobalConsts.ACTION_SCAN_NEW_DEVICE)){
                if(isVisible){
                    ArrayList<iBeaconClass.iBeacon> mLeDevices;
                    mLeDevices = (ArrayList<iBeaconClass.iBeacon>) intent.getSerializableExtra(BleManager.SCAN_BLE_STATUS);
                    showPopupWindow(getContext(), view, mLeDevices);
                }
            }
        }
    };

    private void setProgress1(Integer ii) {
        int margin = ii * 300 / maxVoltage;  //求出progress所占的margin
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) ivTriangle1.getLayoutParams();
        rl.setMarginStart(DensityUtils.dip2px(getActivity(), margin) - DensityUtils.dip2px(getActivity(), 8));
        ivTriangle1.setLayoutParams(rl);
        pb1.setProgress(ii);
        float voltage = (float) ii / 100;
        DecimalFormat df = new DecimalFormat("0.00");
        tvVoltage1.setText(df.format(voltage) + "V");

        if (ii <= standardVoltage) {
            //电压偏低
            pb1.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress_warn));
            ivTriangle1.setImageResource(R.drawable.sort_down_red);
            tvVoltage1.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            tvInfo1.setText("怠速状态充电电压为" + df.format(voltage) + "V，电压偏低");
            tvInfo1.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else if (ii > standardVoltage) {
            //正常电压
            pb1.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress));
            ivTriangle1.setImageResource(R.drawable.sort_down_green);
            tvVoltage1.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            tvInfo1.setText("怠速状态充电电压为" + df.format(voltage) + "V，电压正常");
            tvInfo1.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private void setProgress2(Integer ii) {
        int margin = ii * 300 / maxVoltage;  //求出progress所占的margin
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) ivTriangle2.getLayoutParams();
        rl.setMarginStart(DensityUtils.dip2px(getActivity(), margin) - DensityUtils.dip2px(getActivity(), 8));
        ivTriangle2.setLayoutParams(rl);
        pb2.setProgress(ii);
        float voltage = (float) ii / 100;
        DecimalFormat df = new DecimalFormat("0.00");
        tvVoltage2.setText(df.format(voltage) + "V");

        if (ii <= standardVoltage) {
            //电压偏低
            pb2.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress_warn));
            ivTriangle2.setImageResource(R.drawable.sort_down_red);
            tvVoltage2.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            tvInfo2.setText("高速状态充电电压为" + df.format(voltage) + "V，电压偏低");
            tvInfo2.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else if (ii > standardVoltage) {
            //正常电压
            pb2.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress));
            ivTriangle2.setImageResource(R.drawable.sort_down_green);
            tvVoltage2.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            tvInfo2.setText("高速状态充电电压为" + df.format(voltage) + "V，电压正常");
            tvInfo2.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                if(BleManager.isConnSuccessful){
                    isHigh = true;
                    firstLayout.setVisibility(View.GONE);
                    secondLayout.setVisibility(View.VISIBLE);
                    handler.postDelayed(task, 200);
                    timeCount.start();
                }else{
                    showToastCenter("请先连接设备");
                }
                break;
            case R.id.btn_restart:
                highvVltageList.clear();
                thirdLayout.setVisibility(View.GONE);
                secondLayout.setVisibility(View.GONE);
                firstLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    Runnable task = new Runnable() {
        @Override
        public void run() {
            //对应dashboardView中的setProgress()方法
            ObjectAnimator a = ObjectAnimator.ofFloat(dashboardView, "degree", startDegree, endDegree);
            a.setInterpolator(new LinearInterpolator());
            a.setDuration(500);
            a.start();
            startDegree = endDegree;
            endDegree = randomFloat();
            handler.postDelayed(this, 500);
        }
    };


    private class TimeCount extends CountDownTimer {

        TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            //移除消息
            handler.removeCallbacks(task);
            secondLayout.setVisibility(View.GONE);
            thirdLayout.setVisibility(View.VISIBLE);
            if (lowVoltageList.size() > 0 && highvVltageList.size() > 0) {
                Integer ii = Collections.max(lowVoltageList);
                Integer i2 = Collections.max(highvVltageList);
                setProgress1(ii);
                setProgress2(i2);
            }
        }
    }

    /**
     * 45.0 - 67.5 之间的随机数
     *
     * @return
     */
    private float randomFloat() {
        float min = 45.0f;
        float max = 67.5f;
        float f = min + ((max - min) * new Random().nextFloat());
        BigDecimal b = new BigDecimal(f);
        float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        return f1;
    }

    private ListDialog dialog;

    public void showPopupWindow(Context context, View view, ArrayList<iBeaconClass.iBeacon> mLeDevices) {
        if(dialog == null){
            dialog = new ListDialog(context, new ListDialog.ItemClickCallback() {
                @Override
                public void callback(iBeaconClass.iBeacon bean, int position) {
                    //点击设备连接
                    BleManager.getInstance(getContext()).realConnect(GlobalConsts.BATTERY, bean.bluetoothAddress);
                }
            });
        }
        dialog.changeData(mLeDevices);
        dialog.show(view);
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(GlobalConsts.ACTION_NAME_RSSI);
        myIntentFilter.addAction(GlobalConsts.ACTION_CONNECT_CHANGE);
        myIntentFilter.addAction(GlobalConsts.ACTION_NOTIFI);
        myIntentFilter.addAction(GlobalConsts.ACTION_SCAN_NEW_DEVICE);
        getActivity().registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    protected boolean isVisible;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            //不可见
            isVisible = false;
        }else{
            isVisible = true;
        }
    }
}
