package com.XMBT.bluetooth.le.view.datePicker;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.XMBT.bluetooth.le.view.datePicker.utils.ConvertUtils;
import com.XMBT.bluetooth.le.view.datePicker.utils.MyDateUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


/**
 * 时分选择器
 */
public class SingleTimePicker extends WheelPicker {
    /**
     * 24小时
     */
    public static final int HOUR_OF_DAY = 3;
    /**
     * 12小时
     */
    public static final int HOUR = 4;

    private String hourLabel = "时", minuteLabel = "分";
    private String selectedHour = "", selectedMinute = "";
    private OnDateTimePickListener onDateTimePickListener;
    private int mode;

    @IntDef(flag = false, value = {
            HOUR_OF_DAY, HOUR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public SingleTimePicker(Activity activity, @Mode int mode) {
        super(activity);
        textSize = 16;// 年月日时分，比较宽，设置字体小一点才能显示完整
        Calendar calendar = Calendar.getInstance();
        this.mode = mode;
        selectedHour = MyDateUtils.fillZero(calendar.get(Calendar.HOUR_OF_DAY));
        selectedMinute = MyDateUtils.fillZero(calendar.get(Calendar.MINUTE));
    }

    @NonNull
    @Override
    protected View makeCenterView() {
        // 最外层layout
        LinearLayout layoutOutermost = new LinearLayout(activity);
        layoutOutermost.setOrientation(LinearLayout.VERTICAL);
        layoutOutermost.setPadding(ConvertUtils.toPx(0f), ConvertUtils.toPx(20.0f),
                ConvertUtils.toPx(activity, 0f), ConvertUtils.toPx(activity, 20.0f));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.setMargins(ConvertUtils.toPx(0f), ConvertUtils.toPx(30.0f),
                ConvertUtils.toPx(activity, 0f), ConvertUtils.toPx(activity, 30.0f));

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);

        WheelView hourView = new WheelView(activity);
        hourView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        hourView.setTextSize(textSize);
        hourView.setTextColor(textColorNormal, textColorFocus);
        hourView.setLineVisible(lineVisible);
        hourView.setLineColor(lineColor);
        layout.addView(hourView);
        TextView hourTextView = new TextView(activity);
        hourTextView.setLayoutParams(new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT));
        hourTextView.setTextSize(textSize);
        hourTextView.setTextColor(textColorFocus);
        if (!TextUtils.isEmpty(hourLabel)) {
            hourTextView.setText(hourLabel);
        }
        layout.addView(hourTextView);

        WheelView minuteView = new WheelView(activity);
        minuteView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        minuteView.setTextSize(textSize);
        minuteView.setTextColor(textColorNormal, textColorFocus);
        minuteView.setLineVisible(lineVisible);
        minuteView.setLineColor(lineColor);
        minuteView.setOffset(offset);
        layout.addView(minuteView);

        TextView minuteTextView = new TextView(activity);
        minuteTextView.setLayoutParams(new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT));
        minuteTextView.setTextSize(textSize);
        minuteTextView.setTextColor(textColorFocus);
        if (!TextUtils.isEmpty(minuteLabel)) {
            minuteTextView.setText(minuteLabel);
        }
        layout.addView(minuteTextView);

        ArrayList<String> hours = new ArrayList<String>();
        if (mode == HOUR) {
            for (int i = 1; i <= 12; i++) {
                hours.add(MyDateUtils.fillZero(i));
            }
        } else {
            for (int i = 0; i < 24; i++) {
                hours.add(MyDateUtils.fillZero(i));
            }
        }
        hourView.setItems(hours, selectedHour);
        ArrayList<String> minutes = new ArrayList<String>();
        for (int i = 0; i < 60; i++) {
            minutes.add(MyDateUtils.fillZero(i));
        }
        minuteView.setItems(minutes, selectedMinute);
        hourView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex,
                                   String item) {
                selectedHour = item;
            }
        });
        minuteView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex,
                                   String item) {
                selectedMinute = item;
            }
        });
        layoutOutermost.addView(layout);
        return layoutOutermost;
    }

    @Override
    protected void onSubmit() {
        if (onDateTimePickListener != null) {
            switch (mode) {
                default:
                    if(onDateTimePickListener instanceof OnHourMinutePickListener) {
                        ((OnHourMinutePickListener) onDateTimePickListener)
                                .onHourMinutePick(selectedHour, selectedMinute);
                    }
                    break;
            }
        }
    }

    /**
     * Sets label.
     *
     */
    public void setLabel( String hourLabel, String minuteLabel) {
        this.hourLabel = hourLabel;
        this.minuteLabel = minuteLabel;
    }

    private int findItemIndex(ArrayList<String> items, int item) {
        // 折半查找有序元素的索引
        int index = Collections.binarySearch(items, item,
                new Comparator<Object>() {
                    @Override
                    public int compare(Object lhs, Object rhs) {
                        String lhsStr = lhs.toString();
                        String rhsStr = rhs.toString();
                        lhsStr = lhsStr.startsWith("0") ? lhsStr.substring(1)
                                : lhsStr;
                        rhsStr = rhsStr.startsWith("0") ? rhsStr.substring(1)
                                : rhsStr;
                        return Integer.parseInt(lhsStr)
                                - Integer.parseInt(rhsStr);
                    }
                });
        if (index < 0) {
            index = 0;
        }
        return index;
    }

    /**
     * @param hour
     * @param minute
     */
    public void setSelectedItem(int hour, int minute) {
        selectedHour = String.valueOf(hour);
        selectedMinute = String.valueOf(minute);
    }

    /**
     * The interface On DateTime pick listener.
     */
    protected interface OnDateTimePickListener {

    }

    public static interface OnHourMinutePickListener extends OnDateTimePickListener{
        void onHourMinutePick(String hour, String minute);
    }

    public void setOnDateTimePickListener(OnDateTimePickListener listener) {
        this.onDateTimePickListener = listener;
    }

}
