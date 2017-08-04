package com.XMBT.bluetooth.le.ui.device;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.YunCheListActivity;
import com.XMBT.bluetooth.le.base.BaseFragment;
import com.XMBT.bluetooth.le.bean.AddDeviceEntity;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.sp.ProductSp;
import com.XMBT.bluetooth.le.ui.device.battery.BateryActivity;
import com.XMBT.bluetooth.le.ui.device.power.EmergencyActivity;
import com.XMBT.bluetooth.le.ui.device.lighting.LightFunctionActivity;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.XMBT.bluetooth.le.utils.ToolsUtil;
import com.XMBT.bluetooth.le.utils.Utils;
import com.XMBT.bluetooth.le.view.TitleBar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备Fragment
 * 这界面只做蓝牙初始化，不做连接
 */

public class IndexFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private View view;
    private ListView listView;
    private TitleBar titleBar;
    /**
     * 添加设备的广播接收器
     */
    private MyDeviceReceiver mDeviceReceiver;
    /**
     * 蓝牙连接的广播接收器
     */
    private MyConnectReceiver mConnectReceiver;

    private IndexDeviceAdapter adapter;
    private List<String> names = new ArrayList<>();
    private List<AddDeviceEntity> productList = new ArrayList<>();
    private final static int REQUEST_CODE = 1;
    private String bleDeviceName = "DEVICE_NAME";
    public static final String MAC_ADDRESS = "mac_address";
    public static final String CONNECTED_STATUS = "connected_status";
    /**
     * 是否自动连接
     */
    private boolean AutoConectFlag = true;
    public static BluetoothGattCharacteristic gattCharacteristic_write = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char2 = null;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeClass mBLE;
    public String bluetoothAddress;
    /**
     * 是否正在扫描
     */
    private boolean mScanning;
    private iBeaconClass.iBeacon connectDevice = null;
    /**
     * 扫描出来的设备
     */
    private ArrayList<iBeaconClass.iBeacon> mLeDevices = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.index_fragment, null);
        register();
        getSavedDevice();
        ininView();
        initBle();
        return view;
    }

    /**
     * 注册广播接收器
     */
    private void register() {
        mDeviceReceiver = new MyDeviceReceiver();
        getActivity().registerReceiver(mDeviceReceiver, new IntentFilter(GlobalConsts.FILTER_ADD_DEVICE));
        mConnectReceiver = new MyConnectReceiver();
        getActivity().registerReceiver(mConnectReceiver, new IntentFilter(GlobalConsts.FILTER_ACTION_CONNECT));
    }

    private void initTitle() {
        titleBar = (TitleBar) view.findViewById(R.id.titleBar);
        titleBar.setRightOnClicker(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddDeviceActivity.class));
            }
        });
    }

    private void ininView() {
        initTitle();
        listView = (ListView) view.findViewById(R.id.listView);
        adapter = new IndexDeviceAdapter(productList, getActivity());
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

        if (PreferenceUtils.readBoolean(getContext(), "productInfo", key)) {
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

    /**
     * 初始化蓝牙
     */
    private void initBle() {
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast(getResources().getString(R.string.ble_not_supported));
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            showToast(getResources().getString(R.string.error_bluetooth_not_supported));
            getActivity().finish();
        }
        mBluetoothAdapter.enable();
        mBLE = new BluetoothLeClass(getActivity());
        if (!mBLE.initialize()) {
            getActivity().finish();
        }
        //设置发现服务监听器
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        mBLE.setOnDataAvailableListener(mOnDataAvailable);
    }

    /**
     * 扫描蓝牙设备
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            boolean test = mBluetoothAdapter.startLeScan(mLeScanCallback);
            LogUtils.e("扫描开始----------" + test);
        } else {
            mScanning = false;
            LogUtils.e("扫描结束----------");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    public static void WriteCharX(BluetoothGattCharacteristic GattCharacteristic, byte[] writeValue) {
        if (GattCharacteristic != null) {
            GattCharacteristic.setValue(writeValue);
            mBLE.writeCharacteristic(GattCharacteristic);
        }
    }

    public static void ReadCharX(BluetoothGattCharacteristic GattCharacteristic) {
        if (GattCharacteristic != null) {
            mBLE.readCharacteristic(GattCharacteristic);

        }
    }

    public static void setCharacteristicNotification(
            BluetoothGattCharacteristic gattCharacteristic, boolean enabled) {
        if (gattCharacteristic != null) {
            mBLE.setCharacteristicNotification(gattCharacteristic, enabled);
        }
    }

    /**
     * 设置notify
     */
    public static void setNotify() {
        if (gattCharacteristic_char2 != null) {
            LogUtils.e("开始设置notify-------------");
            boolean enabled = true;
            mBLE.setCharacteristicNotification(gattCharacteristic_char2, enabled);
        } else {
            LogUtils.e("没有成功设置notify");
        }
    }

    /***
     * 蓝牙扫描回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {

            LogUtils.i("蓝牙扫描回调-----");
            final iBeaconClass.iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
            addDevice(ibeacon);
            //按rssi排序
            /*Collections.sort(mLeDevices, new Comparator<iBeaconClass.iBeacon>() {
                @Override
                public int compare(iBeaconClass.iBeacon h1, iBeaconClass.iBeacon h2) {
                    return h2.rssi - h1.rssi;
                }
            });*/
        }
    };

    /**
     * 把扫描出来的设备添加进来，不重复添加
     *
     * @param device
     */
    public void addDevice(iBeaconClass.iBeacon device) {
        if (device == null) {
            LogUtils.d("device is null ");
            return;
        }
        for (int i = 0; i < mLeDevices.size(); i++) {
            String btAddress = mLeDevices.get(i).bluetoothAddress;
            if (btAddress.equals(device.bluetoothAddress)) {
                mLeDevices.add(i + 1, device);
                mLeDevices.remove(i);
                break;
            }
        }
        mLeDevices.add(device);
    }

    /**
     * 发现服务后的监听
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServices(mBLE.getSupportedGattServices());
        }

    };

    /**
     * 发现服务后的操作
     *
     * @param gattServices
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;

        for (BluetoothGattService gattService : gattServices) {

            int type = gattService.getType();

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                int permission = gattCharacteristic.getPermissions();

                int property = gattCharacteristic.getProperties();

                byte[] data = gattCharacteristic.getValue();

                if (gattCharacteristic.getUuid().toString().equals(SampleGattAttributes.CHAR_WRITE)) {
                    gattCharacteristic_write = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(SampleGattAttributes.CHAR_NOTIFY)) {
                    gattCharacteristic_char2 = gattCharacteristic;
                    //连接成功后就使能
                    setNotify();
                }

                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    int descPermission = gattDescriptor.getPermissions();
                    byte[] desData = gattDescriptor.getValue();
                }
            }
        }
    }

    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new BluetoothLeClass.OnDataAvailableListener() {

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.v("MyLog", "onCharRead " + gatt.getDevice().getName() + " read " + characteristic.getUuid().toString() + " -> "
                    + Utils.bytesToHexString(characteristic.getValue()));

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {

            final byte[] data = characteristic.getValue();
            StringBuilder s = HexUtil.byte2HexStr(data);
            LogUtils.v("onCharWrite " + gatt.getDevice().getName() + " write "
                    + characteristic.getUuid().toString() + " -> "
                    + s.toString());
        }
    };

    private ProgressDialog progressDialog;
    private boolean isConnSuccessful = false;

    private void startTheProductActivity(Class<?> clazz, String bluetoothAddress, int position) {
        boolean isHas = false;
        LogUtils.i("mLeDevices.size()--" + mLeDevices.size());
        for (int i = 0; i < mLeDevices.size(); i++) {
            LogUtils.i("mLeDevices.size()--" + i + "///name is--" + mLeDevices.get(i).name);
            //如果设备名字相同就连接
            if (mLeDevices.get(i).name != null && mLeDevices.get(i).name.equals(bleDeviceName)) {
                connectDevice = mLeDevices.get(i);
                if (connectDevice != null) {
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setTitle("请稍候");
                    progressDialog.setMessage("正在连接设备并获取服务");
                    progressDialog.show();
                    if (mScanning) {
                        scanLeDevice(false);
                    }
                    //连接ble
                    isConnSuccessful = mBLE.connect(connectDevice.bluetoothAddress);
                    LogUtils.i("connect bRet = " + isConnSuccessful);
                    productList.get(position).setStatus(1);
                    adapter.notifyDataSetChanged();
                    isHas = true;
                    break;
                }
            }
        }
        if (!isHas) {
            showToast("未能检测到该设备，请稍后重试");
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Intent intent = new Intent(getActivity(), clazz);
        intent.putExtra(MAC_ADDRESS, bluetoothAddress);
        intent.putExtra(CONNECTED_STATUS, isConnSuccessful);
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
            startTheProductActivity(BateryActivity.class, bluetoothAddress, position);
        } else {
            Intent intent = new Intent(getActivity(), YunCheListActivity.class);
            startActivity(intent);
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
                PreferenceUtils.write(getContext(), "productInfo", productName, true);

            } else {
                if (names.contains(addDeviceEntity.getTitle())) {
                    showToast("您已经添加过该设备了");
                } else {
                    productList.add(addDeviceEntity);
                    names.add(addDeviceEntity.getTitle());
                    adapter.notifyDataSetChanged();

                    String productName = addDeviceEntity.getDeviceName();
                    PreferenceUtils.write(getContext(), "productInfo", productName, true);

                }
            }
        }
    }

    class MyConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int error = intent.getIntExtra("error", -1);
            if (error == 0) {
                LogUtils.e("连接状态改变--接收器");
                if (connectDevice != null) {
                    boolean bRet = mBLE.connect(connectDevice.bluetoothAddress);
                    LogUtils.i("connect bRet = " + bRet);
                    showToast("正在连接设备并获取服务中");
                } else {
                    showToast("连接设备失败，请重新尝试");
                }
            }
        }
    }

    public static void disconnect() {
        mBLE.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mDeviceReceiver);
        getActivity().unregisterReceiver(mConnectReceiver);
        scanLeDevice(false);
//            mBLE.disconnect();
//            mBLE.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        scanLeDevice(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
    }
}
