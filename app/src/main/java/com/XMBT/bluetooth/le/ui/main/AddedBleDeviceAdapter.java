package com.XMBT.bluetooth.le.ui.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.bean.AddDeviceEntity;

import java.util.List;

/**
 * Created by haowenlee on 2016/10/21.
 */
public class AddedBleDeviceAdapter extends BaseAdapter {
    List<AddDeviceEntity> addDeviceEntities;
    Context context;

    public AddedBleDeviceAdapter(List<AddDeviceEntity> addDeviceEntities, Context context) {
        this.addDeviceEntities = addDeviceEntities;
        this.context = context;
    }

    @Override
    public int getCount() {
        return addDeviceEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return addDeviceEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(context, R.layout.listitem_device, null);
        TextView titleTv = (TextView) convertView.findViewById(R.id.device_name);
        ImageView picIv = (ImageView) convertView.findViewById(R.id.imageView3);
        titleTv.setText(addDeviceEntities.get(position).getTitle());
        picIv.setImageResource(addDeviceEntities.get(position).getImg());
        return convertView;
    }
}
