package com.XMBT.bluetooth.le.ui.device.battery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.bean.RecordBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/25.
 */

public class DrivingRecordAdapter extends RecyclerView.Adapter<RecordHolder> {

    private List<RecordBean> dataList = new ArrayList<>();
    private Context context;
    public static final int TYPE_TIME = 0;
    public static final int TYPE_FEED = 1;

    @Override
    public int getItemViewType(int position) {
        if (dataList.size() >= 2) {
            if (position != 0) {
                RecordBean bean = dataList.get(position);
                if (bean.date.equals(dataList.get(position - 1).date)) {
                    return TYPE_FEED;
                } else {
                    return TYPE_TIME;
                }
            } else {
                return TYPE_TIME;
            }

        } else {
            return TYPE_TIME;
        }

    }

    @Override
    public RecordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_record, parent, false);
        return new RecordHolder(rootView, mListener);
    }

    @Override
    public void onBindViewHolder(RecordHolder holder, int position) {
        holder.setData(context, dataList, position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public List<RecordBean> getAll() {
        return this.dataList;
    }

    public void clear() {
        this.dataList.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<RecordBean> dataList) {
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public interface OnRecordItemClickListener {
        void onItemClick();
    }

    OnRecordItemClickListener mListener;

    public void setOnItemClickListener(OnRecordItemClickListener listener) {
        mListener = listener;
    }

}

class RecordHolder extends RecyclerView.ViewHolder {

    private DrivingRecordAdapter.OnRecordItemClickListener listener;
    private TextView tvDate;
    private TextView tvStartTime;
    private TextView tvStopTime;
    private TextView tvDuration;

    public RecordHolder(View itemView, DrivingRecordAdapter.OnRecordItemClickListener listener) {
        super(itemView);
        this.listener = listener;
        tvDate = (TextView) itemView.findViewById(R.id.tv_date);
        tvStartTime = (TextView) itemView.findViewById(R.id.tv_startTime);
        tvStopTime = (TextView) itemView.findViewById(R.id.tv_stopTime);
        tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
    }

    public void setData(final Context context, List<RecordBean> dataList, int position) {
        RecordBean bean = dataList.get(position);
        if (bean == null) {
            return;
        }
        if (dataList.size() >= 2) {
            if (position != 0) {
                if (bean.date.equals(dataList.get(position - 1).date)) {
                    tvDate.setVisibility(View.GONE);
                } else {
                    tvDate.setVisibility(View.VISIBLE);
                }
            } else {
                tvDate.setVisibility(View.VISIBLE);
            }
        } else {
            tvDate.setVisibility(View.VISIBLE);
        }
        tvDate.setText(bean.date);
        tvStartTime.setText(bean.startTime);
        tvStopTime.setText(bean.stopTime);
        tvDuration.setText(bean.duration);
    }
}