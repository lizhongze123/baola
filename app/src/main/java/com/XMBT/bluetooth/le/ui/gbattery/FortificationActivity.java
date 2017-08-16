package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 设防控制
 */
public class FortificationActivity extends BaseActivity {

    private FortificationFragment fortificationFragment;
    private StealthFragment stealthFragment;
    private RadioButton[] btnAry = new RadioButton[2];
    private Fragment[] fragmentAry = null;
    private int currentIndex;
    private int selectedIndex;
    private MyButtonListener myButtonListener;
    private YunCheDeviceEntity device;
    private String defenceStatus;
    private String gpsOnOff;
    public static final String STATUS = "status";
    public static final String ON = "1";
    public static final String Off = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortification);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        device = (YunCheDeviceEntity) getIntent().getSerializableExtra(DeviceFragment.DATA_DEVICE);
        getStatus();
    }

    private void initViews() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnAry[0] = (RadioButton) findViewById(R.id.radio1);
        btnAry[1] = (RadioButton) findViewById(R.id.radio2);
        fortificationFragment = FortificationFragment.newInstance(device, defenceStatus);
        stealthFragment = StealthFragment.newInstance(device, gpsOnOff);
        fragmentAry = new Fragment[]{fortificationFragment, stealthFragment};
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransation = fragmentManager.beginTransaction();
        fragmentTransation.add(R.id.fragment_container, fortificationFragment);
        fragmentTransation.show(fortificationFragment);
        fragmentTransation.commit();
    }

    private void addListener() {
        myButtonListener = new MyButtonListener();
        for (int i = 0; i < btnAry.length; i++) {
            btnAry[i].setOnClickListener(myButtonListener);
        }
    }

    class MyButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.radio1:
                    selectedIndex = 0;
                    break;
                case R.id.radio2:
                    selectedIndex = 1;
                    break;
            }
            if (selectedIndex != currentIndex) {
                FragmentTransaction transation = getSupportFragmentManager().beginTransaction();
                transation.hide(fragmentAry[currentIndex]);
                if (!fragmentAry[selectedIndex].isAdded()) {
                    transation.add(R.id.fragment_container, fragmentAry[selectedIndex]);
                }
                transation.show(fragmentAry[selectedIndex]);
                transation.commit();
                btnAry[selectedIndex].setSelected(true);
                btnAry[currentIndex].setSelected(false);
                currentIndex = selectedIndex;
            }
        }
    }

    public void getStatus() {
        showLoadingDialog(null);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "getUserStatus")
                .params("mds", mds)
                .params("macid", device.macid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            LogUtils.d(s);
                            JSONObject jsonObject = new JSONObject(s);
                            String success = jsonObject.getString("success");
                            if (success.equals("false")) {
                                String msg = jsonObject.getString("msg");
                                showToast(msg);
                            }else{
                                JSONObject rowsObj = jsonObject.getJSONObject("rows");
                                defenceStatus = rowsObj.getString("defenceStatus");
                                gpsOnOff = rowsObj.getString("gpsOnOff");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initViews();
                                        addListener();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        dismissLoadingDialog();
                    }
                });

    }
}
