package com.XMBT.bluetooth.le.ui.gbattery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.User;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.http.ApiResultCallback;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.ui.main.ActivityCollector;
import com.XMBT.bluetooth.le.ui.misc.LoginActivity;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.utils.VersionUtils;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 修改密码
 */
public class ChangePwdActivity extends BaseActivity {

    private EditText etOldPwd, etNewPwd, etConfirmPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change(etOldPwd.getText().toString().trim(), etNewPwd.getText().toString().trim(), etConfirmPwd.getText().toString().trim());
            }
        });
        etOldPwd = (EditText) findViewById(R.id.et_oldPwd);
        etNewPwd = (EditText) findViewById(R.id.et_newPwd);
        etConfirmPwd = (EditText) findViewById(R.id.et_confirmPwd);
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void change(String oldPwd, String newPwd, String confirmPwd) {
        if (TextUtils.isEmpty(oldPwd)) {
            showToast("请输入旧密码");
            return;
        } else if (TextUtils.isEmpty(newPwd)) {
            showToast("请输入新密码");
            return;
        } else if (TextUtils.isEmpty(confirmPwd)) {
            showToast("请确认新密码");
            return;
        } else if (!newPwd.equals(confirmPwd)) {
            showToast("新密码不一致");
            return;
        }
        showLoadingDialog("加载中，请稍候");
        String mds = UserSp.getInstance(this).getMds();
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("mds", mds)
                .params("method", "modifyEnterprisePwd")
                .params("reg_pass", newPwd)
                .params("reg_olbpass", oldPwd)
                .execute(new ApiResultCallback<List<String>>() {
                    @Override
                    public void onSuccessResponse(List<String> data) {
                        showToast("修改密码成功");
                        GlobalConsts.isLogin = false;
                        startActivity(new Intent(ChangePwdActivity.this, LoginActivity.class));
                        ActivityCollector.finishAll();
                        finish();
                    }

                    @Override
                    public void onFailure(String errorCode) {
                        if (errorCode.equals("-1")) {
                            showToast("服务器异常");
                        } else {
                            showToast("修改密码失败");
                        }
                    }

                    @Override
                    public void onFinish() {
                        dismissLoadingDialog();
                    }
                });
//                .execute(new StringCallback() {
//                    @Override
//                    public void onAfter(String s, Exception e) {
//                        super.onAfter(s, e);
//                        dismissLoadingDialog();
//                    }
//
//                    @Override
//                    public void onSuccess(String s, Call call, Response response) {
//                        try {
//                            LogUtils.e(s);
//                            JSONObject jsonObject = new JSONObject(s);
//                            String success = jsonObject.getString("success");
//                            if (success.equals("false")) {
//                                String msg = jsonObject.getString("msg");
//                                showToast(msg);
//                            }else{
//                                showToast("修改密码成功");
//                                GlobalConsts.isLogin = false;
//                                startActivity(new Intent(ChangePwdActivity.this, LoginActivity.class));
//                                ActivityCollector.finishAll();
//                                finish();
//                            }
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//
//                    }
//
//                });
    }

}
