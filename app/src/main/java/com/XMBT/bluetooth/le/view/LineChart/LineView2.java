package com.XMBT.bluetooth.le.view.LineChart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.utils.DensityUtils;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LineView2 extends View {

    private List<ItemBean> mItems = new ArrayList<>();//  折线数据

    /**
     * 默认最小宽度
     */
    private static final int defaultMinWidth = 200;

    /**
     * 默认最小高度
     */
    private static final int defaultMinHeight = 150;

    /**
     * 字体最小默认12sp
     */
    private int mTemperTextSize = 12;

    /**
     * 文字颜色
     */
    private int mTextColor = Color.WHITE;

    /**
     * 折线颜色
     */
    private int mLineColor = Color.parseColor("#b07b5c");

    /**
     * 表格颜色
     */
    private int mGridColor = Color.WHITE;

    /**
     * 线的宽度
     */
    private int mLineWidth = 1;

    /**
     * 表格线的宽度
     */
    private float mGridLineWidth = 0.5f;

    /**
     * 画文字的画笔
     */
    private TextPaint mTextPaint;

    /**
     * 文字的FontMetrics
     */
    private Paint.FontMetrics mTextFontMetrics;

    /**
     * 画折线的画笔
     */
    private Paint mLinePaint;

    /**
     * 画折线的画笔
     */
    private Paint mGridPaint;

    private float xOrigin; //  x轴原点坐标
    private float yOrigin;  //  y轴原点坐标
    private int mWidth; //  控件宽度
    private int mHeight;  //  控件高度
    private int max = 1800;
    private int min = 0;

    /**
     * 新new时的时间
     */
    private long currentTime;
    private long startTime = System.currentTimeMillis();

    private float xInterval;
    private float yInterval;
    /**
     * Y轴的文本
     */
    private int[] textArray = new int[]{18, 15, 12, 9, 6, 3};
    /**
     * X轴的时间间隔 一分钟
     */
    private int timeInterval = 60000;
    private int textHeight;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                invalidate();
            }
        }
    };

    public LineView2(Context context) {
        this(context, null);
    }

    public LineView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyledAttrs(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTemperTextSize);
        mTextPaint.setColor(mTextColor);
        mTextFontMetrics = mTextPaint.getFontMetrics();

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setAntiAlias(true); //去锯齿
        mLinePaint.setColor(mLineColor);

        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setStrokeWidth(mGridLineWidth);
        mGridPaint.setAntiAlias(true); //去锯齿
        mGridPaint.setColor(mGridColor);

        setBackgroundColor(Color.BLACK);
        currentTime = System.currentTimeMillis();
        try {
            //主要是为了获取到整分钟时的毫秒值
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String startLine = sdf.format(new Time(startTime));
            Date date = sdf.parse(startLine);
            startTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取自定义属性并赋初始值
     */
    private void obtainStyledAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LineView,
                defStyleAttr, 0);
        mTemperTextSize = (int) a.getDimension(R.styleable.LineView_temperTextSize,
                DensityUtils.sp2px(context, mTemperTextSize));
        mTextColor = a.getColor(R.styleable.LineView_weatextColor, Color.WHITE);
        mLineWidth = (int) a.getDimension(R.styleable.LineView_lineWidth2,
                DensityUtils.dp2px(context, mLineWidth));
        mGridLineWidth = (int) a.getDimension(R.styleable.LineView_gridLineWidth,
                DensityUtils.dp2px(context, mGridLineWidth));
        a.recycle();
    }

    /**
     * @param changed 表示view有新的尺寸或位置
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * 由于onSizeChanged方法在构造方法、onMeasure之后，又在onDraw之前
     * 此时已经完成全局变量初始化，也得到了控件的宽高
     * 所以可以在这个方法中确定一些与宽高有关的数值
     * 比如这个View的半径啊、padding值等，方便绘制的时候计算大小和位置：
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getWidth();
        mHeight = getHeight();
//			初始化原点坐标
        int textWidth = (int) mTextPaint.measureText("000");
        textHeight = (int) (mTextFontMetrics.bottom - mTextFontMetrics.top);
        xOrigin = textWidth;
        yOrigin = mHeight - textHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = getSize(widthMode, widthSize, 0);
        int height = getSize(heightMode, heightSize, 1);
        setMeasuredDimension(width, height);
    }

    /**
     * @param mode Mode
     * @param size Size
     * @param type 0表示宽度，1表示高度
     * @return 宽度或者高度
     */
    private int getSize(int mode, int size, int type) {
        // 默认
        int result;
        //精确值或match_parent
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            if (type == 0) {
                // 最小不能低于最小的宽度
                result = DensityUtils.dp2px(getContext(), defaultMinWidth) + getPaddingLeft() + getPaddingRight();
            } else {
                // 最小不能小于最小的高度加上一些数据
                int textHeight = (int) (mTextFontMetrics.bottom - mTextFontMetrics.top);
                // 加上2个文字的高度
                result = DensityUtils.dp2px(getContext(), defaultMinHeight) + 2 * textHeight + getPaddingTop() + getPaddingBottom();
            }
            //wrap_content
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAxes(canvas);
        drawLine(canvas);
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    private void drawLine(Canvas canvas) {
        //  Y轴坐标间距
        yInterval = (max - min) / yOrigin;
        //  X轴坐标间距  每毫秒的间距  总长6s
        xInterval = (mWidth - xOrigin) / 6 / 1000;

        Path mPath = new Path();

        for (int i = 0; i < mItems.size(); i++) {
            float x = (mItems.get(i).getMsec() - mItems.get(0).getMsec()) * xInterval + xOrigin;
            ;
            if (i == 0) {
                mPath.moveTo(x, yOrigin - (mItems.get(i).getValue() - min) / yInterval);
            } else {
                mPath.lineTo(x, yOrigin - (mItems.get(i).getValue() - min) / yInterval);
            }
        }
        canvas.drawPath(mPath, mLinePaint);
    }

    private void drawAxes(Canvas canvas) {
        int mMargin10 = DensityUtils.dp2px(getContext(), 10);
        //画横线
        int[] yTextWidthArray = new int[textArray.length];
        for (int i = 0; i <= textArray.length; i++) {
            canvas.drawLine(xOrigin, yOrigin / textArray.length * i, mWidth - mMargin10, yOrigin / textArray.length * i, mGridPaint);
            if (i != textArray.length) {
                //Y轴上的文字
                yTextWidthArray[i] = (int) mTextPaint.measureText(textArray[i] + "0");
                if (i == 0) {
                    canvas.drawText(textArray[i] + "", xOrigin - yTextWidthArray[i], yOrigin / textArray.length * i + textHeight, mTextPaint);
                } else {
                    canvas.drawText(textArray[i] + "", xOrigin - yTextWidthArray[i], yOrigin / textArray.length * i + textHeight / 3, mTextPaint);
                }
            }
        }

        String[] timeArray = new String[7];
        //画竖线
        for (int i = 0; i < timeArray.length; i++) {
            canvas.drawLine((mWidth - xOrigin - mMargin10) / (timeArray.length - 1) * i + xOrigin, yOrigin, (mWidth - xOrigin - mMargin10) / (timeArray.length - 1) * i + xOrigin, 0, mGridPaint);
            timeArray[i] = i + "s";
            //X轴上的文字
            int textWidth = (int) mTextPaint.measureText(timeArray[i]);
            if (i == timeArray.length - 1) {
                canvas.drawText(timeArray[i], (mWidth - xOrigin - mMargin10) / (timeArray.length - 1) * i + xOrigin - textWidth, mHeight, mTextPaint);
            } else {
                canvas.drawText(timeArray[i], (mWidth - xOrigin - mMargin10) / (timeArray.length - 1) * i + xOrigin - textWidth / 2, mHeight, mTextPaint);
            }
        }

    }

    /**
     * 设置坐标数据
     *
     * @param items
     */
    public void setItems(List<ItemBean> items) {
        mItems = items;
    }

}
