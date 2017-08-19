package com.XMBT.bluetooth.le.ui.gbattery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.YuCheEntity;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.map.BaiduMapActivity;
import com.XMBT.bluetooth.le.map.TraceActivity;
import com.XMBT.bluetooth.le.map.FenceActivity;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.GridViewForNested;
import com.XMBT.bluetooth.le.view.TitleBar;
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

public class YunCheActivity extends BaseActivity implements XBanner.XBannerAdapter {

    private ScrollView scrollView;
    private GridViewForNested gridView;
    private XBanner xBanner;
    private List<Integer> imgurls = new ArrayList<>();
    private YunCheDeviceEntity device;
    private TextView voltageTv, dayTv, persentTv;
    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yun_che);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, 0);
        initViews();
        Intent intent = getIntent();
        device = (YunCheDeviceEntity) intent.getSerializableExtra(DeviceFragment.DATA_DEVICE);
        getVoltage();
    }

    private void initViews() {
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        voltageTv = (TextView) findViewById(R.id.voltageTv);
        dayTv = (TextView) findViewById(R.id.dayTv);
        persentTv = (TextView) findViewById(R.id.persentTv);
        xBanner = (XBanner) findViewById(R.id.xbanner);
        gridView = (GridViewForNested) findViewById(R.id.gridView);

        List<YuCheEntity> yuCheEntities = new ArrayList<>();
        TypedArray icons = getResources().obtainTypedArray(R.array.gas_icons);
        String[] items = getResources().getStringArray(R.array.gps_items);
        for (int i = 0; i < items.length; i++) {
            YuCheEntity yuCheEntity = new YuCheEntity();
            yuCheEntity.setTitle(items[i]);
            yuCheEntity.setPic(icons.getResourceId(i , 0));
            yuCheEntities.add(yuCheEntity);
        }

        GridAdapter adapter = new GridAdapter(yuCheEntities);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Intent intent = new Intent(YunCheActivity.this, BaiduMapActivity.class);
                    intent.putExtra(DeviceFragment.DATA_DEVICE, device);
                    startActivity(intent);
                } else if (position == 1) {
                    Intent intent = new Intent(YunCheActivity.this, TraceActivity.class);
                    intent.putExtra(DeviceFragment.DATA_DEVICE, device);
                    startActivity(intent);
                } else if (position == 2) {
                    Intent intent = new Intent(YunCheActivity.this, FortificationActivity.class);
                    intent.putExtra(DeviceFragment.DATA_DEVICE, device);
                    startActivity(intent);
                } else if (position == 3) {
                    Intent intent = new Intent(YunCheActivity.this, WarnCenterActivity.class);
                    intent.putExtra(DeviceFragment.DATA_DEVICE, device);
                    startActivity(intent);
                } else if (position == 4) {
                    Intent intent = new Intent(YunCheActivity.this, FenceActivity.class);
                    intent.putExtra(DeviceFragment.DATA_DEVICE, device);
                    startActivity(intent);
                } else if (position == 5) {
                    Intent intent = new Intent(YunCheActivity.this, ForumActivity.class);
                    startActivity(intent);
                }
            }
        });
        imgurls.add(R.drawable.banner_one);
        imgurls.add(R.drawable.banner_three);
        xBanner.setData(imgurls, null);
        xBanner.setmAdapter(this);
    }

    /**
     * 获取电池电压及使用时间
     */
    private void getVoltage() {
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.get(GlobalConsts.URL + "GetDateServices.asmx/GetDate")
                .tag(this)
                .params("method", "GetPowerVAndBetteryV")
                .params("macid", device.macid)
                .params("mds", mds)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            LogUtils.d(s);
                            JSONObject jsonObject = new JSONObject(s);
                            String errorCode = jsonObject.getString("errorCode");
                            if (errorCode.equals("200")) {
                                JSONArray dataary = jsonObject.getJSONArray("data");
                                for (int i = 0; i < dataary.length(); i++) {
                                    JSONObject datajson = dataary.getJSONObject(i);
                                    double ServiceTime = datajson.getDouble("ServiceTime");
                                    if (ServiceTime == -1000) {
                                        showToast("设备未启用");
                                        return;
                                    }
                                    double power = datajson.getDouble("PowerV");
                                    double RemnanLife = datajson.getDouble("RemnantLife");
                                    int Bettery = (int) datajson.getDouble("Bettery");
                                    voltageTv.setText(String.valueOf(power) + "V");
                                    dayTv.setText(String.valueOf(Bettery) + "天");
                                    persentTv.setText(String.valueOf(RemnanLife) + "%");
                                }
                            }else{
                                showToast("没有此设备相关信息");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void loadBanner(XBanner banner, View view, int position) {
        Glide.with(YunCheActivity.this).load(imgurls.get(position)).into((ImageView) view);
    }

    class GridAdapter extends BaseAdapter {
        private List<YuCheEntity> yuCheEntities;

        public GridAdapter(List<YuCheEntity> yuCheEntities) {
            this.yuCheEntities = yuCheEntities;
        }

        @Override
        public int getCount() {
            return yuCheEntities.size();
        }

        @Override
        public Object getItem(int position) {
            return yuCheEntities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = View.inflate(YunCheActivity.this, R.layout.yunche_list_item, null);
            ImageView picIv = (ImageView) convertView.findViewById(R.id.picIv);
            TextView titleTv = (TextView) convertView.findViewById(R.id.titleTv);
            Glide.with(YunCheActivity.this).load(yuCheEntities.get(position).getPic()).into(picIv);
            titleTv.setText(yuCheEntities.get(position).getTitle());
            return convertView;
        }
    }
}
