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

/**
 * Created by lzz on 2017/9/28.
 */

public class InputDialog extends Dialog{

    private Context context;

    private EditText etNum;
    private TextView tvNavigate, tvPositive;

    public InputDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_input_dialog);
        initViews();
        init();
    }

    private void initViews(){
        etNum = (EditText) findViewById(R.id.et_num);
        tvNavigate = (TextView) findViewById(R.id.tv_cancel);
        tvPositive = (TextView) findViewById(R.id.tv_positive);
        tvNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPositive(etNum.getText().toString());
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
        this.setCancelable(true);
        this.setCanceledOnTouchOutside(true);
    }

    private OnPositiveListener mListener;

    public interface OnPositiveListener {
        void onPositive(String str);
    }

    public void setOnPositiveListener(OnPositiveListener listener) {
        this.mListener = listener;
    }
}
