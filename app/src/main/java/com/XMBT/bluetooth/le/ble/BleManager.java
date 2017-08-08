package com.XMBT.bluetooth.le.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.view.loadingdialog.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzz on 2017/8/7.
 */

public class BleManager {

    public static final String CONNECTED_STATUS = "connected_status";

    public static final String SCAN_BLE_STATUS = "scan_ble_status";
    public static final int SCAN_BLE_FAILED = 0;
    public static final int SCAN_BLE_SUCCESSFUL = 1;

    private static BleManager singleton = null;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeClass mBLE;
    public static boolean isConnSuccessful = false;

    public static BluetoothGattCharacteristic gattCharacteristic_write = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char2 = null;

    private iBeaconClass.iBeacon connectDevice = null;
    /**
     * 扫描出来的设备
     */
    private ArrayList<iBeaconClass.iBeacon> mLeDevices;

    private LoadingDialog loadingDialog;

    private String productName = "";

    public static BleManager getInstance(Context context) {
        if (singleton == null) {
            synchronized (BleManager.class) {
                if (singleton == null) {
                    singleton = new BleManager(context);
                }
            }
        }
        return singleton;
    }

    private BleManager(Context context) {
        mContext = context;
        initBle();
    }

    public boolean isSupportBle() {
        return mContext.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean initBle() {
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter.enable();
        mBLE = new BluetoothLeClass(mContext);
        if (!mBLE.initialize()) {
            return false;
        }
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        return true;
    }

    /**
     * 扫描蓝牙设备
     *
     * @param
     */
    public void startScan(Context context, String productName) {
        this.productName = productName;
        loadingDialog = new LoadingDialog(context);
        loadingDialog.setLoadingText("正在连接设备并获取服务中");
        loadingDialog.show();

        mLeDevices = new ArrayList<>();
        mLeCallback.setBleManager(this).notifyScanStarted();
        boolean success = mBluetoothAdapter.startLeScan(mLeCallback);
        if (success) {

        } else {
            mLeCallback.removeHandlerMsg();
        }
    }


    public void stopScan() {
        mLeCallback.removeHandlerMsg();
        mBluetoothAdapter.stopLeScan(mLeCallback);
    }


    /***
     * 蓝牙扫描回调
     */
    private PeriodScanCallback mLeCallback = new PeriodScanCallback(5000) {
        @Override
        public void onScanTimeout() {
            boolean isHas = false;
            for (int i = 0; i < mLeDevices.size(); i++) {
                LogUtils.e("mLeDevices.size()--" + i + "///name is--" + mLeDevices.get(i).name);
                //如果设备名字相同就连接
                if (mLeDevices.get(i).name != null && mLeDevices.get(i).name.equals(productName)) {
                    connectDevice = mLeDevices.get(i);
                    if (connectDevice != null) {
                        isHas = true;
                        //连接ble
                        isConnSuccessful = mBLE.connect(connectDevice.bluetoothAddress);
                        LogUtils.e("connect bRet = " + isConnSuccessful);
                        break;
                    }
                }
            }
            // 发送广播
            Intent mIntent = new Intent(GlobalConsts.ACTION_SCAN_BLE_OVER);
            if (isHas) {
                mIntent.putExtra(SCAN_BLE_STATUS, SCAN_BLE_SUCCESSFUL);
            } else {
                mIntent.putExtra(SCAN_BLE_STATUS, SCAN_BLE_FAILED);

            }
            mContext.sendBroadcast(mIntent);
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            final iBeaconClass.iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
            addDevice(ibeacon);
        }

    };

    /**
     * 把扫描出来的设备添加进来，不重复添加
     *
     * @param device
     */
    public void addDevice(iBeaconClass.iBeacon device) {
        if (device == null) {
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

    public void disconnect() {
        isConnSuccessful = false;
        mBLE.disconnect();
    }

    public static void WriteCharX(BluetoothGattCharacteristic GattCharacteristic, byte[] writeValue) {
        if (GattCharacteristic != null) {
            GattCharacteristic.setValue(writeValue);
            mBLE.writeCharacteristic(GattCharacteristic);
        }
    }

}
