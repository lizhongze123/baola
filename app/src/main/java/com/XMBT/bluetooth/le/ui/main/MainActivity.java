package com.XMBT.bluetooth.le.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.AddDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.ui.gbattery.GpsBatteryActivity;
import com.XMBT.bluetooth.le.ui.misc.LoginActivity;
import com.XMBT.bluetooth.le.ui.start.EmergencyActivity;
import com.XMBT.bluetooth.le.ui.light.LightFunctionActivity;
import com.XMBT.bluetooth.le.ui.pbattery.BatteryActivity;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的设备activity
 */
public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private TitleBar titleBar;
    /**
     * 添加设备的广播接收器
     */
    private MyDeviceReceiver mDeviceReceiver;


    private IndexDeviceAdapter adapter;
    private List<String> names = new ArrayList<>();
    private List<AddDeviceEntity> productList = new ArrayList<>();
    private final static int REQUEST_CODE = 1;
    private String bleDeviceName = "DEVICE_NAME";
    public static final String MAC_ADDRESS = "mac_address";
    public String bluetoothAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);

        register();
        getSavedDevice();
        ininView();
    }

    /**
     * 注册广播接收器
     */
    private void register() {
        mDeviceReceiver = new MyDeviceReceiver();
        registerReceiver(mDeviceReceiver, new IntentFilter(GlobalConsts.FILTER_ADD_DEVICE));
    }

    private void initTitle() {
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setRightOnClicker(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddDeviceActivity.class));
            }
        });
    }

    private void ininView() {
        initTitle();
        listView = (ListView) findViewById(R.id.listView);
        adapter = new IndexDeviceAdapter(productList, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    /**
     * 已经添加过的设备
     */
    private void getSavedDevice() {
        readObject(GlobalConsts.LIGHTING);
        readObject(GlobalConsts.POWER);
        readObject(GlobalConsts.BATTERY);
        readObject(GlobalConsts.GPS_BATTERY);
    }

    /**
     * @param key 产品名字
     */
    private void readObject(String key) {

        if (PreferenceUtils.readBoolean(this, "productInfo", key)) {
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




    private void startTheProductActivity(Class<?> clazz, String bluetoothAddress, int position) {
        Intent intent = new Intent(this, clazz);
        intent.putExtra(MAC_ADDRESS, bluetoothAddress);
        startActivityForResult(intent, REQUEST_CODE);
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
            if (GlobalConsts.isLogin) {
                startActivity(new Intent(MainActivity.this, GpsBatteryActivity.class));
            } else {
                showToast("请先登录");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        }
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
                PreferenceUtils.write(MainActivity.this, "productInfo", productName, true);

            } else {
                if (names.contains(addDeviceEntity.getTitle())) {
                    showToast("您已经添加过该设备了");
                } else {
                    productList.add(addDeviceEntity);
                    names.add(addDeviceEntity.getTitle());
                    adapter.notifyDataSetChanged();

                    String productName = addDeviceEntity.getDeviceName();
                    PreferenceUtils.write(MainActivity.this, "productInfo", productName, true);

                }
            }
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDeviceReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private boolean isExit = false;

    @Override
    public void onBackPressed() {
        if(isExit){
            super.onBackPressed();
        }else{
            isExit = true;
            showToast("再次点击将退出程序");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        }
    }
}
