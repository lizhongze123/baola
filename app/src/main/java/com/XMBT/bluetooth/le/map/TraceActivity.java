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
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.lzy.okgo.OkGo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzz on 2017/8/17.
 */


public class TraceActivity extends BaseActivity implements View.OnClickListener {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    /**
     * 轨迹线
     */
    private Polyline mVirtureRoad;
    /**
     * 车辆marker
     */
    private Marker mMoveMarker;
    private ImageView ivPlay, ivPause;
    private TextView run_speed, tvInfo;

    private View overlayView;
    private SeekBar progressSeekBar, speedSeekBar;
    private YunCheDeviceEntity device;

    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private int TIME_INTERVAL = 500;
    private int default_INTERVAL = 500;
    private double DISTANCE = 0.01;

    private TrackThread trackThread;

    //播放控制
    private boolean isFinish = false;
    private boolean isPause = false;
    private boolean isPlay = false;
    /**
     * 轨迹点集合
     */
    private List<LatLng> polylines = new ArrayList<>();
    /**
     * 轨迹的第一个点
     */
    private LatLng firstPoint;

    private final int RESET_PROGRESS = 1000;
    private final int MOVE_PROGRESS = 1001;
    private final int PROGRESS_FINISH = 1003;

    private int mProgress; //获取进度条进度


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESET_PROGRESS:
                    reset();
                    break;
                case PROGRESS_FINISH:
                    //初始化定位，如果没有数据的话
                    isPlay = false;
                    progressSeekBar.setProgress(0);
                    mMoveMarker.setPosition(firstPoint);
                    ivPlay.setVisibility(View.VISIBLE);
                    ivPause.setVisibility(View.GONE);
                    isFinish = false;
                    trackThread = null;
                    break;
            }
        }
    };

    private void reset() {
        OverlayOptions markerOptions;
        markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory
                .fromResource(R.drawable.icon_car_loc)).position(polylines.get(0)).rotate((float) getAngle(0));
        mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initViews();
//        initRoadData();
    }

    private void initViews() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
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

    private void initRoadData() {
        // init latlng data
        double centerLatitude = 39.916049;
        double centerLontitude = 116.399792;
        double deltaAngle = Math.PI / 180 * 5;
        double radius = 0.02;
        OverlayOptions polylineOptions;

        for (double i = 0; i < Math.PI * 2; i = i + deltaAngle) {
            float latitude = (float) (-Math.cos(i) * radius + centerLatitude);
            float longtitude = (float) (Math.sin(i) * radius + centerLontitude);
            polylines.add(new LatLng(latitude, longtitude));
            if (i > Math.PI) {
                deltaAngle = Math.PI / 180 * 30;
            }
        }

        float latitude = (float) (-Math.cos(0) * radius + centerLatitude);
        float longtitude = (float) (Math.sin(0) * radius + centerLontitude);
        //第一个点
        polylines.add(new LatLng(latitude, longtitude));
        firstPoint = polylines.get(0);

        polylineOptions = new PolylineOptions().points(polylines).width(10).color(Color.RED);

        mVirtureRoad = (Polyline) mBaiduMap.addOverlay(polylineOptions);

        reset();
    }

    private void drawTrace(List<PointBean> list) {
        if (list.size() >= 2) {
            List<LatLng> latLngs = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                double lng = list.get(i).getLongitude();
                double lat = list.get(i).getLatitude();
                LatLng tem = new LatLng(lat, lng);
                latLngs.add(tem);
            }
            OverlayOptions polylineOptions;
            polylineOptions = new PolylineOptions().points(polylines).width(10).color(Color.RED);
            mVirtureRoad = (Polyline) mBaiduMap.addOverlay(polylineOptions);
            reset();
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(latLngs.get(0), 17.0f);
            mBaiduMap.animateMapStatus(u);
        }
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
        LogUtils.d(startTime + "//" + endTime);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_go_pause:
                if (isPlay) {
                    isPause = true;
                    isPlay = false;
                    ivPlay.setVisibility(View.VISIBLE);
                    ivPause.setVisibility(View.GONE);
                }
                break;
            case R.id.iv_go_play:
                if (polylines.size() <= 0) {
                    //没有当前的轨迹数据
                    return;
                }
                ivPlay.setVisibility(View.GONE);
                ivPause.setVisibility(View.VISIBLE);
                if (isPause) {
                    synchronized (trackThread) {
                        trackThread.notify();
                    }
                }
                if (trackThread == null && !isFinish) {
                    progressSeekBar.setMax(polylines.size());
                    trackThread = new TrackThread();
                    trackThread.start();
                } else {
                    if (!isPause) {
                        showToast("车正在行驶..请勿重复操作");
                    }
                }
                isPause = false;
                isPlay = true;
                break;
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap.clear();
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
            TIME_INTERVAL = default_INTERVAL;
            TIME_INTERVAL = TIME_INTERVAL / mProgress;
        }
    };

    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    /**
     * 循环进行移动逻辑
     */

    class TrackThread extends Thread {

        @Override
        public void run() {
            try {

                for (int i = 0; i < mVirtureRoad.getPoints().size() - 1; i++) {
                    synchronized (trackThread) {

                        try {
                            if (isPause) {
                                trackThread.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        final LatLng startPoint = mVirtureRoad.getPoints().get(i);
                        final LatLng endPoint = mVirtureRoad.getPoints().get(i + 1);
                        mMoveMarker.setPosition(startPoint);

                        List<LatLng> objList = new ArrayList<>();
                        objList.add(startPoint);
                        objList.add(endPoint);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // refresh marker's rotate
                                if (mMapView == null) {
                                    return;
                                }
                                mMoveMarker.setRotate((float) getAngle(startPoint,
                                        endPoint));
                            }
                        });
                        double slope = getSlope(startPoint, endPoint);
                        //是不是正向的标示（向上设为正向）
                        boolean isReverse = (startPoint.latitude > endPoint.latitude);

                        double intercept = getInterception(slope, startPoint);

                        double xMoveDistance = isReverse ? getXMoveDistance(slope)
                                : -1 * getXMoveDistance(slope);


                        for (double j = startPoint.latitude;
                             !((j > endPoint.latitude) ^ isReverse);

                             j = j - xMoveDistance) {
                            LatLng latLng;
                            if (slope != Double.MAX_VALUE) {
                                latLng = new LatLng(j, (j - intercept) / slope);
                            } else {
                                latLng = new LatLng(j, startPoint.longitude);
                            }

                            final LatLng finalLatLng = latLng;

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mMapView == null) {
                                        return;
                                    }
                                    // refresh marker's position
                                    mMoveMarker.setPosition(finalLatLng);
                                }
                            });

                            try {
                                Thread.sleep(TIME_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        progressSeekBar.setProgress(i);

                        if (i == mVirtureRoad.getPoints().size() - 2) {
                            isFinish = true;
                            mHandler.sendEmptyMessage(PROGRESS_FINISH);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    /**
     * 根据点获取图标转的角度
     */
    private double getAngle(int startIndex) {
        if ((startIndex + 1) >= mVirtureRoad.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mVirtureRoad.getPoints().get(startIndex);
        LatLng endPoint = mVirtureRoad.getPoints().get(startIndex + 1);
        return getAngle(startPoint, endPoint);
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    /**
     * 算取斜率
     */
    private double getSlope(int startIndex) {
        if ((startIndex + 1) >= mVirtureRoad.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mVirtureRoad.getPoints().get(startIndex);
        LatLng endPoint = mVirtureRoad.getPoints().get(startIndex + 1);
        return getSlope(startPoint, endPoint);
    }

    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;

    }

}
