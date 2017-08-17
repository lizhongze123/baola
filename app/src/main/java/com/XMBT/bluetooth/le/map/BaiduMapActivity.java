package com.XMBT.bluetooth.le.map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.LocalEntity;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.DateFormatUtils;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.XMBT.bluetooth.le.view.ZoomControlView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

public class BaiduMapActivity extends BaseActivity implements OnGetGeoCoderResultListener {

    private String APPID;
    MapView mMapView = null;
    BaiduMap mBaiduMap;
    ZoomControlView zoomControlView;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private CheckBox checkbox1, checkbox2, checkbox3;
    private TextView tvLocation;

    private YunCheDeviceEntity device;
    public static List<Activity> activityList = new LinkedList<>();

    /**
     * 保存车辆的位置
     */
    private List<LocalEntity> localEntities = new ArrayList<>();
    /**
     * 当前位置
     */
    private BDLocation mLocation;

    public static final String ROUTE_PLAN_NODE = "routePlanNode";

    private String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "BAOLIAOSIMPLE";


    private static final String[] authBaseArr = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int authBaseRequestCode = 1;
    private static final int authComRequestCode = 2;
    private static final String[] authComArr = {Manifest.permission.READ_PHONE_STATE};
    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityList.add(this);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        setContentView(R.layout.activity_baidu_map);
        initView();
        initMap();
        if (initDirs()) {
            initNavi();
        }
    }

    private void initView() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        checkbox1 = (CheckBox) findViewById(R.id.checkbox1);
        checkbox2 = (CheckBox) findViewById(R.id.checkbox2);
        checkbox3 = (CheckBox) findViewById(R.id.checkbox3);
        tvLocation = (TextView) findViewById(R.id.tv_location);
    }

    private void initMap() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        //这里显示自定义的缩放控件
        zoomControlView = (ZoomControlView) findViewById(R.id.zoomControlView);
        zoomControlView.setMapView(mMapView);
        mMapView.showZoomControls(false); //设置是否显示缩放控件
        mMapView.showScaleControl(false); //设置是否显示比例尺
        mMapView.removeViewAt(1); //去掉百度logo
        mBaiduMap = mMapView.getMap();

        mLocationClient = new LocationClient(getApplicationContext()); //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        mLocationClient.setLocOption(setLocOption());


        timer = new Timer();
        int span = Integer.valueOf(UserSp.getInstance(this).getRefreshTime(GlobalConsts.userName)) * 1000;
        timer.schedule(task, 1000, span);
    }

    /**
     * 获取车辆位置定时器
     */
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            getLocate();
        }
    };

    private LocationClientOption setLocOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setNeedDeviceDirect(true); //返回的定位结果包含手机机头方向
        mLocationClient.setLocOption(option);
        mLocationClient.requestLocation();//发送请求
        mLocationClient.start();//启动位置请求
        return option;
    }


    /**
     * 创建文件夹
     *
     * @return
     */
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 获取sd卡路径
     *
     * @return
     */
    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    String authinfo = null;

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    // showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    // showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
            // showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            // showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    private boolean hasBasePhoneAuth() {

        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 导航设置管理器
     */
    private void initSetting() {
        /**
         * 日夜模式 1：自动模式 2：白天模式 3：夜间模式
         */
        // BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        /**
         * 设置全程路况显示
         */
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        /**
         * 设置语音播报模式
         */
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        /**
         * 设置省电模式
         */
        // BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        /**
         * 设置实时路况条
         */
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        Bundle bundle = new Bundle();
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            APPID = appInfo.metaData.getInt("com.baidu.tts.APP_ID") + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, APPID);
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    private boolean hasCompletePhoneAuth() {
        PackageManager pm = this.getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 初始化百度地图导航
     */
    private void initNavi() {
        BNOuterTTSPlayerCallback ttsCallback = null;
        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;
            }
        }

        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + msg;
                }
                BaiduMapActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showToast(authinfo);
                    }
                });
            }

            public void initSuccess() {
                showToast("百度导航引擎初始化成功");
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
                showToast("百度导航引擎初始化开始");
            }

            public void initFailed() {
                showToast("百度导航引擎初始化失败");
            }

        }, null, ttsHandler, ttsPlayStateListener);

    }

    private BNRoutePlanNode.CoordinateType mCoordinateType = BNRoutePlanNode.CoordinateType.BD09LL;

    /**
     * 算路设置起、终点，算路偏好，是否模拟导航等参数，然后在回调函数中设置跳转至诱导。
     *
     * @param coType
     */
    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        mCoordinateType = coType;
        if (!hasInitSuccess) {
            showToast("还未初始化");
        }
        // 权限申请
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    this.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
                    showToast("没有完备的权限");
                }
            }

        }
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        switch (coType) {
            case GCJ02: {
                sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
                break;
            }
            case WGS84: {
                sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
                break;
            }
            case BD09_MC: {
                sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
                break;
            }
            case BD09LL: {
                if (localEntities.size() > 0 && mLocation != null) {
                    sNode = new BNRoutePlanNode(mLocation.getLongitude(), mLocation.getLatitude(), "", null, coType);
                    eNode = new BNRoutePlanNode(localEntities.get(0).jingdu, localEntities.get(0).weidu, "", null, coType);
                }
                break;
            }
            default:
        }
        if (sNode != null && eNode != null) {
            //传入的算路节点，顺序是起点、途经点、终点，其中途经点最多三个
            List<BNRoutePlanNode> list = new ArrayList<>();
            list.add(sNode);
            list.add(eNode);
            /**
             * 发起算路操作并在算路成功后通过回调监听器进入导航过程,返回是否执行成功
             * @param preference 算路偏好 1:推荐 8:少收费 2:高速优先 4:少走高速 16:躲避拥堵
             *                   true表示真实GPS导航，false表示模拟导航
             *                   开始导航回调监听器，在该监听器里一般是进入导航过程页面
             */
            BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        //城市->坐标
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        //坐标->坐城市
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            showToast("抱歉，未能找到结果");
            return;
        }
        if (result.getAddress() != null) {
            tvLocation.setText(result.getAddress());
        }
    }

    /**
     * 导航回调监听器
     */
    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            /*
             * 设置途径点以及resetEndNode会回调该接口
             */

            for (Activity ac : activityList) {

                if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {

                    return;
                }
            }

            dismissLoadingDialog();

            /**
             * 导航activity
             */
            Intent intent = new Intent(BaiduMapActivity.this, BNDemoGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            showToast("算路失败");
        }
    }

    /**
     * 获取车辆位置
     */
    private void getLocate() {
        showLoadingDialog(null);
        Intent intent = getIntent();
        device = (YunCheDeviceEntity) intent.getSerializableExtra("device");

        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        String id = UserSp.getInstance(this).getId(GlobalConsts.userName);
        OkGo.get(GlobalConsts.GET_DATE)
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
                        LogUtils.i(s);
                        parseJson(s);
                        setMarker();
                        reverseGeoCodeOption();
                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        super.onAfter(s, e);
                        dismissLoadingDialog();
                    }
                });
    }

    private void parseJson(String json) {
        /**
         "data": [
         {
         "key": {},
         "records": [[]],
         "groups": []
         }
         ]
         */
        try {
            JSONObject jsonObject = new JSONObject(json);
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
    }

    private void reverseGeoCodeOption() {
        LocalEntity localEntity = null;
        for (int i = 0; i < localEntities.size(); i++) {
            if (localEntities.get(i).user_name.equals(device.fullname)) {
                localEntity = localEntities.get(i);
                break;
            }
        }
        GeoCoder mSearch = null;
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        LatLng point = new LatLng(localEntity.weidu, localEntity.jingdu);
        // 反Geo搜索
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(point));
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
        mBaiduMap.clear();
        mBaiduMap.addOverlay(option);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.infowindow, null);
        TextView tvDevice = (TextView) view.findViewById(R.id.tv_device);
        TextView tvStatus = (TextView) view.findViewById(R.id.tv_status);
        TextView tvTime = (TextView) view.findViewById(R.id.tv_time);
        tvDevice.setText(localEntity.user_name);
        tvTime.setText("时间:" + DateFormatUtils.getDate(localEntity.sys_time, DateFormatUtils.FORMAT_YMDHM));
        if (localEntity.status.substring(0).equals("1")) {
            tvStatus.setText("状态:开");
        } else {
            tvStatus.setText("状态:关");
        }
        InfoWindow infoWindow = new InfoWindow(view, point, -75);
        mBaiduMap.showInfoWindow(infoWindow);

        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(16)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.checkbox1:
                if (checkbox1.isChecked()) {
                    //卫星地图
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                } else {
                    //普通地图
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
                break;
            case R.id.checkbox3:
                if (checkbox3.isChecked()) {
                    //开启交通图
                    mBaiduMap.setTrafficEnabled(true);
                } else {
                    //开启交通图
                    mBaiduMap.setTrafficEnabled(false);
                }
                break;
            case R.id.button:
                showLoadingDialog("加载中，请稍候");
                    /**
                      * 判断百度导航是否初始化
                      */
                if (BaiduNaviManager.isNaviInited()) {
                     // 添加起点、终点
                    routeplanToNavi(mCoordinateType);
                }
                break;
            case R.id.checkbox2:
                if (checkbox2.isChecked()) {

                } else {
                    SharedPreferences sp = getSharedPreferences("diaglog_toast", MODE_PRIVATE);
                    int flag = sp.getInt("flag", -1);
                    if (flag == 1) {
                        return;
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("提示")
                                .setMessage("关闭视角跟随时，重新定位后将不会再自动调整视角到当前车辆位置")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setNegativeButton("不再提示", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences sp = getSharedPreferences("diaglog_toast", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putInt("flag", 1);
                                        editor.commit();
                                    }
                                }).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mLocationClient.stop();
        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

        @Override
        public void stopTTS() {
            Log.e("test_TTS", "stopTTS");
        }

        @Override
        public void resumeTTS() {
            Log.e("test_TTS", "resumeTTS");
        }

        @Override
        public void releaseTTSPlayer() {
            Log.e("test_TTS", "releaseTTSPlayer");
        }

        @Override
        public int playTTSText(String speech, int bPreempt) {
            Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

            return 1;
        }

        @Override
        public void phoneHangUp() {
            Log.e("test_TTS", "phoneHangUp");
        }

        @Override
        public void phoneCalling() {
            Log.e("test_TTS", "phoneCalling");
        }

        @Override
        public void pauseTTS() {
            Log.e("test_TTS", "pauseTTS");
        }

        @Override
        public void initTTSPlayer() {
            Log.e("test_TTS", "initTTSPlayer");
        }

        @Override
        public int getTTSState() {
            Log.e("test_TTS", "getTTSState");
            return 1;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                } else {
                    Toast.makeText(BaiduMapActivity.this, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            initNavi();
        } else if (requestCode == authComRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                }
            }
            routeplanToNavi(mCoordinateType);
        }

    }

    class MyLocationListener implements BDLocationListener {

        /**
         * 接收位置的信息回调方法
         *
         * @param location
         */
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            showToast("定位成功" + location.getLatitude() + "," + location.getLongitude());
            mLocation = location;

        }

    }

}


