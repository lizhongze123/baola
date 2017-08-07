package com.XMBT.bluetooth.le.ui.pbattery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.ui.main.MainActivity;
import com.XMBT.bluetooth.le.utils.DateFormatUtils;
import com.XMBT.bluetooth.le.utils.DensityUtils;
import com.XMBT.bluetooth.le.view.LineChart.ItemBean;
import com.XMBT.bluetooth.le.view.LineChart.LineView2;
import com.XMBT.bluetooth.le.view.TitleBar;

import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 汽车智能动力电池
 */
public class BateryActivity extends BaseActivity {

    private VoltageFragment voltageFragment;
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
        voltageFragment = VoltageFragment.newInstance(isConnSuccessful);
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

    /**
     * 汽车智能动力电池--动力测试
     */
    public static class StartTestFragment2 extends Fragment {

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

        public final static String EXTRA_DATA = "EXTRA_DATA";
        public final static String EXTRA_UUID = "EXTRA_UUID";
        public final static String EXTRA_STATUS = "EXTRA_STATUS";
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

        private boolean isConnSuccessful = false;

        private List<ItemBean> mItems;

        public static StartTestFragment2 newInstance(Boolean isConnSuccessful) {
            StartTestFragment2 itemFragement = new StartTestFragment2();
            Bundle bundle = new Bundle();
            bundle.putBoolean(MainActivity.CONNECTED_STATUS, isConnSuccessful);
            itemFragement.setArguments(bundle);
            return itemFragement;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            view = View.inflate(getActivity(), R.layout.fragment_starttest2, null);
            Bundle arguments = getArguments();
            if (arguments != null) {
                isConnSuccessful = arguments.getBoolean(MainActivity.CONNECTED_STATUS);
            }
            initViews();
            return view;
        }

        private void initTitle() {
            titleBar = (TitleBar) view.findViewById(R.id.titleBar);
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

            connectChanged(isConnSuccessful);
            registerBoradcastReceiver();
        }

        private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(GlobalConsts.ACTION_NOTIFI)) {
                    strTemp = intent.getStringExtra(EXTRA_DATA);

                    if (!strTemp.equals("00:00:00:00:00")) {  //过滤掉00:00:00:00:00

                        if (strTemp.length() == 14) {
                            if (strTemp.substring(0, 2).equals("04")) {
                                //启动信号  0X04A35C6699
                                startTime = System.currentTimeMillis();
                                date = DateFormatUtils.getDate(startTime, DateFormatUtils.FORMAT_ALL);
                                tvTime.setText(date);
                                voltageList = new ArrayList<>();
                                mItems = new ArrayList<>();
                            } else if (strTemp.substring(0, 2).equals("08")) {
                                //电压命令为：0X08xxyyzzww；其中xxyy为电压值，zzww为电压值反码
                                long currentTime = System.currentTimeMillis();
                                if (currentTime < startTime + 2000) {
                                    //取收到启动信号后的两秒内的最小值
                                    String substr = strTemp.substring(3, 5) + strTemp.substring(6, 8);
                                    int voltage10 = Integer.parseInt(substr, 16);
                                    voltageList.add(voltage10);
                                    addBean(currentTime, voltage10);
                                } else if (startTime != 0 && currentTime > startTime + 2000) {
                                    //接收两秒的数据完毕
                                    Integer ii = Collections.min(voltageList);
                                    setProgress(ii);
                                    lineView.setItems(mItems);
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
                }
            }
        };


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
            statusIv.setVisibility(View.VISIBLE);

            if (ii <= standardVoltage) {
                //电压偏低
                progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress_warn));
                ivTriangle.setImageResource(R.drawable.sort_down_red);
                tvVoltage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                statusIv.setImageResource(R.drawable.startvoltage_warn);
                tvInfo.setText("启动电压为" + df.format(voltage) + "V，电压偏低");
                tvInfo.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            } else if (ii > standardVoltage) {
                //正常电压
                progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.voltage_progress));
                ivTriangle.setImageResource(R.drawable.sort_down_green);
                tvVoltage.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                statusIv.setImageResource(R.drawable.startvoltage_normal);
                tvInfo.setText("启动电压为" + df.format(voltage) + "V，电压正常");
                tvInfo.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            }
            for (int i = 0; i < voltageList.size(); i++) {
                Log.e("lizhongze", i + "--电压为--" + voltageList.get(i).toString());
            }
        }

        private void addBean(long currentTime, int voltage10) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            final String time0 = sdf.format(new Time(currentTime));
            ItemBean itemBean = new ItemBean(voltage10, time0, currentTime);
            mItems.add(itemBean);
        }

        private void connectChanged(boolean isConnected) {
            if (isAdded()) {
                if (isConnected) {
                    titleBar.setTvLeft("已连接");
                    titleBar.setTvLeftTextColor(getResources().getColor(R.color.dark_blue));
                } else {
                    statusIv.setVisibility(View.INVISIBLE);
                    titleBar.setTvLeft("未连接");
                    titleBar.setTvLeftTextColor(getResources().getColor(R.color.white));
                }
            }
        }

        public void registerBoradcastReceiver() {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction(GlobalConsts.ACTION_NAME_RSSI);
            myIntentFilter.addAction(GlobalConsts.ACTION_CONNECT_CHANGE);
            myIntentFilter.addAction(GlobalConsts.ACTION_NOTIFI);
            getActivity().registerReceiver(mBroadcastReceiver, myIntentFilter);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getActivity().unregisterReceiver(mBroadcastReceiver);
        }

    }
}
