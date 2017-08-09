package com.XMBT.bluetooth.le.map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Toast;

import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.bean.LocalEntity;
import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.view.ZoomControlView;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
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

import okhttp3.Call;
import okhttp3.Response;

public class BaiduMapActivity extends Activity {
    MapView mMapView = null;
    YunCheDeviceEntity device;
    public static List<Activity> activityList = new LinkedList<Activity>();
    List<LocalEntity> localEntities = new ArrayList<LocalEntity>();
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    BaiduMap mBaiduMap;
    ZoomControlView zoomControlView;
    private CheckBox checkbox1, checkbox2, checkbox3;
    private String mSDCardPath = null;
    private static final String[] authBaseArr = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int authBaseRequestCode = 1;
    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";
    private static final String[] authComArr = {Manifest.permission.READ_PHONE_STATE};
    private static final int authComRequestCode = 2;
    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        activityList.add(this);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_baidu_map);
        initView();
        if (initDirs()) {
            initNavi();
        }
        getLocate();
    }

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
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initSetting() {
        // BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, "9454455");
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    private boolean hasCompletePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

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
                        Toast.makeText(BaiduMapActivity.this, authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void initSuccess() {
                Toast.makeText(BaiduMapActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
                Toast.makeText(BaiduMapActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(BaiduMapActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }

        }, null, ttsHandler, ttsPlayStateListener);

    }

    private BNRoutePlanNode.CoordinateType mCoordinateType = null;

    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        mCoordinateType = coType;
        if (!hasInitSuccess) {
            Toast.makeText(BaiduMapActivity.this, "还未初始化!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(BaiduMapActivity.this, "没有完备的权限!", Toast.LENGTH_SHORT).show();
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
                sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.40386525193937, 39.915160800132085, "北京天安门", null, coType);
                break;
            }
            default:
                ;
        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);
            BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
        }
    }

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
            Intent intent = new Intent(BaiduMapActivity.this, BNDemoGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            // TODO Auto-generated method stub
            Toast.makeText(BaiduMapActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        checkbox1 = (CheckBox) findViewById(R.id.checkbox1);
        checkbox2 = (CheckBox) findViewById(R.id.checkbox2);
        checkbox3 = (CheckBox) findViewById(R.id.checkbox3);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        zoomControlView = (ZoomControlView) findViewById(R.id.zoomControlView);
        zoomControlView.setMapView(mMapView);
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
            if (localEntities.get(i).getUser_name().equals(device.fullname)) {
                localEntity = localEntities.get(i);
                break;
            }
        }

        //定义Maker坐标点
        LatLng point = new LatLng(localEntity.getWeidu(), localEntity.getJingdu());
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
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.backIv:
                onBackPressed();
                break;
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
                if (BaiduNaviManager.isNaviInited()) {
                    routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09LL);
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
            // TODO Auto-generated method stub
            Log.e("test_TTS", "stopTTS");
        }

        @Override
        public void resumeTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "resumeTTS");
        }

        @Override
        public void releaseTTSPlayer() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "releaseTTSPlayer");
        }

        @Override
        public int playTTSText(String speech, int bPreempt) {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

            return 1;
        }

        @Override
        public void phoneHangUp() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "phoneHangUp");
        }

        @Override
        public void phoneCalling() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "phoneCalling");
        }

        @Override
        public void pauseTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "pauseTTS");
        }

        @Override
        public void initTTSPlayer() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "initTTSPlayer");
        }

        @Override
        public int getTTSState() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "getTTSState");
            return 1;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
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
}


