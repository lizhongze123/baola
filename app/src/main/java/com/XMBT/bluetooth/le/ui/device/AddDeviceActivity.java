package com.XMBT.bluetooth.le.ui.device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.AddDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.view.TitleBar;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加设备
 */
public class AddDeviceActivity extends BaseActivity {

    private ListView listView;
    private List<AddDeviceEntity> addDeviceEntities = new ArrayList<>();
    private AddedBleDeviceAdapter adapter;
    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        initView();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView2);
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initDatas();

        adapter = new AddedBleDeviceAdapter(addDeviceEntities, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(AddDeviceActivity.this)
                        .setMessage("是否确定要添加此设备?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(GlobalConsts.FILTER_ADD_DEVICE);
                                intent.putExtra("addDeviceEntity", addDeviceEntities.get(position));
                                sendBroadcast(intent);
                                finish();
                            }
                        }).setNegativeButton("取消",null).create().show();
            }
        });
    }

    private void initDatas() {
        AddDeviceEntity addDeviceEntity = new AddDeviceEntity();
        addDeviceEntity.setImg(R.drawable.xm_ligh);
        addDeviceEntity.setTitle(getString(R.string.lighting));
        addDeviceEntity.setDeviceName(GlobalConsts.LIGHTING);
        addDeviceEntities.add(0,addDeviceEntity);

        AddDeviceEntity addDeviceEntity1 = new AddDeviceEntity();
        addDeviceEntity1.setImg(R.drawable.battery);
        addDeviceEntity1.setTitle(getString(R.string.power));
        addDeviceEntity1.setDeviceName(GlobalConsts.POWER);
        addDeviceEntities.add(1,addDeviceEntity1);

        AddDeviceEntity addDeviceEntity2 = new AddDeviceEntity();
        addDeviceEntity2.setDeviceName(GlobalConsts.BATTERY);
        addDeviceEntity2.setTitle(getString(R.string.battery));
        addDeviceEntity2.setImg(R.drawable.battery_electric);
        addDeviceEntities.add(2,addDeviceEntity2);

        AddDeviceEntity addDeviceEntity3 = new AddDeviceEntity();
        addDeviceEntity3.setDeviceName(GlobalConsts.GPS_BATTERY);
        addDeviceEntity3.setTitle(getString(R.string.gpsbattery));
        addDeviceEntity3.setImg(R.drawable.battery_automobile);
        addDeviceEntities.add(3,addDeviceEntity3);
    }


}
