package com.XMBT.bluetooth.le.ui.misc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

public class ForgetPassWordActivity extends Activity {
    private EditText phoneEt, codeEt, passwordEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_forget_pass_word);
        initView();
    }

    private void initView() {
        phoneEt = (EditText) findViewById(R.id.phoneEt);
        codeEt = (EditText) findViewById(R.id.codeEt);
        passwordEt = (EditText) findViewById(R.id.passwordEt);
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.imageView4:
                onBackPressed();
                break;
            case R.id.getcodeBtn:
                if (phoneEt.getText().toString().equals("")) {
                    Toast.makeText(this, "请先输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                OkGo.post(GlobalConsts.URL + "GetDateServices.asmx/GetDate")
                        .tag(this)
                        .params("method", "SendVerifyMessage")
                        .params("telNumber", phoneEt.getText().toString())
                        .params("identifier", "0")
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(String s, Call call, Response response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    String code = jsonObject.getString("errorCode");
                                    String errorDescribe = jsonObject.getString("errorDescribe");
                                    if (code.equals("200")) {
                                        Toast.makeText(ForgetPassWordActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                                    } else if (code.equals("23")) {
                                        Toast.makeText(ForgetPassWordActivity.this, "手机格式不支持", Toast.LENGTH_SHORT).show();
                                    } else if (code.equals("11")) {
                                        Toast.makeText(ForgetPassWordActivity.this, "用户已存在", Toast.LENGTH_SHORT).show();
                                    } else if (code.equals("7")) {
                                        Toast.makeText(ForgetPassWordActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                break;
            case R.id.confirmBtn:
                StringBuilder sb = new StringBuilder();
                if (TextUtils.isEmpty(phoneEt.getText().toString())) {
                    sb.append("手机号不能为空\n");
                }
                if (TextUtils.isEmpty(codeEt.getText().toString())) {
                    sb.append("验证码不能为空\n");
                }
                if (TextUtils.isEmpty(passwordEt.getText().toString())) {
                    sb.append("密码不能为空\n");
                }
//                if(TextUtils.isEmpty(repwEt.getText().toString())){
//                    sb.append("重复密码不能为空\n");
//                }else if(!repwEt.getText().toString().equals(passwordEt.getText().toString())){
//                    sb.append(sb.append("两次密码不同\n"));
//                }
                if (sb.toString().equals("")) {
                    OkGo.post(GlobalConsts.URL + "url/GetDateServices.asmx/GetDate")
                            .tag(this)
                            .params("method", "ResetPassWordWMS")
                            .params("messageCode", codeEt.getText().toString())
                            .params("telNumber", phoneEt.getText().toString())
                            .params("password", passwordEt.getText().toString())
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(String s, Call call, Response response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(s);
                                        String errorCode = jsonObject.getString("errorCode");
                                        String errorDescribe = jsonObject.getString("errorDescribe");
                                        if (errorCode.equals("200")) {
                                            Toast.makeText(ForgetPassWordActivity.this, errorDescribe, Toast.LENGTH_SHORT).show();
                                            finish();
                                            Intent intent = new Intent(ForgetPassWordActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                        } else if (errorCode.equals("21")) {
                                            Toast.makeText(ForgetPassWordActivity.this, "验证码失效", Toast.LENGTH_SHORT).show();
                                        } else if (errorCode.equals("22")) {
                                            Toast.makeText(ForgetPassWordActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                                        } else if (errorCode.equals("7")) {
                                            Toast.makeText(ForgetPassWordActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(ForgetPassWordActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
