package com.XMBT.bluetooth.le.ui.device.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseFragment;
import com.XMBT.bluetooth.le.bean.RecordBean;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.db.DBManger;
import com.XMBT.bluetooth.le.ui.device.IndexFragment;
import com.XMBT.bluetooth.le.utils.DateFormatUtils;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 汽车智能动力电池--行车记录Fragment
 */
public class DrivingFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public final static String EXTRA_DATA = "EXTRA_DATA";
    private View view;
    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();
    SimpleCalendarDialogFragment dialogFragment;
    private TitleBar titleBar;
    private RecyclerView recyclerView;
    private TextView mSuspensionTv;
    /**
     * 记录滑动的item的位置
     */
    private int mCurrentPosition = 0;
    private SwipeRefreshLayout srl;
    private DrivingRecordAdapter adapter;

    private boolean isConnSuccessful = false;

    private RelativeLayout mSuspensionBar;
    private int mSuspensionHeight;
    private List<RecordBean> dataList = new ArrayList<>();

    public static DrivingFragment newInstance(Boolean isConnSuccessful) {
        DrivingFragment itemFragement = new DrivingFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IndexFragment.CONNECTED_STATUS, isConnSuccessful);
        itemFragement.setArguments(bundle);
        return itemFragement;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.driving_fragment, null);
        Bundle arguments = getArguments();
        if (arguments != null) {
            isConnSuccessful = arguments.getBoolean(IndexFragment.CONNECTED_STATUS);
        }
        initViews();
        return view;
    }

    private void initTitle() {
        titleBar = (TitleBar) view.findViewById(R.id.titleBar);
//        mNewAppTitle.setRightTitle("日期");
//        mNewAppTitle.setOnRightButtonClickListener(new TitleBar.OnRightButtonClickListener() {
//            @Override
//            public void OnRightButtonClick(View v) {
//                dialogFragment = new SimpleCalendarDialogFragment();
//                dialogFragment.show(getChildFragmentManager(), "test-simple-calendar");
//            }
//        });
    }

    private void initViews() {
        initTitle();
        dataList.addAll(DBManger.getInstance(getContext()).queryAllRecord());
        mSuspensionTv = (TextView) view.findViewById(R.id.tv_time);
        mSuspensionBar = (RelativeLayout) view.findViewById(R.id.suspension_bar);
        srl = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        //设置下拉刷新监听事件
        srl.setOnRefreshListener(this);
        //设置进度条的颜色
        srl.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new DrivingRecordAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        adapter.addAll(dataList);
        if (dataList.size() != 0) {
            mSuspensionTv.setText(dataList.get(0).date);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mSuspensionHeight = mSuspensionBar.getHeight();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (adapter.getItemViewType(mCurrentPosition + 1) == DrivingRecordAdapter.TYPE_TIME) {
                    View view = linearLayoutManager.findViewByPosition(mCurrentPosition + 1);
                    if (view != null) {
                        if (view.getTop() <= mSuspensionHeight) {
                            mSuspensionBar.setY(-(mSuspensionHeight - view.getTop()));
                        } else {
                            mSuspensionBar.setY(0);
                        }
                    }
                }

                if (mCurrentPosition != linearLayoutManager.findFirstVisibleItemPosition()) {
                    mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    mSuspensionBar.setY(0);

                    updateSuspensionBar();
                }
            }
        });
        connectChanged(isConnSuccessful);
        registerBoradcastReceiver();
    }

    private void updateSuspensionBar() {
        mSuspensionTv.setText(dataList.get(mCurrentPosition).date);
    }

    private void connectChanged(boolean isConnected) {
        if (isAdded()) {
            if (isConnected) {
                titleBar.setTvLeft("已连接");
                titleBar.setTvLeftTextColor(getResources().getColor(R.color.dark_blue));
            } else {
                titleBar.setTvLeft("未连接");
                titleBar.setTvLeftTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    @Override
    public void onRefresh() {
        srl.postDelayed(new Runnable() {
            @Override
            public void run() {
                RecordBean bean6 = new RecordBean();
                bean6.date = "2017-08-07";
                bean6.startTime = "11:11";
                bean6.stopTime = "22:22";
                bean6.duration = 88 + "分钟";
                DBManger.getInstance(getContext()).addRecord(bean6);
                srl.setRefreshing(false);
                adapter.clear();
                dataList.clear();
                dataList.addAll(DBManger.getInstance(getContext()).queryAllRecord());
                adapter.addAll(dataList);
            }
        }, 2000);
    }

    public static class SimpleCalendarDialogFragment extends DialogFragment implements OnDateSelectedListener {

        private TextView textView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_basic, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            textView = (TextView) view.findViewById(R.id.textView);
            MaterialCalendarView widget = (MaterialCalendarView) view.findViewById(R.id.calendarView);
            widget.setOnDateChangedListener(this);
        }

        @Override
        public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
            textView.setText(FORMATTER.format(date.getDate()));
            Toast.makeText(getActivity(), "暂无记录", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GlobalConsts.ACTION_CONNECT_CHANGE)) {
                int status = intent.getIntExtra(BluetoothLeClass.CONNECT_STATUS, BluetoothLeClass.STATE_DISCONNECTED);
                if (status == BluetoothLeClass.STATE_DISCONNECTED) {
                    connectChanged(false);
                } else {
                    connectChanged(true);
                }
            }
        }
    };

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
