package com.XMBT.bluetooth.le.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseFragment;
import com.XMBT.bluetooth.le.bean.AddDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.event.NotifyEvent;
import com.XMBT.bluetooth.le.ui.gbattery.DeviceActivity;
import com.XMBT.bluetooth.le.ui.light.LightFunctionActivity;
import com.XMBT.bluetooth.le.ui.misc.LoginActivity;
import com.XMBT.bluetooth.le.ui.pbattery.BatteryActivity;
import com.XMBT.bluetooth.le.ui.start.EmergencyActivity;
import com.XMBT.bluetooth.le.utils.Configure;
import com.XMBT.bluetooth.le.utils.EvenManager;
import com.XMBT.bluetooth.le.utils.LoginUtil;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.bumptech.glide.Glide;
import com.stx.xhb.xbanner.XBanner;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的产品
 */

public class MyProductFragment extends BaseFragment implements XBanner.XBannerAdapter, AdapterView.OnItemClickListener{

    private View rootView;

    private ListView listView;
    private IndexDeviceAdapter adapter;
    private List<AddDeviceEntity> productList = new ArrayList<>();
    private List<String> names = new ArrayList<>();

    private String bleDeviceName = "DEVICE_NAME";
    public static final String MAC_ADDRESS = "mac_address";
    public String bluetoothAddress;
    private final static int REQUEST_CODE = 1;

    /**
     * 添加设备的广播接收器
     */
    private MyDeviceReceiver mDeviceReceiver;

    private XBanner xBanner;
    private List<Integer> imgurls = new ArrayList<>();

    public static MyProductFragment newInstance() {
        MyProductFragment itemFragement = new MyProductFragment();
        return itemFragement;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = View.inflate(getActivity(), R.layout.fragment_myproduct, null);
        register();
        initViews();
        return rootView;
    }

    private void register() {
        EvenManager.register(this);
        mDeviceReceiver = new MyDeviceReceiver();
        getContext().registerReceiver(mDeviceReceiver, new IntentFilter(GlobalConsts.FILTER_ADD_DEVICE));
    }

    /**
     * 已经添加过的设备
     */
    private void getSavedDevice() {
        productList.clear();
        names.clear();
        readObject(GlobalConsts.LIGHTING);
        readObject(GlobalConsts.POWER);
        readObject(GlobalConsts.BATTERY);
        readObject(GlobalConsts.GPS_BATTERY);
        adapter.notifyDataSetChanged();
    }

    private void initViews() {
        rootView.findViewById(R.id.ll_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUtil.checkLogin(getContext(), new LoginUtil.LoginForCallBack() {
                    @Override
                    public void callBack() {
                        startActivity(new Intent(getContext(), AddDeviceActivity.class));
                    }
                });
            }
        });
        listView = (ListView) rootView.findViewById(R.id.listView);
        adapter = new IndexDeviceAdapter(productList, this.getContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        xBanner = (XBanner) rootView.findViewById(R.id.xbanner);
        imgurls.add(R.drawable.banner_one);
        imgurls.add(R.drawable.banner_three);
        xBanner.setData(imgurls, null);
        xBanner.setmAdapter(this);
    }

    /**
     * @param key 产品名字
     */
    private void readObject(String key) {

        if (PreferenceUtils.readBoolean(getContext(), GlobalConsts.getProductSpName(), key)) {
            AddDeviceEntity addDeviceEntity = new AddDeviceEntity();
            if (key.equals(GlobalConsts.LIGHTING)) {
                addDeviceEntity.setImg(R.drawable.xm_ligh);
                addDeviceEntity.setTitle(getString(R.string.lighting));
                addDeviceEntity.setDeviceName(GlobalConsts.LIGHTING);
            } else if (key.equals(GlobalConsts.POWER)) {
                addDeviceEntity.setImg(R.drawable.battery);
                addDeviceEntity.setTitle(getString(R.string.power));
                addDeviceEntity.setDeviceName(GlobalConsts.POWER);
            } else if (key.equals(GlobalConsts.BATTERY)) {
                addDeviceEntity.setDeviceName(GlobalConsts.BATTERY);
                addDeviceEntity.setTitle(getString(R.string.battery));
                addDeviceEntity.setImg(R.drawable.battery_electric);
            } else if (key.equals(GlobalConsts.GPS_BATTERY)) {
                addDeviceEntity.setDeviceName(GlobalConsts.GPS_BATTERY);
                addDeviceEntity.setTitle(getString(R.string.gpsbattery));
                addDeviceEntity.setImg(R.drawable.battery_automobile);
            }
            productList.add(addDeviceEntity);
            names.add(addDeviceEntity.getTitle());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        bleDeviceName = productList.get(position).getDeviceName();
        if (bleDeviceName.equals(GlobalConsts.LIGHTING)) {
            startTheProductActivity(LightFunctionActivity.class, bluetoothAddress, position);
        } else if (bleDeviceName.equals(GlobalConsts.POWER)) {
            startTheProductActivity(EmergencyActivity.class, bluetoothAddress, position);
        } else if (bleDeviceName.equals(GlobalConsts.BATTERY)) {
            startTheProductActivity(BatteryActivity.class, bluetoothAddress, position);
        } else {
            if (Configure.isLogin) {
                startActivity(new Intent(this.getContext(), DeviceActivity.class));
            } else {
                showToast("请先登录");
                startActivity(new Intent(this.getContext(), LoginActivity.class));
            }
        }
    }

    private void startTheProductActivity(Class<?> clazz, String bluetoothAddress, int position) {
        Intent intent = new Intent(this.getContext(), clazz);
        intent.putExtra(MAC_ADDRESS, bluetoothAddress);
        startActivityForResult(intent, REQUEST_CODE);
    }

    class MyDeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            AddDeviceEntity addDeviceEntity = (AddDeviceEntity) intent.getSerializableExtra("addDeviceEntity");
            if (productList.size() == 0) {
                productList.add(addDeviceEntity);
                names.add(addDeviceEntity.getTitle());
                adapter.notifyDataSetChanged();

                //已添加的设备保存到sp中
                String productName = addDeviceEntity.getDeviceName();
                PreferenceUtils.write(getContext(), GlobalConsts.getProductSpName(), productName, true);

            } else {
                if (names.contains(addDeviceEntity.getTitle())) {
                    showToast("您已经添加过该设备了");
                } else {
                    productList.add(addDeviceEntity);
                    names.add(addDeviceEntity.getTitle());
                    adapter.notifyDataSetChanged();

                    String productName = addDeviceEntity.getDeviceName();
                    PreferenceUtils.write(getContext(), GlobalConsts.getProductSpName(), productName, true);

                }
            }
        }
    }

    @Override
    public void loadBanner(XBanner banner, View view, int position) {
        Glide.with(this.getContext()).load(imgurls.get(position)).into((ImageView) view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getContext().unregisterReceiver(mDeviceReceiver);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotifyEvent(NotifyEvent notifyEvent){
        if(notifyEvent.tag == 0){
            getSavedDevice();
        }else{
            productList.clear();
            names.clear();
            adapter.notifyDataSetChanged();
        }
    }

}
