package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 报警中心
 * 进来先取报警数量
 * 点击后取报警
 */
public class WarnCenterActivity extends BaseActivity {

    private ExpandableListView listView;
    private MyListAdapter adapter;
    private YunCheDeviceEntity device;

    private String dataString = "10,9,8,29,3";
    private String[] groupInt = new String[]{"10","3"};
    private String[] groupStrings = new String[]
            {"位移报警", "围栏报警"};
    private String[][] childStrings = new String[][]
            {
                    {"暂无报警信息"},
                    {"暂无报警信息"}
            };



    private Map<String, String> alarmCount = new HashMap();
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warn_center);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initViews();
        getAlarmCount();
    }

    private void initViews() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        listView = (ExpandableListView) findViewById(R.id.expand);
        listView.setGroupIndicator(null);//将控件默认的左边箭头去掉，
        adapter = new MyListAdapter();
        listView.setAdapter(adapter);
    }

    /**取报警数量*/
    public void getAlarmCount() {
        showLoadingDialog(null);
        device = (YunCheDeviceEntity) getIntent().getSerializableExtra(DeviceActivity.DATA_DEVICE);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.get(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "getAlarmCount")
                .params("macid", device.macid)
                .params("classify", dataString)
                .params("mds", mds)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        LogUtils.d(s);
                        parseJson(s);
                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        dismissLoadingDialog();
                    }
                });
    }

    /**
     * 取报警信息
     */
    public void getDetailData(String type) {
        showLoadingDialog(null);
        device = (YunCheDeviceEntity) getIntent().getSerializableExtra(DeviceActivity.DATA_DEVICE);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.get(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "GetAlarmList")
                .params("macid", device.macid)
                .params("classify", type)
                .params("mapType", "BAIDU")
                .params("mds", mds)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        LogUtils.d(s);
                        parseJson(s);
                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        dismissLoadingDialog();
                    }
                });
    }

    private void parseJson(String json) {
        /**
         {"success":"true","row":{"29":"0","8":"0","9":"0","10":"0","3":"0"}}
         */
        try {
            JSONObject jsonObject = new JSONObject(json);
            String success = jsonObject.getString("success");
            if (success.equals("false")) {
                String msg = jsonObject.getString("msg");
                showToast(msg);
            } else {
                JSONObject datajson  = jsonObject.getJSONObject("row");
                alarmCount.put("10",datajson.getString("10"));//位移报警
                alarmCount.put("3",datajson.getString("3"));//围栏报警
                mHandler.sendEmptyMessage(0);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class MyListAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return groupStrings.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childStrings[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupStrings[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childStrings[groupPosition][childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupViewHolder groupViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(WarnCenterActivity.this).inflate(R.layout.item_expand_group, parent, false);
                groupViewHolder = new GroupViewHolder();
                groupViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.label_expand_group);
                groupViewHolder.tvCount = (TextView) convertView.findViewById(R.id.iv_count);
                groupViewHolder.parentImageViw = (ImageView) convertView.findViewById(R.id.arrowIv);
                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) convertView.getTag();
            }
            groupViewHolder.tvTitle.setText(groupStrings[groupPosition]);
            //判断isExpanded就可以控制是按下还是关闭，同时更换图片
            if (isExpanded) {
                groupViewHolder.parentImageViw.setBackgroundResource(R.drawable.arrow_bottom);
            } else {
                groupViewHolder.parentImageViw.setBackgroundResource(R.drawable.arrow_right);
            }

            if(!alarmCount.get(groupInt[groupPosition]).equals(0)){
                groupViewHolder.tvCount.setText(alarmCount.get(groupInt[groupPosition]));
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildViewHolder childViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(WarnCenterActivity.this).inflate(R.layout.item_expand_child, parent, false);
                childViewHolder = new ChildViewHolder();
                childViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_info);
                convertView.setTag(childViewHolder);
            } else {
                childViewHolder = (ChildViewHolder) convertView.getTag();
            }
            childViewHolder.tvTitle.setText(childStrings[groupPosition][childPosition]);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        class GroupViewHolder {
            TextView tvTitle;
            ImageView parentImageViw;
            TextView tvCount;
        }

        class ChildViewHolder {
            TextView tvTitle;
        }
    }

}
