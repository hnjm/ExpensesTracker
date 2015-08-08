package com.amilabs.android.expensestracker.fragments;

import com.amilabs.android.expensestracker.interfaces.Constants;
import com.amilabs.android.expensestracker.interfaces.OnDateSelectedListener;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements OnDateSetListener, Constants {

    OnDateSelectedListener mListener;
    private int year;
    private int month;
    private int day;
    private boolean isFromBtn;
    private TrackerType trackerType;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDateSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDateSelectedListener");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        final Calendar c = Calendar.getInstance();
        Bundle bundle = getArguments();
        c.setTime(Utils.getDate(bundle.getString("date")));
        String type = bundle.getString("trackerType");
        if (type.equals("NONE"))
            trackerType = TrackerType.NONE;
        else if (type.equals("PLANNER"))
            trackerType = TrackerType.PLANNER;
        else if (type.equals("DISTRIBUTION"))
            trackerType = TrackerType.DISTRIBUTION;
        isFromBtn = bundle.getBoolean("isFromBtn");
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    mListener.onDialogDestroyed(null);
                    dialog.dismiss();
                }
            }
        });
        return dialog;
    }
    
    @Override
    public void onCancel(DialogInterface dialog) {
        mListener.onDialogDestroyed(null);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
    	this.year = year;
    	this.month = month;
    	this.day = day;
    	Calendar c = Calendar.getInstance();
        int todayYear = c.get(Calendar.YEAR);
        int todayMonth = c.get(Calendar.MONTH);
        int todayDay = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, this.year);
        long date = c.getTime().getTime();
        String error = null;
        if (trackerType != TrackerType.NONE) { // planner/distribution fragment
            if (isFromBtn) {
                long dateTo = SharedPref.getDateTo(view.getContext(), trackerType);
                int yearTo = Utils.get(dateTo, Calendar.YEAR);
                int monthTo = Utils.get(dateTo, Calendar.MONTH);
                int dayTo = Utils.get(dateTo, Calendar.DAY_OF_MONTH);
                if (year > yearTo || month > monthTo ||
                        (day >= dayTo && month == monthTo && year == yearTo)) {
                    error = "Start date should be less than end date";
                    date = SharedPref.getDateFrom(view.getContext(), trackerType);
                } else if (year > todayYear || month > todayMonth ||
                        (day > todayDay && month == todayMonth && year == todayYear)) {
                    error = "Start date cannot be bigger than today date";
                    date = SharedPref.getDateFrom(view.getContext(), trackerType);
                }
            } else {
                long dateFrom = SharedPref.getDateFrom(view.getContext(), trackerType);
                int yearFrom = Utils.get(dateFrom, Calendar.YEAR);
                int monthFrom = Utils.get(dateFrom, Calendar.MONTH);
                int dayFrom = Utils.get(dateFrom, Calendar.DAY_OF_MONTH);
                if (year < yearFrom || month < monthFrom ||
                        (day <= dayFrom && month == monthFrom && year == yearFrom)) {
                    date = SharedPref.getDateTo(view.getContext(), trackerType);
                    error = "End date should be bigger than start date";
                } else if (year > todayYear || month > todayMonth ||
                        (day > todayDay && month == todayMonth && year == todayYear)) {
                    error = "End date cannot be bigger than today date";
                    date = SharedPref.getDateTo(view.getContext(), trackerType);
                }
            }
        } else { // add expense dialog
            if (year > todayYear || month > todayMonth ||
                    (day > todayDay && month == todayMonth && year == todayYear))
                error = "Expense date cannot be bigger than today date";
        }
    	mListener.onDateSelected(date);
        mListener.onDialogDestroyed(error);
    }
}
