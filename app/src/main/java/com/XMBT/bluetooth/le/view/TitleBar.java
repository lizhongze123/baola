package com.XMBT.bluetooth.le.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;

/**
 *  标题栏控件
 */

public class TitleBar extends RelativeLayout  implements View.OnClickListener {


    /**
     * 标题栏的根布局
     */
    private RelativeLayout mRelativeLayout;

    /**
     * 左边
     */
    private int left_button_imageId;
    private String left_button_text;
    private int left_button_textColor;
    private int left_button_textSize;
    private ImageView mIvLeft;
    private TextView mTvLeft;
    /**
     * 右边
     */
    private int right_button_textColor;
    private ImageView mIvRight;
    private TextView mTvRight;
    private int right_button_image_id;
    private String right_button_text;
    private int right_button_textSize;
    /**
     * 中间
     */
    private int title_background_color;
    private TextView mTvTilte;
    private String title_text;
    private int title_textColor;
    private int title_textSize;

    /**
     * 标题栏的顶部分割线
     */
    private View line;

    private TitleBarOnClickListener mTitleBarOnClickListener;

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        obtainAttrs(context, attrs);
    }

    private void obtainAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
        /**返回按钮相关*/
        left_button_imageId = typedArray.getResourceId(R.styleable.TitleBar_left_button_image, 0);
        left_button_text = typedArray.getString(R.styleable.TitleBar_left_button_text);
        left_button_textColor = typedArray.getColor(R.styleable.TitleBar_left_button_textColor, Color.GRAY);
        left_button_textSize = typedArray.getDimensionPixelSize(R.styleable.TitleBar_left_button_textSize, sp2px(context, 14));

        /**标题相关*/
        title_background_color = typedArray.getColor(R.styleable.TitleBar_title_background, Color.WHITE);
        title_text = typedArray.getString(R.styleable.TitleBar_title_text);
        title_textColor = typedArray.getColor(R.styleable.TitleBar_title_textColor, Color.GRAY);
        title_textSize = typedArray.getDimensionPixelSize(R.styleable.TitleBar_title_textSize, sp2px(context, 16));

        /**右边保存按钮相关*/
        right_button_image_id = typedArray.getResourceId(R.styleable.TitleBar_right_button_image,0);
        right_button_text = typedArray.getString(R.styleable.TitleBar_right_button_text);
        right_button_textColor = typedArray.getColor(R.styleable.TitleBar_right_button_textColor, Color.GRAY);
        right_button_textSize = typedArray.getDimensionPixelSize(R.styleable.TitleBar_right_button_textSize, sp2px(context, 14));

        /**分割线*/
        typedArray.getBoolean(R.styleable.TitleBar_show_line, true);

        /**设置值*/
        setTitleBarBackground(title_background_color);
        setTilte(title_text);
        setTitleTextSize(title_textSize);
        setTitle_textColor(title_textColor);

        setLeftIcon(left_button_imageId);
        setTvLeft(left_button_text);
        setTvLeftTextSize(left_button_textSize);
        setTvLeftTextColor(left_button_textColor);

        setRightIcon(right_button_image_id);
        setTvRight(right_button_text);
        setTvRightTextColor(right_button_textColor);
        setTvRightTextSize(right_button_textSize);

        typedArray.recycle();
    }

    private void init() {
        /**加载布局文件*/
        View.inflate(getContext(), R.layout.title_bar, this);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relay_background);
        mIvLeft = (ImageView) findViewById(R.id.iv_left);
        mTvLeft = (TextView) findViewById(R.id.tv_left);
        mTvTilte = (TextView) findViewById(R.id.tv_title);
        mTvRight = (TextView) findViewById(R.id.tv_right);
        mIvRight = (ImageView) findViewById(R.id.iv_right);
        line = findViewById(R.id.line);
    }

    public void setTilte(String tilte) {
        if (TextUtils.isEmpty(tilte)) {
            mTvTilte.setVisibility(GONE);
        } else {
            mTvTilte.setText(tilte);
            mTvTilte.setVisibility(VISIBLE);
        }
    }

    public void setTitleTextSize(int textSize) {
        mTvTilte.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
    }

    public void setTitle_textColor(int textColor) {
        mTvTilte.setTextColor(textColor);
    }

    public void setTvLeft(String text) {
        if (TextUtils.isEmpty(text)) {
            mTvLeft.setVisibility(GONE);
        } else {
            mTvLeft.setVisibility(VISIBLE);
            mTvLeft.setText(text);
        }
    }

    public void setTvLeftTextSize(int textSize) {
        mTvLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
    }

    public void setTvLeftTextColor(int textColor) {
        mTvLeft.setTextColor(textColor);
    }

    public void setTvRight(String text) {
        if (TextUtils.isEmpty(text)) {
            mTvRight.setVisibility(GONE);
        } else {
            mTvRight.setVisibility(VISIBLE);
            mTvRight.setText(text);
        }
    }

    public void setTvRightTextSize(int textSize) {
        mTvRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
    }

    public void setTvRightTextColor(int textColor) {
        mTvRight.setTextColor(textColor);
    }

    public void setLeftIcon(int resId) {
        if (resId == 0) {
            mIvLeft.setVisibility(View.GONE);
        } else {
            mIvLeft.setVisibility(View.VISIBLE);
            mIvLeft.setImageResource(resId);
        }
    }

    public void setRightIcon(int resId) {
        if (resId == 0) {
            mIvRight.setVisibility(View.GONE);
        } else {
            mIvRight.setVisibility(View.VISIBLE);
            mIvRight.setImageResource(resId);
        }
    }

    public void setAction(TitleBarOnClickListener listener) {
        mIvLeft.setOnClickListener(this);
        mIvRight.setOnClickListener(this);
        mTvLeft.setOnClickListener(this);
        mTvRight.setOnClickListener(this);
        mTitleBarOnClickListener = listener;
    }

    public void setLeftOnClickListener(OnClickListener listener) {
        mIvLeft.setOnClickListener(listener);
        mTvLeft.setOnClickListener(listener);
    }


    /**
     * 设置是否显示分割线
     *
     * @param visibility
     */
    public void setLineIsVisible(int visibility) {
        line.setVisibility(visibility);
    }

    /**
     * 设置是否显示右边按钮
     *
     * @param show_right_button
     */
    public void setShow_right_button(boolean show_right_button) {
        mTvRight.setVisibility(show_right_button ? VISIBLE : INVISIBLE);
        mIvRight.setVisibility(show_right_button ? VISIBLE : INVISIBLE);
    }

    /**
     * 设置是否显示左边按钮
     *
     * @param show_left_button
     */
    public void setShow_left_button(boolean show_left_button) {
        mIvLeft.setVisibility(show_left_button ? VISIBLE : INVISIBLE);
        mIvLeft.setVisibility(show_left_button ? VISIBLE : INVISIBLE);
    }


    /**
     * 设置标题栏背景色
     *
     * @param resId
     */
    public void setTitleBarBackground(int resId) {
        mRelativeLayout.setBackgroundColor(resId);
    }


    public  long lastClickTime = 0;
    public  boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (timeD >= 0 && timeD <= 500) {
            return true;
        } else {
            lastClickTime = time;
            return false;
        }
    }

    public interface TitleBarOnClickListener {
        void performAction(View view);
    }

    @Override
    public void onClick(View v) {
        if (isFastDoubleClick()) {
            return;
        }

//        mTitleBarOnClickListener.performAction(v);
    }



    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }
}
