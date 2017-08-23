package com.XMBT.bluetooth.le.ble;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzz on 2017/8/22.
 */

public abstract class MLeScanCallback extends PeriodScanCallback{

    private List<BluetoothDevice> deviceList = new ArrayList<>();

    public MLeScanCallback (long timeoutMillSecond){
        super(timeoutMillSecond);
    }

    @Override
    public void onScanTimeout() {
        onDeviceFound(deviceList);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if(device == null){
            return;
        }
        if(!deviceList.contains(device)){
            deviceList.add(device);
        }
    }

    public abstract void onDeviceFound(List<BluetoothDevice>devices);
}
