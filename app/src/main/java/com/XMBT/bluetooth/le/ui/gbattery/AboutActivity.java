package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.utils.VersionUtils;
import com.XMBT.bluetooth.le.view.TitleBar;


public class AboutActivity extends BaseActivity {

    private TextView tvVersion;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initView();
    }

    private void initView() {
        tvVersion = (TextView) findViewById(R.id.tv_version);
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.ll_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingDialog(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoadingDialog();
                        showToast("已经是最新版本");
                    }
                }, 2000);
            }
        });
        tvVersion.setText(VersionUtils.getAppVersionName(this));
    }

}
