package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.ui.gbattery.adapter.AlarmBean;
import com.XMBT.bluetooth.le.ui.gbattery.adapter.AlarmAdapter;
import com.XMBT.bluetooth.le.ui.gbattery.adapter.MyAlarmAdapter;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 报警中心
 * 进来先取报警数量
 * 点击后取报警
 */
public class WarnCenterActivity2 extends BaseActivity implements RadioGroup.OnCheckedChangeListener, BaseQuickAdapter.RequestLoadMoreListener {

    private RadioGroup radioGroup;
    private RadioButton rbFence, rbDisplacement;
    private LinearLayout ll_displacement, ll_fence;
    private RecyclerView rv, rv2;
    private YunCheDeviceEntity device;
    private String dataString = "10,9,8,29,3";

    private MyAlarmAdapter mAdapter;
    private MyAlarmAdapter mAdapter2;

    private Map<String, String> alarmCount = new HashMap();
    private List<AlarmBean.RowsBean> data2 = new ArrayList<>();
    private List<AlarmBean.RowsBean> data = new ArrayList<>();

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

                rbDisplacement.setText("位移报警  " + alarmCount.get("10"));
                rbFence.setText("围栏报警  " + alarmCount.get("3"));


        }
    };

    private int pageSize = 20;
    private int dPageIndex = 0;
    private int fPageIndex = 0;
    private View empty_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warn_center2);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initViews();
        getAlarmCount();
        getDetailData("10", dPageIndex, pageSize);
        getDetailData("3", fPageIndex, pageSize);
    }

    private void initViews() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        radioGroup = (RadioGroup) findViewById(R.id.rg);
        radioGroup.setOnCheckedChangeListener(this);
        rbFence = (RadioButton) findViewById(R.id.rb_fence);
        rbDisplacement = (RadioButton) findViewById(R.id.rb_displacement);
        ll_displacement = (LinearLayout) findViewById(R.id.ll_displacement);
        ll_fence = (LinearLayout) findViewById(R.id.ll_fence);

        empty_view = getLayoutInflater().inflate(R.layout.layout_emety, null, false);

        rv = (RecyclerView) findViewById(R.id.rv);
        mAdapter = new MyAlarmAdapter("位移报警");
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv.setAdapter(mAdapter);
        // 滑动最后一个Item的时候回调onLoadMoreRequested方法
        mAdapter.setOnLoadMoreListener(this,rv);

        rv2 = (RecyclerView) findViewById(R.id.rv2);
        mAdapter2 = new MyAlarmAdapter("围栏报警");
        rv2.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv2.setAdapter(mAdapter2);
        // 滑动最后一个Item的时候回调onLoadMoreRequested方法
        mAdapter2.setOnLoadMoreListener(this,rv2);
    }

    /**
     * 取报警数量
     */
    public void getAlarmCount() {
        device = (YunCheDeviceEntity) getIntent().getSerializableExtra(DeviceFragment.DATA_DEVICE);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.get(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "getAlarmCount")
                .params("macid", device.macid)
                .params("classify", dataString)
                .params("mds", mds)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        LogUtils.d(s);
                        parseJson(s);
                    }

                    @Override
                    public void onAfter(String s, Exception e) {

                    }
                });
    }

    /**
     * 取报警信息
     */
    public void getDetailData(final String type, final int pageIndex, int pageSize) {
        device = (YunCheDeviceEntity) getIntent().getSerializableExtra(DeviceFragment.DATA_DEVICE);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.get(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "GetAlarmList")
                .params("macid", device.macid)
                .params("classify", type)
                .params("mapType", "BAIDU")
                .params("mds", mds)
                .params("pageSize", pageSize)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        LogUtils.d(s);
                        final AlarmBean bean = new Gson().fromJson(s, AlarmBean.class);
                        if (bean.success.equals("false")) {
                            String msg = bean.errorDescribe;
                            showToast(msg);
                        } else {
                            if (type.equals("10")) {

                                rv.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(pageIndex == 0){
                                            //第0页
                                            data.clear();
                                        }

                                        if(bean.rows.size() == 0){
                                            if(pageIndex == 0){
                                                mAdapter.setEmptyView(empty_view);
                                            }else{
                                                mAdapter.loadMoreEnd();
                                            }
                                        }else{
                                            data.addAll(bean.rows);
                                            mAdapter.addData(data);
                                            mAdapter.loadMoreComplete();
                                        }
                                    }
                                });

                            } else {
                                rv2.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(fPageIndex == 0){
                                            //第0页
                                            data2.clear();
                                        }

                                        if(bean.rows.size() == 0){
                                            if(fPageIndex == 0){
                                                mAdapter2.setEmptyView(empty_view);
                                            }else{
                                                mAdapter2.loadMoreEnd();
                                            }
                                        }else{
                                            data.addAll(bean.rows);
                                            mAdapter2.addData(data2);
                                            mAdapter2.loadMoreComplete();
                                        }
                                    }
                                });
                            }
                        }

                    }

                    @Override
                    public void onAfter(String s, Exception e) {

                    }
                });
    }

    private void parseJson(String json) {
        /**
         {"success":"true","row":{"29":"0","8":"0","9":"0","10":"0","3":"0"}}
         */
        try {
            JSONObject jsonObject = new JSONObject(json);
            String success = jsonObject.getString("success");
            if (success.equals("false")) {
                String msg = jsonObject.getString("msg");
                showToast(msg);
            } else {
                JSONObject datajson = jsonObject.getJSONObject("row");
                alarmCount.put("10", datajson.getString("10"));//位移报警
                alarmCount.put("3", datajson.getString("3"));//围栏报警
                mHandler.sendEmptyMessage(1003);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (checkedId == R.id.rb_displacement) {
            ll_fence.setVisibility(View.GONE);
            ll_displacement.setVisibility(View.VISIBLE);
//            getDetailData("10");
        } else if (checkedId == R.id.rb_fence) {
            ll_displacement.setVisibility(View.GONE);
            ll_fence.setVisibility(View.VISIBLE);
//            getDetailData("3");
        }
    }

    @Override
    public void onLoadMoreRequested() {

        if(View.VISIBLE == ll_displacement.getVisibility()){

            if(data.size() < pageSize){
                //没有新的数据了
                mAdapter.loadMoreEnd();
            }else{
                if(data.size() % pageSize == 0){
                    getDetailData("10", dPageIndex + 1, pageSize);
                    dPageIndex  = dPageIndex + 1;
                }
            }

        }else{

            if(data2.size() < pageSize){
                //没有新的数据了
                mAdapter2.loadMoreEnd();
            }else{
                if(data2.size() % pageSize == 0){
                    getDetailData("10", fPageIndex + 1, pageSize);
                    fPageIndex  = fPageIndex + 1;
                }
            }

        }

    }
}
