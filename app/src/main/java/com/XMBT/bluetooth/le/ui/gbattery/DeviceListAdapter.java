package com.XMBT.bluetooth.le.ui.gbattery;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.YunCheDeviceEntity;

import java.util.List;


class DeviceListAdapter extends BaseAdapter {

    private Context mContext;
    private List<YunCheDeviceEntity> devices;

    public DeviceListAdapter(Context context, List<YunCheDeviceEntity> devices) {
        this.devices = devices;
        mContext = context;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext, R.layout.yunche_list, null);
        ImageView picIv = (ImageView) convertView.findViewById(R.id.picIv);
        TextView titleTv = (TextView) convertView.findViewById(R.id.titleTv);
        TextView statusTv = (TextView) convertView.findViewById(R.id.statusTv);
        if (devices.get(position).equipmentBatteryVType == 1) {
            picIv.setImageResource(R.drawable.battery_automobile);
        } else {
            picIv.setImageResource(R.drawable.battery_electric);
        }
        titleTv.setText(devices.get(position).fullname);
        if (devices.get(position).equipmentStatus == 0) {
            statusTv.setText("未启用");
        } else if (devices.get(position).equipmentStatus == 1) {
            statusTv.setText("离线");
        } else if (devices.get(position).equipmentStatus == 2) {
            statusTv.setText("在线");
        }
        return convertView;
    }
}
