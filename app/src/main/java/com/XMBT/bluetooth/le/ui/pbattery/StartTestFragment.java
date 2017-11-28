package com.XMBT.bluetooth.le.ui.pbattery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.BatteryCache;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.utils.DateFormatUtils;
import com.XMBT.bluetooth.le.utils.DensityUtils;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.view.LineChart.ItemBean;
import com.XMBT.bluetooth.le.view.LineChart.LineView2;
import com.XMBT.bluetooth.le.view.ListDialog;
import com.XMBT.bluetooth.le.view.TitleBar;

import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 汽车智能动力电池--动力测试
 */
public class StartTestFragment extends Fragment {

    private View view;
    private ProgressBar progressBar;
    private ImageView statusIv;
    private ImageView ivTriangle;
    private TextView tvVoltage;
    private TextView tvTime;
    private TextView tvInfo;
    private TitleBar titleBar;
    private LineView2 lineView;
    private TextView tvLeftStandard;

    /**是否接收到启动信号*/
    private boolean isReceiveSignal;

    public final static String EXTRA_DATA = "EXTRA_DATA";
    /**
     * 收到的数据
     */
    private String strTemp;
    /**
     * 最大电压
     */
    private int maxVoltage = 1800;
    /**
     * 标准电压
     */
    private int standardVoltage = 950;
    /**
     * 检测时间
     */
    private String date;
    /**
     * 收到启动信号的时间
     */
    private long startTime;
    /**
     * 存放启动信号之后两秒内的集合
     */
    private List<Integer> voltageList;
    /**
     * 存放启动信号之前的10个电压值
     */
    private List<Integer> beforeList = new ArrayList<>();

    private List<ItemBean> mItems;

