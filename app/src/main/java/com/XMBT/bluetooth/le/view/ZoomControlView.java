package com.XMBT.bluetooth.le.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.XMBT.bluetooth.le.R;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;

/**
 * Created by haowenlee on 2017/4/20.
 */
public class ZoomControlView extends LinearLayout implements View.OnClickListener {

    private MapView mapView;

    private BaiduMap baiduMap;//百度地图对象控制器

    private MapStatus mapStatus;//百度地图状态

    private ImageView zoomOut, zoomIn;

    private float MaxLevel;

    private float MinLevel;

    public ZoomControlView(Context context) {
        super(context);
        initZoomControlView(context);
    }

    public ZoomControlView(Context context, AttributeSet attr) {
        super(context, attr);
        initZoomControlView(context);
    }

    private void initZoomControlView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.zoom_control_layout, this);
        zoomIn = (ImageView) view.findViewById(R.id.zoom_control_plus);
        zoomOut = (ImageView) view.findViewById(R.id.zoom_control_reduce);
        zoomIn.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
    }

    /**
     * set {@link MapView}
     */
    public void setMapView(MapView mapView) {
        if (mapView != null) {
            this.mapView = mapView;
            baiduMap = mapView.getMap();
            mapStatus = baiduMap.getMapStatus();
            MaxLevel = baiduMap.getMaxZoomLevel();
            MinLevel = baiduMap.getMinZoomLevel();
        } else {
            throw new NullPointerException("you should call setMapView(MapView mapView) at first");
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.zoom_control_plus:
                baiduMap.setMapStatus(MapStatusUpdateFactory.zoomIn());//放大
                break;
            case R.id.zoom_control_reduce:
                baiduMap.setMapStatus(MapStatusUpdateFactory.zoomOut());//缩小

                break;
            default:
                break;
        }
        mapStatus = mapView.getMap().getMapStatus();
        refreshZoomControlView();
    }

    private void refreshZoomControlView() {

        float zoom = mapStatus.zoom;

        if (zoom > MinLevel && zoom < MaxLevel) {

            if (!zoomIn.isEnabled()) {
                zoomIn.setEnabled(true); //设置为可点击
            }

            if (!zoomOut.isEnabled()) {
                zoomOut.setEnabled(true);
            }

        } else if (zoom == MinLevel) {
            zoomOut.setEnabled(false);
            zoomIn.setEnabled(true);

        } else {

            zoomIn.setEnabled(false);
            zoomOut.setEnabled(true);
        }
    }

}

