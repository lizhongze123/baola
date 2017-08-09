package com.XMBT.bluetooth.le.ui.gbattery;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.List;


class DeviceListAdapter extends BaseSwipeAdapter {

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
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup viewGroup) {
        View convertView = View.inflate(mContext, R.layout.yunche_list, null);
        SwipeLayout swipeLayout = (SwipeLayout) convertView.findViewById(R.id.swipe);
        swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                // SwipeLayout划出时调用
            }
        });
        swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout layout, boolean surface) {
                // SwipeLayout双击时调用
            }
        });
        convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 监听SwipeLayout中的组件的点击事件
                onDeleteListener.onDelete(position);
            }
        });

        return convertView;
    }

    @Override
    public void fillValues(int position, View convertView) {
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
    }

    public interface OnDeleteListener {
        void onDelete(int pos);
    }

    private OnDeleteListener onDeleteListener;

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }
}
