package com.XMBT.bluetooth.le.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


import com.XMBT.bluetooth.le.R;

import java.util.HashMap;
import java.util.Map;

public class BaseDialog extends Dialog {

    public BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
    }

    public BaseDialog(Context context) {
        super(context);
    }

    public static class Builder {
        private Context context;
        private boolean cancelable = false;
        private OnCancelListener cancelListener;
        private int theme = R.style.tip_dialog;
        private CharSequence positionText;
        private CharSequence negativeText;
        private OnClickListener positiveListener;
        private OnClickListener negativeListener;
        private View centerView;
        private CharSequence title;
        private LayoutParams centerLP = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        private Map<String, View> viewMap;
        private int titleImageResId;
        private CharSequence message;
        private int messageColor = -1;
        private int titleBackgroundColor = -1;
        private int titleImageBackgroundResId = -1;

        public Builder(Context context) {
            this.context = context;
            viewMap = new HashMap<String, View>();
        }

        public Context getContext() {
            return context;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setCancelListener(OnCancelListener cancelListener) {
            this.cancelListener = cancelListener;
            return this;
        }

        public Builder setTheme(int theme) {
            this.theme = theme;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(int textId) {
            this.title = getContext().getString(textId);
            return this;
        }

        public Builder setCenterView(View centerView) {
            this.centerView = centerView;
            return this;
        }

        public Builder setCenterView(int resId) {
            this.centerView = View.inflate(getContext(), resId, null);
            return this;
        }

        public Builder setPositiveButton(int textId, OnClickListener listener) {
            this.positionText = getContext().getString(textId);
            this.positiveListener = listener;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
            this.positionText = text;
            this.positiveListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId, OnClickListener listener) {
            this.negativeText = getContext().getString(textId);
            this.negativeListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, OnClickListener listener) {
            this.negativeText = text;
            this.negativeListener = listener;
            return this;
        }

        public Dialog create() {
            final Dialog dialog = new Dialog(getContext(), theme);
            dialog.setCancelable(cancelable);
            dialog.setOnCancelListener(cancelListener);
            View view = View.inflate(getContext(), R.layout.common_base_dialog, null);
            dialog.setContentView(view);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            if (titleImageResId > 0) {
                ImageView iv_title = (ImageView) view.findViewById(R.id.iv_title);
                iv_title.setImageResource(titleImageResId);
                if (titleImageBackgroundResId != -1) {
                    iv_title.setBackgroundResource(titleImageBackgroundResId);
                }
            } else if (!TextUtils.isEmpty(title)) {
                tv_title.setText(title);
            } else {
                tv_title.setText("");
            }
            if (titleBackgroundColor != -1) {
                tv_title.setBackgroundColor(titleBackgroundColor);
            }
            if (TextUtils.isEmpty(positionText) && TextUtils.isEmpty(negativeText)) {
                view.findViewById(R.id.lay_bottom).setVisibility(View.GONE);
            } else {
                View bottomView = view.findViewById(R.id.lay_bottom);
                bottomView.setVisibility(View.VISIBLE);
                Button btn_positive = (Button) bottomView.findViewById(R.id.btn_positive);
                Button btn_negative = (Button) bottomView.findViewById(R.id.btn_negative);
                if (TextUtils.isEmpty(positionText)) {
                    btn_positive.setVisibility(View.GONE);
                } else {
                    btn_positive.setVisibility(View.VISIBLE);
                    btn_positive.setText(positionText);
                    btn_positive.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            if (positiveListener != null) {
                                positiveListener.onClick(dialog, BUTTON_POSITIVE);
                            }
                        }
                    });
                }

                if (TextUtils.isEmpty(negativeText)) {
                    btn_negative.setVisibility(View.GONE);
                } else {
                    btn_negative.setVisibility(View.VISIBLE);
                    btn_negative.setText(negativeText);
                    btn_negative.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            if (negativeListener != null) {
                                negativeListener.onClick(dialog, BUTTON_NEGATIVE);
                            }
                        }
                    });
                }
                if (!TextUtils.isEmpty(positionText) && !TextUtils.isEmpty(negativeText)) {
                    btn_negative.setBackgroundResource(R.drawable.common_selector_dialog_press_negative);
                    btn_positive.setBackgroundResource(R.drawable.common_selector_dialog_press_position);
                } else {
                    btn_negative.setBackgroundResource(R.drawable.common_selector_dialog_press_one_negative);
                    btn_positive.setBackgroundResource(R.drawable.common_selector_dialog_press_one_position);
                }
            }
            ViewGroup centerLay = (ViewGroup) view.findViewById(R.id.lay_center);
            centerView = getCenterView(centerView, centerLay);
            if (centerView != null) {
                centerView.setLayoutParams(centerLP);
                centerLay.removeAllViews();
                centerLay.addView(centerView);
            } else if (message != null) {
                TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
                tv_content.setText(message);
                if (messageColor != -1) {
                    tv_content.setTextColor(messageColor);
                }
            }
            initDialogSize(dialog);
            return dialog;
        }

        private void initDialogSize(Dialog dialog) {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
//			params.width = (int) (metrics.widthPixels - 40 * 2 * metrics.density);
            Configuration mConfiguration = getContext().getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
                params.width = (int) (metrics.widthPixels * 0.5);
            } else {
                params.width = (int) (metrics.widthPixels * 0.8);
            }
            params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
        }

        protected View getCenterView(View centerView, ViewGroup container) {
            return centerView;
        }

        public void saveView(String key, View view) {
            viewMap.put(key, view);
        }

        public Map getViewMap() {
            return viewMap;
        }

        public View getView(String key) {
            return viewMap.get(key);
        }

        public int getTitleImageResId() {
            return titleImageResId;
        }

        public Builder setTitleImageResId(int titleImageResId) {
            this.titleImageResId = titleImageResId;
            return this;
        }

        public CharSequence getMessage() {
            return message;
        }

        public Builder setMessage(CharSequence message) {
            this.message = message;
            return this;
        }

        public int getMessageColor() {
            return messageColor;
        }

        public Builder setMessageColor(int messageColor) {
            this.messageColor = messageColor;
            return this;
        }

        public int getTitleBackgroundColor() {
            return titleBackgroundColor;
        }

        public Builder setTitleBackgroundColor(int titleBackgroundColor) {
            this.titleBackgroundColor = titleBackgroundColor;
            return this;
        }

        public int getTitleImageBackgroundResId() {
            return titleImageBackgroundResId;
        }

        public Builder setTitleImageBackgroundResId(int titleImageBackgroundResId) {
            this.titleImageBackgroundResId = titleImageBackgroundResId;
            return this;
        }
    }
}
