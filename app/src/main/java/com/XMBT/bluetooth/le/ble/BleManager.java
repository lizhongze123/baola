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
import android.text.TextUtils;
import android.util.Log;

import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.consts.BatteryCache;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.consts.SampleGattAttributes;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.LoginUtil;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.XMBT.bluetooth.le.utils.Utils;
import com.XMBT.bluetooth.le.view.loadingdialog.LoadingDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class BleManager {

    public static final String CONNECTED_STATUS = "connected_status";

    public static final String SCAN_BLE_STATUS = "scan_ble_status";
    public static final String SCAN_BLE_DATA = "scan_ble_data";
    public static final int SCAN_BLE_FAILED = 0;
    public static final int SCAN_BLE_SUCCESSFUL = 1;

    private static BleManager singleton = null;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeClass mBLE;
    /**
     * 是否连接成功
     */
    public static boolean isConnSuccessful = false;
    /**
     * 当前连接的类型
     */
    public static String CONNECT_TYPE = "";

    public static BluetoothGattCharacteristic gattCharacteristic_write = null;
    public static BluetoothGattCharacteristic gattCharacteristic_char2 = null;

    private iBeaconClass.iBeacon connectDevice = null;
    /**
     * 扫描出来的设备
     */
    private ArrayList<iBeaconClass.iBeacon> mLeDevices;

    private LoadingDialog loadingDialog;

    private int SCAN_TIME = 8000;
    private boolean mScanning = false;

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
        mScanning = true;
        boolean success = mBluetoothAdapter.startLeScan(mLeCallback);
        if (success) {

        } else {
            mLeCallback.removeHandlerMsg();
        }
    }

    public void startScan(Context context, String productName, PeriodScanCallback callback) {
        this.productName = productName;
        loadingDialog = new LoadingDialog(context);
        loadingDialog.setLoadingText("正在连接设备并获取服务中");
        loadingDialog.show();

        mLeDevices = new ArrayList<>();
        mLeCallback.setBleManager(this).notifyScanStarted();
        mScanning = true;
        boolean success = mBluetoothAdapter.startLeScan(callback);
        if (success) {

        } else {
            mLeCallback.removeHandlerMsg();
        }
    }


    public void stopScan() {
        mScanning = false;
        mLeCallback.removeHandlerMsg();
        mBluetoothAdapter.stopLeScan(mLeCallback);
    }


    /***
     * 蓝牙扫描回调
     */
    private PeriodScanCallback mLeCallback = new PeriodScanCallback(SCAN_TIME) {
        @Override
        public void onScanTimeout() {
            LogUtils.d("mLeDevices.size()--" + mLeDevices.size());

            Intent mIntent = new Intent(GlobalConsts.ACTION_SCAN_BLE_OVER);
            mIntent.putExtra(SCAN_BLE_DATA, mLeDevices);
            mContext.sendBroadcast(mIntent);
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            mScanning = false;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            if(device.getName() != null){
                if(device.getName().equals(GlobalConsts.POWER)
                        || device.getName().equals(GlobalConsts.LIGHTING)
                        || device.getName().equals(GlobalConsts.BATTERY)
                        || device.getName().equals(GlobalConsts.GPS_BATTERY)){
                    final iBeaconClass.iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
                    LogUtils.d("添加蓝牙设备--"+device.getAddress());
                    addDevice(ibeacon);
                }else{
                    LogUtils.d("扫描出未知设备--"+device.getAddress());
                }
            }else{
                LogUtils.d("未知设备"+device.getAddress());
            }
        }

    };

    public void realConnect(final String type, final String address){
        if(!TextUtils.isEmpty(address)){

            mBLE.connect(address);
            mBLE.setOnConnectListener(new BluetoothLeClass.OnConnectListener() {
                @Override
                public void onConnect(BluetoothGatt gatt) {

                        //连接成功后保存地址在本地
                        if(isConnSuccessful){
                            //保存连接成功后的type
                            CONNECT_TYPE = type;
                            if(type.equals(GlobalConsts.BATTERY)){
                                PreferenceUtils.write(mContext, GlobalConsts.SP_BLUETOOTH_DEVICE, GlobalConsts.BATTERY, address);
                            }else if(type.equals(GlobalConsts.POWER)){
                                PreferenceUtils.write(mContext, GlobalConsts.SP_BLUETOOTH_DEVICE, GlobalConsts.POWER, address);
                            }else if(type.equals(GlobalConsts.LIGHTING)){
                                PreferenceUtils.write(mContext, GlobalConsts.SP_BLUETOOTH_DEVICE, GlobalConsts.LIGHTING, address);
                            }

                        }

                }
            });
            mBLE.setOnDataAvailableListener(new BluetoothLeClass.OnDataAvailableListener() {
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    //notify
                    String strTemp = "";
                    boolean isGood = Conversion.isAsciiPrintable(new String(characteristic.getValue(), 0, characteristic.getValue().length));
                    if (!isGood) {
                        strTemp = Conversion.BytetohexString(characteristic.getValue(), characteristic.getValue().length);
                    }
                        if (!TextUtils.isEmpty(strTemp)) {
                            strTemp = strTemp.replaceAll(":", "");
                        }

                        if (!strTemp.equals("0000000000")) {  //过滤掉00:00:00:00:00

                            if (strTemp.length() == 10) {



                                if(BleManager.CONNECT_TYPE.equals(GlobalConsts.BATTERY)){
                                    notifyBattery(strTemp);
                                }else if(BleManager.CONNECT_TYPE.equals(GlobalConsts.POWER)){
                                }else if(BleManager.CONNECT_TYPE.equals(GlobalConsts.LIGHTING)){
                                }


                            }


                        } else {
                            LogUtils.e("数据为--0000000000");
                        }

                }
            });

        }
