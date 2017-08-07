package com.XMBT.bluetooth.le.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

public class AddYuncheActivity extends Activity {
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_yunche);
        initView();
    }

    private void initView() {
        editText = (EditText) findViewById(R.id.addEt);
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.addBtn:
                addDevice();
                break;
        }
    }

    private void addDevice() {
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String mds = sp.getString("mds", null);
        String id = sp.getString("id", null);
        OkGo.post(GlobalConsts.URL + "GetDateServices.asmx/GetDate")
                .tag(this)
                .params("method", "MoveEquipment")
                .params("mds", mds)
                .params("macid", editText.getText().toString())
                .params("id", id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            String error = jsonObject.getString("errorCode");
                            String errorDescribe = jsonObject.getString("errorDescribe");
                            if (error.equals("200")) {
                                Toast.makeText(AddYuncheActivity.this, errorDescribe, Toast.LENGTH_SHORT).show();
                                finish();
                            } else if (error.equals("12")) {
                                Toast.makeText(AddYuncheActivity.this, "设备不存在", Toast.LENGTH_SHORT).show();
                            } else if (error.equals("9")) {
                                Toast.makeText(AddYuncheActivity.this, "已被别人绑定", Toast.LENGTH_SHORT).show();
                            } else if (error.equals("13")) {
                                Toast.makeText(AddYuncheActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddYuncheActivity.this, errorDescribe, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
