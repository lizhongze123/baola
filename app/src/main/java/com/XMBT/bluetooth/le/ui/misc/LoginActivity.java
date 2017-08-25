package com.XMBT.bluetooth.le.ui.misc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.User;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.ui.gbattery.GpsBatteryActivity;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {

    private EditText phoneEt, passwordEt;
    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
        phoneEt = (EditText) findViewById(R.id.phoneEt);
        passwordEt = (EditText) findViewById(R.id.passwordEt);
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.imageView4:
                onBackPressed();
                break;
            case R.id.button8:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.button7:
                Intent intent1 = new Intent(this, ForgetPassWordActivity.class);
                startActivity(intent1);
                break;
            case R.id.button9:
                StringBuilder sb = new StringBuilder();
                if (TextUtils.isEmpty(phoneEt.getText().toString())) {
                    sb.append("账号不能为空\n");
                }
                if (TextUtils.isEmpty(passwordEt.getText().toString())) {
                    sb.append("密码不能为空");
                }
                if (sb.toString().equals("")) {
                    showLoadingDialog("加载中，请稍候");
                    OkGo.post(GlobalConsts.LOGIN)
                            .tag(this)
                            .params("LoginName", phoneEt.getText().toString())
                            .params("LoginPassword", passwordEt.getText().toString())
                            .params("LoginType", "ENTERPRISE")
                            .params("ISMD5", "0")
                            .params("timeZone", "8")
                            .params("apply", "APP")
                            .params("loginUrl", GlobalConsts.URL)
                            .execute(new StringCallback() {
                                @Override
                                public void onAfter(String s, Exception e) {
                                    super.onAfter(s, e);
                                    dismissLoadingDialog();
                                }

                                @Override
                                public void onError(Call call, Response response, Exception e) {
                                    super.onError(call, response, e);
                                    showToast("登录失败");

                                }

                                @Override
                                public void onSuccess(String s, Call call, Response response) {
                                    try {
                                        LogUtils.d(s);
                                        JSONObject jsonObject = new JSONObject(s);
                                        String success = jsonObject.getString("success");
                                        if (success.equals("false")) {
                                            String msg = jsonObject.getString("msg");
                                            showToast(msg);
                                        } else {
                                            String id = jsonObject.getString("id");
                                            String mds = jsonObject.getString("mds");
                                            String grade = jsonObject.getString("grade");
                                            User user = new User(id, mds, grade);

                                            // 创建字节输出流
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            try {
                                                // 创建对象输出流，并封装字节流
                                                ObjectOutputStream oos = new ObjectOutputStream(baos);
                                                // 将对象写入字节流
                                                oos.writeObject(user);
                                                // 将字节流编码成base64的字符窜
                                                String oAuth_Base64 = new String(Base64.encodeBase64(baos
                                                        .toByteArray()));
                                                GlobalConsts.userName = phoneEt.getText().toString();
                                                UserSp.getInstance(LoginActivity.this).setProduct(GlobalConsts.userName, oAuth_Base64);
                                                UserSp.getInstance(LoginActivity.this).setMds(GlobalConsts.userName, mds);
                                                UserSp.getInstance(LoginActivity.this).setId(GlobalConsts.userName,id);

                                                showToast("登录成功");
                                                GlobalConsts.isLogin = true;
                                                startActivity(new Intent(LoginActivity.this, GpsBatteryActivity.class));
                                                finish();
                                            } catch (IOException e) {

                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                } else {
                    showToast(sb.toString());
                }
                break;
        }
    }
}
