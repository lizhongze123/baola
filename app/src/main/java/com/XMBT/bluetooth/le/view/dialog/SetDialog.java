package com.XMBT.bluetooth.le.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;


public class SetDialog extends Dialog {

    private Context mContext;

    private EditText et_phoneNum;
    private TextView tv_cancel;
    private TextView tv_positive;

    public SetDialog(Context context) {
        super(context, R.style.tip_dialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.dialog_set);
        initView();
        init();
    }

    private void initView() {
        et_phoneNum = (EditText) findViewById(R.id.et_phoneNum);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_positive = (TextView) findViewById(R.id.tv_positive);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tv_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPositive(et_phoneNum.getText().toString().trim());
            }
        });
    }

    private void init() {
        Window window = this.getWindow();
        LayoutParams params = window.getAttributes();
        LayoutParams p = window.getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(p);
//      window.setWindowAnimations(R.style.Animation_Popup);
        this.setCancelable(true);
        this.setCanceledOnTouchOutside(true);
    }

    public void setHint(String hint){
        et_phoneNum.setHint(hint);
    }

    public void setEtText(String var){
        if(var != null){
            et_phoneNum.setText(var);
        }
    }

    private OnPositiveListener mListener;

    public interface OnPositiveListener {
        void onPositive(String str);
    }

    public void setOnPositiveListener(OnPositiveListener listener) {
        this.mListener = listener;
    }


}
