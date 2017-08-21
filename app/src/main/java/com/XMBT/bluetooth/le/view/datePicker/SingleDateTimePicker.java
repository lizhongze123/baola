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


public class SingleDateTimePicker extends WheelPicker {
    /**
     * 年月日
     */
    public static final int YEAR_MONTH_DAY = 0;
    /**
     * 年月
     */
    public static final int YEAR_MONTH = 1;
    /**
     * 月日
     */
    public static final int MONTH_DAY = 2;

    /**
     * 24小时
     */
    public static final int HOUR_OF_DAY = 3;
    /**
     * 12小时
     */
    public static final int HOUR = 4;

    private ArrayList<String> years = new ArrayList<String>();
    private ArrayList<String> months = new ArrayList<String>();
    private ArrayList<String> days = new ArrayList<String>();
    //
    private ArrayList<String> secYears = new ArrayList<String>();
    private ArrayList<String> secMonths = new ArrayList<String>();
    private ArrayList<String> secDays = new ArrayList<String>();

    private String yearLabel = "年", monthLabel = "月", dayLabel = "日";
    private int selectedYearIndex = 0, selectedMonthIndex = 0,
            selectedDayIndex = 0, selectedSecYearIndex = 0,
            selectedSecMonthIndex = 0, selectedSecDayIndex = 0;
    private String hourLabel = "时", minuteLabel = "分";
    private String selectedHour = "", selectedMinute = "",
            selectedSecHour = "", selectedSecMinute = "";
    private OnDateTimePickListener onDateTimePickListener;
    private int mode;

