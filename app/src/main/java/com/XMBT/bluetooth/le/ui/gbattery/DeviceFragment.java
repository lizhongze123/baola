package com.XMBT.bluetooth.le.ui.gbattery;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseFragment;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.http.ApiResultCallback;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.ui.misc.LoginActivity;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.stx.xhb.xbanner.XBanner;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends BaseFragment implements XBanner.XBannerAdapter, View.OnClickListener {

    private View rootView;

    public final int REQUEST_CODE = 0X01;

    private XBanner xBanner;
    private ListView listView;
    private List<Integer> imgurls = new ArrayList<>();
    private List<YunCheDeviceEntity> yunCheDeviceEntities = new ArrayList<>();
    private DeviceListAdapter adapter;
    private SwipeRefreshLayout swipe;
    private TitleBar titleBar;


    public static DeviceFragment newInstance() {
        DeviceFragment itemFragement = new DeviceFragment();
        return itemFragement;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = View.inflate(getActivity(), R.layout.fragment_device, null);
        initViews();
        getDevice();
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            getDevice();
        }
    }

    private void initViews() {
        titleBar = (TitleBar) rootView.findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        rootView.findViewById(R.id.addBtn).setOnClickListener(this);
        xBanner = (XBanner) rootView.findViewById(R.id.xbanner);
        listView = (ListView) rootView.findViewById(R.id.listView);
        swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        imgurls.add(R.drawable.banner_one);
        imgurls.add(R.drawable.banner_three);
        xBanner.setData(imgurls, null);
        xBanner.setmAdapter(this);
        adapter = new DeviceListAdapter(getContext(), yunCheDeviceEntities);
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
                Intent intent = new Intent(getContext(), YunCheActivity.class);
                intent.putExtra("device", yunCheDeviceEntities.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public void loadBanner(XBanner banner, View view, int position) {
        Glide.with(DeviceFragment.this).load(imgurls.get(position)).into((ImageView) view);
    }

    private void getDevice() {
        String mds = UserSp.getInstance(getContext()).getMds();
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
                        if (swipe.isRefreshing()) {
                            swipe.setRefreshing(false);
                        }
                    }
                });
    }

    private void delDevice(final int position) {
        String mds = UserSp.getInstance(getContext()).getMds();
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
                    public void onFailure(String errorCode) {
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
                Intent intent = new Intent(getContext(), AddYuncheActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
        }

    }
}
