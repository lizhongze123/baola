package com.XMBT.bluetooth.le.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.utils.DensityUtils;

/**
 * 充电进度绘制，使用属性动画完成。
 * Created by ly on 2016/12/6.
 */

public class ChargingProgess extends View {

    private Context mContext;
    private Paint mPaint;


    //竖直方向
    private static final int VERTICAL = 0;
    //水平方向
    private static final int HORIZONTAL = 1;

    //view的方向
    private int oritation;
    //边界宽度
    private float border_width;
    //item个数
    private int item_count;

    //边界宽度
    private float item_width;
    //边界高度
    private float item_height;
    //view内部的进度前景色
    private int item_charging_src;
    //view内部的进度背景色
    private int item_charging_background;
    //view背景色
    private int background;
    //<!--边界颜色-->
    private int border_color;
    //圆角半径
    private float border_cornor_radius;
    //动画时间
    private int duration;

    //view的宽度和高度
    private int mWidth;
    private int mHeight;

    //交替显示充电完成的动画
    private boolean show = true;

    //直流电: direct-current （ DC ）
    public static final int DC = 1;

    //交流电:	alternating current ( AC ) （交流电流） AC
    public static final int AC = 2;

    //充电类型，默认为交流
    private int chargeType = DC;

    private int progress = 0;

    private ObjectAnimator animAC;
    private ValueAnimator animatorDC;


    public ChargingProgess(Context context) {
        this(context, null);
    }

    public ChargingProgess(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChargingProgess(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        getSettingValue(attrs);
        initView(context);
    }

    /**
     * 获取在xml中设置的属性值
     *
     * @param attrs
     */
    private void getSettingValue(AttributeSet attrs) {
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.charging_progress);
        oritation = array.getInt(R.styleable.charging_progress_cgv_oritation, HORIZONTAL);
        border_width = array.getDimension(R.styleable.charging_progress_cgv_border_width, DensityUtils.dp2px(mContext, 2));
        item_height = array.getDimension(R.styleable.charging_progress_cgv_item_height, DensityUtils.dp2px(mContext,15));
        item_width = array.getDimension(R.styleable.charging_progress_cgv_item_width, DensityUtils.dp2px(mContext,8));
        item_charging_src = array.getColor(R.styleable.charging_progress_cgv_item_charging_src, 0xff33b5e5);
        item_charging_background = array.getColor(R.styleable.charging_progress_cgv_item_charging_background, 0x4009f7f7);
        background = array.getColor(R.styleable.charging_progress_cgv_background, 0x2009f7f7);
        border_color = array.getColor(R.styleable.charging_progress_cgv_border_color, 0xff33b5e5);
        border_cornor_radius = array.getDimension(R.styleable.charging_progress_cgv_border_cornor_radius, DensityUtils.dp2px(mContext,2));
        duration = array.getInt(R.styleable.charging_progress_cgv_duration, 4 * 1000);
        item_count = array.getInt(R.styleable.charging_progress_cgv_item_count, 4);

    }

