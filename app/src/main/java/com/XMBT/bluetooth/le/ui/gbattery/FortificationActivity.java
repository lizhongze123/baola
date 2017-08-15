package com.XMBT.bluetooth.le.ui.gbattery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;

/**
 * 设防控制
 */
public class FortificationActivity extends BaseActivity {

    private FortificationFragment fortificationFragment;
    private InvisibleFragment invisibleFragment;
    private RadioButton[] btnAry = new RadioButton[2];
    private Fragment[] fragmentAry = null;
    private int currentIndex;
    private int selectedIndex;
    private MyButtonListener myButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortification);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        initViews();
        addListener();
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
        fortificationFragment = new FortificationFragment();
        invisibleFragment = new InvisibleFragment();
        fragmentAry = new Fragment[]{fortificationFragment, invisibleFragment};
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
}
