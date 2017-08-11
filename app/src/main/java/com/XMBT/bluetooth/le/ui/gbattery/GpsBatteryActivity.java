package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.ui.main.ActivityCollector;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;

/**
 * 汽车智能动力电池
 */
public class GpsBatteryActivity extends BaseActivity {

    private DeviceFragment deviceFragment;
    private PartsFragment partsFragment;
    private AdviceFragment adviceFragment;
    private MeFragment meFragment;

    private RadioButton[] btnAry = new RadioButton[4];
    private Fragment[] fragmentAry = null;
    private int currentIndex;
    private int selectedIndex;
    private MyButtonListener myButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsbattery);
        StatusBarHelper.setStatusBarColor(this, R.color.title_bg);
        ActivityCollector.addActivity(this);
        initViews();
        addListener();
    }


    private void initViews() {
        btnAry[0] = (RadioButton) findViewById(R.id.radio1);
        btnAry[1] = (RadioButton) findViewById(R.id.radio2);
        btnAry[2] = (RadioButton) findViewById(R.id.radio3);
        btnAry[3] = (RadioButton) findViewById(R.id.radio4);
        deviceFragment = DeviceFragment.newInstance();
        partsFragment = new PartsFragment();
        adviceFragment = new AdviceFragment();
        meFragment = new MeFragment();
        fragmentAry = new Fragment[]{deviceFragment, partsFragment, adviceFragment, meFragment};
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, deviceFragment);
        fragmentTransaction.show(deviceFragment);
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

}
