package com.XMBT.bluetooth.le.ui.gbattery;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;

public class WarnCenterActivity extends Activity {
    private ExpandableListView listView;
    private MyListAdapter adapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_warn_center);
        initView();
    }

    private void initView() {
        listView = (ExpandableListView) findViewById(R.id.expand);
        listView.setGroupIndicator(null);//将控件默认的左边箭头去掉，
        adapter = new MyListAdapter();
        listView.setAdapter(adapter);
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

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.backIv:
                onBackPressed();
                break;
        }
    }
}
