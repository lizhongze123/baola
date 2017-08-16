package com.XMBT.bluetooth.le.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;

/**
 * Created by lzz on 2017/8/11.
 */

public class WarningDialog {

    public static Dialog normal (final Context context, String title, String msg, String okText, String cancelText, final DialogInterface.OnClickListener onClickListener){
        BaseDialog.Builder builder = new BaseDialog.Builder(context){
            @Override
            protected View getCenterView(View centerView, ViewGroup container) {
                return centerView;
            }
        };
        View view = View.inflate(context, R.layout.common_warn_dialog, null);
        ((TextView) view.findViewById(R.id.dialog_hint)).setText(msg);
        builder.setCenterView(view);
        if (!TextUtils.isEmpty(okText)) {
            builder.setPositiveButton(okText, onClickListener);
        }
        if (!TextUtils.isEmpty(cancelText)) {
            builder.setNegativeButton(cancelText, onClickListener);
        }
        builder.setTitle(title);
        return builder.create();
    }

}
