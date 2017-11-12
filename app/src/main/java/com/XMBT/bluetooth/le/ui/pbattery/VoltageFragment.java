package com.XMBT.bluetooth.le.ui.pbattery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseFragment;
import com.XMBT.bluetooth.le.bean.RecordBean;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.BatteryCache;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.db.DBManger;
import com.XMBT.bluetooth.le.utils.DateFormatUtils;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.ToastUtils;
import com.XMBT.bluetooth.le.view.LineChart.ItemBean;
import com.XMBT.bluetooth.le.view.LineChart.LineView;
import com.XMBT.bluetooth.le.view.ListDialog;
import com.XMBT.bluetooth.le.view.LoadingView;
import com.XMBT.bluetooth.le.view.TitleBar;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 汽车智能动力电池--电压测试Fragment
 */
public class VoltageFragment extends BaseFragment {

    private View view;

    public final static String EXTRA_DATA = "EXTRA_DATA";

    private TitleBar titleBar;
    private TextView tvStatus;
    private LoadingView loadingView;
    private String strTemp;
    private LineView myline;
    private TextView tvUseDay, tvStopDay, tvStartCounts;

    /**
     * 折线数据
     */
    private List<ItemBean> mItems = Collections.synchronizedList(new ArrayList<ItemBean>());
    private boolean firstInit = true;
    /**
     * 记录折线图x=0的title，删除数据用
     */
    private String tag;

    public static VoltageFragment newInstance(Boolean isConnSuccessful) {
        VoltageFragment itemFragement = new VoltageFragment();
        return itemFragement;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.voltage_fragment, null);
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
        myline = (LineView) view.findViewById(R.id.myline);
        myline.setLineListener(new LineView.OnLineListener() {
            @Override
            public void onChange() {
                synchronized (mItems) {
                    if (mItems.size() > 0) {
                        List<ItemBean> itemBeens = new ArrayList<>();
                        for (int i = 0; i < mItems.size(); i++) {
                            if (mItems.get(i).getTag().equals(tag)) {
                                itemBeens.add(mItems.get(i));
                            }
                        }
                        mItems.removeAll(itemBeens);
                        tag = mItems.get(0).getTag();
                    }
                }
            }
        });

        tvStatus = (TextView) view.findViewById(R.id.tv_status);
        loadingView = (LoadingView) view.findViewById(R.id.loadingView);

        tvUseDay = (TextView) view.findViewById(R.id.tv_useDay);
        tvStopDay = (TextView) view.findViewById(R.id.tv_stopDay);
        tvStartCounts = (TextView) view.findViewById(R.id.tv_startCounts);

