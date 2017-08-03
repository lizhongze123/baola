package com.XMBT.bluetooth.le.ui.misc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.bean.User;
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

public class LoginActivity extends Activity {
    private EditText phoneEt,passwordEt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        phoneEt= (EditText) findViewById(R.id.phoneEt);
        passwordEt= (EditText) findViewById(R.id.passwordEt);
    }

    public void doClick(View view){
        switch (view.getId()){
            case R.id.imageView4:
                onBackPressed();
                break;
            case R.id.button8:
                Intent intent=new Intent(this,RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.button7:
                Intent intent1=new Intent(this,ForgetPassWordActivity.class);
                startActivity(intent1);
                break;
            case R.id.button9:
                StringBuilder sb=new StringBuilder();
                if (TextUtils.isEmpty(phoneEt.getText().toString())){
                    sb.append("账号不能为空\n");
                }
                if (TextUtils.isEmpty(passwordEt.getText().toString())){
                    sb.append("密码不能为空");
                }
                if(sb.toString().equals("")){
                    OkGo.post(GlobalConsts.URL+"GetDateServices.asmx/loginSystem")
                            .tag(this)
                            .params("LoginName",phoneEt.getText().toString())
                            .params("LoginPassword",passwordEt.getText().toString())
                            .params("LoginType","ENTERPRISE")
                            .params("ISMD5","0")
                            .params("timeZone","8")
                            .params("apply","APP")
                            .params("loginUrl","http://app.ycqpmall.com/")
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(String s, Call call, Response response) {
                                    try {
                                        JSONObject jsonObject=new JSONObject(s);
                                        String success=jsonObject.getString("success");
                                        if(success.equals("false")){
                                            String msg=jsonObject.getString("msg");
                                            Toast.makeText(LoginActivity.this,msg,Toast.LENGTH_SHORT).show();
                                        }else {
                                            String id=jsonObject.getString("id");
                                            String mds=jsonObject.getString("mds");
                                            String grade=jsonObject.getString("grade");
                                            User user=new User(id,mds,grade);
                                            SharedPreferences preferences = getSharedPreferences("userInfo", MODE_PRIVATE);
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
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.putString("product", oAuth_Base64);
                                                editor.putString("mds",mds);
                                                editor.putString("id",id);
                                                editor.commit();
                                                Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                                                finish();
                                            } catch (IOException e) {
                                                // TODO Auto-generated
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                }else {
                    Toast.makeText(this,sb.toString(),Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
