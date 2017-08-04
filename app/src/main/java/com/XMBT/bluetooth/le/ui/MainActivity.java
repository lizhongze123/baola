package com.XMBT.bluetooth.le.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;

import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.ui.device.IndexFragment;
import com.XMBT.bluetooth.le.ui.parts.PartsFragment;
import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.ble.DeviceScanActivity;
import com.XMBT.bluetooth.le.ui.advice.AdviceFragment;

/**
 * MainActivity
 */
public class MainActivity extends BaseActivity {

    private IndexFragment indexFragment;
    private PartsFragment partsFragment;
    private AdviceFragment adviceFragment;
    //    MeFragment meFragment;
    private RadioButton[] btnAry = new RadioButton[3];
    private Fragment[] fragmentAry = null;
    private int currentIndex;
    private int selectedIndex;
    private MyButtonListener myButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        setView();
        addListener();
    }

    private void setView() {
        btnAry[0] = (RadioButton) findViewById(R.id.radio1);
        btnAry[1] = (RadioButton) findViewById(R.id.radio2);
        btnAry[2] = (RadioButton) findViewById(R.id.radio3);
        indexFragment = new IndexFragment();
        partsFragment = new PartsFragment();
//        meFragment=new MeFragment();
        adviceFragment = new AdviceFragment();
        fragmentAry = new Fragment[]{indexFragment, partsFragment, adviceFragment};
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransation = fragmentManager.beginTransaction();
        fragmentTransation.add(R.id.fragment_container, indexFragment);
        fragmentTransation.show(indexFragment);
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
                case R.id.radio3:
                    selectedIndex = 2;
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

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                Intent intent = new Intent(this, DeviceScanActivity.class);
                startActivity(intent);
                break;
        }
    }
}
