package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.XMBT.bluetooth.le.view.dialog.SetDialog;
import com.XMBT.bluetooth.le.R;


public class RefreshActivity extends BaseActivity {

    private TextView tvHome, tvLocation;
    private SetDialog setDialog;
    private int timeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initView();
    }

    private void initView() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setDialog = new SetDialog(this);
        findViewById(R.id.ll_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType = 0;
                setDialog.show();
                setDialog.setEtText("30");
                setDialog.setHint("建议设置刷新时间30秒以上");
            }
        });
        findViewById(R.id.ll_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType = 1;
                setDialog.show();
                setDialog.setEtText("5");
                setDialog.setHint("建议设置刷新时间5秒以上");
            }
        });
        setDialog.setOnPositiveListener(new SetDialog.OnPositiveListener() {
            @Override
            public void onPositive(String str) {
                if(TextUtils.isEmpty(str)){
                    showToast("请输入刷新时间");

                }else if(str.substring(0).equals("0")){
                    showToast("请输入正确的时间");
                }else {
                    setTime(str);
                    setDialog.dismiss();
                }
            }
        });
        tvHome = (TextView) findViewById(R.id.tv_home);
        tvLocation = (TextView) findViewById(R.id.tv_location);
    }

    private void setTime(String second) {
        if(timeType == 0){
            tvHome.setText(second);
        }else{
            tvLocation.setText(second);
        }
        UserSp.getInstance(this).setRefreshTime(GlobalConsts.userName, second);
    }

}
