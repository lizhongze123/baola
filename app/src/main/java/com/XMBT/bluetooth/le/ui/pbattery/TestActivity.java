package com.XMBT.bluetooth.le.ui.pbattery;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.consts.GlobalConsts;

import java.util.List;

/**
 * Created by Administrator on 2017/7/17.
 */

public class TestActivity extends BaseActivity {


    private ListView lv;
    private BleManager bleManager;
    private boolean isConnSuccessful = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        lv = (ListView) findViewById(R.id.lv);
        initBle();
    }

    private void initBle() {
        bleManager = BleManager.getInstance(this);
        if (!bleManager.isSupportBle()) {
            showToast(getResources().getString(R.string.ble_not_supported));
        }
        bleManager.startScan(this, GlobalConsts.BATTERY);
    }

}

class TestBean {
    public String name;
    public String address;
}

class MyAdapter extends BaseAdapter {

    private Context mcontext;
    private List<TestBean> list;

    MyAdapter(Context context, List<TestBean> list) {
        mcontext = context;
        this.list = list;
    }

    private Context context;

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(mcontext, R.layout.item_test, null);
        TextView tvname = (TextView) convertView.findViewById(R.id.name);
        TextView tvaddress = (TextView) convertView.findViewById(R.id.address);
        tvname.setText("" + list.get(position).name);
        tvaddress.setText("" + list.get(position).address);
        return convertView;
    }
}
