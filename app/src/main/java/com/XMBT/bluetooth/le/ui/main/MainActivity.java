package com.XMBT.bluetooth.le.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.ui.gbattery.AdviceFragment;
import com.XMBT.bluetooth.le.ui.gbattery.PartsFragment;
import com.XMBT.bluetooth.le.utils.Configure;
import com.XMBT.bluetooth.le.utils.PreferenceUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;

/**
 * 首页tab activity
 */

public class MainActivity extends BaseActivity {

    private MyProductFragment myProductFragment;
    private PartsFragment partsFragment2;
    private AdviceFragment adviceFragment;
    private PartsFragment partsFragment4;
    private MySelfFragment mySelfFragment;

    private RadioButton[] btnAry = new RadioButton[5];
    private Fragment[] fragmentAry = null;
    private int currentIndex;
    private int selectedIndex;
    private MyButtonListener myButtonListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarHelper.setStatusBarColor(this, R.color.title_bg);
        ActivityCollector.addActivity(this);
        initViews();
        addListener();
        init(this);
    }

    // 初始化
    public static void init(Context mContext) {
//        Configure.USERID = PreferenceUtils.readString(mContext, "USER", "USER_ID");
//        if (!TextUtils.isEmpty(Configure.USERID)){
//            Configure.isLogin = true;
//        }
    }

    private void initViews() {
        btnAry[0] = (RadioButton) findViewById(R.id.radio1);
        btnAry[1] = (RadioButton) findViewById(R.id.radio2);
        btnAry[2] = (RadioButton) findViewById(R.id.radio3);
        btnAry[3] = (RadioButton) findViewById(R.id.radio4);
        btnAry[4] = (RadioButton) findViewById(R.id.radio5);
        myProductFragment = new MyProductFragment();
        partsFragment2 = new PartsFragment();
        adviceFragment = new AdviceFragment();
        partsFragment4 = new PartsFragment();
        mySelfFragment = new MySelfFragment();
        fragmentAry = new Fragment[]{myProductFragment, partsFragment2, adviceFragment, partsFragment4, mySelfFragment};
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, myProductFragment);
        fragmentTransaction.show(myProductFragment);
        fragmentTransaction.commit();
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
                case R.id.radio3:
                    selectedIndex = 2;
                    break;
                case R.id.radio4:
                    selectedIndex = 3;
                    break;
                case R.id.radio5:
                    selectedIndex = 4;
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

    private boolean isExit = false;

    @Override
    public void onBackPressed() {
        if(isExit){
            super.onBackPressed();
        }else{
            isExit = true;
            showToast("再次点击将退出程序");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        }
    }
}
