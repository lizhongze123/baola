package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.http.ApiResultCallback;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.lzy.okgo.OkGo;

import java.util.List;

public class AddYuncheActivity extends BaseActivity {

    private EditText editText;
    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_yunche);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initView();
    }

    private void initView() {
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        editText = (EditText) findViewById(R.id.addEt);
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.addBtn:
                if (!GlobalConsts.isLogin) {
                    showToast("请先登录");
                } else {
                    if (!TextUtils.isEmpty(editText.getText().toString())) {
                        addDevice();
                    } else {
                        showToast("设备号不能为空");
                    }
                }
                break;
        }
    }

    private void addDevice() {
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        String id = UserSp.getInstance(this).getId(GlobalConsts.userName);
        showLoadingDialog("加载中，请稍候");
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "MoveEquipment")
                .params("mds", mds)
                .params("macid", editText.getText().toString())
                .params("id", id)
                .execute(new ApiResultCallback<List<String>>() {

                    @Override
                    public void onSuccessResponse(List<String> data) {
                        showToast("绑定设备成功");
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(String errorCode, String describe) {
                        if (errorCode.equals("-1")) {
                            showToast("服务器异常");
                        } else {
                            if (errorCode.equals("12")) {
                                showToast("设备不存在");
                            } else if (errorCode.equals("9")) {
                                showToast("已被别人绑定");
                            } else if (errorCode.equals("13")) {
                                showToast("用户不存在");
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        dismissLoadingDialog();
                    }
                });

    }
}
