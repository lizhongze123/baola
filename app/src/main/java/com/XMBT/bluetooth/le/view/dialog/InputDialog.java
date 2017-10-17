package com.XMBT.bluetooth.le.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.utils.LogUtils;


public class InputDialog extends Dialog{

    private Context context;

    private EditText etNum;
    private TextView tvNavigate, tvPositive;

    public InputDialog(@NonNull Context context) {
        super(context, R.style.tip_dialog);
        this.context = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_input_dialog);
        init();
        initViews();
    }

    private void initViews(){
        LogUtils.e("1111");
        etNum = (EditText) findViewById(R.id.et_num);
        tvNavigate = (TextView) findViewById(R.id.tv_cancel);
        tvPositive = (TextView) findViewById(R.id.tv_positive);
        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPositive(etNum.getText().toString());
            }
        });
        tvNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNavigate();
                dismiss();
            }
        });
    }

    private void init() {
        Window window = this.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        WindowManager.LayoutParams p = window.getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(p);
//      window.setWindowAnimations(R.style.Animation_Popup);
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);
    }

    public void showDialog(){
        if(etNum != null){
            etNum.setText("");
        }
        this.show();
    }

    private OnButtonListener mListener;

    public interface OnButtonListener {
        void onPositive(String str);
        void onNavigate();
    }

    public void setOnButtonListener(OnButtonListener listener) {
        this.mListener = listener;
    }
}
