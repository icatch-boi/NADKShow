package com.icatchtek.nadk.show.utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.DatePicker;



import java.util.Calendar;


/**
 * 时间、日期选择器
 */
public class DatePickerHelper {
    /**
     * 日期选择器的回调
     * 格式 年-月-日
     */
    public interface OnDateSelectedBlock {
        void onDateSelected(int year, int month, int dayOfMonth);
    }


    /**
     * 时间选择器的回调
     * 格式 时-分
     */
    public interface OnTimeSelectedBlock {
        void onTimeSelected(int hour, int min, int seconds);
    }

    public interface OnDateTimeSelectedBlock {
        void onDateSelected(int year, int month, int dayOfMonth, int hour, int min, int seconds);
    }

    public static void showDateTimePickerDialog(final Context context, Calendar calendar, final OnDateTimeSelectedBlock block) {
        int themeResId = 3;
        showDatePickerDialog(context, themeResId, calendar, new OnDateSelectedBlock() {
            @Override
            public void onDateSelected(int year, int month, int dayOfMonth) {
                showMyTimePickerDialog(context, themeResId, calendar, new OnTimeSelectedBlock() {
                    @Override
                    public void onTimeSelected(int hour, int min, int seconds) {
                        block.onDateSelected(year, month, dayOfMonth, hour, min, seconds);
                    }
                });
            }
        });

//        showMyTimePickerDialog(context, themeResId, calendar, new OnTimeSelectedBlock() {
//            @Override
//            public void onTimeSelected(int hour, int min, int seconds) {
//                block.onDateSelected(2023, 7, 17, hour, min, seconds);
//            }
//        });
    }


    /**
     * 日期选择 年-月-日
     *
     * @param context
     * @param themeResId
     * @param calendar
     */
    public static void showDatePickerDialog(final Context context, int themeResId, Calendar calendar, final OnDateSelectedBlock block) {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(context, themeResId, new DatePickerDialog.OnDateSetListener() {
            // 绑定监听器(How the parent is notified that the date is set.)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 此处得到选择的时间，可以进行你想要的操作
                block.onDateSelected(year, monthOfYear, dayOfMonth);
            }
        }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH)
                , calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }


    /**
     * 时间选择 时-分
     *
     * @param activity
     * @param themeResId
     * @param calendar
     */
    public static void showTimePickerDialog(Activity activity, int themeResId, Calendar calendar, final OnTimeSelectedBlock block) {
        // Calendar c = Calendar.getInstance();
        // 创建一个TimePickerDialog实例，并把它显示出来
        // 解释一哈，Activity是context的子类
        new TimePickerDialog(activity, themeResId,
                // 绑定监听器
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
                        block.onTimeSelected(hourOfDay, minute, 0);
                    }
                }
                // 设置初始时间
                , calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE)
                // true表示采用24小时制
                , true).show();
    }


    /**
     * 时间选择 时-分-秒
     *
     * @param context
     * @param themeResId
     * @param calendar
     */
    public static void showMyTimePickerDialog(Context context, int themeResId, Calendar calendar, final OnTimeSelectedBlock block) {
        // Calendar c = Calendar.getInstance();
        // 创建一个TimePickerDialog实例，并把它显示出来
        // 解释一哈，Activity是context的子类
        new MyTimePickerDialog(context, themeResId,
                // 绑定监听器
                new MyTimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
                        block.onTimeSelected(hourOfDay, minute, seconds);
                    }
                }
                // 设置初始时间
                , calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE)
                , calendar.get(Calendar.SECOND)
                // true表示采用24小时制
                , true).show();
    }
}