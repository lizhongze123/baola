package com.XMBT.bluetooth.le.ui.gbattery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.drawable.PaintDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.utils.DateFormatUtils;
import com.XMBT.bluetooth.le.utils.DensityUtils;
import com.XMBT.bluetooth.le.view.datePicker.DateTimePicker;

import java.util.Calendar;

@SuppressLint("NewApi")
public class TimePopuWindow implements OnClickListener {

    public static final int BUTTON_TYPE_OK = 6;
    public static final int BUTTON_TYPE_CUSTOM = 5;
    public static final int BUTTON_TYPE_ALL = 4;
    public static final int BUTTON_TYPE_TDBY = 3;
    public static final int BUTTON_TYPE_YESTERDAY = 2;
    public static final int BUTTON_TYPE_TODAY = 1;

    private Activity context;
    private LayoutInflater inflater;
    private PopupWindow popupWindow;
    private View popupWindow_view;
    private TextView tvStartTime, tvEndTime;
    private LinearLayout llCusomTime;
    private int mHour, mMinute, mYear, mMonth, mDay;
    View v;

    public TimePopuWindow(Context context ,View v) {
        this.context = (Activity) context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.v = v;
        initPopuptWindow();
    }

    /**
     * 创建PopupWindow
     */
    protected void initPopuptWindow() {
        // 获取自定义布局文件
        popupWindow_view = inflater.inflate(R.layout.time_popupwindow, null, false);

        int[] arrayOfInt = DensityUtils.getScreenWH2(context);
        int w = (int) (arrayOfInt[0] * 0.9D);

        popupWindow = new PopupWindow(popupWindow_view, w, LayoutParams.WRAP_CONTENT);
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);// 取得焦点,不然拦截不到返回键
        // 点击其他地方和返回键消失,数值参数为透明色
        popupWindow.setBackgroundDrawable(new PaintDrawable(0x00000000));
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });


        popupWindow_view.findViewById(R.id.btn_custom).setOnClickListener(this);
        popupWindow_view.findViewById(R.id.btn_tdby).setOnClickListener(this);
        popupWindow_view.findViewById(R.id.btn_yesterday).setOnClickListener(this);
        popupWindow_view.findViewById(R.id.btn_today).setOnClickListener(this);
        popupWindow_view.findViewById(R.id.btn_All).setOnClickListener(this);
        popupWindow_view.findViewById(R.id.btn_ok).setOnClickListener(this);
        popupWindow_view.findViewById(R.id.btn_hour).setOnClickListener(this);

        llCusomTime = (LinearLayout) popupWindow_view.findViewById(R.id.ll_customTime);

        tvStartTime = (TextView) popupWindow_view.findViewById(R.id.tv_startTime);
        tvStartTime.setText(DateFormatUtils.getBeforeDayStart(3, DateFormatUtils.DATE));
        tvStartTime.setOnClickListener(this);
        tvEndTime = (TextView) popupWindow_view.findViewById(R.id.tv_endTime);
        tvEndTime.setOnClickListener(this);
        tvEndTime.setText(DateFormatUtils.getBeforeDayStart(0, DateFormatUtils.DATE));
    }

    /**
     * 显示
     */
    public void showPopupWindow(View view) {
        if (!popupWindow.isShowing()) {
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        }
    }

    private OnTimeSelectListener listener;

    public void dismiss() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public interface OnTimeSelectListener {
        void onSelect(int type, String startTime, String endTime);

    }

    public void setOnTimeSelectListener(OnTimeSelectListener listener) {
        this.listener = listener;
    }

    private Calendar c = Calendar.getInstance();
    String startTime = "";
    String endTime = "";
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_today:
                startTime = DateFormatUtils.getBeforeDayStart(0, DateFormatUtils.MILLISECOND);
                endTime = DateFormatUtils.getDate() + "";
                this.listener.onSelect(BUTTON_TYPE_TODAY, startTime, endTime);
                break;
            case R.id.btn_yesterday:
                startTime = DateFormatUtils.getBeforeDayStart(1, DateFormatUtils.MILLISECOND);
                endTime = DateFormatUtils.getBeforeDayEnd(1, DateFormatUtils.MILLISECOND);
                this.listener.onSelect(BUTTON_TYPE_YESTERDAY, startTime, endTime);
                break;
            case R.id.btn_tdby:
                startTime = DateFormatUtils.getBeforeDayStart(2, DateFormatUtils.MILLISECOND);
                endTime = DateFormatUtils.getBeforeDayEnd(2, DateFormatUtils.MILLISECOND);
                this.listener.onSelect(BUTTON_TYPE_TDBY, startTime, endTime);
                break;
            case R.id.btn_hour:
                startTime = DateFormatUtils.getBeforeHour(1, DateFormatUtils.MILLISECOND);
                endTime = DateFormatUtils.getDate() + "";
                this.listener.onSelect(BUTTON_TYPE_TDBY, startTime, endTime);
                break;
            case R.id.btn_All:
                this.listener.onSelect(BUTTON_TYPE_ALL, startTime, endTime);
                break;
            case R.id.btn_ok:
                startTime = DateFormatUtils.getTimeMills(tvStartTime.getText().toString()) + "";
                endTime = DateFormatUtils.getTimeMills(tvEndTime.getText().toString()) + "";
                this.listener.onSelect(BUTTON_TYPE_OK, startTime, endTime);
                break;
            case R.id.btn_custom:
