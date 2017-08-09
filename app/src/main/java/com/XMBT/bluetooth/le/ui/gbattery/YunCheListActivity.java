package com.XMBT.bluetooth.le.ui.gbattery;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.YunCheActivity;
import com.XMBT.bluetooth.le.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.http.ApiResultCallback;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.ui.misc.LoginActivity;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.stx.xhb.xbanner.XBanner;

import java.util.ArrayList;
import java.util.List;

public class YunCheListActivity extends BaseActivity implements XBanner.XBannerAdapter {

    public final int REQUEST_CODE = 0X01;

    private XBanner xBanner;
    private ListView listView;
    private List<Integer> imgurls = new ArrayList<>();
    private List<YunCheDeviceEntity> yunCheDeviceEntities = new ArrayList<>();
    private DeviceListAdapter adapter;
    private SwipeRefreshLayout swipe;
    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yun_che_list);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initView();
        if (GlobalConsts.isLogin) {
            getDevice();
        } else {
            showToast("请先登录");
            Intent intent = new Intent(YunCheListActivity.this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    private void initView() {
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.setRightOnClicker(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        loginChanged(GlobalConsts.isLogin);
        xBanner = (XBanner) findViewById(R.id.xbanner);
        listView = (ListView) findViewById(R.id.listView);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        imgurls.add(R.drawable.banner_one);
        imgurls.add(R.drawable.banner_three);
        xBanner.setData(imgurls, null);
        xBanner.setmAdapter(this);
        adapter = new DeviceListAdapter(this, yunCheDeviceEntities);
        adapter.setOnDeleteListener(new DeviceListAdapter.OnDeleteListener() {
            @Override
            public void onDelete(int pos) {
                deleteDevice(pos);
            }
        });
        listView.setAdapter(adapter);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDevice();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(YunCheListActivity.this, YunCheActivity.class);
                intent.putExtra("device", yunCheDeviceEntities.get(position));
                startActivity(intent);
            }
        });
    }

    private void deleteDevice(int pos) {
        yunCheDeviceEntities.remove(pos);
        adapter.notifyDataSetChanged();
    }

    private void login() {
        if (GlobalConsts.isLogin) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定退出登陆？")
                    .setPositiveButton("确定", null)
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            startActivity(new Intent(YunCheListActivity.this, LoginActivity.class));
        }
    }

    private void loginChanged(boolean isLogin) {
        if (isLogin) {
            titleBar.setTvRight("已登陆");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.dark_blue));
        } else {
            titleBar.setTvRight("未登陆");
            titleBar.setTvRightTextColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginChanged(GlobalConsts.isLogin);
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.backIv:
                onBackPressed();
                break;
            case R.id.addBtn:
                Intent intent = new Intent(YunCheListActivity.this, AddYuncheActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void loadBanner(XBanner banner, View view, int position) {
        Glide.with(YunCheListActivity.this).load(imgurls.get(position)).into((ImageView) view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                getDevice();
            }
        }
    }

    private void getDevice() {
        String mds = UserSp.getInstance(this).getMds();
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
                    public void onFailure(String errorCode) {
                        if (errorCode.equals("-1")) {
                            showToast("服务器异常");
                        }

                    }

                    @Override
                    public void onFinish() {
                        dismissLoadingDialog();
                        if(swipe.isRefreshing()){
                            swipe.setRefreshing(false);
                        }
                    }
                });
    }
}
