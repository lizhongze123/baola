package com.XMBT.bluetooth.le.ui.gbattery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;

import java.util.List;

/**
 * Created by lzz on 2017/8/28.
 */

public class AlarmAdapter extends ArrayAdapter<AlarmBean.RowsBean> {

    private static final int RESOURCE_ID = R.layout.item_expand_child;
    private Context mContext;
    private List<String> dataList;
    private LayoutInflater inflater;
    private String type;

    public AlarmAdapter(Context context, String type) {
        super(context, RESOURCE_ID);
        this.mContext = context;
        this.type = type;
        inflater = LayoutInflater.from(context);
    }

    public AlarmAdapter(Context context, List<AlarmBean.RowsBean> datas) {
        super(context, RESOURCE_ID, datas);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExamRecordHolder holder;
        if (convertView == null) {
            convertView = View.inflate(getContext(), RESOURCE_ID, null);
            holder = new ExamRecordHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ExamRecordHolder) convertView.getTag();
        }
        setData(holder, position);
        return convertView;
    }

    public void setData(ExamRecordHolder holder, int position) {
        AlarmBean.RowsBean info = getItem(position);
        holder.setData(info);
    }


    public class ExamRecordHolder {
        private View rootView;
        public TextView tv_info;

        public ExamRecordHolder(View rootView) {
            this.rootView = rootView;
            tv_info = (TextView) rootView.findViewById(R.id.tv_info);
        }

        public void setData(AlarmBean.RowsBean info) {
            if (info == null) {
                return;
            }
            tv_info.setText(info.ptime + "  " + type);
        }
    }
}