    public static StartTestFragment newInstance(Boolean isConnSuccessful) {
        StartTestFragment itemFragement = new StartTestFragment();
        return itemFragement;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.fragment_starttest, null);
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
                if (BleManager.isConnSuccessful) {
                    BleManager.getInstance(getContext()).disconnect();
                } else {
                    BleManager.getInstance(getContext()).startScan(getContext(), GlobalConsts.BATTERY);
                }
            }
        });
    }

    private void initViews() {
        initTitle();
        lineView = (LineView2) view.findViewById(R.id.myline);
        tvTime = (TextView) view.findViewById(R.id.tvTime);
        progressBar = (ProgressBar) view.findViewById(R.id.pb);
        ivTriangle = (ImageView) view.findViewById(R.id.ivTriangle);
        tvVoltage = (TextView) view.findViewById(R.id.tvVoltage);
        statusIv = (ImageView) view.findViewById(R.id.statusIv);
        tvInfo = (TextView) view.findViewById(R.id.tvInfo);
        tvLeftStandard = (TextView) view.findViewById(R.id.tv_leftStandard);

        //画出标准区间
        int margin = standardVoltage * 300 / maxVoltage;  //求出标准所占的margin
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) tvLeftStandard.getLayoutParams();
        rl.setMarginStart(DensityUtils.dip2px(getActivity(), margin));
        tvLeftStandard.setLayoutParams(rl);

        connectChanged(BleManager.isConnSuccessful);
        registerBoradcastReceiver();
        initAll();
    }

    private void initAll(){
        tvTime.setText(BatteryCache.testDate);
        tvVoltage.setText(BatteryCache.testVolatage);
        if(BatteryCache.testVoltageStatus.equals("red")){
            //电压偏低
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress_warn));
            ivTriangle.setImageResource(R.drawable.sort_down_red);
            tvVoltage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            statusIv.setImageResource(R.drawable.startvoltage_warn);
            tvInfo.setText("启动电压为" + BatteryCache.testInfo+ "V，电压偏低");
            tvInfo.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else  if(BatteryCache.testVoltageStatus.equals("green")){
            //正常电压
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress));
            ivTriangle.setImageResource(R.drawable.sort_down_green);
            tvVoltage.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            statusIv.setImageResource(R.drawable.startvoltage_normal);
            tvInfo.setText("启动电压为" + BatteryCache.testInfo+ "V，电压正常");
            tvInfo.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
        if(BatteryCache.mItems != null){
            lineView.setItems(BatteryCache.mItems);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GlobalConsts.ACTION_NOTIFI)) {
                strTemp = intent.getStringExtra(EXTRA_DATA);

                if (!TextUtils.isEmpty(strTemp)) {
                    strTemp = strTemp.replaceAll(":", "");
                }

                if (!strTemp.equals("0000000000")) {  //过滤掉00:00:00:00:00

                    if (strTemp.length() == 10) {

                        String substr = strTemp.substring(0, 6);
                        String substr2 = strTemp.substring(6, 10);

                        if (strTemp.equals(SampleGattAttributes.START_SIGNAL)) {
                            //启动信号
                            LogUtils.d("收到启动信号");
                            isReceiveSignal = true;
                            startTime = System.currentTimeMillis();
                            date = DateFormatUtils.getDate(startTime, DateFormatUtils.FORMAT_ALL);
                            tvTime.setText(date);
                            BatteryCache.testDate = date;
                            voltageList = new ArrayList<>();
                            mItems = new ArrayList<>();
                        } else if(substr.equals(SampleGattAttributes.P_BATTERY_VOLTAGE)){
                            long currentTime = System.currentTimeMillis();
                            int voltage10 = Integer.parseInt(substr2, 16);
                            if(isReceiveSignal){
                                if (currentTime < startTime + 3600) {
                                    //保存收到启动信号后的数据
                                    voltageList.add(voltage10);
//                                addBean(currentTime, voltage10);
                                } else if (currentTime > startTime + 3600) {
                                    //接收两秒的数据完毕
                                    Integer ii = Collections.min(voltageList);
                                    setProgress(ii);
                                    voltageList.addAll(0,beforeList);
                                    addBean(currentTime);
                                    lineView.setItems(mItems);
                                    BatteryCache.mItems = mItems;
                                    LogUtils.e("mItem---"+mItems.size());
                                    isReceiveSignal = false;
                                }
                            }else{
                                //一直保存两秒的数据
                                if(beforeList.size() == 10){
                                    beforeList.remove(0);
                                }
                                beforeList.add(voltage10);
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

    private ListDialog dialog;

    public void showPopupWindow(Context context, View view, ArrayList<iBeaconClass.iBeacon> mLeDevices) {
        if(dialog == null){
            dialog = new ListDialog(context, new ListDialog.ItemClickCallback() {
                @Override
                public void callback(iBeaconClass.iBeacon bean, int position) {
                    //点击设备连接
                    BleManager.getInstance(getContext()).realConnect(bean.name, bean.bluetoothAddress);
                }
            });
        }
        dialog.changeData(mLeDevices);
        dialog.show(view);
    }

    /**
     * @param ii 放大100倍
     */
    private void setProgress(Integer ii) {
        int margin = ii * 300 / maxVoltage;  //求出progress所占的margin
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) ivTriangle.getLayoutParams();
        rl.setMarginStart(DensityUtils.dip2px(getActivity(), margin) - DensityUtils.dip2px(getActivity(), 8));
        ivTriangle.setLayoutParams(rl);
        progressBar.setProgress(ii);
        float voltage = (float) ii / 100;
        DecimalFormat df = new DecimalFormat("0.00");
        tvVoltage.setText(df.format(voltage) + "V");
        BatteryCache.testVolatage = df.format(voltage) + "V";
        statusIv.setVisibility(View.VISIBLE);

        if (ii <= standardVoltage) {
            //电压偏低
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress_warn));
            ivTriangle.setImageResource(R.drawable.sort_down_red);
            tvVoltage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            statusIv.setImageResource(R.drawable.startvoltage_warn);
            tvInfo.setText("启动电压为" + df.format(voltage) + "V，电压偏低");
            tvInfo.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            BatteryCache.testInfo = "启动电压为" + df.format(voltage) + "V，电压偏低";
            BatteryCache.testVoltageStatus = "green";
        } else if (ii > standardVoltage) {
            //正常电压
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress));
            ivTriangle.setImageResource(R.drawable.sort_down_green);
            tvVoltage.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            statusIv.setImageResource(R.drawable.startvoltage_normal);
            tvInfo.setText("启动电压为" + df.format(voltage) + "V，电压正常");
            tvInfo.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            BatteryCache.testInfo = "启动电压为" + df.format(voltage) + "V，电压正常";
            BatteryCache.testVoltageStatus = "red";
        }
    }

    private void addBean(long currentTime, int voltage10) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        final String time0 = sdf.format(new Time(currentTime));
        ItemBean itemBean = new ItemBean(voltage10, time0, currentTime);
        mItems.add(itemBean);
    }

    private void addBean(long currentTime) {

        for (int i = 0; i < voltageList.size(); i++) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            final String time0 = sdf.format(new Time(currentTime));
            ItemBean itemBean = new ItemBean(voltageList.get(i), time0, currentTime);
            mItems.add(itemBean);
        }

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

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(GlobalConsts.ACTION_NAME_RSSI);
        myIntentFilter.addAction(GlobalConsts.ACTION_CONNECT_CHANGE);
        myIntentFilter.addAction(GlobalConsts.ACTION_NOTIFI);
        myIntentFilter.addAction(GlobalConsts.ACTION_SCAN_NEW_DEVICE);
//        getActivity().registerReceiver(mBroadcastReceiver, myIntentFilter);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, myIntentFilter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        getActivity().unregisterReceiver(mBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);

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