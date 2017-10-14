/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.XMBT.bluetooth.le.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.utils.LogUtils;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/*
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BluetoothLeClass extends Service {

    private final static String TAG = "BluetoothLeClass";// BluetoothLeClass.class.getSimpleName();

    private final String ACTION_NAME_RSSI = "XM_RSSI"; // 其他文件广播的定义必须一致

    private final String ACTION_NOTIFI = "XM_NOTIFI";

    //接收广播用的
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String EXTRA_UUID = "EXTRA_UUID";
    public final static String EXTRA_STATUS = "EXTRA_STATUS";

    public final static String CONNECT_STATUS = "CONNECT_STATUC";
    public final static int STATE_CONNECTED = 1;
    public final static int STATE_DISCONNECTED = 0;


    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothManager mBluetoothManager;

    //代表本地蓝牙
    private BluetoothAdapter mBluetoothAdapter;

    public String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;

    public interface OnConnectListener {
        public void onConnect(BluetoothGatt gatt);
    }

    public interface OnDisconnectListener {
        public void onDisconnect(BluetoothGatt gatt);
    }

    public interface OnReadRemoteRssiListener {
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi);
    }

    public interface OnServiceDiscoverListener {
        public void onServiceDiscover(BluetoothGatt gatt);
    }

    public interface OnDataAvailableListener {
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status);

        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic);
    }

    private OnConnectListener mOnConnectListener;
    private OnDisconnectListener mOnDisconnectListener;
    private OnReadRemoteRssiListener mOnReadRemoteRssiListener;
    private OnServiceDiscoverListener mOnServiceDiscoverListener;
    private OnDataAvailableListener mOnDataAvailableListener;

    private Context mContext;

    public void setOnConnectListener(OnConnectListener l) {
        mOnConnectListener = l;
    }

    public void setOnDisconnectListener(OnDisconnectListener l) {
        mOnDisconnectListener = l;
    }

    public void setReadRemoteRssiListener(OnReadRemoteRssiListener l) {
        mOnReadRemoteRssiListener = l;
    }

    public void setOnServiceDiscoverListener(OnServiceDiscoverListener l) {
        mOnServiceDiscoverListener = l;
    }

    public void setOnDataAvailableListener(OnDataAvailableListener l) {
        mOnDataAvailableListener = l;
    }

    public BluetoothLeClass(Context c) {
        mContext = c;
    }

    // Implements callback methods for GATT events that the app cares about. For
    // example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                BleManager.isConnSuccessful = true;
                if (mOnConnectListener != null){
                    mOnConnectListener.onConnect(gatt);
                }
                LogUtils.d("Connected to GATT server.");

                // Attempts to discover services after successful connection.
                LogUtils.i("Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

                // 增加读rssi 的定时器
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt.readRemoteRssi();
                        }
                    }
                };
                Timer rssiTimer = new Timer();

                // rssiTimer.schedule(task, 1000, 1000);
//				rssiTimer.schedule(task, 160, 160);

                // 发送广播
                Intent mIntent = new Intent(GlobalConsts.ACTION_CONNECT_CHANGE);
                mIntent.putExtra(CONNECT_STATUS, STATE_CONNECTED);
                mContext.sendBroadcast(mIntent);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                BleManager.isConnSuccessful = false;
                if (mOnDisconnectListener != null){
                    mOnDisconnectListener.onDisconnect(gatt);
                }
                LogUtils.d("Disconnected from GATT server.");

                // 发送广播
                Intent mIntent = new Intent(GlobalConsts.ACTION_CONNECT_CHANGE);
                mIntent.putExtra(CONNECT_STATUS, STATE_DISCONNECTED);
                mContext.sendBroadcast(mIntent);

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && mOnServiceDiscoverListener != null) {
                mOnServiceDiscoverListener.onServiceDiscover(gatt);
            } else {
                LogUtils.i("onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (mOnDataAvailableListener != null)
                mOnDataAvailableListener.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (mOnDataAvailableListener != null)
                mOnDataAvailableListener.onCharacteristicWrite(gatt, characteristic);
            // 不调用它,接收不到数据.
            broadcastUpdate(ACTION_NOTIFI, characteristic);
//            LogUtils.i("收到通知--" + characteristic.getUuid().toString());
        }

        /**
         * RSSI在扫描时可以通过扫描回调接口获得
         * 但是在连接之后要不断地使用mBluetoothGatt.readRemoteRssi()向底层驱动发出读取RSSI请求，当底层获取到新的RSSI后会进行以下回调：
         * @param gatt
         * @param rssi  即是新的信号强度值。
         * @param status
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            LogUtils.i("--onReadRemoteRssi--: " + status + ",   rssi:" + rssi
                    + "----------------------------------");
            updateRssiBroadcast(rssi);
        }
    };

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

            if (mBluetoothManager == null) {
                LogUtils.e("Unable to initialize BluetoothManager.");
                return false;
            }
        }

        //判断是否有蓝牙
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtils.e("Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    // 收到数据就在这个方法里面处理
    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        String txt = new String(characteristic.getValue(), 0, characteristic.getValue().length);
        /* 测试
    	byte[] b = new byte[3];
    	b[0] = 0x04;
    	b[1] = (byte) 0xa7;
    	b[2] = 0x58; 
    	String txt = new String(b,0,b.length);
    	*/
        boolean isGood = Conversion.isAsciiPrintable(txt);
        if (!isGood) {
            txt = Conversion.BytetohexString(characteristic.getValue(), characteristic.getValue().length);
        }
        //txt = Conversion.BytetohexString(b,b.length);
        intent.putExtra(EXTRA_DATA, txt);
        mContext.sendBroadcast(intent);
    }

    public void updateRssiBroadcast(int rssi) {
        LogUtils.i("updateRssiBroadcast1 " + rssi);

        Intent mIntent = new Intent(ACTION_NAME_RSSI);
        mIntent.putExtra("RSSI", rssi);
        // 发送广播
        mContext.sendBroadcast(mIntent);
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            LogUtils.w("BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            LogUtils.d("Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            LogUtils.w("Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        LogUtils.d("Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtils.w("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtils.w("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtils.w("BluetoothAdapter not initialized");
            return;
        }
        if (enabled == true) {

            LogUtils.i("Enable Notification");
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        } else {
            LogUtils.i("Disable Notification");
            mBluetoothGatt.setCharacteristicNotification(characteristic, false);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        mBluetoothGatt.writeCharacteristic(characteristic);
    }


    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
