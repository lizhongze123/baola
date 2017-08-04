package com.XMBT.bluetooth.le;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.ui.device.AddYuncheActivity;
import com.XMBT.bluetooth.le.ui.misc.LoginActivity;
import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.stx.xhb.xbanner.XBanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class YunCheListActivity extends Activity implements XBanner.XBannerAdapter {
    private XBanner xBanner;
    private ListView listView;
    private List<Integer> imgurls = new ArrayList<>();
    private List<YunCheDeviceEntity> yunCheDeviceEntities = new ArrayList<>();
    private MyListAdapter adapter;
    private SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_yun_che_list);
        initView();
        getDevice();
    }

    private void initView() {
        xBanner = (XBanner) findViewById(R.id.xbanner);
        listView = (ListView) findViewById(R.id.listView);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        imgurls.add(R.drawable.banner_one);
        imgurls.add(R.drawable.banner_three);
        xBanner.setData(imgurls, null);
        xBanner.setmAdapter(this);
        adapter = new MyListAdapter(yunCheDeviceEntities);
        listView.setAdapter(adapter);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDevice();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(YunCheListActivity.this, YunCheActivity.class);
                intent.putExtra("device", yunCheDeviceEntities.get(position));
                startActivity(intent);
            }
        });
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.backIv:
                onBackPressed();
                break;
            case R.id.addBtn:
                Intent intent = new Intent(YunCheListActivity.this, AddYuncheActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void loadBanner(XBanner banner, View view, int position) {
        Glide.with(YunCheListActivity.this).load(imgurls.get(position)).into((ImageView) view);
    }

    private void getDevice() {
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String mds = sp.getString("mds", null);
        OkGo.get(GlobalConsts.URL + "GetDateServices.asmx/GetDate")
                .tag(this)
                .params("method", "GetEquipmentList")
                .params("mds", mds)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            String success = jsonObject.getString("success");
                            String code = jsonObject.getString("errorCode");
                            if (success.equals("false") && code.equals("403")) {
                                Toast.makeText(YunCheListActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(YunCheListActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                yunCheDeviceEntities.clear();
                                JSONArray arydata = jsonObject.getJSONArray("data");
                                for (int i = 0; i < arydata.length(); i++) {
                                    JSONObject datajson = arydata.getJSONObject(i);
                                    String id = datajson.getString("Id");
                                    String fullName = datajson.getString("FullName");
                                    String macid = datajson.getString("Macid");
                                    String plateNumber = datajson.getString("PlateNumber");
                                    int equipmentStatus = datajson.getInt("EquipmentStatus");
                                    int equipmentBatteryVType = datajson.getInt("EquipmentBetteryVType");
                                    YunCheDeviceEntity device = new YunCheDeviceEntity();
                                    device.setFullname(fullName);
                                    device.setId(id);
                                    device.setEquipmentBatteryVType(equipmentBatteryVType);
                                    device.setMacid(macid);
                                    device.setEquipmentStatus(equipmentStatus);
                                    device.setPlatenumber(plateNumber);
                                    if (i == 0) {
                                        device.setImg(R.drawable.battery_automobile);
                                    } else if (i == 1) {
                                        device.setImg(R.drawable.battery_electric);
                                    }
                                    yunCheDeviceEntities.add(device);
                                }
                                adapter.notifyDataSetChanged();
                                swipe.setRefreshing(false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    class MyListAdapter extends BaseAdapter {
        private List<YunCheDeviceEntity> devices;

        public MyListAdapter(List<YunCheDeviceEntity> devices) {
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = View.inflate(YunCheListActivity.this, R.layout.yunche_list, null);
            ImageView picIv = (ImageView) convertView.findViewById(R.id.picIv);
            TextView titleTv = (TextView) convertView.findViewById(R.id.titleTv);
            TextView statusTv = (TextView) convertView.findViewById(R.id.statusTv);
            if (devices.get(position).getEquipmentBatteryVType() == 1) {
                picIv.setImageResource(R.drawable.battery_automobile);
            } else {
                picIv.setImageResource(R.drawable.battery_electric);
            }
            titleTv.setText(devices.get(position).getFullname());
            if (devices.get(position).getEquipmentStatus() == 0) {
                statusTv.setText("未启用");
            } else if (devices.get(position).getEquipmentStatus() == 1) {
                statusTv.setText("离线");
            } else if (devices.get(position).getEquipmentStatus() == 2) {
                statusTv.setText("在线");
            }
            return convertView;
        }
    }
}
