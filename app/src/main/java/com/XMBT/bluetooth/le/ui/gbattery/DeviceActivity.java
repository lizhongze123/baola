package com.XMBT.bluetooth.le.ui.gbattery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.http.ApiResultCallback;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.lzy.okgo.OkGo;

import java.util.ArrayList;
import java.util.List;

/**
 * gps设备界面
 */
public class DeviceActivity extends BaseActivity implements View.OnClickListener {

    public final int REQUEST_CODE = 0X01;

    public static String DATA_DEVICE = "device";

    private ListView listView;
    private List<YunCheDeviceEntity> yunCheDeviceEntities = new ArrayList<>();
    private DeviceListAdapter adapter;
    private SwipeRefreshLayout swipe;
    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        StatusBarHelper.setStatusBarColor(this, R.color.title_bg);
        initViews();
        getDevice();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            getDevice();
        }
    }

    private void initViews() {
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.addBtn).setOnClickListener(this);
        listView = (ListView) findViewById(R.id.listView);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        adapter = new DeviceListAdapter(DeviceActivity.this, yunCheDeviceEntities);
        adapter.setOnDeleteListener(new DeviceListAdapter.OnDeleteListener() {
            @Override
            public void onDelete(int pos) {
                delDevice(pos);
            }
        });
        listView.setAdapter(adapter);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDevice();
                adapter.onRefreshSwipe();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DeviceActivity.this, YunCheActivity.class);
                intent.putExtra("test",4);
                if(yunCheDeviceEntities.get(position) != null){
                    intent.putExtra(DATA_DEVICE, yunCheDeviceEntities.get(position));
                    startActivity(intent);
                }else{
                    LogUtils.e("DeviceActivity的device为null");
                }
            }
        });
    }

    private void getDevice() {
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        showLoadingDialog("加载中，请稍候");
        OkGo.get(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "GetEquipmentList")
                .params("mds", mds)
                .execute(new ApiResultCallback<List<YunCheDeviceEntity>>() {

                    @Override
                    public void onSuccessResponse(List<YunCheDeviceEntity> data) {
                        if (data != null && data.size() > 0) {
                            yunCheDeviceEntities.clear();
                            yunCheDeviceEntities.addAll(data);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(String errorCode, String describe) {
                        if (errorCode.equals("-1")) {
                            showToast("服务器异常");
                        }

                    }

                    @Override
                    public void onFinish() {
                        dismissLoadingDialog();
                        if (swipe.isRefreshing()) {
                            swipe.setRefreshing(false);
                        }
                    }
                });
    }

    private void delDevice(final int position) {
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        String macId = yunCheDeviceEntities.get(position).macid;
        showLoadingDialog("加载中，请稍候");
        OkGo.get(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "unbundlingDevice")
                .params("mds", mds)
                .params("macid", macId)
                .execute(new ApiResultCallback<List<String>>() {

                    @Override
                    public void onSuccessResponse(List<String> data) {
                        yunCheDeviceEntities.remove(position);
                        adapter.notifyDataSetChanged();
                        showToast("解绑成功");
                    }

                    @Override
                    public void onFailure(String errorCode, String describe) {
                        if (errorCode.equals("-1")) {
                            showToast("服务器异常");
                        }

                    }

                    @Override
                    public void onFinish() {
                        dismissLoadingDialog();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addBtn:
                Intent intent = new Intent(this, AddYuncheActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
        }

    }
}