    @IntDef(flag = false, value = {YEAR_MONTH_DAY, YEAR_MONTH, MONTH_DAY,
            HOUR_OF_DAY, HOUR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public SingleDateTimePicker(Activity activity, @Mode int mode) {
        super(activity);
        textSize = 16;// 年月日时分，比较宽，设置字体小一点才能显示完整
        Calendar calendar = Calendar.getInstance();
        this.mode = mode;
        for (int i = 2000; i <= 2050; i++) {
            years.add(String.valueOf(i));
            secYears.add(String.valueOf(i));
        }
        for (int i = 1; i <= 12; i++) {
            months.add(MyDateUtils.fillZero(i));
            secMonths.add(MyDateUtils.fillZero(i));
        }
        for (int i = 1; i <= 31; i++) {
            days.add(MyDateUtils.fillZero(i));
            secDays.add(MyDateUtils.fillZero(i));
        }
        selectedHour = MyDateUtils.fillZero(calendar.get(Calendar.HOUR_OF_DAY));
        selectedMinute = MyDateUtils.fillZero(calendar.get(Calendar.MINUTE));
        selectedSecHour = MyDateUtils
                .fillZero(calendar.get(Calendar.HOUR_OF_DAY));
        selectedSecMinute = MyDateUtils.fillZero(calendar.get(Calendar.MINUTE));
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

        WheelView yearView = new WheelView(activity.getBaseContext());
        yearView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        yearView.setTextSize(textSize);
        yearView.setTextColor(textColorNormal, textColorFocus);
        yearView.setLineVisible(lineVisible);
        yearView.setLineColor(lineColor);
        yearView.setOffset(offset);
        layout.addView(yearView);
        TextView yearTextView = new TextView(activity);
        yearTextView.setLayoutParams(new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT));
        yearTextView.setTextSize(textSize);
        yearTextView.setTextColor(textColorFocus);
        if (!TextUtils.isEmpty(yearLabel)) {
            yearTextView.setText(yearLabel);
        }
        layout.addView(yearTextView);

        WheelView monthView = new WheelView(activity.getBaseContext());
        monthView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        monthView.setTextSize(textSize);
        monthView.setTextColor(textColorNormal, textColorFocus);
        monthView.setLineVisible(lineVisible);
        monthView.setLineColor(lineColor);
        monthView.setOffset(offset);
        layout.addView(monthView);
        TextView monthTextView = new TextView(activity);
        monthTextView.setLayoutParams(new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT));
        monthTextView.setTextSize(textSize);
        monthTextView.setTextColor(textColorFocus);
        if (!TextUtils.isEmpty(monthLabel)) {
            monthTextView.setText(monthLabel);
        }
        layout.addView(monthTextView);

        final WheelView dayView = new WheelView(activity.getBaseContext());
        dayView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        dayView.setTextSize(textSize);
        dayView.setTextColor(textColorNormal, textColorFocus);
        dayView.setLineVisible(lineVisible);
        dayView.setLineColor(lineColor);
        dayView.setOffset(offset);
        layout.addView(dayView);
        TextView dayTextView = new TextView(activity);
        dayTextView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,
                WRAP_CONTENT));
        dayTextView.setTextSize(textSize);
        dayTextView.setTextColor(textColorFocus);
        if (!TextUtils.isEmpty(dayLabel)) {
            dayTextView.setText(dayLabel);
        }
        layout.addView(dayTextView);

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

        if (mode == YEAR_MONTH) {
            dayView.setVisibility(View.GONE);
            dayTextView.setVisibility(View.GONE);
        } else if (mode == MONTH_DAY) {
            yearView.setVisibility(View.GONE);
            yearTextView.setVisibility(View.GONE);
        }
        if (mode != MONTH_DAY) {
            if (!TextUtils.isEmpty(yearLabel)) {
                yearTextView.setText(yearLabel);
            }
            if (selectedYearIndex == 0) {
                yearView.setItems(years);
            } else {
                yearView.setItems(years, selectedYearIndex);
            }
            yearView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(boolean isUserScroll, int selectedIndex,
                                       String item) {
                    selectedYearIndex = selectedIndex;
                    // 需要根据年份及月份动态计算天数
                    days.clear();
                    int maxDays = MyDateUtils
                            .calculateDaysInMonth(stringToYearMonthDay(item),
                                    stringToYearMonthDay(months
                                            .get(selectedMonthIndex)));
                    for (int i = 1; i <= maxDays; i++) {
                        days.add(MyDateUtils.fillZero(i));
                    }
                    if (selectedDayIndex >= maxDays) {
                        // 年或月变动时，保持之前选择的日不动：如果之前选择的日是之前年月的最大日，则日自动为该年月的最大日
                        selectedDayIndex = days.size() - 1;
                    }
                    dayView.setItems(days, selectedDayIndex);
                }
            });
        }
        if (!TextUtils.isEmpty(monthLabel)) {
            monthTextView.setText(monthLabel);
        }
        if (selectedMonthIndex == 0) {
            monthView.setItems(months);
        } else {
            monthView.setItems(months, selectedMonthIndex);
        }
        monthView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex,
                                   String item) {
                selectedMonthIndex = selectedIndex;
                if (mode != YEAR_MONTH) {
                    // 年月日或年月模式下，需要根据年份及月份动态计算天数
                    days.clear();
                    int maxDays = MyDateUtils.calculateDaysInMonth(
                            stringToYearMonthDay(years.get(selectedYearIndex)),
                            stringToYearMonthDay(item));
                    for (int i = 1; i <= maxDays; i++) {
                        days.add(MyDateUtils.fillZero(i));
                    }
                    if (selectedDayIndex >= maxDays) {
                        // 年或月变动时，保持之前选择的日不动：如果之前选择的日是之前年月的最大日，则日自动为该年月的最大日
                        selectedDayIndex = days.size() - 1;
                    }
                    dayView.setItems(days, selectedDayIndex);
                }
            }
        });
        if (mode != YEAR_MONTH) {
            if (!TextUtils.isEmpty(dayLabel)) {
                dayTextView.setText(dayLabel);
            }
            if (selectedDayIndex == 0) {
                dayView.setItems(days);
            } else {
                dayView.setItems(days, selectedDayIndex);
            }
            dayView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(boolean isUserScroll, int selectedIndex,
                                       String item) {
                    selectedDayIndex = selectedIndex;
                }
            });
        }

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
            String year = getSelectedYear();
            String month = getSelectedMonth();
            String day = getSelectedDay();
            String secYear = getSelectedSecYear();
            String secMonth = getSelectedSecMonth();
            String secDay = getSelectedSecDay();
            switch (mode) {
                case YEAR_MONTH:
                    ((OnYearMonthPickListener) onDateTimePickListener)
                            .onDateTimePicked(year, month, selectedHour,
                                    selectedMinute);
                    break;
                case MONTH_DAY:
                    ((OnMonthDayPickListener) onDateTimePickListener)
                            .onDateTimePicked(month, day, selectedHour,
                                    selectedMinute);
                    break;
                case YEAR_MONTH_DAY:
                    ((OnYearMonthDayTimePickListener)onDateTimePickListener).onDateTimePicked(year,month,day,selectedHour,selectedMinute);
                    break;
                default:
                    // ((OnYearMonthDayTimePickListener)
                    // onDateTimePickListener).onDateTimePicked(year, month,
                    // day, selectedHour, selectedMinute);
                    ((OnYearMonthDayTimePickListener) onDateTimePickListener)
                            .onDateTimePicked(year, month, day, selectedHour,
                                    selectedMinute);
                    break;
            }
        }
    }

    /**
     * Gets selected year.
     *
     * @return the selected year
     */
    public String getSelectedYear() {
        return years.get(selectedYearIndex);
    }

    /**
     * Gets selected month.
     *
     * @return the selected month
     */
    public String getSelectedMonth() {
        return months.get(selectedMonthIndex);
    }

    /**
     * Gets selected day.
     *
     * @return the selected day
     */
    public String getSelectedDay() {
        return days.get(selectedDayIndex);
    }

    // second 时间选择器

    /**
     * Gets selected year.
     *
     * @return the selected year
     */
    public String getSelectedSecYear() {
        return secYears.get(selectedSecYearIndex);
    }

    /**
     * Gets selected month.
     *
     * @return the selected month
     */
    public String getSelectedSecMonth() {
        return secMonths.get(selectedSecMonthIndex);
    }

    /**
     * Gets selected day.
     *
     * @return the selected day
     */
    public String getSelectedSecDay() {
        return secDays.get(selectedSecDayIndex);
    }

    private int stringToYearMonthDay(String text) {
        if (text.startsWith("0")) {
            // 截取掉前缀0以便转换为整数
            text = text.substring(1);
        }
        return Integer.parseInt(text);
    }

    /**
     * Sets label.
     *
     * @param yearLabel  the year label
     * @param monthLabel the month label
     * @param dayLabel   the day label
     */
    public void setLabel(String yearLabel, String monthLabel, String dayLabel,
                         String hourLabel, String minuteLabel) {
        this.yearLabel = yearLabel;
        this.monthLabel = monthLabel;
        this.dayLabel = dayLabel;
        this.hourLabel = hourLabel;
        this.minuteLabel = minuteLabel;
    }

    /**
     * Sets range.
     *
     * @param startYear the start year
     * @param endYear   the end year
     */
    public void setRange(int startYear, int endYear) {
        years.clear();
        for (int i = startYear; i <= endYear; i++) {
            years.add(String.valueOf(i));
        }
        secYears.clear();
        for (int i = startYear; i <= endYear; i++) {
            secYears.add(String.valueOf(i));
        }
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
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     */
    public void setSelectedItem(int year, int month, int day, int hour,
                                int minute) {
        selectedYearIndex = findItemIndex(years, year);
        selectedMonthIndex = findItemIndex(months, month);
        selectedDayIndex = findItemIndex(days, day);
        selectedHour = String.valueOf(hour);
        selectedMinute = String.valueOf(minute);
        // second
        selectedSecYearIndex = findItemIndex(secYears, year);
        selectedSecMonthIndex = findItemIndex(secMonths, month);
        selectedSecDayIndex = findItemIndex(secDays, day);
        selectedSecHour = String.valueOf(hour);
        selectedSecMinute = String.valueOf(minute);
    }

    /**
     * Sets selected item.
     *
     * @param yearOrMonth the year or month
     * @param monthOrDay  the month or day
     */
    public void setSelectedItem(int yearOrMonth, int monthOrDay) {
        if (mode == MONTH_DAY) {
            selectedMonthIndex = findItemIndex(months, yearOrMonth);
            selectedDayIndex = findItemIndex(days, monthOrDay);
        } else {
            selectedYearIndex = findItemIndex(years, yearOrMonth);
            selectedMonthIndex = findItemIndex(months, monthOrDay);
        }
    }

    /**
     * The interface On DateTime pick listener.
     */
    protected interface OnDateTimePickListener {

    }

    /**
     * The interface On year month day pick listener.
     */
    public interface OnYearMonthDayTimePickListener extends
            OnDateTimePickListener {

        /**
         * On date picked.
         *
         * @param year  the year
         * @param month the month
         * @param day   the day
         */
        void onDateTimePicked(String year, String month, String day,
                              String hour, String minute);

    }

    /**
     * The interface On year month pick listener.
     */
    public interface OnYearMonthPickListener extends OnDateTimePickListener {

        /**
         * On date picked.
         *
         * @param year  the year
         * @param month the month
         */
        void onDateTimePicked(String year, String month, String hour,
                              String minute);

    }

    /**
     * The interface On month day pick listener.
     */
    public interface OnMonthDayPickListener extends OnDateTimePickListener {

        /**
         * On date picked.
         *
         * @param month the month
         * @param day   the day
         */
        void onDateTimePicked(String month, String day, String hour,
                              String minute);

    }

    public void setOnDateTimePickListener(OnDateTimePickListener listener) {
        this.onDateTimePickListener = listener;
    }

}
