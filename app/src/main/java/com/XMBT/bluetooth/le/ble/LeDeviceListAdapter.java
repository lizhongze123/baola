package com.XMBT.bluetooth.le.ble;

import java.util.ArrayList;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.bean.iBeaconClass.iBeacon;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class LeDeviceListAdapter extends BaseAdapter {


    private ArrayList<iBeacon> mLeDevices;
    private LayoutInflater mInflator;
    private Activity mContext;

    public LeDeviceListAdapter(Activity c) {
        super();
        mContext = c;
        mLeDevices = new ArrayList<iBeacon>();
        mInflator = mContext.getLayoutInflater();
    }

    public void addDevice(iBeacon device) {
        if (device == null)
            return;

        for (int i = 0; i < mLeDevices.size(); i++) {
            String btAddress = mLeDevices.get(i).bluetoothAddress;
            if (btAddress.equals(device.bluetoothAddress)) {
                mLeDevices.add(i + 1, device);
                mLeDevices.remove(i);
                return;
            }
        }
        mLeDevices.add(device);

    }

    public iBeacon getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (view == null) {
            view = mInflator.inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
//			viewHolder.deviceAddress 		= (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
//			viewHolder.deviceUUID 			= (TextView) view.findViewById(R.id.device_beacon_uuid);
//			viewHolder.deviceMajor_Minor 	= (TextView) view.findViewById(R.id.device_major_minor);
//			viewHolder.devicetxPower_RSSI 	= (TextView) view.findViewById(R.id.device_txPower_rssi);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (mLeDevices.size() == 0) {
            Toast.makeText(mContext, "未搜索到设备", Toast.LENGTH_SHORT).show();
        } else {
            iBeacon device = mLeDevices.get(i);
            final String deviceName = device.name;

//		if (deviceName != null && deviceName.length() > 0)
//			viewHolder.deviceName.setText(deviceName);
//		else
//			viewHolder.deviceName.setText(R.string.unknown_device);

            if (deviceName != null && deviceName.length() > 0) {
                if (deviceName.equals("BCM90") || deviceName.equals("Pourio HID")) {
                    viewHolder.deviceName.setText("智能氙气灯");
                } else {
                    viewHolder.deviceName.setText(R.string.unknown_device);
                }
            }
            if (device.isIbeacon) {
                viewHolder.deviceName.append(" [iBeacon]");
            }

//		viewHolder.deviceAddress.setText(device.bluetoothAddress);
//		viewHolder.deviceUUID.setText(device.proximityUuid);
//
//		if (device.isIbeacon) {
//			viewHolder.deviceMajor_Minor.setText("major:" + device.major + ",minor:" + device.minor);
//			viewHolder.devicetxPower_RSSI.setText("txPower:" + device.txPower + ", rssi:" + device.rssi);
//		}
//		else {
//			viewHolder.devicetxPower_RSSI.setText("rssi:" + device.rssi);
//		}

//			if (i % 2 == 0) {
//				view.setBackgroundColor(Color.argb(25, 255, 0, 0));
//			} else {
//				view.setBackgroundColor(Color.argb(25, 0, 255, 0));
//			}
        }
        return view;
    }

    class ViewHolder {
        TextView deviceName;
//		TextView deviceAddress;
//		TextView deviceUUID;
//		TextView deviceMajor_Minor;
//		TextView devicetxPower_RSSI;
    }
}