    private void initView(Context context) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(border_width);
        mPaint.setColor((border_color));
    }

    /**
     * 当前进度
     *
     * @return
     */
    public int getProgress() {
        return progress % 40;
    }

    /**
     * 设置充电进度
     *
     * @param progress
     */
    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int left = 0;
        int top = mHeight / 4;
        int right = (int) item_height / 2;
        int bottom = 3 * mHeight / 4;

        //顶部的矩形
        RectF topRect = new RectF(mWidth, top, mWidth + right, bottom);
        canvas.drawRoundRect(topRect, border_cornor_radius, border_cornor_radius, mPaint);
        //总的进度背景
        RectF border = new RectF(left, left, mWidth, mHeight);
        canvas.drawRoundRect(border, border_cornor_radius, border_cornor_radius, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor((background));

        //填充顶部矩形的背景
        RectF topRectInner = new RectF(mWidth + left + border_cornor_radius / 2, top + border_cornor_radius / 2, mWidth + right - border_cornor_radius / 2, bottom - border_cornor_radius / 2);
        canvas.drawRect(topRectInner, mPaint);

        //填充总的进度背景
        RectF borderInner = new RectF(left + border_cornor_radius / 2, left + border_cornor_radius / 2, mWidth - border_cornor_radius / 2, mHeight - border_cornor_radius / 2);
        canvas.drawRect(borderInner, mPaint);


        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(border_width);
        mPaint.setColor((border_color));

        //绘制所有的进度
        for (int i = 1; i <= item_count; i++) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor((item_charging_background));
            RectF backRect = new RectF((i + 1) * item_height / 2 + (i - 1) * item_height,
                    mHeight / 4,
                    item_height / 2 + i * (3 * item_height / 2), 3 * mHeight / 4);
            canvas.drawRoundRect(backRect, border_cornor_radius, border_cornor_radius, mPaint);
        }

        //直流动画
        if (chargeType == DC) {
            drawDCAniamtion(canvas);
        } else {
            //交流动画
            drawACAnimaiton(canvas);

        }


        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(border_width);
        mPaint.setColor((border_color));

    }

    /**
     * 关闭动画
     */
    public void closeAnimation() {

        progress = 0;
        invalidate();

        if (animAC != null) {
            animAC.cancel();
        }

        if (animatorDC != null) {
            animatorDC.cancel();
        }
    }


    /**
     * 直流动画
     *
     * @param progress
     */
    public void setDCAnimation(final int progress) {
        chargeType = DC;
        animatorDC = ValueAnimator.ofFloat(0, 1);
        animatorDC.setInterpolator(new LinearInterpolator());
        animatorDC.setDuration(1000);
        animatorDC.setRepeatCount(-1);
        animatorDC.setRepeatMode(ValueAnimator.RESTART);
        animatorDC.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if (value > 0.5) {
                    show = true;
                } else {
                    show = false;
                }
                setProgress(progress);
            }
        });
        animatorDC.start();

    }


    /**
     * 直流动画
     *
     * @param canvas
     */
    private void drawDCAniamtion(Canvas canvas) {
        if(getProgress() == 0){
            return;
        }
        int j = getProgress() / item_count;
        //已经充好的进度
        if(getProgress() != 4){
            for (int i = 1; i <= j; i++) {
                RectF backRect = new RectF((i + 1) * item_height / 2 + (i - 1) * item_height,
                        mHeight / 4,
                        item_height / 2 + i * (3 * item_height / 2), 3 * mHeight / 4);
                canvas.drawRoundRect(backRect, border_cornor_radius, border_cornor_radius, mPaint);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(item_charging_src);
                canvas.drawRoundRect(backRect, border_cornor_radius, border_cornor_radius, mPaint);
            }
        }


        //下一个进度，隐藏和显示交替执行动画
        int i;
        if(getProgress() != 4){
            i = j + 1;
        }else{
            i = 1;
        }

        if (i <= 4) {
            RectF backRect = new RectF((i + 1) * item_height / 2 + (i - 1) * item_height,
                    mHeight / 4,
                    item_height / 2 + i * (3 * item_height / 2), 3 * mHeight / 4);
            mPaint.setStyle(Paint.Style.FILL);
            if (show) {
                mPaint.setColor((item_charging_src));
            } else {
                mPaint.setColor((item_charging_background));
            }
            canvas.drawRoundRect(backRect, border_cornor_radius, border_cornor_radius, mPaint);
        }
    }


    /**
     * 设置交流动画
     */
    public void setACAnimation() {
        chargeType = AC;
        animAC = ObjectAnimator.ofInt(this, "progress", 100);
        animAC.setDuration(10 * 1000);
        animAC.setInterpolator(new LinearInterpolator());
        animAC.setRepeatCount(ValueAnimator.INFINITE);
        animAC.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        animAC.start();
    }


    /**
     * 绘制交流动画
     *
     * @param canvas
     */
    private void drawACAnimaiton(Canvas canvas) {
        int j = getProgress() / item_count;
        //已经充好的进度
        for (int i = 1; i <= (item_count - j); i++) {
            RectF backRect = new RectF(mWidth / 4,
                    (i + 1) * item_height / 2 + (i - 1) * item_height,
                    3 * mWidth / 4,
                    item_height / 2 + i * (3 * item_height / 2));
            canvas.drawRoundRect(backRect, border_cornor_radius, border_cornor_radius, mPaint);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(item_charging_src);
            canvas.drawRoundRect(backRect, border_cornor_radius, border_cornor_radius, mPaint);
        }

    }


    /**
     * 测量view的宽和高，给定默认值，宽300dp，高400dp
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (oritation == VERTICAL) {
            //总间隔数=(item_count+1)  乘以间隔高度（间隔高度等于item_height的一半）
            //总数=item_count 乘以 item_height + 总间隔数 + 顶部一个矩形（高度等于item的高度，宽度等于item的宽度的一半）
            mHeight = (int) (item_count * item_height + (item_count + 1) * item_height / 2 + item_height);
            mWidth = (int) (2 * item_width);
        } else {
            mHeight = (int) (2 * item_width);
            mWidth = (int) (item_count * item_height + (item_count + 1) * item_height / 2 + item_height);
        }
        setMeasuredDimension(mWidth + (int) (item_height / 2), mHeight);
    }

}
