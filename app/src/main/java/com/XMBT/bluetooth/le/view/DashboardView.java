package com.XMBT.bluetooth.le.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.utils.DensityUtils;

import java.math.BigDecimal;
import java.util.Random;

public class DashboardView extends View {

    private Paint mPaint;
    private float degree;

    public DashboardView(Context context) {
        this(context, null);
    }

    public DashboardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundResource(R.drawable.cashboard);
        canvas.rotate(degree, getWidth() / 2, getHeight());
        Path path = new Path();
        path.moveTo(getWidth() / 2, getHeight());
        path.lineTo(getWidth() / 2, getHeight() - DensityUtils.dp2px(getContext(), 6));
        path.lineTo(getWidth() / 2 - DensityUtils.dp2px(getContext(), 70), getHeight() - DensityUtils.dp2px(getContext(), 3));
        path.lineTo(getWidth() / 2, getHeight());
        path.close();
        canvas.drawPath(path, mPaint);

    }

    /**
     * 利用属性动画，不那么生硬
     *
     * @param degree
     */
    public void setDegree(float degree) {
        if (this.degree == degree) {
            return;
        }
        this.degree = degree;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }


    private float randomFloat() {
        float min = 45.0f;
        float max = 67.5f;
        float f = min + ((max - min) * new Random().nextFloat());
        BigDecimal b = new BigDecimal(f);
        float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        return f1;
    }

}
