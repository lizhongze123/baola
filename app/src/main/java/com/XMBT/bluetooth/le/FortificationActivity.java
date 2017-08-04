package com.XMBT.bluetooth.le;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;

public class FortificationActivity extends FragmentActivity {
    FortificationFragment fortificationFragment;
    InvisibleFragment invisibleFragment;
    RadioButton[] btnAry = new RadioButton[2];
    Fragment[] fragmentAry = null;
    int currentIndex;
    int selectedIndex;
    MyButtonListener myButtonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortification);
        setView();
        addListener();
    }

    private void setView() {
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

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.backIv:
                onBackPressed();
                break;
        }
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
