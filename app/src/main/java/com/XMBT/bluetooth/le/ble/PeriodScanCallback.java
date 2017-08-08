package com.XMBT.bluetooth.le.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by lzz on 2017/8/7.
 */

public abstract class PeriodScanCallback implements BluetoothAdapter.LeScanCallback{

    private Handler handler = new Handler(Looper.getMainLooper());
    private long time = 10000;
    BleManager bleManager;

    PeriodScanCallback(long time){
        this.time = time;
    }

    public abstract void onScanTimeout();

    public void notifyScanStarted(){
        if(time > 0){
            removeHandlerMsg();
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onScanTimeout();
                bleManager.stopScan();
            }
        },time);
    }

    public void removeHandlerMsg(){
        handler.removeCallbacksAndMessages(null);
    }

    public PeriodScanCallback setBleManager(BleManager bleManager){
        this.bleManager = bleManager;
        return this;
    }
}
