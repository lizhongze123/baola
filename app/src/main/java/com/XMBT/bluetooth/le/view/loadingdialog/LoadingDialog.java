package com.XMBT.bluetooth.le.view.loadingdialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;

/**
 * Created by lzz on 2017/8/8.
 */
public class LoadingDialog extends Dialog {

    private TextView loadingText;
    private LoadingDrawable mMaterialDrawable;
    private Context context;

    public LoadingDialog(Context context) {

        super(context, R.style.loading_dialog);
        this.context = context;
        setContentView(R.layout.common_loadingdialog);
        ImageView mImageView = (ImageView) findViewById(R.id.loadingdialog_img);
        mMaterialDrawable = new LoadingDrawable(new MaterialLoadingRenderer(context));
        mImageView.setImageDrawable(mMaterialDrawable);
        loadingText = (TextView) findViewById(R.id.loadingdialog_text);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMaterialDrawable.stop();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mMaterialDrawable.stop();
    }

    @Override
    public void show() {
        super.show();
        mMaterialDrawable.start();
    }

    public void setLoadingText(String str) {
        if (loadingText != null)
            loadingText.setText(str);
    }

    public void setLoadingText(int resId) {
        if (loadingText != null)
            loadingText.setText(resId);
    }
}