        connectChanged(BleManager.isConnSuccessful);
        registerBoradcastReceiver();
        initAll();
    }

    private void initAll(){
        loadingView.setPercentText(BatteryCache.voltage);
        loadingView.setProgress(BatteryCache.progress);
        tvStatus.setText(BatteryCache.tvSstatus);
        tvUseDay.setText(BatteryCache.usedDay);
        tvStopDay.setText(BatteryCache.stopDay);
        tvStartCounts.setText(BatteryCache.startCounts);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(GlobalConsts.ACTION_NOTIFI)) {  //收到notify
                strTemp = intent.getStringExtra(EXTRA_DATA);
                //去除：
                if (!TextUtils.isEmpty(strTemp)) {
                    strTemp = strTemp.replaceAll(":", "");
                }

                if (!strTemp.equals("0000000000")) {  //过滤掉00:00:00:00:00

                    if (strTemp.length() == 10) {
                        String substr = strTemp.substring(0, 6);
                        String substr2 = strTemp.substring(6, 10);

                        if (substr.equals(SampleGattAttributes.USE_DAYS)) {
                            //试用天数
                            tvUseDay.setText(Integer.parseInt(substr2, 16) + "天");
                        } else if (substr.equals(SampleGattAttributes.STOP_DAYS)) {
                            //停用天数
                            tvStopDay.setText(Integer.parseInt(substr2, 16) + "天");
                        } else if (substr.equals(SampleGattAttributes.START_COUNT)) {
                            //启动次数
                            tvStartCounts.setText(Integer.parseInt(substr2, 16) + "次");
                        } else if(strTemp.equals(SampleGattAttributes.BATTERY_CHARGING_LOW)){
                            tvStatus.setText("充电中 电压偏低");
                        } else if(strTemp.equals(SampleGattAttributes.BATTERY_CHARGING_NORMAL)){
                            tvStatus.setText("充电中 电压正常");
                        } else if(strTemp.equals(SampleGattAttributes.BATTERY_CHARGING_HIGHT)){
                            tvStatus.setText("充电中 电压过高");
                        } else if(strTemp.equals(SampleGattAttributes.BATTERY_LOW)){
                            tvStatus.setText("电压偏低");
                        } else if(strTemp.equals(SampleGattAttributes.BATTERY_NORMAL)){
                            tvStatus.setText("电压正常");
                        } else if(substr.equals(SampleGattAttributes.PERCENT)){
                            //百分比
                            int progress = Integer.parseInt(substr2.substring(0,2), 16); //702
                            loadingView.setProgress(progress);
                            percentLine(progress);
                        } else if(substr.equals(SampleGattAttributes.P_BATTERY_VOLTAGE)){
                            //实时电压
                            int voltage10 = Integer.parseInt(substr2, 16); //702
                            LogUtils.d("收到的数据为--" + strTemp + "电压值为--" + voltage10);
                            loadingView.setPercentText(voltage10 * 1.0 / 100 + " V");
                        }
                    }


                } else {
                    LogUtils.e("数据为--0000000000");
                }

            } else if (action.equals(GlobalConsts.ACTION_CONNECT_CHANGE)) {
                int status = intent.getIntExtra(BluetoothLeClass.CONNECT_STATUS, BluetoothLeClass.STATE_DISCONNECTED);
                if (status == BluetoothLeClass.STATE_DISCONNECTED) {
                    connectChanged(false);
                } else {
                    connectChanged(true);
                }
            } else if (action.equals(GlobalConsts.ACTION_SCAN_NEW_DEVICE)) {
                if (isVisible) {
                    ArrayList<iBeaconClass.iBeacon> mLeDevices;
                    mLeDevices = (ArrayList<iBeaconClass.iBeacon>) intent.getSerializableExtra(BleManager.SCAN_BLE_DATA);
                    showPopupWindow(getContext(), view, mLeDevices);
                }
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
                    BleManager.getInstance(getContext()).realConnect(bean.name, bean.bluetoothAddress);
                }
            });
        }
        dialog.changeData(mLeDevices);
        dialog.show(view);
    }

    private void connectChanged(boolean isConnected) {
        if (isAdded()) {
            if (isConnected) {
                titleBar.setTvRight("已连接");
                titleBar.setTvRightTextColor(getResources().getColor(R.color.dark_blue));
            } else {
                titleBar.setTvRight("未连接");
                titleBar.setTvRightTextColor(getResources().getColor(R.color.white));
                mItems.clear();
            }
        }
    }

    /**
     * 注册广播接收器
     */
    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(GlobalConsts.ACTION_NAME_RSSI);
        myIntentFilter.addAction(GlobalConsts.ACTION_CONNECT_CHANGE);
        myIntentFilter.addAction(GlobalConsts.ACTION_NOTIFI);
        myIntentFilter.addAction(GlobalConsts.ACTION_SCAN_NEW_DEVICE);
        getActivity().registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    /**
     * 设置折线图数据
     *
     * @param progress
     */
    public void percentLine(int progress) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        long tempTime = System.currentTimeMillis();
        final String time0 = sdf.format(new Time(tempTime));
        ItemBean itemBean = new ItemBean(progress, time0, tempTime);
        mItems.add(itemBean);
        if (firstInit) {
            tag = time0;
            firstInit = false;
        }
        myline.setItems(mItems);
    }


    /**
     * 不知道什么用处
     */
    private void writeParameter() {
        SharedPreferences.Editor sharedata = getActivity().getSharedPreferences("data", 0).edit();
    }

    /**
     * 不知道什么用处
     */
    private void ReadParameter() {
        SharedPreferences sharedata = getActivity().getSharedPreferences("data", 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    protected boolean isVisible = true;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtils.d("VoltageFragment.onHiddenChanged---"+hidden);
        if (hidden) {
            //不可见
            isVisible = false;
        } else {
            isVisible = true;
        }
    }
}