//        for (int i = 0; i < mLeDevices.size(); i++) {
//            //如果设备名字相同就连接
//            if (mLeDevices.get(i).name != null && mLeDevices.get(i).name.equals(productName)) {
//                connectDevice = mLeDevices.get(i);
//                if (connectDevice != null) {
//                    //连接ble
//                    isConnSuccessful = mBLE.connect(connectDevice.bluetoothAddress);
//                    LogUtils.e("connect bRet = " + isConnSuccessful);
//                    break;
//                }
//            }
//        }
    }

    /**
     * 把扫描出来的设备添加进来，不重复添加
     *
     * @param device
     */
    public void addDevice(iBeaconClass.iBeacon device) {
        if (device == null) {
            return;
        }
        if(mLeDevices.size() != 0){
            boolean isRepeat = false;
            for (int i = 0; i < mLeDevices.size(); i++) {
                String btAddress = mLeDevices.get(i).bluetoothAddress;
                if(btAddress.equals(device.bluetoothAddress)){
                    isRepeat = true;
                    break;
                }
            }
            if(!isRepeat){
                mLeDevices.add(device);
            }
        }else{
            mLeDevices.add(device);
        }

        Intent mIntent = new Intent(GlobalConsts.ACTION_SCAN_NEW_DEVICE);
        mIntent.putExtra(SCAN_BLE_DATA, mLeDevices);
        mContext.sendBroadcast(mIntent);
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
        if (gattServices == null) {
            return;
        }

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


    /**
     * 智能电瓶相关指令
     */
    private void notifyBattery(String strTemp) {

        String substr = strTemp.substring(0, 6);
        String substr2 = strTemp.substring(6, 10);

        if(substr.equals(SampleGattAttributes.SEND_CHECK)){
            LogUtils.e("----防止蓝牙死机指令发送----");
            WriteCharX(BleManager.gattCharacteristic_write, HexUtil.hexStringToBytes(SampleGattAttributes.RECEIVE_CHECK));
        } else if (substr.equals(SampleGattAttributes.USE_DAYS)) {
            //试用天数
            BatteryCache.usedDay = Integer.parseInt(substr2, 16) + "天";
            //广播通知在service里
        } else if (substr.equals(SampleGattAttributes.STOP_DAYS)) {
            //停用天数
            BatteryCache.stopDay = Integer.parseInt(substr2, 16) + "天";
        } else if (substr.equals(SampleGattAttributes.START_COUNT)) {
            //启动次数
            BatteryCache.startCounts = Integer.parseInt(substr2, 16) + "次";
        } else if(strTemp.equals(SampleGattAttributes.BATTERY_CHARGING_LOW)){
            //充电中 电压偏低
            BatteryCache.tvSstatus = "充电中 电压偏低";
        } else if(strTemp.equals(SampleGattAttributes.BATTERY_CHARGING_NORMAL)){
            //充电中 电压正常
            BatteryCache.tvSstatus = "充电中 电压正常";
        } else if(strTemp.equals(SampleGattAttributes.BATTERY_CHARGING_HIGHT)){
            //充电中 电压过高
            BatteryCache.tvSstatus = "充电中 电压过高";
        } else if(strTemp.equals(SampleGattAttributes.BATTERY_LOW)){
            //电压偏低
            BatteryCache.tvSstatus = "电压偏低";
        } else if(strTemp.equals(SampleGattAttributes.BATTERY_NORMAL)){
            //电压正常
            BatteryCache.tvSstatus = "电压正常";
        }else if(substr.equals(SampleGattAttributes.PERCENT)){
            //百分比
            int progress = Integer.parseInt(substr2.substring(0,2), 16); //702
            BatteryCache.progress = progress;
        } else if(substr.equals(SampleGattAttributes.P_BATTERY_VOLTAGE)){
            //实时电压
            int voltage10 = Integer.parseInt(substr2, 16); //702
            BatteryCache.voltage = voltage10 * 1.0 / 100 + " V";
        } else if(strTemp.equals(SampleGattAttributes.MCU_NEED_TIME)){
            //发送date
            Calendar cal = Calendar.getInstance();
            String year = Integer.toHexString(Integer.valueOf(String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4)));
            String day = Integer.toHexString(cal.get(Calendar.DATE));
            String month = Integer.toHexString(cal.get(Calendar.MONTH) + 1);

            String newValue = SampleGattAttributes.APP_DATE + year + month + day;
            byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);
            BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);

            String hour = Integer.toHexString(cal.get(Calendar.HOUR_OF_DAY));
            String minute = Integer.toHexString(cal.get(Calendar.MINUTE));
            String newValue1 = SampleGattAttributes.APP_TIME + hour + minute;
            byte[] dataToWrite1 = HexUtil.hexStringToBytes(newValue1);
            BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite1);

        }
    }
}