//                if (llCusomTime.getVisibility() == View.GONE) {
//                    llCusomTime.setVisibility(View.VISIBLE);
//                } else {
//                    llCusomTime.setVisibility(View.GONE);
//                }
                showDateTimeDialog();
                break;
            case R.id.tv_startTime:
                selectTime(0);
                break;
            case R.id.tv_endTime:
                selectTime(1);
                break;
        }
    }

    private void showDateTimeDialog() {
        Calendar calendar = Calendar.getInstance();
        DateTimePicker dateTimePicker = new DateTimePicker(context, DateTimePicker.HOUR_OF_DAY);
        dateTimePicker.setRange(calendar.get(Calendar.YEAR) - 10, calendar.get(Calendar.YEAR));
        dateTimePicker.setSelectedItem(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        dateTimePicker.setOnDateTimePickListener(new DateTimePicker.OnTwoYearMonthDayTimePickListener() {

            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute, String secYear, String secMonth,
                                         String secDay, String secHour, String secMinute) {
                    String beginTime = year + "-" + month + "-" + day + " " + hour + ":" + minute ;
                    String overTime = secYear + "-" + secMonth + "-" + secDay + " " + secHour + ":" + secMinute ;

                    startTime = DateFormatUtils.getTimeMills(beginTime) + "";
                    endTime = DateFormatUtils.getTimeMills(overTime) + "";
                    listener.onSelect(BUTTON_TYPE_OK, startTime, endTime);

            }
        });
        dateTimePicker.show();
    }


    /**
     * @param type 0为开始时间
     */
    private void selectTime(final int type) {
        //日期选择器
        int year = c.get(Calendar.YEAR);
        int monthOfYear = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                mYear = year;
                mMonth = monthOfYear + 1;
                mDay = dayOfMonth;

                //时间选择器
                int h = c.get(Calendar.HOUR_OF_DAY);
                int m = c.get(Calendar.MINUTE);
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {

                        mHour = hour;
                        mMinute = minute;
                        if (type == 0) {
                            tvStartTime.setText(mYear + (mMonth < 10 ? "-0" + mMonth : "-" + mMonth) + (mDay < 10 ? "-0" + mDay : "-" + mDay) + (mHour < 10 ? " 0" + mHour : " " + mHour) + (mMinute < 10 ? "-0" + mMinute : ":" + mMinute));
                        } else {
                            tvEndTime.setText(mYear + (mMonth < 10 ? "-0" + mMonth : "-" + mMonth) + (mDay < 10 ? "-0" + mDay : "-" + mDay) + (mHour < 10 ? " 0" + mHour : " " + mHour) + (mMinute < 10 ? "-0" + mMinute : ":" + mMinute));
                        }
                    }
                }, h, m, true).show();
            }
        }, year, monthOfYear, dayOfMonth);
        datePickerDialog.show();
    }

}
