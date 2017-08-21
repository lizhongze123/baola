package com.XMBT.bluetooth.le.map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.PointBean;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.http.ApiResultCallback;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.ui.gbattery.TimePopuWindow;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.lzy.okgo.OkGo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * 历史轨迹
 */
public class TraceDemoActivity extends BaseActivity implements View.OnClickListener {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private Overlay lineOverlay;

    private YunCheDeviceEntity device;
    private TitleBar titleBar;
    private ImageView ivPlay, ivPause;
    private TextView run_speed, tvInfo;
    private SeekBar progressSeekBar, speedSeekBar;
    private View overlayView;
    private List<LatLng> pointList = new ArrayList<>();
    private double lat, lng;
    private LatLng firstPoint;
    private Marker markerCar;

    //播放控制
    private boolean isFinish = false;
    private boolean isPause = false;
    private boolean isPlay = false;

    private int mProgress; //获取进度条进度
    private long delayMillis = 1000; //初始播放速度

    private final int RESET_PROGRESS = 1000;
    private final int MOVE_PROGRESS = 1001;

    private TrackThread trackThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initViews();
        initData();
    }

    private void initViews() {
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.setRightOnClicker(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });
        mMapView = (MapView) findViewById(R.id.mapView);
        mBaiduMap = mMapView.getMap();
        mMapView.showZoomControls(false); //设置是否显示缩放控件
        mMapView.removeViewAt(1); //去掉百度logo

        ivPause = (ImageView) findViewById(R.id.iv_go_pause);
        ivPlay = (ImageView) findViewById(R.id.iv_go_play);
        ivPause.setOnClickListener(this);
        ivPlay.setOnClickListener(this);

        overlayView = View.inflate(this, R.layout.overlay_item, null);
        tvInfo = (TextView) overlayView.findViewById(R.id.tv_info);

        progressSeekBar = (SeekBar) findViewById(R.id.seekBar);
        speedSeekBar = (SeekBar) findViewById(R.id.verticalSeekBar);
        run_speed = (TextView) findViewById(R.id.run_speed);
        speedSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private void initData() {
        pointList = testPoint();
        lat = pointList.get(0).latitude;
        lng = pointList.get(0).longitude;
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 17.0f));
        if(mBaiduMap != null){
            mBaiduMap.clear();
        }
        firstPoint = pointList.get(0);
        mHandler.sendEmptyMessage(RESET_PROGRESS);
        drawLine(pointList);
    }

    private List<LatLng> testPoint() {
        Random random = new Random(100);
        LatLng start = new LatLng(30.47523, 114.385532);
        List<LatLng> points = new ArrayList<>();
        int i = 0;
        while (i++ < 100) {
            double lat = random.nextDouble() * 0.1;
            double lng = random.nextDouble() * 0.1;
            LatLng latLng = new LatLng(start.latitude + lat, start.longitude + lng);
            points.add(latLng);
        }
        return points;
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case RESET_PROGRESS:
                    initOverlay(firstPoint);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(firstPoint, 17.0f));
                    break;
                case MOVE_PROGRESS:
                    LatLng point = (LatLng) msg.obj;
                    moveCar(point);
                    break;
                case 1002:
                    //初始化定位，如果没有数据的话
                    initOverlay(new LatLng(lat, lng));
                    break;
            }
        }
    };

    private void initOverlay(LatLng latlng) {
        if(markerCar != null){
            markerCar.remove();
            markerCar = null;
        }

        //定义Maker坐标点
        LatLng point = new LatLng(lat, lng);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_car_loc);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示

        markerCar = (Marker) mBaiduMap.addOverlay(option);
    }

    private void moveCar(LatLng point){
        markerCar.setPosition(point);
        markerCar.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car_loc));
        if(isFinish){
            isFinish = false;
            isPlay = false;
            progressSeekBar.setProgress(0);
            markerCar.setPosition(firstPoint);
            markerCar.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car_loc));
        }
    }

    public void drawLine(List<LatLng> latLngs){
        try{
            if(lineOverlay != null){
                lineOverlay.remove();
                lineOverlay = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(latLngs != null && latLngs.size() >= 2){
            try {
                PolylineOptions lineOptions = new PolylineOptions().width(5).color(Color.RED).points(latLngs);
                lineOverlay = mBaiduMap.addOverlay(lineOptions);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        /**
         * 进度改变
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mProgress = progress / 10;
            run_speed.setText(mProgress + 1 + ".0x");
        }

        /**
         * 开始拖动
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        /**
         * 停止拖动
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            delayMillis = 1000;
            delayMillis = delayMillis - mProgress * 100;
        }
    };

    class TrackThread extends Thread {

        private List<LatLng> locList;

        private TrackThread(List<LatLng> list) {
            this.locList = list;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < locList.size(); i++) {
                    synchronized (trackThread) {
                        try {
                            if (isPause) {
                                trackThread.wait();
                            }
                            Thread.sleep(delayMillis);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (i == locList.size() - 1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ivPlay.setVisibility(View.VISIBLE);
                                    ivPause.setVisibility(View.GONE);
                                }
                            });

                            isFinish = true;
                            progressSeekBar.setProgress(100);
                            trackThread = null;
                        } else {
                            progressSeekBar.setProgress(i);
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(1001, locList.get(i)));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showPopupWindow(View view) {
        final TimePopuWindow popuWindow = new TimePopuWindow(this, view);
        popuWindow.showPopupWindow(view);
        popuWindow.setOnTimeSelectListener(new TimePopuWindow.OnTimeSelectListener() {
            @Override
            public void onSelect(int type, String startTime, String endTime) {
                getTrace(startTime, endTime);
                popuWindow.dismiss();
            }
        });
    }

    /**
     * 获取轨迹点数据
     *
     * @param startTime
     * @param endTime
     */
    private void getTrace(String startTime, String endTime) {
        showLoadingDialog(null);
        Intent intent = getIntent();
        device = (YunCheDeviceEntity) intent.getSerializableExtra("device");
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        String id = UserSp.getInstance(this).getId(GlobalConsts.userName);
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "getHistoryMByMUtcNew")
                .params("option", "cn")
                .params("mds", mds)
                .params("school_id", id)
                .params("custid", id)
                .params("userID", device.id)
                .params("mapType", "BAIDU")
                .params("from", startTime)
                .params("to", endTime)
                .execute(new ApiResultCallback<List<PointBean>>() {

                    @Override
                    public void onSuccessResponse(List<PointBean> data) {
                        showToast("查询成功" + data.size());
                        drawTrace(data);
                    }

                    @Override
                    public void onFailure(String errorCode, String describe) {
                        showToast(describe);
                    }

                    @Override
                    public void onFinish() {
                        dismissLoadingDialog();
                    }
                });
    }

    private void drawTrace(List<PointBean> list) {
        if (lineOverlay != null) {
            lineOverlay.remove();
            lineOverlay = null;
        }
        if (list.size() >= 2) {
            List<LatLng> latLngs = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                double lng = list.get(i).getLongitude();
                double lat = list.get(i).getLatitude();
                LatLng tem = new LatLng(lat, lng);
                latLngs.add(tem);
            }
            PolylineOptions lineOptions = new PolylineOptions().width(5).color(Color.RED).points(latLngs);
            lineOverlay = mBaiduMap.addOverlay(lineOptions);
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(latLngs.get(0), 17.0f);
            mBaiduMap.animateMapStatus(u);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_go_pause:
                if(isPlay){
                    isPause = true;
                    isPlay = false;
                    ivPlay.setVisibility(View.VISIBLE);
                    ivPause.setVisibility(View.GONE);
                }
                break;
            case R.id.iv_go_play:
                if(pointList.size() <= 0){
                    //没有当前的轨迹数据
                    return;
                }
                ivPlay.setVisibility(View.GONE);
                ivPause.setVisibility(View.VISIBLE);
                if(isPause){
                    synchronized (trackThread){
                        trackThread.notify();
                    }
                }
                if (trackThread == null && !isFinish) {
//                        if (markerCar == null) {
//                            initOverlay(firstPoint);
//                        }
                    progressSeekBar.setMax(pointList.size());
                    trackThread = new TrackThread(pointList);
                    trackThread.start();
                } else {
                    if (!isPause) {
//						Toast.makeText(TrajectoryActivity.this, "车正在行驶..请勿重复操作", Toast.LENGTH_SHORT).show();
                    }
                }
                isPause = false;
                isPlay = true;
                break;

        }
    }
}
