package com.XMBT.bluetooth.le.ui.gbattery.adapter;

import com.XMBT.bluetooth.le.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class MyAlarmAdapter extends BaseQuickAdapter<AlarmBean.RowsBean, BaseViewHolder> {

    private String type;

    public MyAlarmAdapter(String type) {
        super(R.layout.item_expand_child);
        this.type = type;
    }

    public MyAlarmAdapter(List<AlarmBean.RowsBean> data) {
        super(R.layout.item_expand_child, data);
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, AlarmBean.RowsBean item) {
        viewHolder.setText(R.id.tv_info, item.ptime + "  " + type);
    }
}
