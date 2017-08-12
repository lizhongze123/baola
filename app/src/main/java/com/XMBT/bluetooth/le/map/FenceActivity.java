package com.XMBT.bluetooth.le.map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.bean.LocalEntity;
import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.api.fence.CreateFenceRequest;
import com.baidu.trace.api.fence.CreateFenceResponse;
import com.baidu.trace.api.fence.DeleteFenceResponse;
import com.baidu.trace.api.fence.FenceListResponse;
import com.baidu.trace.api.fence.HistoryAlarmResponse;
import com.baidu.trace.api.fence.MonitoredStatusByLocationResponse;
import com.baidu.trace.api.fence.MonitoredStatusResponse;
import com.baidu.trace.api.fence.OnFenceListener;
import com.baidu.trace.api.fence.UpdateFenceResponse;
import com.baidu.trace.model.CoordType;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;


public class FenceActivity extends Activity {
    MapView mMapView = null;
    BaiduMap mBaiduMap;
    YunCheDeviceEntity device;
    List<LocalEntity> localEntities = new ArrayList<LocalEntity>();
    private Map<Integer, Overlay> tempOverlays = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_fence);
        initView();
        getLocate();
    }

    private void initView() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();

    }

    private void getLocate() {
        Intent intent = getIntent();
        device = (YunCheDeviceEntity) intent.getSerializableExtra("device");
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String mds = sp.getString("mds", null);
        String id = sp.getString("id", null);
        OkGo.get(GlobalConsts.URL + "GetDateServices.asmx/GetDate")
                .tag(this)
                .params("method", "getUserAndGpsInfoByIDsUtcNew")
                .params("school_id", id)
                .params("custid", id)
                .params("userIDs", device.id)
                .params("mapType", "BAIDU")
                .params("option", "cn")
                .params("mds", mds)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            JSONArray arydata = jsonObject.getJSONArray("data");
                            for (int i = 0; i < arydata.length(); i++) {
                                JSONObject datajson = arydata.getJSONObject(i);
                                JSONObject keyjson = datajson.getJSONObject("key");
                                int sys_time = keyjson.getInt("sys_time");
                                int user_name = keyjson.getInt("user_name");
                                int jingdu = keyjson.getInt("jingdu");
                                int weidu = keyjson.getInt("weidu");
                                int ljingdu = keyjson.getInt("ljingdu");
                                int lweidu = keyjson.getInt("lweidu");
                                int datetime = keyjson.getInt("datetime");
                                int heart_time = keyjson.getInt("heart_time");
                                int su = keyjson.getInt("su");
                                int status = keyjson.getInt("status");
                                int hangxiang = keyjson.getInt("hangxiang");
                                int sim_id = keyjson.getInt("sim_id");
                                int user_id = keyjson.getInt("user_id");
                                int sale_type = keyjson.getInt("sale_type");
                                int iconType = keyjson.getInt("iconType");
                                int server_time = keyjson.getInt("server_time");
                                int product_type = keyjson.getInt("product_type");
                                int expire_date = keyjson.getInt("expire_date");
                                int group_id = keyjson.getInt("group_id");
                                int statenumber = keyjson.getInt("statenumber");
                                int eletric = keyjson.getInt("electric");
                                JSONArray aryrecord = datajson.getJSONArray("records");
                                for (int j = 0; j < aryrecord.length(); j++) {
                                    JSONArray aryrecords = aryrecord.getJSONArray(j);
                                    long sys_timestr = aryrecords.getLong(sys_time);
                                    String user_namestr = aryrecords.getString(user_name);
                                    double jingdustr = aryrecords.getDouble(jingdu);
                                    double weidustr = aryrecords.getDouble(weidu);
                                    double ljingdustr = aryrecords.getDouble(ljingdu);
                                    double lweidustr = aryrecords.getDouble(lweidu);
                                    long datetimestr = aryrecords.getLong(datetime);
                                    long heart_timestr = aryrecords.getLong(heart_time);
                                    int sustr = aryrecords.getInt(su);
                                    String statusstr = aryrecords.getString(status);
                                    int hangxiangstr = aryrecords.getInt(hangxiang);
                                    String sim_idstr = aryrecords.getString(sim_id);
                                    String user_idstr = aryrecords.getString(user_id);
                                    String sale_typestr = aryrecords.getString(sale_type);
                                    String iconTypestr = aryrecords.getString(iconType);
                                    long server_timestr = aryrecords.getLong(server_time);
                                    String product_typestr = aryrecords.getString(product_type);
                                    long expire_datestr = aryrecords.getLong(expire_date);
                                    String group_idstr = aryrecords.getString(group_id);
                                    String statusnumberstr = aryrecords.getString(statenumber);
                                    double eletricstr = aryrecords.getDouble(eletric);
                                    LocalEntity localEntity = new LocalEntity(sys_timestr, user_namestr, jingdustr, weidustr, ljingdustr
                                            , lweidustr, datetimestr, heart_timestr, sustr, hangxiangstr, sim_idstr, user_idstr, iconTypestr,
                                            sale_typestr, statusstr, server_timestr, product_typestr, expire_datestr, group_idstr,
                                            statusnumberstr, eletricstr);
                                    localEntities.add(localEntity);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setMarker();
                    }
                });
    }

    private void setMarker() {
        LocalEntity localEntity = null;
        for (int i = 0; i < localEntities.size(); i++) {
            if (localEntities.get(i).user_name.equals(device.fullname)) {
                localEntity = localEntities.get(i);
                break;
            }
        }

        //定义Maker坐标点
        LatLng point = new LatLng(localEntity.weidu, localEntity.jingdu);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.map_annotation_image);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(16)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        setFence(localEntity);
    }

    private void setFence(LocalEntity localEntity) {

        // 请求标识
        int tag = 3;
        // 轨迹服务ID
        long serviceId = 137150;
        LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
        // 围栏名称
        String fenceName = "local_circle";
        // 监控对象
        String monitoredPerson = "myTrace";
        // 围栏圆心
        com.baidu.trace.model.LatLng center = new com.baidu.trace.model.LatLng(localEntity.weidu, localEntity.jingdu);
        // 围栏半径（单位 : 米）
        double radius = 800;
        // 去噪精度
        int denoise = 200;
        // 坐标类型
        CoordType coordType = CoordType.bd09ll;


        // 创建本地圆形围栏请求实例
        CreateFenceRequest localCircleFenceRequest = CreateFenceRequest.buildLocalCircleRequest(tag, serviceId, fenceName, monitoredPerson, center, radius, denoise, coordType);
        LatLng circleCenter = new LatLng(localEntity.weidu, localEntity.jingdu);
        OverlayOptions overlayOptions = new CircleOptions().fillColor(0x000000FF).center(circleCenter)
                .stroke(new Stroke(5, Color.rgb(0x23, 0x19, 0xDC))).radius((int) radius);
        tempOverlays.put(tag, mBaiduMap.addOverlay(overlayOptions));
        // 初始化围栏监听器
        OnFenceListener mFenceListener = new OnFenceListener() {
            // 创建围栏回调
            @Override
            public void onCreateFenceCallback(CreateFenceResponse response) {
            }

            // 更新围栏回调
            @Override
            public void onUpdateFenceCallback(UpdateFenceResponse response) {
            }

            // 删除围栏回调
            @Override
            public void onDeleteFenceCallback(DeleteFenceResponse response) {
            }

            // 围栏列表回调
            @Override
            public void onFenceListCallback(FenceListResponse response) {
            }

            // 监控状态回调
            @Override
            public void onMonitoredStatusCallback(MonitoredStatusResponse
                                                          response) {
            }

            // 指定位置监控状态回调
            @Override
            public void onMonitoredStatusByLocationCallback(MonitoredStatusByLocationResponse response) {
            }

            // 历史报警回调
            @Override
            public void onHistoryAlarmCallback(HistoryAlarmResponse response) {
            }
        };
        // 创建本地圆形围栏
        mTraceClient.createFence(localCircleFenceRequest, mFenceListener);
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.backIv:
                onBackPressed();
                break;
        }
    }
}
