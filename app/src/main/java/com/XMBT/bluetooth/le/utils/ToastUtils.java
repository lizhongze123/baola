package com.XMBT.bluetooth.le.utils;


import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {

    private static Toast toast;


   /* public static void toast(Context context, String tips) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_toast, null);
        TextView t = (TextView) view.findViewById(R.id.toast_text);
        t.setText(tips);
        if (toast != null) {
            toast.cancel();
        }
        toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }*/


    public static void toastInBottom(Context context, String tip) {

        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), tip, Toast.LENGTH_SHORT);
        } else {
            toast.setText(tip);
        }
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.show();
    }

}
