package com.XMBT.bluetooth.le.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.view.DateTimePickDialogUtil;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;

public class TraceActivity extends Activity {
    MapView mMapView = null;
    BaiduMap mBaiduMap;
    YunCheDeviceEntity device;
    MyBroadCastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_trace);
        initView();
        receiver = new MyBroadCastReceiver();
        registerReceiver(receiver, new IntentFilter("datetime"));
        showAlertDialog();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void showAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TraceActivity.this);
        builder.setTitle("请选择时间");
        //    指定下拉列表的显示数据
        final String[] times = {"前天", "昨天", "今天", "前一个小时", "自定义", "取消"};
        //    设置一个下拉的列表选择项
        builder.setItems(times, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(TraceActivity.this, "选择的时间为：" + times[which], Toast.LENGTH_SHORT).show();
                if (times[which].equals("取消")) {
                    builder.show().dismiss();
                } else if (times[which].equals("前天")) {
                    Toast.makeText(TraceActivity.this, "这段时间内没有历史轨迹", Toast.LENGTH_SHORT).show();
                } else if (times[which].equals("昨天")) {
                    Toast.makeText(TraceActivity.this, "这段时间内没有历史轨迹", Toast.LENGTH_SHORT).show();
                } else if (times[which].equals("今天")) {
                    Toast.makeText(TraceActivity.this, "这段时间内没有历史轨迹", Toast.LENGTH_SHORT).show();
                } else if (times[which].equals("前一个小时")) {
                    Toast.makeText(TraceActivity.this, "这段时间内没有历史轨迹", Toast.LENGTH_SHORT).show();
                } else if (times[which].equals("自定义")) {
                    showDateTimePickerDialog();
                }
            }
        });
        builder.show();
    }

    private void showDateTimePickerDialog() {
        DateTimePickDialogUtil dateTimePicker = new DateTimePickDialogUtil(
                TraceActivity.this, "");
        dateTimePicker.dateTimePickDialog(0, "开始时间");
    }

    private void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.backIv:
                onBackPressed();
                break;
            case R.id.dateBtn:
                showAlertDialog();
                break;
        }
    }

    private void getTrace() {
//        Intent intent=getIntent();
//        device= (YunCheDeviceEntity) intent.getSerializableExtra("device");
//        SharedPreferences sp=getSharedPreferences("userInfo",MODE_PRIVATE);
//        String mds=sp.getString("mds",null);
//        String id=sp.getString("id",null);
//        OkGo.post(GlobalConsts.URL+"GetDateServices.asmx/GetDate")
//                .tag(this)
//                .params("method","etHistoryMByMUtcNew")
//                .params("option","cn")
//                .params("mds",mds)
//                .params("school_id",id)
//                .params("custid",id)
//                .params("userID",device.getId())
//                .params("mapType","BAIDU")
//                .params("from",)
//                .params("to",)
    }

    class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int flag = intent.getIntExtra("flag", -1);
            if (flag == 0) {
                DateTimePickDialogUtil dateTimePicker = new DateTimePickDialogUtil(
                        TraceActivity.this, "");
                dateTimePicker.dateTimePickDialog(1, "结束时间");
            }
        }
    }
}
