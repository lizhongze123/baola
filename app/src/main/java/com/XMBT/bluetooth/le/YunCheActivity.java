package com.XMBT.bluetooth.le;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.XMBT.bluetooth.le.bean.YuCheEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.map.BaiduMapActivity;
import com.XMBT.bluetooth.le.map.FenceActivity;
import com.XMBT.bluetooth.le.map.TraceActivity;
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

public class YunCheActivity extends Activity implements XBanner.XBannerAdapter{
    private GridView gridView;
    private XBanner xBanner;
    private List<Integer> imgurls=new ArrayList<>();
    YunCheDeviceEntity device;
    private TextView voltageTv,dayTv,persentTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yun_che);
        initView();
        Intent intent=getIntent();
        device= (YunCheDeviceEntity) intent.getSerializableExtra("device");
        getVoltage();
    }

    private void initView() {
        voltageTv= (TextView) findViewById(R.id.voltageTv);
        dayTv= (TextView) findViewById(R.id.dayTv);
        persentTv= (TextView) findViewById(R.id.persentTv);
        xBanner= (XBanner) findViewById(R.id.xbanner);
        gridView= (GridView) findViewById(R.id.gridView);
        List<YuCheEntity> yuCheEntities=new ArrayList<>();
        YuCheEntity yuCheEntity=new YuCheEntity();
        yuCheEntity.setPic(R.drawable.main_location);
        yuCheEntity.setTitle("车辆位置");
        yuCheEntities.add(yuCheEntity);
        YuCheEntity yuCheEntity1=new YuCheEntity();
        yuCheEntity1.setTitle("历史轨迹");
        yuCheEntity1.setPic(R.drawable.main_track);
        yuCheEntities.add(yuCheEntity1);
        YuCheEntity yuCheEntity2=new YuCheEntity();
        yuCheEntity2.setTitle("设防控制");
        yuCheEntity2.setPic(R.drawable.main_control);
        yuCheEntities.add(yuCheEntity2);
        YuCheEntity yuCheEntity3=new YuCheEntity();
        yuCheEntity3.setTitle("报警中心");
        yuCheEntity3.setPic(R.drawable.main_alarmcenter);
        yuCheEntities.add(yuCheEntity3);
        YuCheEntity yuCheEntity4=new YuCheEntity();
        yuCheEntity4.setTitle("电子围栏");
        yuCheEntity4.setPic(R.drawable.main_aroundnavi);
        yuCheEntities.add(yuCheEntity4);
        YuCheEntity yuCheEntity5=new YuCheEntity();
        yuCheEntity5.setTitle("云车社区");
        yuCheEntity5.setPic(R.drawable.main_aroundinfo);
        yuCheEntities.add(yuCheEntity5);
        GridAdapter adapter=new GridAdapter(yuCheEntities);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    Intent intent=new Intent(YunCheActivity.this,BaiduMapActivity.class);
                    intent.putExtra("device",device);
                    startActivity(intent);
                }else if(position==1){
                    Intent intent=new Intent(YunCheActivity.this,TraceActivity.class);
                    intent.putExtra("device",device);
                    startActivity(intent);
                }else if(position==2){
                    Intent intent=new Intent(YunCheActivity.this,FortificationActivity.class);
                    startActivity(intent);
                }else if(position==3){
                    Intent intent=new Intent(YunCheActivity.this,WarnCenterActivity.class);
                    startActivity(intent);
                }else if(position==4){
                    Intent intent=new Intent(YunCheActivity.this,FenceActivity.class);
                    intent.putExtra("device",device);
                    startActivity(intent);
                }
            }
        });
        imgurls.add(R.drawable.banner_one);
        imgurls.add(R.drawable.banner_three);
        xBanner.setData(imgurls,null);
        xBanner.setmAdapter(this);
    }

    private void getVoltage() {
        SharedPreferences sp=getSharedPreferences("userInfo",MODE_PRIVATE);
        String mds=sp.getString("mds",null);
        OkGo.post(GlobalConsts.URL+"GetDateServices.asmx/GetDate")
                .tag(this)
                .params("method","GetPowerVAndBetteryV")
                .params("macid",device.getId())
                .params("mds",mds)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            JSONObject jsonObject=new JSONObject(s);
                            String errorCode=jsonObject.getString("errorCode");
                            if(errorCode.equals("200")){
                                JSONArray dataary=jsonObject.getJSONArray("data");
                                for (int i=0;i<dataary.length();i++){
                                    JSONObject datajson=dataary.getJSONObject(i);
                                    double ServiceTime=datajson.getDouble("ServiceTime");
                                    if(ServiceTime==-1000){
                                        Toast.makeText(YunCheActivity.this,"设备未启用",Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    double power=datajson.getDouble("PowerV");
                                    double RemnanLife=datajson.getDouble("RemnantLife");
                                    double Bettery=datajson.getDouble("Bettery");
                                    voltageTv.setText(String.valueOf(power)+"V");
                                    dayTv.setText(String.valueOf(Bettery)+"天");
                                    persentTv.setText(String.valueOf(RemnanLife)+"%");
                                }
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

    public void doClick(View view){
        switch (view.getId()){
            case R.id.backIv:
                onBackPressed();
                break;
        }
    }

    class GridAdapter extends BaseAdapter{
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
            convertView=View.inflate(YunCheActivity.this,R.layout.yunche_list_item,null);
            ImageView picIv= (ImageView) convertView.findViewById(R.id.picIv);
            TextView titleTv= (TextView) convertView.findViewById(R.id.titleTv);
            Glide.with(YunCheActivity.this).load(yuCheEntities.get(position).getPic()).into(picIv);
            titleTv.setText(yuCheEntities.get(position).getTitle());
            return convertView;
        }
    }
}
