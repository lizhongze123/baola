package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.LocalEntity;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 报警中心
 */
public class WarnCenterActivity extends BaseActivity {

    private ExpandableListView listView;
    private MyListAdapter adapter;
    private YunCheDeviceEntity device;

    private String dataString = "10,9,8,29,3";
    private String[] groupInt = new String[]{"10", "9", "8", "29", "3"};
    private String[] groupStrings = new String[]
            {"位移报警", "震动报警", "低电报警", "高压报警", "围栏报警"};
    private String[][] childStrings = new String[][]
            {
                    {"暂无报警信息"},
                    {"暂无报警信息"},
                    {"暂无报警信息"},
                    {"暂无报警信息"},
                    {"暂无报警信息"}
            };

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
        getData();
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

    public void getData() {
        showLoadingDialog(null);
        device = (YunCheDeviceEntity) getIntent().getSerializableExtra(DeviceFragment.DATA_DEVICE);
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
                for (int i = 0; i < groupInt.length; i++) {
                    childStrings[i][0] = datajson.getString(groupInt[i]);
                }
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
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildViewHolder childViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(WarnCenterActivity.this).inflate(R.layout.item_expand_child, parent, false);
                childViewHolder = new ChildViewHolder();
                childViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.label_expand_child);
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
        }

        class ChildViewHolder {
            TextView tvTitle;
        }
    }

}
