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
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.db.DBManger;
import com.XMBT.bluetooth.le.utils.DateFormatUtils;
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
    private ImageView statusIv;
    private LoadingView loadingView;
    private String strTemp;
    private TextView persentTv;
    private LineView myline;
    private TextView tvUseDay, tvStopDay, tvStartCounts;

    static final int rssibufferSize = 10;
    private int[] rssibuffer = new int[rssibufferSize];
    private int rssibufferIndex = 0;
    private boolean rssiUsedFalg = false;

    /**
     * 折线数据
     */
    private List<ItemBean> mItems = Collections.synchronizedList(new ArrayList<ItemBean>());
    private boolean firstInit = true;
    /**
     * 记录折线图x=0的title，删除数据用
     */
    private String tag;
    /**
     * 是否启动车了？
     */
    private boolean isStartCar;
    /**
     * 是否接收启动信号
     */
    private boolean isStartCarSignal;
    /**
     * 启动汽车的时间
     */
    private long startCarTime;
    /**
     * 熄火时间
     */
    private long stopCarTime;
    /**
     * 电压大于1290时候的电压
     */
    private long time1290;
    /**
     * 正在开始熄火
     */
    private boolean isStopping;
    /**
     * 记录了第一次小于1290的时间的flag，只需要记录一次
     */
    private boolean flag = true;
    /**
     * 存放启动信号之后5秒内的集合
     */
    private List<Integer> voltageList = new ArrayList<>();

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

        statusIv = (ImageView) view.findViewById(R.id.statusIv);
        loadingView = (LoadingView) view.findViewById(R.id.loadingView);
        persentTv = (TextView) view.findViewById(R.id.textView10);

        tvUseDay = (TextView) view.findViewById(R.id.tv_useDay);
        tvStopDay = (TextView) view.findViewById(R.id.tv_stopDay);
        tvStartCounts = (TextView) view.findViewById(R.id.tv_startCounts);

        connectChanged(BleManager.isConnSuccessful);
        registerBoradcastReceiver();
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
                        } else if (substr.equals(SampleGattAttributes.START_SIGNAL)) {
                            //启动信号  0X04A35C6699
                            startCarTime = System.currentTimeMillis();//记录下开始时间
                            isStartCarSignal = true;
                            LogUtils.d("--------收到启动信号--------");
                        } else {
                            //实时电压
                            //电压命令为：08xxyyzzww；其中xxyy为电压值，zzww为电压值反码
                            //08 02 BE FD 41 -> 02 BE = 702 / 放大了100倍，7.02V
                            String voltageStr = strTemp.substring(2, 6);
                            int voltage10 = Integer.parseInt(voltageStr, 16); //702
                            //百分比与电压值关系：0~100%对应的电压为12.00~12.60V
                            LogUtils.d("收到的数据为--" + strTemp + "电压值为--" + voltage10);
                            //成功接收到启动信号后
                            if (isStartCarSignal) {
                                //接到启动信号后，要有电压大于13.20才算启动成功
                                if (voltage10 >= 1320) {
                                    //真正的启动成功
                                    isStartCar = true;
                                    LogUtils.d("真正的启动成功");
                                }
                                if (isStartCar) {
                                    //真正地启动车后
                                    //如果某一时刻的电压值小于1290，即代表可能开始熄火
                                    if (flag) {
                                        //如果记录了第一次小于1290的时间，后面的不再需要记录时间
                                        if (voltage10 < 1290) {
                                            //可能开始熄火,记录下第一个低于1290的时间
                                            time1290 = System.currentTimeMillis();
                                            isStopping = true;
                                            voltageList = new ArrayList<>();
                                            flag = false;
                                        }
                                    }
                                    //如果可能开始熄火了，记录5秒内电压的平均值是否小于1290
                                    if (isStopping) {
                                        //判断是否过了5秒
                                        long currentTime = System.currentTimeMillis();
                                        if (currentTime < time1290 + 5000) {
                                            //把电压保存下来
                                            voltageList.add(voltage10);
                                        } else {
                                            //过了5秒，求平均值
                                            if (voltageList.size() > 0) {
                                                int sum = 0;
                                                for (int i = 0; i < voltageList.size(); i++) {
                                                    sum = sum + voltageList.get(i);
                                                }
                                                int avg = sum / voltageList.size();
                                                if (avg < 1290) {
                                                    //代表真正的熄火成功
                                                    ToastUtils.toastInBottom(getContext(), "熄火成功");
                                                    stopCarTime = System.currentTimeMillis();
                                                    //只有大于5分钟的数据才保存
                                                    if ((stopCarTime - startCarTime) >= 300000) {
                                                        //保存到数据库
                                                        RecordBean bean = new RecordBean();
                                                        bean.date = DateFormatUtils.getDate(DateFormatUtils.FORMAT_YMD);
                                                        bean.startTime = DateFormatUtils.getDate(startCarTime, DateFormatUtils.FORMAT_HM);
                                                        bean.stopTime = DateFormatUtils.getDate(stopCarTime, DateFormatUtils.FORMAT_HM);
                                                        bean.duration = (stopCarTime - startCarTime) / 60000 + "分钟";
                                                        DBManger.getInstance(getContext()).addRecord(bean);
                                                        Log.e("lizhongze", "行驶时间为：" + ((stopCarTime - startCarTime) / 60000) + "分钟");
                                                    }
                                                    resetFlag();
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (voltage10 < 1240) {
                                //< 12.40 电池充电
                                statusIv.setVisibility(View.VISIBLE);
                                statusIv.setImageResource(R.drawable.stutus2);
                                int progress = (voltage10 - 1200) * 100 / 60;
                                if (progress <= 0) {
                                    progress = 0;
                                }
                                loadingView.setProgress(progress);
//                                loadingView.setPercentText(Double.valueOf(progress * 0.006 + 12) +" V");
                                loadingView.setPercentText(voltage10 * 1.0 / 100 + " V");
                                persentTv.setText(progress + "%");
                                percentLine(progress);
                            } else if (voltage10 > 1239 && voltage10 < 1340) {
                                //12.39 < x < 13.40 电压正常
                                statusIv.setVisibility(View.VISIBLE);
                                statusIv.setImageResource(R.drawable.status3);
                                int progress = (voltage10 - 1200) * 100 / 60;
                                if (progress >= 100) {
                                    progress = 100;
                                }
                                loadingView.setPercentText(voltage10 * 1.0 / 100 + " V");
                                if (voltage10 >= 1260) {
                                    loadingView.setProgress(100);
                                    percentLine(100);
                                    persentTv.setText("100%");
                                } else {
                                    loadingView.setProgress(progress);
                                    percentLine(progress);
                                    persentTv.setText(progress + "%");
                                }
                            } else if (voltage10 > 1340) {
                                // > 13.40 电池充电中
                                statusIv.setVisibility(View.VISIBLE);
                                statusIv.setImageResource(R.drawable.status1);
                                loadingView.setProgress(100);
                                loadingView.setPercentText(voltage10 * 1.0 / 100 + " V");
                                percentLine(100);
                                persentTv.setText("100%");
                            }
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
                    mLeDevices = (ArrayList<iBeaconClass.iBeacon>) intent.getSerializableExtra(BleManager.SCAN_BLE_STATUS);
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
                loadingView.setProgress(0);
                loadingView.setPercentText(0 + "V");
                persentTv.setText("0%");
                statusIv.setVisibility(View.GONE);
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

    private void resetFlag() {
        flag = true;
        isStartCarSignal = false;
        isStartCar = false;
        isStopping = false;
        startCarTime = 0;
        stopCarTime = 0;
        time1290 = 0;
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

    protected boolean isVisible;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            //不可见
            isVisible = false;
        } else {
            isVisible = true;
        }
    }
}
