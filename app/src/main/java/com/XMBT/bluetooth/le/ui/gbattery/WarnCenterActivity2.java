package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
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
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
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
public class WarnCenterActivity2 extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    private RadioGroup radioGroup;
    private RadioButton rbFence, rbDisplacement;
    private LinearLayout ll_displacement, ll_fence;
    private ListView lv_displacement, lv_fence;
    private YunCheDeviceEntity device;
    private String dataString = "10,9,8,29,3";
    private AlarmAdapter displacementAdapter;
    private AlarmAdapter fencelAdapter;
    private Map<String, String> alarmCount = new HashMap();
    private List<AlarmBean.RowsBean> displacementData = new ArrayList<>();
    private List<AlarmBean.RowsBean> fenceData = new ArrayList<>();

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1001) {
                displacementAdapter.clear();
                displacementAdapter.addAll(displacementData);
            } else if (msg.what == 1002) {
                fencelAdapter.clear();
                fencelAdapter.addAll(fenceData);
            } else {
                rbDisplacement.setText("位移报警  " + alarmCount.get("10"));
                rbFence.setText("围栏报警  " + alarmCount.get("3"));
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warn_center2);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initViews();
        getAlarmCount();
        getDetailData("10");
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
        lv_fence = (ListView) findViewById(R.id.lv_fence);
        lv_displacement = (ListView) findViewById(R.id.lv_displacement);
        TextView tv = (TextView)findViewById(R.id.empty_list_view);
        TextView tv2 = (TextView)findViewById(R.id.empty_list_view2);
        lv_fence.setEmptyView(tv2);
        lv_displacement.setEmptyView(tv);
        displacementAdapter = new AlarmAdapter(this, "位移报警");
        lv_displacement.setAdapter(displacementAdapter);
        fencelAdapter = new AlarmAdapter(this, "围栏报警");
        lv_fence.setAdapter(fencelAdapter);
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
    public void getDetailData(final String type) {
        showLoadingDialog(null);
        device = (YunCheDeviceEntity) getIntent().getSerializableExtra(DeviceFragment.DATA_DEVICE);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.get(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "GetAlarmList")
                .params("macid", device.macid)
                .params("classify", type)
                .params("mapType", "BAIDU")
                .params("mds", mds)
                .params("pageSize", 5)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        LogUtils.d(s);

                        AlarmBean bean = new Gson().fromJson(s, AlarmBean.class);
                        if (bean.success.equals("false")) {
                            String msg = bean.errorDescribe;
                            showToast(msg);
                        } else {
                            if (type.equals("10")) {
                                displacementData.clear();
                                displacementData.addAll(bean.rows);
                                mHandler.sendEmptyMessage(1001);
                            } else {
                                fenceData.clear();
                                fenceData.addAll(bean.rows);
                                mHandler.sendEmptyMessage(1002);
                            }
                        }

                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        dismissLoadingDialog();
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
            getDetailData("10");
        } else if (checkedId == R.id.rb_fence) {
            ll_displacement.setVisibility(View.GONE);
            ll_fence.setVisibility(View.VISIBLE);
            getDetailData("3");
        }
    }

}
