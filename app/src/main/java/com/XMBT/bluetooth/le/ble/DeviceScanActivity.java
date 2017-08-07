
package com.XMBT.bluetooth.le.ble;

import android.app.ActionBar;
import android.app.ListActivity;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.utils.Utils;
import com.XMBT.bluetooth.le.ui.gbattery.XM_Bt_Demo;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass.OnDataAvailableListener;
import com.XMBT.bluetooth.le.ble.BluetoothLeClass.OnServiceDiscoverListener;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.bean.iBeaconClass.iBeacon;


public class DeviceScanActivity extends ListActivity {

    private final static String TAG = "DeviceScanActivity";

    public static final int CONNECT_EVENT = 0x000001;
    private final static int REQUEST_CODE = 1;

    private final static String BLE_DEVICE_NAME = "BCM90";


    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String EXTRA_UUID = "EXTRA_UUID";
    public final static String EXTRA_STATUS = "EXTRA_STATUS";

    boolean AutoConectFlag = true;

    public static String UUID_CHAR1 = "00002af1-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR2 = "00002af0-0000-1000-8000-00805f9b34fb";

    static BluetoothGattCharacteristic gattCharacteristic_char1 = null;
    static BluetoothGattCharacteristic gattCharacteristic_char2 = null;

    private LeDeviceListAdapter mLeDeviceListAdapter = null;
    private BluetoothAdapter mBluetoothAdapter;
    static private BluetoothLeClass mBLE;

    public String bluetoothAddress;
    static private byte writeValue_char1 = 0;
    private boolean mScanning;

    private Handler mHandler = null;

    private static final long SCAN_PERIOD = 100000;
    iBeacon xm_device = null;
    RelativeLayout actionbarLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        actionbarLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.actionbar_layout, null);
        mActionBar.setCustomView(actionbarLayout, new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ActionBar.LayoutParams mP = (ActionBar.LayoutParams) actionbarLayout
                .getLayoutParams();
        mP.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        mActionBar.setCustomView(actionbarLayout, mP);
        mActionBar.setTitle("添加设备");
        mActionBar.setHomeAsUpIndicator(R.drawable.back);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBluetoothAdapter.enable();

        mBLE = new BluetoothLeClass(this);
        if (!mBLE.initialize()) {
            finish();
        }
        Log.v("MyLog", "mBLE = e" + mBLE);

        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        mBLE.setOnDataAvailableListener(mOnDataAvailable);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == CONNECT_EVENT) {
                    if (xm_device != null) {
                        if (mScanning) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            mScanning = false;
                        }

                        boolean bRet = mBLE.connect(xm_device.bluetoothAddress);
                        Log.i(TAG, "connect bRet = " + bRet);
                    }
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static public void WriteCharX(BluetoothGattCharacteristic GattCharacteristic, byte[] writeValue) {


        if (GattCharacteristic != null) {
            GattCharacteristic.setValue(writeValue);
            mBLE.writeCharacteristic(GattCharacteristic);
        }
    }

    static public void ReadCharX(BluetoothGattCharacteristic GattCharacteristic) {
        if (GattCharacteristic != null) {
            mBLE.readCharacteristic(GattCharacteristic);

        }
    }

    ;

    static public void setCharacteristicNotification(
            BluetoothGattCharacteristic gattCharacteristic, boolean enabled) {
        if (gattCharacteristic != null) {
            mBLE.setCharacteristicNotification(gattCharacteristic, enabled);
        }
    }

    ;

    static public void setNotifi() {

        if (gattCharacteristic_char2 != null) {
            boolean enabled = true;
            mBLE.setCharacteristicNotification(gattCharacteristic_char2, enabled);
        }
    }

    public void DisplayStart() {

    }

    public void DisplayStop() {

    }

    @Override
    protected void onResume() {

        super.onResume();
        mBLE.close();

        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        DisplayStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanLeDevice(false);
        mBLE.disconnect();
        mBLE.close();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final iBeacon device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) {
            return;
        }
        if (device.name == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "目前不支持该蓝牙设备", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else if (device.name.equals(BLE_DEVICE_NAME) || !device.name.equals("Pourio HID")) {
            bluetoothAddress = device.bluetoothAddress;
            xm_device = device;
            Message msg = new Message();
            msg.what = CONNECT_EVENT;
            mHandler.sendMessage(msg);
            Intent intent = new Intent();
            intent.setClass(DeviceScanActivity.this, XM_Bt_Demo.class);
            intent.putExtra("mac_addr", bluetoothAddress);
            startActivityForResult(intent, REQUEST_CODE);
            Toast toast = Toast.makeText(getApplicationContext(), "正在连接设备并获取服务中", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            Intent intent1 = new Intent("add_device");
            intent1.putExtra("device", (Serializable) device);
            sendBroadcast(intent1);
//			finish();
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServices(mBLE.getSupportedGattServices());
        }

    };

    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new OnDataAvailableListener() {

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.v("MyLog", "onCharRead " + gatt.getDevice().getName() + " read " + characteristic.getUuid().toString() + " -> "
                    + Utils.bytesToHexString(characteristic.getValue()));

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            Log.v("MyLog", "onCharWrite " + gatt.getDevice().getName() + " write "
                    + characteristic.getUuid().toString() + " -> "
                    + new String(characteristic.getValue()));

        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {

            final iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi,
                    scanRecord);

            if (AutoConectFlag == true) {
//				if(ibeacon.name.equals(BLE_DEVICE_NAME))
//				{
//					xm_device = ibeacon;
//					Message msg = new Message();
//					msg.what = CONNECT_EVENT;
//			        mHandler.sendMessage(msg);
//				}
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(ibeacon);
                    mLeDeviceListAdapter.notifyDataSetChanged();
//					for (int i = 0; i < iBeacons.size(); i++) {
//						String btAddress = iBeacons.get(i).bluetoothAddress;
//						if (btAddress.equals(ibeacon.bluetoothAddress)) {
//							iBeacons.add(i + 1, ibeacon);
//							iBeacons.remove(i);
//							return;
//						}
//					}
//					iBeacons.add(ibeacon);
//					addDevice(ibeacon);
                    if (mScanning == true) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                    }
                }
            });

        }
    };

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


                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR1)) {
                    gattCharacteristic_char1 = gattCharacteristic;
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR2)) {
                    gattCharacteristic_char2 = gattCharacteristic;
                    boolean enabled = true;

                    mBLE.setCharacteristicNotification(gattCharacteristic_char2, enabled);
                }

                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    int descPermission = gattDescriptor.getPermissions();


                    byte[] desData = gattDescriptor.getValue();

                }
            }
        }
    }
}
